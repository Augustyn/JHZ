package net.amg.jira.plugins.jhz.services;

import com.atlassian.jira.util.InjectableComponent;
import net.amg.jira.plugins.jhz.model.FormField;
import net.amg.jira.plugins.jhz.rest.model.ErrorCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Simple implementation of Validator.
 */
@Component
public class ValidatorImpl implements Validator {

    private static final Logger log = LoggerFactory.getLogger(ValidatorImpl.class);

    @Override
    public ErrorCollection validate(Map<FormField, String> paramMap) {
        ErrorCollection errorCollection = new ErrorCollection();
        for (Map.Entry<FormField, String> entry : paramMap.entrySet()) {
            entry.getKey().validate(errorCollection, entry.getValue());
        }
        return errorCollection;
    }

    @Override
    public boolean checkIfDate(String value) {
        return FormField.datePattern.matcher(value).matches();
    }

    @Override
    public boolean checkIfProject(String value) {
        return value.startsWith("project-");
    }
}