package com.funiverise.oauth.oauth2.support;


import com.funiverise.common.crypto.KeyPairUtil;
import com.funiverise.common.crypto.SM3Util;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.util.Base64;

/**
 * @author : hanyuefan
 * @version : [1.0.0, 2023/09/20]
 * @description :   国密算法密码
 */
public class SMCryptPasswordEncoder implements PasswordEncoder {
    static {
        Security.addProvider(new BouncyCastleProvider());
    }


    @Override
    public String encode(CharSequence rawPassword) {
        byte[] hash = SM3Util.hash(rawPassword.toString().getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }

    public byte[] decryptPassword(String rawPassword) {
        String plain = KeyPairUtil.decryptWithServerPriKey(rawPassword);
        if (StringUtils.isNotBlank(plain) && plain.length() > 13) {
            String timestamp = plain.substring(0, 13);
            // TODO 时间戳校验
            String password = plain.substring(13);
            return password.getBytes();
        }
        return new byte[0];
    }

    /**
     * @param rawPassword
     * @return String
     * @description 用于更新时生成新的密码存至数据库
     * @author hanyuefan
     * @date 2023/9/21 14:57
     */
    public String decryptAndSM3(String rawPassword) {
        byte[] password = decryptPassword(rawPassword);
        if (null != password) {
            return Base64.getEncoder().encodeToString(SM3Util.hash(password));
        }
        return null;

    }


    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        byte[] password = decryptPassword(rawPassword.toString());
        if (null != password) {
            byte[] hash = SM3Util.hash(password);
            return encodedPassword.equals(Base64.getEncoder().encodeToString(hash));
        }
        return false;
    }

    @Override
    public boolean upgradeEncoding(String encodedPassword) {
        return PasswordEncoder.super.upgradeEncoding(encodedPassword);
    }


}
