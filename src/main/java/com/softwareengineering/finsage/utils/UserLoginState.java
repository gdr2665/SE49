package com.softwareengineering.finsage.utils;

import com.softwareengineering.finsage.model.User;

public class UserLoginState {
    private static User currentUser;

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static void logout() {
        currentUser = null;
    }

    public static String getCurrentUserId() {
        return isLoggedIn() ? currentUser.getId() : null;
    }
}
