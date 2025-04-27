package org.unisoftware.gestioncurricular.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;

@Configuration
public class RepositoryConfig {

    public RepositoryConfig(RepositoryRestConfiguration config) {
        config.disableDefaultExposure(); // Deshabilita la exposición automática de repositorios
    }
}