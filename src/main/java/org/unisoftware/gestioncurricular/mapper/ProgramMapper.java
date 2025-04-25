package org.unisoftware.gestioncurricular.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.MappingTarget;
import org.unisoftware.gestioncurricular.dto.ProgramDTO;
import org.unisoftware.gestioncurricular.entity.Program;

/**
 * MapStruct mapper entre Program y ProgramDTO.
 */
@Mapper(componentModel = "spring")
public interface ProgramMapper {

    /**
     * Convierte la entidad Program a ProgramDTO (DTO en inglés).
     */
    @Mapping(target = "id", ignore = true)
    ProgramDTO toDto(Program entity);

    /**
     * Convierte un ProgramDTO a la entidad Program (para creación y actualización).
     * Ignoramos id para creación ya que es generado por la base de datos.
     */
    @Mapping(target = "id", ignore = true)

    Program toEntity(ProgramDTO dto);

    /**
     * Para actualizar una entidad existente con los datos del DTO.
     */

    @Mapping(target = "id", ignore = true)
    void updateFromDto(ProgramDTO dto, @MappingTarget Program entity);
}
