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

package net.amg.jira.plugins.jhz.services;

import net.amg.jira.plugins.jhz.model.FormField;
import net.amg.jira.plugins.jhz.rest.model.ErrorCollection;

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