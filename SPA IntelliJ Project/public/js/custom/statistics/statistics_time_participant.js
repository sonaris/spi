//To store current filter selection
var p_selectedSystem = "";
var p_selectedRole = "";
var p_selectedUser = "";

//To store current result table
var p_TableData = [];

//To store current time format
var p_timeFormat = "Minutes";

var initializeParticipantFilter = function(data)
{
    participantFilterData = data.results.bindings;

    var systemParams = {};
    systemParams.displayMember = ['sName'];
    systemParams.valueMember = ['s'];
    systemParams.filter = [];

    var roleParams = {};
    roleParams.displayMember = ['pName'];
    roleParams.valueMember = ['p'];
    roleParams.filter = [];

    var userParams = {};
    userParams.displayMember = ['aName'];
    userParams.valueMember = ['a'];
    userParams.filter = [];

    participant_fillSystemNames(generateFilterSource(participantFilterData, systemParams));
    participant_fillRoleNames(generateFilterSource(participantFilterData, roleParams));
    participant_fillUserNames(generateFilterSource(participantFilterData, userParams));
}

function showResultsParticipant()
{
    var query;
    if ($( "#metrics_p_r" ).hasClass( "active" )) query = config_queries_participantStatistics_Running;
    else if ($( "#metrics_p_s" ).hasClass( "active" )) query = config_queries_participantStatistics_Suspend;

    var filter = getParticipantFilter();

    query = query.replace('$$filter$$', filter)

    executeQueryAction(query, createParticipantChart);
}

function getParticipantFilter()
{
    if (((p_selectedSystem === "all") || (p_selectedSystem === "")) && ((p_selectedRole === "all") || (p_selectedRole === "")) && ((p_selectedUser === "all") || (p_selectedUser === "")))
        return '';
    //single filter
    else if (((p_selectedSystem != "all") && (p_selectedSystem != "")) && ((p_selectedRole === "all") || (p_selectedRole === "")) && ((p_selectedUser === "all") || (p_selectedUser === "")))
        return 'FILTER (?s =<' + p_selectedSystem + '>)';
    else if (((p_selectedSystem === "all") || (p_selectedSystem === "")) && ((p_selectedRole != "all") && (p_selectedRole != "")) && ((p_selectedUser === "all") || (p_selectedUser === "")))
        return 'FILTER (?p =<' + p_selectedRole + '>)';
    else if (((p_selectedSystem === "all") || (p_selectedSystem === "")) && ((p_selectedRole === "all") || (p_selectedRole === "")) && ((p_selectedUser != "all") && (p_selectedUser != "")))
        return 'FILTER (?a =<' + p_selectedUser + '>)';

    //two filters
    else if (((p_selectedSystem != "all") || (p_selectedSystem != "")) && ((p_selectedRole != "all") || (p_selectedRole != "")) && ((p_selectedUser === "all") || (p_selectedUser === "")))
        return 'FILTER ((?s= <' + p_selectedSystem + '>) && (?p =<' + p_selectedRole + '>))';
    else if (((p_selectedSystem === "all") || (p_selectedSystem === "")) && ((p_selectedRole != "all") || (p_selectedRole != "")) && ((p_selectedUser != "all") || (p_selectedUser != "")))
        return 'FILTER ((?p= <' + p_selectedRole + '>) && (?a =<' + p_selectedUser + '>))';
    else if (((p_selectedSystem != "all") && (p_selectedSystem != "")) && ((p_selectedRole === "all") || (p_selectedRole === "")) && ((p_selectedUser != "all") && (p_selectedUser != "")))
        return 'FILTER ((?s= <' + p_selectedSystem + '>) && (?a =<' + p_selectedUser + '>))';

    //three filters
    else
        return 'FILTER ((?s= <' + selectedSystem + '") && (?p =<' + selectedWorkflow + '>) && (?a =<' + selectedActivity + '>))';
}

var fillParticipantResultsTable = function(data)
{
    if (data != null)
        p_TableData = data;

    //Create column data
    var tColumns = generateParticipantStatisticsColumnData();

    //Create table data
    var tData = generateParticipantStatisticsTableData(w_TableData);

    //updateColumnHeaders(tColumns);

    $('#workflowStatisticsResult').DataTable({
        destroy: true,
        data: tData,
        columns: tColumns
    });
}

function generateParticipantStatisticsColumnData()
{
    var columns = [];

    return [{"data": "System"}, {"data": "Workflow"}, {"data": "tTime"}];
}

function generateParticipantStatisticsTableData(result)
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
            Workflow: binding['pName'].value,
            tTime: value
        };

        data.push(row);
    });

    return data;
}

