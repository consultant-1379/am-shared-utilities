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
package com.ericsson.am.shared.spring.security.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@SpringBootTest
public class AuthenticationUtilsTest {

    private static final String FUNC_USER = "funcuser";

    @Autowired
    private AuthenticationConfiguration authenticationConfiguration;

    @Test
    public void getUserName() throws Exception {
        Optional<String> expected = Optional.of(FUNC_USER);
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                new UsernamePasswordAuthenticationToken(FUNC_USER, null);
        Authentication authentication = authenticationConfiguration.getAuthenticationManager()
                .authenticate(usernamePasswordAuthenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        Optional<String> actual = AuthenticationUtils.getUserName();
        assertThat(actual).isEqualTo(expected);
    }
}