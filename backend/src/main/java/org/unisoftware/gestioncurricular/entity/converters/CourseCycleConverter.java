package org.unisoftware.gestioncurricular.entity.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.unisoftware.gestioncurricular.util.courseEnums.CourseCycle;

@Converter(autoApply = true) // Esto aplica el converter automáticamente a todas las columnas que usan este tipo
public class CourseCycleConverter implements AttributeConverter<CourseCycle, String> {

    @Override
    public String convertToDatabaseColumn(CourseCycle courseCycle) {
        return courseCycle != null ? courseCycle.name() : null;
    }

    @Override
    public CourseCycle convertToEntityAttribute(String dbData) {
        return dbData != null ? CourseCycle.valueOf(dbData) : null;
    }
}
