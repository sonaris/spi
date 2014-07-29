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
    getResult(config_queries_summary);
}

function getResult(query)
{
    $.ajax({
        type: "POST",
        dataType: "json",
        url: config_endpoint,
        data: 'query=' + urlencode(config_prefixes + query),
        async: false,
        success: function(data) {
            createChart(data);
        },
        error: function(data) {
            alert("Error: " + data.statusText);
            return [];
        }
    });
}

function createChart(data)
{
    var systems = ['System'];

    var summaries = {};
    var headers = [];

    //prepare summary arrays
    $.each(data.head.vars, function(i, entry) {
        if (entry != 'sName')
        {
            summaries[entry] = [entry];
            headers.push(entry);
        }
    });

    //iterate through each system summary to fill arrays
    $.each(data.results.bindings, function(i, entry) {
        //add new system first
        systems.push(entry['sName'].value);
        //retrieve all summary values and add to respective arrays
        $.each(headers, function(i, header) {
            var value = parseInt(entry[header].value);
            summaries[header].push(value);

        });
    });

    //create data array

    var dataArray = [];
    dataArray.push(systems);

    $.each(summaries, function(i, array) {
            dataArray.push(array);
    });

    var data = google.visualization.arrayToDataTable(dataArray);

    var options = {
        height: 600,
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





