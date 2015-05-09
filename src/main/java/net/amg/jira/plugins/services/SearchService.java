package net.amg.jira.plugins.services;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.status.Status;

import java.text.ParseException;
import java.util.Collection;
import java.util.List;

/**
 * Represents search operations used by the gadget.
 */
public interface SearchService {

    /**
     * Finds issues given the search parameters.
     *
     * @param projectOrFilter id of project or filter, should start with 'project-' or 'filter-'
     * @param issueTypes      to be found (ungrouped)
     * @param date  date or number of days into the past constituting the beginning of requested history
     * @return List of issues constituting issue history grouped by graph index
     * @throws SearchException thrown by Lucene SearchProvider
     * @throws ParseException  thrown whenever user preference cannot be parsed
     */
    List<List<Issue>> findIssues(String projectOrFilter, String issueTypes, String date)
            throws SearchException, ParseException;

    /**
     * Returns all JIRA statuses available for the currently logged in user.
     *
     * @return Collection of available statuses
     */
    Collection<Status> findAllStatuses();

}