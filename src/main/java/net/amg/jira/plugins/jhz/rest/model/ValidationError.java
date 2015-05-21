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

package net.amg.jira.plugins.jhz.rest.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Represents an error in configuration of specific field
 */
@XmlRootElement
public class ValidationError {

    @XmlElement
    private String field;

    @XmlElement
    private String error;

    @XmlElement
    private Collection<String> params = new ArrayList<>();

    /**
     * @param field  UserPref name to which the error refers to
     * @param error  i18n key of the error message to be displayed
     * @param params optional parameters
     */
    public ValidationError(String field, String error, Collection<String> params) {
        this.field = field;
        this.error = error;
        this.params = params;
    }

    /**
     * @param field UserPref name to which the error refers to
     * @param error i18n key of the error message to be displayed
     */
    public ValidationError(String field, String error) {
        this.field = field;
        this.error = error;
    }
}
