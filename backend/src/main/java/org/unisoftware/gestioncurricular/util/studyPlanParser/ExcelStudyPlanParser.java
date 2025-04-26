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
                if (r.getRowNum() == 0) continue;  // salto cabecera
                // siempre devolvemos al menos 3 columnas de String
                String[] cols = new String[3];
                // SNIES y semestre
                cols[0] = r.getCell(0).toString().trim();
                cols[1] = r.getCell(1).toString().trim();
                // requisitos raw
                Cell c2 = r.getCell(2);
                cols[2] = (c2 == null ? "" : c2.toString().trim());
                rows.add(cols);
            }
        }
        return rows;
    }

    @Override
    protected List<String> getSupportedExtensions() {
        return List.of(".xlsx");
    }
}
