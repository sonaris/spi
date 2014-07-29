package logic;

import java.util.HashMap;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import other.Settings;

public class ModelCreator {

    private HashMap<String, Resource> events;
    private HashMap<String, Resource> activityEvents;
    private HashMap<String, Resource> workflowEvents;
    private HashMap<String, Resource> workflowDefinitions;
    private HashMap<String, Resource> activityDefinitions;
    private HashMap<String, Resource> workflowInstances;
    private HashMap<String, Resource> activityInstances;
    private HashMap<String, Resource> participants;
    private HashMap<String, Resource> applications;
    private HashMap<String, Resource> systems;
    private HashMap<String, Resource> transitions;
    private HashMap<String, Resource> businessPartners;
    private HashMap<String, Resource> economicObjects;
    private HashMap<String, Resource> accounts;

    private Model dataModel;
    private OntModel ontModel;

    private String baseURI;

    public ModelCreator(String baseURI) {
        this.events = new HashMap<String, Resource>();
        this.workflowEvents = new HashMap<String, Resource>();
        this.activityEvents = new HashMap<String, Resource>();
        this.workflowDefinitions = new HashMap<String, Resource>();
        this.activityDefinitions = new HashMap<String, Resource>();
        this.workflowInstances = new HashMap<String, Resource>();
        this.activityInstances = new HashMap<String, Resource>();
        this.participants = new HashMap<String, Resource>();
        this.applications = new HashMap<String, Resource>();
        this.systems = new HashMap<String, Resource>();
        this.transitions = new HashMap<String, Resource>();
        this.businessPartners = new HashMap<String, Resource>();
        this.economicObjects = new HashMap<String, Resource>();
        this.accounts = new HashMap<String, Resource>();

        this.ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RULE_INF);
        this.ontModel.read(Settings.eventOntologyFile);

