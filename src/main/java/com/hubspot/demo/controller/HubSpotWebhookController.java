package com.hubspot.demo.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@RestController
@RequestMapping("/hubspot")
public class HubSpotWebhookController {

    @PostMapping("/webhook/contact_creation")
    public ResponseEntity<String> handleContactCreation(@RequestBody String payload, 
                                                         @RequestHeader("X-HubSpot-Signature") String signature) {
        // Aqui voce pode validar a assinatura do HubSpot
        if (!isValidHubSpotSignature(payload, signature)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Assinatura inválida");
        }

        // Log para verificar o payload recebido
        log.info("Recebido evento de criação de contato: {}", payload);

        try {
            // Se necessario, voce pode converter o payload para um objeto JSON
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode eventNode = objectMapper.readTree(payload);

            JsonNode contactData = eventNode.path("object");
            String contactId = contactData.path("id").asText();
            String firstName = contactData.path("properties").path("firstname").asText();
            String lastName = contactData.path("properties").path("lastname").asText();

            // Aqui voce pode salvar os dados em sua base de dados, fazer algo com eles, etc.
            log.info("Novo contato criado: ID = {}, First Name = {}, Last Name = {}", contactId, firstName, lastName);

            // Retorne indicando que o webhook foi processado com sucesso
            return ResponseEntity.ok("Evento de criação de contato processado com sucesso");
        } catch (Exception e) {
            log.error("Erro ao processar o evento de criação de contato: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao processar o evento");
        }
    }

    // Funcao para validar a assinatura do HubSpot
    private boolean isValidHubSpotSignature(String payload, String signature) {
        String secretKey = "sua-chave-secreta"; // Substitua pela chave secreta configurada no HubSpot
        String expectedSignature = calculateSignature(payload, secretKey);
        return signature.equals(expectedSignature);
    }

    private String calculateSignature(String payload, String secretKey) {
        // Implementacao para calcular a assinatura HMAC
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] rawHmac = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return new String(Base64.getEncoder().encode(rawHmac));
        } catch (Exception e) {
            log.error("Erro ao calcular a assinatura: {}", e.getMessage());
            return "";
        }
    }
}
