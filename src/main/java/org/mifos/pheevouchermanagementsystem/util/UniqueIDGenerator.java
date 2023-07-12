package org.mifos.pheevouchermanagementsystem.util;

import java.util.Random;

public class UniqueIDGenerator {
    public static String generateUniqueNumber(int length) {
        Random rand = new Random();
        long timestamp = System.currentTimeMillis();
        long randomLong = rand.nextLong(100000000);
        String uniqueNumber = timestamp + "" + randomLong;
        return uniqueNumber.substring(0, length);
    }
}
