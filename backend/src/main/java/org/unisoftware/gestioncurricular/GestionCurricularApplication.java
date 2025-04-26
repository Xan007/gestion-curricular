package org.unisoftware.gestioncurricular;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.unisoftware.gestioncurricular.config.SupabaseProperties;

@SpringBootApplication
@EnableConfigurationProperties(SupabaseProperties.class)
public class GestionCurricularApplication {

    public static void main(String[] args) throws Exception {
        //SupabaseAuth authTest = new SupabaseAuth();


        //System.out.println(authTest.signUp("sierrasander29@gmail.com", "sander2929"));
        //System.out.println(authTest.signIn("sierrasander29@gmail.com", "sander2929"));
        SpringApplication.run(GestionCurricularApplication.class, args);
    }

}
