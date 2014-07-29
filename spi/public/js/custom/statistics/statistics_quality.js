//

var gscript = document.createElement('script');
gscript.src = "http://www.google.com/jsapi";
gscript.setAttribute("type", "application/javascript");
gscript.setAttribute("id", "XX-GMPlusGoogle-XX");
document.body.appendChild(gscript);

// event listener setup
gscript.addEventListener("load",
        function changeCB(params) {
            gscript.removeEventListener("load", changeCB);
            google.load("visualization", "1", {packages: ["corechart"], "callback": generateQualityStatistics});
        }
);

function generateQualityStatistics()
{
    executeQueryAction(config_queries_statistics_quality, createQualityChart);
}

function createQualityChart(data)
{
    $("#qualityCharts").replaceWith('<div id="qualityCharts"></div>');

    var tmp = {};
    $.each(data.results.bindings, function(i, row) {
        var system = row['s'].value;
        var systemName = row['sName'].value;
        if (tmp[system] == null)
            tmp[system] = {'name': systemName, 'entries': []};
        var entry = [row['wName'].value, parseInt(row['numberOpened'].value), parseInt(row['numberCompleted'].value)];
        tmp[system].entries.push(entry);
    });



    $.each(tmp, function(i, system) {
        var systemName = system.name;
        var dataArray = [['Workflow', 'Opened', 'Closed']];

        $.each(system.entries, function(i, entry) {
            dataArray.push(entry);
        });

        var data = google.visualization.arrayToDataTable(dataArray);

        var options = {
        };

        var chartName = 'qualityChart_' + systemName;

        $("#qualityCharts").append(
                '  <div  class="panel panel-default">\n'
                + '    <div class="panel-heading">' + systemName + '</div>\n'
                + '    <div class="panel-body">\n'
                + '        <div id="' + chartName + '" style="..."></div>\n'
                + '    </div>\n'
                + ' </div>\n'
                );

        var chart = new google.visualization.BarChart(document.getElementById(chartName));
        chart.draw(data, options);

    });
}





