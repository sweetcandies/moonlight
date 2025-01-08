package com.funiverise.common.crypto;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.engines.SM4Engine;
import org.bouncycastle.crypto.macs.CBCBlockCipherMac;
import org.bouncycastle.crypto.macs.GMac;
import org.bouncycastle.crypto.modes.GCMBlockCipher;
import org.bouncycastle.crypto.paddings.BlockCipherPadding;
import org.bouncycastle.crypto.paddings.PKCS7Padding;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;

public class SM4Util {
    private SM4Util(){}
    static {
        Security.addProvider(new BouncyCastleProvider());
    }
    public static final String ALGORITHM_NAME = "SM4";
    public static final String ALGORITHM_NAME_ECB_PADDING = "SM4/ECB/PKCS5Padding";
    public static final String ALGORITHM_NAME_ECB_NOPADDING = "SM4/ECB/NoPadding";
    public static final String ALGORITHM_NAME_CBC_PADDING = "SM4/CBC/PKCS5Padding";
    public static final String ALGORITHM_NAME_CBC_NOPADDING = "SM4/CBC/NoPadding";

    /**
     * SM4算法目前只支持128位（即密钥16字节）
     */
    private static final int DEFAULT_KEY_SIZE = 128;

    public static byte[] generateKey() {
        try {
            KeyGenerator kg = KeyGenerator.getInstance(ALGORITHM_NAME, BouncyCastleProvider.PROVIDER_NAME);
            kg.init(DEFAULT_KEY_SIZE, new SecureRandom());
            return kg.generateKey().getEncoded();
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            e.printStackTrace();
            return new byte[]{};
        }
    }


    public static byte[] encrypt_ECB_Padding(byte[] key, byte[] data)  {
        Cipher cipher = generateECBCipher(ALGORITHM_NAME_ECB_PADDING, Cipher.ENCRYPT_MODE, key);
        try {
            return cipher.doFinal(data);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] decrypt_ECB_Padding(byte[] key, byte[] cipherText)  {
        Cipher cipher = generateECBCipher(ALGORITHM_NAME_ECB_PADDING, Cipher.DECRYPT_MODE, key);
        try {
            return cipher.doFinal(cipherText);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] encrypt_ECB_NoPadding(byte[] key, byte[] data) {
        Cipher cipher = generateECBCipher(ALGORITHM_NAME_ECB_NOPADDING, Cipher.ENCRYPT_MODE, key);
        try {
            return cipher.doFinal(data);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] decrypt_ECB_NoPadding(byte[] key, byte[] cipherText) {
        Cipher cipher = generateECBCipher(ALGORITHM_NAME_ECB_NOPADDING, Cipher.DECRYPT_MODE, key);
        try {
            return cipher.doFinal(cipherText);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] encrypt_CBC_Padding(byte[] key, byte[] iv, byte[] data) {
        Cipher cipher = generateCBCCipher(ALGORITHM_NAME_CBC_PADDING, Cipher.ENCRYPT_MODE, key, iv);
        try {
            return cipher.doFinal(data);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] decrypt_CBC_Padding(byte[] key, byte[] iv, byte[] cipherText) {
        Cipher cipher = generateCBCCipher(ALGORITHM_NAME_CBC_PADDING, Cipher.DECRYPT_MODE, key, iv);
        try {
            return cipher.doFinal(cipherText);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * CBC NO PADDING 模式加密，原文必须为16字节
     * @param key   SM416位密钥
     * @param iv    初始向量
     * @param data  数据（16的倍数字节）
     */
    public static byte[] encrypt_CBC_NoPadding(byte[] key, byte[] iv, byte[] data) {
        Cipher cipher = generateCBCCipher(ALGORITHM_NAME_CBC_NOPADDING, Cipher.ENCRYPT_MODE, key, iv);
        try {
            return cipher.doFinal(data);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] decrypt_CBC_NoPadding(byte[] key, byte[] iv, byte[] cipherText) {
        Cipher cipher = generateCBCCipher(ALGORITHM_NAME_CBC_NOPADDING, Cipher.DECRYPT_MODE, key, iv);
        try {
            return cipher.doFinal(cipherText);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] doCMac(byte[] key, byte[] data) {
        Key keyObj = new SecretKeySpec(key, ALGORITHM_NAME);
        return doMac("SM4-CMAC", keyObj, data);
    }

    public static byte[] doGMac(byte[] key, byte[] iv, int tagLength, byte[] data) {
        org.bouncycastle.crypto.Mac mac = new GMac(new GCMBlockCipher(new SM4Engine()), tagLength * 8);
        return doMac(mac, key, iv, data);
    }

    /**
     * 默认使用PKCS7Padding/PKCS5Padding填充的CBCMAC
     *
     * @param key
     * @param iv
     * @param data
     * @return
     */
    public static byte[] doCBCMac(byte[] key, byte[] iv, byte[] data) {
        SM4Engine engine = new SM4Engine();
        org.bouncycastle.crypto.Mac mac = new CBCBlockCipherMac(engine, engine.getBlockSize() * 8, new PKCS7Padding());
        return doMac(mac, key, iv, data);
    }

    /**
     * @param key
     * @param iv
     * @param padding 可以传null，传null表示NoPadding，由调用方保证数据必须是BlockSize的整数倍
     * @param data
     * @return
     * @throws Exception
     */
    public static byte[] doCBCMac(byte[] key, byte[] iv, BlockCipherPadding padding, byte[] data) throws Exception {
        SM4Engine engine = new SM4Engine();
        if (padding == null) {
            if (data.length % engine.getBlockSize() != 0) {
                throw new Exception("if no padding, data length must be multiple of SM4 BlockSize");
            }
        }
        org.bouncycastle.crypto.Mac mac = new CBCBlockCipherMac(engine, engine.getBlockSize() * 8, padding);
        return doMac(mac, key, iv, data);
    }


    private static byte[] doMac(org.bouncycastle.crypto.Mac mac, byte[] key, byte[] iv, byte[] data) {
        CipherParameters cipherParameters = new KeyParameter(key);
        mac.init(new ParametersWithIV(cipherParameters, iv));
        mac.update(data, 0, data.length);
        byte[] result = new byte[mac.getMacSize()];
        mac.doFinal(result, 0);
        return result;
    }

    private static byte[] doMac(String algorithmName, Key key, byte[] data)  {
        try {
            Mac mac = Mac.getInstance(algorithmName, BouncyCastleProvider.PROVIDER_NAME);
            mac.init(key);
            mac.update(data);
            return mac.doFinal();
        } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchProviderException e) {
            throw new RuntimeException(e);
        }
    }

    private static Cipher generateECBCipher(String algorithmName, int mode, byte[] key) {
        try {
            Cipher cipher = Cipher.getInstance(algorithmName, BouncyCastleProvider.PROVIDER_NAME);
            Key sm4Key = new SecretKeySpec(key, ALGORITHM_NAME);
            cipher.init(mode, sm4Key);
            return cipher;
        } catch (NoSuchAlgorithmException | NoSuchProviderException | NoSuchPaddingException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    private static Cipher generateCBCCipher(String algorithmName, int mode, byte[] key, byte[] iv) {
        try {
            Cipher cipher = Cipher.getInstance(algorithmName, BouncyCastleProvider.PROVIDER_NAME);
            Key sm4Key = new SecretKeySpec(key, ALGORITHM_NAME);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
            cipher.init(mode, sm4Key, ivParameterSpec);
            return cipher;
        } catch (NoSuchAlgorithmException | NoSuchProviderException | NoSuchPaddingException | InvalidKeyException |
                 InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);
        }
    }
}
