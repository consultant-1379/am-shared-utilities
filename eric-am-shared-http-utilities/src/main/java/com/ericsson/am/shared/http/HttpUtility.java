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
package com.ericsson.am.shared.http;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public final class HttpUtility {

    private static final String FORWARDED_PROTO = "x-forwarded-proto";
    private static final String FORWARDED_HOST = "x-forwarded-host";

    private HttpUtility() {

    }

    public static HttpServletRequest getCurrentHttpRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();
    }

    public static String getHostUrl() {
        StringBuilder resolvedUrl = new StringBuilder();
        HttpServletRequest request = getCurrentHttpRequest();

        // Using Contains method to check for HTTPS as the header might contain comma separated values http,http
        String protocol = StringUtils.containsIgnoreCase(request.getHeader(FORWARDED_PROTO), "https") ?
                "https" : "http";

        String host = request.getHeader(FORWARDED_HOST) != null ? request.getHeader(FORWARDED_HOST) :
                request.getRemoteHost();

        resolvedUrl.append(protocol).append("://").append(host);

        return resolvedUrl.toString();
    }

}
