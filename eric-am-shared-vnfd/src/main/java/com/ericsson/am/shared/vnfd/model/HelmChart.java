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
package com.ericsson.am.shared.vnfd.model;

import java.util.Objects;

public class HelmChart implements ToscaNodeInput {
    private String path;
    private HelmChartType chartType;
    private String chartKey;

    public HelmChart() {
    }

    public HelmChart(final String path, final HelmChartType chartType, final String chartKey) {
        this.path = path;
        this.chartType = chartType;
        this.chartKey = chartKey;
    }

    public String getPath() {
        return path;
    }

    public void setPath(final String path) {
        this.path = path;
    }

    public HelmChartType getChartType() {
        return chartType;
    }

    public void setChartType(final HelmChartType chartType) {
        this.chartType = chartType;
    }

    public String getChartKey() {
        return chartKey;
    }

    public void setChartKey(final String chartKey) {
        this.chartKey = chartKey;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final HelmChart helmChart = (HelmChart) o;
        return Objects.equals(path, helmChart.path) && chartType == helmChart.chartType && Objects.equals(chartKey, helmChart.chartKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, chartType, chartKey);
    }
}
