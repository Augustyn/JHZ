package net.amg.jira.plugins.components;

import net.amg.jira.plugins.rest.configuration.ErrorCollection;

/**
 * Contains field validation method.
 * Created by Ivo on 18/04/15.
 */
public interface FieldValidator {
    public void validate(String value, ErrorCollection errorCollection);
}
