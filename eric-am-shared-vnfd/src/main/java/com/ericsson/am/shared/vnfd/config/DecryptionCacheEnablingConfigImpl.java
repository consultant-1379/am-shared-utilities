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
package com.ericsson.am.shared.vnfd.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * This implementation doesn't support dynamic property reloading - the component should restart for notice the change.
 */
@Component
public class DecryptionCacheEnablingConfigImpl implements DecryptionCacheEnablingConfig {

    @Value("${crypto.cache.enabled:false}")
    private boolean isDecryptionCacheEnabled;

    @Override
    public boolean isDecryptionCacheEnabled() {
        return isDecryptionCacheEnabled;
    }

    public void setDecryptionCacheEnabled(boolean decryptionCacheEnabled) {
        this.isDecryptionCacheEnabled = decryptionCacheEnabled;
    }
}
