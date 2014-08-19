//To store current filter selection
var a_selectedSystem = "";
var a_selectedWorkflow = "";
var a_selectedActivity = "";

//To store current result table
var a_TableData = [];

//To store current time format
var a_timeFormat = "Minutes";

var a_results = {};

var a_tColumns = [];

var initializeActivityFilter = function()
{
    var systemParams = {};
    systemParams.displayMember = ['sName'];
    systemParams.valueMember = ['s'];
    systemParams.filter = [];

    var workflowParams = {};
    workflowParams.displayMember = ['wName'];
    workflowParams.valueMember = ['w'];
    workflowParams.filter = [];

    var activityParams = {};
    activityParams.displayMember = ['aName'];
    activityParams.valueMember = ['a'];
    activityParams.filter = [];

    activity_fillSystemNames(generateFilterSource(workflowAndActivityFilterData, systemParams));
    activity_fillWorkflowNames(generateFilterSource(workflowAndActivityFilterData, workflowParams));
    activity_fillActivityNames(generateFilterSource(workflowAndActivityFilterData, activityParams));
}

function getActivityFilter()
{
    if (((a_selectedSystem === "all") || (a_selectedSystem === "")) && ((a_selectedWorkflow === "all") || (a_selectedWorkflow === "")) && ((a_selectedActivity === "all") || (a_selectedActivity === "")))
        return '';
    //single filter
    else if (((a_selectedSystem != "all") && (a_selectedSystem != "")) && ((a_selectedWorkflow === "all") || (a_selectedWorkflow === "")) && ((a_selectedActivity === "all") || (a_selectedActivity === "")))
        return 'FILTER (?s =<' + a_selectedSystem + '>)';
    else if (((a_selectedSystem === "all") || (a_selectedSystem === "")) && ((a_selectedWorkflow != "all") && (a_selectedWorkflow != "")) && ((a_selectedActivity === "all") || (a_selectedActivity === "")))
        return 'FILTER (?w =<' + a_selectedWorkflow + '>)';
    else if (((a_selectedSystem === "all") || (a_selectedSystem === "")) && ((a_selectedWorkflow === "all") || (a_selectedWorkflow === "")) && ((a_selectedActivity != "all") && (a_selectedActivity != "")))
        return 'FILTER (?a =<' + a_selectedActivity + '>)';

    //two filters
    else if (((a_selectedSystem != "all") || (a_selectedSystem != "")) && ((a_selectedWorkflow != "all") || (a_selectedWorkflow != "")) && ((a_selectedActivity === "all") || (a_selectedActivity === "")))
        return 'FILTER ((?s= <' + a_selectedSystem + '>) && (?w =<' + a_selectedWorkflow + '>))';
    else if (((a_selectedSystem === "all") || (a_selectedSystem === "")) && ((a_selectedWorkflow != "all") || (a_selectedWorkflow != "")) && ((a_selectedActivity != "all") || (a_selectedActivity != "")))
        return 'FILTER ((?w= <' + a_selectedWorkflow + '>) && (?a =<' + a_selectedActivity + '>))';
    else if (((a_selectedSystem != "all") && (a_selectedSystem != "")) && ((a_selectedWorkflow === "all") || (a_selectedWorkflow === "")) && ((a_selectedActivity != "all") && (a_selectedActivity != "")))
        return 'FILTER ((?s= <' + a_selectedSystem + '>) && (?a =<' + a_selectedActivity + '>))';

    //three filters
    else
        return 'FILTER ((?s= <' + a_selectedSystem + '>) && (?w =<' + a_selectedWorkflow + '>) && (?a =<' + a_selectedActivity + '>))';
}

function showResultsActivity()
{
    var result = {};
    activityTableData = result;

    var filter = getActivityFilter();

    executeQueries(filter);
}

