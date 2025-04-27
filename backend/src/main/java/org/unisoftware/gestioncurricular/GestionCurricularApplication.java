package org.unisoftware.gestioncurricular;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.unisoftware.gestioncurricular.config.SupabaseProperties;

@SpringBootApplication
@EnableConfigurationProperties(SupabaseProperties.class)
public class GestionCurricularApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(GestionCurricularApplication.class, args);
    }

}
