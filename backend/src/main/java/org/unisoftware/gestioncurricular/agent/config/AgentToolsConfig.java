package org.unisoftware.gestioncurricular.agent;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.unisoftware.gestioncurricular.agent.tools.ProgramTools;
import org.unisoftware.gestioncurricular.service.ProgramService;

@Configuration
public class AgentToolsConfig {

    @Bean
    public ProgramTools programTools(ProgramService programService) {
        return new ProgramTools(programService);
    }
}