function executeQueries(filter)
{
    a_results = {};
    if ($( "#metrics_a_t" ).hasClass( "active" ) && !$( "#metrics_a_a" ).hasClass( "active" ) && !$( "#metrics_a_s" ).hasClass( "active" ))
    {
        var query1 = config_queries_statistics_activity_tt.replace('$$filter$$', filter);
        var task1 = function(data) {
            a_results.tt = data;
            mergeResults(a_results);
        };

        executeQueryAction(query1, task1);
    }
    else if (
                ($( "#metrics_a_t" ).hasClass( "active" ) && $( "#metrics_a_a" ).hasClass( "active" ) && !$( "#metrics_a_s" ).hasClass( "active" )) ||
                (!$( "#metrics_a_t" ).hasClass( "active" ) && $( "#metrics_a_a" ).hasClass( "active" ) && !$( "#metrics_a_s" ).hasClass( "active" ))
            )
    {
        var query1 = config_queries_statistics_activity_tt.replace('$$filter$$', filter);
        var task1 = function(data) {
            a_results.tt = data;
            //execute next query
            var query2 = config_queries_statistics_activity_at.replace('$$filter$$', filter);
            var task2 = function(data) {
                a_results.at = data;
                mergeResults(a_results);
            };
            executeQueryAction(query2, task2);
        };

        executeQueryAction(query1, task1);
    }
    else if (
                ($( "#metrics_a_t" ).hasClass( "active" ) && !$( "#metrics_a_a" ).hasClass( "active" ) && $( "#metrics_a_s" ).hasClass( "active" )) ||
                (!$( "#metrics_a_t" ).hasClass( "active" ) && !$( "#metrics_a_a" ).hasClass( "active" ) && $( "#metrics_a_s" ).hasClass( "active" ))
            )
    {
        var query1 = config_queries_statistics_activity_tt.replace('$$filter$$', filter);
        var task1 = function(data) {
            a_results.tt = data;
            //execute next query
            var query2 = config_queries_statistics_activity_st.replace('$$filter$$', filter);
            var task2 = function(data) {
                a_results.st = data;
                mergeResults(a_results);
            };
            executeQueryAction(query2, task2);
        };

        executeQueryAction(query1, task1);
    }
    else if (
                ($( "#metrics_a_t" ).hasClass( "active" ) && $( "#metrics_a_a" ).hasClass( "active" ) && $( "#metrics_a_s" ).hasClass( "active" )) ||
                (!$( "#metrics_a_t" ).hasClass( "active" ) && $( "#metrics_a_a" ).hasClass( "active" ) && $( "#metrics_a_s" ).hasClass( "active" ))
            )
    {
        var query1 = config_queries_statistics_activity_tt.replace('$$filter$$', filter);
        var task1 = function(data) {
            a_results.tt = data;
            //execute next query
            var query2 = config_queries_statistics_activity_at.replace('$$filter$$', filter);
            var task2 = function(data) {
                a_results.at = data;
                //execute next query
                var query3 = config_queries_statistics_activity_st.replace('$$filter$$', filter);
                var task3 = function(data) {
                    a_results.st = data;

                    mergeResults(a_results);
                };
                executeQueryAction(query3, task3);
            };
            executeQueryAction(query2, task2);
        };

        executeQueryAction(query1, task1);
    }
}

