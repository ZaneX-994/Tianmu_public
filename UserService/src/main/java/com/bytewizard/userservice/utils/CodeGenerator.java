package com.bytewizard.userservice.utils;

import java.util.Random;

public class CodeGenerator {

    public static String generateVerificationCode() {
        Random random = new Random();
        int code = random.nextInt(900000) + 100000;

        return String.format("%06d", code);
    }
}
