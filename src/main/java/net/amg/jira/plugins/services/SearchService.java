package net.amg.jira.plugins.services;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.status.Status;

import java.text.ParseException;
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
     * Finds issues given the search parameters.
     *
     * @param projectOrFilter id of project or filter, should start with 'project-' or 'filter-'
     * @param issueTypes      to be found (ungrouped)
     * @param date            date or number of days into the past constituting the beginning of requested history
     * @return List of issues constituting issue history grouped by graph index
     * @throws SearchException thrown by Lucene SearchProvider
     * @throws ParseException  thrown whenever user preference cannot be parsed
     */
    Map<String, List<Issue>> findIssues(String projectOrFilter, String issueTypes, String date)
            throws SearchException, ParseException;

    /**
     * Returns all JIRA statuses available for the currently logged in user.
     *
     * @return Collection of available statuses
     */
    Collection<Status> findAllStatuses();


    /**
     * Finds all issues from chosen project or filter
     *
     * @param projectOrFilter id of project or filter, should start with 'project-' or 'filter-'
     * @return List of issues
     * @throws SearchException thrown by Lucene SearchProvider
     */
    List<Issue> findAllIssues(String projectOrFilter) throws SearchException;

    /**
     * Groups issue type names in sets mapped to group names.
     *
     * @param ungroupedTypes
     * @return group names (GROUP_BASEn where n=group index) mapped to sets of issue type names. There are as many
     * groups as group numbers found in the argument. i.e. issue9|issue99 gives two groups group9 and group99.
     */
    Map<String, Set<String>> getGroupedIssueTypes(String ungroupedTypes);
}