function mergeResults(results)
{
    if ($( "#metrics_a_t" ).hasClass( "active" ) && !$( "#metrics_a_a" ).hasClass( "active" ) && !$( "#metrics_a_s" ).hasClass( "active" ))
    {
        a_tColumns = [{"data": "System"}, {"data": "Workflow"}, {"data": "Activity"}, {"data": "tTime"}];
    }
    else if (!$( "#metrics_a_t" ).hasClass( "active" ) && $( "#metrics_a_a" ).hasClass( "active" ) && !$( "#metrics_a_s" ).hasClass( "active" ))
    {
        a_tColumns = [{"data": "System"}, {"data": "Workflow"}, {"data": "Activity"}, {"data": "aTime"}];
        $.each(results.tt.results.bindings, function(i, row_tt) {
            var system_tt = row_tt['sName'].value;
            var workflow_tt = row_tt['wName'].value;
            var activity_tt = row_tt['aName'].value;

            //search other results for same identifiers and add results if found
            $.each(results.at.results.bindings, function(i, row_at) {
                try{
                    var system_at = row_at['sName'].value;
                    var workflow_at = row_at['wName'].value;
                    var activity_at = row_at['aName'].value;
                    if (system_at == system_tt && workflow_at == workflow_tt && activity_at == activity_tt)
                    {
                        row_tt['aTime_avg'] = row_at['aTime_avg'];
                        row_tt['aTime_min'] = row_at['aTime_min'];
                        row_tt['aTime_max'] = row_at['aTime_max'];
                    }
                }
                catch (e) {}
            });
        });
    }
    else if (!$( "#metrics_a_t" ).hasClass( "active" ) && !$( "#metrics_a_a" ).hasClass( "active" ) && $( "#metrics_a_s" ).hasClass( "active" ))
    {
        a_tColumns = [{"data": "System"}, {"data": "Workflow"}, {"data": "Activity"}, {"data": "sTime"}];
        $.each(results.tt.results.bindings, function(i, row_tt) {
            var system_tt = row_tt['sName'].value;
            var workflow_tt = row_tt['wName'].value;
            var activity_tt = row_tt['aName'].value;

            //search other results for same identifiers and add results if found
            $.each(results.st.results.bindings, function(i, row_st) {
                try{
                    var system_st = row_st['sName'].value;
                    var workflow_st = row_st['wName'].value;
                    var activity_st = row_st['aName'].value;
                    if (system_st == system_tt && workflow_st == workflow_tt && activity_st == activity_tt)
                    {
                        row_tt['aTime_avg'] = row_st['aTime_avg'];
                        row_tt['aTime_min'] = row_st['aTime_min'];
                        row_tt['aTime_max'] = row_st['aTime_max'];
                    }
                }
                catch (e) {}
            });
        });
    }
    else if ($( "#metrics_a_t" ).hasClass( "active" ) && $( "#metrics_a_a" ).hasClass( "active" ) && !$( "#metrics_a_s" ).hasClass( "active" ))
    {
        //Create column data
        a_tColumns = [{"data": "System"}, {"data": "Workflow"}, {"data": "Activity"}, {"data": "tTime"}, {"data": "aTime"}];
        //Merge data
        $.each(results.tt.results.bindings, function(i, row_tt) {
            var system_tt = row_tt['sName'].value;
            var workflow_tt = row_tt['wName'].value;
            var activity_tt = row_tt['aName'].value;

            //search other results for same identifiers and add results if found
            $.each(results.at.results.bindings, function(i, row_at) {
                try{
                    var system_at = row_at['sName'].value;
                    var workflow_at = row_at['wName'].value;
                    var activity_at = row_at['aName'].value;
                    if (system_at == system_tt && workflow_at == workflow_tt && activity_at == activity_tt)
                    {
                        row_tt['aTime_avg'] = row_at['aTime_avg'];
                        row_tt['aTime_min'] = row_at['aTime_min'];
                        row_tt['aTime_max'] = row_at['aTime_max'];
                    }
                }
                catch (e) {}
            });
        });
    }
    else if (!$( "#metrics_a_t" ).hasClass( "active" ) && $( "#metrics_a_a" ).hasClass( "active" ) && $( "#metrics_a_s" ).hasClass( "active" ))
    {
        //Create column data
        a_tColumns = [{"data": "System"}, {"data": "Workflow"}, {"data": "Activity"}, {"data": "aTime"}, {"data": "sTime"}];
        //Merge data
        $.each(results.tt.results.bindings, function(i, row_tt) {
            var system_tt = row_tt['sName'].value;
            var workflow_tt = row_tt['wName'].value;
            var activity_tt = row_tt['aName'].value;

            //search other results for same identifiers and add results if found
            $.each(results.at.results.bindings, function(i, row_at) {
                try{
                    var system_at = row_at['sName'].value;
                    var workflow_at = row_at['wName'].value;
                    var activity_at = row_at['aName'].value;
                    if (system_at == system_tt && workflow_at == workflow_tt && activity_at == activity_tt)
                    {
                        row_tt['aTime_avg'] = row_at['aTime_avg'];
                        row_tt['aTime_min'] = row_at['aTime_min'];
                        row_tt['aTime_max'] = row_at['aTime_max'];
                    }
                }
                catch (e) {}
            });

            $.each(results.st.results.bindings, function(i, row_st) {
                try{
                    var system_st = row_st['sName'].value;
                    var workflow_st = row_st['wName'].value;
                    var activity_st = row_st['aName'].value;
                    if (system_st == system_tt && workflow_st == workflow_tt && activity_st == activity_tt)
                    {
                        row_tt['sTime_avg'] = row_st['sTime_avg'];
                        row_tt['sTime_min'] = row_st['sTime_min'];
                        row_tt['sTime_max'] = row_st['sTime_max'];
                    }
                }
                catch (e) {}
            });
        });
    }
    else if ($( "#metrics_a_t" ).hasClass( "active" ) && !$( "#metrics_a_a" ).hasClass( "active" ) && $( "#metrics_a_s" ).hasClass( "active" ))
    {
        a_tColumns = [{"data": "System"}, {"data": "Workflow"}, {"data": "Activity"}, {"data": "tTime"}, {"data": "sTime"}];
        $.each(results.tt.results.bindings, function(i, row_tt) {
            var system_tt = row_tt['sName'].value;
            var workflow_tt = row_tt['wName'].value;
            var activity_tt = row_tt['aName'].value;

            //search other results for same identifiers and add results if found
            $.each(results.st.results.bindings, function(i, row_st) {
                try{
                    var system_st = row_st['sName'].value;
                    var workflow_st = row_st['wName'].value;
                    var activity_st = row_st['aName'].value;
                    if (system_st == system_tt && workflow_st == workflow_tt && activity_st == activity_tt)
                    {
                        row_tt['aTime_avg'] = row_st['aTime_avg'];
                        row_tt['aTime_min'] = row_st['aTime_min'];
                        row_tt['aTime_max'] = row_st['aTime_max'];
                    }
                }
                catch (e) {}
            });
        });
    }
    else if ($( "#metrics_a_t" ).hasClass( "active" ) && $( "#metrics_a_a" ).hasClass( "active" ) && $( "#metrics_a_s" ).hasClass( "active" ))
    {
        //Create column data
        a_tColumns = [{"data": "System"}, {"data": "Workflow"}, {"data": "Activity"}, {"data": "tTime"}, {"data": "aTime"}, {"data": "sTime"}];
        //Merge data
        $.each(results.tt.results.bindings, function(i, row_tt) {
            var system_tt = row_tt['sName'].value;
            var workflow_tt = row_tt['wName'].value;
            var activity_tt = row_tt['aName'].value;

            //search other results for same identifiers and add results if found
            $.each(results.at.results.bindings, function(i, row_at) {
                try{
                    var system_at = row_at['sName'].value;
                    var workflow_at = row_at['wName'].value;
                    var activity_at = row_at['aName'].value;
                    if (system_at == system_tt && workflow_at == workflow_tt && activity_at == activity_tt)
                    {
                        row_tt['aTime_avg'] = row_at['aTime_avg'];
                        row_tt['aTime_min'] = row_at['aTime_min'];
                        row_tt['aTime_max'] = row_at['aTime_max'];
                    }
                }
                catch (e) {}
            });

            $.each(results.st.results.bindings, function(i, row_st) {
                try{
                    var system_st = row_st['sName'].value;
                    var workflow_st = row_st['wName'].value;
                    var activity_st = row_st['aName'].value;
                    if (system_st == system_tt && workflow_st == workflow_tt && activity_st == activity_tt)
                    {
                        row_tt['sTime_avg'] = row_st['sTime_avg'];
                        row_tt['sTime_min'] = row_st['sTime_min'];
                        row_tt['sTime_max'] = row_st['sTime_max'];
                    }
                }
                catch (e) {}
            });
        });
    }
    fillActivityResultsTable(results.tt);
}

