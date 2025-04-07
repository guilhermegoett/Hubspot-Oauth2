package com.hubspot.demo.controller;

import com.hubspot.demo.domain.port.in.HubSpotAuthController;
import com.hubspot.demo.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/hubspot")
public class HubSpotAuthControllerImpl implements HubSpotAuthController {

    private final AuthService authService;

    public HubSpotAuthControllerImpl(AuthService authService) {
        this.authService = authService;
    }

    @Override
    @GetMapping("/authorize")
    public ResponseEntity<Void> getAuthorizationUrl(HttpServletRequest request) {
        String url = authService.createOauthUrl();
        return ResponseEntity.status(HttpStatus.FOUND).header("Location", url).build();
    }

    @Override
    @GetMapping("/callback")
    public ResponseEntity<String> exchangeCodeForAccessToken(
        @RequestParam("code") String authorizationCode,
        @RequestParam(value = "state", required = false) String state,
        HttpServletRequest request) throws UnsupportedEncodingException {

        if (!authService.isValidState(state)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid state parameter");
        }

        return authService.fetchToken(authorizationCode);
    }
}
