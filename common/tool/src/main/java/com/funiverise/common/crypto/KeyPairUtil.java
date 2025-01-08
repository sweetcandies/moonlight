package com.funiverise.common.crypto;


import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.operator.InputDecryptorProvider;
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;
import org.bouncycastle.pkcs.PKCSException;
import org.bouncycastle.pkcs.jcajce.JcePKCSPBEInputDecryptorProviderBuilder;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Objects;

/**
 * @author : hanyuefan
 * @version : [1.0.0, 2023/09/19]
 * @description :
 */
public class KeyPairUtil {
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private static final byte[] PUBLIC_KEY = readPublicKeyFromKey();

    private static final byte[] PRIVATE_KEY = readPrivateKeyFromPem();
    // 私钥口令
    private static final String PRIVATE_KEY_WORD = "11111111";

    /**
     * @description 对外提供的加密方法
     * @author hanyuefan
     * @date 2023/11/24 11:13
     * @param plain 原文字符串
     * @return 十六进制字符串
     */
    public static String encryptWithServerPubKey(String plain) {
        return Hex.toHexString(Objects.requireNonNull(SM2Util.encrypt(PUBLIC_KEY, plain.getBytes())));
    }

    /**
     * @description 对外提供的解密方法
     * @author hanyuefan
     * @date 2023/11/24 11:13
     * @param cipher    十六进制密文
     * @return String
     */
    public static String decryptWithServerPriKey(String cipher) {
        if (StringUtils.isNotBlank(cipher)) {
            try {
                return new String(Objects.requireNonNull(SM2Util.decrypt(PRIVATE_KEY, Hex.decodeStrict(cipher))));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static void main(String[] args) {
        System.out.println(decryptWithServerPriKey("045ac15cc89d9822f5f5118c2d1edf517ca4992f9ccb0c7511b36be2144d05c1bb0d6a9ca39009b6fb89f4d95fc9cd142c15037baf7c457766e9d7d8ee5434a7851d58edb1025de568cf60bdcbc12e6ec379879d00fee7bb0fe916cf348434612a4fd39ace5c5893145d107b553db98c62e6d573e5d2d869f1bba9fa5c17af20ca4b266e673fc4a7f1524b97f1845b9a0acae4aedfab7651881f8c67ef180c24293ec6d2ad4bcbc8557934b4768e663813ffd8f1455f9ca85cf1a5352c5918f572833b4a8ebcbf2b782bc8492afd0a9dfcce98eaef2bf436eb470e021df7d1d42cec1e75330347b4f39e83740d9d6d15de73d9a2710edd73b5eaf793182b5821eb2b563c75ef9ddb3a5e37138d677b178ccb2e81c772d23e94b2455cb48f38fe2dfa4e4e3a235bb849405b50e4de9b422667b13b15e8ef468b164ab4068c3fe964234b1e5ea17eb1500795b6b5b8098c0d6a482ac90e58f25dc647b3f49e72a867aca32f1caf0cfa25477ef4341945c865d8f6bdbcb8546a6663953a69093c62ab41978e4e6e7c11d55a03678254d34e927ce050cd26cc844c189471a737477463ab4547a0904fff1788b48df2c78c59f4d7a33ae9a6070bb1e35389ac6e820e765b087f3afaaa973aadde1909b07983ca2e107835a6f43e623998a5bc8f3c8bb34102e592399c8a25af8dee006fd19701a1704b6d194404fe32179b8f61be5b0439a737eb3f397b92a82cb425ff3ef74e89069be8b9b54106c186dac289dde54bc3c5d74650275698ccfffd36b173581870dbd9fd7278263d2e9e9c3a7e7d73446dc1e92647861f2e9f763a8db5db11b221fe493cc535b59ef140405eaf44b6ea4e26c37cc7695cbcb86dd6ddff596e142d398396a11fa385d690442c2e233ca842795a85a4bfed21f029825b5ba5430eea772a3575d267fd84ea00dde89498635e4e8b4c969bd7f5ffb7b9efdb5eb74f4c23613d6861b17181461cc8856bf6f593241e441c451c0687375ef71b0c69aec2d2e7c10cf8a5c41e07dab11a8f3ee17107bf35e5fe0cdfef7a9191c5cb67f5c95e7797594372d673519cdc04abaaeefea9c0c38fed84bde843dbd8549c143f866f36320c7463d4af81774ffc177155af99cf29741777f2fe965c6ce6aa4fa5e5ca37f36ec1da053d750b79c04c0705143a3780cb4edfc16380f5be85bdea441b9536bb9fc9f639a502b97ce72138bc74be688093545e779e61e10b3a45bfcc6e5e26bcda0f136408685f88c21841da5979c3b7621942f90b3b7be73cc0139774f9a7bd2992d14337b64fa5e73700902aa3f3db48bfc17aaff7f5e8b6d4a021984fec58c5c7d0a6db1b0626bd4f26fac5d8a06b451b1848524016f4321e9c3fe580f79d6158e2d148f938fb2f421205ffea086c280bbdf1ac5b48ae192011f52da8d126bb4ae358521c1d68f0f0880754ce7515b328d141df3483273a09fc26aef90b332c217683f02c9e0a39f526c7cc3522ee1eeeb53af710e44a46a827d16e5bc85b7dede048846c5aef1114f15c88015f48f0e575f296c105d043f8a6beeb93aedd9fa4f54608b61b9b483d03dd10c0fcab3eb967b77f5afdc4ca55a766d8380aa75b3552b1d1aabfca2631b9a5eaae660f60d4ae91ad9b58e9c5988474da222f34b88a669e2eb62720b90c342dd5d79c8181783152e9f3a0edc22494cb846c2f4ac03d5ddb69a6e0976783c9fe2f595d3df533fabfe4008c4419210e856fcb43043105117b97c9534771fe21336f5cefa05001a738e39d4e2334ed3e3bdad33147941e2e6ae0a58648dbdd3b6afbd8383693ac7dc80427a3e8d6b419980564c654616affadb09b0bf5572e5f996674bad155f9d14de91b6a66f2b133d6e0a481fbbe1b38e7b970c25964f16f66781d737d0f064008d272116a0481306bc5800700fc5ec6a9609c03ddd0d23673939604042fcf9cd56e997c8dd36226587f6167bb8b04437d0b0ec106b97de20f786a5269e86cd18a9998e1ecee2b2b3b7c215c717dc607cb9157b7c269e4e6de3c821bcc45b894a0f758d6da0f8389efbe77cd6d29f4c703297b6cb044120bffc2b8b199056a4c7ff084b3fcb6f59d65abcbf4b63d5da67eb1044a192db103cd2994c86181cd0076c9421202c22e4a1af1afeb99e95d827d407c3aaedd0ec3eb9eaf6361212bf5b67c7d604fe5e0623fe3c658d89beb2eef35d650c8a41ab8527a9463ea4ba5fe08f3032c81c4477a8f07025bd3c73a4829cec811657d5aff5e6013473a66d70ff7a7a770c338e17dc11b993ae90bfa2255424376e681970c5c7d9ebf233913d983303e253843ed19b1bf6968c4b52416f7a99ca3c5d0e073bc46094caaa5637383598f651fbc17a97b9ba619b15d5ed2515cde6b37a3878be33d625c631d84fa2204118edabbb049e9fbb51e176ff8883b582412f281c1475a785d6ead3f22649a106d7f8b9ed530869d61e8f8db0e1c13ae13d43a89f97af4ed793f75dd0e518af733353c7c62646a7bef3ed4296a2051cf338865f53f965bb0d76be85d3f57a33aaab076da2bcf850e7fd90f1a85bb9cfdd5c1c7ce2063e1573afb27d86da083a13a974f4adf44cedbb69c8fe33f67da468382dcc8e1c03f7e65703ab97abd81de2332bb68b9f7247ca05bca8475f4bc269fb4bbcc4ec76783faa5e4d9e0a62ed98749b17f52265b0efb4dc5f52eb8a09f70ff2c8d8f900b2d656932e9b04b3991ab9b1d9267eee77785b0902290451f4faba59ebf621551a6377a01a06d402f471ebf6a13393b28ead0f4aab392e1342fc40f3e6e09f51abaf95d0cac6edb5a1d3815c93c8e4a4c614d512d1fea794ef47710970f5c2ede67fa5eadbd220d88b48210aa302a0c487d49edb78612141f39e6858db9b87c0d2e095943b4d8a5a402231304294420c349887403b91559fbd45b931a1ea270636dce33380d7aad16f82f9591d387cb8b4629babc70232806460a59dd219a0a926d978112c645fbfda147222ab09d7148b2fd4578568cb0812aaf4270097e62d496017faea164873de9c6159c1d1b5b636e359d5fd836238a1372f3abf789ff72a5c962b36b81d8967537b019bfcabc82388362489bd97f5725dbbd7d84aa62694945be647c45da34e204ee62a0a1ad7ad59a6cb44b850da13f1b1c9aa44f3691fc97968127d9c82eab861dcd643cb5058a4653f4df5730bd526c57f10788cdd80a265485de4a0c6a031cb68c8ece303f1b02782f5b019970fed6281b7d121902ec2d2f1a92bd93e47f2a50d6452f7603e21ed3c1f94ef70ed0663263fa5c46aa9a57c0e68404e10699981b514c763018d82e5fca5278ef0259655e32f8211157d52968ed2170a9eeb547a3a7e92b4fd0ab24f0e9f71849263b9fda871a9203160331c772af9fa5ccb55df6451688c4460818d8ac812ac7a3e4e5e11058a974a2da7d8a9746b152045e714c47f6b09aae0b6c17bbb69c027f403c555636ec1b6f0a8bec8d22c94836afad549ed931a0db6eb261d20d0e4ba53152a5eef0f066903a2d74f00c38095de3f1a48ea7f6646160afba2bd043a4c26035a9f8a223766f8e424469b52f8d65db392f1e53e00c004f08eeaf3a927e7ccbb24e5401e39707818832c66167ee014ed28d7150fa5c14f70d6adda511a9bf392e26dbbfe61ac0fe433a0243f90be173f0e8665672a79cdfa66c5d0e5039e2be9773f4db7f6eb5d05652c86a3c097b2af687b8bcf78298dd5b5d335ee1193194091aae6be846f5e9f4494a7a87bd9ebf21831ae5687f908728c3b7ebedef7c9021f98f41d7b932cf3d10b28951ca0750e58ace9af2ac3de674a0e972e839e71610f5ffcd96a1af1a921a809864ea50c612aa47fbfe8495787a088bd3c27e39a8b54f4b7dae41c0f46e6d64ce9cab70de365ba28521fa0dd731aa774e64fba6bde6a769216713d97063b4113d3ec8ffdc5c12abcb4ce00e9107ad80323052e56168073916e131b1fc3e53ee95227146f8e032aef44e3a4dbcc1017325f4d6d793904d97539d70c2bde7abb49a9cc4ae8453f837ba41faa0ec71f3089800d3a71a8401eebaa84c8fa0c5341516567aad2265ae642333d1b92e722b2db7b21b3df974ebc06401985d757de74a8d8e329a6751c22d8d780c8db7d88247127174c1600dbd34dd251166b7d17f2201be4e9a1b49da7be8260bf4a555078cefa0e8024a92f33d069108c92bcfec543834ba448696c1adc85736db7b228945236e7aa1c87804afa7291c52c98684397849f3b090fb51d9fde56d92b51edbb7b12005803836e0e9b7aa73502ee9bb1ee48e0ea45291829e08b467ee62c17121934ea3d2cb8991fecc74581791ee3f2594297f6541e64d0733c380f2b6f1fd73bbfc7f0fca5336b6827c4df473fe725b5934e7fbf41fba6acb1168077b193a27e07823be0ae786f13a7ffd86fc808b97e0e1e6a4aa30bf24bb4bf9e42b1d012eeab5222c1e27223133f2c3a8a35ac159f5f602e3f650c5af1f6ab4aa27038b7721eccab439f8cc9fb031ec3f66d627536b22004526878143eb8411750caf97d20d183ad52bd736ee0b0989210d23a00dabcba0db30f74cebfba8a5b5a9d86a9941161756d8ea1ef63bfb673b26971bd5eafe981997666f6dbfc6229a9fbfb6df4e1668986e6ecb7083beae4801f67dbff8f98eaa8c6011e4e9506451a6f1087a470da38fc369621c4e9607a65c17290283290ddc5ad8de44e0955cd6932636473ef69d2309f012288800d8afae694588e470035b333ce1ab09eaec6abe47d9a8866fd329c9ff9d405ed585ea33af71330b32bd62ab9cf95e78d9ff82ba2db7a28c59e304e93e6122f20ab16e73667bfc8252bc8510373b9273198f1c49f93b9a167f53cd780ce0c4c21e72deef05db2f8d346cc0a197a1cdda8a87043e2ec57a09e22ad0091cc92c644251efa26ac5b312635d81ecc9fb2d7c9c2abc9fe53fcc1b855d9b5ea8d9bab56fa3165473245e299d63452bc5e1d173d9b1aa2e6daa4b89679c6ab9652539e1a79c43ac817cfbf4ef26e5ac91626ae0a4ac83781f200bd1ae7c9427966f041a9d12fd2a0079c59cac4f3f440e96087ae4cb94753b904a64b48e9243c460a14e737494e345e5be177fd39c80a4baef2685921b89d26ecc639055ecac920d17faf4dd8d453243d53d3946a7203d7a710fc19ed624c60d"));
//        System.out.println(encryptWithServerPubKey("Guide12345678,"));
    }

    /**
     * @description     读取私钥的方法，不对外提供
     * @author hanyuefan
     * @date 2023/11/24 11:15
     * @return byte[]
     */
    private static byte[] readPrivateKeyFromPem() {
        InputStream resourceAsStream;
        String encryptedKey;
        try {
            resourceAsStream = new ClassPathResource("sm2_keys/pri_key.pem").getInputStream();
            encryptedKey = new String(resourceAsStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        PrivateKeyInfo pki;
        try (PEMParser pemParser = new PEMParser(new StringReader(encryptedKey))) {
            Object o = pemParser.readObject();
            PKCS8EncryptedPrivateKeyInfo epki = (PKCS8EncryptedPrivateKeyInfo) o;
            JcePKCSPBEInputDecryptorProviderBuilder builder =
                    new JcePKCSPBEInputDecryptorProviderBuilder().setProvider("BC");
            InputDecryptorProvider idp = builder.build(PRIVATE_KEY_WORD.toCharArray());
            pki = epki.decryptPrivateKeyInfo(idp);
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
            PrivateKey privateKey = converter.getPrivateKey(pki);
            BCECPrivateKey pri = (BCECPrivateKey) privateKey;
            return SM2Util.getRawPrivateKey(pri);
        } catch (IOException | PKCSException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @description 读取公钥的方法，不对外提供
     * @author hanyuefan
     * @date 2023/11/24 11:15
     * @return byte[]
     */
    private static byte[] readPublicKeyFromKey() {
        InputStream resourceAsStream;
        String keyStr;
        try {
            resourceAsStream = new ClassPathResource("sm2_keys/pub.key").getInputStream();
            keyStr = new String(resourceAsStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        keyStr = keyStr.replace("-----BEGIN PUBLIC KEY-----", "");
        keyStr = keyStr.replace("-----END PUBLIC KEY-----", "");
        keyStr = keyStr.replace("\n", "").replace("\r", "");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(keyStr));
        try {
            // 获取 KeyFactory 对象
            KeyFactory keyFactory = KeyFactory.getInstance("ECDSA");
            // 生成 PublicKey 对象
            PublicKey publicKey = keyFactory.generatePublic(keySpec);
            BCECPublicKey pub = (BCECPublicKey) publicKey;
            return SM2Util.getRawPublicKey(pub);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getDecodePassword(String password) {
        byte[] newPlain = SM2Util.decrypt(PRIVATE_KEY, Hex.decodeStrict(password));
        return new String(Objects.requireNonNull(newPlain)).substring(13);
    }

    public static String encrypt(String password) {
        Security.addProvider(new BouncyCastleProvider());
        String pass = System.currentTimeMillis() + password;
        byte[] cipher = SM2Util.encrypt(PUBLIC_KEY, pass.getBytes());
        return Hex.toHexString(cipher);
    }

    public static String getBase64Password(String password) {
        if (StringUtils.isBlank(password)) {
            return null;
        }
        byte[] plain = SM2Util.decrypt(PRIVATE_KEY, Hex.decodeStrict(password));
        String str = new String(Objects.requireNonNull(plain)).substring(13);
        if (StringUtils.isBlank(str)) {
            return null;
        }
        byte[] newCipher = SM2Util.encrypt(PUBLIC_KEY, str.getBytes());
        return Base64.getEncoder().encodeToString(newCipher);
    }

    public static String getDecodeBase64Password(String base64Password) {
        if (StringUtils.isBlank(base64Password)) {
            return null;
        }
        byte[] newPlain = SM2Util.decrypt(PRIVATE_KEY, Base64.getDecoder().decode(base64Password));
        return new String(Objects.requireNonNull(newPlain));
    }
}
