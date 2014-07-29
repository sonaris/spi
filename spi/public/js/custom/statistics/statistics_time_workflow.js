//To store current filter selection
var w_selectedSystem = "";
var w_selectedWorkflow = "";

//To store current result table
var w_TableData = [];

//To store current time format
var w_timeFormat = "Minutes";

var initializeWorkflowAndActivityFilter = function(data)
{
    workflowAndActivityFilterData = data.results.bindings;
    initializeWorkflowFilter();
    initializeActivityFilter();
}

var initializeWorkflowFilter = function()
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

    workflow_fillSystemNames(generateFilterSource(workflowAndActivityFilterData, systemParams));
    workflow_fillWorkflowNames(generateFilterSource(workflowAndActivityFilterData, workflowParams));
}

function getWorkflowFilter()
{
    if (((w_selectedSystem === "all") || (w_selectedSystem === "")) && ((w_selectedWorkflow === "all") || (w_selectedWorkflow === "")))
        return '';
    //single filter
    else if (((w_selectedSystem != "all") && (w_selectedSystem != "")) && ((w_selectedWorkflow === "all") || (w_selectedWorkflow === "")))
        return 'FILTER (?s =<' + w_selectedSystem + '>)';
    else if (((w_selectedSystem === "all") || (w_selectedSystem === "")) && ((w_selectedWorkflow != "all") && (w_selectedWorkflow != "")))
        return 'FILTER (?w =<' + w_selectedWorkflow + '>)';

    //two filters
    else if (((w_selectedSystem != "all") || (w_selectedSystem != "")) && ((w_selectedWorkflow != "all") || (w_selectedWorkflow != "")))
        return 'FILTER ((?s= <' + w_selectedSystem + '>) && (?w =<' + w_selectedWorkflow + '>))';
}

function showResultsWorkflow()
{
    var query = config_queries_getTurnaroundTimeWorkflow;

    var filter = getWorkflowFilter();
    query = query.replace('$$filter$$', filter);

    executeQueryAction(query, fillWorkflowResultsTable);
}

var fillWorkflowResultsTable = function(data)
{
    if (data != null)
        w_TableData = data;

    //Create column data
    var tColumns = generateWorkflowStatisticsColumnData();

    //Create table data
    var tData = generateWorkflowStatisticsTableData(w_TableData);

    //updateColumnHeaders(tColumns);

    $('#workflowStatisticsResult').DataTable({
        destroy: true,
        data: tData,
        columns: tColumns
    });
}

function generateWorkflowStatisticsColumnData()
{
    var columns = [];

    return [{"data": "System"}, {"data": "Workflow"}, {"data": "tTime"}];
}

function generateWorkflowStatisticsTableData(result)
{
    var data = [];
    $.each(result.results.bindings, function(i, binding) {

        var tTime_avg;
        var tTime_min;
        var tTime_max;
        try
        {
            tTime_avg = truncateDecimals(convertMilli(binding['tTime_avg'].value, w_timeFormat));
        }
        catch (err) {
            tTime_avg = "not available"
        }

        try
        {
            tTime_min = truncateDecimals(convertMilli(binding['tTime_min'].value, w_timeFormat));
        }
        catch (err) {
            tTime_min = "not available"
        }

        try
        {
            tTime_max = truncateDecimals(convertMilli(binding['tTime_max'].value, w_timeFormat));
        }
        catch (err) {
            tTime_max = "not available"
        }



        var value = 'Average: ' + tTime_avg + ' <br/> ' + 'Minimum: ' + tTime_min + ' <br/> ' + 'Maximum: ' + tTime_max;

        var row = {
            System: binding['sName'].value,
            Workflow: binding['wName'].value,
            tTime: value
        };

        data.push(row);
    });

    return data;
}

function workflow_fillSystemNames(source)
{
    // Create a jqxComboBox
    $("#w_filter_systemnames").jqxComboBox({source: source, selectedIndex: 0, displayMember: 'name', valueMember: 'uri', width: '250px', height: '25px'});
    // bind to 'select' event.
    $('#w_filter_systemnames').bind('select', function(event) {
        var args = event.args;
        var item = $('#w_filter_systemnames').jqxComboBox('getItem', args.index);
        w_selectedSystem = item.value;

        //Filter workflow names
        var workflowParams = {};
        workflowParams.displayMember = ['wName'];
        workflowParams.valueMember = ['w'];
        workflowParams.filter = [{'key': 's', 'value': w_selectedSystem}];

        workflow_fillWorkflowNames(generateFilterSource(workflowAndActivityFilterData, workflowParams));
    });
}



function workflow_fillWorkflowNames(source)
{
    // Create a jqxComboBox
    $("#w_filter_workflownames").jqxComboBox({source: source, selectedIndex: 0, displayMember: 'name', valueMember: 'uri', width: '250px', height: '25px'});
    // bind to 'select' event.
    $('#w_filter_workflownames').bind('select', function(event) {
        var args = event.args;
        var item = $('#w_filter_workflownames').jqxComboBox('getItem', args.index);
        w_selectedWorkflow = item.value;
    });
}




