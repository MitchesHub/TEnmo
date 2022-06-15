package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

@Component
public class JdbcUserDao implements UserDao {

    private static final BigDecimal STARTING_BALANCE = new BigDecimal("1000.00");
    private JdbcTemplate jdbcTemplate;

    public JdbcUserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public int findIdByUsername(String username) {
        String sqlString = "SELECT user_id  FROM tenmo_user WHERE username = ?";
        Integer id = jdbcTemplate.queryForObject(sqlString, Integer.class, username);
        if (id != null) {
            return id;
        } else {
            return -1;
        }

    }

    @Override
    public List<User> findAll(){
        List<User> userList = new ArrayList<>();
        String sqlString = "SELECT * FROM tenmo_user";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sqlString);
        while (results.next()) {
            User user = mapRowToUser(results);
            userList.add(user);
        }
        return userList;
    }

    @Override
    public User findByUsername(String username) {
        return null;
    }

    public User findByUserName(String username) throws UsernameNotFoundException {
       String sqlString = "SELECT * FROM tenmo_user WHERE username = ?";
       SqlRowSet results = jdbcTemplate.queryForRowSet(sqlString, username);
       if (results.next()) {
           return mapRowToUser(results);
       }
       throw new UsernameNotFoundException( "User " + username +" was not found");
    }

    @Override //create new user
    public boolean create(String username, String password) {
        boolean userCreated = false;
        boolean accountCreated = false;

        String sqlInsertUser = "INSERT INTO tenmo_user( username, password_hash) VALUES (?, ?)";
        String passwordHash = new BCryptPasswordEncoder().encode(password);

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        String colum_id = "user_id";
        userCreated = jdbcTemplate.update(con-> {
            PreparedStatement preparedStatement = con.prepareStatement(sqlInsertUser, new String[] {colum_id});
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, passwordHash);
            return preparedStatement;
        }
        , keyHolder) ==1;
        int newUserId = (int) keyHolder.getKeys().get(colum_id);

        // creating account
        String sqlInsertAccount = "INSERT INTO account (user_id, balance) VALUES(?, ?)";
        accountCreated = jdbcTemplate.update(sqlInsertAccount, newUserId, STARTING_BALANCE) ==1;

        return userCreated && accountCreated;
    }



    private User mapRowToUser(SqlRowSet results) {
        User user = new User();
        user.setId(results.getLong("user_id"));
        user.setUsername(results.getString("username"));
        user.setPassword(results.getString("password_hash"));
        user.setActivated(true);
        user.setAuthorities("ROLE_USER");
        return user;
    }


}
