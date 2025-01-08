package com.funiverise.oauth.oauth2.endpoint;

import com.guideir.oauth.oauth2.support.service.CustomTokenServices;
import com.guideir.oauth.oauth2.support.service.TokenEndpointService;
import com.guideir.system.common.dto.GdUserDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.endpoint.FrameworkEndpoint;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 由于spring security oauth2没有单独提供对应的登出或清除token的接口，自定义一个
 */
@Api(tags = "自定义token处理请求端点")
@FrameworkEndpoint
public class CustomerTokenEndpoint {

    @Autowired
    private TokenEndpointService tokenEndpointService;

    @Autowired
    private HttpServletRequest request;
    @Resource
    private CustomTokenServices customTokenServices;

    @ApiOperation(value = "获取当前用户")
    @PostMapping({"/oauth/current_user"})
    @ResponseBody
    public GdUserDTO getCurrentUser() {
        return tokenEndpointService.getCurrentUser();
    }

    @ApiOperation(value = "其它获取token，刷新token，检验token等接口为oauth2内部接口，暂时不支持通过swagger展示，可使用postman等工具")
    @PostMapping({"/oauth/clear_token"})
    @ResponseBody
    public Object clearToken(String token) {
        // 请求头中有token的优先使用请求头里的token（非feign调用）
        String auth = request.getHeader("Authorization");
        if (StringUtils.isNotBlank(request.getHeader("Authorization"))) {
            return tokenEndpointService.clearToken(auth.split(" ")[1]);
        }
        return tokenEndpointService.clearToken(token);
    }

    @PostMapping("/oauth/third_party_login")
    @ResponseBody
    @ApiModelProperty("三方平台免密登录接口")
    public Object login(@RequestBody GdUserDTO model ) {
        return customTokenServices.processThirdLogin(model.getToken());
    }

}
