package org.unisoftware.gestioncurricular.entity.enumTypes;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.unisoftware.gestioncurricular.util.courseEnums.CourseType;

@Converter(autoApply = true) // Esto aplica el converter automáticamente a todas las columnas que usan este tipo
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
