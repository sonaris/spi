function getExport()
{
    $.ajax({
        type: "POST",
        dataType: "text",
        url: "/getExport",
        data: 'format='+getFormat(),
        //async: false,
        success: function(data) {
            document.getElementById("output").value = data;

            $.notify("Export created", { className: 'success', position: 'bottom right' });
        },
        error: function(data) {
            alert("Error: " + data.statusText);
            return [];
        }
    });
}

function getFormat()
{
    if ($( "#exportXML" ).hasClass( "active" )) return "RDF/XML";
    else if ($( "#exportJSON" ).hasClass( "active" )) return "RDF/JSON";
    else if ($( "#exportTurtle" ).hasClass( "active" )) return "Turtle";
}