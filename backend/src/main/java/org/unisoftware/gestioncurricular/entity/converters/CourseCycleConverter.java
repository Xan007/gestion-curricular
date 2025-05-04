package org.unisoftware.gestioncurricular.entity.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.unisoftware.gestioncurricular.util.enums.courseEnums.CourseCycle;

@Converter(autoApply = true)
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
