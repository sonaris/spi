//To store filter data
var workflowAndActivityFilterData = [];
var participantFilterData = [];
var activityResultTable;


$(document).ready(function() {
    $('#workflowStatisticsResult').dataTable();
    activityResultTable = $('#activityStatisticsResult').dataTable();
    //$('#participantStatisticsResult').dataTable();

    executeQueryAction(config_queries_AllSystemWorkflowActivityNames, initializeWorkflowAndActivityFilter);
    executeQueryAction(config_queries_AllSystemParticipantAccountNames, initializeParticipantFilter);

});