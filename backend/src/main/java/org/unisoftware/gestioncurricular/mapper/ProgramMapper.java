package org.unisoftware.gestioncurricular.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.MappingTarget;
import org.unisoftware.gestioncurricular.dto.ProgramDTO;
import org.unisoftware.gestioncurricular.entity.Program;

@Mapper(componentModel = "spring")
public interface ProgramMapper {
    ProgramDTO toDto(Program entity);

    @Mapping(target = "id", ignore = true)
    Program toEntity(ProgramDTO dto);

    void updateFromDto(ProgramDTO dto, @MappingTarget Program entity);
}
