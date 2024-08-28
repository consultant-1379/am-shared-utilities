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
package com.ericsson.am.shared.vnfd.model.servicemodel;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServiceModelMetadata {
    @JsonProperty("template_name")
    private String templateName;
    @JsonProperty("template_version")
    private String templateVersion;
    @JsonProperty("template_author")
    private String templateAuthor;

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(final String templateName) {
        this.templateName = templateName;
    }

    public String getTemplateVersion() {
        return templateVersion;
    }

    public void setTemplateVersion(final String templateVersion) {
        this.templateVersion = templateVersion;
    }

    public String getTemplateAuthor() {
        return templateAuthor;
    }

    public void setTemplateAuthor(final String templateAuthor) {
        this.templateAuthor = templateAuthor;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ServiceModelMetadata that = (ServiceModelMetadata) o;
        return Objects.equals(templateName, that.templateName) && Objects.equals(templateVersion, that.templateVersion)
                && Objects.equals(templateAuthor, that.templateAuthor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(templateName, templateVersion, templateAuthor);
    }
}
