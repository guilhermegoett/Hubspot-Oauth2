package com.meetime.demo.controller;

import lombok.extern.slf4j.Slf4j;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meetime.demo.utils.SessionManager;
import jakarta.servlet.http.HttpServletRequest;


@Slf4j
@RestController
@RequestMapping("/hubspot")
public class HubSpotAuthController {

    @Value("${clientid}")
    private String CLIENT_ID;

    @Value("${secretid}")
    private String CLIENT_SECRET;

    @Value("${redirecturi}")
    private String REDIRECT_URI;

    @Value("${authorizationuri}")
    private String AUTHORIZATION_URI;

    @Value("${tokenuri}")
    private String TOKEN_URI;

    @Value("${scope}")
    private String SCOPES;

    private SessionManager session;

    @GetMapping("/authorize")
    public ResponseEntity<Void> getAuthorizationUrl(HttpServletRequest request) {
        String state = criaSessao(request);

        String url = criaUrlOauth(state);
                
        return ResponseEntity.status(HttpStatus.FOUND).header(HttpHeaders.LOCATION, url).build();
    }

    private String criaUrlOauth(String state) {
        String url = AUTHORIZATION_URI +
                "?client_id=" + CLIENT_ID +
                "&redirect_uri=" + REDIRECT_URI +
                "&scope=" + SCOPES +
                "&response_type=code" +
                "&state=" + state;
        return url;
    }

    private String criaSessao(HttpServletRequest request) {
        // Gerar um valor unico para o state
        String state = UUID.randomUUID().toString();
        session = new SessionManager(request.getSession());
        // Armazena na sessao do usuario
        session.setState(state);
        return state;
    }

    @GetMapping("/callback")
    public ResponseEntity<String> exchangeCodeForAccessToken(
        @RequestParam("code") String authorizationCode,
        @RequestParam(value = "state", required = false) String state,
        HttpServletRequest request) throws UnsupportedEncodingException {

        // Verifica se o state recebido corresponde ao esperado
        if (session.getState() == null || !session.getState().equals(state)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid state parameter");
        }

        // Remove o state da sessao apos a validacao (seguranca)
        session.clearState();

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = criaHeaderToken();
        String body = criaBodyToken(authorizationCode);
        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        return pegaToken(restTemplate, entity);
    }

    private ResponseEntity<String> pegaToken(RestTemplate restTemplate, HttpEntity<String> entity) {
        try {
            ResponseEntity<String> response = restTemplate.exchange(TOKEN_URI, HttpMethod.POST, entity, String.class);
            // Extrai o token de acesso do JSON retornado
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonResponse = objectMapper.readTree(response.getBody());
            session.setAccessToken(jsonResponse.get("access_token").asText());

            return ResponseEntity.ok("Access Token armazenado com sucesso!");
        } catch (Exception e) {
            log.error("Erro ao trocar o código por token: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao trocar o código por um token");
        }
    }

    private HttpHeaders criaHeaderToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return headers;
    }

    private String criaBodyToken(String authorizationCode) throws UnsupportedEncodingException {
        String encodedRedirectUri = URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8.toString());
        String url = "grant_type=authorization_code" +
                "&code=" + authorizationCode +
                "&redirect_uri=" + encodedRedirectUri +
                "&client_id=" + CLIENT_ID +
                "&client_secret=" + CLIENT_SECRET;
        return url;
    }

    // Endpoint para fazer uma chamada à API do HubSpot usando o token de acesso
    @GetMapping("/contatos")
    public ResponseEntity<String> getContacts() {
        if (!session.hasAccessToken()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Access Token não encontrado. Faça a autenticação primeiro.");
        }
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + session.getAccessToken());

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            // Realizando a chamada para a API HubSpot (get contacts)
            ResponseEntity<String> response = restTemplate.exchange(
                    "https://api.hubapi.com/crm/v4/objects/contacts", HttpMethod.GET, entity, String.class);

            // Retornando a resposta da API
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            log.error("Erro ao obter contatos do HubSpot: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao obter contatos do HubSpot");
        }
    }

    @PostMapping("/criarcontato")
    public ResponseEntity<String> createContact(@RequestBody String contactJson, HttpServletRequest request) {
        if (!session.hasAccessToken()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Access Token não encontrado. Faça a autenticação primeiro.");
        }

        // Configurando o RestTemplate para fazer a requisição à API HubSpot
        RestTemplate restTemplate = new RestTemplate();

        // Definindo os cabeçalhos da requisição
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + session.getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Criando o corpo da requisição (os dados do contato a serem criados)
        HttpEntity<String> entity = new HttpEntity<>(contactJson, headers);

        try {
            // Realizando a chamada para a API HubSpot (create contact)
            ResponseEntity<String> response = restTemplate.exchange(
                    "https://api.hubapi.com/crm/v4/objects/contacts", HttpMethod.POST, entity, String.class);

            // Verifica os cabeçalhos de rate limit
            HttpHeaders responseHeaders = response.getHeaders();
            String remainingCalls = responseHeaders.getFirst("X-HubSpot-Rate-Limit-Remaining");
            String resetTime = responseHeaders.getFirst("X-HubSpot-Rate-Limit-Reset");

            if (remainingCalls != null && Integer.parseInt(remainingCalls) == 0) {
                // Se o limite de chamadas for 0, esperamos até o reset
                long resetTimeMillis = Long.parseLong(resetTime) * 1000;
                long currentTime = System.currentTimeMillis();

                // Se o tempo de reset ainda não chegou, espera até o reset
                if (resetTimeMillis > currentTime) {
                    long waitTime = resetTimeMillis - currentTime;
                    System.out.println("Limite de chamadas atingido. Aguardando " + waitTime / 1000 + " segundos.");
                    Thread.sleep(waitTime); // Espera até o reset do limite
                }
            }

            // Retorna a resposta da API do HubSpot
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (Exception e) {
            log.error("Erro ao criar contato no HubSpot: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao criar o contato no HubSpot");
        }
    }

}