function fillActivityResultsTable(data)
{
    if (data != null)
        a_TableData = data;

    //Create table data
    var tData = generateActivityStatisticsTableData(a_TableData);

    activityResultTable.fnDestroy(true);
    recreateTable(a_tColumns);

    activityResultTable = $('#activityStatisticsResult').dataTable({
        data: tData,
        columns: a_tColumns
    });
}

function generateActivityStatisticsTableData(result)
{
    var data = [];
    if ($( "#metrics_a_t" ).hasClass( "active" ) && !$( "#metrics_a_a" ).hasClass( "active" ) && !$( "#metrics_a_s" ).hasClass( "active" ))
    {
        $.each(result.results.bindings, function(i, binding) {
            var row = {
                System: binding['sName'].value,
                Workflow: binding['wName'].value,
                Activity: binding['aName'].value,
                tTime: getFormatedValue(binding, 't'),
            };

            data.push(row);
        });
    }
    else if (!$( "#metrics_a_t" ).hasClass( "active" ) && $( "#metrics_a_a" ).hasClass( "active" ) && !$( "#metrics_a_s" ).hasClass( "active" ))
    {
        $.each(result.results.bindings, function(i, binding) {
            var row = {
                System: binding['sName'].value,
                Workflow: binding['wName'].value,
                Activity: binding['aName'].value,
                aTime: getFormatedValue(binding, 'a'),
            };

            data.push(row);
        });
    }
    else if (!$( "#metrics_a_t" ).hasClass( "active" ) && !$( "#metrics_a_a" ).hasClass( "active" ) && $( "#metrics_a_s" ).hasClass( "active" ))
    {
        $.each(result.results.bindings, function(i, binding) {
            var row = {
                System: binding['sName'].value,
                Workflow: binding['wName'].value,
                Activity: binding['aName'].value,
                sTime: getFormatedValue(binding, 's'),
            };

            data.push(row);
        });
    }
    else if ($( "#metrics_a_t" ).hasClass( "active" ) && $( "#metrics_a_a" ).hasClass( "active" ) && !$( "#metrics_a_s" ).hasClass( "active" ))
    {
        $.each(result.results.bindings, function(i, binding) {
            var row = {
                System: binding['sName'].value,
                Workflow: binding['wName'].value,
                Activity: binding['aName'].value,
                tTime: getFormatedValue(binding, 't'),
                aTime: getFormatedValue(binding, 'a')
            };

            data.push(row);
        });
    }
    else if (!$( "#metrics_a_t" ).hasClass( "active" ) && $( "#metrics_a_a" ).hasClass( "active" ) && $( "#metrics_a_s" ).hasClass( "active" ))
    {
        $.each(result.results.bindings, function(i, binding) {
            var row = {
                System: binding['sName'].value,
                Workflow: binding['wName'].value,
                Activity: binding['aName'].value,
                aTime: getFormatedValue(binding, 'a'),
                sTime: getFormatedValue(binding, 's')
            };

            data.push(row);
        });
    }
    else if ($( "#metrics_a_t" ).hasClass( "active" ) && !$( "#metrics_a_a" ).hasClass( "active" ) && $( "#metrics_a_s" ).hasClass( "active" ))
    {
        $.each(result.results.bindings, function(i, binding) {
            var row = {
                System: binding['sName'].value,
                Workflow: binding['wName'].value,
                Activity: binding['aName'].value,
                tTime: getFormatedValue(binding, 't'),
                sTime: getFormatedValue(binding, 's')
            };

            data.push(row);
        });
    }
    else if ($( "#metrics_a_t" ).hasClass( "active" ) && $( "#metrics_a_a" ).hasClass( "active" ) && $( "#metrics_a_s" ).hasClass( "active" ))
    {
        $.each(result.results.bindings, function(i, binding) {
            var row = {
                System: binding['sName'].value,
                Workflow: binding['wName'].value,
                Activity: binding['aName'].value,
                tTime: getFormatedValue(binding, 't'),
                aTime: getFormatedValue(binding, 'a'),
                sTime: getFormatedValue(binding, 's')
            };

            data.push(row);
        });
    }

    return data;
}

