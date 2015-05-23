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