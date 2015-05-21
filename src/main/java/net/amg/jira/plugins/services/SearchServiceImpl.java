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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    public Map<String, List<Issue>> findIssues(String projectOrFilter, String issueTypes, String date)
            throws SearchException, ParseException {
        Date beginningDate = resolveDaysPreviously(date);
        Map<String,Set<String>> groupedIssueTypes = getGroupedIssueTypes(issueTypes);
        JqlClauseBuilder commonClauseBuilder = JqlQueryBuilder.newBuilder().where().createdAfter(beginningDate).and();
        if (validator.checkIfProject(projectOrFilter)) {
            commonClauseBuilder.project(projectOrFilter.split("-")[1]);
        } else {
            commonClauseBuilder.savedFilter(projectOrFilter.split("-")[1]);
        }
        Query commonQuery = commonClauseBuilder.buildQuery();
        Map<String,List<Issue>> issueMap = new HashMap<>();
        for(String groupName : groupedIssueTypes.keySet()) {
            String[] typesGroup = groupedIssueTypes.get(groupName).toArray(new String[0]);
            jqlClauseBuilder = JqlQueryBuilder.newBuilder(commonQuery).where().and().status(typesGroup);
            issueMap.put(groupName,searchProvider.search(jqlClauseBuilder.buildQuery(), jiraAuthenticationContext.getUser(),
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

    @Override
    public Map<String, Set<String>> getGroupedIssueTypes(String ungroupedTypes) {
        String[] ungroupedTypesArr = ungroupedTypes.split("\\|");
        Map<String,Set<String>> issueTypeMap = new HashMap<>();
        Pattern pattern = Pattern.compile("\\d");
        for(String type : ungroupedTypesArr) {
            Matcher matcher = pattern.matcher(type);
            matcher.find();
            String groupName = LABEL_BASE +type.substring(matcher.start());
            if(!issueTypeMap.containsKey(groupName)) {
                issueTypeMap.put(groupName,new HashSet<String>());
            }
            issueTypeMap.get(groupName).add(type.replaceAll("\\d",""));
        }
        return  issueTypeMap;
    }
}