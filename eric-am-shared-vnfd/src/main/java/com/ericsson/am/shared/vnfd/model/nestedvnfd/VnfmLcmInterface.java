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

import java.util.Locale;
import java.util.Objects;

public class VnfmLcmInterface {

    public enum Type {

        CHANGE_PACKAGE("change_package"),
        CHANGE_CURRENT_PACKAGE("change_current_package"),
        INSTANTIATE("instantiate"),
        TERMINATE("terminate"),
        HEAL("heal"),
        SCALE("scale");

        private final String label;

        Type(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private Type type;
    private Inputs inputs;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setType(String label) {
        this.type = Type.valueOf(label.toUpperCase(Locale.ENGLISH));
    }

    public Inputs getInputs() {
        return inputs;
    }

    public void setInputs(Inputs inputs) {
        this.inputs = inputs;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final VnfmLcmInterface that = (VnfmLcmInterface) o;
        return type == that.type && Objects.equals(inputs, that.inputs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, inputs);
    }
}
