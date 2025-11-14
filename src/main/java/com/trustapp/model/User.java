package com.trustapp.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {
    
    private Long id;
    private String username;
    private String email;
    private String password;
    private String fullName;
    private String phone;
    private Boolean isActive;
    private Boolean isLocked;
    
    // UserDetails interface implementation
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Return empty authorities for now
        // Can be enhanced later to include roles/permissions
        return Collections.emptyList();
    }
    
    @Override
    public String getPassword() {
        return password;
    }
    
    @Override
    public String getUsername() {
        return email; // Use email as username for authentication
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return isLocked == null || !isLocked;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return isActive != null && isActive;
    }
}

