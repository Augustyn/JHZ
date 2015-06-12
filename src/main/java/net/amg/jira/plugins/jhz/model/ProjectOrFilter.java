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

/**
 * Represents project of filter with an id
 * Created by Ivo on 01/06/15.
 */
public class ProjectOrFilter {

    private final ProjectsType type;
    private final int id;

    public ProjectOrFilter(ProjectsType type, int id) {
        this.type = type;
        this.id = id;
    }

    public ProjectsType getType() {
        return type;
    }

    public int getId() {
        return id;
    }
}
