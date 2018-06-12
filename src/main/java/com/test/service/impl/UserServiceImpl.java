package com.test.service.impl;

import com.test.service.UserService;
import com.test.vo.AuthUserVO;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Vector;

@Service
public class UserServiceImpl implements UserDetailsService , UserService {

    public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
        System.out.println("Auth====================="+id);
        AuthUserVO user = this.getUser(id);
        List<GrantedAuthority> authorities = new Vector<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("USER"));

        return new User(user.getUsername(), user.getPassword(), authorities);
    }

    private AuthUserVO getUser(String id) {
        AuthUserVO user = new AuthUserVO();

        user.setUsername("admin");
        user.setPassword("12345");

        return user;
    }
}
