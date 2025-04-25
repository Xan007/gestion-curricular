package org.unisoftware.gestioncurricular.util.courseParser;

import org.unisoftware.gestioncurricular.dto.CourseDTO;

import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public abstract class AbstractCourseFileParser implements CourseFileParser {

    @Override
    public List<CourseDTO> parse(InputStream is) throws IOException {
        List<String[]> rows = extractRows(is);
        List<CourseDTO> dtos = new ArrayList<>();
        int rowNum = 2; // Start counting from 2 because we skipped the header
        for (String[] cols : rows) {
            try {
                CourseDTO dto = processRow(cols, rowNum); // Use processRow for custom logic
                dtos.add(dto);
            } catch (IllegalArgumentException ex) {
                System.out.println(ex.getMessage());
            }
            rowNum++;
        }
        return dtos;
    }

    protected abstract List<String[]> extractRows(InputStream is) throws IOException;

    protected CourseDTO processRow(String[] cols, int rowNum) {
        long snies = Long.parseLong(cols[0]);
        String name = cols[1].trim();

        // Usar los enums para transformar los valores
        String type = cols[2].trim().toUpperCase(Locale.ROOT);
        String cycle = cols[3].trim().toUpperCase(Locale.ROOT);
        String area = cols[4].trim().toUpperCase(Locale.ROOT);

        Integer credits = Integer.parseInt(cols[5].trim());
        String relation = cols.length > 6 && !cols[6].isEmpty() ? cols[6].trim() : "1:1";

        // Crear el DTO con la validación incluida
        CourseDTO dto = new CourseDTO(snies, name, type, cycle, area, credits, relation);
        dto.setId(snies);
        // Validación de todos los campos
        validate(dto, rowNum);

        return dto;
    }

    // Método de validación global
    protected void validate(CourseDTO dto, int rowNum) {
        try {
            // Validar los valores del DTO
            validateSnies(dto.getId(), rowNum);
            // No es necesario hacer validación explícita de 'type', 'cycle', y 'area' aquí, ya que los enums ya validan
            validateCredits(dto.getCredits(), rowNum);
            validateRelation(dto.getRelation());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Error in row " + rowNum + ": " + ex.getMessage());
        }
    }

    private void validateSnies(long snies, int rowNum) {
        if (snies <= 0) {
            throw new IllegalArgumentException("Invalid SNIES value at row " + rowNum + ": " + snies);
        }
    }

    private void validateCredits(int credits, int rowNum) {
        if (credits <= 0) {
            throw new IllegalArgumentException("Invalid Credits value at row " + rowNum + ": " + credits);
        }
    }

    private void validateRelation(String relation) {
        if (relation == null || relation.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid Relation value: " + relation);
        }
    }

    @Override
    public boolean supports(String filename) {
        return getSupportedExtensions().stream()
                .anyMatch(filename::endsWith);
    }

    protected abstract List<String> getSupportedExtensions();
}
