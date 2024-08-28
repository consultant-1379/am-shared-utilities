/*
 * COPYRIGHT Ericsson 2024
 *
 *
 *
 * The copyright to the computer program(s) herein is the property of
 *
 * Ericsson Inc. The programs may be used and/or copied only with written
 *
 * permission from Ericsson Inc. or in accordance with the terms and
 *
 * conditions stipulated in the agreement/contract under which the
 *
 * program(s) have been supplied.
 */
package com.ericsson.am.shared.spring.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.ericsson.am.shared.spring.security.model.JwtModel;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JwtBasicAuthenticationFilter extends BasicAuthenticationFilter {

    private static final String TOKEN_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtBasicAuthenticationFilter.class);

    JwtBasicAuthenticationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws IOException, ServletException {

        AuthenticationManager authenticationManager = getAuthenticationManager();
        String header = request.getHeader(TOKEN_HEADER);

        Authentication authenticate;
        if (StringUtils.isEmpty(header) || !header.startsWith(TOKEN_PREFIX)) {
            authenticate = authenticationManager.authenticate(getAnonymousUser());
        } else {
            authenticate = authenticationManager.authenticate(getAuthentication(request));
        }

        SecurityContextHolder.getContext().setAuthentication(authenticate);
        filterChain.doFilter(request, response);
    }

    private static UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
        String token = request.getHeader(TOKEN_HEADER).replace(TOKEN_PREFIX, "");
        if (StringUtils.isNotEmpty(token)) {
            try {
                Jwt decodedToken = JwtHelper.decode(token);
                String claims = decodedToken.getClaims();
                ObjectMapper objectMapper = new ObjectMapper();
                JwtModel mappedToken = objectMapper.readValue(claims, JwtModel.class);

                String username = mappedToken.getPreferredUsername();

                List<GrantedAuthority> authorities = mappedToken.getRealmAccess().getRoles()
                        .stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

                if (StringUtils.isNotEmpty(username)) {
                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                            new UsernamePasswordAuthenticationToken(username, null, authorities);
                    usernamePasswordAuthenticationToken.setDetails(objectMapper.readValue(claims, Object.class));
                    return usernamePasswordAuthenticationToken;
                }
            } catch (Exception exception) {
                LOGGER.warn("JWT failed to parse : {} failed : {}", token, exception.getMessage());
            }
        }

        return getAnonymousUser();
    }

    private static UsernamePasswordAuthenticationToken getAnonymousUser() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        return new UsernamePasswordAuthenticationToken("Unknown", null, authorities);
    }
}
