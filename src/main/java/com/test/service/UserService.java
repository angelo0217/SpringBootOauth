package com.test.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface UserService {
    public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException;
}
