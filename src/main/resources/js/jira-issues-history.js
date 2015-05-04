jQuery.namespace("AMG.jhz");
AMG.jhz.init = function (params) {
    var gadget = AJS.Gadget({
        baseUrl: params.baseUrl,
        useOauth: "/rest/gadget/1.0/currentUser",
        config: {
            descriptor: function (args) {
                var gadget = this;
                var searchParam;
                if (/^jql-/.test(this.getPref("Project")) || this.getPref("isPopup") === "true") {
                    searchParam =
                    {
                        userpref: "projectOrFilterId",
                        type: "hidden",
                        value: gadgets.util.unescapeString(gadget.getPref("Project"))
                    };
                }
                else {
                    searchParam = AJS.gadget.fields.projectOrFilterPicker(gadget, "Project");
                }
                return {
                    theme: "long-label",
                    action: "/rest/issueshistoryresource/1.0/configuration/validate",
                    fields: [
                        jQuery.extend(true, {}, searchParam, {
                            label: gadget.getMsg("issues.history.gadget.field.project.label"),
                            description: gadget.getMsg("issues.history.gadget.field.project.description")
                        }),
                        {
                            userpref: "Issues",
                            selected: gadget.getPref("Issues"),
                            label: gadget.getMsg("issues.history.gadget.field.issue.label"),
                            description: gadget.getMsg("issues.history.gadget.field.issue.description"),
                            type: "multiselect",
                            options: args.statuses.statuses
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
                            id: "Calendar",
                            userpref: "Date",
                            label: gadget.getMsg("issues.history.gadget.field.date.label"),
                            type: "callbackBuilder",
                            callback: function (parentDiv) {
                                parentDiv.append(
                                    AJS.$("<input/>").attr({
                                        id: "date-picker",
                                        type: "text",
                                        name: "Date",
                                        "class": "text"
                                    }).val(gadget.getPref("Date"))
                                );
                                parentDiv.append(
                                    AJS.$("<button />").attr({
                                        id: "date-picker-button",
                                        type: "button",
                                        "class": "aui-icon icon-date"
                                    }).val("date")
                                );
                                parentDiv.append(
                                    AJS.$("<div/>").attr({
                                        "class": "description"
                                    }).text(gadget.getMsg("issues.history.gadget.field.date.description"))
                                );
                                Calendar.setup({
                                    firstDay: 1,
                                    inputField: 'date-picker',
                                    button: 'date-picker-button',
                                    align: 'Br',
                                    singleClick: true,
                                    showsTime: true,
                                    useISO8601WeekNumbers: false,
                                    ifFormat: '%Y-%m-%d'
                                });
                            }
                        },
                        {
                            userpref: "Version",
                            label: gadget.getMsg("issues.history.gadget.field.version.label"),
                            selected: gadget.getPref("Version"),
                            description: gadget.getMsg("issues.history.gadget.field.version.description"),
                            type: "select",
                            options: [
                                {
                                    label: gadget.getMsg("issues.history.gadget.field.version.option.All"),
                                    value: "All versions"
                                },
                                {
                                    label: gadget.getMsg("issues.history.gadget.field.version.option.Major"),
                                    value: "Only major versions"
                                },
                                {
                                    label: gadget.getMsg("issues.history.gadget.field.version.option.None"),
                                    value: "None"
                                }
                            ]
                        },
                        AJS.gadget.fields.nowConfigured()
                    ]
                }
            },
            args: [{
                key: "statuses",
                ajaxOptions: "/rest/issueshistoryresource/1.0/issues/statuses.json"
            }]
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
                mainDiv.append(
                    AJS.$("<h1/>").text(gadget.getMsg("issues.history.gadget.field.project.label") + gadget.getPref("Project")),
                    AJS.$("<h1/>").text(gadget.getMsg("issues.history.gadget.field.issue.label") + gadget.getPref("Issues")),
                    AJS.$("<h1/>").text(gadget.getMsg("issues.history.gadget.field.period.label") + gadget.getPref("Period")),
                    AJS.$("<h1/>").text(gadget.getMsg("issues.history.gadget.field.previously.label") + gadget.getPref("Previously")),
                    AJS.$("<h1/>").text(gadget.getMsg("issues.history.gadget.field.date.label") + gadget.getPref("Date")),
                    AJS.$("<h1/>").text(gadget.getMsg("issues.history.gadget.field.version.label") + gadget.getPref("Version")),
                    AJS.$("<h1/>").text(gadget.getMsg("gadget.common.refresh.label") + gadget.getPref("refresh"))
                );
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
                }
            ]
        }
    });
}