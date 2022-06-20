package com.techelevator.tenmo.services;

import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.AuthenticatedUser;
import com.techelevator.tenmo.model.Transfers;
import com.techelevator.tenmo.model.User;
import io.cucumber.java.sl.In;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.io.Console;
import java.math.BigDecimal;
import java.util.Scanner;

public class TransferService {
    private String BASE_URL;
    private RestTemplate restTemplate = new RestTemplate();
    private AuthenticatedUser currentUser;
    private ConsoleService consoleService = new ConsoleService();
    private final long ACCOUNT_ID_OFFSET = 1000;

    public TransferService(String url, AuthenticatedUser currentUser) {
        BASE_URL = url;
        this.currentUser = currentUser;
    }

    public Transfers[] transferList() {
        Transfers[] output = null;

        try {
            output = restTemplate.exchange(BASE_URL + "account/transfers/" + currentUser.getUser().getId(),
                    HttpMethod.GET, makeAuthEntity(), Transfers[].class).getBody();

            System.out.println("-------------------------------------------" + System.lineSeparator() +
                    "Transfers" + System.lineSeparator() +
                    "ID\t\tFrom/To\t\t\t\tAmount" + System.lineSeparator() +
                    "-------------------------------------------");

            String toFrom = "";
            String name = "";

            for (Transfers i : output) {
                if (currentUser.getUser().getId() + ACCOUNT_ID_OFFSET == i.getAccountFrom()) {
                    toFrom = "To: ";
                    name = i.getUserTo();
                } else {
                    toFrom = "From: ";
                    name = i.getUserFrom();
                }

                System.out.println(i.getTransferId() + "\t" + toFrom + name + "\t\t\t$" + i.getAmount());
            }

            System.out.println("-------------------------------------------\r" +
                    "Please enter transfer ID to view details (0 to cancel):");

            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine();

            if (Long.parseLong(input) != 0) {
                boolean givenTransferId = false;

                for (Transfers i : output) {
                    if (Long.parseLong(input) == i.getTransferId()) {
                        Transfers pending = restTemplate.exchange(BASE_URL + "transfers/" + i.getTransferId(),
                                HttpMethod.GET, makeAuthEntity(), Transfers.class).getBody();
                        givenTransferId = true;
                        assert pending != null;

                        System.out.println("-------------------------------------------" + System.lineSeparator() +
                                "Transfer Details" + System.lineSeparator() +
                                "-------------------------------------------" + System.lineSeparator() +
                                "Id: " + pending.getTransferId() + System.lineSeparator() +
                                "From: " + pending.getUserFrom() + System.lineSeparator() +
                                "To: " +pending.getUserTo() + System.lineSeparator() +
                                "Type: " + pending.getTransferType() + System.lineSeparator() +
                                "Status: " + pending.getTransferStatus() + System.lineSeparator() +
                                "Amount: $" + pending.getAmount());
                    }
                }

                if (!givenTransferId) {
                    System.out.println("Transfer ID not valid");
                }
            }
        } catch (Exception exception) {
            System.out.println("Transaction not successful");
        }

        return output;
    }

    public void sendBucks() {
        User[] users = null;
        Transfers transfer = new Transfers();

        try {
            Scanner scanner = new Scanner(System.in);
            users = restTemplate.exchange(BASE_URL + "api/users", HttpMethod.GET, makeAuthEntity(), User[].class).getBody();

            System.out.println("-------------------------------------------" + System.lineSeparator() +
                    "Users" + System.lineSeparator() +
                    "ID\t\t\tName" + System.lineSeparator() +
                    "-------------------------------------------");

            for (User i : users) {
                if (i.getId() != currentUser.getUser().getId()) {
                    System.out.println(i.getId() + "\t\t" + i.getUsername());
                }
            }

            System.out.print("-------------------------------------------" + System.lineSeparator());

            transfer.setAccountTo(consoleService.promptForLong("Enter ID of user you are sending to (0 to cancel): "));
            transfer.setAccountFrom(currentUser.getUser().getId() + ACCOUNT_ID_OFFSET);

            if (transfer.getAccountTo() != 0) {
                transfer.setAmount(consoleService.promptForBigDecimal("Enter amount: "));
                String output = restTemplate.exchange(BASE_URL + "transfer", HttpMethod.POST, makeTransferEntity(transfer), String.class).getBody();
                System.out.println(output);
            }
        } catch (Exception e) {
            System.out.println("Bad input!");
        }
    }

