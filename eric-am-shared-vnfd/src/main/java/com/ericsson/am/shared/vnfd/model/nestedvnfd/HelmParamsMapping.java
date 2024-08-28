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

import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HelmParamsMapping {

    private Map<String, HelmParamsVdu> vdus;
    private Map<String, HelmParamsExtCp> extCps;

    public HelmParamsMapping() {
    }

    public HelmParamsMapping(final Map<String, HelmParamsVdu> vdus,
                             final Map<String, HelmParamsExtCp> extCps) {
        this.vdus = vdus;
        this.extCps = extCps;
    }

    public Map<String, HelmParamsVdu> getVdus() {
        return vdus;
    }

    public void setVdus(final Map<String, HelmParamsVdu> vdus) {
        this.vdus = vdus;
    }

    public Map<String, HelmParamsExtCp> getExtCps() {
        return extCps;
    }

    public void setExtCps(final Map<String, HelmParamsExtCp> extCps) {
        this.extCps = extCps;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final HelmParamsMapping that = (HelmParamsMapping) o;
        return Objects.equals(getVdus(), that.getVdus()) && Objects.equals(getExtCps(), that.getExtCps());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getVdus(), getExtCps());
    }
}
