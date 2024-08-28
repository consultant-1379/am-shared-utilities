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

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Multus {

    private String format;
    private String path;
    private String param;
    private String nad;
    private String ns;
    @JsonProperty("if")
    private String multusInterface;
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<String> set;

    public Multus() {
    }

    public Multus(String format,
                  String path,
                  String param,
                  String nad,
                  String ns,
                  String multusInterface,
                  List<String> set) {
        this.format = format;
        this.path = path;
        this.param = param;
        this.nad = nad;
        this.ns = ns;
        this.multusInterface = multusInterface;
        this.set = set;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public String getNad() {
        return nad;
    }

    public void setNad(String nad) {
        this.nad = nad;
    }

    public String getNs() {
        return ns;
    }

    public void setNs(String ns) {
        this.ns = ns;
    }

    public String getMultusInterface() {
        return multusInterface;
    }

    public void setMultusInterface(String multusInterface) {
        this.multusInterface = multusInterface;
    }

    public List<String> getSet() {
        return set;
    }

    public void setSet(List<String> set) {
        this.set = set;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Multus multus = (Multus) o;
        return Objects.equals(format, multus.format) && Objects.equals(path, multus.path) && Objects.equals(param, multus.param)
                && Objects.equals(nad, multus.nad) && Objects.equals(ns, multus.ns) && Objects.equals(multusInterface, multus.multusInterface)
                && Objects.equals(set, multus.set);
    }

    @Override
    public int hashCode() {
        return Objects.hash(format, path, param, nad, ns, multusInterface, set);
    }
}
