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


    @RequestMapping(value = "/account/transfers", method = RequestMethod.GET)
    public List<Transfers> getAllTransfers(@PathVariable int id) {
       List <Transfers> results = transferDao.getAllTransfers(id);
        return results;
    }
    @RequestMapping(path = "/account/transfers{id}", method = RequestMethod.GET)
    public Transfers getSelectedTransfer(@PathVariable int id) {
        return transferDao.getTransfersById(id);
    }

    @RequestMapping(path = "/transfer", method = RequestMethod.POST)
    public String sendTransfer(@RequestBody Transfers transfers) {
        return transferDao.sendTransfer(transfers.getAccountFrom(), transfers.getAccountTo(), transfers.getAmount());
    }

    @RequestMapping(path = "/request", method = RequestMethod.POST)
    public String requestTransfer(@RequestBody Transfers transfers) {
        return transferDao.requestTransfer(transfers.getAccountFrom(), transfers.getAccountTo(), transfers.getAmount());
    }
}
