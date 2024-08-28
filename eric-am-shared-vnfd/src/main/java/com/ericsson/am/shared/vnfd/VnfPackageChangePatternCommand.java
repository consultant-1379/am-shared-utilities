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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum VnfPackageChangePatternCommand {
    ROLLBACK("rollback"), INSTALL("install"), UPGRADE("upgrade"), DELETE("delete"), DELETE_PVC("delete_pvc");

    private static final Pattern DELETE_PVC_PATTERN = Pattern
            .compile("^delete_pvc?\\[([a-zA-z0-9-./_]+=[a-zA-z0-9-./_]+)?(,?[a-zA-z0-9-./_]+=[a-zA-z0-9-./_]+)*\\]$");

    private static final VnfPackageChangePatternCommand[] VNF_PACKAGE_CHANGE_PATTERN_COMMANDS = values();

    private String command;

    VnfPackageChangePatternCommand(final String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    public static boolean isSupportedCommand(String requestedCommand) {
        String filteredCommand = requestedCommand.replaceAll("\\s+", ""); //NOSONAR
        Matcher matcher = DELETE_PVC_PATTERN.matcher(filteredCommand);

        if (matcher.find()) {
            return true;
        }

        for (VnfPackageChangePatternCommand command : VNF_PACKAGE_CHANGE_PATTERN_COMMANDS) {
            if (command.getCommand().equals(requestedCommand)) {
                return true;
            }
        }
        return false;
    }
}
