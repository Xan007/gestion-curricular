package org.unisoftware.gestioncurricular.util.studyPlanParser;

import org.springframework.stereotype.Component;

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
            boolean isFirstLine = true;

            while ((line = br.readLine()) != null) {
                // Saltar la primera línea (encabezados)
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                if (line.isBlank()) continue;

                // Dividir respetando comillas
                String[] cols = line.split(SPLIT_REGEX, -1);

                // Limpiar comillas exteriores y espacios
                for (int i = 0; i < cols.length; i++) {
                    cols[i] = cols[i].trim()
                            .replaceAll("^\"|\"$", "")
                            .replaceAll("^'|'$", "");
                }

                // Validar que al menos tenga las columnas mínimas
                if (cols.length < 9) {
                    throw new IllegalArgumentException("Fila inválida de plan de estudio CSV. " +
                            "Se esperan 9 columnas (SNIES, Curso, Tipo, Ciclo, Área, Créditos, Relación, Semestre, Requisitos), " +
                            "se encontraron: " + cols.length + ". Línea: " + line);
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