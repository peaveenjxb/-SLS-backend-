package com.jwtauth.backend.dto;

import java.util.Set;

public class AuthResponse {
    private String token;
    private String type;
    private Long id;
    private String username;
    private String email;
    private Set<String> roles;

    public AuthResponse(String token, String type, Long id, String username, String email, Set<String> roles) {
        this.token = token;
        this.type = type;
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = roles;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getToken() {
        return token;
    }

    public String getType() {
        return type;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public static final class Builder {
        private String token;
        private String type;
        private Long id;
        private String username;
        private String email;
        private Set<String> roles;

        public Builder token(String token) {
            this.token = token;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder roles(Set<String> roles) {
            this.roles = roles;
            return this;
        }

        public AuthResponse build() {
            return new AuthResponse(token, type, id, username, email, roles);
        }
    }
}
