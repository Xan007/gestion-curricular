package org.unisoftware.gestioncurricular.util.courseParser;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class ExcelCourseFileParser extends AbstractCourseFileParser {

    @Override
    protected List<String[]> extractRows(InputStream is) throws IOException {
        try (Workbook wb = new XSSFWorkbook(is)) {
            Sheet sheet = wb.getSheetAt(0);
            List<String[]> rows = new ArrayList<>();
            int rowNum = 2;
            for (Row r : sheet) {
                if (r.getRowNum() == 0) continue;
                String[] cols = new String[7];
                for (int i = 0; i < 7; i++) {
                    Cell c = r.getCell(i);
                    cols[i] = c == null ? "" : c.toString().trim();
                }
                try {
                    processRow(cols, rowNum);
                } catch (Exception ex) {
                    throw new IllegalArgumentException("Error processing row " + rowNum + ": " + r, ex);
                }
                rows.add(cols);
                rowNum++;
            }
            return rows;
        }
    }

    @Override
    protected List<String> getSupportedExtensions() {
        return List.of(".xlsx");
    }
}
