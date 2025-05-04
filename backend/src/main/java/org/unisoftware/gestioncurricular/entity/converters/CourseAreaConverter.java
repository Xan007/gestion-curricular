package org.unisoftware.gestioncurricular.entity.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.unisoftware.gestioncurricular.util.enums.courseEnums.CourseArea;

@Converter(autoApply = true)
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
