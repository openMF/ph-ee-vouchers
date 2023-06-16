package org.mifos.pheevouchermanagementsystem.util;


import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class EncryptVoucher {
    public static String hashVoucherNumber(String serialNumber, String voucherNumber) {
        try {
            // Create a MessageDigest instance with SHA algorithm
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // Concatenate the serialNumber and voucherNumber
            StringBuilder saltedInputBuilder = new StringBuilder(serialNumber);
            saltedInputBuilder.append(voucherNumber);

            // Convert the salted input to bytes
            byte[] saltedInputBytes = saltedInputBuilder.toString().getBytes(StandardCharsets.UTF_8);

            // Hash the salted input bytes
            byte[] hashedBytes = digest.digest(saltedInputBytes);

            // Convert the hashed bytes to a hexadecimal string
            StringBuilder hexString = new StringBuilder();
            for (byte hashedByte : hashedBytes) {
                String hex = Integer.toHexString(0xFF & hashedByte);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return null;
    }


}
