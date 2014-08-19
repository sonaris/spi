var queryTable;
var availableQueries;
var selectedQuery;

$(document).ready(function() {
    queryTable = $('#sparqlResult').dataTable();
});

function showQueryResult(query)
{
    $.ajax({
        type: "POST",
        dataType: "json",
        url: config_endpoint,
        data: 'query=' + urlencode(query),
        //async: false,
        success: function(data) {
            if (data.answer == null)
            {
                //Check if contains error, if yes display error

                var tData = generateTableData(data);

                //Create column data
                var tColumns = generateColumnData(tData);

                queryTable.fnDestroy(true);
                recreateTable(tColumns);
                //queryTable = $('#sparqlResult').dataTable();

                queryTable = $('#sparqlResult').dataTable({
                     data: tData,
                     columns: tColumns
                });
            }
            else
            {
                alert(data.answer);
            }
        },
        error: function(data) {
            alert("Error: " + data.responseText);
        }
    });
}

function getQueryURI (name)
{
    var uri = "";
    $.each(availableQueries, function(i, query) {
        if (query.name == name)
        {
            uri = query.query;
        }
    });
    return uri;
}

function deleteQuery()
{
    if (selectedQuery != null && selectedQuery != '')
    {
        var query = getQueryURI (selectedQuery);

        $.ajax({
            type: "POST",
            dataType: "json",
            url: "/deleteQuery",
            data: 'queryURI=' +query,
            async: false,
            success: function(data) {
                $.notify("Query Successfully Deleted", { className: 'success', position: 'bottom right' });
                getQueries();
            },
            error: function(data) {
                $.notify("A Problem Occured During Deletion", { className: 'error', position: 'bottom right' });
            }
        });
     }
}

function saveQuery()
{
    if (availableQueries == null ) getQueries();

    var data = {};
    data.query = urlencode(editor.getValue());
    data.name = $("#queryName").val();

    //if name not already existent
    if (data.name == '') $.notify("Please enter a query name!", { className: 'warning', position: 'bottom right' });
    else if (!isNewName(data.name)) $.notify("The query name already exists!", { className: 'warning', position: 'bottom right' });
    else
    {
        $.ajax({
            type: "POST",
            dataType: "json",
            url: "/saveQuery",
            data: data,
            async: false,
            success: function(data) {
                $.notify("Query successfully saved!", { className: 'success', position: 'bottom right' });


            },
            error: function(data) {
                alert("Error: " + data.statusText);
                return [];
            }
        });
    }
}

function isNewName(name)
{
    var result = true;
    $.each(availableQueries, function(i, entry) {
         if (entry.name == name)
         {
            result =  false;
         }
    });

    return result;
}

function getQueries()
{
    var data = {};
    $("#queryPreview").val("");

    $.ajax({
        type: "POST",
        dataType: "json",
        url: "/getQueries",
        data: 'query=' + urlencode(config_prefixes + config_queries_getSavedQueries),
        async: false,
        success: function(result) {

        var data = generateTableData(result);
        //remove URL encoding
        $.each(data, function(i, entry) {
            entry.content = urldecode(entry.content);
        });

        availableQueries = data;

                            var source =
                            {
                                datatype: "json",
                                datafields: [
                                    { name: 'name' },
                                    { name: 'content' }
                                ],
                                localdata: data
                            };

                            var dataAdapter = new $.jqx.dataAdapter(source);
                            // Create a jqxListBox
                            $("#queries").jqxListBox({ source: dataAdapter, displayMember: "name", valueMember: "content", width: 200, height: 250, theme: '' });
                            $("#queries").bind('select', function (event) {
                                if (event.args) {
                                    var item = event.args.item;
                                    if (item) {
                                        $("#queryPreview").val(item.value);
                                        selectedQuery = item.label;
                                    }
                                }
                            });



        },
        error: function(data) {
            alert("Error: " + data.statusText);
            return [];
        }
    });
}

function insertQuery()
{
    var value = $("#queryPreview").val();
    editor.setValue(value);
}


function recreateTable(tColumns)
{
    var header = '';

    $.each(tColumns, function(index, value) {
        header += '<th>' + '?'+value.data + '</th>';
    });



    $("#queryResult").replaceWith
    (
         '<div id="queryResult">'
         +'<table id="sparqlResult" class="table table-striped table-bordered" cellspacing="0" width="100%">'
            +'<thead><tr>' + header + '</tr></thead>'
         +'</table>'
         +'</div>'
    );
}


