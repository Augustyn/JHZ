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

import com.atlassian.jira.bc.config.ConstantsService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.query.Query;
import net.amg.jira.plugins.jhz.model.ProjectOrFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementation of SearchService that uses Apache Lucene.
 */
@Component
public class SearchServiceImpl implements SearchService {

    private static final Logger log = LoggerFactory.getLogger(SearchServiceImpl.class);

    private JiraAuthenticationContext jiraAuthenticationContext;
    private SearchProvider searchProvider;
    private ConstantsService constantsService;
    private Validator validator;
    private JqlClauseBuilder jqlClauseBuilder;

    @Override
    public Collection<Status> findAllStatuses() {
        return constantsService.getAllStatuses(jiraAuthenticationContext.getUser().getDirectoryUser()).getReturnedValue();
    }

    @Override
    public Map<String, List<Issue>> findIssues(ProjectOrFilter projectOrFilter, String issueTypes, String date)
            throws SearchException, ParseException {
        Date beginningDate = resolveDaysPreviously(date);
        Map<String, Set<String>> groupedIssueTypes = getGroupedIssueTypes(issueTypes);
        JqlClauseBuilder commonClauseBuilder = JqlQueryBuilder.newBuilder().where().createdAfter(beginningDate).and();
        switch (projectOrFilter.getType()) {
            case PROJECT:
                commonClauseBuilder.project(String.valueOf(projectOrFilter.getId()));
                break;
            case FILTER:
                commonClauseBuilder.savedFilter(String.valueOf(projectOrFilter.getId()));
                break;
        }
        Query commonQuery = commonClauseBuilder.buildQuery();
        Map<String, List<Issue>> issueMap = new HashMap<>();
        for (String groupName : groupedIssueTypes.keySet()) {
            String[] typesGroup = groupedIssueTypes.get(groupName).toArray(new String[0]);
            jqlClauseBuilder = JqlQueryBuilder.newBuilder(commonQuery).where().and().status(typesGroup);
            issueMap.put(groupName, searchProvider.search(jqlClauseBuilder.buildQuery(), jiraAuthenticationContext.getUser(),
                    PagerFilter.getUnlimitedFilter()).getIssues());
        }
        return issueMap;
    }

    private Date resolveDaysPreviously(String date) throws ParseException {
        Date beginningDate;
        if (validator.checkIfDate(date)) {
            beginningDate = new SimpleDateFormat("yyyy-MM-dd").parse(date.replace("/", "-").replace(".", "-"));
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, -Integer.parseInt(date.replaceAll("[d-]", "")));
            beginningDate = calendar.getTime();
        }
        return beginningDate;
    }

    @Override
    public List<Issue> findAllIssues(ProjectOrFilter projectOrFilter) throws SearchException {
        JqlClauseBuilder commonClauseBuilder = JqlQueryBuilder.newBuilder().where();
        switch (projectOrFilter.getType()) {
            case PROJECT:
                commonClauseBuilder.project(String.valueOf(projectOrFilter.getId()));
                break;
            case FILTER:
                commonClauseBuilder.savedFilter(String.valueOf(projectOrFilter.getId()));
                break;
        }
        Query commonQuery = commonClauseBuilder.buildQuery();
        return searchProvider.search(commonQuery, jiraAuthenticationContext.getUser(),
                PagerFilter.getUnlimitedFilter()).getIssues();
    }

    @Override
    public Map<String, Set<String>> getGroupedIssueTypes(String ungroupedTypes) {
        String[] ungroupedTypesArr = ungroupedTypes.split("\\|");
        Map<String, Set<String>> issueTypeMap = new HashMap<>();
        Pattern pattern = Pattern.compile("\\d");
        for (String type : ungroupedTypesArr) {
            Matcher matcher = pattern.matcher(type);
            matcher.find();
            String groupName = LABEL_BASE + type.substring(matcher.start());
            if (!issueTypeMap.containsKey(groupName)) {
                issueTypeMap.put(groupName, new HashSet<String>());
            }
            issueTypeMap.get(groupName).add(type.replaceAll("\\d", ""));
        }
        return issueTypeMap;
    }

    @ServiceReference
    public void setJiraAuthenticationContext(JiraAuthenticationContext jiraAuthenticationContext) {
        this.jiraAuthenticationContext = jiraAuthenticationContext;
    }

    @ServiceReference
    public void setSearchProvider(SearchProvider searchProvider) {
        this.searchProvider = searchProvider;
    }

    @ServiceReference
    public void setConstantsService(ConstantsService constantsService) {
        this.constantsService = constantsService;
    }

    @ServiceReference
    public void setValidator(Validator validator) {
        this.validator = validator;
    }
}