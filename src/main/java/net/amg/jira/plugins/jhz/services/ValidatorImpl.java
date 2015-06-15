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

import com.google.gson.Gson;
import net.amg.jira.plugins.jhz.model.FormField;
import net.amg.jira.plugins.jhz.rest.model.ErrorCollection;
import net.amg.jira.plugins.jhz.rest.model.ValidationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.Response;
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
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public ErrorCollection validate(Map<FormField, String> paramMap){
        ErrorCollection errorCollection = new ErrorCollection();
        Boolean period=false;
        Boolean days=false;

        for (Map.Entry<FormField, String> entry : paramMap.entrySet()) {
            entry.getKey().validate(errorCollection, entry.getValue());
            if(FormField.DATE.equals(entry.getKey()) && FormField.datePattern.matcher(entry.getValue()).matches()){
                Date beginningDate = null;

                try {
                    beginningDate = dateFormat.parse(entry.getValue().replace("/", "-").replace(".", "-"));
                } catch (ParseException e) {
                    log.error("{} Unable to parse date={} cause: {}", new Object[]{beginningDate, e});
                }

                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DAY_OF_YEAR, -11);

                if(calendar.getTime().after(beginningDate)){
                   days=true;
                }

            }else
            if(FormField.DATE.equals(entry.getKey()) && FormField.daysBackPattern.matcher(entry.getValue()).matches()){
                String value=entry.getValue();

                if(value.startsWith("-")){
                    value=value.substring(1, value.length()-1).replaceAll("\\s","");
                }else
                if(value.length()>0)
                {
                    value=value.substring(0, value.length()-1).replaceAll("\\s","");
                }

                try{
                    if(Integer.parseInt(value)>10){
                        days=true;
                    }
                }catch(NumberFormatException e){
                    log.error("{} Unable to parse string={} cause: {}", new Object[]{value, e});
                }

            }else
            if(FormField.PERIOD.equals(entry.getKey()) && entry.getValue().equals("hourly")){
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