package logic;

import com.hp.hpl.jena.rdf.model.Model;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.util.Iterator;

public class Synthetic {

    private String workflowPath;
    private String eventPath;
    private String contextPath;
    private ModelCreator mc;

    // jdbc Connection
    private Connection conn = null;

    public Synthetic(String baseURI, String workflowPath, String eventPath, String contextPath) {
        mc = new ModelCreator(baseURI);
        this.workflowPath = workflowPath;
        this.eventPath = eventPath;
        this.contextPath = contextPath;
    }

    public Model createModel() {

        createWorkflowModel();
        createEvents();
        createContext();

        return mc.getModel();
    }

    private void createWorkflowModel() {

        try {
            FileInputStream file = new FileInputStream(new File(workflowPath));

            //Create Workbook instance holding reference to .xlsx file
            XSSFWorkbook workbook = new XSSFWorkbook(file);

            //Create Workflow Definitions
            XSSFSheet workflowSheet = workbook.getSheetAt(0);

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

            //Create Participants
            XSSFSheet participantSheet = workbook.getSheetAt(1);

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

            //Create Activity Definitions
            XSSFSheet activitySheet = workbook.getSheetAt(2);

            //Iterate through each rows one by one
            Iterator<Row> activityRowIterator = activitySheet.iterator();
            while (activityRowIterator.hasNext()) {
                Row row = activityRowIterator.next();

                if (row.getRowNum() != 0) {
                    String aid = getCellContent(row.getCell(0));
                    String wId = getCellContent(row.getCell(1));
                    String pId = getCellContent(row.getCell(2));
                    String name = getCellContent(row.getCell(3));
                    String atype = getCellContent(row.getCell(4));

                    mc.createActivityDefinition(aid, name, atype);
                    mc.connectActivityDefinition(aid, wId, pId);
                }
            }

            //Create Transitions
            XSSFSheet transitionSheet = workbook.getSheetAt(3);

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
            XSSFSheet controlFlowSheet = workbook.getSheetAt(4);

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

    private void createEvents() {

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
                    String oid = getCellContent(row.getCell(0));
                    String wdId = getCellContent(row.getCell(1));
                    mc.createWorkflowInstance(oid);
                    mc.connectWorkflowInstance(oid, wdId, null);
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
                        mc.connectActivityInstance(aiId, adId, wiId);
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

                    mc.createEvent(eId, state, "Workflow", time);
                    mc.connectEventToWorkflowInstance(eId, wiId);

                    if (wiId.equals(lastWorkflowInstanceId)) {
                        mc.connectToPrecedingEvent(eId, lastWorkflowEventId);
                    }

                    lastWorkflowInstanceId = wiId;
                    lastWorkflowEventId = eId;
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

                    mc.createEvent(eId, state, "Activity", time);
                    mc.connectEventToActivityInstance(eId, aiId);

                    if (aiId.equals(lastActivityInstanceId)) {
                        mc.connectToPrecedingEvent(eId, lastActivityEventId);
                    }

                    lastActivityInstanceId = aiId;
                    lastActivityEventId = eId;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createContext() {

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
                    String wiId = getCellContent(row.getCell(0));
                    String bpId = getCellContent(row.getCell(1));
                    String bpType = getCellContent(row.getCell(2));
                    String role = getCellContent(row.getCell(3));
                    String valueType = getCellContent(row.getCell(4));
                    String value = getCellContent(row.getCell(5));

                    if (!bpId.equals("")) {
                        if (mc.getBusinessPartners().get(bpId) == null) {
                            mc.createBusinessPartner(bpId, bpType, role);
                            mc.connectBusinessPartner(bpId, wiId);
                        }

                        mc.addBusinessPartnerAttribute(bpId, valueType, value);
                    }
                }
            }

            //Create System Context
            XSSFSheet systemsSheet = workbook.getSheetAt(2);

            //Iterate through each rows one by one
            Iterator<Row> systemRowIterator = systemsSheet.iterator();
            while (systemRowIterator.hasNext()) {
                Row row = systemRowIterator.next();

                if (row.getRowNum() != 0) {
                    String wiId = getCellContent(row.getCell(0));
                    String sId = getCellContent(row.getCell(1));
                    String sName = getCellContent(row.getCell(2));
                    String sType = getCellContent(row.getCell(3));

                    if (mc.getSystems().get(sId) == null) {
                        mc.createSystem(sId, sType, sName);
                    }

                    if (!wiId.equals("")) {
                        mc.connectWorkflowInstance(wiId, null, sId);
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
                    return String.valueOf((int) cell.getNumericCellValue());
                case Cell.CELL_TYPE_STRING:
                    return cell.getStringCellValue();
            }
        } catch (Exception e) {
            return "";
        }

        return "";

    }

}
