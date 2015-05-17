package net.amg.jira.plugins.services;

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
import com.atlassian.query.clause.Clause;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Implementation of SearchService that uses Apache Lucene.
 */
public class SearchServiceImpl implements SearchService {

    private final static int ISSUE_GROUP_COUNT = 5;
    private static final Logger log = LoggerFactory.getLogger(SearchServiceImpl.class);

    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final SearchProvider searchProvider;
    private final ConstantsService constantsService;
    private final Validator validator;
    private JqlClauseBuilder jqlClauseBuilder;

    public SearchServiceImpl(JiraAuthenticationContext authenticationContext, SearchProvider searchProvider, ConstantsService constantsService, Validator validator) {
        this.jiraAuthenticationContext = authenticationContext;
        this.searchProvider = searchProvider;
        this.constantsService = constantsService;
        this.validator = validator;
    }

    @Override
    public Collection<Status> findAllStatuses() {
        return constantsService.getAllStatuses(jiraAuthenticationContext.getUser().getDirectoryUser()).getReturnedValue();
    }

    @Override
    public List<List<Issue>> findIssues(String projectOrFilter, String issueTypes, String date)
            throws SearchException, ParseException {
        Date beginningDate = resolveDaysPreviously(date);
        String[][] groupedIssueTypes = groupIssueNamesByIndex(issueTypes);
        JqlClauseBuilder commonClauseBuilder = JqlQueryBuilder.newBuilder().where().createdAfter(beginningDate).and();
        if (validator.checkIfProject(projectOrFilter)) {
            commonClauseBuilder.project(projectOrFilter.split("-")[1]);
        } else {
            commonClauseBuilder.savedFilter(projectOrFilter.split("-")[1]);
        }
        Query commonQuery = commonClauseBuilder.buildQuery();
        List<List<Issue>> issueLists = new ArrayList<>();
        for (int i = 0; i < ISSUE_GROUP_COUNT; i++) {
            if (groupedIssueTypes[i].length > 0) {
                jqlClauseBuilder = JqlQueryBuilder.newBuilder(commonQuery).where().and().status(groupedIssueTypes[i]);
                issueLists.add(searchProvider.search(jqlClauseBuilder.buildQuery(), jiraAuthenticationContext.getUser(),
                        PagerFilter.getUnlimitedFilter()).getIssues());
            } else {
                issueLists.add(new ArrayList<Issue>());
            }
        }
        return issueLists;
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

    private String[][] groupIssueNamesByIndex(String issues) {
        String[] ungroupedTypes = issues.split("\\|");
        String[][] typeGroups = new String[ISSUE_GROUP_COUNT][];
        for (int i = 0; i < ISSUE_GROUP_COUNT; i++) {
            List<String> typeGroupList = new ArrayList<>();
            for (String issue : ungroupedTypes) {
                if (issue.endsWith(String.valueOf(i))) {
                    typeGroupList.add(issue.replace(String.valueOf(i), ""));
                }
            }
            typeGroups[i] = new String[typeGroupList.size()];
            typeGroupList.toArray(typeGroups[i]);
        }
        return typeGroups;
    }
    
    @Override
    public List<Issue> findAllIssues(String projectOrFilter) throws SearchException {
        JqlClauseBuilder commonClauseBuilder = JqlQueryBuilder.newBuilder().where();
        if (validator.checkIfProject(projectOrFilter)) {
            commonClauseBuilder.project(projectOrFilter.split("-")[1]);
        } else {
            commonClauseBuilder.savedFilter(projectOrFilter.split("-")[1]);
        }
        Query commonQuery = commonClauseBuilder.buildQuery();
        
        return searchProvider.search(commonQuery, jiraAuthenticationContext.getUser(),
                        PagerFilter.getUnlimitedFilter()).getIssues();
    }
}