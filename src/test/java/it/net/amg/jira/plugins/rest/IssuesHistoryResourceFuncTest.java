package it.net.amg.jira.plugins.rest;

import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.mockito.Mockito;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import net.amg.jira.plugins.rest.IssuesHistoryResource;
import net.amg.jira.plugins.rest.IssuesHistoryResourceModel;
import org.apache.wink.client.Resource;
import org.apache.wink.client.RestClient;

public class IssuesHistoryResourceFuncTest {

    @Before
    public void setup() {

    }

    @After
    public void tearDown() {

    }

    @Test
    public void messageIsValid() {

        String baseUrl = System.getProperty("baseurl");
        String resourceUrl = baseUrl + "/rest/issueshistoryresource/1.0/message";

        RestClient client = new RestClient();
        Resource resource = client.resource(resourceUrl);

        IssuesHistoryResourceModel message = resource.get(IssuesHistoryResourceModel.class);

//        assertEquals("wrong message","Hello World",message.getMessage());
    }
}
