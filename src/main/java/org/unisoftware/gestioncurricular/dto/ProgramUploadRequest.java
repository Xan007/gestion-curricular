package org.unisoftware.gestioncurricular.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Setter
@Getter
public class ProgramUploadRequest {

    private MultipartFile file;

    public ProgramUploadRequest() {}

    public ProgramUploadRequest(MultipartFile file) {
        this.file = file;
    }

}
