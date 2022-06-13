package com.techelevator.tenmo.services;

import com.techelevator.tenmo.model.AuthenticatedUser;
import com.techelevator.tenmo.model.UserCredentials;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;


import java.math.BigDecimal;

public class AccountService {
    private String BASE_URL;
    private RestTemplate restTemplate = new RestTemplate();
    public AuthenticatedUser currentUser;

    private AccountService(String url, AuthenticatedUser currentUser) {
        this.currentUser = currentUser;
        BASE_URL = url;
    }

    public BigDecimal getBalance() {
        BigDecimal balance = new BigDecimal(0);
        try {
            balance = restTemplate.exchange(BASE_URL + "balance/" + currentUser.getUser().getId(), HttpMethod.GET,
                    makeAuthEntity(), BigDecimal.class).getBody();
            System.out.println("Your current account Balance is: $" + balance);
        }catch (RestClientResponseException exception) {
            System.out.println("Unable to retrieve Balance");
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