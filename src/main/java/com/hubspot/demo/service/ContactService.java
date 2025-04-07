package com.hubspot.demo.service;

import com.hubspot.demo.utils.SessionManager;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

@Service
public class ContactService {

    @Value("${contactsUrl}")
    private String contactsUrl;

    @Autowired
    private ObjectFactory<SessionManager> sessionFactory;

    public String getContacts() {
        System.out.println("Entrou na função get Contacts");
        SessionManager session = sessionFactory.getObject();
        
        if (!session.hasAccessToken()) {
            throw new IllegalStateException("Access Token não encontrado. Faça a autenticação primeiro.");
        }
        System.out.println("Token armazenado: " + session.getAccessToken());

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = createAuthorizationHeaders(session);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                contactsUrl, HttpMethod.GET, new HttpEntity<>(headers), String.class);
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao obter contatos do HubSpot", e);
        }
    }

    public String createContact(String contactJson) {
        SessionManager session = sessionFactory.getObject();
        if (!session.hasAccessToken()) {
            throw new IllegalStateException("Access Token não encontrado. Faça a autenticação primeiro.");
        }

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = createAuthorizationHeaders(session);
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                contactsUrl, HttpMethod.POST, new HttpEntity<>(contactJson, headers), String.class);

            checkRateLimit(response.getHeaders());
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao criar contato no HubSpot", e);
        }
    }

    private HttpHeaders createAuthorizationHeaders(SessionManager session) {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + session.getAccessToken());
        return headers;
    }

    private void checkRateLimit(HttpHeaders responseHeaders) throws InterruptedException {
        String remainingCalls = responseHeaders.getFirst("X-HubSpot-Rate-Limit-Remaining");
        String resetTime = responseHeaders.getFirst("X-HubSpot-Rate-Limit-Reset");

        if (remainingCalls != null && Integer.parseInt(remainingCalls) == 0) {
            long resetTimeMillis = Long.parseLong(resetTime) * 1000;
            long currentTime = System.currentTimeMillis();

            if (resetTimeMillis > currentTime) {
                long waitTime = resetTimeMillis - currentTime;
                TimeUnit.MILLISECONDS.sleep(waitTime);
            }
        }
    }
}
