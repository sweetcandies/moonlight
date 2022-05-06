package com.funiverise.api.auth.impl;

import com.funiverise.api.auth.AuthorityProvider;
import com.funiverise.enums.ReturnResultEnums;
import com.funiverise.message.ReturnMsg;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

@Service
public class AuthorityProviderImpl implements AuthorityProvider {
    @Override
    public Object postAccessToken(MultiValueMap<String, String> parameters, MultiValueMap<String, String> headers) {
        return ReturnMsg.initFailResult(ReturnResultEnums.R_000006);
    }
}
