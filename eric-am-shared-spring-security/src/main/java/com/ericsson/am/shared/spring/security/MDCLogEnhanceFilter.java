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
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.ericsson.am.shared.spring.security.utils.AuthenticationUtils;

@Component
public class MDCLogEnhanceFilter extends OncePerRequestFilter {

    private static final String USERNAME_KEY = "userName";
    private static final Logger LOGGER = LoggerFactory.getLogger(MDCLogEnhanceFilter.class);

    private static String getUserName() {
        try {
            return AuthenticationUtils.getUserName().orElse(StringUtils.EMPTY);
        } catch (Exception e) {
            LOGGER.warn("Could not get userName from JWT: {}", e.getMessage());
        }
        return StringUtils.EMPTY;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        String userName = getUserName();
        try {
            MDC.put(USERNAME_KEY, userName);
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(USERNAME_KEY);
        }
    }
}
