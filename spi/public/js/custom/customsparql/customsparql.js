var queryTable;

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
        },
        error: function(data) {
            alert("Error: " + data.responseText);
        }
    });
}

function saveQuery()
{
    var data = {};
    data.query = editor.getValue();
    data.name = $("#queryName").val();

    $.ajax({
        type: "POST",
        dataType: "json",
        url: "/saveQuery",
        data: data,
        async: false,
        success: function(data) {
            alert("Query successfully saved!");


        },
        error: function(data) {
            alert("Error: " + data.statusText);
            return [];
        }
    });
}

function getQueries()
{
    var data = {};

    $.ajax({
        type: "POST",
        dataType: "json",
        url: "/getQueries",
        data: data,
        async: false,
        success: function(result) {

        var data = generateTableData(result);
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