function getFormatedValue(data, type)
{
    var value = "Not available";
    try{
         var time_avg = truncateDecimals(convertMilli(data[type+'Time_avg'].value, a_timeFormat));
         var time_min = truncateDecimals(convertMilli(data[type+'Time_min'].value, a_timeFormat));
         var time_max = truncateDecimals(convertMilli(data[type+'Time_avg'].value, a_timeFormat));

         value = 'Average: ' + time_avg + '<br/> ' + 'Minimum: ' + time_min + ' <br/> ' + 'Maximum: ' + time_max;
      }
      catch (e) {}
      return value;
}

function activity_fillSystemNames(source)
{
    $("#a_filter_systemnames").jqxComboBox({source: source, selectedIndex: 0, displayMember: 'name', valueMember: 'uri', width: '250px', height: '25px'});
    // bind to 'select' event.
    $('#a_filter_systemnames').bind('select', function(event) {
        var args = event.args;
        var item = $('#a_filter_systemnames').jqxComboBox('getItem', args.index);
        a_selectedSystem = item.value;

        //Filter workflow names
        var workflowParams = {};
        workflowParams.displayMember = ['wName'];
        workflowParams.valueMember = ['w'];
        workflowParams.filter = [{'key': 's', 'value': a_selectedSystem}];

        activity_fillWorkflowNames(generateFilterSource(workflowAndActivityFilterData, workflowParams));

        //Filter activity names
        var activityParams = {};
        activityParams.displayMember = ['aName'];
        activityParams.valueMember = ['a'];
        activityParams.filter = [{'key': 's', 'value': a_selectedSystem}];

        activity_fillActivityNames(generateFilterSource(workflowAndActivityFilterData, activityParams));
    });
}

