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
    private TransfersDao transfersDao;

    @RequestMapping(value = "account/transfers/{id}", method = RequestMethod.GET)
    public List<Transfers> getAllMyTransfers(@PathVariable long id) {
        return transfersDao.getAllTransfers(id);
    }

    @RequestMapping(path = "transfers/{id}", method = RequestMethod.GET)
    public Transfers getSelectedTransfer(@PathVariable long id) {
        return transfersDao.getTransferById(id);
    }

    @RequestMapping(path = "transfer", method = RequestMethod.POST)
    public String sendTransferRequest(@RequestBody Transfers transfer) {
        String results = transfersDao.sendTransfer(transfer.getAccountFrom(), transfer.getAccountTo(), transfer.getAmount());
        return results;
    }

    @RequestMapping(path = "request", method = RequestMethod.POST)
    public String requestTransferRequest(@RequestBody Transfers transfer) {
        String results = transfersDao.requestTransfer(transfer.getAccountFrom(), transfer.getAccountTo(), transfer.getAmount());
        return results;
    }

    @RequestMapping(value = "request/{id}", method = RequestMethod.GET)
    public List<Transfers> getAllTransferRequests(@PathVariable int id) {
        List<Transfers> output = transfersDao.getPendingRequests(id);
        return output;
    }

    @RequestMapping(path = "transfer/status/{statusId}", method = RequestMethod.PUT)
    public String updateRequest(@RequestBody Transfers transfer, @PathVariable int statusId) {
        String output = transfersDao.updateTransferRequest(transfer, statusId);
        return output;
    }
}