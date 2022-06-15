package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.TransfersDao;
import com.techelevator.tenmo.model.Transfers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@PreAuthorize("isAuthenticated()")

public class TransferController {
    @Autowired
    private TransfersDao transferDao;

    @RequestMapping(value = "account/transfers/{id}", method = RequestMethod.GET)
    public List<Transfers> getAllMyTransfers(@PathVariable int id) {
        List<Transfers> outcome = transferDao.getAllTransfers(id);
        return outcome;
    }

    @RequestMapping(path = "transfers/{id}", method = RequestMethod.GET)
    public Transfers getSelectedTransfer(@PathVariable int id) {
        Transfers transfer = transferDao.getTransfersById(id);
        return transfer;
    }

    @RequestMapping(path = "transfer", method = RequestMethod.POST)
    public String sendTransfer(@RequestBody Transfers transfers) {
        String results = transferDao.sendTransfer(transfers.getAccountFrom(),
                transfers.getAccountTo(), transfers.getAmount());
        return results;
    }

    @RequestMapping(path = "request", method = RequestMethod.POST)
    public String requestTransfer(@RequestBody Transfers transfers) {
        String results = transferDao.requestTransfer(transfers.getAccountFrom(),
                transfers.getAccountTo(), transfers.getAmount());
        return results;

    }
}
