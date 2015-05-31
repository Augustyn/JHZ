jQuery.namespace("AMG.jhz");
AMG.jhz.appendStatusesValue = function (statuses, index) {
    var statusGroup = JSON.parse(JSON.stringify(statuses));
    for (var i = 0, len = statusGroup.length; i < len; i++) {
        statusGroup[i].value = statusGroup[i].label.concat(index);
    }
    return statusGroup;
}
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
                            options: AMG.jhz.appendStatusesValue(args.statuses.statuses, "1")
                        },
                        {
                            userpref: "Issues",
                            selected: gadget.getPref("Issues"),
                            description: gadget.getMsg("issues.history.gadget.field.issue.description.others"),
                            type: "multiselect",
                            options: AMG.jhz.appendStatusesValue(args.statuses.statuses, "2")
                        },
                        {
                            userpref: "Issues",
                            selected: gadget.getPref("Issues"),
                            description: gadget.getMsg("issues.history.gadget.field.issue.description.others"),
                            type: "multiselect",
                            options: AMG.jhz.appendStatusesValue(args.statuses.statuses, "3")
                        },
                        {
                            userpref: "Issues",
                            selected: gadget.getPref("Issues"),
                            description: gadget.getMsg("issues.history.gadget.field.issue.description.others"),
                            type: "multiselect",
                            options: AMG.jhz.appendStatusesValue(args.statuses.statuses, "4")
                        },
                        {
                            userpref: "Issues",
                            selected: gadget.getPref("Issues"),
                            description: gadget.getMsg("issues.history.gadget.field.issue.description.others"),
                            type: "multiselect",
                            options: AMG.jhz.appendStatusesValue(args.statuses.statuses, "5")
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
                        {
                            id: "my-callback-field",
                            type: "callbackBuilder",
                            callback: function (parentDiv) {
                                AJS.$("[name='Issues']").select2();
                            }
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
                gadget.getView().addClass("chart").empty();

                var getChartContainer = function () {
                    var chart = AJS.$("<div id='chart' />").appendTo(gadget.getView());
                    return function () {
                        return chart;
                    };
                }();

                var safeEscapeString = function (text) {
                    if (text) {
                        return gadgets.util.escapeString(text);
                    } else {
                        return '';
                    }
                };

                var getChartImg = function () {
                    AJS.$("#chart", gadget.getView()).get(0).innerHTML += "<img style='display:none' src='" + gadget.getBaseUrl() + "/charts?filename=" + args.chart.location + "' alt='" + safeEscapeString(args.chart.filterTitle) + "' usemap='#" +
                    args.chart.imageMapName + "' height='" + args.chart.height + "' width='" + args.chart.width + "' />";
                    gadget.getView().append(args.chart.imageMap);
                    gadget.showLoading();
                    var chartImg = AJS.$("img", getChartContainer());
                    AJS.$(chartImg, gadget.getView()).load(function () {
                        AJS.$(this).show();
                        gadget.hideLoading();
                        gadget.resize();
                    });
                    return function () {
                        return chartImg;
                    };
                }();
            },
            args: [
                {
                    key: "chart",
                    ajaxOptions: function () {

                        var width = Math.round(gadgets.window.getViewportDimensions().width * 0.9);
                        if (width < 150) {
                            width = 150;
                        }
                        var height = Math.round(width * 2 / 3);

                        return {
                            url: "/rest/issueshistoryresource/1.0/chart/generate",
                            data: {
                                project: this.getPref("Project"),
                                date: this.getPref("Date"),
                                period: this.getPref("Period"),
                                issues: this.getPref("Issues"),
                                width: width,
                                height: height,
                                version: this.getPref("Version")
                            }
                        };
                    }
                }
            ]
        }
    });

}