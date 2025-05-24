package org.unisoftware.gestioncurricular.util.studyPlanParser;

import org.springframework.web.multipart.MultipartFile;
import org.unisoftware.gestioncurricular.util.enums.courseEnums.CourseArea;
import org.unisoftware.gestioncurricular.util.enums.courseEnums.CourseCycle;
import org.unisoftware.gestioncurricular.util.enums.courseEnums.CourseType;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class AbstractStudyPlanParser implements StudyPlanParser {

    // Columnas esperadas: SNIES, Curso, Tipo, Ciclo, Área, Creditos, Relación, Semestre, Requisito(s)
    private static final int MIN_COLUMNS = 9;
    private static final int SNIES_INDEX = 0;
    private static final int COURSE_NAME_INDEX = 1;
    private static final int TYPE_INDEX = 2;
    private static final int CYCLE_INDEX = 3;
    private static final int AREA_INDEX = 4;
    private static final int CREDITS_INDEX = 5;
    private static final int RELATION_INDEX = 6;
    private static final int SEMESTER_INDEX = 7;
    private static final int REQUISITES_INDEX = 8;

    @Override
    public List<PlanRow> parse(MultipartFile file) throws Exception {
        List<PlanRow> result = new ArrayList<>();
        List<String[]> raw = extractRows(file.getInputStream());

        for (String[] cols : raw) {
            if (cols.length < MIN_COLUMNS) {
                throw new IllegalArgumentException("Fila incompleta. Se esperan al menos " + MIN_COLUMNS +
                        " columnas, se encontraron: " + cols.length + ". Datos: " + Arrays.toString(cols));
            }

            PlanRow planRow = new PlanRow();

            // SNIES (obligatorio)
            String sniesStr = cols[SNIES_INDEX].trim();
            if (sniesStr.isEmpty()) {
                throw new IllegalArgumentException("SNIES no puede estar vacío. Datos: " + Arrays.toString(cols));
            }
            planRow.setSnies(Long.parseLong(sniesStr));

            // Nombre del curso (obligatorio)
            String courseName = cols[COURSE_NAME_INDEX].trim();
            if (courseName.isEmpty()) {
                throw new IllegalArgumentException("Nombre del curso no puede estar vacío. Datos: " + Arrays.toString(cols));
            }
            planRow.setCourseName(courseName);

            // Tipo de curso (TP, T, P)
            String typeStr = cols[TYPE_INDEX].trim();
            if (!typeStr.isEmpty()) {
                try {
                    planRow.setCourseType(CourseType.fromCode(typeStr));
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Tipo de curso inválido: '" + typeStr +
                            "'. Valores válidos: TP, T, P. Datos: " + Arrays.toString(cols));
                }
            }

            // Ciclo del curso (A, E, F, NA)
            String cycleStr = cols[CYCLE_INDEX].trim();
            System.out.println("Ciclo: " + cycleStr);
            if (!cycleStr.isEmpty()) {
                try {
                    planRow.setCourseCycle(CourseCycle.fromCode(cycleStr));
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Ciclo del curso inválido: '" + cycleStr +
                            "'. Valores válidos: A, E, F, NA. Datos: " + Arrays.toString(cols));
                }
            }

            // Área del curso (PS, BA, CO, PZ, I)
            String areaStr = cols[AREA_INDEX].trim();
            System.out.println("Area: " + areaStr);
            if (!areaStr.isEmpty()) {
                try {
                    planRow.setCourseArea(CourseArea.fromCode(areaStr));
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Área del curso inválida: '" + areaStr +
                            "'. Valores válidos: PS, BA, CO, PZ, I. Datos: " + Arrays.toString(cols));
                }
            }

            // Créditos (obligatorio)
            String creditsStr = cols[CREDITS_INDEX].trim();
            if (creditsStr.isEmpty()) {
                throw new IllegalArgumentException("Créditos no puede estar vacío. Datos: " + Arrays.toString(cols));
            }
            planRow.setCredits(Integer.parseInt(creditsStr));

            // Relación (por defecto "1:1" si está vacío)
            String relation = cols[RELATION_INDEX].trim();
            System.out.println("Relación: " + relation);
            planRow.setRelation(relation.isEmpty() ? "1:1" : relation);

            // Semestre (obligatorio)
            String semesterStr = cols[SEMESTER_INDEX].trim();
            if (semesterStr.isEmpty()) {
                throw new IllegalArgumentException("Semestre no puede estar vacío. Datos: " + Arrays.toString(cols));
            }
            planRow.setSemester(Integer.parseInt(semesterStr));

            // Requisitos (opcional)
            String rawReq = cols[REQUISITES_INDEX].trim();
            List<Long> reqs = parseRequisitos(rawReq);
            planRow.setRequisitos(reqs);

            System.out.println(planRow);

            result.add(planRow);
        }

        return result;
    }

    protected abstract List<String[]> extractRows(InputStream is) throws Exception;

    /**
     * Parsea una cadena de requisitos separados por ; o ,
     */
    private List<Long> parseRequisitos(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        return Arrays.stream(raw.split("[;,]"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::parseLong)
                .toList();
    }

    /**
     * Parsea un valor enum con manejo de errores mejorado
     */
    private <T extends Enum<T>> T parseEnumValue(Class<T> enumClass, String value, String fieldName) {
        try {
            return Enum.valueOf(enumClass, value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(fieldName + " inválido: '" + value +
                    "'. Valores válidos: " + Arrays.toString(enumClass.getEnumConstants()));
        }
    }

    @Override
    public boolean supports(String filename) {
        if (filename == null) return false;
        return getSupportedExtensions().stream()
                .anyMatch(filename.toLowerCase()::endsWith);
    }

    protected abstract List<String> getSupportedExtensions();
}