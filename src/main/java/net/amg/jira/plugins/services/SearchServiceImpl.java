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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Implementation of SearchService that uses Apache Lucene.
 */
public class SearchServiceImpl implements SearchService {

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
    public List<Issue> findIssues(String projectOrFilter, String issueTypes, String daysPreviously)
            throws SearchException, ParseException {
        jqlClauseBuilder = JqlQueryBuilder.newBuilder().where();
        resolveProjectOrFilter(projectOrFilter);
        jqlClauseBuilder.and();
        resolveIssueTypes(issueTypes);
        jqlClauseBuilder.and();
        resolveDaysPreviously(daysPreviously);
        return searchProvider.search(jqlClauseBuilder.buildQuery(), jiraAuthenticationContext.getUser(),
                PagerFilter.getUnlimitedFilter()).getIssues();
    }

    private void resolveProjectOrFilter(String projectOrFilter) {
        if (validator.checkIfProject(projectOrFilter)) {
            jqlClauseBuilder.project(projectOrFilter.split("-")[1]);
        } else {
            jqlClauseBuilder.savedFilter(projectOrFilter.split("-")[1]);
        }
    }

    private void resolveIssueTypes(String issueTypes) {
        jqlClauseBuilder.status(issueTypes.split("\\|"));
    }

    private void resolveDaysPreviously(String date) throws ParseException {
        Date beginningDate;
        if (validator.checkIfDate(date)) {
            beginningDate = new SimpleDateFormat("yyyy-MM-dd").parse(date.replace("/", "-").replace(".", "-"));
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, -Integer.parseInt(date.replaceAll("[d-]", "")));
            beginningDate = calendar.getTime();
        }
        jqlClauseBuilder.createdAfter(beginningDate);
    }
}