package net.amg.jira.plugins.components;

import com.atlassian.jira.bc.config.ConstantsService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.InjectableComponent;
import com.atlassian.jira.web.bean.PagerFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Implementation of SearchService that uses Apache Lucene.
 */
public class SearchServiceImpl implements SearchService {

    private static final Logger log = LoggerFactory.getLogger(SearchServiceImpl.class);
    private static final Pattern datePattern = Pattern.compile("\\d{4}[-\\.\\/]\\d{1,2}[-\\.\\/]\\d{1,2}", Pattern.CASE_INSENSITIVE);
    private static final Pattern daysPattern = Pattern.compile("-?\\d{1,4}d", Pattern.CASE_INSENSITIVE);

    private JiraAuthenticationContext jiraAuthenticationContext;
    private SearchProvider searchProvider;
    private ConstantsService constantsService;
    private JqlClauseBuilder jqlClauseBuilder;

    public SearchServiceImpl(JiraAuthenticationContext authenticationContext, SearchProvider searchProvider, ConstantsService constantsService) {
        this.jiraAuthenticationContext = authenticationContext;
        this.searchProvider = searchProvider;
        this.constantsService = constantsService;
    }

    @Override
    public Collection<Status> findAllStatuses() {
        return constantsService.getAllStatuses(jiraAuthenticationContext.getUser().getDirectoryUser()).getReturnedValue();
    }

    @Override
    public List<Issue> findIssues(String projectOrFilter, String issueTypes, String daysPreviously)
            throws SearchException, InvalidPreferenceException, ParseException {
        jqlClauseBuilder = JqlQueryBuilder.newBuilder().where();
        resolveProjectOrFilter(projectOrFilter);
        resolveIssueTypes(issueTypes);
        resolveDaysPreviously(daysPreviously);
        return searchProvider.search(jqlClauseBuilder.buildQuery(), jiraAuthenticationContext.getUser(),
                PagerFilter.getUnlimitedFilter()).getIssues();
    }

    private void resolveProjectOrFilter(String projectOrFilter) throws InvalidPreferenceException {
        if (projectOrFilter.startsWith("project")) {
            jqlClauseBuilder.project(projectOrFilter.split("-")[1]);
        } else if (projectOrFilter.startsWith("filter")) {
            jqlClauseBuilder.savedFilter(projectOrFilter.split("-")[1]);
        } else {
            throw new InvalidPreferenceException("Invalid projectOrFilter format:" + projectOrFilter);
        }
        jqlClauseBuilder.and();
    }

    private void resolveIssueTypes(String issueTypes) {
        //TODO issue types parsing
        jqlClauseBuilder.status("open", "resolved").and();
    }

    private void resolveDaysPreviously(String daysPreviously) throws ParseException, InvalidPreferenceException {
        Date date;
        if (datePattern.matcher(daysPreviously).matches()) {
            date = new SimpleDateFormat("yyyy-MM-dd").parse(daysPreviously.replace("/", "-").replace(".", "-"));
        } else if (daysPattern.matcher(daysPreviously).matches()) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, -Integer.parseInt(daysPreviously.replaceAll("[d-]", "")));
            date = calendar.getTime();
        } else {
            throw new InvalidPreferenceException("Invalid daysPreviously format:" + daysPreviously);
        }
        jqlClauseBuilder.createdAfter(date);
    }
}