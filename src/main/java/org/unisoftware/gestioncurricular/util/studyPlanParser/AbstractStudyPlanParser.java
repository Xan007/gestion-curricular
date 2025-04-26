package org.unisoftware.gestioncurricular.util.studyPlanParser;

import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class AbstractStudyPlanParser implements StudyPlanParser {

    @Override
    public List<PlanRow> parse(MultipartFile file) throws Exception {
        List<PlanRow> result = new ArrayList<>();
        // extrae cada fila cruda como String[]
        List<String[]> raw = extractRows(file.getInputStream());

        for (String[] cols : raw) {
            // SNIES y semestre son obligatorios
            long snies = Long.parseLong(cols[0].trim());
            int semester = Integer.parseInt(cols[1].trim());

            // requisitos puede venir vacío
            String rawReq = cols.length > 2 ? cols[2].trim() : "";
            List<Long> reqs = parseRequisitos(rawReq);

            PlanRow pr = new PlanRow();
            pr.setSnies(snies);
            pr.setSemester(semester);
            pr.setRequisitos(reqs);

            result.add(pr);
        }
        return result;
    }

    /** Extrae de un InputStream la lista de filas en bruto como String[] */
    protected abstract List<String[]> extractRows(InputStream is) throws Exception;

    /** Lógica común: parsea el tercer campo; puede estar separado por ';' o por comas */
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

    @Override
    public boolean supports(String filename) {
        if (filename == null) return false;
        return getSupportedExtensions().stream()
                .anyMatch(filename.toLowerCase()::endsWith);
    }

    /** Cada parser concreto declara aquí sus extensiones (".csv", ".xlsx", etc) */
    protected abstract List<String> getSupportedExtensions();
}
