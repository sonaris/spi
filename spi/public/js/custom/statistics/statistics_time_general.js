//To store filter data
var workflowAndActivityFilterData = [];
var participantFilterData = [];


$(document).ready(function() {
    $('#workflowStatisticsResult').dataTable();
    $('#activityStatisticsResult').dataTable();
    //$('#participantStatisticsResult').dataTable();

    executeQueryAction(config_queries_AllSystemWorkflowActivityNames, initializeWorkflowAndActivityFilter);
    executeQueryAction(config_queries_AllSystemParticipantAccountNames, initializeParticipantFilter);

});