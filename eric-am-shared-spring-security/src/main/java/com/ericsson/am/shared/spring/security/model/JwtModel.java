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
package com.ericsson.am.shared.spring.security.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class JwtModel {

    @JsonProperty("preferred_username")
    private String preferredUsername;

    @JsonProperty("realm_access")
    private RealmAccess realmAccess = new RealmAccess();

    public String getPreferredUsername() {
        return preferredUsername;
    }

    public void setPreferredUsername(String preferredUsername) {
        this.preferredUsername = preferredUsername;
    }

    public RealmAccess getRealmAccess() {
        return realmAccess;
    }

    public void setRealmAccess(RealmAccess realmAccess) {
        this.realmAccess = realmAccess;
    }
}
