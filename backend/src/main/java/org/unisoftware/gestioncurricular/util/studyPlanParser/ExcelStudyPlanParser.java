package org.unisoftware.gestioncurricular.util.studyPlanParser;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

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
            for (Row r : sheet) {
                if (r.getRowNum() == 0) continue;

                String[] cols = new String[3];
                cols[0] = getCellValue(r, 0, true);
                cols[1] = getCellValue(r, 1, true);
                cols[2] = getCellValue(r, 2, true);

                if (cols[0].isEmpty() || cols[1].isEmpty()) {
                    continue;
                }

                rows.add(cols);
            }
        }
        return rows;
    }

    private String getCellValue(Row row, int index, boolean truncateAtDot) {
        Cell cell = row.getCell(index);
        if (cell == null) return "";
        String value = cell.toString().trim();
        if (truncateAtDot) {
            int dotIndex = value.indexOf(".");
            if (dotIndex != -1) {
                value = value.substring(0, dotIndex);
            }
        }
        System.out.println(value);
        return value;
    }

    @Override
    protected List<String> getSupportedExtensions() {
        return List.of(".xlsx");
    }
}
