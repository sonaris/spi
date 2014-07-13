$(document).ready(function() {
    $('#workflowStatisticsResult').dataTable();
    $('#activityStatisticsResult').dataTable();
});

var selectedSystem = "";
var selectedWorkflow = "";
var selectedWorkflowVersion = "";
var selectedActivity = "";

var tableData = [];

var timeFormat = "min";

function getSystemNames()
{
    getDetails(config_queries_AllSystemNames, "SN");
}

function getWorkflowNames()
{
    var query = config_queries_WorkflowsFiltered.replace('$$filter$$', getWorkflowFilter());
    getDetails(query, "WN");
}

function getActivityNames()
{
    getDetails(config_queries_ActivitiesFiltered.replace('$$filter$$', getActivityFilter()), "AN");
}

function getActivityFilter()
{
    if ((selectedSystem === '' || selectedSystem === "all") && (selectedWorkflow === '' || selectedWorkflow === "all"))
        return '';
    else if (selectedSystem === '' || selectedSystem === "all")
        return 'FILTER (?sName= "' + selectedSystem + '")';
    else if (selectedWorkflow === '' || selectedWorkflow === "all")
        return 'FILTER (?wName= "' + selectedWorkflow + '")';
    else
        return 'FILTER ((?sName= "' + selectedSystem + '") && (?wName ="' + selectedWorkflow + '"))';
}

function getWorkflowFilter()
{
    if (selectedSystem === '' || selectedSystem === "all")
        return '';
    else
        return 'FILTER (?sName= "' + selectedSystem + '")';

}

function initializeCheckboxes()
{
    //$("#checkbox_turnaround").jqxCheckBox({width: 120, height: 25});   
}

function generateSource(data, column)
{
    var source = [];
    source.push("all");
    $.each(data.results.bindings, function(i, binding) {

        for (var key in binding) {
            if (key === column)
                source.push(binding[key].value);
        }
    });

    return source;
}

function getDetails(query, type)
{
    $.ajax({
        type: "POST",
        dataType: "json",
        url: config_endpoint,
        data: 'query=' + urlencode(config_prefixes + query),
        //async: false,
        success: function(data) {
            if (type === "SN")
                fillSystemNames(generateSource(data, config_bindings_systemname));
            if (type === "WN")
                fillWorkflowNames(generateSource(data, config_bindings_workflowname));
            if (type === "AN")
                fillActivityNames(generateSource(data, config_bindings_activityname));

        },
        error: function(data) {
            alert("Error: " + data.statusText);
            return [];
        }
    });
}

function fillSystemNames(source)
{
    // Create a jqxComboBox
    $("#w_filter_systemnames").jqxComboBox({source: source, selectedIndex: 0, width: '200px', height: '25px'});
    // bind to 'select' event.
    $('#w_filter_systemnames').bind('select', function(event) {
        var args = event.args;
        var item = $('#w_filter_systemnames').jqxComboBox('getItem', args.index);
        selectedSystem = item.label;

        //File Activities correspondingly
        getWorkflowNames();
    });

    $("#a_filter_systemnames").jqxComboBox({source: source, selectedIndex: 0, width: '200px', height: '25px'});
    // bind to 'select' event.
    $('#a_filter_systemnames').bind('select', function(event) {
        var args = event.args;
        var item = $('#a_filter_systemnames').jqxComboBox('getItem', args.index);
        selectedSystem = item.label;

        //File Activities correspondingly
        getWorkflowNames();
    });
}

function fillWorkflowNames(source)
{
    // Create a jqxComboBox
    $("#w_filter_workflownames").jqxComboBox({source: source, selectedIndex: 0, width: '200px', height: '25px'});
    // bind to 'select' event.
    $('#w_filter_workflownames').bind('select', function(event) {
        var args = event.args;
        var item = $('#w_filter_workflownames').jqxComboBox('getItem', args.index);
        selectedWorkflow = item.label;
    });

    $("#a_filter_workflownames").jqxComboBox({source: source, selectedIndex: 0, width: '200px', height: '25px'});
    // bind to 'select' event.
    $('#a_filter_workflownames').bind('select', function(event) {
        var args = event.args;
        var item = $('#a_filter_workflownames').jqxComboBox('getItem', args.index);
        selectedWorkflow = item.label;

        getActivityNames();
    });
}

function fillActivityNames(source)
{
    // Create a jqxListBox
    $("#a_filter_activitynames").jqxComboBox({source: source, selectedIndex: 0, width: '200px', height: '25px'});
    // bind to 'select' event.
    $('#a_filter_activitynames').bind('select', function(event) {
        var args = event.args;
        var item = $('#a_filter_activitynames').jqxComboBox('getItem', args.index);
        selectedActivity = item;
    });
}

