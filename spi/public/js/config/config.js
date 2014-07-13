//Endpoint URL
var config_endpoint = "/getQueryResult";
var config_endpoint2 = "http://localhost:3031/ds/query";

//Prefixes
var config_prefixes = 
    'PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n'
    +'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n'
    +'PREFIX event: <http://www.spi.com/event.owl#>\n'
    +'PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n'
    +'PREFIX workflow: <http://www.spi.com/workflow.owl#>\n'
    +'PREFIX economicobject: <http://www.spi.com/economicobject.owl#>\n'
    +'PREFIX businesspartner: <http://www.spi.com/businesspartner.owl#>\n'
    +'PREFIX system: <http://www.spi.com/system.owl#>\n'
    +'PREFIX afn: <http://jena.hpl.hp.com/ARQ/function#>';


//Queries


var config_queries_summary = 
    'SELECT ?name ?number\n'
    +'WHERE\n'
    +'{\n'
    +' {\n'
    +'  #Number of different WFM Systems\n'
    +'  SELECT (str("WFM Systems") as ?name) (COUNT(?wdo) As ?number)\n'
    +'  WHERE {?wdo a system:WFMSystem.}\n'
    +' }\n'
    +' UNION {\n'
    +'  #Number of Workflow Definitions\n'
    +'  SELECT (str("Workflow Definitions") as ?name) (COUNT(?wdo) As ?number)\n'
    +'  WHERE {?wdo a workflow:Workflow.}\n'
    +' }\n'
    +' UNION {\n'
    +'  #Number of Workflow Instances\n'
    +'  SELECT (str("Workflow Instances") as ?name) (COUNT(?wio) As ?number)\n'
    +'  WHERE {?wio a event:WorkflowInstance.}\n'
    +' }\n'
    +' UNION {\n'
    +'  #Number of Activity Definitions\n'
    +'  SELECT (str("Activity Definitions") as ?name) (COUNT(?ado) As ?number)\n'
    +'  WHERE {?ado a workflow:Activity.}\n'
    +' }\n'
    +' UNION {\n'
    +'  #Number of Activity Instances\n'
    +'  SELECT (str("Activity Instances") as ?name) (COUNT(?aio) As ?number)\n'
    +'  WHERE {?aio a event:ActivityInstance.}\n'
    +' }\n'
    +' UNION {\n'
    +'  #Number of Events\n'
    +'  SELECT (str("Events") as ?name) (COUNT(?events) As ?number)\n'
    +'  WHERE {?events a event:Event.}\n'
    +' }\n'
    +'}';
    
var config_queries_workflowsPerSystem = 
'SELECT ?sName (Count (?w) as ?number)\n'
+'WHERE\n'
+'{SELECT DISTINCT ?sName ?w\n'
+'WHERE {\n'
+'  ?s system:hasName ?sName.\n'
+'  OPTIONAL{\n'
+'  ?wi event:generatedBySystem ?s.\n'
+'  ?wi event:definedByWorkflow ?w.\n'
+'  }\n'
+'}\n'
+'}\n'
+'GROUP BY ?sName\n'
+'ORDER BY ?sName\n';

var config_queries_activitiesPerSystem = 
'SELECT ?sName (Count (?a) as ?number)\n'
+'WHERE\n'
+'{SELECT DISTINCT ?sName ?a\n'
+'WHERE {\n'
+'  ?s system:hasName ?sName.\n'
+'  OPTIONAL{\n'
+'  ?wi event:generatedBySystem ?s.\n'
+'  ?wi event:definedByWorkflow ?w.\n'
+'  ?a workflow:partOfWorkflow ?w.\n'
+'  }\n'
+'}\n'
+'}\n'
+'GROUP BY ?sName\n'
+'ORDER BY ?sName\n';

var config_queries_participantsPerSystem =
'SELECT ?sName (Count (?p) as ?number)\n'
+'WHERE\n'
+'{SELECT DISTINCT ?sName ?p\n'
+'WHERE {\n'
+'  ?s system:hasName ?sName.\n'
+'  OPTIONAL{\n'
+'  ?wi event:generatedBySystem ?s.\n'
+'  ?wi event:definedByWorkflow ?w.\n'
+'  ?a workflow:partOfWorkflow ?w.\n'
+'  ?a workflow:performedByParticipant ?p.\n'
+'  }\n'
+'}\n'
+'}\n'
+'GROUP BY ?sName\n'
+'ORDER BY ?sName\n';

var config_queries_workflowInstancesPerSystem = 
'SELECT  ?sName (COUNT(?wi) as ?number)\n'
+'WHERE {\n'
+'  ?s system:hasName ?sName.\n'
+'  OPTIONAL{\n'
+'  ?wi event:generatedBySystem ?s.\n'
+'  }\n'
+'}\n'
+'GROUP BY ?sName\n'
+'ORDER BY ?sName\n';

