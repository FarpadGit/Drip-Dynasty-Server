package com.farpad.dripServer.auth;

import com.farpad.dripServer.models.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Value("${drip-app.admin-username}")
    private String AdminUserName;
    @Value("${drip-app.admin-password}")
    private String AdminPassword;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        String EncryptedAdminPassword = new BCryptPasswordEncoder().encode(AdminPassword);
        User user;

        if(username.equals(AdminUserName)) {
            user = new User(AdminUserName, EncryptedAdminPassword, Set.of(User.Role.ROLE_ADMIN));
        }
        else {
            user = new User("Customer", "", Set.of(User.Role.ROLE_CUSTOMER));
        }

        Set<GrantedAuthority> authorities = user.getAuthorities().stream()
                .map((role) -> new SimpleGrantedAuthority(role.getAuthority()))
                .collect(Collectors.toSet());

        return new org.springframework.security.core.userdetails.User(
                username,
                user.getPassword(),
                authorities
        );
    }
}
