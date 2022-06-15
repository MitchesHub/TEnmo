package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class JDBCAccountDao implements AccountDao {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public JDBCAccountDao() {} // empty?

    public JDBCAccountDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate= jdbcTemplate;
    }

    @Override
    public BigDecimal getBalance(int userId) {
        String sqlString = "SELECT balance FROM account WHERE user_id = ?";
        SqlRowSet results = null;
        BigDecimal balance = null;
        try {
            results = jdbcTemplate.queryForRowSet(sqlString, userId);
            if (results.next()) {
                balance = results.getBigDecimal("balance");
            }
        } catch (DataAccessException exception) {
            System.out.println("Something went wrong, error accessing data");
        }
        return balance;
    }

    @Override
    public BigDecimal addToBalance(BigDecimal amountToAdd, int id) {
        Account account = findAccountById(id);
        BigDecimal newBalance = account.getBalance().add(amountToAdd);
        System.out.println(newBalance);
        String sqlString = "UPDATE account SET balance = ? WHERE user_id =?";
        try {
            jdbcTemplate.update(sqlString, newBalance, id);
        } catch (DataAccessException exception) {
            System.out.println("Something went wrong, error accessing data");
        }
        return account.getBalance();
    }

    @Override
    public BigDecimal subtractFromBalance(BigDecimal amountToSubtract, int id) {
        return null;
    }

    @Override
    public Account findUserById(int userId) {
        String sqlString = "SELECT * FROM account WHERE user_id = ?";
        Account account = null;
        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sqlString, userId);
            account = mapRowToAccount(results);
        } catch (DataAccessException exception) {
            System.out.println("Something went wrong, error accessing data");
        }
        return account;
    }

    @Override
    public Account findAccountById(int id) {
            Account account = null;
            String sqlString = "SELECT * FROM account WHERE account_id = ?";
            SqlRowSet results = jdbcTemplate.queryForRowSet(sqlString, id);
            if (results.next()) {
                account = mapRowToAccount(results);
            }
            return account;
    }

    private Account mapRowToAccount(SqlRowSet results) {
            Account account = new Account();
            account.setBalance(results.getBigDecimal("balance"));
            account.setAccountId(results.getInt("account_id"));
            account.setUserId(results.getInt("user_id"));
            return account;
    }
}
