package com.farpad.dripServer.controllers;

import com.farpad.dripServer.models.clientSideData.CustomerFormData;
import com.farpad.dripServer.services.EmailService;
import jakarta.annotation.Nonnull;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping("/email")
public class EmailController {
    private final EmailService emailService;

    @PostMapping("/order-history")
    public ResponseEntity<Void> sendOrderHistoryEmail(@Nonnull @RequestBody CustomerFormData body){
        emailService.sendOrderHistoryEmail(body.getEmail());
        return new ResponseEntity<>(HttpStatus.OK);
    }
}