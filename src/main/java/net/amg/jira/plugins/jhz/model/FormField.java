package net.amg.jira.plugins.jhz.model;

import net.amg.jira.plugins.jhz.rest.model.ErrorCollection;
import net.amg.jira.plugins.jhz.rest.model.ValidationError;

import java.util.regex.Pattern;

/**
 * A set of field categories constituting gadget configuration.
 * Created by Ivo on 19/04/15.
 */
public enum FormField {

    PROJECT("Project") {
        @Override
        public void validate(ErrorCollection errorCollection, String value) {
            if (value == null || value.isEmpty()) {
                errorCollection.addValidationError(new ValidationError(FormField.PROJECT.fieldName,
                        ERROR_PREFIX + EMPTY_FIELD));
            } else if (!projectPattern.matcher(value).matches()) {
                errorCollection.addValidationError(new ValidationError(FormField.PROJECT.fieldName,
                        ERROR_PREFIX + "invalidValue"));
            }
        }
    }, ISSUES("Issues") {
        public void validate(ErrorCollection errorCollection, String value) {
            if (value == null || value.isEmpty()) {
                errorCollection.addValidationError(new ValidationError(FormField.ISSUES.fieldName,
                        ERROR_PREFIX + EMPTY_FIELD));
            } else if (!issuesPattern.matcher(value).matches()) {
                errorCollection.addValidationError(new ValidationError(FormField.ISSUES.fieldName,
                        ERROR_PREFIX + "invalidValue"));
            }
        }
    }, DATE("Date") {
        public void validate(ErrorCollection errorCollection, String value) {
            if (value == null || value.isEmpty()) {
                errorCollection.addValidationError(new ValidationError(FormField.DATE.fieldName,
                        ERROR_PREFIX + EMPTY_FIELD));
            } else {
                if (!daysBackPattern.matcher(value).matches() && !datePattern.matcher(value).matches()) {
                    errorCollection.addValidationError(new ValidationError(FormField.DATE.fieldName,
                            ERROR_PREFIX + "wrongFormat"));
                }
            }
        }
    }, PERIOD("Period") {
        public void validate(ErrorCollection errorCollection, String value) {
            if (value == null || value.isEmpty()) {
                errorCollection.addValidationError(new ValidationError(FormField.PERIOD.fieldName,
                        ERROR_PREFIX + EMPTY_FIELD));
            }
        }
    };

    private static final String ERROR_PREFIX = "issues.history.gadget.errors.";
    private static final String EMPTY_FIELD = "emptyField";
    public static final Pattern daysBackPattern = Pattern.compile("^-?([0-9][0-9]?|[12][0-9][0-9]|3[0-5][0-9]|36[0-5])d$", Pattern.CASE_INSENSITIVE);
    public static final Pattern datePattern = Pattern.compile("^[1-2]\\d{3}[/\\-[.]](0[1-9]|1[012])[/\\-[.]](0[1-9]|[12][0-9]|3[01])$", Pattern.CASE_INSENSITIVE);
    public static final Pattern projectPattern = Pattern.compile("(project-|filter-)\\d+", Pattern.CASE_INSENSITIVE);
    public static final Pattern issuesPattern = Pattern.compile("[aA-zZ\\s]+\\d+((\\|[aA-zZ\\s]+\\d+)?)*", Pattern.CASE_INSENSITIVE);

    private final String fieldName;

    private FormField(String fieldName) {
        this.fieldName = fieldName;
    }

    /**
     * Checks field value for correctness.
     *
     * @param errorCollection container for detected errors
     * @param value           checked
     */
    public abstract void validate(ErrorCollection errorCollection, String value);
}
