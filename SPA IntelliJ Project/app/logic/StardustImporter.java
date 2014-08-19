package logic;

import java.sql.*;


public class StardustImporter extends Importer {

    private String dbURL;
    private Connection conn;
    
    protected void createWorkflowModel()
    {
        //Create Participants
        try {
            Statement stmt = conn.createStatement();
            ResultSet results = stmt.executeQuery("SELECT * FROM " + "PARTICIPANT");

            while (results.next()) {
                String pId = results.getString(1);
                String pName = results.getString(4);
                String pType = results.getString(5);

                mc.createParticipant(pId,convertParticipantType(pType),pName);
            }
            results.close();
            stmt.close();
        } catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }

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
                String aId = results.getString(1);
                String name = results.getString(4);
                String wId = results.getString(5);
                mc.createActivityDefinition(aId, name, "Activity");
                mc.connectActivityDefinitionToWorkflow(aId, wId);
                //mc.connectActivityDefinitionToPerformer(aId, pId, null);
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

    protected void createEventAndContextData() {
        createEventData();
        createContextData();
    }

    private void createEventData() {
        //Create Workflow User
        try {
            Statement stmt = conn.createStatement();
            ResultSet results = stmt.executeQuery("SELECT * FROM " + "WORKFLOWUSER");

            while (results.next()) {
                String uId = results.getString(1);
                String accountName = results.getString(2);
                String firstName = results.getString(3);
                String lastName = results.getString(4);

                mc.createAccount(uId, accountName);
                mc.connectAccountToParticipant(uId, getParticipantId(uId));
            }
            results.close();
            stmt.close();
        } catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }

