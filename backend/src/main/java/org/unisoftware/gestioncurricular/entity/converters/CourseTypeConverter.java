package org.unisoftware.gestioncurricular.entity.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.unisoftware.gestioncurricular.util.courseEnums.CourseType;

@Converter(autoApply = true)
public class CourseTypeConverter implements AttributeConverter<CourseType, String> {

    @Override
    public String convertToDatabaseColumn(CourseType courseType) {
        return courseType != null ? courseType.name() : null;
    }

    @Override
    public CourseType convertToEntityAttribute(String dbData) {
        return dbData != null ? CourseType.valueOf(dbData) : null;
    }
}
