package logic;

import com.hp.hpl.jena.rdf.model.Model;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.ServiceTask;
import org.camunda.bpm.model.bpmn.instance.UserTask;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.camunda.bpm.model.xml.type.ModelElementType;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import other.Request;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CamundaImporter extends Importer{

    private String restURL;

    // jdbc Connection
    private Connection conn = null;

    public CamundaImporter (String restURL)
    {
        this.restURL = restURL;
    }

    protected void createWorkflowModel()
    {
        //Create Roles
        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(Request.getResultFromURL(restURL + "group"));
            JSONArray array = (JSONArray) obj;

            for (int i = 0; i < array.size(); i++) {
                JSONObject obj2 = (JSONObject) array.get(i);

                String id = (String) obj2.get("id");
                String name = (String) obj2.get("name");
                String type = (String) obj2.get("type");

                mc.createParticipant(id,"RoleParticipant",name);
            }
        } catch (IOException ex) {
            Logger.getLogger(CamundaImporter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(CamundaImporter.class.getName()).log(Level.SEVERE, null, ex);
        }

        //Create Users and assign to Roles
        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(Request.getResultFromURL(restURL + "user"));
            JSONArray array = (JSONArray) obj;

            for (int i = 0; i < array.size(); i++) {
                JSONObject obj2 = (JSONObject) array.get(i);

                String userId = (String) obj2.get("id");
                String firstName = (String) obj2.get("firstName");
                String lastName = (String) obj2.get("lastName");

                mc.createAccount(userId, firstName+" "+lastName);

                JSONParser parserGroup = new JSONParser();
                Object objGroups = parserGroup.parse(Request.getResultFromURL(restURL + "identity/groups?userId=" + userId));
                JSONObject tmp = (JSONObject) objGroups;
                JSONArray arrayGroups = (JSONArray) tmp.get("groups");

                for (int j = 0; j < arrayGroups.size(); j++) {
                    JSONObject objGroup = (JSONObject) arrayGroups.get(j);

                    String groupId = (String) objGroup.get("id");
                    String groupName = (String) objGroup.get("name");

                    //connect to Role identity/groups?userId=john
                    mc.createParticipant(groupId, "RoleParticipant", groupName);
                    mc.connectAccountToParticipant(userId, groupId);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(CamundaImporter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(CamundaImporter.class.getName()).log(Level.SEVERE, null, ex);
        }

        //Create Workflow Definitions
        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(Request.getResultFromURL(restURL + "process-definition?latest=true"));
            JSONArray array = (JSONArray) obj;

            for (int i = 0; i < array.size(); i++) {
                JSONObject obj2 = (JSONObject) array.get(i);

                String oid = (String) obj2.get("id");
                String name = (String) obj2.get("name");
                String key = (String) obj2.get("key");
                if (name != null) mc.createWorkflowDefinition(oid, name);
                else mc.createWorkflowDefinition(oid, key);

            }
        } catch (IOException ex) {
            Logger.getLogger(CamundaImporter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(CamundaImporter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception e){

        }

        //Create rest of the workflow model
        for (String currentProcessDef : this.mc.getWorkflowDefinitions().keySet()) {
            try {
                String currentModel = Request.getResultFromURL(restURL + "process-definition/" + currentProcessDef + "/xml");

                JSONParser parser = new JSONParser();
                Object obj = parser.parse(currentModel);
                JSONObject obj2 = (JSONObject) obj;
                String xml = (String) obj2.get("bpmn20Xml");

                InputStream stream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));

                BpmnModelInstance modelInstance = Bpmn.readModelFromStream(stream);

                // find all elements of the type user task
                ModelElementType taskType = modelInstance.getModel().getType(UserTask.class);
                Collection<ModelElementInstance> taskInstances = modelInstance.getModelElementsByType(taskType);

                for (ModelElementInstance mi : taskInstances) {
                    UserTask t = (UserTask) mi;
                    String id = t.getId();
                    String name = t.getName();
                    String assignee = t.getCamundaAssignee();
                    String groups = t.getCamundaCandidateGroups();
                    String users = t.getCamundaCandidateUsers();

                    String activityId = currentProcessDef + id;

                    mc.createActivityDefinition(activityId, name, "NoneImplementationActivity");
                    mc.connectActivityDefinitionToWorkflow(activityId, currentProcessDef);

                    if (assignee != null)
                    {
                        if (!assignee.contains("${"))
                        {
                            mc.createParticipant(assignee,"HumanParticipant",assignee);
                            mc.connectActivityDefinitionToPerformer(activityId, assignee, null);
                        }
                    }
                    else if (groups != null)
                    {
                        mc.createParticipant(id,"RoleParticipant",groups);
                        mc.connectActivityDefinitionToPerformer(activityId, groups, null);
                    }
                }

                // find all elements of the type service task
                taskType = modelInstance.getModel().getType(ServiceTask.class);
                taskInstances = modelInstance.getModelElementsByType(taskType);

                for (ModelElementInstance mi : taskInstances) {
                    ServiceTask t = (ServiceTask) mi;
                    String id = t.getId();
                    String name = t.getName();
                    String camundaClass = t.getCamundaClass();
                    String camundaDelegateExpression = t.getCamundaDelegateExpression();
                    String camundaExpression = t.getCamundaExpression();

                    String activityId = currentProcessDef + id;

                    mc.createActivityDefinition(activityId, name, "ApplicationImplementationActivity");
                    mc.connectActivityDefinitionToWorkflow(activityId, currentProcessDef);

                    //Create application first (web service offers no separate method)
                    if (camundaClass != null)
                    {
                        String appName = "Class: "+camundaClass;
                        mc.createApplication(appName, appName);
                        //Connect activity to application
                        mc.connectActivityDefinitionToPerformer(activityId, null,appName);
                    }
                    else if (camundaDelegateExpression != null)
                    {
                        String appName = "Delegate Expression: "+camundaDelegateExpression;
                        mc.createApplication(appName, appName);
                        //Connect activity to application
                        mc.connectActivityDefinitionToPerformer(activityId, null,appName);
                    }
                    else if (camundaExpression != null)
                    {
                        String appName = "Expression: "+camundaExpression;
                        mc.createApplication(appName, appName);
                        //Connect activity to application
                        mc.connectActivityDefinitionToPerformer(activityId, null,appName);
                    }

                }

            } catch (IOException ex) {
                Logger.getLogger(CamundaImporter.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ParseException ex) {
                Logger.getLogger(CamundaImporter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    protected void createEventAndContextData()
    {
        createEventData();
        createContextData();
    }

    private void createEventData()
    {
        //Create Workflow Instances
        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(Request.getResultFromURL(restURL + "history/process-instance"));
            JSONArray array = (JSONArray) obj;

            for (int i = 0; i < array.size(); i++) {
                JSONObject obj2 = (JSONObject) array.get(i);

                String wiId = (String) obj2.get("id");
                String wdId = (String) obj2.get("processDefinitionId");
                String startTime = (String) obj2.get("startTime");
                String endTime = (String) obj2.get("endTime");
                String durationMillis = String.valueOf(obj2.get("durationInMillis"));

                if (mc.getWorkflowDefinitions().get(wdId) != null) {
                    mc.createWorkflowInstance(wiId);
                    mc.connectWorkflowInstanceToWorkflowDefinition(wiId, wdId);
                    mc.connectWorkflowInstanceToSystem(wiId, "1");

                    //Create Events
                    long startTimeMilli = convertTimeStringToMilli(startTime);


                    //Create Start Event
                    String eventId1 = String.valueOf(mc.getWorkflowEvents().size() + 1);
                    String workflowEventId1 = "WorkflowEvent" + eventId1;

                    mc.createEvent(workflowEventId1, "Open.Running", String.valueOf(startTimeMilli), false);
                    mc.connectEventToWorkflowInstance(workflowEventId1, wiId);

                    //Create End Event if possible
                    if (endTime != null) {
                        long endTimeMilli = startTimeMilli+Long.parseLong(durationMillis);
                        String eventId2 = String.valueOf(mc.getWorkflowEvents().size() + 1);
                        String workflowEventId2 = "WorkflowEvent" + eventId2;

                        mc.createEvent(workflowEventId2, "Closed.Completed", String.valueOf(endTimeMilli), false);
                        mc.connectEventToWorkflowInstance(workflowEventId2, wiId);
                        mc.connectToPrecedingEvent(workflowEventId2, workflowEventId1);
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(CamundaImporter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(CamundaImporter.class.getName()).log(Level.SEVERE, null, ex);
        }

        //Create Activity Instances
        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(Request.getResultFromURL(restURL + "history/activity-instance"));
            JSONArray array = (JSONArray) obj;

            for (int i = 0; i < array.size(); i++) {
                JSONObject obj2 = (JSONObject) array.get(i);

                String aiId = (String) obj2.get("id");
                String adId = (String) obj2.get("activityId");
                String wdId = (String) obj2.get("processDefinitionId");
                String wiId = (String) obj2.get("processInstanceId");
                String activityType = (String) obj2.get("activityType");
                String assignee = (String) obj2.get("assignee");
                String taskId = (String) obj2.get("taskId");
                String durationMillis = String.valueOf(obj2.get("durationInMillis"));

                String startTime = (String) obj2.get("startTime");
                String endTime = (String) obj2.get("endTime");

                long startTimeMilli = convertTimeStringToMilli(startTime);

                if (mc.getWorkflowInstances().get(wiId) != null && mc.getWorkflowDefinitions().get(wdId) != null) {

                    //add user tasks only
                    if (activityType.equals("userTask") || activityType.equals("serviceTask")) {
                        mc.createActivityInstance(aiId);
                        mc.connectActivityInstanceToWorkflowInstance(aiId, wiId);
                        mc.connectActivityInstanceToActivityDefinition(aiId, wdId + adId);

                        //Create Start Event
                        String eventId1 = String.valueOf(mc.getActivityEvents().size() + 1);
                        String activityEventId1 = "ActivityEvent" + eventId1;
                        mc.createEvent(activityEventId1, "Open.Running", String.valueOf(startTimeMilli), true);
                        mc.connectEventToActivityInstance(activityEventId1, aiId);
                        mc.connectEventToPerformer(activityEventId1, assignee, null);

                        //Create End Event if possible
                        if (endTime != null) {
                            long endTimeMilli = startTimeMilli+Long.parseLong(durationMillis);
                            String eventId2 = String.valueOf(mc.getActivityEvents().size() + 1);
                            String activityEventId2 = "ActivityEvent" + eventId2;

                            mc.createEvent(activityEventId2, "Closed.Completed", String.valueOf(endTimeMilli), true);
                            mc.connectEventToActivityInstance(activityEventId2, aiId);
                            mc.connectEventToPerformer(activityEventId2, assignee, null);
                            mc.connectToPrecedingEvent(activityEventId2, activityEventId1);
                        }

                        if (activityType.equals("userTask") && assignee != null) {
                            mc.connectEventToPerformer(activityEventId1, assignee, null);
                        }

                        //Find Role for user and connect
                        if (activityType.equals("userTask") && assignee != null) {
                            mc.connectEventToPerformer(activityEventId1, assignee, null);


                        }
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(CamundaImporter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(CamundaImporter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void createContextData()
    {
        //for each workflow instance retrieve all variables
        for (String wiId :mc.getWorkflowInstances().keySet()) {
            try {
                JSONParser parser = new JSONParser();
                Object obj = parser.parse(Request.getResultFromURL(restURL + "history/variable-instance?processInstanceId=" + wiId));
                JSONArray array = (JSONArray) obj;

                String bpId = String.valueOf(mc.getBusinessPartners().size() + 1);
                String eoId = String.valueOf(mc.getEconomicObjects().size() + 1);

                for (int i = 0; i < array.size(); i++) {
                    JSONObject obj2 = (JSONObject) array.get(i);

                    String name = (String) obj2.get("name");
                    String value = String.valueOf(obj2.get("value"));

                    //Create new resource and add data
                    if (name.contains("BusinessPartner#")) {
                        String[] parts = name.split("#");

                        String classesString = parts[1];
                        String[] classes = classesString.split("_");
                        String dataProperty = parts[2];
                        //Create new business partner for wiId if not already done (is handled within method)

                        mc.createBusinessPartner(bpId, classes[0], classes[1]);
                        mc.addBusinessPartnerAttribute(bpId, dataProperty, value);
                        mc.connectWorkflowInstanceToBusinessPartner(wiId, bpId);
                    } else if (name.contains("EconomicObject#")) {
                        String[] parts = name.split("#");

                        String classString = parts[1];
                        String dataProperty = parts[2];

                        //Create new economic object for wiId if not already done (is handled within method)
                        mc.createEconomicObject(eoId, classString);
                        mc.addEconomicObjectAttribute(eoId, dataProperty, value);
                        mc.connectWorkflowInstanceToEconomicObject(wiId, eoId);
                    }
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private long convertTimeStringToMilli(String timeString) {
        try {
            String input = "Sat Feb 17 2012";
            Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(timeString);
            return date.getTime();
        } catch (Exception ex) {
            return -1;
        }
    }
}
