package com.hubspot.demo.controller;

import com.hubspot.demo.domain.port.in.HubSpotContactController;
import com.hubspot.demo.service.ContactService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/hubspot")
public class HubSpotContactControllerImpl implements HubSpotContactController{

    @Autowired
    private ContactService contactService;

    @Override
    @GetMapping("/contacts")
    public ResponseEntity<String> getContacts() {
        try {
            String contacts = contactService.getContacts();
            return ResponseEntity.ok(contacts);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            log.error("Erro ao obter contatos do HubSpot: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao obter contatos do HubSpot");
        }
    }

    @Override
    @PostMapping("/create-contact")
    public ResponseEntity<String> createContact(@RequestBody String contactJson) {
        try {
            String response = contactService.createContact(contactJson);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            log.error("Erro ao criar contato no HubSpot: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao criar o contato no HubSpot");
        }
    }
}