function participant_fillSystemNames(source)
{
    $("#p_filter_systemnames").jqxComboBox({source: source, selectedIndex: 0, displayMember: 'name', valueMember: 'uri', width: '250px', height: '25px'});
    // bind to 'select' event.
    $('#p_filter_systemnames').bind('select', function(event) {
        var args = event.args;
        var item = $('#p_filter_systemnames').jqxComboBox('getItem', args.index);
        p_selectedSystem = item.value;

        //Filter Role names
        var roleParams = {};
        roleParams.displayMember = ['pName'];
        roleParams.valueMember = ['p'];
        roleParams.filter = [{'key': 's', 'value': p_selectedSystem}];

        participant_fillRoleNames(generateFilterSource(participantFilterData, roleParams));

        //Filter user names
        var userParams = {};
        userParams.displayMember = ['aName'];
        userParams.valueMember = ['a'];
        userParams.filter = [{'key': 's', 'value': p_selectedSystem}];

        participant_fillUserNames(generateFilterSource(participantFilterData, userParams));
    });
}

function participant_fillRoleNames(source)
{
    $("#p_filter_rolenames").jqxComboBox({source: source, selectedIndex: 0, displayMember: 'name', valueMember: 'uri', width: '250px', height: '25px'});
    // bind to 'select' event.
    $('#p_filter_rolenames').bind('select', function(event) {
        var args = event.args;
        var item = $('#p_filter_rolenames').jqxComboBox('getItem', args.index);
        p_selectedRole = item.value;

        //Filter user names
        var userParams = {};
        userParams.displayMember = ['aName'];
        userParams.valueMember = ['a'];
        userParams.filter = [{'key': 'p', 'value': p_selectedRole}];

        participant_fillUserNames(generateFilterSource(participantFilterData, userParams));
    });
}

function participant_fillUserNames(source)
{
    $("#p_filter_usernames").jqxComboBox({source: source, selectedIndex: 0, displayMember: 'name', valueMember: 'uri', width: '250px', height: '25px'});
    // bind to 'select' event.
    $('#p_filter_usernames').bind('select', function(event) {
        var args = event.args;
        var item = $('#p_filter_usernames').jqxComboBox('getItem', args.index);
        p_selectedRole = item.value;
    });
}


var gscript = document.createElement('script');
gscript.src = "http://www.google.com/jsapi";
gscript.setAttribute("type", "application/javascript");
gscript.setAttribute("id", "XX-GMPlusGoogle-XX");
document.body.appendChild(gscript);

// event listener setup
gscript.addEventListener("load",
        function changeCB(params) {
            gscript.removeEventListener("load", changeCB);
            google.load("visualization", "1", {packages: ["corechart"], "callback": nothing});
        }
);

function nothing(){}

function createParticipantChart(data)
{
    $("#participantCharts").replaceWith('<div id="participantCharts"></div>');

    var tmp = {};
    $.each(data.results.bindings, function(i, row) {
        var user = row['p'].value;
        var userName = row['pName'].value;
        var workflow = row['w'].value;
        var workflopName = row['wName'].value;
        var activityName = row['aName'].value;
        var timeValue = row['timeTotal'].value;
        if (tmp[user] == null)
            tmp[user] = {'name': userName, 'workflows': {}};
        if (tmp[user].workflows[workflow] == null)
            tmp[user].workflows[workflow] = {'name': workflopName, 'entries': []};

        var timeValueUpdated = truncateDecimals(convertMilli(parseInt(timeValue), p_timeFormat));
        var entry = [activityName, timeValueUpdated];
        tmp[user].workflows[workflow].entries.push(entry);
    });



    //create chart for each user
    $.each(tmp, function(i, user) {
        var userNameOriginal = user.name;
        var userName = userNameOriginal.replace('@', '_').replace('.', '_');
        var workflows = user.workflows;

        var panelName = "userCharts_" + userName;

        $("#participantCharts").append(
                '  <div  class="panel panel-default">\n'
                + '    <div class="panel-heading">User: ' + userNameOriginal + '</div>\n'
                + '    <div id="' + panelName + '" class="panel-body">\n'
                + '    </div>\n'
                + ' </div>\n'
                );

        //for each user, create chart for each workflow
        $.each(workflows, function(i, workflow) {
            var workflowName = workflow.name;
            var entries = workflow.entries;

            var chartName = userName + "_workflowChart_" + workflowName;

            $("#" + panelName).append(
                    '<div id="' + chartName + '"></div>'
                    );

            //create data
            var dataArray = [];

            //create header
            var header = ['Activity Name', 'Time'];
            dataArray.push(header);
            $.each(entries, function(i, entry) {
                dataArray.push(entry);
            });

            var data = google.visualization.arrayToDataTable(dataArray);

            var width = $("#" + panelName).width();
            var options = {
                width: 1000,
                title: 'Workflow: '+workflowName,
                chartArea: {width: '70%'}
            };

            var chart = new google.visualization.BarChart(document.getElementById(chartName));
            chart.draw(data, options);
        });
    });
}