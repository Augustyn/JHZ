package net.amg.jira.plugins.components;

import net.amg.jira.plugins.rest.configuration.ErrorCollection;

import java.util.Map;

/**
 * Contains validation methods for gadget configuration.
 */
public interface Validator {

    /**
     * Inspects values of fields for correctness.
     *
     * @param paramMap contains pairs [field name, field value]
     * @return JAXB class with detected errors
     */
    public ErrorCollection validate(Map<String, String> paramMap);
}