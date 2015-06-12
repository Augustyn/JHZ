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
import net.amg.jira.plugins.jhz.rest.model.ValidationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 * Simple implementation of Validator.
 */
@Component
public class ValidatorImpl implements Validator {

    private static final Logger log = LoggerFactory.getLogger(ValidatorImpl.class);

    @Override
    public ErrorCollection validate(Map<FormField, String> paramMap){
        ErrorCollection errorCollection = new ErrorCollection();
        Boolean period=false;
        Boolean days=false;

        for (Map.Entry<FormField, String> entry : paramMap.entrySet()) {
            entry.getKey().validate(errorCollection, entry.getValue());

            if(entry.getKey().equals("Date") && FormField.datePattern.matcher(entry.getValue()).matches()){
                Date beginningDate = null;
                try {
                    beginningDate = new SimpleDateFormat("yyyy-MM-dd").parse(entry.getValue().replace("/", "-").replace(".", "-"));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DAY_OF_YEAR, -10);

                if(calendar.getTime().after(beginningDate)){
                   days=true;
                }

            }else
            if(entry.getKey().equals("Date") && FormField.daysBackPattern.matcher(entry.getValue()).matches()){
                String value=entry.getValue();

                if(entry.getValue().startsWith("-")){
                    value.substring(1, value.length()-1);
                }else
                if(value.length()>0){
                    value.substring(0, value.length()-1);
                }

                if(Integer.parseInt(value)>10){
                    days=true;
                }
            }else
            if(entry.getKey().equals("Period") && entry.getValue().equals("Hourly")){
                period=true;
            }

        }

        if(period && days){
            errorCollection.addValidationError(new ValidationError(FormField.PERIOD.getFieldName(),
                    "issues.history.gadget.errors.toManyPeriods"));
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