var config_queries_activityInstancesPerSystem = 
'SELECT  ?sName (COUNT(?ai) as ?number)\n'
+'WHERE {\n'
+'  ?s system:hasName ?sName.\n'
+'  OPTIONAL{\n'
+'  ?wi event:generatedBySystem ?s.\n'
+'  ?ai event:partOfWorkflowInstance ?wi.\n'
+'  }\n'
+'}\n'
+'GROUP BY ?sName\n'
+'ORDER BY ?sName\n';
    
var config_queries_eventsPerSystem = 
'SELECT ?sName (COUNT(?event) as ?number)\n'
+'WHERE\n'
+'{\n'
+'  ?system system:hasName ?sName.\n'
+'  ?wi event:generatedBySystem ?system.\n'
+'  OPTIONAL{\n'
+'  ?event event:originatedFromActivityInstance ?ai.\n'
+'  ?ai event:partOfWorkflowInstance ?wi.\n'
+'  }\n'
+'}\n'
+'GROUP BY ?sName\n'
+'ORDER BY ?sName\n';
//Statistics************************************

var config_queries_AllSystemNames = 
    'SELECT DISTINCT ?sName\n'
    +'WHERE{\n'
    +'?s system:hasName ?sName.\n'
    +'}'
    +'ORDER BY ?sName\n';

var config_queries_AllWorkflowNames = 
    'SELECT DISTINCT ?wName\n' 
    +'WHERE {\n'
    +'    ?workflow workflow:hasWorkflowName ?wName\n'
    +'}';
    
var config_queries_AllActivityNames = 
    'SELECT DISTINCT ?aName\n'
    +'WHERE {\n'     
    +'  ?activity workflow:hasActivityName ?aName.\n'    
    +'}';
    
var config_bindings_systemname = 'sName';
var config_bindings_workflowname = 'wName';
var config_bindings_activityname = 'aName';

//FILTER (?sName= "Camunda") 
var config_queries_WorkflowsFiltered = 
    'SELECT DISTINCT ?sName ?wName\n'
    +'WHERE {\n'
    +'  ?workflowInstance event:definedByWorkflow ?workflow.\n'
    +'  ?workflowInstance event:generatedBySystem ?system.\n'
    +'  ?system system:hasName ?sName.\n'
    +'  ?workflow workflow:hasWorkflowName ?wName.\n'
    +'  $$filter$$\n'
    +'}\n';

//FILTER ((?sName= "Camunda") && (?wName ="New Order"))
var config_queries_ActivitiesFiltered = 
    'SELECT DISTINCT ?sName ?wName ?aName\n'
    +'WHERE {\n'
    +'  ?workflowInstance event:definedByWorkflow ?workflow.\n'
    +'  ?workflowInstance event:generatedBySystem ?system.\n'
    +'  ?system system:hasName ?sName.\n'
    +'  ?workflow workflow:hasWorkflowName ?wName.\n'
    +'  ?activity workflow:partOfWorkflow ?workflow.\n' 
    +'  ?activity workflow:hasActivityName ?aName.\n' 
    +'  $$filter$$\n'
    +'}\n';
    

var config_queries_getTurnaroundTimeWorkflow = 
'SELECT ?sName ?wName (COUNT(?wi) as ?num_wi) (AVG(?tTime) as ?tTime_avg) (MIN(?tTime) as ?tTime_min) (MAX(?tTime) as ?tTime_max)\n'
+'WHERE{\n'
+'  {\n'
+'    SELECT ?wi ((?end-?start) as ?tTime)\n'
+'    WHERE{\n'
+'        SELECT ?wi (MIN(?tOpen) as ?start) (MAX(?tClosed) as ?end)\n'
+'        WHERE { \n'
+'          ?eOpen event:originatedFromWorkflowInstance ?wi; a event:OpenEvent; event:hasTimeMilli ?tOpen.\n'
+'          ?eClosed event:originatedFromWorkflowInstance ?wi; a event:ClosedEvent; event:hasTimeMilli ?tClosed.\n'
+'        }\n'
+'        GROUP BY ?wi  \n'
+'    } \n'
+'  }\n'
+'  ?wi event:definedByWorkflow ?w.\n'
+'  ?w workflow:hasWorkflowName ?wName.\n'
+'  ?wi event:generatedBySystem ?s.\n'
+'  ?s system:hasName ?sName.\n'
+'}\n'
+'GROUP BY ?sName ?wName\n';

