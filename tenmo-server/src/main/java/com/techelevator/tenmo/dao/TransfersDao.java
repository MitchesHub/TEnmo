package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Transfers;

import java.math.BigDecimal;
import java.util.List;

public interface TransfersDao {



    public List<Transfers> getAllTransfers(int userId);
    public List<Transfers>getPendingRequests(int userId);
    public Transfers getTransfersById(int transferId);
    public String sendTransfer(int userFrom, int userTo, BigDecimal amount);
    public String requestTransfer(int userFrom, int userTo, BigDecimal amount);
    public String updateTransferRequest(Transfers transfers, int statusId);


}
