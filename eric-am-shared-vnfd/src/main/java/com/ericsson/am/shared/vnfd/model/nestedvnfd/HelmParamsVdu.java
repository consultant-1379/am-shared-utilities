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
package com.ericsson.am.shared.vnfd.model.nestedvnfd;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HelmParamsVdu {

    private Multus multus;

    public HelmParamsVdu(Multus multus) {
        this.multus = multus;
    }

    public HelmParamsVdu() {
    }

    public Multus getMultus() {
        return multus;
    }

    public void setMultus(Multus multus) {
        this.multus = multus;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final HelmParamsVdu that = (HelmParamsVdu) o;
        return Objects.equals(multus, that.multus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(multus);
    }
}