var config_queries_getTurnaroundTimeActivity = 
'SELECT ?sName ?wName ?aName (COUNT(?ai) as ?num_ai) (AVG(?tTime) as ?tTime_avg) (MIN(?tTime) as ?tTime_min) (MAX(?tTime) as ?tTime_max)\n'
+'WHERE{\n'
+'  {\n'
+'    SELECT ?ai ((?end-?start) as ?tTime)\n'
+'    WHERE{\n'
+'        SELECT ?ai (MIN(?tOpen) as ?start) (MAX(?tClosed) as ?end)\n'
+'        WHERE { \n'
+'          ?eOpen event:originatedFromActivityInstance ?ai; a event:OpenEvent; event:hasTimeMilli ?tOpen.\n'
+'          ?eClosed event:originatedFromActivityInstance ?ai; a event:ClosedEvent; event:hasTimeMilli ?tClosed.\n'
+'        }\n'
+'        GROUP BY ?ai  \n'
+'    } \n'
+'  }\n'
+'  ?ai event:definedByActivity ?a.\n'
+'  ?a workflow:hasActivityName ?aName.\n'
+'  ?ai event:partOfWorkflowInstance ?wi.\n'
+'  ?wi event:definedByWorkflow ?w.\n'
+'  ?w workflow:hasWorkflowName ?wName.\n'
+'  ?wi event:generatedBySystem ?s.\n'
+'  ?s system:hasName ?sName.\n'
+'}\n'
+'GROUP BY ?sName ?wName ?aName  \n';
        
var config_queries_getActiveTimeActivity = 
'SELECT ?sName ?wName ?aName (COUNT(?ai) as ?num_ai) (AVG(?aTimeSum) as ?aTime_avg) (MIN(?aTimeSum) as ?aTime_min) (MAX(?aTimeSum) as ?aTime_max)\n'
+'WHERE{\n'
+'  {\n'
+'    SELECT ?ai (SUM(?aTime) as ?aTimeSum)\n'
+'    WHERE{\n'
+'      SELECT ?ai ((?e2Time-?e1Time) as ?aTime)\n'
+'      WHERE{\n'
+'       ?e1 event:originatedFromActivityInstance ?ai; a event:RunningEvent.\n'
+'       ?e2 event:originatedFromActivityInstance ?ai.\n'
+'       ?e2 event:causedBy ?e1.\n'
+'       ?e2 event:hasTimeMilli ?e2Time.\n'
+'       ?e1 event:hasTimeMilli ?e1Time.\n'
+'      }\n'
+'    }\n'
+'    GROUP BY ?ai\n'
+'  }\n'
+'  ?ai event:definedByActivity ?a.\n'
+'  ?a workflow:hasActivityName ?aName.\n'
+'  ?ai event:partOfWorkflowInstance ?wi.\n'
+'  ?wi event:definedByWorkflow ?w.\n'
+'  ?w workflow:hasWorkflowName ?wName.\n'
+'  ?wi event:generatedBySystem ?s.\n'
+'  ?s system:hasName ?sName.\n'
+'}\n'
+'GROUP BY ?sName ?wName ?aName \n';

var config_queries_getSuspendTimeActivity = 
'SELECT ?sName ?wName ?aName (COUNT(?ai) as ?num_ai) (AVG(?sTimeSum) as ?sTime_avg) (MIN(?sTimeSum) as ?sTime_min) (MAX(?sTimeSum) as ?sTime_max)\n'
+'WHERE{\n'
+'  {\n'
+'    SELECT ?ai (SUM(?sTime) as ?sTimeSum)\n'
+'    WHERE{\n'
+'      SELECT ?ai ((?e2Time-?e1Time) as ?sTime)\n'
+'      WHERE{\n'
+'       ?e1 event:originatedFromActivityInstance ?ai; a event:SuspendEvent.\n'
+'       ?e2 event:originatedFromActivityInstance ?ai.\n'
+'       ?e2 event:causedBy ?e1.\n'
+'       ?e2 event:hasTimeMilli ?e2Time.\n'
+'       ?e1 event:hasTimeMilli ?e1Time.\n'
+'      }\n'
+'    }\n'
+'    GROUP BY ?ai\n'
+'  }\n'
+'  ?ai event:definedByActivity ?a.\n'
+'  ?a workflow:hasActivityName ?aName.\n'
+'  ?ai event:partOfWorkflowInstance ?wi.\n'
+'  ?wi event:definedByWorkflow ?w.\n'
+'  ?w workflow:hasWorkflowName ?wName.\n'
+'  ?wi event:generatedBySystem ?s.\n'
+'  ?s system:hasName ?sName.\n'
+'}\n'
+'GROUP BY ?sName ?wName ?aName\n';        


