package org.unisoftware.gestioncurricular.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.unisoftware.gestioncurricular.dto.ProposalDTO;
import org.unisoftware.gestioncurricular.entity.Course;
import org.unisoftware.gestioncurricular.entity.Proposal;

@Mapper(componentModel = "spring")
public interface ProposalMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lastUpdatedAt", ignore = true)
    @Mapping(target = "course", source = "courseId")
    Proposal toEntity(ProposalDTO dto);

    @Mapping(target = "courseId", source = "course.id")
    ProposalDTO toDto(Proposal entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lastUpdatedAt", ignore = true)
    void updateEntityFromDto(ProposalDTO dto, @MappingTarget Proposal entity);

    default Course map(Long courseId) {
        if (courseId == null) return null;
        Course course = new Course();
        course.setId(courseId);
        return course;
    }
}
