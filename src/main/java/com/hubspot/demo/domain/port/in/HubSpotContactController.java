package com.hubspot.demo.domain.port.in;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

public interface HubSpotContactController {

    ResponseEntity<String> getContacts();

    ResponseEntity<String> createContact(@RequestBody String contactJson);
    
}
