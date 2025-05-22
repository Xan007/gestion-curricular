package org.unisoftware.gestioncurricular.dto.files;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class CourseMicroFileDTO {
    public Long id;
    public String url;
    public LocalDateTime uploadedAt;
    public LocalDateTime date;
    public Boolean isMain;
}
