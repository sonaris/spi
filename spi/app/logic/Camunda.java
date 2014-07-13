package logic;

import com.hp.hpl.jena.rdf.model.Model;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.UserTask;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.camunda.bpm.model.xml.type.ModelElementType;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Camunda {

    private String restURL = "http://localhost:8080/engine-rest/";
    private ModelCreator mc;
    private String systemId;
    private int eventId = 0;

    // jdbc Connection
    private Connection conn = null;

    public Camunda(String baseURI, String systemId) {
        mc = new ModelCreator(baseURI);
        this.systemId = systemId;
        mc.createSystem(systemId, "WFMSystem", "Camunda");
    }

    public Model createModel() {

        //Create and connect Workflow Model
        creatWorkflowModel();
        createInstances();
        //createActivityInstances();
        //createTransitions();

        //Create and connect Events
        //createEvents();
        return mc.getModel();
    }

    private void creatWorkflowModel() {

        //Create Workflow Definitions
        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(Request.getResultFromURL(restURL + "process-definition"));
            JSONArray array = (JSONArray) obj;

            for (int i = 0; i < array.size(); i++) {
                JSONObject obj2 = (JSONObject) array.get(i);

                String oid = (String) obj2.get("id");
                String name = (String) obj2.get("name");
                mc.createWorkflowDefinition(oid, name);
            }
        } catch (IOException ex) {
            Logger.getLogger(Camunda.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(Camunda.class.getName()).log(Level.SEVERE, null, ex);
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

                    mc.createActivityDefinition(currentProcessDef + id, name, "Activity");
                    mc.connectActivityDefinition(currentProcessDef + id, currentProcessDef, null);
                }

            } catch (IOException ex) {
                Logger.getLogger(Camunda.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ParseException ex) {
                Logger.getLogger(Camunda.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    private void createInstances() {
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

                mc.createWorkflowInstance(wiId);
                mc.connectWorkflowInstance(wiId, wdId, systemId);
                
                //Create Events
                String startTimeMilli = convertTimeStringToMilli(startTime);
                String endTimeMilli = convertTimeStringToMilli(endTime);

                //Create Start Event
                this.eventId++;
                mc.createEvent(String.valueOf(this.eventId), "Open.Running", "Workflow", startTimeMilli);
                mc.connectEventToWorkflowInstance(String.valueOf(this.eventId), wiId);

                //Create End Event if possible
                if (endTimeMilli != null) {
                    this.eventId++;
                    mc.createEvent(String.valueOf(this.eventId), "Closed.Completed", "Workflow", endTimeMilli);
                    mc.connectEventToWorkflowInstance(String.valueOf(this.eventId), wiId);
                    mc.connectToPrecedingEvent(String.valueOf(this.eventId), String.valueOf(this.eventId - 1));
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Camunda.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(Camunda.class.getName()).log(Level.SEVERE, null, ex);
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
                String assignee = (String) obj2.get("assignee");
                String taskId = (String) obj2.get("taskId");

                String startTime = (String) obj2.get("startTime");
                String endTime = (String) obj2.get("endTime");

                String startTimeMilli = convertTimeStringToMilli(startTime);
                String endTimeMilli = convertTimeStringToMilli(endTime);

                //add user tasks only
                if (taskId != null) {
                    mc.createActivityInstance(aiId);
                    mc.connectActivityInstance(aiId, wdId + adId, wiId);

                    //Create Start Event
                    this.eventId++;
                    mc.createEvent(String.valueOf(this.eventId), "Open.Running", "Activity", startTimeMilli);
                    mc.connectEventToActivityInstance(String.valueOf(this.eventId), aiId);

                    //Create End Event if possible
                    if (endTimeMilli != null) {
                        this.eventId++;
                        mc.createEvent(String.valueOf(this.eventId), "Closed.Completed", "Activity", endTimeMilli);
                        mc.connectEventToActivityInstance(String.valueOf(this.eventId), aiId);
                        mc.connectToPrecedingEvent(String.valueOf(this.eventId), String.valueOf(this.eventId - 1));
                    }
                }

            }
        } catch (IOException ex) {
            Logger.getLogger(Camunda.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(Camunda.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private String convertTimeStringToMilli(String timeString) {
        try {
            String input = "Sat Feb 17 2012";
            Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(timeString);
            long milliseconds = date.getTime();
            return String.valueOf(milliseconds);
        } catch (Exception ex) {
            return null;
        }
    }
}
