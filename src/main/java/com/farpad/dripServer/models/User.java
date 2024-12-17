package com.farpad.dripServer.models;

import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Set;

@Getter @Setter
@AllArgsConstructor
public class User implements UserDetails {

    private String username;
    private String password;
    private Set<Role> authorities;

    @Getter
    public enum Role implements GrantedAuthority {
        ROLE_CUSTOMER("CUSTOMER"),
        ROLE_ADMIN("ADMIN");

        private final String value;

        Role(String value) {
            this.value = value;
        }

        @Override
        public String getAuthority() {
            return name();
        }
    }
}