package org.unisoftware.gestioncurricular.dto;

import lombok.Data;

@Data
public class ProposalUploadRequest {
    public String title;
    public Long courseId;
}
