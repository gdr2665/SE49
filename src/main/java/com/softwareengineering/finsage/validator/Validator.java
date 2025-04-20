package com.softwareengineering.finsage.validator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

public class Validator {
    // Username: 4-20 characters, only letters and numbers
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9]{4,20}$");

    // Password: 8-20 characters, letters, numbers and special symbols
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^[a-zA-Z0-9@#$%^&+=]{8,20}$");

    // Email: standard email format
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    // Phone: 11 digits starting with 1 in China
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[0-9]{10}$");

    // Amount: positive or negative number with optional decimal places
    private static final Pattern AMOUNT_PATTERN = Pattern.compile("^-?\\d+(\\.\\d{1,2})?$");

    // Service name: only QQ or WeChat
    private static final Pattern SERVICE_NAME_PATTERN = Pattern.compile("^(QQ|WeChat)$", Pattern.CASE_INSENSITIVE);

    // Category name: 1-50 characters, letters, numbers, Chinese characters and spaces
    private static final Pattern CATEGORY_NAME_PATTERN = Pattern.compile("^[\\u4e00-\\u9fa5a-zA-Z0-9 ]{1,50}$");

    // Holiday name: 1-50 characters, letters, numbers, Chinese characters and spaces
    private static final Pattern HOLIDAY_NAME_PATTERN = Pattern.compile("^[\\u4e00-\\u9fa5a-zA-Z0-9 ]{1,50}$");

    // Item name: 1-50 characters, letters, numbers, Chinese characters and spaces
    private static final Pattern ITEM_NAME_PATTERN = Pattern.compile("^[\\u4e00-\\u9fa5a-zA-Z0-9 ]{1,50}$");

    // Note: 0-200 characters, any characters
    private static final Pattern NOTE_PATTERN = Pattern.compile("^.{0,200}$");

    public static boolean validateUsername(String username) {
        return username != null && USERNAME_PATTERN.matcher(username).matches();
    }

    public static boolean validatePassword(String password) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }

    public static boolean validateEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean validatePhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    public static boolean validateAmount(String amount) {
        if (amount == null) return false;
        try {
            new BigDecimal(amount);
            return AMOUNT_PATTERN.matcher(amount).matches();
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean validateDate(String date) {
        try {
            LocalDate.parse(date);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public static boolean validateServiceName(String serviceName) {
        return serviceName != null && SERVICE_NAME_PATTERN.matcher(serviceName).matches();
    }

    public static boolean validateCategoryName(String categoryName) {
        return categoryName != null && CATEGORY_NAME_PATTERN.matcher(categoryName).matches();
    }

    public static boolean validateHolidayName(String holidayName) {
        return holidayName != null && HOLIDAY_NAME_PATTERN.matcher(holidayName).matches();
    }

    public static boolean validateItemName(String itemName) {
        return itemName != null && ITEM_NAME_PATTERN.matcher(itemName).matches();
    }

    public static boolean validateNote(String note) {
        return note == null || NOTE_PATTERN.matcher(note).matches();
    }
}
