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

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.status.Status;
import net.amg.jira.plugins.jhz.model.ProjectOrFilter;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents search operations used by the gadget.
 */
public interface SearchService {

    public static final String LABEL_BASE = "group";

    /**
     * Returns all JIRA statuses available for the currently logged in user.
     *
     * @return Collection of available statuses
     */
    Collection<Status> findAllStatuses();


    /**
     * Finds all issues from chosen project or filter
     *
     * @param projectOrFilter representing project or filter with an id
     * @return List of issues
     * @throws SearchException thrown by Lucene SearchProvider
     */
    List<Issue> findAllIssues(ProjectOrFilter projectOrFilter) throws SearchException;

    /**
     * Groups issue type names in sets mapped to group names.
     *
     * @param ungroupedTypes
     * @return group names (GROUP_BASEn where n=group index) mapped to sets of issue type names. There are as many
     * groups as group numbers found in the argument. i.e. issue9|issue99 gives two groups group9 and group99.
     */
    Map<String, Set<String>> getGroupedIssueTypes(String ungroupedTypes);
}