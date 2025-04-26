package org.unisoftware.gestioncurricular.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@ConfigurationProperties(prefix = "supabase")
public class SupabaseProperties {

    private String url;
    private String anonKey;
    private String jwtSecret;
    private String serviceRoleKey;

}
