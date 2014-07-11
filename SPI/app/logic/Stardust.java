package logic;

import com.hp.hpl.jena.rdf.model.Model;

import java.sql.*;


public class Stardust {

    private String dbURL = "jdbc:derby://localhost/carnot;user=carnot;password=ag";
    private ModelCreator mc;
    private String systemId;
    private int workflowEventId = 0;

    // jdbc Connection
    private Connection conn = null;

    public Stardust(String baseURI, String systemId) {
        mc = new ModelCreator(baseURI);
        this.systemId = systemId;
        mc.createSystem(systemId, "WFMSystem", "Stardust");
    }

    public Model createModel() {
        createConnection();

        createWorkflowModel();
        createInstances();

        //Create and connect Events
        createEvents();

	//shutdown();
        return mc.getModel();
    }
    
    private void createWorkflowModel()
    {
        //Create Workflow Definitions
        try {
            Statement stmt = conn.createStatement();
            ResultSet results = stmt.executeQuery("SELECT * FROM " + "PROCESS_DEFINITION");

            while (results.next()) {
                String oid = results.getString(1);
                String name = results.getString(4);
                mc.createWorkflowDefinition(oid, name);
            }
            results.close();
            stmt.close();
        } catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }
        
        //Create Activity Definitions
        try {
            Statement stmt = conn.createStatement();
            ResultSet results = stmt.executeQuery("SELECT * FROM " + "Activity");

            while (results.next()) {
                String oid = results.getString(1);
                String name = results.getString(4);
                String wId = results.getString(5);
                mc.createActivityDefinition(oid, name, "Activity");
                mc.connectActivityDefinition(oid, wId, null);
            }
            results.close();
            stmt.close();
        } catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }
        
        //Create Transitions
        try {
            Statement stmt = conn.createStatement();
            ResultSet results = stmt.executeQuery("SELECT * FROM " + "TRANSITION");

            while (results.next()) {
                String oid = results.getString(1);
                String sourceId = results.getString(5);
                String targetId = results.getString(6);
                String condition = results.getString(7);
                mc.createTransition(oid, sourceId, targetId, condition);
            }
            results.close();
            stmt.close();
        } catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }
    }

    private void createInstances()
    {
        //Create Workflow Instances
        try {
            Statement stmt = conn.createStatement();
            ResultSet results = stmt.executeQuery("SELECT * FROM " + "PROCESS_INSTANCE");

            while (results.next()) {
                String oid = results.getString(1);
                String pdId = results.getString(6);
                mc.createWorkflowInstance(oid);
                mc.connectWorkflowInstance(oid, pdId, systemId);
            }
            results.close();
            stmt.close();
        } catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }
        
        //Create Activity Instances
        try {
            Statement stmt = conn.createStatement();
            ResultSet results = stmt.executeQuery("SELECT * FROM " + "ACTIVITY_INSTANCE");

            while (results.next()) {
                String oid = results.getString(1);
                String adId = results.getString(6);
                String wiId = results.getString(13);
                mc.createActivityInstance(oid);
                mc.connectActivityInstance(oid, adId, wiId);
            }
            results.close();
            stmt.close();
        } catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }
    }

    private void createEvents() {
        
        //Create Workflow Events
        try {
            Statement stmt = conn.createStatement();
            ResultSet results = stmt.executeQuery("SELECT * FROM " + "PROCESS_INSTANCE");

            while (results.next()) {
                String wiId = results.getString(1); 
                String startTimeMilli = results.getString(2);
                String endTimeMilli = results.getString(3);

                //Create Start Event
                this.workflowEventId++;
                mc.createEvent(String.valueOf(this.workflowEventId), "Open.Running", "Workflow", startTimeMilli);
                mc.connectEventToWorkflowInstance(String.valueOf(this.workflowEventId), wiId);

                //Create End Event if possible
                if (!endTimeMilli.equals("0")) {
                    this.workflowEventId++;
                    mc.createEvent(String.valueOf(this.workflowEventId), "Closed.Completed", "Workflow", endTimeMilli);
                    mc.connectEventToWorkflowInstance(String.valueOf(this.workflowEventId), wiId);
                    mc.connectToPrecedingEvent(String.valueOf(this.workflowEventId), String.valueOf(this.workflowEventId - 1));
                }
            }
            results.close();
            stmt.close();
        } catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }
        
        //Create Activity Events
        try {
            Statement stmt = conn.createStatement();
            ResultSet results = stmt.executeQuery("SELECT * FROM " + "ACTIVITY_INST_LOG");

            String lastEventId = "";
            String lastActivityInstanceId = "";
            String lastState = "";

            while (results.next()) {
                String id = results.getString(1);
                String timeMilli = results.getString(3);
                String state = results.getString(2);
                String activityInstanceId = results.getString(7);

                mc.createEvent(id, convertEventState(state, lastState, !activityInstanceId.equals(lastActivityInstanceId)), "Activity", timeMilli);
                mc.connectEventToActivityInstance(id, activityInstanceId);

                if (activityInstanceId.equals(lastActivityInstanceId)) {
                    mc.connectToPrecedingEvent(id, lastEventId);
                }

                lastState = state;
                lastActivityInstanceId = activityInstanceId;
                lastEventId = id;
            }
            results.close();
            stmt.close();
        } catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }
    }

    private String convertEventState(String currentState, String previousState, boolean newActivityInstance) {
        if (currentState.equals("0")) {
            return "Open.NotRunning.Ready";
        } else if (currentState.equals("1")) {
            return "Open.Running";
        } else if (currentState.equals("2")) {
            if (previousState.equals("1") && !newActivityInstance) {
                return "Open.NotRunning.Suspended";
            } else {
                return "Open.NotRunning.Assigned";
            }

        } else if (currentState.equals("3")) {
            return "Closed.Completed.Success";
        } else if (currentState.equals("4")) {
            return "-1";
        } else if (currentState.equals("5")) {
            return "-1";
        } else if (currentState.equals("6")) {
            return "Closed.Cancelled.Aborted";
        } else if (currentState.equals("7")) {
            return "Open.NotRunning.Ready";
        } else {
            return "-1";
        }
    }

    private void createConnection() {
        try {
            Class.forName("org.apache.derby.jdbc.ClientDriver").newInstance();
            //Get a connection
            conn = DriverManager.getConnection(dbURL);
        } catch (Exception except) {
            except.printStackTrace();
        }
    }

    private void shutdown() {
        try {
            if (conn != null) {
                DriverManager.getConnection(dbURL + ";shutdown=true");
                conn.close();
            }
        } catch (SQLException sqlExcept) {

        }

    }

}
