package net.amg.jira.plugins.services;

import net.amg.jira.plugins.model.FormField;
import net.amg.jira.plugins.rest.model.ErrorCollection;

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
    ErrorCollection validate(Map<FormField, String> paramMap);

    /**
     * Checks if given string represents a date
     *
     * @param value
     * @return true if value has a date format
     */
    boolean checkIfDate(String value);

    /**
     * Check if given string represents a project
     *
     * @param value
     * @return true if value represents a project
     */
    boolean checkIfProject(String value);

}