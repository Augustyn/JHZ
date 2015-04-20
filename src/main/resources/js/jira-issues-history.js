jQuery.namespace("AMG.jhz");
AMG.jhz.init = function (args) {
    var gadget = AJS.Gadget({
        baseUrl: args.baseUrl,
        useOauth: "/rest/gadget/1.0/currentUser",
        config: {
            descriptor: function (args) {
                var gadget = this;
                return {
                    theme: "long-label",
                    action: "/rest/issueshistoryresource/1.0/configuration/validate",
                    fields: [
                        {
                            userpref: "Project",
                            class: "numField",
                            value: gadget.getPref("Project"),
                            label: gadget.getMsg("issues.history.gadget.field.project.label"),
                            description: gadget.getMsg("issues.history.gadget.field.project.description"),
                            type: "text"
                        },
                        {
                            userpref: "Issues",
                            class: "numField",
                            value: gadget.getPref("Issues"),
                            label: gadget.getMsg("issues.history.gadget.field.issue.label"),
                            description: gadget.getMsg("issues.history.gadget.field.issue.description"),
                            type: "text"
                        },
                        {
                            userpref: "Period",
                            label: gadget.getMsg("issues.history.gadget.field.period.label"),
                            selected: gadget.getPref("Period"),
                            description: gadget.getMsg("issues.history.gadget.field.period.description"),
                            type: "select",
                            options: [
                                {
                                    label: gadget.getMsg("issues.history.gadget.field.period.option.Hourly"),
                                    value: "hourly"
                                },
                                {
                                    label: gadget.getMsg("issues.history.gadget.field.period.option.Daily"),
                                    value: "daily"
                                },
                                {
                                    label: gadget.getMsg("issues.history.gadget.field.period.option.Weekly"),
                                    value: "weekly"
                                },
                                {
                                    label: gadget.getMsg("issues.history.gadget.field.period.option.Monthly"),
                                    value: "monthly"
                                },
                                {
                                    label: gadget.getMsg("issues.history.gadget.field.period.option.Quarterly"),
                                    value: "quarterly"
                                },
                                {
                                    label: gadget.getMsg("issues.history.gadget.field.period.option.Yearly"),
                                    value: "yearly"
                                }
                            ]
                        },
                        {
                            userpref: "Previously",
                            class: "numField",
                            value: gadget.getPref("Previously"),
                            label: gadget.getMsg("issues.history.gadget.field.previously.label"),
                            description: gadget.getMsg("issues.history.gadget.field.previously.description"),
                            type: "text"
                        },
                        AJS.gadget.fields.nowConfigured()
                    ]

                }
            }
        },
        view: {
            enableReload: true,
            onResizeReload: true,
            template: function (args) {
                var gadget = this;
                var mainDiv = AJS.$("<div/>");
                mainDiv.append(
                    AJS.$("<h1/>").text(args.user["fullName"])
                );
                mainDiv.append(
                    AJS.$("<h1/>").text(gadget.getMsg("issues.history.gadget.headers.allIssues"))
                );
                var issueList = AJS.$("<ul/>");
                AJS.$(args.issuesData.issues).each(function () {
                    issueList.append(
                        AJS.$("<li/>").append(
                            AJS.$("<a/>").attr({
                                target: "_parent",
                                title: gadgets.util.escapeString(this.key),
                                href: gadget.getBaseUrl() + "/browse/" + this.key
                            }).text(this.id + " " + this.key + " status:" + this.statusName)
                        )
                    );
                });
                mainDiv.append(issueList);
                mainDiv.append(
                    AJS.$("<h1/>").text(gadget.getMsg("issues.history.gadget.headers.specificIssues") + " " + gadget.getPref("Project"))
                );
                var requestedIssueList = AJS.$("<ul/>");
                AJS.$(args.requestedIssuesData.issues).each(function () {
                    requestedIssueList.append(
                        AJS.$("<li/>").append(
                            AJS.$("<a/>").attr({
                                target: "_parent",
                                title: gadgets.util.escapeString(this.key),
                                href: gadget.getBaseUrl() + "/browse/" + this.key
                            }).text(this.id + " " + this.key + " status:" + this.statusName)
                        )
                    );
                });
                mainDiv.append(requestedIssueList);
                gadget.getView().html(mainDiv);
            },
            args: [
                {
                    key: "user",
                    ajaxOptions: function () {
                        return {
                            url: "/rest/gadget/1.0/currentUser"
                        };
                    }
                },
                {
                    key: "issuesData",
                    ajaxOptions: function () {
                        return {
                            url: "/rest/issueshistoryresource/1.0/issues.json"
                        };
                    }
                },
                {
                    key: "requestedIssuesData",
                    ajaxOptions: function () {
                        return {
                            url: "/rest/issueshistoryresource/1.0/issues/byProjectName/" + encodeURI(this.getPref("Project")),
                            error: function (msg) {
                                gadget.showMessage("error", gadget.getMsg("issues.history.gadget.errors.emptyProjectOrFilter"), true, true);
                            }
                        };
                    }
                }
            ]
        }
    });
}
