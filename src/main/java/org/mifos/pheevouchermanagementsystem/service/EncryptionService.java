package org.mifos.pheevouchermanagementsystem.service;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import org.mifos.connector.common.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EncryptionService {

    @Value("${rsa-key.private}")
    private String privateKey;

    @Value("${rsa-key.public}")
    private String publicKey;

    public String encrypt(String data) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException,
            BadPaddingException, InvalidKeySpecException, InvalidKeyException {
        return SecurityUtil.encryptUsingPrivateKey(data, privateKey);
    }

    public String decrypt(String encryptedData) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException,
            BadPaddingException, InvalidKeySpecException, InvalidKeyException {
        return SecurityUtil.decryptUsingPublicKey(encryptedData, publicKey);
    }

}
