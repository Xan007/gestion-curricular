package org.unisoftware.gestioncurricular.util.studyPlanParser;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface StudyPlanParser {
    boolean supports(String filename);
    List<PlanRow> parse(MultipartFile file) throws Exception;
}
