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

package net.amg.jira.plugins.jhz.rest.controller;

import com.atlassian.jira.issue.status.Status;
import com.google.gson.Gson;
import net.amg.jira.plugins.jhz.rest.model.StatusesResourceModel;
import net.amg.jira.plugins.jhz.services.SearchServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.osgi.extensions.annotation.ServiceReference;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;

/**
 * Resource providing all issue status types
 */
@Path("/issues")
public class IssuesHistoryResource {

    private static final Logger logger = LoggerFactory.getLogger(IssuesHistoryResource.class);
    private SearchServiceImpl searchService;

    /**
     * Exposes all available statuses in Jira for the currently logged in user
     *
     * @return Response with statuses (StatusRepresentations) in JSON format.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/statuses")
    public Response getStatusTypes() {
        Collection<Status> allStatuses = searchService.findAllStatuses();
        Gson gson = new Gson();
        return Response.ok(gson.toJson(new StatusesResourceModel(allStatuses))).build();
    }

    @POST
    @Path("{args : (.*)?}")
    public Response postStub() {
        return Response.status(Response.Status.NOT_FOUND).entity("No such resource").build();
    }

    @PUT
    @Path("{args : (.*)?}")
    public Response putStub() {
        return Response.status(Response.Status.NOT_FOUND).entity("No such resource").build();
    }

    @DELETE
    @Path("{args : (.*)?}")
    public Response deleteStub() {
        return Response.status(Response.Status.NOT_FOUND).entity("No such resource").build();
    }

    @ServiceReference
    public void setSearchService(SearchServiceImpl searchService) {
        this.searchService = searchService;
    }
}
