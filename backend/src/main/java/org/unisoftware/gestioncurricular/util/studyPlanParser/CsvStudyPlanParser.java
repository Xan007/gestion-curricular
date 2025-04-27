package org.unisoftware.gestioncurricular.util.studyPlanParser;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class CsvStudyPlanParser extends AbstractStudyPlanParser {

    private static final String SPLIT_REGEX = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";

    @Override
    protected List<String[]> extractRows(InputStream is) throws Exception {
        List<String[]> rows = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                if (first) { first = false; continue; }
                if (line.isBlank()) continue;

                // split fuera de comillas
                String[] cols = line.split(SPLIT_REGEX, -1);
                // limpiar comillas exteriores
                for (int i = 0; i < cols.length; i++) {
                    cols[i] = cols[i].trim()
                            .replaceAll("^\"|\"$", "")
                            .replaceAll("^'|'$", "");
                }
                // al menos SNIES y semestre
                if (cols.length < 2) {
                    throw new IllegalArgumentException("Fila inválida de plan de estudio CSV: " + line);
                }
                rows.add(cols);
            }
        }
        return rows;
    }

    @Override
    protected List<String> getSupportedExtensions() {
        return List.of(".csv");
    }
}
