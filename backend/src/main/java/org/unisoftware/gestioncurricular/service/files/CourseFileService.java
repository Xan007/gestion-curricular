package org.unisoftware.gestioncurricular.service.files;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.support.SimpleTriggerContext;
import org.springframework.stereotype.Service;
import org.unisoftware.gestioncurricular.config.BucketsConfig;
import org.unisoftware.gestioncurricular.dto.files.CourseMicroFileDTO;
import org.unisoftware.gestioncurricular.dto.files.CourseSupportFileDTO;
import org.unisoftware.gestioncurricular.dto.files.UpdateCourseMicroFileDTO;
import org.unisoftware.gestioncurricular.dto.files.UpdateCourseSupportFileDTO;
import org.unisoftware.gestioncurricular.entity.Course;
import org.unisoftware.gestioncurricular.entity.files.CursoApoyosFile;
import org.unisoftware.gestioncurricular.entity.files.CursoMicrocurriculumFile;
import org.unisoftware.gestioncurricular.repository.CourseRepository;
import org.unisoftware.gestioncurricular.repository.files.CursoApoyosFileRepository;
import org.unisoftware.gestioncurricular.repository.files.CursoMicrocurriculumFileRepository;
import org.unisoftware.gestioncurricular.repository.files.StorageObjectRepository;
import org.unisoftware.gestioncurricular.util.PublicFileUrlBuilder;
import org.unisoftware.gestioncurricular.util.enums.AcademicSupportType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseFileService {

    private final CursoApoyosFileRepository apoyosFileRepository;
    private final CursoMicrocurriculumFileRepository microRepository;
    private final CourseRepository courseRepository;
    private final PublicFileUrlBuilder urlBuilder;
    private final StorageObjectRepository storageObjectRepository;

    public String generateApoyoUploadUrl(Long courseId, String filename) {
        String path = String.format("cursos/%d/apoyos/%s", courseId, filename);
        return urlBuilder.buildUploadUrl(BucketsConfig.PUBLIC_BUCKET, path);
    }

    @Transactional
    public void registerApoyoAcademico(Long courseId, UUID fileId, AcademicSupportType tipo) {
        Course course = courseRepository.findById(courseId).orElseThrow();

        CursoApoyosFile file = new CursoApoyosFile();
        file.setCourse(course);
        file.setFileId(fileId);
        file.setTipo(tipo);
        file.setUploadedAt(LocalDateTime.now());

        apoyosFileRepository.save(file);
    }

    public List<CourseSupportFileDTO> getAllApoyos(Long courseId, Optional<AcademicSupportType> tipo) {
        List<CursoApoyosFile> archivos = tipo
                .map(t -> apoyosFileRepository.findByCourseIdAndTipo(courseId, t))
                .orElseGet(() -> apoyosFileRepository.findAllByCourseId(courseId));

        return archivos.stream()
                .map(file -> new CourseSupportFileDTO(
                                file.getId(),
                                buildApoyoUrl(file),
                                file.getUploadedAt(),
                                file.getTipo()
                        ))
                .collect(Collectors.toList());
    }

    public String generateMicrocurriculumUploadUrl(Long courseId, LocalDate date) {
        String filename = String.format("cursos/%d/microcurriculums/" + "microcurriculum_%s", courseId, date.format(DateTimeFormatter.ofPattern("yyyy_MM_dd")) + ".pdf");
        return urlBuilder.buildUploadUrl(BucketsConfig.PUBLIC_BUCKET, filename);
    }

    private String buildMicrocurriculumUrl(CursoMicrocurriculumFile file) {
        return storageObjectRepository.findById(file.getFileId())
                .map(obj -> urlBuilder.buildUrl(BucketsConfig.PUBLIC_BUCKET, obj.getName()))
                .orElse(null);
    }

    private String buildApoyoUrl(CursoApoyosFile file) {
        return storageObjectRepository.findById(file.getFileId())
                .map(obj -> urlBuilder.buildUrl(BucketsConfig.PUBLIC_BUCKET, obj.getName()))
                .orElse(null);
    }

    @Transactional
    public void updateMicrocurriculumFileDetails(Long courseId, Long fileId, UpdateCourseMicroFileDTO dto) {
        CursoMicrocurriculumFile file = microRepository.findById(fileId).orElseThrow();

        if (!file.getCourse().getId().equals(courseId)) {
            throw new IllegalArgumentException("El archivo no pertenece al curso indicado.");
        }

        if (dto.getDate() != null) {
            file.setDate(dto.getDate());
        }

        file.setMain(dto.getIsMain());

        microRepository.save(file);
    }

    public List<CourseMicroFileDTO> getAllMicro(Long courseId, Optional<String> sortBy, boolean ascending) {
        List<CursoMicrocurriculumFile> files = microRepository.findByCourseId(courseId);

        Comparator<CursoMicrocurriculumFile> comparator = Comparator.comparing(CursoMicrocurriculumFile::getUploadedAt);
        if (sortBy.isPresent() && "date".equalsIgnoreCase(sortBy.get())) {
            comparator = Comparator.comparing(CursoMicrocurriculumFile::getDate);
        }
        if (!ascending) {
            comparator = comparator.reversed();
        }

        return files.stream()
                .sorted(comparator)
                .map(file ->
                        new CourseMicroFileDTO(
                                file.getId(),
                                buildMicrocurriculumUrl(file),
                                file.getUploadedAt(),
                                file.getDate(),
                                file.isMain()
                        ))
                .collect(Collectors.toList());
    }



    public String getMainMicrocurriculum(Long courseId) {
        CursoMicrocurriculumFile file = microRepository.findByCourseIdAndIsMainTrue(courseId).orElse(null);
        if (file == null) return null;

        return buildMicrocurriculumUrl(file);
    }

    @Transactional
    public void updateApoyoFileDetails(Long courseId, UUID fileId, UpdateCourseSupportFileDTO dto) {
        CursoApoyosFile file = apoyosFileRepository.findByCourseIdAndFileId(courseId, fileId);

        if (dto.getTipo() != null) {
            file.setTipo(dto.getTipo());
        }

        apoyosFileRepository.save(file);
    }


}
