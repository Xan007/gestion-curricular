package org.unisoftware.gestioncurricular.dto.files;

import lombok.Data;

import java.util.UUID;

@Data
public class ProposalFileDTO {
    private UUID id;
    private String url;
}
