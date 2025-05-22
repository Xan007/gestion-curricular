package org.unisoftware.gestioncurricular.entity.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.unisoftware.gestioncurricular.util.enums.AcademicSupportType;

@Converter(autoApply = true)
public class AcademicSupportTypeConverter implements AttributeConverter<AcademicSupportType, String> {

    @Override
    public String convertToDatabaseColumn(AcademicSupportType type) {
        return type != null ? type.name() : null;
    }

    @Override
    public AcademicSupportType convertToEntityAttribute(String dbData) {
        return dbData != null ? AcademicSupportType.valueOf(dbData) : null;
    }
}