function activity_fillWorkflowNames(source)
{
    $("#a_filter_workflownames").jqxComboBox({source: source, selectedIndex: 0, displayMember: 'name', valueMember: 'uri', width: '250px', height: '25px'});
    // bind to 'select' event.
    $('#a_filter_workflownames').bind('select', function(event) {
        var args = event.args;
        var item = $('#a_filter_workflownames').jqxComboBox('getItem', args.index);
        a_selectedWorkflow = item.value;

        //Filter activity names
        var activityParams = {};
        activityParams.displayMember = ['aName'];
        activityParams.valueMember = ['a'];
        activityParams.filter = [{'key': 'w', 'value': a_selectedWorkflow}];

        activity_fillActivityNames(generateFilterSource(workflowAndActivityFilterData, activityParams));
    });
}

function activity_fillActivityNames(source)
{
    // Create a jqxListBox
    $("#a_filter_activitynames").jqxComboBox({source: source, selectedIndex: 0, displayMember: 'name', valueMember: 'uri', width: '250px', height: '25px'});
    // bind to 'select' event.
    $('#a_filter_activitynames').bind('select', function(event) {
        var args = event.args;
        var item = $('#a_filter_activitynames').jqxComboBox('getItem', args.index);
        a_selectedActivity = item.value;
    });
}

function recreateTable()
{
    var tColumns;
    if ($( "#metrics_a_t" ).hasClass( "active" ) && $( "#metrics_a_a" ).hasClass( "active" ) && $( "#metrics_a_s" ).hasClass( "active" )) tColumns = ['System', 'Workflow', 'Activity', 'Turnaround Time', 'Running Time', 'Suspend Time'];
    else if ($( "#metrics_a_t" ).hasClass( "active" ) && !$( "#metrics_a_a" ).hasClass( "active" ) && !$( "#metrics_a_s" ).hasClass( "active" )) tColumns = ['System', 'Workflow', 'Activity', 'Turnaround Time'];
    else if (!$( "#metrics_a_t" ).hasClass( "active" ) && $( "#metrics_a_a" ).hasClass( "active" ) && !$( "#metrics_a_s" ).hasClass( "active" )) tColumns = ['System', 'Workflow', 'Activity', 'Running Time'];
    else if (!$( "#metrics_a_t" ).hasClass( "active" ) && !$( "#metrics_a_a" ).hasClass( "active" ) && $( "#metrics_a_s" ).hasClass( "active" )) tColumns = ['System', 'Workflow', 'Activity', 'Suspend Time'];
    else if ($( "#metrics_a_t" ).hasClass( "active" ) && $( "#metrics_a_a" ).hasClass( "active" ) && !$( "#metrics_a_s" ).hasClass( "active" )) tColumns = ['System', 'Workflow', 'Activity', 'Turnaround Time', 'Running Time'];
    else if (!$( "#metrics_a_t" ).hasClass( "active" ) && $( "#metrics_a_a" ).hasClass( "active" ) && $( "#metrics_a_s" ).hasClass( "active" )) tColumns = ['System', 'Workflow', 'Activity', 'Running Time', 'Suspend Time'];
    else if ($( "#metrics_a_t" ).hasClass( "active" ) && !$( "#metrics_a_a" ).hasClass( "active" ) && $( "#metrics_a_s" ).hasClass( "active" )) tColumns = ['System', 'Workflow', 'Activity', 'Turnaround Time', 'Suspend Time'];
    else if ($( "#metrics_a_t" ).hasClass( "active" ) && $( "#metrics_a_a" ).hasClass( "active" ) && $( "#metrics_a_s" ).hasClass( "active" )) tColumns = ['System', 'Workflow', 'Activity', 'Turnaround Time', 'Running Time', 'Suspend Time'];

    var header = '';

    $.each(tColumns, function(index, value) {
        header += '<th>' + value + '</th>';
    });

    $("#activityResult").replaceWith
    (
         '<div id="activityResult">'
         +'<table id="activityStatisticsResult" class="table table-striped table-bordered" cellspacing="0" width="100%">'
            +'<thead><tr>' + header + '</tr></thead>'
         +'</table>'
         +'</div>'
    );
}