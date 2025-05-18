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

        List<String[]> raw = extractRows(file.getInputStream());

        for (String[] cols : raw) {
            String sniesStr = cols[0].trim();
            String semesterStr = cols[1].trim();

            if (sniesStr.isEmpty() || semesterStr.isEmpty()) {
                throw new IllegalArgumentException("SNIES y semestre no pueden estar vacíos. Datos: " + Arrays.toString(cols));
            }

            long snies = Long.parseLong(sniesStr);
            int semester = Integer.parseInt(semesterStr);

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

    protected abstract List<String[]> extractRows(InputStream is) throws Exception;

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

    protected abstract List<String> getSupportedExtensions();
}
