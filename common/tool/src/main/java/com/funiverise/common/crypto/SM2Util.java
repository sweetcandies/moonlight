package com.funiverise.common.crypto;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.SM2Engine;
import org.bouncycastle.crypto.engines.SM2Engine.Mode;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.params.ParametersWithID;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.crypto.signers.SM2Signer;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.ec.custom.gm.SM2P256V1Curve;
import org.bouncycastle.util.encoders.Hex;

import java.io.IOException;
import java.math.BigInteger;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECFieldFp;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPrivateKeySpec;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.EllipticCurve;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.util.Arrays;
import java.util.Base64;

public class SM2Util {
    private SM2Util(){}


    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    //////////////////////////////////////////////////////////////////////////////////////
    /*
     * 以下为SM2推荐曲线参数
     */
    private static final String CURVE_NAME = "sm2p256v1";
    public static final SM2P256V1Curve CURVE = new SM2P256V1Curve();
    private static final BigInteger SM2_ECC_P = CURVE.getQ();
    private static final BigInteger SM2_ECC_A = CURVE.getA().toBigInteger();
    private static final BigInteger SM2_ECC_B = CURVE.getB().toBigInteger();
    public static final BigInteger SM2_ECC_N = CURVE.getOrder();
    public static final BigInteger SM2_ECC_H = CURVE.getCofactor();
    private static final BigInteger SM2_ECC_GX = new BigInteger(
            "32C4AE2C1F1981195F9904466A39C9948FE30BBFF2660BE1715A4589334C74C7", 16);
    private final static BigInteger SM2_ECC_GY = new BigInteger(
            "BC3736A2F4F6779C59BDCEE36B692153D0A9877CC62A474002DF32E52139F0A0", 16);
    public static final ECPoint G_POINT = CURVE.createPoint(SM2_ECC_GX, SM2_ECC_GY);

    private static final byte[] WITH_ID = "1234567812345678".getBytes();
    protected static final ECDomainParameters DOMAIN_PARAMS = new ECDomainParameters(CURVE, G_POINT,
            SM2_ECC_N, SM2_ECC_H);
    private static final int CURVE_LEN = BCECUtil.getCurveLength(DOMAIN_PARAMS);
    //////////////////////////////////////////////////////////////////////////////////////

    private static final EllipticCurve JDK_CURVE = new EllipticCurve(new ECFieldFp(SM2_ECC_P), SM2_ECC_A, SM2_ECC_B);
    private static final java.security.spec.ECPoint JDK_G_POINT = new java.security.spec.ECPoint(
            G_POINT.getAffineXCoord().toBigInteger(), G_POINT.getAffineYCoord().toBigInteger());
    protected static final ECParameterSpec JDK_EC_SPEC = new ECParameterSpec(
            JDK_CURVE, JDK_G_POINT, SM2_ECC_N, SM2_ECC_H.intValue());

    //////////////////////////////////////////////////////////////////////////////////////


