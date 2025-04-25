package org.unisoftware.gestioncurricular.security.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.unisoftware.gestioncurricular.dto.AuthRequest;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private SupabaseAuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<String> signUp(@RequestBody AuthRequest authRequest) {
        try {
            String response = authService.signUp(authRequest.getEmail(), authRequest.getPassword());
            return ResponseEntity.status(201).body(response);  // Status 201 for Created
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());  // Status 400 for Bad Request
        }
    }

    @PostMapping("/signin")
    public ResponseEntity<String> signIn(@RequestBody AuthRequest authRequest) {
        try {
            String jwt = authService.signIn(authRequest.getEmail(), authRequest.getPassword());
            return ResponseEntity.ok(jwt);  // Status 200 for OK
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());  // Status 400 for Bad Request
        }
    }
}
