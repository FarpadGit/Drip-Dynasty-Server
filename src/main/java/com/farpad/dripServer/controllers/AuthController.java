package com.farpad.dripServer.controllers;

import com.farpad.dripServer.models.clientSideData.Credentials;
import com.farpad.dripServer.services.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {

    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Credentials credentials){
        String token = authService.login(credentials);
        return new ResponseEntity<>(token, HttpStatus.OK);
    }

    @GetMapping("/enticate")
    public ResponseEntity<Void> checkCredentials(){
        return new ResponseEntity<>(HttpStatus.OK);
    }

}