config_queries_getAllMetricsActivity=
'SELECT *\n'
+'WHERE\n'
+'{\n'    
    +'{\n'
        +'SELECT ?sName ?wName ?aName (COUNT(?ai) as ?num_ai1) (AVG(?tTime) as ?tTime_avg) (MIN(?tTime) as ?tTime_min) (MAX(?tTime) as ?tTime_max)\n'
        +'WHERE{\n'
        +'  {\n'
        +'    SELECT ?ai ((?end-?start) as ?tTime)\n'
        +'    WHERE{\n'
        +'        SELECT ?ai (MIN(?tOpen) as ?start) (MAX(?tClosed) as ?end)\n'
        +'        WHERE { \n'
        +'          ?eOpen event:originatedFromActivityInstance ?ai; a event:OpenEvent; event:hasTimeMilli ?tOpen.\n'
        +'          ?eClosed event:originatedFromActivityInstance ?ai; a event:ClosedEvent; event:hasTimeMilli ?tClosed.\n'
        +'        }\n'
        +'        GROUP BY ?ai  \n'
        +'    } \n'
        +'  }\n'
        +'  ?ai event:definedByActivity ?a.\n'
        +'  ?a workflow:hasActivityName ?aName.\n'
        +'  ?ai event:partOfWorkflowInstance ?wi.\n'
        +'  ?wi event:definedByWorkflow ?w.\n'
        +'  ?w workflow:hasWorkflowName ?wName.\n'
        +'  ?wi event:generatedBySystem ?s.\n'
        +'  ?s system:hasName ?sName.\n'
        +'}\n'
        +'GROUP BY ?sName ?wName ?aName  \n'
    +'}\n'
    
    +'.\n'
    
    +'{\n' 
        +'SELECT ?sName ?wName ?aName (COUNT(?ai) as ?num_ai2) (AVG(?aTimeSum) as ?aTime_avg) (MIN(?aTimeSum) as ?aTime_min) (MAX(?aTimeSum) as ?aTime_max)\n'
        +'WHERE{\n'
        +'  {\n'
        +'    SELECT ?ai (SUM(?aTime) as ?aTimeSum)\n'
        +'    WHERE{\n'
        +'      SELECT ?ai ((?e2Time-?e1Time) as ?aTime)\n'
        +'      WHERE{\n'
        +'       ?e1 event:originatedFromActivityInstance ?ai; a event:RunningEvent.\n'
        +'       ?e2 event:originatedFromActivityInstance ?ai.\n'
        +'       ?e2 event:causedBy ?e1.\n'
        +'       ?e2 event:hasTimeMilli ?e2Time.\n'
        +'       ?e1 event:hasTimeMilli ?e1Time.\n'
        +'      }\n'
        +'    }\n'
        +'    GROUP BY ?ai\n'
        +'  }\n'
        +'  ?ai event:definedByActivity ?a.\n'
        +'  ?a workflow:hasActivityName ?aName.\n'
        +'  ?ai event:partOfWorkflowInstance ?wi.\n'
        +'  ?wi event:definedByWorkflow ?w.\n'
        +'  ?w workflow:hasWorkflowName ?wName.\n'
        +'  ?wi event:generatedBySystem ?s.\n'
        +'  ?s system:hasName ?sName.\n'
        +'}\n'
        +'GROUP BY ?sName ?wName ?aName \n'
    +'}\n'
    
    +'.\n'
    
    +'{\n' 
        +'SELECT ?sName ?wName ?aName (COUNT(?ai) as ?num_ai3) (AVG(?sTimeSum) as ?sTime_avg) (MIN(?sTimeSum) as ?sTime_min) (MAX(?sTimeSum) as ?sTime_max)\n'
        +'WHERE{\n'
        +'  {\n'
        +'    SELECT ?ai (SUM(?sTime) as ?sTimeSum)\n'
        +'    WHERE{\n'
        +'      SELECT ?ai ((?e2Time-?e1Time) as ?sTime)\n'
        +'      WHERE{\n'
        +'       ?e1 event:originatedFromActivityInstance ?ai; a event:SuspendEvent.\n'
        +'       ?e2 event:originatedFromActivityInstance ?ai.\n'
        +'       ?e2 event:causedBy ?e1.\n'
        +'       ?e2 event:hasTimeMilli ?e2Time.\n'
        +'       ?e1 event:hasTimeMilli ?e1Time.\n'
        +'      }\n'
        +'    }\n'
        +'    GROUP BY ?ai\n'
        +'  }\n'
        +'  ?ai event:definedByActivity ?a.\n'
        +'  ?a workflow:hasActivityName ?aName.\n'
        +'  ?ai event:partOfWorkflowInstance ?wi.\n'
        +'  ?wi event:definedByWorkflow ?w.\n'
        +'  ?w workflow:hasWorkflowName ?wName.\n'
        +'  ?wi event:generatedBySystem ?s.\n'
        +'  ?s system:hasName ?sName.\n'
        +'}\n'
        +'GROUP BY ?sName ?wName ?aName\n'
    +'}\n'
+'}\n';