function showResultsWorkflow()
{
    var query = config_queries_getTurnaroundTimeWorkflow;

    if (selectedWorkflow === '' || selectedWorkflow === "all")
        query = query.replace('$$wNameFilter$$', '?wName').replace('$$sNameFilter$$', '?sName');
    else
        query = query.replace('$$wNameFilter$$', '"' + selectedWorkflow + '"').replace('$$sNameFilter$$', '?sName');

    $.ajax({
        type: "POST",
        dataType: "json",
        url: config_endpoint,
        data: 'query=' + config_prefixes + query,
        //async: false,
        success: function(data) {
            //save result for later processing (e.g. time format change)
            tableData = data;

            //Create column data
            var tColumns = generateWorkflowStatisticsColumnData(tData);

            //Create table data
            var tData = generateWorkflowStatisticsTableData(data);

            //updateColumnHeaders(tColumns);

            $('#workflowStatisticsResult').DataTable({
                destroy: true,
                data: tData,
                columns: tColumns
            });

        },
        error: function(data) {
            alert("Error: " + data.statusText);
        }
    });
}

function showResultsActivity()
{
    var result = {};
    getResultsActivityRecursive(config_queries_getAllMetricsActivity, "allMetrics", result);

    //Create column data
    var tColumns = generateActivityStatisticsColumnData();

    //Create table data
    var tData = generateActivityStatisticsTableData(result);

    //updateColumnHeaders(tColumns);

    $('#activityStatisticsResult').DataTable({
        destroy: true,
        data: tData,
        columns: tColumns
    });

}

function getResultsActivityRecursive(query, type, result)
{
    $.ajax({
        type: "POST",
        dataType: "json",
        url: config_endpoint,
        data: 'query=' + config_prefixes + query,
        async: false,
        success: function(data) {
            if (type === "allMetrics")
            {
                result.allMetrics = data;
            }
        },
        error: function(data) {
            alert("Error: " + data.statusText);
            return {};
        }
    });
}

function generateWorkflowStatisticsColumnData()
{
    var columns = [];

    return [{"data": "System"}, {"data": "Workflow"}, {"data": "tTime"}];
}

function generateActivityStatisticsColumnData()
{
    var columns = [];

    return [{"data": "System"}, {"data": "Workflow"}, {"data": "Activity"}, {"data": "tTime"}, {"data": "aTime"}, {"data": "sTime"}];
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
            tTime_avg = truncateDecimals(convertMilli(binding['tTime_avg'].value, timeFormat));
        }
        catch (err) {tTime_avg = "not available"}
        
        try 
        {
            tTime_min = truncateDecimals(convertMilli(binding['tTime_min'].value, timeFormat));
        }
        catch (err) {tTime_min = "not available"}
        
        try 
        {
            tTime_max = truncateDecimals(convertMilli(binding['tTime_max'].value, timeFormat));
        }
        catch (err) {tTime_max = "not available"}
        
        

        var value = tTime_avg + ' / ' + tTime_min + ' / ' + tTime_max;

        var row = {
            System: binding['sName'].value,
            Workflow: binding['wName'].value,
            tTime: value
        };

        data.push(row);
    });

    return data;
}

