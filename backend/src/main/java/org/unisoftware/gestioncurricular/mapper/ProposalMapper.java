package org.unisoftware.gestioncurricular.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.unisoftware.gestioncurricular.dto.ProposalDTO;
import org.unisoftware.gestioncurricular.entity.Proposal;

/**
 * Mapper para convertir entre Proposal y ProposalDTO.
 * Utiliza MapStruct para simplificar la transformación de datos.
 */
@Mapper(componentModel = "spring")
public interface ProposalMapper {

    /**
     * Convierte un DTO a entidad, ignorando campos autogenerados.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lastUpdatedAt", ignore = true)
    Proposal toEntity(ProposalDTO dto);

    /**
     * Convierte una entidad a su DTO correspondiente.
     */
    ProposalDTO toDto(Proposal entity);

    /**
     * Actualiza una entidad existente con datos de un DTO.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lastUpdatedAt", ignore = true)
    void updateEntityFromDto(ProposalDTO dto, @MappingTarget Proposal entity);
}