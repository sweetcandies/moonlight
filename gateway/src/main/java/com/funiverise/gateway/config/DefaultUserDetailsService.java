package com.funiverise.gateway.config;

import com.funiverise.gateway.service.IUserService;
import com.funiverise.message.ReturnMsg;
import com.funiverise.object.pojo.Permission;
import com.funiverise.object.pojo.Role;
import com.funiverise.object.vo.UserDetailVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Funny
 * @version 1.0
 * @description: TODO
 * @date 2021/12/8 14:39
 */
@Slf4j
@Component(value = "defaultUserDetailsService")
public class DefaultUserDetailsService implements UserDetailsService {


    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private IUserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 查询数据库操作
        if(!username.equals("admin")){
            throw new UsernameNotFoundException("the user is not found");
        }else{
            ReturnMsg<UserDetailVO> userResult = userService.getUserDetail(username);
            if (userResult.isHasError()) {
                throw new UsernameNotFoundException("查询失败");
            }
            UserDetailVO user = userResult.getResult();
            List<GrantedAuthority> authorities = new ArrayList<>();
            for (Role role : user.getRoleSet()) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
            }
            for (Permission permission : user.getPermissionSet()) {
                authorities.add(new SimpleGrantedAuthority(permission.getCode()));
            }
            return new org.springframework.security.core.userdetails.User(username,user.getPassword(), authorities);
        }
    }
}
