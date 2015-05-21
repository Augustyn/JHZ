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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collection;
import java.util.HashSet;

/**
 * Represents a collection of all available JIRA statuses for the currently logged-in user.
 * Created by Ivo on 01/05/15.
 */
@XmlRootElement(name = "statuses")
@XmlAccessorType(XmlAccessType.FIELD)
public class StatusesResourceModel {

    @XmlElement
    private Collection<StatusRepresentation> statuses;

    /**
     * Encapsulates IssueTypeRepresentation objects.
     *
     * @param statusTypes requested by gadget
     */
    public StatusesResourceModel(Iterable<Status> statusTypes) {
        this.statuses = new HashSet<StatusRepresentation>();
        for (Status status : statusTypes) {
            this.statuses.add(new StatusRepresentation(status));
        }
    }

}
