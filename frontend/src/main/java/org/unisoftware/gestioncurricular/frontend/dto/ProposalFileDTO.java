package org.unisoftware.gestioncurricular.frontend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProposalFileDTO {
    private String url;
    private String fileKey; // Puede ser relevante para la subida

    // Constructores, Getters y Setters
    public ProposalFileDTO() {
    }

    public ProposalFileDTO(String url, String fileKey) {
        this.url = url;
        this.fileKey = fileKey;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFileKey() {
        return fileKey;
    }

    public void setFileKey(String fileKey) {
        this.fileKey = fileKey;
    }
}

