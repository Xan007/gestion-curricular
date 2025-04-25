package org.unisoftware.gestioncurricular.util.courseParser;

import org.springframework.stereotype.Component;
import org.unisoftware.gestioncurricular.dto.CourseDTO;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class CsvCourseFileParser extends AbstractCourseFileParser {
    private static final String CSV_SPLIT_REGEX = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";

    @Override
    protected List<String[]> extractRows(InputStream is) throws IOException {
        List<String[]> rows = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String line;
            br.readLine(); // skip header
            int rowNum = 2; // contaremos filas para errores
            while ((line = br.readLine()) != null) {
                // 1) split por comas **fuera** de dobles comillas
                String[] values = line.split(CSV_SPLIT_REGEX, -1);

                // 2) limpia comillas alrededor de cada valor
                for (int i = 0; i < values.length; i++) {
                    values[i] = values[i].trim()
                            .replaceAll("^\"|\"$", "")   // quita " al inicio/fin
                            .replaceAll("^'|'$", "");    // y también '
                }

                // 3) valida fila
                try {
                    processRow(values, rowNum);
                } catch (Exception ex) {
                    throw new IllegalArgumentException(
                            "Error processing row " + rowNum + ": " + line, ex
                    );
                }

                rows.add(values);
                rowNum++;
            }
        }
        return rows;
    }

    @Override
    protected List<String> getSupportedExtensions() {
        return List.of(".csv");
    }
}
