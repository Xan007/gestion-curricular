package org.unisoftware.gestioncurricular.dto.files;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class UpdateCourseMicroFileDTO {
    public LocalDateTime date;
    public Boolean isMain;
}
