package org.unisoftware.gestioncurricular.frontend.service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.unisoftware.gestioncurricular.frontend.util.SessionManager;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.io.FileSystemResource;
import java.io.File;
import org.springframework.util.MultiValueMap;
import org.springframework.util.LinkedMultiValueMap;


@Service
public class ExcelUploadService {
    public void uploadPlan(Long programId, File file) {
        String url = "http://localhost:8080/programas/" + programId + "/upload-plan";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("Authorization", "Bearer " + SessionManager.getInstance().getToken());

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<String, Object>();
        body.add("file", new FileSystemResource(file));

        HttpEntity<MultiValueMap<String, Object>> requestEntity =
                new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.postForEntity(url, requestEntity, String.class);
    }
}