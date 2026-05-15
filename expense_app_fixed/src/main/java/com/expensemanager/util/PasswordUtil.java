package com.expensemanager.util;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class PasswordUtil {

    public static String hashPassword(String originalPassword) {
        return BCrypt.withDefaults().hashToString(12, originalPassword.toCharArray());
    }

    public static boolean checkPassword(String originalPassword, String hashedPassword) {
        BCrypt.Result result = BCrypt.verifyer().verify(originalPassword.toCharArray(), hashedPassword);
        return result.verified;
    }
}
