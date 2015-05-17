package net.amg.jira.plugins.model.charting;

import net.amg.jira.plugins.rest.model.IssuesHistoryChartModel;
import com.atlassian.jira.charts.Chart;
import com.atlassian.jira.charts.ChartFactory;
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.rest.v1.util.CacheControl;
import com.atlassian.jira.timezone.TimeZoneManager;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import net.amg.jira.plugins.services.SearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jarek
 */
@Path("/chart")
public class JiraChartResource {

    private static final Logger logger = LoggerFactory.getLogger(JiraChartResource.class);

    private final int ISSUES_GROUPS = 5;
    private final SearchService searchService;
    private final SearchProvider searchProvider;
    private final TimeZoneManager timeZoneManager;
    private final ChangeHistoryManager changeHistoryManager;
    private final VersionManager versionManager;
    private final ProjectManager projectManager;

    /**
     * Used by Spring to inject dependencies
     *
     * @param searchService
     * @param searchProvider
     * @param timeZoneManager
     * @param versionManager
     * @param projectManager
     * @param userManager
     * @param changeHistoryManager
     */
    public JiraChartResource(SearchService searchService,
            SearchProvider searchProvider, TimeZoneManager timeZoneManager,
            VersionManager versionManager, ProjectManager projectManager,
            ChangeHistoryManager changeHistoryManager) {
        this.searchService = searchService;
        this.searchProvider = searchProvider;
        this.timeZoneManager = timeZoneManager;
        this.versionManager = versionManager;
        this.changeHistoryManager = changeHistoryManager;
        this.projectManager = projectManager;
    }

    
    /**
     * Allows to generate chart which will be displayed later
     * @param project value of Project field
     * @param date value of date field
     * @param periodName value of period field
     * @param issues  values of Issues field
     * @param width chart width
     * @param height chart height
     * @param versionLabel value of version field
     * @return 
     */
    @GET
    @Path("/generate")
    public Response generateChart(
            @QueryParam("project") String project,
            @QueryParam("date") String date,
            @QueryParam("period") String periodName,
            @QueryParam("issues") String issues,
            @QueryParam("width") int width,
            @QueryParam("height") int height,
            @QueryParam("version") String versionLabel) {

        Date dateBegin = null;

        final List<Set<String>> statusesSets = getStatusesList(issues);
        try {
            dateBegin = getBeginDate(date);
        } catch (ParseException ex) {
            java.util.logging.Logger.getLogger(JiraChartResource.class.getName()).log(Level.SEVERE, null, ex);
        }

        final ChartFactory.PeriodName period = ChartFactory.PeriodName.valueOf(periodName.toLowerCase());
        final ChartFactory.VersionLabel label = getVersionLabel(versionLabel);
        
        
        JiraChart jirachart = new JiraChart(searchProvider, versionManager,
                searchService, timeZoneManager, changeHistoryManager, projectManager);

        Chart chart = jirachart.generateChart(project, statusesSets, period, label, dateBegin, width, height);

        IssuesHistoryChartModel jiraIssuesHistoryChart = new IssuesHistoryChartModel(chart.getLocation(), "title", chart.getImageMap(), chart.getImageMapName(), width, height);

        return Response.ok(jiraIssuesHistoryChart).cacheControl(CacheControl.NO_CACHE).build();
    }


    private ChartFactory.VersionLabel getVersionLabel(String versionLabel) {
        if(versionLabel.contains("major") || versionLabel.contains("znaczące")) return ChartFactory.VersionLabel.major;
        else if(versionLabel.contains("all") || versionLabel.contains("Wszystkie")) return ChartFactory.VersionLabel.all;
        else  return ChartFactory.VersionLabel.none;
    }

    private Date getBeginDate(String date) throws ParseException {
        Date beginningDate;
        if (date.startsWith("[d-]")) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, -Integer.parseInt(date.replaceAll("[d-]", "")));
            beginningDate = calendar.getTime();
        } else {
             beginningDate = new SimpleDateFormat("yyyy-MM-dd").parse(date.replace("/", "-").replace(".", "-"));
        }
        return beginningDate;
    }

    private List<Set<String>> getStatusesList(String issues) {
        String[] ungroupedTypes = issues.split("\\|");
        List<Set<String>> list = new ArrayList<>();
        
        for (int i = 0; i < ISSUES_GROUPS; i++) {
            list.add(new HashSet<String>());
            for (String issue : ungroupedTypes) {
                if (issue.endsWith(String.valueOf(i))) {
                    list.get(i).add(issue.replace(String.valueOf(i), ""));
                }
            }
        }
        return list;
    }

}
