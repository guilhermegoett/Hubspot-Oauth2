package com.hubspot.demo.domain.port.in;

import java.io.UnsupportedEncodingException;

import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpServletRequest;

public interface HubSpotAuthController {

    ResponseEntity<Void> getAuthorizationUrl(HttpServletRequest request);

    ResponseEntity<String> exchangeCodeForAccessToken(String authorizationCode, String state, HttpServletRequest request) throws UnsupportedEncodingException;

   
}
