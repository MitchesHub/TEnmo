package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Account;

import java.math.BigDecimal;

public interface AccountDao {
    BigDecimal getBalance(long userId);

    BigDecimal addToBalance(BigDecimal amountToAdd, long id);

    BigDecimal subtractFromBalance(BigDecimal amountToSubtract, long id);

    Account findAccountById(long id);
}
