package com.techelevator.tenmo.model;

public class TransferStatuses {

    private int transferStatusId;
    private String TransferStatusDesc;

    public int getTransferStatusId() {
        return transferStatusId;
    }

    public void setTransferStatusId(int transferStatusId) {
        this.transferStatusId = transferStatusId;
    }

    public String getTransferStatusDesc() {
        return TransferStatusDesc;
    }

    public void setTransferStatusDesc(String transferStatusDesc) {
        TransferStatusDesc = transferStatusDesc;
    }
}
