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
                                    value: "all"
                                },
                                {
                                    label: gadget.getMsg("issues.history.gadget.field.version.option.Major"),
                                    value: "major"
                                },
                                {
                                    label: gadget.getMsg("issues.history.gadget.field.version.option.None"),
                                    value: "none"
                                }
                            ]
                        },
                        {
                            id: "my-callback-field",
                            type: "callbackBuilder",
                            callback: function (parentDiv) {
                                AJS.$("[name='Issues']").select2();
                                AJS.$("[class*='select2-container']").attr({
                                    style: "width: 250px"
                                });
                                AJS.$("[name='Issues']").on("select2:select", function (e) { gadget.resize(); });
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
                gadget.projectOrFilterName = args.chart.filterTitle;
                if (gadgets.views.getCurrentView().getName() === "canvas") {

                    var dataTable = AJS.gadgets.templater.Table({
                        descriptor: function (args) {
                            return {
                                cols: function () {
                                    var headers = [];
                                    headers.push({
                                        header: "Period"
                                    })
                                    args.groupNames.forEach(function (h) {
                                        headers.push({
                                            header: h
                                        })
                                    });
                                    return headers;
                                }(),
                                data: function () {
                                    var rows = [];
                                    args.chartData.forEach(function (entry) {
                                        var cells = [];
                                        cells.push({
                                            value: entry.period,
                                            label: new Date(entry.period)
                                        });
                                        entry.issueCount.forEach(function(count) {
                                            cells.push({
                                                value: count.value,
                                                label: count.value
                                            })
                                        })
                                        rows.push(cells);
                                    });
                                    return rows;
                                }()
                            };
                        },
                        sortable: true,
                        args: [
                            {key: "chartData", data: args.chart.table.entries},
                            {key: "groupNames", data: args.chart.table.groupNames}
                        ]
                    });

                    var getDataTable = function () {
                        return AJS.$("table.aui", gadget.getView());
                    };

                    var configureAlignment = function () {
                        getDataTable().css("marginLeft", function () {
                            var chartWidth = getChartContainer().outerWidth();
                            var offsetX = (gadget.getView().outerWidth() - chartWidth - getDataTable().width()) / 2;
                            if (offsetX > 0) {
                                return offsetX;
                            }
                            return 0;
                        }());
                    };

                    var createCanvasDataTable = function () {
                        dataTable.addCallback(function (fragment) {
                            gadget.getView().append(fragment);
                            configureAlignment();
                            gadget.resize();
                        });
                        dataTable.build();
                    };
                    getChartImg().append(createCanvasDataTable);
                }
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
                                version: this.getPref("Version"),
                                table: gadgets.views.getCurrentView().getName() === "canvas"
                            }
                        };
                    }
                }
            ]
        }
    });

}