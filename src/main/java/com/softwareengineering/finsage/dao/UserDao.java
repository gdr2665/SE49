package com.softwareengineering.finsage.dao;

import com.softwareengineering.finsage.model.User;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class UserDao extends BaseDao<User> {
    private static final String[] HEADERS = {"id", "username", "email", "phone", "password", "resetCode", "resetTime"};
    private static final String CSV_FILE = "data/users.csv";

    public UserDao() {
        super(CSV_FILE, HEADERS);
    }

    @Override
    protected void printRecord(CSVPrinter printer, User user) throws IOException {
        printer.printRecord(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPhone(),
                user.getPassword(),
                user.getResetCode(),
                user.getResetTime() != null ? user.getResetTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : ""
        );
    }

    @Override
    protected User parseRecord(CSVRecord record) {
        User user = new User();
        user.setId(record.get("id"));
        user.setUsername(record.get("username"));
        user.setEmail(record.get("email"));
        user.setPhone(record.get("phone"));
        user.setPassword(record.get("password"));
        user.setResetCode(record.get("resetCode"));
        String resetTimeStr = record.get("resetTime");
        if (!resetTimeStr.isEmpty()) {
            user.setResetTime(LocalDateTime.parse(resetTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        return user;
    }

    @Override
    protected String getId(User user) {
        return user.getId();
    }

    public User getByUsername(String username) {
        return findBy(u -> u.getUsername().equalsIgnoreCase(username))
                .stream()
                .findFirst()
                .orElse(null);
    }

    public User getByEmail(String email) {
        return findBy(u -> u.getEmail().equalsIgnoreCase(email))
                .stream()
                .findFirst()
                .orElse(null);
    }

    public User getByPhone(String phone) {
        return findBy(u -> u.getPhone().equals(phone))
                .stream()
                .findFirst()
                .orElse(null);
    }

    public User getByResetCode(String resetCode) {
        return findBy(u -> resetCode.equals(u.getResetCode()))
                .stream()
                .findFirst()
                .orElse(null);
    }

    public boolean existsByUsername(String username) {
        return getByUsername(username) != null;
    }

    public boolean existsByEmail(String email) {
        return getByEmail(email) != null;
    }

    public boolean existsByPhone(String phone) {
        return getByPhone(phone) != null;
    }

    public List<User> searchByUsernameOrEmail(String keyword) {
        return findBy(u -> u.getUsername().toLowerCase().contains(keyword.toLowerCase()) ||
                u.getEmail().toLowerCase().contains(keyword.toLowerCase()));
    }

    public boolean updateResetInfo(String userId, String resetCode, LocalDateTime resetTime) {
        User user = getById(userId);
        if (user != null) {
            user.setResetCode(resetCode);
            user.setResetTime(resetTime);
            return update(user);
        }
        return false;
    }

    public boolean clearResetInfo(String userId) {
        User user = getById(userId);
        if (user != null) {
            user.setResetCode(null);
            user.setResetTime(null);
            return update(user);
        }
        return false;
    }

    public boolean updatePassword(String userId, String newPassword) {
        User user = getById(userId);
        if (user != null) {
            user.setPassword(newPassword);
            user.setResetCode(null);
            user.setResetTime(null);
            return update(user);
        }
        return false;
    }
}
