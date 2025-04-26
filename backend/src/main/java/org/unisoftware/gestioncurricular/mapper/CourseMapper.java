package org.unisoftware.gestioncurricular.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.unisoftware.gestioncurricular.dto.CourseDTO;
import org.unisoftware.gestioncurricular.entity.Course;

@Mapper(componentModel = "spring")
public interface CourseMapper {
    // Map DTO to entity for creation
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Course toEntity(CourseDTO dto);

    // Map entity to DTO if needed
    CourseDTO toDto(Course entity);

    // Update existing entity with DTO values
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateCourseFromDto(CourseDTO dto, @MappingTarget Course entity);
}
