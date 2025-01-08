package com.funiverise.oauth.oauth2.support;

import com.guideir.system.common.dto.GdUserDTO;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * 授权用户信息详情封装
 */
public class Oauth2UserDetails implements UserDetails {

    private GdUserDTO user;

    public Oauth2UserDetails(GdUserDTO user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        if(user != null && !CollectionUtils.isEmpty(user.getAuthorities())){
            for (String code : user.getAuthorities()) {
                authorities.add(new SimpleGrantedAuthority(code));
            }
        }
        //返回当前用户的权限
        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        if (null != user && null != user.getValidStartTime() && null != user.getValidEndTime()) {
            Date current = new Date();
            return current.before(user.getValidEndTime()) && current.after(user.getValidStartTime());
        }
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user != null && user.getUserStatus() != null && user.getUserStatus() == 1;
    }

    public GdUserDTO getUser() {
        return user;
    }
}
