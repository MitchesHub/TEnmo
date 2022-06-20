package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Transfers;

import java.math.BigDecimal;
import java.util.List;

public interface TransfersDao {
    public List<Transfers> getAllTransfers(long userId);
    public List<Transfers> getPendingRequests(long userId);
    public Transfers getTransferById(long transferId);
    public String sendTransfer(long userFrom, long userTo, BigDecimal amount);
    public String requestTransfer(long userFrom, long userTo, BigDecimal amount);
    public String updateTransferRequest(Transfers transfers, int statusId);
}
