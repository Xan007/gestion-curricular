package org.unisoftware.gestioncurricular.agent.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.unisoftware.gestioncurricular.agent.tools.CourseTools;
import org.unisoftware.gestioncurricular.agent.tools.ProgramTools;
import org.unisoftware.gestioncurricular.service.CourseService;
import org.unisoftware.gestioncurricular.service.ProgramService;

@Configuration
public class AgentToolsConfig {

    @Bean
    public ProgramTools programTools(ProgramService programService) {
        return new ProgramTools(programService);
    }

    @Bean
    public CourseTools courseTools(ProgramService programService, CourseService courseService) {
        return new CourseTools(courseService, programService);
    }
}
