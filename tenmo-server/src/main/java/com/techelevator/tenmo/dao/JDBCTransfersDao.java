package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Account;
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
    private final long ACCOUNT_ID_OFFSET = 1000; // workaround for getting account ID

    @Override
    public List<Transfers> getAllTransfers(long userId) {
        List<Transfers> list = new ArrayList<>();
        String sqlString = "SELECT t.*, tu.username AS userFrom, tub.username AS userTo " +
                "FROM transfer t " +
                "JOIN account a ON t.account_from = a.account_id " +
                "JOIN account b ON t.account_to = b.account_id " +
                "JOIN tenmo_user tu ON a.user_id = tu.user_id " +
                "JOIN tenmo_user tub ON b.user_id = tub.user_id " +
                "WHERE a.user_id = ? OR b.user_id = ?;";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sqlString, userId, userId);

        while (results.next()) {
            Transfers transfers = mapRowToTransfer(results);
            list.add(transfers);
        }

        return list;
    }

    @Override
    public Transfers getTransferById(long transferId) {
        Transfers transfer;
        String sqlString = "SELECT t.*, u.username AS userFrom, v.username AS userTo, ts.transfer_status_desc, tt.transfer_type_desc " +
                "FROM transfer t " +
                "JOIN account a ON t.account_from = a.account_id " +
                "JOIN account b ON t.account_to = b.account_id " +
                "JOIN tenmo_user u ON a.user_id = u.user_id " +
                "JOIN tenmo_user v ON b.user_id = v.user_id " +
                "JOIN transfer_status ts ON t.transfer_status_id = ts.transfer_status_id " +
                "JOIN transfer_type tt ON t.transfer_type_id = tt.transfer_type_id " +
                "WHERE t.transfer_id = ?;";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sqlString, transferId);

        if (results.next()) {
            transfer = mapRowToTransfer(results);
        } else {
            throw new TransferNotFoundException();
        }

        return transfer;
    }

    @Override
    public String sendTransfer(long userFrom, long userTo, BigDecimal amount) {
        userTo += ACCOUNT_ID_OFFSET; // hackish solution
        // if userFrom is sending less money than he/she has in their account, AND the amount they are sending is greater than 0...
        int checkBalance = amount.compareTo(accountDao.getBalance(userFrom - ACCOUNT_ID_OFFSET));
        int checkPositive = amount.compareTo(new BigDecimal(0));

        if (userFrom == userTo) {
            return "You cannot send money to yourself";
        }

        if (checkBalance < 0 && checkPositive > 0) { // -1 and +1, respectively
            String sqlString = "INSERT INTO transfer (transfer_type_id, transfer_status_id, account_from, account_to, amount) " +
                    "VALUES (2, 2, ?, ?, ?);"; // 2 = send, 2 = approved
            jdbcTemplate.update(sqlString, userFrom, userTo, amount);
            accountDao.addToBalance(amount, userTo);
            accountDao.subtractFromBalance(amount, userFrom);

            return "Transfer complete";
        } else {
            return "Transfer failed due to a lack of funds or a non-positive amount was entered";
        }
    }

    @Override
    public String requestTransfer(long userFrom, long userTo, BigDecimal amount) {
        userTo += ACCOUNT_ID_OFFSET; // hackish solution
        int checkPositive = amount.compareTo(new BigDecimal(0));

        if (userFrom == userTo) {
            return "You cannot request money from yourself";
        }

        if (checkPositive > 0) {
            String sqlString = "INSERT INTO transfer (transfer_type_id, transfer_status_id, account_from, account_to, amount) " +
                    "VALUES (1, 1, ?, ?, ?);"; // 1 = request, 1 = pending
            jdbcTemplate.update(sqlString, userFrom, userTo, amount);
            return "Your request has been sent";
        } else {
            return "Something went wrong and could not complete your request";
        }
    }

    @Override
    public List<Transfers> getPendingRequests(long userId) {
        userId += ACCOUNT_ID_OFFSET; // hackish solution
        List<Transfers> outcome = new ArrayList<>();
        String sqlString = "SELECT t.*, u.username AS userFrom, v.username AS userTo " +
                "FROM transfer t " +
                "JOIN account a ON t.account_from = a.account_id " +
                "JOIN account b ON t.account_to = b.account_id " +
                "JOIN tenmo_user u ON a.user_id = u.user_id " +
                "JOIN tenmo_user v ON b.user_id = v.user_id " +
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
        if (statusId == 3) { // 3 = rejected; same as entering a 2
            String sqlString = "UPDATE transfer SET transfer_status_id = ? WHERE transfer_id = ?;";
            jdbcTemplate.update(sqlString, statusId, transfers.getTransferId());

            return "Update has been successful";
        }

        int checkBalance = accountDao.getBalance(transfers.getAccountFrom() - ACCOUNT_ID_OFFSET).compareTo(transfers.getAmount());

        // user has accepted the transfer
        if (checkBalance > 0) {
            String sqlString = "UPDATE transfer SET transfer_status_id = ? WHERE transfer_id = ?;";
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
        } catch (Exception ignored) {}

        try {
            transfer.setTransferStatus(results.getString("transfer_status_desc"));
            transfer.setTransferType(results.getString("transfer_type_desc"));
        } catch (Exception ignored) {}

        return transfer;
    }
}
