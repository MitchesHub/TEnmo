package com.techelevator.tenmo.services;

import com.techelevator.tenmo.model.AuthenticatedUser;
import com.techelevator.tenmo.model.UserCredentials;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;


import java.math.BigDecimal;

public class AccountService {
    private String BASE_URL;
    private RestTemplate restTemplate = new RestTemplate();
    public AuthenticatedUser currentUser;

    public AccountService(String url, AuthenticatedUser currentUser) {
        BASE_URL = url;
        this.currentUser = currentUser;
    }

    public BigDecimal getBalance() {
        BigDecimal balance = null;
        try {
            balance = restTemplate.exchange(BASE_URL + "api/balance", HttpMethod.GET,
                    makeAuthEntity(), BigDecimal.class).getBody();
            System.out.println("Your current account balance is: $" + balance);
        } catch (RestClientResponseException exception) {
            System.out.println("Unable to retrieve balance");
        }
        return balance;
    }

    private HttpEntity<UserCredentials> makeAuthEntity() {
        org.springframework.http.HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(currentUser.getToken());
        HttpEntity entity = new HttpEntity<>(headers);
        return entity;
    }
}
