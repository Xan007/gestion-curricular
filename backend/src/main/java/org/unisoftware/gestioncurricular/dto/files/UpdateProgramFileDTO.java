package org.unisoftware.gestioncurricular.dto.files;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UpdateProgramFileDTO {
    private LocalDateTime date;
    private Boolean isMain;
}
