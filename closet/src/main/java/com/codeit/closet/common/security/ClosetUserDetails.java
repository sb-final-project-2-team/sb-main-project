package com.codeit.closet.common.security;

import com.codeit.closet.module.user.dto.user.UserDTO;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@RequiredArgsConstructor
@EqualsAndHashCode(of = "userDTO")
public class ClosetUserDetails implements UserDetails {
    private final UserDTO userDTO;
    private final String password;
    private final String tempPassword;
    private final Instant tempPasswordExpiredAt;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + userDTO.role().name()));
    }

    @Override // 로그인 식별자
    public String getUsername() {
        return userDTO.email();
    }

    @Override
    public String getPassword() {
        return password;
    }
}

