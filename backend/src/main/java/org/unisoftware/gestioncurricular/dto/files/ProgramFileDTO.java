package org.unisoftware.gestioncurricular.dto.files;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ProgramFileDTO {
    private Long id;
    private String url;
    private LocalDateTime uploadedAt;
    private LocalDateTime date;
}