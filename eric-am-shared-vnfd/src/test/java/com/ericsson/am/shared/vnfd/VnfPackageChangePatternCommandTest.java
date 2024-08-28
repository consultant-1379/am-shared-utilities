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
package com.ericsson.am.shared.vnfd;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VnfPackageChangePatternCommandTest {

    @Test
    void isSupportedCommandForDeletePVCRegex() {
        String deletePvc = "delete_pvc";

        boolean supportedCommand = VnfPackageChangePatternCommand.isSupportedCommand(deletePvc);
        assertTrue(supportedCommand, String.format("Assertion for %s command failed", deletePvc));

        String deletePvcEmptyBracket = "delete_pvc[]";

        supportedCommand = VnfPackageChangePatternCommand.isSupportedCommand(deletePvcEmptyBracket);
        assertTrue(supportedCommand, String.format("Assertion for %s command failed", deletePvcEmptyBracket));

        String deletePvcWithLabel = "delete_pvc[app=trigger]";

        supportedCommand = VnfPackageChangePatternCommand.isSupportedCommand(deletePvcWithLabel);
        assertTrue(supportedCommand, String.format("Assertion for %s command failed", deletePvcWithLabel));

        String deletePvcWithMultipleLabels = "delete_pvc[app=trigger, app.kubernetes.io/instance=test , " +
                "app.kubernetes.io/name=eric-eo-evnfm-mb ]";

        supportedCommand = VnfPackageChangePatternCommand.isSupportedCommand(deletePvcWithMultipleLabels);
        assertTrue(supportedCommand, String.format("Assertion for %s command failed", deletePvcWithMultipleLabels));

    }

    @Test
    void isSupportedCommandForInvalidDeletePVCRegex() {
        String deletePvc = "delete_pvc_";

        boolean supportedCommand = VnfPackageChangePatternCommand.isSupportedCommand(deletePvc);
        assertFalse(supportedCommand, String.format("Assertion for %s command failed", deletePvc));

        String deletePvcOpenBracket = "delete_pvc[";

        supportedCommand = VnfPackageChangePatternCommand.isSupportedCommand(deletePvcOpenBracket);
        assertFalse(supportedCommand, String.format("Assertion for %s command failed", deletePvcOpenBracket));

        String deletePvcClosedBracket = "delete_pvc]";

        supportedCommand = VnfPackageChangePatternCommand.isSupportedCommand(deletePvcClosedBracket);
        assertFalse(supportedCommand, String.format("Assertion for %s command failed", deletePvcClosedBracket));

        String deletePvcCurlyBracket = "delete_pvc{}";

        supportedCommand = VnfPackageChangePatternCommand.isSupportedCommand(deletePvcCurlyBracket);
        assertFalse(supportedCommand, String.format("Assertion for %s command failed", deletePvcCurlyBracket));

        String deletePvcWithLabel = "delete_pvc[app=trigger, ]";

        supportedCommand = VnfPackageChangePatternCommand.isSupportedCommand(deletePvcWithLabel);
        assertFalse(supportedCommand, String.format("Assertion for %s command failed", deletePvcWithLabel));

        String deletePvcWithMultipleLabels = "delete_pvc[app=trigger, test, pair]";

        supportedCommand = VnfPackageChangePatternCommand.isSupportedCommand(deletePvcWithMultipleLabels);
        assertFalse(supportedCommand, String.format("Assertion for %s command failed", deletePvcWithMultipleLabels));

    }
}