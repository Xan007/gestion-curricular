package org.unisoftware.gestioncurricular.entity.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.unisoftware.gestioncurricular.util.enums.ProposalStatus;

@Converter(autoApply = true)
public class ProposalStatusConverter implements AttributeConverter<ProposalStatus, String> {

    @Override
    public String convertToDatabaseColumn(ProposalStatus proposalStatus) {
        return proposalStatus != null ? proposalStatus.name() : null;
    }

    @Override
    public ProposalStatus convertToEntityAttribute(String dbData) {
        return dbData != null ? ProposalStatus.valueOf(dbData) : null;
    }
}
