package org.unisoftware.gestioncurricular.util;

import lombok.Getter;
import lombok.Setter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class StudyPlanExcelParser {

    /**
     * DTO-like holder for one row of the plan de estudios Excel
     */
    @Getter
    @Setter
    public static class PlanRow {
        private Long snies;
        private Integer semester;
        private List<Long> requisitos;
    }

    /**
     * Parse the study plan Excel file (one sheet) into a list of PlanRow
     * Format: SNIES (numeric), Semestre (numeric), Requisito(s) (comma-separated SNIES)
     */
    public static List<PlanRow> parse(MultipartFile file) throws Exception {
        List<PlanRow> rows = new ArrayList<>();
        try (InputStream is = file.getInputStream();
             Workbook wb = new XSSFWorkbook(is)) {
            Sheet sheet = wb.getSheetAt(0);
            for (Row r : sheet) {
                if (r.getRowNum() == 0) continue; // skip header
                PlanRow pr = new PlanRow();
                // SNIES
                pr.setSnies((long) r.getCell(0).getNumericCellValue());
                // Semestre
                pr.setSemester((int) r.getCell(1).getNumericCellValue());
                // Requisitos
                Cell reqCell = r.getCell(2);
                List<Long> reqs = new ArrayList<>();
                if (reqCell != null && reqCell.getCellType() == CellType.STRING) {
                    String[] parts = reqCell.getStringCellValue().split(",");
                    for (String part : parts) {
                        String trim = part.trim();
                        if (!trim.isEmpty()) {
                            reqs.add(Long.parseLong(trim));
                        }
                    }
                }
                pr.setRequisitos(reqs);
                rows.add(pr);
            }
        }
        return rows;
    }
}