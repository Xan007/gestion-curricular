package org.unisoftware.gestioncurricular.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.unisoftware.gestioncurricular.entity.StudyPlanEntry;
import org.unisoftware.gestioncurricular.entity.StudyPlanEntryId;

import java.util.List;

public interface StudyPlanViewRepository
        extends JpaRepository<StudyPlanEntry, StudyPlanEntryId> {

    List<StudyPlanEntry>
    findById_ProgramIdOrderBySemesterAscId_CourseIdAsc(Long programId);

    List<StudyPlanEntry> findById_ProgramIdAndId_YearOrderBySemesterAscId_CourseIdAsc(Long programId, Integer year);
}


