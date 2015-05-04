package it.net.amg.jira.plugins.rest;

import net.amg.jira.plugins.rest.model.IssuesHistoryResourceModel;
import org.apache.wink.client.Resource;
import org.apache.wink.client.RestClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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
