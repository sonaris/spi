var systemData = null;
var eventData = null;
var workflowData = null;
var activityData = null;
var participantData = null;
var workflowInstanceData = null;
var activityInstanceData = null;

var gscript = document.createElement('script');
gscript.src = "http://www.google.com/jsapi";
gscript.setAttribute("type", "application/javascript");
gscript.setAttribute("id", "XX-GMPlusGoogle-XX");
document.body.appendChild(gscript);

// event listener setup
gscript.addEventListener("load",
    function changeCB(params) {
        gscript.removeEventListener("load", changeCB);
        google.load("visualization", "1", {packages:["corechart"], "callback":generateStatistics});
    }
);

function generateStatistics()
{
    getResult(config_queries_AllSystemNames, "S");
}

function getResult(query, type)
{
    $.ajax({
        type: "POST",
        dataType: "json",
        url: config_endpoint,
        data: 'query=' + urlencode(config_prefixes + query),
        async: false,
        success: function(data) {
            if (type === "S")
            {
                systemData = data;
                //Start next Query
                getResult(config_queries_workflowsPerSystem, "WPS");
            }
            else if (type === "WPS")
            {
                workflowData = data;
                //Start next Query
                getResult(config_queries_workflowInstancesPerSystem, "WIPS");
            }
            else if (type === "WIPS")
            {
                workflowInstanceData = data;
                //Start next Query
                getResult(config_queries_activitiesPerSystem, "APS");
            }
            else if (type === "APS")
            {
                activityData = data;
                //Start next Query
                getResult(config_queries_participantsPerSystem, "P");
            }
            else if (type === "P")
            {
                participantData = data;
                //Start next Query
                getResult(config_queries_activityInstancesPerSystem, "AIPS");
            }
            else if (type === "AIPS")
            {
                activityInstanceData = data;
                //Start next Query
                getResult(config_queries_eventsPerSystem, "EPS");
            }
            else if (type === "EPS")
            {
                eventData = data;
                createChart();
            }


        },
        error: function(data) {
            alert("Error: " + data.statusText);
            return [];
        }
    });
}

function createChart()
{
    var header = ['Element'];
    var workflows = ['Workflows'];
    var activities = ['Activities'];
    var participants = ['Participants'];
    var activityInstances = ['Activity Instances'];
    var workflowInstances = ['Workflow Instances'];
    var events = ['Events'];

    header = header.concat(generateSystemsSource(systemData, "sName"));
    workflows = workflows.concat(generateNumbersSource(workflowData, "number"));
    workflowInstances = workflowInstances.concat(generateNumbersSource(workflowInstanceData, "number"));
    activities = activities.concat(generateNumbersSource(activityData, "number"));
    participants = participants.concat(generateNumbersSource(participantData, "number"));
    activityInstances = activityInstances.concat(generateNumbersSource(activityInstanceData, "number"));
    events = events.concat(generateNumbersSource(eventData, "number"));

    var data = google.visualization.arrayToDataTable([
        header,
        workflows,
        workflowInstances,
        activities,
        participants,
        activityInstances,
        events
    ]);

    var options = {
        height: 500,
        legend: {position: 'top', maxLines: 3},
        bar: {groupWidth: '75%'},
        isStacked: false,
    };

    var chart = new google.visualization.BarChart(document.getElementById('stackedChart'));
    chart.draw(data, options);
}

function generateTableOverview(data)
{
    $.each(data.results.bindings, function(index, value) {
        $("#overview").append(
                '<li class="list-group-item"> <span class="badge">' + value.number.value + "</span>" + "Number of " + value.name.value + "</li>");
    });
}

function generateSystemsSource(data, column)
{
    var source = [];
    $.each(data.results.bindings, function(i, binding) {

        for (var key in binding) {
            if (key === column) source.push(binding[key].value);
        }
    });

    return source;
}

function generateNumbersSource(data, column)
{
    var source = [];
    $.each(data.results.bindings, function(i, binding) {

        for (var key in binding) {
            if (key === column) source.push(parseInt(binding[key].value));
        }
    });

    return source;
}





