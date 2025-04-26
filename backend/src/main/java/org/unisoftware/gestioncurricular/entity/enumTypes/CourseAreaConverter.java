package org.unisoftware.gestioncurricular.entity.enumTypes;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.unisoftware.gestioncurricular.util.courseEnums.CourseArea;

@Converter(autoApply = true) // Esto aplica el converter automáticamente a todas las columnas que usan este tipo
public class CourseAreaConverter implements AttributeConverter<CourseArea, String> {

    @Override
    public String convertToDatabaseColumn(CourseArea courseArea) {
        return courseArea != null ? courseArea.name() : null;
    }

    @Override
    public CourseArea convertToEntityAttribute(String dbData) {
        return dbData != null ? CourseArea.valueOf(dbData) : null;
    }
}
