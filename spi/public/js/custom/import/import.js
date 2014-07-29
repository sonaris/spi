jQuery("#action_emptyTripleStore").click(function(e){
    $.ajax({
            type: "POST",
            dataType: "json",
            url: "/emptyTripleStore",
            success: function(data) {
                $.notify("Triple Store emptied", { className: 'success', position: 'bottom right' });
            },
            error: function(data) {
                $.notify("A Problem Occurred During Execution", { className: 'error', position: 'bottom right' });
            }
        });
});

jQuery("#action_reasonOverTripleStore").click(function(e){
    $.ajax({
                type: "POST",
                dataType: "json",
                url: "/reasonOverTripleStore",
                success: function(data) {
                    $.notify("Reasoning Successfully Completed", { className: 'success', position: 'bottom right' });
                },
                error: function(data) {
                    $.notify("A Problem Occurred During Reasoning", { className: 'error', position: 'bottom right' });
                }
            });
});

function importSynthetic()
{
    $.ajax({
                type: "POST",
                dataType: "json",
                url: "/importSynthetic",
                success: function(data) {
                    $.notify("Import Successfully Completed", { className: 'success', position: 'bottom right' });
                },
                error: function(data) {
                    $.notify("A Problem Occurred During Import", { className: 'error', position: 'bottom right' });
                }
            });
}