    public static byte[][] genKeyPair() {
        try {
            SecureRandom random = new SecureRandom();
            KeyPair keyPair = BCECUtil.generateKeyPair(DOMAIN_PARAMS, random);
            return new byte[][]{
                    getRawPublicKey((BCECPublicKey) keyPair.getPublic()),
                    getRawPrivateKey((BCECPrivateKey) keyPair.getPrivate())};
        } catch (NoSuchProviderException | NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 只获取私钥里的d值，32字节
     *
     */
    public static byte[] getRawPrivateKey(BCECPrivateKey privateKey) {
        return fixToCurveLengthBytes(privateKey.getD().toByteArray());
    }

    /**
     * 只获取公钥里的XY分量，64字节
     *
     */
    public static byte[] getRawPublicKey(BCECPublicKey publicKey) {
        byte[] src65 = publicKey.getQ().getEncoded(false);
        byte[] rawXY = new byte[CURVE_LEN * 2];//SM2的话这里应该是64字节
        System.arraycopy(src65, 1, rawXY, 0, rawXY.length);
        return rawXY;
    }

    private static BCECPublicKey genECPublicKey(byte[] pubkey) {
        try {
            AlgorithmParameters params = AlgorithmParameters.getInstance("EC", "BC");
            params.init(new ECGenParameterSpec(CURVE_NAME));

            ECParameterSpec ecParameterSpec = params.getParameterSpec(ECParameterSpec.class);

            if (pubkey.length % 2 != 0 && pubkey[0] == '4') {
                pubkey = Arrays.copyOfRange(pubkey, 1, pubkey.length);
            }

            byte[] x = Arrays.copyOfRange(pubkey, 0, pubkey.length / 2);
            byte[] y = Arrays.copyOfRange(pubkey, pubkey.length / 2, pubkey.length);

            java.security.spec.ECPoint ecPoint = new java.security.spec.ECPoint(new BigInteger(1, x), new BigInteger(1, y));


            KeyFactory keyFactory = KeyFactory.getInstance("EC", "BC");
            ECPublicKey ecPublicKey = (ECPublicKey) keyFactory.generatePublic(new ECPublicKeySpec(ecPoint, ecParameterSpec));

            return (BCECPublicKey) ecPublicKey;
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidParameterSpecException |
                 InvalidKeySpecException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static BCECPrivateKey genECPrivateKey(byte[] prikey) {
        try {
            AlgorithmParameters params = AlgorithmParameters.getInstance("EC", "BC");
            params.init(new ECGenParameterSpec(CURVE_NAME));

            ECParameterSpec ecParameterSpec = params.getParameterSpec(ECParameterSpec.class);

            KeyFactory keyFactory = KeyFactory.getInstance("EC", "BC");
            BigInteger s = new BigInteger(1, prikey);
            ECPrivateKey ecPrivateKey = (ECPrivateKey) keyFactory.generatePrivate(new ECPrivateKeySpec(s, ecParameterSpec));
            return (BCECPrivateKey) ecPrivateKey;
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidParameterSpecException |
                 InvalidKeySpecException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] encrypt(byte[] pubKey, byte[] plain) {
        try {
            BCECPublicKey publicKey = genECPublicKey(pubKey);
            if (null == publicKey) {
                return new byte[0];
            }
            ECPublicKeyParameters pubKeyParameters = BCECUtil.convertPublicKeyToParameters(publicKey);
            SM2Engine engine = new SM2Engine(Mode.C1C3C2);
            ParametersWithRandom pwr = new ParametersWithRandom(pubKeyParameters, new SecureRandom());
            engine.init(true, pwr);
            return engine.processBlock(plain, 0, plain.length);
        } catch (InvalidCipherTextException e) {
            e.printStackTrace();
            return new byte[]{};
        }
    }


    public static byte[] decrypt(byte[] priKey, byte[] cipher) {
        try {
            BCECPrivateKey privateKey = genECPrivateKey(priKey);
            if (null == privateKey) {
                return new byte[0];
            }
            ECPrivateKeyParameters priKeyParameters = BCECUtil.convertPrivateKeyToParameters(privateKey);
            SM2Engine engine = new SM2Engine(Mode.C1C3C2);
            engine.init(false, priKeyParameters);
            return engine.processBlock(cipher, 0, cipher.length);
        } catch (InvalidCipherTextException e) {
            e.printStackTrace();
            return new byte[]{};
        }
    }

    /**
     * @param cipher 密文
     * @return byte[]
     * @description 将DER编码格式密文转为C1C3C2格式的密文
     * @author hanyuefan
     * @date 2023/9/21 15:48
     */
    public static byte[] transferASN1ToPlain(byte[] cipher) {
        try (ASN1InputStream is = new ASN1InputStream(cipher)) {
            ASN1Sequence sequence = ASN1Sequence.getInstance(is.readObject());
            // 获取C1、C3和C2值
            BigInteger x = new BigInteger(((ASN1Integer) sequence.getObjectAt(0)).getPositiveValue().toByteArray());
            BigInteger y = new BigInteger(((ASN1Integer) sequence.getObjectAt(1)).getPositiveValue().toByteArray());


            ECPoint c1 = SM2Util.CURVE.createPoint(x, y);
            byte[] c3 = ((DEROctetString) sequence.getObjectAt(2)).getOctets();
            byte[] c2 = ((DEROctetString) sequence.getObjectAt(3)).getOctets();
            // 构建无格式的C1C3C2密文
            byte[] c1c3c2Bytes = new byte[c1.getEncoded(false).length + c3.length + c2.length];
            System.arraycopy(c1.getEncoded(false), 0, c1c3c2Bytes, 0, c1.getEncoded(false).length);
            System.arraycopy(c3, 0, c1c3c2Bytes, c1.getEncoded(false).length, c3.length);
            System.arraycopy(c2, 0, c1c3c2Bytes, c1.getEncoded(false).length + c3.length, c2.length);
            return c1c3c2Bytes;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @description 讲Plain密文转换为ASN1格式化数据密文
     * @author hanyuefan
     * @date 2024/8/8 17:01
     * @param cipher
     * @return byte[]
     */
    public static byte[] transferPlainToASN1(byte[] cipher) {
        byte[] c1xBytes = new byte[32];
        byte[] c1yBytes = new byte[32];

        // 假设plainCipherText是整个Plain格式密文
        System.arraycopy(cipher, 0, c1xBytes, 0, 32);       // 提取C1的x坐标
        System.arraycopy(cipher, 32, c1yBytes, 0, 32);      // 提取C1的y坐标

        byte[] c3Bytes = new byte[32];
        System.arraycopy(cipher, 64, c3Bytes, 0, 32); // 提取C3

        byte[] c2Bytes = new byte[cipher.length - 96];
        System.arraycopy(cipher, 96, c2Bytes, 0, cipher.length - 96); // 提取C2


        // 步骤2：构造 ASN.1 结构
        ASN1EncodableVector v = new ASN1EncodableVector();
        v.add(new DERBitString(c1xBytes));
        v.add(new DERBitString(c1yBytes));
        v.add(new DEROctetString(c3Bytes));           // 添加 C3 哈希值
        v.add(new DEROctetString(c2Bytes));           // 添加 C2 密文数据

        // 步骤3：将构造的向量转换为 DERSequence (ASN.1)
        ASN1Primitive asn1Structure = new DERSequence(v);
        try {
            // 步骤4：输出 ASN.1 格式的字节数组
            return asn1Structure.getEncoded();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }




    public static byte[] sign(byte[] prikey, byte[] srcData) {
        try {
            BCECPrivateKey privateKey = genECPrivateKey(prikey);
            if (null == privateKey) {
                return new byte[0];
            }
            ECPrivateKeyParameters priKeyParameters = BCECUtil.convertPrivateKeyToParameters(privateKey);
            SM2Signer signer = new SM2Signer();
            ParametersWithRandom pwr = new ParametersWithRandom(priKeyParameters, new SecureRandom());
            CipherParameters param = new ParametersWithID(pwr, WITH_ID);
            signer.init(true, param);
            signer.update(srcData, 0, srcData.length);
            return signer.generateSignature();
        } catch (CryptoException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean verify(String pbData, String sign, String publicKey) {
        byte[] decode = Base64.getDecoder().decode(publicKey);
        final ASN1Primitive primitive;
        try {
            primitive = ASN1Primitive.fromByteArray(decode);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
        final ASN1Sequence asn1Sequence = ASN1Sequence.getInstance(primitive);
        String asn = asn1Sequence.getObjectAt(1).toString();// ASN.1 EC pubkey
        String pubKey = asn.substring(9);
        return verify(Hex.decodeStrict(pubKey), pbData.getBytes(), Base64.getDecoder().decode(sign));
    }


    public static boolean verify(byte[] pubkey, byte[] srcData, byte[] sign) {
        BCECPublicKey publicKey = genECPublicKey(pubkey);
        if (null == publicKey) {
            return false;
        }
        ECPublicKeyParameters pubKeyParameters = BCECUtil.convertPublicKeyToParameters(publicKey);
        SM2Signer signer = new SM2Signer();
        CipherParameters param = new ParametersWithID(pubKeyParameters, WITH_ID);

        signer.init(false, param);
        signer.update(srcData, 0, srcData.length);
        return signer.verifySignature(sign);
    }

    private static byte[] fixToCurveLengthBytes(byte[] src) {
        if (src.length == CURVE_LEN) {
            return src;
        }

        byte[] result = new byte[CURVE_LEN];
        if (src.length > CURVE_LEN) {
            System.arraycopy(src, src.length - result.length, result, 0, result.length);
        } else {
            System.arraycopy(src, 0, result, result.length - src.length, src.length);
        }
        return result;
    }
}
