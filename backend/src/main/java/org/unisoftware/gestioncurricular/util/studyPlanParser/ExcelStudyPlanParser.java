package org.unisoftware.gestioncurricular.util.studyPlanParser;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component
public class ExcelStudyPlanParser extends AbstractStudyPlanParser {

    @Override
    protected List<String[]> extractRows(InputStream is) throws Exception {
        List<String[]> rows = new ArrayList<>();

        try (Workbook wb = new XSSFWorkbook(is)) {
            Sheet sheet = wb.getSheetAt(0);

            for (Row row : sheet) {
                // Saltar la primera fila (encabezados)
                if (row.getRowNum() == 0) continue;

                // Verificar que la fila no esté completamente vacía
                if (isRowEmpty(row)) continue;

                // Extraer las 9 columnas esperadas
                String[] cols = new String[9];
                cols[0] = getCellValue(row, 0); // SNIES
                cols[1] = getCellValue(row, 1); // Curso
                cols[2] = getCellValue(row, 2); // Tipo
                cols[3] = getCellValue(row, 3); // Ciclo
                cols[4] = getCellValue(row, 4); // Área
                cols[5] = getCellValue(row, 5); // Créditos
                cols[6] = getCellValue(row, 6); // Relación
                cols[7] = getCellValue(row, 7); // Semestre
                cols[8] = getCellValue(row, 8); // Requisitos

                // Validar que al menos SNIES no esté vacío
                if (cols[0].isEmpty()) continue;

                rows.add(cols);
            }
        }

        return rows;
    }

    private String getCellValue(Row row, int columnIndex) {
        Cell cell = row.getCell(columnIndex);
        if (cell == null) return "";
        System.out.println(cell);

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == Math.floor(numericValue)) {
                        return String.valueOf((long) numericValue);
                    } else {
                        return String.valueOf(numericValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case BLANK:
            case _NONE:
            default:
                return "";
        }
    }

    private boolean isRowEmpty(Row row) {
        for (int i = 0; i < 9; i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK &&
                    !getCellValue(row, i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected List<String> getSupportedExtensions() {
        return List.of(".xlsx", ".xls");
    }
}