        //Create Workflow Instances
        try {
            Statement stmt = conn.createStatement();
            ResultSet results = stmt.executeQuery("SELECT * FROM " + "PROCESS_INSTANCE");

            while (results.next()) {
                String wiId = results.getString(1);
                String wdId = results.getString(6);
                mc.createWorkflowInstance(wiId);
                mc.connectWorkflowInstanceToWorkflowDefinition(wiId, wdId);
                mc.connectWorkflowInstanceToSystem(wiId, "1");
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
                String aiId = results.getString(1);
                String adId = results.getString(6);
                String wiId = results.getString(13);


                mc.createActivityInstance(aiId);
                mc.connectActivityInstanceToWorkflowInstance(aiId, wiId);
                mc.connectActivityInstanceToActivityDefinition(aiId, adId);
            }
            results.close();
            stmt.close();
        } catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }

        //Create Workflow Events
        try {
            Statement stmt = conn.createStatement();
            ResultSet results = stmt.executeQuery("SELECT * FROM " + "PROCESS_INSTANCE");

            while (results.next()) {
                String wiId = results.getString(1); 
                String startTimeMilli = results.getString(2);
                String endTimeMilli = results.getString(3);

                //Create Start Event
                String eventId1 = String.valueOf(mc.getWorkflowEvents().size()+1);
                String workflowEventId1 = "WorkflowEvent"+eventId1;
                mc.createEvent(workflowEventId1, "Open.Running", startTimeMilli, false);
                mc.connectEventToWorkflowInstance(workflowEventId1, wiId);

                //Create End Event if possible
                if (!endTimeMilli.equals("0")) {
                    String eventId2 = String.valueOf(mc.getWorkflowEvents().size()+1);
                    String workflowEventId2 = "WorkflowEvent"+eventId2;
                    mc.createEvent(workflowEventId2, "Closed.Completed", endTimeMilli, false);
                    mc.connectEventToWorkflowInstance(workflowEventId2, wiId);
                    mc.connectToPrecedingEvent(workflowEventId2, workflowEventId1);
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
            ResultSet results = stmt.executeQuery("SELECT * FROM " + "ACT_INST_HISTORY");

            String lastEventId = "";
            String lastActivityInstanceId = "";
            String lastState = "";
            String lastPerformerKind = "";
            String lastPerfomer = "";

            while (results.next()) {
                String timeMilli = results.getString(4);
                String state = results.getString(3);
                String activityInstanceId = results.getString(2);
                String performerKind = results.getString(7);
                String performer = results.getString(8);
                String userId = results.getString(14);
                String onBehalfOfKind = results.getString(10);

                String eventId = String.valueOf(mc.getActivityEvents().size()+1);
                String activityEventId = "ActivityEvent"+eventId;

                mc.createEvent(activityEventId, convertEventState2(state, lastState, !activityInstanceId.equals(lastActivityInstanceId)), timeMilli, true);
                mc.connectEventToActivityInstance(activityEventId, activityInstanceId);


                if (activityInstanceId.equals(lastActivityInstanceId)) {
                    mc.connectToPrecedingEvent(activityEventId, lastEventId);
                }

                if (!onBehalfOfKind.equals("0")) mc.connectEventToPerformer(activityEventId, userId, null);

                lastState = state;
                lastActivityInstanceId = activityInstanceId;
                lastEventId = activityEventId;
                lastPerformerKind = performerKind;
            }
            results.close();
            stmt.close();
        } catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }
    }

    private void createContextData() {
        try {
            Statement stmt = conn.createStatement();
            ResultSet results_data_value = stmt.executeQuery("SELECT * FROM " + "DATA_VALUE");

            //for each workflow instance
            while (results_data_value.next()) {
                String dataId = results_data_value.getString(3);
                String dataValue = results_data_value.getString(4);
                String dataType = results_data_value.getString(7);
                String wiId = results_data_value.getString(8);

                //For each data object

                //When structured data
                if (dataType.equals("5"))
                {
                    //Get Class Type
                    stmt = conn.createStatement();
                    ResultSet results_classType = stmt.executeQuery(
                            "SELECT NAME" + "\n"+
                            "FROM DATA" + "\n"+
                            "WHERE OID = "+dataId
                    );

                    String classType = "";
                    while (results_classType.next()) {
                        classType = results_classType.getString(1);
                    }

                    //Create new resource and add data
                    if (classType.contains("BusinessPartner#"))
                    {
                        String[] parts = classType.split("\\#");

                        String classesString = parts[1];
                        String[] classes = classesString.split("_");

                        String bpId = String.valueOf(mc.getBusinessPartners().size() + 1);

                        mc.createBusinessPartner(bpId,classes[0], classes[1]);

                        //add data
                        stmt = conn.createStatement();
                        ResultSet results_data = stmt.executeQuery(
                                "SELECT b.XPATH as name, a.STRING_VALUE, a.NUMBER_VALUE, a.DOUBLE_VALUE"+ "\n"+
                                "FROM STRUCTURED_DATA_VALUE a INNER JOIN STRUCTURED_DATA b ON a.XPATH = b.OID"+ "\n"+
                                "WHERE a.STRING_VALUE != '<null>' AND a.PROCESSINSTANCE = "+wiId+" AND b.DATA = "+dataId+ "\n"+
                                "ORDER BY a.PROCESSINSTANCE, a.OID"
                        );

                        while (results_data.next()) {
                            String valueType = results_data.getString(1);
                            String value = results_data.getString(2);

                            //Catch the case that the value type is not part of the ontology
                            try{
                                mc.addBusinessPartnerAttribute(bpId,valueType, value);
                            }
                            catch(Exception e) {}

                        }
                        results_data.close();

                        mc.connectWorkflowInstanceToBusinessPartner(wiId,bpId);
                    }
                    else if (classType.contains("EconomicObject#"))
                    {
                        String[] parts = classType.split("\\#");

                        String classesString = parts[1];

                        String eoId = String.valueOf(mc.getEconomicObjects().size()+1);
                        mc.createEconomicObject(eoId,classesString);

                        //add data
                        stmt = conn.createStatement();
                        ResultSet results_data = stmt.executeQuery(
                                "SELECT b.XPATH as name, a.STRING_VALUE, a.NUMBER_VALUE, a.DOUBLE_VALUE"+ "\n"+
                                        "FROM STRUCTURED_DATA_VALUE a INNER JOIN STRUCTURED_DATA b ON a.XPATH = b.OID"+ "\n"+
                                        "WHERE a.STRING_VALUE != '<null>' AND a.PROCESSINSTANCE = "+wiId+" AND b.DATA = "+dataId+ "\n"+
                                        "ORDER BY a.PROCESSINSTANCE, a.OID"
                        );

                        while (results_data.next()) {
                            String valueType = results_data.getString(1);
                            String value = results_data.getString(2);

                            //Catch the case that the value type is not part of the ontology
                            try{
                                mc.addEconomicObjectAttribute(eoId, valueType, value);
                            }
                            catch(Exception e) {}
                        }
                        results_data.close();

                        mc.connectWorkflowInstanceToEconomicObject(wiId, eoId);
                    }
                }
                //When primitive data
                //...not implemented yet
            }
            stmt.close();
        } catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }
    }


    //Helper Methods
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

    private String convertEventState2(String currentState, String previousState, boolean newActivityInstance) {
        if (currentState.equals("0")) {
            return "Open.NotRunning.Ready";
        } else if (currentState.equals("1")) {
            return "Open.Running";
        } else if (currentState.equals("2")) {
            return "Closed.Completed";
        }
        else if (currentState.equals("3")) {
            return "-1";
        }
        else if (currentState.equals("4")) {
                return "Open.NotRunning";
        } else if (currentState.equals("5")) {
            if (previousState.equals("1") && !newActivityInstance) {
                return "Open.NotRunning.Suspended";
            } else {
                return "Open.NotRunning.Assigned";
            }
        }
        else if (currentState.equals("6")) {
            return "Closed.Cancelled.Aborted";
        } else if (currentState.equals("7")) {
            return "Open.NotRunning.Ready";
        } else {
            return "-1";
        }
    }

    private String convertParticipantType(String typeStardust) {
        if (typeStardust.equals("0")) {
            return "RoleParticipant";
        } else return "RoleParticipant";

    }

    public void createConnection(String dbURL) {
        this.dbURL = dbURL;

        try {
            Class.forName("org.apache.derby.jdbc.ClientDriver").newInstance();
            //Get a connection
            conn = DriverManager.getConnection(dbURL);
        } catch (Exception except) {
            except.printStackTrace();
        }
    }

    private String getParticipantId(String userId)
    {
        //Get Participant Id
        try {
            Statement stmt = conn.createStatement();
            ResultSet result_participantId = null;

            result_participantId = stmt.executeQuery(
                    "SELECT PARTICIPANT"+ "\n"+
                    "FROM USER_PARTICIPANT"+ "\n"+
                    "WHERE WORKFLOWUSER = "+userId
            );


            String pId = "";
            while (result_participantId.next()) {
                pId = result_participantId.getString(1);
            }

            stmt.close();
            return pId;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getParticipantId_old(String performerId)
    {
        //Get Participant Id
        try {
            Statement stmt = conn.createStatement();
            ResultSet result_participantId = null;

            result_participantId = stmt.executeQuery(
                    "SELECT DISTINCT c.OID as participantId, c.NAME" + "\n"+
                            "FROM WORKFLOWUSER a INNER JOIN USER_PARTICIPANT b ON a.OID = b.WORKFLOWUSER" + "\n"+
                            "INNER JOIN PARTICIPANT c ON b.PARTICIPANT = c.OID" + "\n"+
                            "WHERE a.OID = "+performerId
            );


            String pId = "";
            while (result_participantId.next()) {
                pId = result_participantId.getString(1);
            }

            stmt.close();
            return pId;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void shutdown() {
        try {
            if (conn != null) {
                DriverManager.getConnection(dbURL + ";shutdown=true");
                conn.close();
            }
        } catch (SQLException sqlExcept) {

        }

    }

}
