/*
 * Copyright 2015 AMG.net - Politechnika Łódzka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.amg.jira.plugins.jhz.model;

import com.atlassian.jira.charts.ChartFactory;
import net.amg.jira.plugins.jhz.rest.model.ErrorCollection;
import net.amg.jira.plugins.jhz.rest.model.ValidationError;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
        @Override
        public void validate(ErrorCollection errorCollection, String value) {
            if (value == null || value.isEmpty()) {
                errorCollection.addValidationError(new ValidationError(FormField.ISSUES.fieldName,
                        ERROR_PREFIX + "emptyIssue"));
            } else if (!issuesPattern.matcher(value).matches()) {
                errorCollection.addValidationError(new ValidationError(FormField.ISSUES.fieldName,
                        ERROR_PREFIX + "invalidValue"));
            }
        }
    }, DATE("Date") {
        @Override
        public void validate(ErrorCollection errorCollection, String value) {
            if (value == null || value.isEmpty()) {
                errorCollection.addValidationError(new ValidationError(FormField.DATE.fieldName,
                        ERROR_PREFIX + EMPTY_FIELD));
            } else {
                if (!daysBackPattern.matcher(value).matches() && !datePattern.matcher(value).matches()) {
                    errorCollection.addValidationError(new ValidationError(FormField.DATE.fieldName,
                            ERROR_PREFIX + "wrongFormat"));
                } else {
                    if (datePattern.matcher(value).matches()) {
                        Calendar today = Calendar.getInstance();
                        Calendar setDate = Calendar.getInstance();

                        if (value.charAt(4) == '/' || value.charAt(4) == '.' ||
                                value.charAt(7) == '/' || value.charAt(7) == '.') {
                            value = value.substring(0, 4) + "-" + value.substring(5, 7) + "-" + value.substring(8);
                        }

                        try {
                            setDate.setTime(simpleDateFormat.parse(value));
                        } catch (ParseException e) {
                            errorCollection.addValidationError(new ValidationError(FormField.DATE.fieldName,
                                    ERROR_PREFIX + "noWay"));
                        }

                        if (setDate.after(today)) {
                            errorCollection.addValidationError(new ValidationError(FormField.DATE.fieldName,
                                    ERROR_PREFIX + "futureDate"));
                        }

                        today.set(Calendar.YEAR, today.get(Calendar.YEAR) - 1);
                        if (setDate.before(today)) {
                            errorCollection.addValidationError(new ValidationError(FormField.DATE.fieldName,
                                    ERROR_PREFIX + "overYear"));
                        }
                    }
                }
            }
        }
    }, PERIOD("Period") {
        @Override
        public void validate(ErrorCollection errorCollection, String value) {
            if (value == null || value.isEmpty()) {
                errorCollection.addValidationError(new ValidationError(FormField.PERIOD.fieldName,
                        ERROR_PREFIX + EMPTY_FIELD));
            }
        }
    }, VERSION("Version") {
        @Override
        public void validate(ErrorCollection errorCollection, String value) {
            if (value == null || value.isEmpty()) {
                errorCollection.addValidationError(new ValidationError(FormField.VERSION.fieldName,
                        ERROR_PREFIX + EMPTY_FIELD));
            } else {
                try {
                    ChartFactory.VersionLabel.valueOf(value);
                } catch (IllegalArgumentException ex) {
                    errorCollection.addValidationError(new ValidationError(FormField.VERSION.fieldName,
                            ERROR_PREFIX + "invalidValue"));
                }
            }
        }
    };

    private final static String ERROR_PREFIX = "issues.history.gadget.errors.";
    private final static String EMPTY_FIELD = "emptyField";
    private final static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    public static final Pattern daysBackPattern = Pattern.compile("^-?([0-9][0-9]?|[12][0-9][0-9]|3[0-5][0-9]|36[0-5])[ ]?d$", Pattern.CASE_INSENSITIVE);
    public static final Pattern datePattern = Pattern.compile("^[1-2]\\d{3}[/\\-[.]](0[1-9]|1[012])[/\\-[.]](0[1-9]|[12][0-9]|3[01])$", Pattern.CASE_INSENSITIVE);
    public static final Pattern projectPattern = Pattern.compile("(project-|filter-)\\d+", Pattern.CASE_INSENSITIVE);
    public static final Pattern issuesPattern = Pattern.compile("[aA-zZ\\s]+\\d+((\\|[aA-zZ\\s]+\\d+)?)*", Pattern.CASE_INSENSITIVE);

    private final String fieldName;

    private FormField(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldName(){
        return fieldName;
    }
    /**
     * Checks field value for correctness.
     *
     * @param errorCollection container for detected errors
     * @param value           checked
     */
    public abstract void validate(ErrorCollection errorCollection, String value);
}
