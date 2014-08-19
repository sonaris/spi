$(document).ready(function() {
    $('#controlFlowStatisticsResult').dataTable();

    executeQueryAction(config_queries_AllSystemWorkflowActivityNames, initializeWorkflowAndActivityFilter);

});

var workflowAndActivityFilterData = [];

//To store current filter selection
var c_selectedSystem = "";
var c_selectedWorkflow = "";

//To store current result table
var c_TableData = [];

//To store current time format
var c_timeFormat = "Minutes";

var initializeWorkflowAndActivityFilter = function(data)
{
    workflowAndActivityFilterData = data.results.bindings;
    initializeControlFlowFilter();
}

var initializeControlFlowFilter = function()
{
    var systemParams = {};
    systemParams.displayMember = ['sName'];
    systemParams.valueMember = ['s'];
    systemParams.filter = [];

    var workflowParams = {};
    workflowParams.displayMember = ['wName'];
    workflowParams.valueMember = ['w'];
    workflowParams.filter = [];

    controlflow_fillSystemNames(generateFilterSource(workflowAndActivityFilterData, systemParams));
    controlflow_fillWorkflowNames(generateFilterSource(workflowAndActivityFilterData, workflowParams));
}

function getControlFlowFilter()
{
    if (((c_selectedSystem === "all") || (c_selectedSystem === "")) && ((c_selectedWorkflow === "all") || (c_selectedWorkflow === "")))
        return '';
    //single filter
    else if (((c_selectedSystem != "all") && (c_selectedSystem != "")) && ((c_selectedWorkflow === "all") || (c_selectedWorkflow === "")))
        return 'FILTER (?s =<' + a_selectedSystem + '>)';
    else if (((c_selectedSystem === "all") || (c_selectedSystem === "")) && ((c_selectedWorkflow != "all") && (c_selectedWorkflow != "")))
        return 'FILTER (?w =<' + c_selectedWorkflow + '>)';

    //two filters
    else if (((c_selectedSystem != "all") || (c_selectedSystem != "")) && ((c_selectedWorkflow != "all") || (c_selectedWorkflow != "")))
        return 'FILTER ((?s= <' + c_selectedSystem + '>) && (?w =<' + c_selectedWorkflow + '>))';
}

function showResultsControlFlow()
{
    var query = config_queries_controlFlowStatistics;
    var filter = getControlFlowFilter();

    query = query.replace('$$filter$$', filter);

    executeQueryAction(query, fillControlFlowResultsTable);
}

var fillControlFlowResultsTable = function(data)
{
    if (data != null) c_TableData = data;

    //Create column data
    var tColumns = [{"data": "System"}, {"data": "Workflow"}, {"data": "Order"}, {"data": "Number"}];

    //Create table data
    var tData = generateControlFlowTableData(c_TableData);

    //updateColumnHeaders(tColumns);

    $('#controlFlowStatisticsResult').DataTable({
        destroy: true,
        data: tData,
        columns: tColumns
    });
}

function generateControlFlowTableData(result)
{
    var data = [];
    $.each(result.results.bindings, function(i, binding) {

        var row = {
            System: binding['sName'].value,
            Workflow: binding['wName'].value,
            Order: binding['excecutionOrder'].value,
            Number: binding['number'].value,
        };

        data.push(row);
    });

    return data;
}

function controlflow_fillSystemNames(source)
{
    // Create a jqxComboBox
    $("#c_filter_systemnames").jqxComboBox({source: source, selectedIndex: 0, displayMember: 'name', valueMember: 'uri', width: '250px', height: '25px'});
    // bind to 'select' event.
    $('#c_filter_systemnames').bind('select', function(event) {
        var args = event.args;
        var item = $('#c_filter_systemnames').jqxComboBox('getItem', args.index);
        c_selectedSystem = item.value;

        //Filter workflow names
        var workflowParams = {};
        workflowParams.displayMember = ['wName'];
        workflowParams.valueMember = ['w'];
        workflowParams.filter = [{'key': 's', 'value': c_selectedSystem}];

        controlflow_fillWorkflowNames(generateFilterSource(workflowAndActivityFilterData, workflowParams));
    });
}



function controlflow_fillWorkflowNames(source)
{
    // Create a jqxComboBox
    $("#c_filter_workflownames").jqxComboBox({source: source, selectedIndex: 0, displayMember: 'name', valueMember: 'uri', width: '250px', height: '25px'});
    // bind to 'select' event.
    $('#c_filter_workflownames').bind('select', function(event) {
        var args = event.args;
        var item = $('#c_filter_workflownames').jqxComboBox('getItem', args.index);
        c_selectedWorkflow = item.value;
    });
}