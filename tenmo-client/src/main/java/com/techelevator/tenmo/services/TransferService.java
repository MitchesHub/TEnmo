package com.techelevator.tenmo.services;

import com.techelevator.tenmo.model.AuthenticatedUser;
import com.techelevator.tenmo.model.Transfers;
import io.cucumber.java.sl.In;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.Scanner;

public class TransferService {
    private String BASE_URL;
    private RestTemplate restTemplate = new RestTemplate();
    private AuthenticatedUser currentUser;

    public TransferService(String url, AuthenticatedUser currentUser) {
        BASE_URL = url;
        this.currentUser = currentUser;
    }

    public Transfers[] transferList() {
        Transfers[] outcome = null;
        try {
            outcome = restTemplate.exchange(BASE_URL + "account/transfers", HttpMethod.GET, makeAuthEntity(),
                    Transfers[].class).getBody();
            System.out.println("-------------------------------------------\r" +
                    "Transfers\r\n" + "ID          From/To                 Amount\r" +
                            "-------------------------------------------\r");
            String fromTo = "";
            String name = "";
            for (Transfers i : outcome) {
                if (currentUser.getUser().getId() == i.getAccountFrom()) {
                    fromTo = "From: ";
                    name = i.getUserTo();
                } else {
                    fromTo = "To: ";
                    name = i.getUserFrom();
                }
                System.out.println(i.getTransferId() + "\t" + fromTo + name +
                        "\t$" + i.getAmount());
            }
            System.out.println("-------------------------------------------\r" +
                    "Please enter transfer ID to view details (0 to cancel):");
            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine();
            if (Integer.parseInt(input) == 0) {
                boolean givenTransferId = false;
                for (Transfers i : outcome) {
                    if (Integer.parseInt(input) == i.getTransferId()) {
                        Transfers pending = restTemplate.exchange(BASE_URL +
                                "transfers/" + i.getTransferId(), HttpMethod.GET,
                                makeAuthEntity(), Transfers.class).getBody();
                        givenTransferId = true;
                        assert pending != null;
                        System.out.println("-------------------------------------------\r" +
                                "Transfer Details\r " +
                                "-------------------------------------------\r" +
                                "Id: " + pending.getTransferId() + "\r" +
                                "From: " + pending.getUserFrom() + "\r" +
                                "To: " +pending.getUserTo() + "\r" +
                                "Type: " + pending.getTransferType() + "\r" +
                                "Status: " + pending.getTransferStatus()+ "\r" +
                                "Amount: $" + pending.getAmount() + "\r");
                    }
                }
                if (!givenTransferId) {
                    System.out.println("Transfer ID not valid");
                }
            }
        } catch (Exception exception) {
            System.out.println("Transaction not successful");
        }
        return outcome;
    }

    private HttpEntity makeAuthEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(currentUser.getToken());
        HttpEntity entity = new HttpEntity<>(headers);
        return entity;
    }
//
//    private HttpEntity<Transfers> makeAuthEntity() {
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        headers.setBearerAuth(currentUser.getToken());
//        HttpEntity <Transfers>  entity = new HttpEntity<>(transfer, headers);
//        return entity;
//    }
}
