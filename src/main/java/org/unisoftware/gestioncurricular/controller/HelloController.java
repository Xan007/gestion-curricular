package org.unisoftware.gestioncurricular.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class HelloController {

    @GetMapping("/public/hello")
    public String publicHello() {
        return "Hello from public endpoint!";
    }

    @PreAuthorize("hasRole('DECANO')")
    @GetMapping("/private/hello")
    public String privateHello() {
        return "Hello from protected endpoint!";
    }
}
