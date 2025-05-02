package org.unisoftware.gestioncurricular.security.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.unisoftware.gestioncurricular.dto.AuthRequest;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticación", description = "Endpoints relacionados con la autenticación de usuarios")
public class AuthController {

    @Autowired
    private SupabaseAuthService authService;

    @PostMapping("/signup")
    @Operation(
            summary = "Registrar un nuevo usuario",
            description = "Permite registrar un nuevo usuario proporcionando un correo electrónico y una contraseña.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos de registro del usuario",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthRequest.class)
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Usuario registrado exitosamente"),
                    @ApiResponse(responseCode = "400", description = "Error en los datos proporcionados")
            }
    )
    public ResponseEntity<String> signUp(@RequestBody AuthRequest authRequest) {
        try {
            String response = authService.signUp(authRequest.getEmail(), authRequest.getPassword());
            return ResponseEntity.status(201).body(response);  // Status 201 for Created
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());  // Status 400 for Bad Request
        }
    }

    @PostMapping("/signin")
    @Operation(
            summary = "Iniciar sesión",
            description = "Permite iniciar sesión proporcionando un correo electrónico y una contraseña. Devuelve un token JWT en caso de éxito.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Credenciales del usuario",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthRequest.class)
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Inicio de sesión exitoso, devuelve el token JWT"),
                    @ApiResponse(responseCode = "400", description = "Credenciales inválidas")
            }
    )
    public ResponseEntity<?> signIn(@RequestBody AuthRequest authRequest) {
        try {
            AuthTokens tokens = authService.signIn(authRequest.getEmail(), authRequest.getPassword());
            return ResponseEntity.ok(tokens);  // 200 OK
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());  // 400 Bad Request
        }
    }

    @PostMapping("/refresh")
    @Operation(
            summary = "Refrescar el token de acceso",
            description = "Permite obtener un nuevo access_token usando un refresh_token previamente emitido.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Refresh token válido emitido previamente",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RefreshRequest.class)
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Token refrescado exitosamente, devuelve nuevos tokens"),
                    @ApiResponse(responseCode = "400", description = "Error al refrescar el token")
            }
    )
    public ResponseEntity<?> refresh(@RequestBody RefreshRequest request) {
        try {
            AuthTokens tokens = authService.refreshAccessToken(request.getRefreshToken());
            return ResponseEntity.ok(tokens);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }


}
