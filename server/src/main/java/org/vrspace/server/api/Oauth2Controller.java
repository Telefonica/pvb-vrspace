package org.vrspace.server.api;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.vrspace.server.api.model.PvbUserResponse;
import org.vrspace.server.core.ClientFactory;
import org.vrspace.server.core.VRObjectRepository;
import org.vrspace.server.obj.Client;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/oauth2")
public class Oauth2Controller {
  @Autowired
  VRObjectRepository db;
  @Autowired
  ClientFactory clientFactory;

  private static final String TEST_TOKEN_URL = "http://localhost:8000/api/v1/login/test-token";

  private String getPvbUsername(String accessToken) {
    String username = null;

    try {
      if (accessToken != null && !accessToken.isEmpty()) {
        RestTemplate restTemplate = new RestTemplate();
      
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
  
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(new LinkedMultiValueMap<>(), headers);
        ResponseEntity<PvbUserResponse> response = restTemplate.postForEntity(TEST_TOKEN_URL, requestEntity, PvbUserResponse.class);

        PvbUserResponse body = response.getBody();
        
        if (body != null) {
          username = body.getFullName() != null ? body.getFullName() : body.getEmail();
        }
      }
    } catch (Exception e) {
      log.error("getPvbUsername", e);
    }

    return username;
  }

  @GetMapping("/login")
  public ResponseEntity<String> login(@RequestHeader String authorization, HttpSession session, HttpServletRequest request) {
    String accessToken = null;
    if (authorization != null && !authorization.isBlank() && authorization.startsWith("Bearer ")) {
			accessToken = authorization.substring(7, authorization.length());
    }
    
    String name = getPvbUsername(accessToken);

    if (ObjectUtils.isEmpty(name)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
    }
    
    log.debug("login as:" + name);
    String identity = identity(name);

    Client client = db.getClientByName(name);
    if (client != null) {
      if (client.getIdentity() != null && client.getIdentity().equals(identity)) {
        log.debug("Welcome back: " + name);
      } else {
        throw new ApiException("Someone else uses this name: " + name);
      }
    } else {
      log.debug("Welcome new user: " + name);
      client = new Client(name);
      client.setIdentity(identity);
      client = db.save(client);
    }

    session.setAttribute(clientFactory.clientAttribute(), name);

    String referer = request.getHeader(HttpHeaders.REFERER);
    return ResponseEntity.status(HttpStatus.FOUND).header("Location", referer).body("Redirecting to " + referer);
  }

  private String identity(String name) {
    return "local:" + name;
  }

}
