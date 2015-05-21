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

import com.atlassian.jira.issue.status.Status;

import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Encapsulates attributes of a JIRA status.
 * Created by Ivo on 01/05/15.
 */
@Immutable
@XmlRootElement
public class StatusRepresentation {

    @XmlElement
    private String value;

    @XmlElement
    private String label;


    /**
     * Extracts required attributes from the given JIRA status.
     *
     * @param status
     */
    public StatusRepresentation(Status status) {
        this.value = status.getName();
        this.label = status.getNameTranslation();
    }

}
