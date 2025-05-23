    package org.unisoftware.gestioncurricular.service.files;

    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;
    import org.unisoftware.gestioncurricular.config.BucketsConfig;
    import org.unisoftware.gestioncurricular.config.SupabaseProperties;
    import org.unisoftware.gestioncurricular.dto.files.ProgramFileDTO;
    import org.unisoftware.gestioncurricular.dto.files.UpdateProgramFileDTO;
    import org.unisoftware.gestioncurricular.entity.Program;
    import org.unisoftware.gestioncurricular.entity.files.ProgramaCurriculumFile;
    import org.unisoftware.gestioncurricular.entity.files.ProgramaResultadosFile;
    import org.unisoftware.gestioncurricular.repository.ProgramRepository;
    import org.unisoftware.gestioncurricular.repository.files.ProgramaCurriculumFileRepository;
    import org.unisoftware.gestioncurricular.repository.files.ProgramaResultadosFileRepository;
    import org.unisoftware.gestioncurricular.repository.files.StorageObjectRepository;
    import org.unisoftware.gestioncurricular.util.PublicFileUrlBuilder;

    import java.time.LocalDate;
    import java.time.format.DateTimeFormatter;
    import java.util.List;
    import java.util.Objects;
    import java.util.Optional;
    import java.util.stream.Collectors;

    @Service
    @Transactional
    public class ProgramFileService {

        private final ProgramRepository programRepository;
        private final ProgramaCurriculumFileRepository curriculumRepo;
        private final ProgramaResultadosFileRepository resultadosRepo;
        private final SupabaseProperties supabaseProperties;
        private final StorageObjectRepository storageObjectRepository;
        private final PublicFileUrlBuilder urlBuilder;

        public ProgramFileService(ProgramRepository programRepository,
                                  ProgramaCurriculumFileRepository curriculumRepo,
                                  ProgramaResultadosFileRepository resultadosRepo,
                                  SupabaseProperties supabaseProperties,
                                  StorageObjectRepository storageObjectRepository,
                                  PublicFileUrlBuilder urlBuilder) {
            this.programRepository = programRepository;
            this.curriculumRepo = curriculumRepo;
            this.resultadosRepo = resultadosRepo;
            this.supabaseProperties = supabaseProperties;
            this.storageObjectRepository = storageObjectRepository;
            this.urlBuilder = urlBuilder;
        }

        public String generateCurriculumUploadUrl(Long programId, LocalDate date) {
            Program program = programRepository.findById(programId)
                    .orElseThrow(() -> new IllegalArgumentException("Program not found"));

            String formattedDate = formatDateForFileName(date);
            String path = String.format("programas/%d/curriculums/curriculum_%s.pdf", program.getId(), formattedDate);
            return urlBuilder.buildUploadUrl(BucketsConfig.PUBLIC_BUCKET, path);
        }

        private String formatDateForFileName(LocalDate date) {
            if (date == null) {
                return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy"));
            }
            if (date.getDayOfMonth() != 1) {
                return date.format(DateTimeFormatter.ofPattern("yyyy_MM_dd"));
            } else if (date.getMonthValue() != 1) {
                return date.format(DateTimeFormatter.ofPattern("yyyy_MM"));
            } else {
                return date.format(DateTimeFormatter.ofPattern("yyyy"));
            }
        }

        public Optional<String> getMainCurriculumUrl(Long programId) {
            Optional<ProgramaCurriculumFile> mainFile = curriculumRepo.findFirstByProgramIdAndIsMainTrue(programId);
            return mainFile.map(this::buildCurriculumUrl);
        }

        private String buildCurriculumUrl(ProgramaCurriculumFile file) {
            return storageObjectRepository.findById(file.getFileId())
                    .map(obj -> urlBuilder.buildUrl(BucketsConfig.PUBLIC_BUCKET, obj.getName()))
                    .orElse(null);
        }

        private String buildResultadosUrl(ProgramaResultadosFile file) {
            return storageObjectRepository.findById(file.getFileId())
                    .map(obj -> urlBuilder.buildUrl(BucketsConfig.PUBLIC_BUCKET, obj.getName()))
                    .orElse(null);
        }

        public String generateResultadosUploadUrl(Long programId, LocalDate date) {
            Program program = programRepository.findById(programId)
                    .orElseThrow(() -> new IllegalArgumentException("Program not found"));

            String formattedDate = formatDateForFileName(date);
            String path = String.format("programas/%d/resultados/resultados_%s.pdf", program.getId(), formattedDate);
            return urlBuilder.buildUploadUrl(BucketsConfig.PUBLIC_BUCKET, path);
        }

        public Optional<String> getMainResultadoUrl(Long programId) {
            return resultadosRepo.findFirstByProgramIdAndIsMainTrue(programId)
                    .flatMap(file -> storageObjectRepository.findById(file.getFileId())
                            .map(obj -> urlBuilder.buildUrl(BucketsConfig.PUBLIC_BUCKET, obj.getName())));
        }

        public void editCurriculumFile(Long programId, Long curriculumId, UpdateProgramFileDTO dto) {
            ProgramaCurriculumFile file = curriculumRepo.findById(curriculumId)
                    .orElseThrow(() -> new IllegalArgumentException("Curriculum not found"));

            if (!file.getProgram().getId().equals(programId)) {
                throw new IllegalArgumentException("Curriculum does not belong to program");
            }

            if (dto.getDate() != null) {
                file.setDate(dto.getDate());
            }

            if (Boolean.TRUE.equals(dto.getIsMain())) {
                // Se marcará este como principal, los demás como false (trigger en la base ya lo hace)
                file.setMain(true);
            } else if (Boolean.FALSE.equals(dto.getIsMain())) {
                file.setMain(false);
            }

            curriculumRepo.save(file);
        }

        public void editResultadosFile(Long programId, Long resultadosId, UpdateProgramFileDTO dto) {
            ProgramaResultadosFile file = resultadosRepo.findById(resultadosId)
                    .orElseThrow(() -> new IllegalArgumentException("Resultados file not found"));

            if (!file.getProgram().getId().equals(programId)) {
                throw new IllegalArgumentException("Resultados file does not belong to program");
            }

            if (dto.getDate() != null) {
                file.setDate(dto.getDate());
            }

            if (Boolean.TRUE.equals(dto.getIsMain())) {
                // Se marcará este como principal, los demás como false (trigger en la base ya lo hace)
                file.setMain(true);
            } else if (Boolean.FALSE.equals(dto.getIsMain())) {
                file.setMain(false);
            }

            resultadosRepo.save(file);
        }


        public List<ProgramFileDTO> listCurriculums(Long programId, String orderBy, String orderDir) {
            List<ProgramaCurriculumFile> files = curriculumRepo.findByProgramId(programId);

            if ("date".equalsIgnoreCase(orderBy) || "uploadedAt".equalsIgnoreCase(orderBy)) {
                if ("desc".equalsIgnoreCase(orderDir)) {
                    files.sort((a, b) -> b.getUploadedAt().compareTo(a.getUploadedAt()));
                } else {
                    files.sort((a, b) -> a.getUploadedAt().compareTo(b.getUploadedAt()));
                }
            }

            return files.stream()
                    .map(file -> {
                        String url = buildCurriculumUrl(file);
                        return url != null ? new ProgramFileDTO(file.getId(), url, file.getUploadedAt(), file.getDate()) : null;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }

        public List<ProgramFileDTO> listResultados(Long programId, String orderBy, String orderDir) {
            List<ProgramaResultadosFile> files = resultadosRepo.findByProgramId(programId);

            if ("date".equalsIgnoreCase(orderBy) || "uploadedAt".equalsIgnoreCase(orderBy)) {
                if ("desc".equalsIgnoreCase(orderDir)) {
                    files.sort((a, b) -> b.getUploadedAt().compareTo(a.getUploadedAt()));
                } else {
                    files.sort((a, b) -> a.getUploadedAt().compareTo(b.getUploadedAt()));
                }
            }

            return files.stream()
                    .map(file -> storageObjectRepository.findById(file.getFileId())
                            .map(obj -> new ProgramFileDTO(file.getId(), buildResultadosUrl(file), file.getUploadedAt(), file.getDate()))
                            .orElse(null))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }


    }
