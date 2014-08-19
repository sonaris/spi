package logic;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;

public class SyntheticImporter extends Importer{

    private String workflowPath;
    private String eventPath;
    private String contextPath;

    public void setFilePaths (String workflowPath, String eventPath, String contextPath)
    {
        this.workflowPath = workflowPath;
        this.eventPath = eventPath;
        this.contextPath = contextPath;
    }

    protected void createWorkflowModel() {

        try {
            FileInputStream file = new FileInputStream(new File(workflowPath));

            //Create Workbook instance holding reference to .xlsx file
            XSSFWorkbook workbook = new XSSFWorkbook(file);

            //Create Participants
            XSSFSheet participantSheet = workbook.getSheetAt(0);

            //Iterate through each rows one by one
            Iterator<Row> participantRowIterator = participantSheet.iterator();
            while (participantRowIterator.hasNext()) {
                Row row = participantRowIterator.next();

                if (row.getRowNum() != 0) {
                    String pId = getCellContent(row.getCell(0));
                    String name = getCellContent(row.getCell(1));
                    String pType = getCellContent(row.getCell(2));

                    if (pId != "") mc.createParticipant(pId,pType,name);
                }
            }

            //Create Applications
            XSSFSheet applicationSheet = workbook.getSheetAt(1);

            //Iterate through each rows one by one
            Iterator<Row> applicationRowIterator = applicationSheet.iterator();
            while (applicationRowIterator.hasNext()) {
                Row row = applicationRowIterator.next();

                if (row.getRowNum() != 0) {
                    String aId = getCellContent(row.getCell(0));
                    String name = getCellContent(row.getCell(1));

                    if (aId != "") mc.createApplication(aId, name);
                }
            }


            //Create Workflow Definitions
            XSSFSheet workflowSheet = workbook.getSheetAt(2);

            //Iterate through each rows one by one
            Iterator<Row> workflowRowIterator = workflowSheet.iterator();
            while (workflowRowIterator.hasNext()) {
                Row row = workflowRowIterator.next();

                if (row.getRowNum() != 0) {
                    String oid = getCellContent(row.getCell(0));
                    String name = getCellContent(row.getCell(1));
                    mc.createWorkflowDefinition(oid, name);
                }
            }

            //Create Activity Definitions
            XSSFSheet activitySheet = workbook.getSheetAt(3);

            //Iterate through each rows one by one
            Iterator<Row> activityRowIterator = activitySheet.iterator();
            while (activityRowIterator.hasNext()) {
                Row row = activityRowIterator.next();

                if (row.getRowNum() != 0) {
                    String aId = getCellContent(row.getCell(0));
                    String wId = getCellContent(row.getCell(1));
                    String name = getCellContent(row.getCell(2));
                    String atype = getCellContent(row.getCell(3));
                    String pId = getCellContent(row.getCell(4));
                    String appId = getCellContent(row.getCell(5));

                    mc.createActivityDefinition(aId, name, atype);
                    mc.connectActivityDefinitionToWorkflow(aId, wId);

                    if (!aId.equals("")) {
                        if (atype.equals("NoneImplementationActivity"))
                        {
                            mc.connectActivityDefinitionToPerformer(aId, pId, null);
                        }
                        else if (atype.equals("ApplicationImplementationActivity"))
                        {
                            mc.connectActivityDefinitionToPerformer(aId, null, appId);
                        }
                    }
                }
            }

            //Create Transitions
            XSSFSheet transitionSheet = workbook.getSheetAt(4);

            //Iterate through each rows one by one
            Iterator<Row> transitionRowIterator = transitionSheet.iterator();
            while (transitionRowIterator.hasNext()) {
                Row row = transitionRowIterator.next();

                if (row.getRowNum() != 0) {
                    String tid = getCellContent(row.getCell(0));
                    String sourceId = getCellContent(row.getCell(1));
                    String targetId = getCellContent(row.getCell(2));
                    String condition = getCellContent(row.getCell(3));
                    mc.createTransition(tid, sourceId, targetId, condition);
                }
            }

            //Create ControlFlows
            XSSFSheet controlFlowSheet = workbook.getSheetAt(5);

            //Iterate through each rows one by one
            Iterator<Row> controlFlowRowIterator = controlFlowSheet.iterator();
            while (controlFlowRowIterator.hasNext()) {
                Row row = controlFlowRowIterator.next();

                if (row.getRowNum() != 0) {
                    String cid = getCellContent(row.getCell(0));
                    String activityId = getCellContent(row.getCell(1));
                    String generalType = getCellContent(row.getCell(2));
                    String subType = getCellContent(row.getCell(3));
                    mc.createControlFlow(cid, activityId, generalType, subType);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void createEventAndContextData() {

        //Create Context Data
        try {
            FileInputStream file = new FileInputStream(new File(contextPath));

            //Create Workbook instance holding reference to .xlsx file
            XSSFWorkbook workbook = new XSSFWorkbook(file);

            //Create Business Partner Context
            XSSFSheet businessPartnerSheet = workbook.getSheetAt(0);

            //Iterate through each rows one by one
            Iterator<Row> businessPartnerRowIterator = businessPartnerSheet.iterator();
            while (businessPartnerRowIterator.hasNext()) {
                Row row = businessPartnerRowIterator.next();

                if (row.getRowNum() != 0) {
                    String bpId = getCellContent(row.getCell(0));
                    String bpType = getCellContent(row.getCell(1));
                    String role = getCellContent(row.getCell(2));
                    String valueType = getCellContent(row.getCell(3));
                    String value = getCellContent(row.getCell(4));

                    if (!bpId.equals("")) {
                        if (mc.getBusinessPartners().get(bpId) == null) {
                            mc.createBusinessPartner(bpId, bpType, role);
                        }
                        mc.addBusinessPartnerAttribute(bpId, valueType, value);
                    }
                }
            }

            //Create Economic Object Context
            XSSFSheet economicObjectSheet = workbook.getSheetAt(1);

            //Iterate through each rows one by one
            Iterator<Row> economicObjectRowIterator = economicObjectSheet.iterator();
            while (economicObjectRowIterator.hasNext()) {
                Row row = economicObjectRowIterator.next();

                if (row.getRowNum() != 0) {
                    String eoId = getCellContent(row.getCell(0));
                    String eoType = getCellContent(row.getCell(1));
                    String valueType = getCellContent(row.getCell(2));
                    String value = getCellContent(row.getCell(3));

                    if (!eoId.equals("")) {
                        if (mc.getEconomicObjects().get(eoId) == null) {
                            mc.createEconomicObject(eoId, eoType);
                        }
                        mc.addEconomicObjectAttribute(eoId, valueType, value);
                    }
                }
            }

            //Create Account Context
            XSSFSheet accountSheet = workbook.getSheetAt(2);

            //Iterate through each rows one by one
            Iterator<Row> accountRowIterator = accountSheet.iterator();
            while (accountRowIterator.hasNext()) {
                Row row = accountRowIterator.next();

                if (row.getRowNum() != 0) {
                    String accId = getCellContent(row.getCell(0));
                    String pId = getCellContent(row.getCell(1));
                    String userName = getCellContent(row.getCell(2));

                    if (!accId.equals("")) {
                        mc.createAccount(accId, userName);
                        mc.connectAccountToParticipant(accId,pId);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Create Event Data
        try {
            FileInputStream file = new FileInputStream(new File(eventPath));

            //Create Workbook instance holding reference to .xlsx file
            XSSFWorkbook workbook = new XSSFWorkbook(file);

            //Create Workflow Instances
            XSSFSheet workflowInstancesSheet = workbook.getSheetAt(0);

            //Iterate through each rows one by one
            Iterator<Row> workflowInstanceRowIterator = workflowInstancesSheet.iterator();
            while (workflowInstanceRowIterator.hasNext()) {
                Row row = workflowInstanceRowIterator.next();

                if (row.getRowNum() != 0) {
                    String wiId = getCellContent(row.getCell(0));
                    String wdId = getCellContent(row.getCell(1));
                    String bpId = getCellContent(row.getCell(2));
                    String eoId = getCellContent(row.getCell(3));
                    if (!wiId.equals("")) {
                        mc.createWorkflowInstance(wiId);
                        mc.connectWorkflowInstanceToWorkflowDefinition(wiId, wdId);
                        mc.connectWorkflowInstanceToSystem(wiId, "1");

                        //Iterate through all Business Partners if more than one
                        String [] parts = bpId.split(",");
                        for (String current_bpId : parts)
                        {
                            mc.connectWorkflowInstanceToBusinessPartner(wiId, current_bpId);
                        }

                        //Iterate through all Economic Objects if more than one
                        parts = eoId.split(",");
                        for (String current_eoId : parts)
                        {
                            mc.connectWorkflowInstanceToEconomicObject(wiId, current_eoId);
                        }
                    }

                }
            }

            //Create Activity Instances
            XSSFSheet activityInstancesSheet = workbook.getSheetAt(1);

            //Iterate through each rows one by one
            Iterator<Row> activityInstanceRowIterator = activityInstancesSheet.iterator();
            while (activityInstanceRowIterator.hasNext()) {
                Row row = activityInstanceRowIterator.next();

                if (row.getRowNum() != 0) {
                    String aiId = getCellContent(row.getCell(0));
                    String adId = getCellContent(row.getCell(1));
                    String wiId = getCellContent(row.getCell(2));

                    if (!aiId.equals("")) {
                        mc.createActivityInstance(aiId);
                        mc.connectActivityInstanceToWorkflowInstance(aiId, wiId);
                        mc.connectActivityInstanceToActivityDefinition(aiId, adId);
                    }
                }
            }

            //Workflow Events
            XSSFSheet workflowEventsSheet = workbook.getSheetAt(2);

            //Iterate through each rows one by one
            Iterator<Row> workflowEventRowIterator = workflowEventsSheet.iterator();

            String lastWorkflowEventId = "";
            String lastWorkflowInstanceId = "";

            while (workflowEventRowIterator.hasNext()) {
                Row row = workflowEventRowIterator.next();

                if (row.getRowNum() != 0) {
                    String eId = getCellContent(row.getCell(0));
                    String wiId = getCellContent(row.getCell(1));
                    String time = getCellContent(row.getCell(2));
                    String state = getCellContent(row.getCell(3));

                    if (!eId.equals("")) {
                        eId = "WorkflowEvent"+eId;

                        mc.createEvent(eId, state, time, false);
                        mc.connectEventToWorkflowInstance(eId, wiId);

                        if (wiId.equals(lastWorkflowInstanceId)) {
                            mc.connectToPrecedingEvent(eId, lastWorkflowEventId);
                        }

                        lastWorkflowInstanceId = wiId;
                        lastWorkflowEventId = eId;
                    }
                }
            }

            //Activity Events
            XSSFSheet activityEventsSheet = workbook.getSheetAt(3);

            //Iterate through each rows one by one
            Iterator<Row> activityEventRowIterator = activityEventsSheet.iterator();

            String lastActivityEventId = "";
            String lastActivityInstanceId = "";

            while (activityEventRowIterator.hasNext()) {
                Row row = activityEventRowIterator.next();

                if (row.getRowNum() != 0) {
                    String eId = getCellContent(row.getCell(0));
                    String aiId = getCellContent(row.getCell(1));
                    String time = getCellContent(row.getCell(2));
                    String state = getCellContent(row.getCell(3));
                    String accountId = getCellContent(row.getCell(4));
                    String applicationId = getCellContent(row.getCell(5));

                    if (!eId.equals("")) {
                        eId = "ActivityEvent"+eId;

                        mc.createEvent(eId, state, time, true);
                        mc.connectEventToActivityInstance(eId, aiId);

                        if (!accountId.equals("")) mc.connectEventToPerformer(eId, accountId, null);
                        else if (!applicationId.equals("")) mc.connectEventToPerformer(eId, null, applicationId);

                        if (aiId.equals(lastActivityInstanceId)) {
                            mc.connectToPrecedingEvent(eId, lastActivityEventId);
                        }

                        lastActivityInstanceId = aiId;
                        lastActivityEventId = eId;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getCellContent(Cell cell) {
        try {
            switch (cell.getCellType()) {
                case Cell.CELL_TYPE_NUMERIC:
                    return String.valueOf((long) cell.getNumericCellValue());
                case Cell.CELL_TYPE_STRING:
                    return cell.getStringCellValue();
            }
        } catch (Exception e) {
            return "";
        }

        return "";
    }
}
