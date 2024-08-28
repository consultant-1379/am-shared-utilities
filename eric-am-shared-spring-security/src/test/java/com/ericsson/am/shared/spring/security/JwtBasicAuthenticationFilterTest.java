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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@SpringBootTest
public class JwtBasicAuthenticationFilterTest {

    private static final String UNKNOWN_USER = "Unknown";
    private static final String INVALID_TOKEN = "Bearer invalid_token";

    @Autowired
    private AuthenticationConfiguration authenticationConfiguration;

    @ParameterizedTest
    @ValueSource(strings = { "", INVALID_TOKEN })
    public void testReadJWTAndReturnUnknownUser(String token) throws Exception {
        MockFilterChain filterchain = new MockFilterChain();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.addHeader(HttpHeaders.AUTHORIZATION, token);

        JwtBasicAuthenticationFilter jwtBasicAuthenticationFilter = new JwtBasicAuthenticationFilter(
                authenticationConfiguration.getAuthenticationManager());

        jwtBasicAuthenticationFilter.doFilterInternal(request, response, filterchain);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        assertThat(authentication.getPrincipal().toString()).isEqualTo(UNKNOWN_USER);
    }

    @Test
    public void testReadJWTAndReturnUser() throws Exception {
        MockFilterChain filterChain = new MockFilterChain();
        MockHttpServletRequest request = new MockHttpServletRequest();
        String token =
                "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJncWFMd1h0TG5UbldULXpJTW1KSm43OE15MWRESm8xWmwwSFplcTh"
                        + "iUHdZIn0.eyJqdGkiOiIwZjNmY2QzNC02YzhkLTQyM2EtYjBiMi1lMmFjMDZiODZkOGMiLCJleHAiOjE1NTg2OTc4NzIs"
                        + "Im5iZiI6MCwiaWF0IjoxNTU4Njk3NTcyLCJpc3MiOiJodHRwOi8vaWFtLmdlci50b2RkMDQxLnJuZC5naWMuZXJpY3Nzb"
                        + "24uc2UvYXV0aC9yZWFsbXMvQURQLUFwcGxpY2F0aW9uLU1hbmFnZXIiLCJhdWQiOiJlcmljc3Nvbi1hcHAiLCJzdWIiO"
                        + "iI2ZTNhNDViYy0zYmVkLTQwMGQtOWNlZi0yMmMyNDIyYjRhZWEiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJlcmljc3Nvbi"
                        + "1hcHAiLCJhdXRoX3RpbWUiOjE1NTg2OTc1NzIsInNlc3Npb25fc3RhdGUiOiJhYjkzMTVlZC01NDgyLTRlZjEtYWYzNi1"
                        + "hN2U3NzkzOWRhZWIiLCJhY3IiOiIxIiwiYWxsb3dlZC1vcmlnaW5zIjpbXSwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbI"
                        + "m9mZmxpbmVfYWNjZXNzIiwidW1hX2F1dGhvcml6YXRpb24iXX0sInJlc291cmNlX2FjY2VzcyI6eyJhY2NvdW50Ijp7In"
                        + "JvbGVzIjpbIm1hbmFnZS1hY2NvdW50IiwibWFuYWdlLWFjY291bnQtbGlua3MiLCJ2aWV3LXByb2ZpbGUiXX19LCJzY29"
                        + "wZSI6Im9wZW5pZCBwcm9maWxlIG9mZmxpbmVfYWNjZXNzIGVtYWlsIGFkZHJlc3MgcGhvbmUiLCJhZGRyZXNzIjp7fSwi"
                        + "ZW1haWxfdmVyaWZpZWQiOmZhbHNlLCJuYW1lIjoiZnVuY1VzZXIgZnVuY1VzZXIiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOi"
                        + "JmdW5jdXNlciIsImdpdmVuX25hbWUiOiJmdW5jVXNlciIsImZhbWlseV9uYW1lIjoiZnVuY1VzZXIiLCJlbWFpbCI6ImZ"
                        + "1bmN1c2VyQGVyaWNzc29uLmNvbSJ9.JKyofwJUccHUIIWeNeSWyDsjJOOltMqn3ia1amb6RdJQCD6cZr4O1GPI6fgjDGf"
                        + "yC_rfayk9MReF5ZUmmwfVFLTXRKfWGUWMccmwRsFAaJy1Sl_29L_38kBeDGtOBByHHXylLN3F5wub0qjmr7VSQRnRcabtR"
                        + "rSf-9FfvHfYhDduCBJUPnkvSec8djbpfY3nSP-W5Wj9QT8BuTk-MtDwJ_D-JnFFBvguDNab_7ClJ96_TevyblCowfDFpEUa"
                        + "qKl-OWJO2_-3FiCrolqxFisFXhvGzFGWCwp7wExUGO7otTBPmko57IbMtpAqH3iJcBx_D8QZnNe1ndW8HXTCS-ik7g";
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        JwtBasicAuthenticationFilter jwtBasicAuthenticationFilter = new JwtBasicAuthenticationFilter(
                authenticationConfiguration.getAuthenticationManager());

        jwtBasicAuthenticationFilter.doFilterInternal(request, response, filterChain);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        assertThat(authentication.getPrincipal().toString()).isEqualTo("funcuser");
    }
}
