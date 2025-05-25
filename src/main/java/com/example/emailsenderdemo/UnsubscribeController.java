package com.example.emailsenderdemo;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class UnsubscribeController {

    @GetMapping("/unsubscribe")
    public ResponseEntity<Void> unsubscribe(@RequestParam String email) {
        boolean result = EmailSenderdemoApplication.unsubscribeEmail(email);

        if (result) {
            return ResponseEntity.status(302).header("Location", "/unsubscribed").build();
        } else {
            return ResponseEntity.status(302).header("Location", "/unsubscribed-error").build();
        }
    }
}
