package com.funiverise.authority.config;

import com.funiverise.authority.service.IUserService;
import com.funiverise.message.ReturnMsg;
import com.funiverise.object.pojo.Permission;
import com.funiverise.object.pojo.Role;
import com.funiverise.object.vo.UserDetailVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Funny
 * @version 1.0
 * @description: 默认用户详情
 * @date 2021/12/8 14:39
 */
@Slf4j
@Component(value = "defaultUserDetailsService")
public class DefaultUserDetailsService implements UserDetailsService {



    @Autowired
    private IUserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        ReturnMsg<UserDetailVO> userResult = userService.getUserDetail(username);
        if (userResult.isHasError()) {
            throw new UsernameNotFoundException("查询失败");
        }
        UserDetailVO user = userResult.getResult();
        Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
        // 可用性 :true:可用 false:不可用
        boolean enabled = user.isEnable();
        // 过期性 :true:没过期 false:过期
        boolean accountNonExpired = true;
        // 有效性 :true:凭证有效 false:凭证无效
        boolean credentialsNonExpired = true;
        // 锁定性 :true:未锁定 false:已锁定
        boolean accountNonLocked = true;
        for (Role role : user.getRoleSet()) {
            //角色必须是ROLE_开头，可以在数据库中设置
            GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(role.getCode());
            grantedAuthorities.add(grantedAuthority);
        }
        //获取权限
        for (Permission permission : user.getPermissionSet()) {
            GrantedAuthority authority = new SimpleGrantedAuthority(permission.getCode());
            grantedAuthorities.add(authority);
        }
        return new User(user.getUsername(), user.getPassword(),
                enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, grantedAuthorities);

    }

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println(encoder.encode("123456"));
    }
}