    public void requestBucks() {
        User[] users = null;
        Transfers transfer = new Transfers();

        try {
            Scanner scanner = new Scanner(System.in);
            users = restTemplate.exchange(BASE_URL + "api/users", HttpMethod.GET, makeAuthEntity(), User[].class).getBody();

            System.out.println("-------------------------------------------" + System.lineSeparator() +
                    "Users" + System.lineSeparator() +
                    "ID\t\t\tName" + System.lineSeparator() +
                    "-------------------------------------------");

            for (User i : users) {
                if (i.getId() != currentUser.getUser().getId()) {
                    System.out.println(i.getId() + "\t\t" + i.getUsername());
                }
            }

            System.out.print("-------------------------------------------" + System.lineSeparator());

            transfer.setAccountTo(currentUser.getUser().getId());
            transfer.setAccountFrom(consoleService.promptForLong("Enter ID of user you are requesting from (0 to cancel): ") + ACCOUNT_ID_OFFSET);

            if (transfer.getAccountTo() != 0) {
                transfer.setAmount(consoleService.promptForBigDecimal("Enter amount: "));
                String output = restTemplate.exchange(BASE_URL + "request", HttpMethod.POST, makeTransferEntity(transfer), String.class).getBody();
                System.out.println(output);
            }
        } catch (Exception e) {
            System.out.println("Bad input!");
        }
    }

    public Transfers[] transfersRequestList() {
        Transfers [] output = null;
        String results;

        try {
            output = restTemplate.exchange(BASE_URL + "request/" + currentUser.getUser().getId(), HttpMethod.GET, makeAuthEntity(), Transfers[].class).getBody();

            System.out.println("-------------------------------------------" + System.lineSeparator() +
                    "Pending Transfers" + System.lineSeparator() +
                    "ID\t\t\tFrom/To\t\t\tAmount" + System.lineSeparator() +
                    "-------------------------------------------");

            String toOrFrom = "";
            String name = "";

            for (Transfers i : output) {
                if (currentUser.getUser().getId() + ACCOUNT_ID_OFFSET == i.getAccountFrom()) {
                    toOrFrom = "To: ";
                    name = i.getUserTo();
                } else {
                    toOrFrom = "From: ";
                    name = i.getUserFrom();
                }

                System.out.println(i.getTransferId() +"\t\t" + toOrFrom + name + "\t\t$" + i.getAmount());
            }

            System.out.print("-------------------------------------------" + System.lineSeparator());

            long input = consoleService.promptForLong("Please enter transfer ID to approve/reject (0 to cancel): ");

            if (input != 0) {
                boolean foundTransferId = false;

                for (Transfers i : output) {
                    if (i.getAccountTo() != currentUser.getUser().getId() + ACCOUNT_ID_OFFSET) {
                        if (input == i.getTransferId()) {
                            System.out.print("-------------------------------------------" + System.lineSeparator() +
                                    i.getTransferId() +"\t\t" + toOrFrom + name + "\t\t$" + i.getAmount() + System.lineSeparator() +
                                    "1: Approve" + System.lineSeparator() +
                                    "2: Reject" + System.lineSeparator() +
                                    "0: Don't approve or reject" + System.lineSeparator() +
                                    "--------------------------" + System.lineSeparator());

                            try {
                                int id = consoleService.promptForInt("Please choose an option: ");

                                // only options 1 and 2 do anything
                                if (id == 1 || id == 2) {
                                    results = restTemplate.exchange(BASE_URL + "transfer/status/" + (id + 1), HttpMethod.PUT, makeTransferEntity(i), String.class).getBody();
                                    System.out.println(results);
                                    foundTransferId = true;
                                }
                            } catch (NumberFormatException e) {
                                System.out.println("Not a valid transfer option");
                            }

                            if (!foundTransferId) {
                                System.out.println("Not a valid transfer ID");
                            }
                        }
                    } else {
                        System.out.println("You can not approve/reject your own request.");
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Something went wrong!");
        }

        return output;
    }

    private HttpEntity<Transfers> makeTransferEntity(Transfers transfer) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(currentUser.getToken());
        HttpEntity<Transfers> entity = new HttpEntity<>(transfer, headers);

        return entity;
    }

    private HttpEntity makeAuthEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(currentUser.getToken());
        HttpEntity entity = new HttpEntity<>(headers);

        return entity;
    }
}
