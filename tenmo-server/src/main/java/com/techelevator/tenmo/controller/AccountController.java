package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.AccountDao;
import com.techelevator.tenmo.dao.UserDao;
import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

@RestController
@PreAuthorize("isAuthenticated()")
@RequestMapping("/api")
public class AccountController {
    @Autowired
    private AccountDao accountDao;
    @Autowired
    private UserDao userDao;

    @Autowired
    public AccountController(AccountDao accountDao, UserDao userDao) {
        this.accountDao = accountDao;
        this.userDao = userDao;
    }

    @PreAuthorize("hasRole('USER')")
    @RequestMapping(path = "/users", method = RequestMethod.GET)
    public List<User> listUsers() {
        return userDao.findAll();
    }

    @RequestMapping(path = "/balance", method = RequestMethod.GET)
    public BigDecimal getBalance(Principal principal) {
       User user = userDao.findByUsername(principal.getName());
       long id = user.getId();
       return accountDao.getBalance(id);
    }

    @RequestMapping(path = "/account/user/{id}", method = RequestMethod.GET)
    public Account findUserById(@PathVariable long userId) {
        return accountDao.findUserById(userId);
    }
}