function generateActivityStatisticsTableData(result)
{
    var data = [];
    $.each(result.allMetrics.results.bindings, function(i, binding) {

        var tTime_avg = truncateDecimals(convertMilli(binding['tTime_avg'].value, timeFormat));
        var tTime_min = truncateDecimals(convertMilli(binding['tTime_min'].value, timeFormat));
        var tTime_max = truncateDecimals(convertMilli(binding['tTime_avg'].value, timeFormat));
        
        var tValue = tTime_avg + ' / ' + tTime_min + ' / ' + tTime_max;
        
        var aTime_avg = truncateDecimals(convertMilli(binding['tTime_avg'].value, timeFormat));
        var aTime_min = truncateDecimals(convertMilli(binding['tTime_min'].value, timeFormat));
        var aTime_max = truncateDecimals(convertMilli(binding['tTime_avg'].value, timeFormat));
        
        var aValue = aTime_avg + ' / ' + aTime_min + ' / ' + aTime_max;
        
        var sTime_avg = truncateDecimals(convertMilli(binding['tTime_avg'].value, timeFormat));
        var sTime_min = truncateDecimals(convertMilli(binding['tTime_min'].value, timeFormat));
        var sTime_max = truncateDecimals(convertMilli(binding['tTime_avg'].value, timeFormat));

        var sValue = sTime_avg + ' / ' + sTime_min + ' / ' + sTime_max;

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

function generateActivityStatisticsTableData_old(result)
{
    var data = [];
    $.each(result.tt.results.bindings, function(i, binding) {

        var tTime_avg = truncateDecimals(convertMilli(binding['tTime_avg'].value, timeFormat));
        var tTime_min = truncateDecimals(convertMilli(binding['tTime_min'].value, timeFormat));
        var tTime_max = truncateDecimals(convertMilli(binding['tTime_avg'].value, timeFormat));

        var value = tTime_avg + ' / ' + tTime_min + ' / ' + tTime_max;

        var row = {
            System: binding['sName'].value,
            Workflow: binding['wName'].value,
            Activity: binding['aName'].value,
            tTime: value,
            aTime: null,
            sTime: null
        };

        data.push(row);
    });

    return data;
}

function truncateDecimals(number) {
    number = number * 100;
    number_floor = Math[number < 0 ? 'ceil' : 'floor'](number);
    return number_floor / 100;
}
;

function convertMilli(number, format)
{
    try
    {
        if (format === "min")
            return (number / 1000) / 60;
    }
    catch (e)
    {
        return "Not available";
    }


}


function updateColumnHeaders(tColumns)
{
    var header = '';

    $.each(tColumns, function(index, value) {
        header += '<th>' + value.data + '</th>';
    });

    $("#detailsResult tr").replaceWith("<tr>" + header + "</tr>");
}

/* Formatting function for row details - modify as you need */
function createChildRow(d) {
    // `d` is the original data object for the row

    var name = escape(d.name);

    var query = 'PREFIX event: <http://dl.dropboxusercontent.com/u/983997/ontologies/event.owl#> SELECT ?wdo WHERE {?wio event:definedByWorkflowDef ?wdo. ?wdo event:hasName ' + name + '.}';
    var endpoint = "http://localhost:3031/ds/query?query="

    var url = "http://localhost:3031/ds/query?query=PREFIX+event%3A+%3Chttp%3A%2F%2Fdl.dropboxusercontent.com%2Fu%2F983997%2Fontologies%2Fevent.owl%23%3E%0D%0A%0D%0ASELECT+%3Fwdo%0D%0AWHERE+%7B%0D%0A%3Fwio+event%3AdefinedByWorkflowDef+%3Fwdo.%0D%0A%3Fwdo+event%3AhasName+%22New+Order%22.%0D%0A%7D&output=json&stylesheet=";

    var result = '';

    $.ajax({
        dataType: "json",
        url: url,
        async: false,
        success: function(data) {
            result = '<table cellpadding="5" cellspacing="0" border="0" style="padding-left:50px;">';
            $.each(data.results.bindings, function(index, value) {
                result += '<tr> <td>' + value.wdo.value + '</td> </tr>';
            });
            result += '</table>';

        },
        error: function(data) {
            alert("Error!");

        }
    });

    return result;
}

function generateWorkflows()
{
    var url = "http://localhost:3031/ds/query?query=PREFIX+xsd%3A+%3Chttp%3A%2F%2Fwww.w3.org%2F2001%2FXMLSchema%23%3E%0D%0APREFIX+rdfs%3A+%3Chttp%3A%2F%2Fwww.w3.org%2F2000%2F01%2Frdf-schema%23%3E%0D%0APREFIX+event%3A+%3Chttp%3A%2F%2Fdl.dropboxusercontent.com%2Fu%2F983997%2Fontologies%2Fevent.owl%23%3E%0D%0A%0D%0ASELECT+%3Fname+%28Count%28%3Fwdo%29+AS+%3Fversions%29%0D%0AWHERE+%7B%0D%0A%3Fwio+event%3AdefinedByWorkflowDef+%3Fwdo.%0D%0A%3Fwdo+event%3AhasName+%3Fname.%0D%0A%7D%0D%0AGROUP+BY+%3Fname&output=json&stylesheet=";

    $.ajax({
        dataType: "json",
        url: url,
        success: function(result) {
            var table = $('#details').DataTable({
                data: generateWorkflowsData(result),
                columns: [
                    {
                        "class": 'details-control',
                        "orderable": false,
                        "data": null,
                        "defaultContent": ''
                    },
                    {data: 'name'},
                    {data: 'versions'},
                ]
            });

            // Add event listener for opening and closing details
            $('#details tbody').on('click', 'td.details-control', function() {
                var tr = $(this).parents('tr');
                var row = table.row(tr);

                if (row.child.isShown()) {
                    // This row is already open - close it
                    row.child.hide();
                    tr.removeClass('shown');
                }
                else {
                    // Open this row
                    row.child(createChildRow(row.data())).show();
                    tr.addClass('shown');
                }
            });
        },
        error: function(data) {
            alert("Connection to data source failed!");
        }
    });
}


function createTable(data)
{
    $("table").tablecloth({
        theme: "default",
        bordered: true,
        condensed: true,
        striped: true,
        sortable: true,
        clean: true,
        cleanElements: "th td"

    });

    $.each(data.results.bindings, function(index, value) {
        $(".roomsDetails").append(
                "<tr><td>" + value.aio.value + "</td><td>" + value.an.value + "</td><td>" + value.wn.value + "</td><td>" + value.start.value + "</td><td>" + value.end.value + "</td></tr>");
    });
}

function drawChart(data) {
    var chartData = google.visualization.arrayToDataTable(generateChartData(data, "seconds"));

    var options = {
        title: 'Average time between assignment and start of activity',
        vAxis: {title: 'Activity', titleTextStyle: {color: 'red'}}
    };

    var chart = new google.visualization.BarChart(document.getElementById('chart_div'));
    chart.draw(chartData, options);
}

