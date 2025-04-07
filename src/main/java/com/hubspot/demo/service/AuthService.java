package com.hubspot.demo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hubspot.demo.utils.SessionManager;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
public class AuthService {

    @Value("${clientid}")
    private String clientId;

    @Value("${secretid}")
    private String clientSecret;

    @Value("${redirecturi}")
    private String redirectUri;

    @Value("${authorizationuri}")
    private String authorizationUri;

    @Value("${tokenuri}")
    private String tokenUri;

    @Value("${scope}")
    private String scopes;

    @Autowired
    private ObjectFactory<SessionManager> sessionFactory;

    public String createOauthUrl() {
        String state = createSession();
        return authorizationUri +
               "?client_id=" + clientId +
               "&redirect_uri=" + redirectUri +
               "&scope=" + scopes +
               "&response_type=code" +
               "&state=" + state;
    }

    public String createSession() {
        System.out.println("SessionManager: " + sessionFactory.getObject());
        String state = UUID.randomUUID().toString();
        sessionFactory.getObject().setState(state);
        return state;
    }

    public boolean isValidState(String state) {
        SessionManager session = sessionFactory.getObject();
        return session.getState() != null && session.getState().equals(state);
    }

    public String createTokenBody(String authorizationCode) throws UnsupportedEncodingException {
        String encodedRedirectUri = URLEncoder.encode(redirectUri, StandardCharsets.UTF_8.toString());
        return "grant_type=authorization_code" +
               "&code=" + authorizationCode +
               "&redirect_uri=" + encodedRedirectUri +
               "&client_id=" + clientId +
               "&client_secret=" + clientSecret;
    }

    public ResponseEntity<String> fetchToken(String authorizationCode) throws UnsupportedEncodingException {
        RestTemplate restTemplate = new RestTemplate();
        sessionFactory.getObject().clearState();
        HttpHeaders headers = createTokenHeaders();
        String body = createTokenBody(authorizationCode);

        try {
            HttpEntity<String> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.exchange(tokenUri, HttpMethod.POST, entity, String.class);
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonResponse = objectMapper.readTree(response.getBody());
            sessionFactory.getObject().setAccessToken(jsonResponse.get("access_token").asText());            
            return ResponseEntity.ok("Access Token armazenado com sucesso!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao trocar o código por um token");
        }
    }

    private HttpHeaders createTokenHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return headers;
    }
}
