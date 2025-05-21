package org.unisoftware.gestioncurricular.controller.files;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Controlador para testear la subida de archivos a Supabase Storage.
 */
@RestController
@RequestMapping("/test-upload")
@RequiredArgsConstructor
@Tag(name = "Test Upload", description = "Endpoints para probar subida de archivos a Supabase Storage")
public class TestUploadController {

    @Value("${supabase.service-role-key}")
    private String supabaseServiceRoleKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @Operation(summary = "Subir archivo de prueba a Supabase Storage con URL completa y devolver la misma URL")
    @PostMapping("/file")
    public ResponseEntity<String> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("url") String url // URL completa donde subir el archivo (PUT)
    ) throws IOException {

        // Headers para la petición
        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", supabaseServiceRoleKey);
        headers.set("Authorization", "Bearer " + supabaseServiceRoleKey);
        headers.setContentType(MediaType.parseMediaType(file.getContentType()));

        // Cuerpo con los bytes del archivo
        HttpEntity<byte[]> requestEntity = new HttpEntity<>(file.getBytes(), headers);

        // Hacer petición PUT a la URL completa que envía el cliente
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, requestEntity, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            // Devolver la misma URL recibida si se subió correctamente
            return ResponseEntity.ok(url);
        } else {
            return ResponseEntity.status(response.getStatusCode())
                    .body("Error subiendo archivo: " + response.getBody());
        }
    }
}