        this.dataModel = ModelFactory.createDefaultModel();
        this.dataModel.setNsPrefix("rdfs", RDFS.getURI());
        this.dataModel.setNsPrefix("event", Settings.eventOntologyFile);
        this.baseURI = baseURI;
    }

    public Model getModel() {
        return this.dataModel;
    }

    public HashMap<String, Resource> getEvents() {
        return events;
    }

    public void setEvents(HashMap<String, Resource> events) {
        this.events = events;
    }

    public HashMap<String, Resource> getWorkflowDefinitions() {
        return workflowDefinitions;
    }

    public void setWorkflowDefinitions(HashMap<String, Resource> workflowDefinitions) {
        this.workflowDefinitions = workflowDefinitions;
    }

    public HashMap<String, Resource> getActivityDefinitions() {
        return activityDefinitions;
    }

    public void setActivityDefinitions(HashMap<String, Resource> activityDefinitions) {
        this.activityDefinitions = activityDefinitions;
    }

    public HashMap<String, Resource> getWorkflowInstances() {
        return workflowInstances;
    }

    public void setWorkflowInstances(HashMap<String, Resource> workflowInstances) {
        this.workflowInstances = workflowInstances;
    }

    public HashMap<String, Resource> getActivityInstances() {
        return activityInstances;
    }

    public void setActivityInstances(HashMap<String, Resource> activityInstances) {
        this.activityInstances = activityInstances;
    }

    public HashMap<String, Resource> getParticipants() {
        return participants;
    }

    public void setParticipants(HashMap<String, Resource> participants) {
        this.participants = participants;
    }

    public HashMap<String, Resource> getApplications() {
        return applications;
    }

    public void setApplications(HashMap<String, Resource> applications) {
        this.applications = applications;
    }

    public HashMap<String, Resource> getSystems() {
        return systems;
    }

    public void setSystems(HashMap<String, Resource> systems) {
        this.systems = systems;
    }

    public HashMap<String, Resource> getTransitions() {
        return transitions;
    }

    public void setTransitions(HashMap<String, Resource> transitions) {
        this.transitions = transitions;
    }
    
    public HashMap<String, Resource> getBusinessPartners() {
        return businessPartners;
    }

    public void setBusinessPartners(HashMap<String, Resource> businessPartners) {
        this.businessPartners = businessPartners;
    }

    public HashMap<String, Resource> getEconomicObjects() {
        return economicObjects;
    }

    public void setEconomicObjects(HashMap<String, Resource> economicObjects) {
        this.economicObjects = economicObjects;
    }

    public HashMap<String, Resource> getActivityEvents() {
        return activityEvents;
    }

    public void setActivityEvents(HashMap<String, Resource> activityEvents) {
        this.activityEvents = activityEvents;
    }

    public HashMap<String, Resource> getWorkflowEvents() {
        return workflowEvents;
    }

    public void setWorkflowEvents(HashMap<String, Resource> workflowEvents) {
        this.workflowEvents = workflowEvents;
    }

    public HashMap<String, Resource> getAccounts() {
        return accounts;
    }

    public void setAccounts(HashMap<String, Resource> accounts) {
        this.accounts = accounts;
    }

    public void createAccount(String id, String name) {
        if (accounts.get(id) == null) {
            // Create Resource
            Resource r = dataModel.createResource(baseURI + "#Account" + id).addProperty(RDF.type, ontModel.getOntClass(Settings.systemOntologyNameSpace + "#Account"));
            r.addProperty(ontModel.getOntProperty(Settings.systemOntologyNameSpace + "#hasAccountUserName"), name);
            // Add to HashMap
            accounts.put(id, r);
        }
    }

    public void createWorkflowDefinition(String id, String name) {
        if (workflowDefinitions.get(id) == null) {
            // Create Resource
            Resource r = dataModel.createResource(baseURI + "#Workflow" + id).addProperty(RDF.type, ontModel.getOntClass(Settings.workflowOntologyNameSpace + "#Workflow"));
            r.addProperty(ontModel.getOntProperty(Settings.workflowOntologyNameSpace + "#hasWorkflowName"), name);
            // Add to HashMap
            workflowDefinitions.put(id, r);
        }
    }

    public void createActivityDefinition(String id, String name, String type) {
        if (activityDefinitions.get(id) == null) {
            // Create Resource
            Resource r = dataModel.createResource(baseURI + "#Activity" + id).addProperty(RDF.type, ontModel.getOntClass(Settings.workflowOntologyNameSpace + "#"+type));
            r.addProperty(ontModel.getOntProperty(Settings.workflowOntologyNameSpace + "#hasActivityName"), name);
            // Add to HashMap
            activityDefinitions.put(id, r);
        }
    }

    public void createApplication(String id, String name) {
        if (applications.get(id) == null) {
            // Create Resource
            Resource r = dataModel.createResource(baseURI + "#Application" + id).addProperty(RDF.type, ontModel.getOntClass(Settings.workflowOntologyNameSpace + "#Application"));
            r.addProperty(ontModel.getOntProperty(Settings.workflowOntologyNameSpace + "#hasApplicationName"), name);
            // Add to HashMap
            applications.put(id, r);
        }
    }
    
    public void createBusinessPartner(String id, String type, String role) {
        if (businessPartners.get(id) == null) {
            // Create Resource
            Resource r = dataModel.createResource(baseURI + "#BusinessPartner" + id)
                    .addProperty(RDF.type, ontModel.getOntClass(Settings.businessPartnerOntologyNameSpace + "#"+type))
                    .addProperty(RDF.type, ontModel.getOntClass(Settings.businessPartnerOntologyNameSpace + "#"+role));
            // Add to HashMap
            businessPartners.put(id, r);
        }
    }
    
    public void connectWorkflowInstanceToBusinessPartner(String wiId, String bpId)
    {
        if (workflowInstances.get(wiId) != null) {
            workflowInstances.get(wiId).addProperty(ontModel.getOntProperty(Settings.eventOntologyNameSpace + "#hasBusinessPartnerContext"), businessPartners.get(bpId));
        }
    }
    
    public void addBusinessPartnerAttribute(String bpId, String valueType, String value) {
        if (businessPartners.get(bpId) != null) {
            // Create Resource
            businessPartners.get(bpId).addLiteral(ontModel.getOntProperty(Settings.businessPartnerOntologyNameSpace + "#has"+valueType), ResourceFactory.createTypedLiteral(value, XSDDatatype.XSDstring)); 
        }
    }

    public void createEconomicObject(String id, String type) {
        if (economicObjects.get(id) == null) {
            // Create Resource
            Resource r = dataModel.createResource(baseURI + "#EconomicObject" + id)
                    .addProperty(RDF.type, ontModel.getOntClass(Settings.economicObjectOntologyNameSpace + "#"+type));
            // Add to HashMap
            economicObjects.put(id, r);
        }
    }

    public void connectWorkflowInstanceToEconomicObject(String wiId, String eoId)
    {
        if (workflowInstances.get(wiId) != null) {
            workflowInstances.get(wiId).addProperty(ontModel.getOntProperty(Settings.eventOntologyNameSpace + "#hasEconomicObjectContext"), economicObjects.get(eoId));
        }
    }

    public void addEconomicObjectAttribute(String eoId, String valueType, String value) {
        if (economicObjects.get(eoId) != null) {
            // Create Resource
            economicObjects.get(eoId).addLiteral(ontModel.getOntProperty(Settings.economicObjectOntologyNameSpace + "#has"+valueType), ResourceFactory.createTypedLiteral(value, XSDDatatype.XSDstring));
        }
    }

    public void createTransition(String id, String aIdSource, String aIdTarget, String condition) {
        if (transitions.get(id) == null) {
            // Create Resource
            Resource r = dataModel.createResource(baseURI + "#Transition" + id).addProperty(RDF.type, ontModel.getOntClass(Settings.workflowOntologyNameSpace + "#Transition"));
            r.addProperty(ontModel.getOntProperty(Settings.workflowOntologyNameSpace + "#hasSourceActivity"), this.activityDefinitions.get(aIdSource));
            r.addProperty(ontModel.getOntProperty(Settings.workflowOntologyNameSpace + "#hasTargetActivity"), this.activityDefinitions.get(aIdTarget));

            //Connect Activities directly
            this.activityDefinitions.get(aIdTarget).addProperty(ontModel.getOntProperty(Settings.workflowOntologyNameSpace + "#precededBy"), this.activityDefinitions.get(aIdSource));

            r.addLiteral(ontModel.getOntProperty(Settings.workflowOntologyNameSpace + "#hasTransitionCondition"), ResourceFactory.createTypedLiteral(condition, XSDDatatype.XSDstring));

            // Add to HashMap
            transitions.put(id, r);
        }
    }

    public void createControlFlow(String cId, String aId, String generalType, String subType) {
        if (this.activityDefinitions.get(aId) == null) {
            // Create Resource

            if (generalType.equals("Join")) {
                Resource r = dataModel.createResource(baseURI + "#JoinControlFlow" + cId).addProperty(RDF.type, ontModel.getOntClass(Settings.workflowOntologyNameSpace + "#JoinControlFlow"));
                r.addLiteral(ontModel.getOntProperty(Settings.workflowOntologyNameSpace + "#hasControlFlowType"), ResourceFactory.createTypedLiteral(subType, XSDDatatype.XSDstring));
                activityDefinitions.get(aId).addProperty(ontModel.getOntProperty(Settings.workflowOntologyNameSpace + "#hasJoin"), this.activityDefinitions.get(r));
            } else {
                Resource r = dataModel.createResource(baseURI + "#SplitControlFlow" + cId).addProperty(RDF.type, ontModel.getOntClass(Settings.workflowOntologyNameSpace + "#SplitControlFlow"));
                r.addLiteral(ontModel.getOntProperty(Settings.workflowOntologyNameSpace + "#hasControlFlowType"), ResourceFactory.createTypedLiteral(subType, XSDDatatype.XSDstring));
                activityDefinitions.get(aId).addProperty(ontModel.getOntProperty(Settings.workflowOntologyNameSpace + "#hasSplit"), this.activityDefinitions.get(r));
            }
        }
    }

    public void connectActivityDefinitionToWorkflow(String adId, String wdId) {
        activityDefinitions.get(adId).addProperty(ontModel.getOntProperty(Settings.workflowOntologyNameSpace + "#partOfWorkflow"), workflowDefinitions.get(wdId));
    }

    public void connectActivityDefinitionToPerformer(String aId, String pId, String apId) {
        if (pId != null) activityDefinitions.get(aId).addProperty(ontModel.getOntProperty(Settings.workflowOntologyNameSpace + "#performedByParticipant"), participants.get(pId));
        if (apId != null) activityDefinitions.get(aId).addProperty(ontModel.getOntProperty(Settings.workflowOntologyNameSpace + "#performedByApplication"), applications.get(apId));
    }

    public void connectEventToPerformer(String eId, String accId, String apId) {
        if (accId != null) events.get(eId).addProperty(ontModel.getOntProperty(Settings.eventOntologyNameSpace + "#producedByAccount"), accounts.get(accId));
        if (apId != null) events.get(eId).addProperty(ontModel.getOntProperty(Settings.eventOntologyNameSpace + "#producedByApplication"), applications.get(apId));
    }

    public void connectAccountToParticipant(String accId, String pId) {
        accounts.get(accId).addProperty(ontModel.getOntProperty(Settings.eventOntologyNameSpace + "#belongsToParticipant"), participants.get(pId));
    }

    public void createWorkflowInstance(String id) {
        if (workflowInstances.get(id) == null) {
            // Create Resource
            Resource r = dataModel.createResource(baseURI + "#WorkflowInstance" + id).addProperty(RDF.type, ontModel.getOntClass(Settings.eventOntologyNameSpace + "#WorkflowInstance"));
            // Add to HashMap
            workflowInstances.put(id, r);
        }
    }

    public void connectWorkflowInstanceToWorkflowDefinition(String wiId, String wdId) {
        workflowInstances.get(wiId).addProperty(ontModel.getOntProperty(Settings.eventOntologyNameSpace + "#definedByWorkflow"), workflowDefinitions.get(wdId));
    }

    public void connectWorkflowInstanceToSystem(String wiId, String systemId) {
        workflowInstances.get(wiId).addProperty(ontModel.getOntProperty(Settings.eventOntologyNameSpace + "#generatedBySystem"), systems.get(systemId));
    }

    public void createActivityInstance(String id) {
        if (activityInstances.get(id) == null) {
            // Create Resource
            Resource r = dataModel.createResource(baseURI + "#ActivityInstance" + id).addProperty(RDF.type, ontModel.getOntClass(Settings.eventOntologyNameSpace + "#ActivityInstance"));
            // Add to HashMap
            activityInstances.put(id, r);
        }

    }

    public void connectActivityInstanceToWorkflowInstance(String aiId, String wiId) {
        activityInstances.get(aiId).addProperty(ontModel.getOntProperty(Settings.eventOntologyNameSpace + "#partOfWorkflowInstance"), workflowInstances.get(wiId));
    }

    public void connectActivityInstanceToActivityDefinition(String aiId, String adId) {
        activityInstances.get(aiId).addProperty(ontModel.getOntProperty(Settings.eventOntologyNameSpace + "#definedByActivity"), activityDefinitions.get(adId));
    }

    public void createEvent(String eventID, String eventType, String time, boolean activityEvent) {
        if (events.get(eventID) == null) {
            //Create Event
            Resource event = dataModel.createResource(baseURI + "#" + eventID);

            switch (eventType) {
                case "Closed":
                    event.addProperty(RDF.type, ontModel.getOntClass(Settings.eventOntologyNameSpace + "#ClosedEvent"));
                    break;
                case "Closed.Cancelled":
                    event.addProperty(RDF.type, ontModel.getOntClass(Settings.eventOntologyNameSpace + "#CancelledEvent"));
                    break;
                case "Closed.Cancelled.Aborted":
                    event.addProperty(RDF.type, ontModel.getOntClass(Settings.eventOntologyNameSpace + "#AbortedEvent"));
                    break;
                case "Closed.Cancelled.Terminated":
                    event.addProperty(RDF.type, ontModel.getOntClass(Settings.eventOntologyNameSpace + "#TerminatedEvent"));
                    break;
                case "Closed.Completed":
                    event.addProperty(RDF.type, ontModel.getOntClass(Settings.eventOntologyNameSpace + "#CompletedEvent"));
                    break;
                case "Closed.Completed.Failed":
                    event.addProperty(RDF.type, ontModel.getOntClass(Settings.eventOntologyNameSpace + "#FailedEvent"));
                    break;
                case "Closed.Completed.Success":
                    event.addProperty(RDF.type, ontModel.getOntClass(Settings.eventOntologyNameSpace + "#SuccessEvent"));
                    break;
                case "Open":
                    event.addProperty(RDF.type, ontModel.getOntClass(Settings.eventOntologyNameSpace + "#OpenEvent"));
                    break;
                case "Open.NotRunning":
                    event.addProperty(RDF.type, ontModel.getOntClass(Settings.eventOntologyNameSpace + "#NotRunningEvent"));
                    break;
                case "Open.NotRunning.Assigned":
                    event.addProperty(RDF.type, ontModel.getOntClass(Settings.eventOntologyNameSpace + "#AssignedEvent"));
                    break;
                case "Open.NotRunning.Ready":
                    event.addProperty(RDF.type, ontModel.getOntClass(Settings.eventOntologyNameSpace + "#ReadyEvent"));
                    break;
                case "Open.NotRunning.Suspended":
                    event.addProperty(RDF.type, ontModel.getOntClass(Settings.eventOntologyNameSpace + "#SuspendedEvent"));
                    break;
                case "Open.Running":
                    event.addProperty(RDF.type, ontModel.getOntClass(Settings.eventOntologyNameSpace + "#RunningEvent"));
                    break;
            }

            event.addLiteral(ontModel.getOntProperty(Settings.eventOntologyNameSpace + "#hasTimeMilli"), ResourceFactory.createTypedLiteral(time, XSDDatatype.XSDlong));

            this.events.put(eventID, event);

            if (activityEvent) this.activityEvents.put(eventID, event);
            else this.workflowEvents.put(eventID, event);
        }
    }

    public void connectEventToActivityInstance(String eId, String aiId) {
        this.events.get(eId).addProperty(ontModel.getOntProperty(Settings.eventOntologyNameSpace + "#originatedFromActivityInstance"), this.activityInstances.get(aiId));
    }

    public void connectEventToWorkflowInstance(String eId, String wiId) {
        this.events.get(eId).addProperty(ontModel.getOntProperty(Settings.eventOntologyNameSpace + "#originatedFromWorkflowInstance"), this.workflowInstances.get(wiId));
    }

    public void connectToPrecedingEvent(String eId1, String eId2) {
        this.events.get(eId1).addProperty(ontModel.getOntProperty(Settings.eventOntologyNameSpace + "#precededBy"), this.events.get(eId2));
        this.events.get(eId1).addProperty(ontModel.getOntProperty(Settings.eventOntologyNameSpace + "#causedBy"), this.events.get(eId2));
    }

    public void createParticipant(String pId, String participantType, String participantName) {
        if (this.participants.get(pId) == null) {
            Resource participant = dataModel.createResource(baseURI + "#Participant" + pId);

            switch (participantType) {
                case "HumanParticipant":
                    participant.addProperty(RDF.type, ontModel.getOntClass(Settings.workflowOntologyNameSpace + "#HumanParticipant"));
                    break;
                case "OrganizationUnitParticipant":
                    participant.addProperty(RDF.type, ontModel.getOntClass(Settings.workflowOntologyNameSpace + "#OrganizationUnitParticipant"));
                    break;
                case "ResourceParticipant":
                    participant.addProperty(RDF.type, ontModel.getOntClass(Settings.workflowOntologyNameSpace + "#ResourceParticipant"));
                    break;
                case "ResourceSetParticipant":
                    participant.addProperty(RDF.type, ontModel.getOntClass(Settings.workflowOntologyNameSpace + "#ResourceSetParticipant"));
                    break;
                case "RoleParticipant":
                    participant.addProperty(RDF.type, ontModel.getOntClass(Settings.workflowOntologyNameSpace + "#RoleParticipant"));
                    break;
                case "SystemParticipant":
                    participant.addProperty(RDF.type, ontModel.getOntClass(Settings.workflowOntologyNameSpace + "#SystemParticipant"));
                    break;
            }

            participant.addLiteral(ontModel.getOntProperty(Settings.workflowOntologyNameSpace + "#hasParticipantName"), ResourceFactory.createTypedLiteral(participantName, XSDDatatype.XSDstring));
            // Add to HashMap
            this.participants.put(pId, participant);
        }
    }

    public void createSystem(String sId, String systemName, String systemType) {
        if (this.systems.get(sId) == null) {
            Resource system = dataModel.createResource(baseURI + "#System" + sId);

            switch (systemType) {
                case "BPMSystem":
                    system.addProperty(RDF.type, ontModel.getOntClass(Settings.systemOntologyNameSpace + "#BPMSystem"));
                    break;
                case "CRMSystem":
                    system.addProperty(RDF.type, ontModel.getOntClass(Settings.systemOntologyNameSpace + "#CRMSystem"));
                    break;

            }

            system.addLiteral(ontModel.getOntProperty(Settings.systemOntologyNameSpace + "#hasSystemName"), ResourceFactory.createTypedLiteral(systemName, XSDDatatype.XSDstring));
            // Add to HashMap
            this.systems.put(sId, system);
        }
    }

}
