package org.unisoftware.gestioncurricular.dto;

import lombok.Getter;

@Getter
public class ProgramFileDTO {
    private Long id;
    private String url;

    public ProgramFileDTO(Long id, String url) {
        this.id = id;
        this.url = url;
    }

}