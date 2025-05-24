package org.unisoftware.gestioncurricular.frontend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FileUploadInfoDTO {
    private String uploadUrl;
    private String fileId; // UUID del FileStorage creado en el backend

    public FileUploadInfoDTO() {
    }

    public FileUploadInfoDTO(String uploadUrl, String fileId) {
        this.uploadUrl = uploadUrl;
        this.fileId = fileId;
    }

    public String getUploadUrl() {
        return uploadUrl;
    }

    public void setUploadUrl(String uploadUrl) {
        this.uploadUrl = uploadUrl;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }
}

