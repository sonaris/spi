//To store current filter selection
var a_selectedSystem = "";
var a_selectedWorkflow = "";
var a_selectedActivity = "";

//To store current result table
var a_TableData = [];

//To store current time format
var a_timeFormat = "Minutes";

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

    var query = config_queries_getAllMetricsActivity;
    var filter = getActivityFilter();

    query = query.replace('$$filter$$', filter).replace('$$filter$$', filter).replace('$$filter$$', filter);

    executeQueryAction(query, fillActivityResultsTable);
}

function fillActivityResultsTable(data)
{
    if (data != null)
        a_TableData = data;

    //Create column data
    var tColumns = generateActivityStatisticsColumnData();

    //Create table data
    var tData = generateActivityStatisticsTableData(a_TableData);

    //updateColumnHeaders(tColumns);

    $('#activityStatisticsResult').DataTable({
        destroy: true,
        data: tData,
        columns: tColumns
    });
}

function generateActivityStatisticsColumnData()
{
    var columns = [];

    return [{"data": "System"}, {"data": "Workflow"}, {"data": "Activity"}, {"data": "tTime"}, {"data": "aTime"}, {"data": "sTime"}];
}

function generateActivityStatisticsTableData(result)
{
    var data = [];
    $.each(result.results.bindings, function(i, binding) {

        var tTime_avg = truncateDecimals(convertMilli(binding['tTime_avg'].value, a_timeFormat));
        var tTime_min = truncateDecimals(convertMilli(binding['tTime_min'].value, a_timeFormat));
        var tTime_max = truncateDecimals(convertMilli(binding['tTime_avg'].value, a_timeFormat));

        var tValue = 'Average: ' + tTime_avg + '<br/> ' + 'Minimum: ' + tTime_min + ' <br/> ' + 'Maximum: ' + tTime_max;

        var aTime_avg = truncateDecimals(convertMilli(binding['aTime_avg'].value, a_timeFormat));
        var aTime_min = truncateDecimals(convertMilli(binding['aTime_min'].value, a_timeFormat));
        var aTime_max = truncateDecimals(convertMilli(binding['aTime_avg'].value, a_timeFormat));

        var aValue = 'Average: ' + aTime_avg + ' <br/> ' + 'Minimum: ' + aTime_min + ' <br/> ' + 'Maximum: ' + aTime_max;

        var sTime_avg = truncateDecimals(convertMilli(binding['sTime_avg'].value, a_timeFormat));
        var sTime_min = truncateDecimals(convertMilli(binding['sTime_min'].value, a_timeFormat));
        var sTime_max = truncateDecimals(convertMilli(binding['sTime_avg'].value, a_timeFormat));

        var sValue = 'Average: ' + sTime_avg + ' <br/> ' + 'Minimum: ' + sTime_min + ' <br/> ' + 'Maximum: ' + sTime_max;

        var row = {
            System: binding['sName'].value,
            Workflow: binding['wName'].value,
            Activity: binding['aName'].value,
            tTime: tValue,
            aTime: aValue,
            sTime: sValue
        };

        data.push(row);
    });

    return data;
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