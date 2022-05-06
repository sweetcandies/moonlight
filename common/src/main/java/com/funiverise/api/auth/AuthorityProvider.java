package com.funiverise.api.auth;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "authority")
public interface AuthorityProvider {

    @RequestMapping(method = RequestMethod.POST, value = "/oauth/token")
    Object postAccessToken(@RequestParam MultiValueMap<String, String> parameters, @RequestHeader MultiValueMap<String, String> headers);


}
