package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.TransferNotFoundException;
import com.techelevator.tenmo.model.Transfers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class JDBCTransfersDao implements TransfersDao {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private AccountDao accountDao;

    @Override
    public List<Transfers> getAllTransfers(int userId) {
        List<Transfers> list = new ArrayList<>();
        String sqlString = "SELECT t.*, tu.username AS userFrom, tub.username AS userTo " +
                "FROM transfer t " +
                "JOIN account a ON t.account_from = a.account_id " +
                "JOIN account b ON t.account_to = b.account_id " +
                "JOIN tenmo_user tu ON a.user_id = tu.user_id " +
                "JOIN tenmo_user tub ON b.user_id = tub.user_id " +
                "WHERE a.user_id = ? OR b.user_id = ?";

        SqlRowSet results = jdbcTemplate.queryForRowSet(sqlString, userId, userId);
        while (results.next()) {
            Transfers transfers = mapRowToTransfer(results);
            list.add(transfers);
        }
        return list;
    }

    @Override
    public Transfers getTransfersById(int transferId) {
        Transfers transfers = new Transfers();
        String sqlString = "SELECT t.*, tu.username AS userFrom, tub.username AS userTo " +
                "FROM transfer t " +
                "JOIN account a ON t.account_from = a.account_id " +
                "JOIN account b ON t.account_to = b.account_id " +
                "JOIN tenmo_user tu ON a.user_id = tu.user_id " +
                "JOIN tenmo_user tub ON b.user_id = tub.user_ids " +
                "WHERE a.user_id = ? OR b.user_id = ?";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sqlString, transferId);
        if (results.next()) {
            transfers = mapRowToTransfer(results);
        } else {
            throw new TransferNotFoundException();
        }
        return transfers;
    }

    @Override
    public String sendTransfer(int userFrom, int userTo, BigDecimal amount) {
        if (userFrom == userTo) {
            return "Invalid, may not send money to yourself";
        }
        if (amount.compareTo(accountDao.getBalance(userFrom)) == -1 && amount.compareTo(new BigDecimal(0)) == 1) {
            String sqlString = "INSERT INTO transfer (transfer_type_id, transfer_status_id, " +
                    "account_from, account_to, amount " +
                    "VALUES (2, 2, ?, ?, ?)"; // 2 = send, 2 = approved
            jdbcTemplate.update(sqlString, userFrom, userTo, amount);
            accountDao.addToBalance(amount, userTo);
            accountDao.subtractFromBalance(amount, userFrom);
            return "Transfer has been completed";
        } else {
            return "Transfer failed, not enough money in account to complete or entered an invalid user";
        }
    }

    @Override
    public String requestTransfer(int userFrom, int userTo, BigDecimal amount) {
        if (userFrom == userTo) {
            return "You may not request money from yourself";
        }
        if (amount.compareTo(new BigDecimal(0)) == 1) {
            String sqlString = "INSERT INTO transfer (transfer_type_id, transfer_status_id, " +
                    "account_from, account_to, amount " +
                    "VALUES (1, 1, ?, ?, ?)"; // 1 = request, 1 = pending
            jdbcTemplate.update(sqlString, userFrom, userFrom, amount);
            return "Your request has been sent";
        } else {
            return "Something went wrong and could not complete your request";
        }
    }

    @Override
    public List<Transfers> getPendingRequests(int userId) {
        List<Transfers> outcome = new ArrayList<>();
        String sqlString = "SELECT t.*, tu.username AS userFrom, tub.username AS userTo " +
                "FROM transfer t " +
                "JOIN account a ON t.account_from = a.account_id " +
                "JOIN account b ON t.account_to = b.account_id " +
                "JOIN tenmo_user tu ON a.user_id = tu.user_id " +
                "JOIN tenmo_user tub ON b.user_id = tub.user_id " +
                "WHERE transfer_status_id = 1 AND (account_from = ? OR account_to = ?)";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sqlString, userId, userId);
        while (results.next()) {
            Transfers transfers = mapRowToTransfer(results);
            outcome.add(transfers);
        }
        return outcome;
    }

    @Override
    public String updateTransferRequest(Transfers transfers, int statusId) {
        if (statusId == 3) { // 3 = rejected
            String sqlString = "UPDATE transfer " +
                    "SET transfer_status_id = ?" +
                    "WHERE transfer_id = ?";
            jdbcTemplate.update(sqlString, statusId, transfers.getTransferId());
            return "Update has been successful";
        }
        if (!(accountDao.getBalance(transfers.getAccountFrom()).compareTo(transfers.getAmount()) == -1)) {
            String sqlString = "UPDATE transfer SET transfer_status_id = ? WHERE transfer_id = ?";
            jdbcTemplate.update(sqlString, statusId, transfers.getTransferId());
            accountDao.addToBalance(transfers.getAmount(), transfers.getAccountTo());
            accountDao.subtractFromBalance(transfers.getAmount(), transfers.getAccountFrom());
            return "Update has been successful";
        } else {
            return "Not enough money in your account";
        }
    }

    private Transfers mapRowToTransfer(SqlRowSet results) {
        Transfers transfer = new Transfers();
        transfer.setTransferTypeId(results.getInt("transfer_type_id"));
        transfer.setTransferId(results.getInt("transfer_id"));
        transfer.setTransferStatusId(results.getInt("transfer_status_id"));
        transfer.setAccountTo(results.getInt("account_to"));
        transfer.setAccountFrom(results.getInt("account_from"));
        transfer.setAmount(results.getBigDecimal("amount"));
        try {
            transfer.setUserFrom(results.getString("userFrom"));
            transfer.setUserTo(results.getString("userTo"));
        } catch (Exception exception) {
        }
        try {
            transfer.setTransferStatus(results.getString("transfer_status_desc"));
            transfer.setTransferType(results.getString("transfer_type_desc"));
        } catch (Exception exception) {

        }
        return transfer;
    }

}
