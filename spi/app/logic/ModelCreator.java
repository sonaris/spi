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

public class ModelCreator {

    private HashMap<String, Resource> events;
    private HashMap<String, Resource> workflowDefinitions;
    private HashMap<String, Resource> activityDefinitions;
    private HashMap<String, Resource> workflowInstances;
    private HashMap<String, Resource> activityInstances;
    private HashMap<String, Resource> participants;
    private HashMap<String, Resource> systems;
    private HashMap<String, Resource> transitions;
    private HashMap<String, Resource> businessPartners;

    private Model dataModel;
    private OntModel ontModel;

    private String baseURI;

    public ModelCreator(String baseURI) {
        this.events = new HashMap<String, Resource>();
        this.workflowDefinitions = new HashMap<String, Resource>();
        this.activityDefinitions = new HashMap<String, Resource>();
        this.workflowInstances = new HashMap<String, Resource>();
        this.activityInstances = new HashMap<String, Resource>();
        this.participants = new HashMap<String, Resource>();
        this.systems = new HashMap<String, Resource>();
        this.transitions = new HashMap<String, Resource>();
        this.businessPartners = new HashMap<String, Resource>();

        this.ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RULE_INF);
        this.ontModel.read(Settings.ontologyFile);

        this.dataModel = ModelFactory.createDefaultModel();
        this.dataModel.setNsPrefix("rdfs", RDFS.getURI());
        this.dataModel.setNsPrefix("event", Settings.ontologyFile);
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
    
    public void connectBusinessPartner(String bpId, String wiId)
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

    public void createTransition(String id, String aIdSource, String aIDTarget, String condition) {
        if (transitions.get(id) == null) {
            // Create Resource
            Resource r = dataModel.createResource(baseURI + "#Transition" + id).addProperty(RDF.type, ontModel.getOntClass(Settings.workflowOntologyNameSpace + "#Transition"));
            r.addProperty(ontModel.getOntProperty(Settings.workflowOntologyNameSpace + "#hasSourceActivity"), this.activityDefinitions.get(aIdSource));
            r.addProperty(ontModel.getOntProperty(Settings.workflowOntologyNameSpace + "#hasTargetActivity"), this.activityDefinitions.get(aIDTarget));

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

    public void connectActivityDefinition(String aId, String wId, String pId) {
        activityDefinitions.get(aId).addProperty(ontModel.getOntProperty(Settings.workflowOntologyNameSpace + "#partOfWorkflow"), workflowDefinitions.get(wId));
        if (pId != null) activityDefinitions.get(aId).addProperty(ontModel.getOntProperty(Settings.workflowOntologyNameSpace + "#performedByParticipant"), participants.get(pId));
    }

    public void createWorkflowInstance(String id) {
        if (workflowInstances.get(id) == null) {
            // Create Resource
            Resource r = dataModel.createResource(baseURI + "#WorkflowInstance" + id).addProperty(RDF.type, ontModel.getOntClass(Settings.eventOntologyNameSpace + "#WorkflowInstance"));
            // Add to HashMap
            workflowInstances.put(id, r);
        }
    }

    public void connectWorkflowInstance(String wiId, String wdId, String systemId) {
        if (wdId != null) workflowInstances.get(wiId).addProperty(ontModel.getOntProperty(Settings.eventOntologyNameSpace + "#definedByWorkflow"), workflowDefinitions.get(wdId));
        if (systemId != null) workflowInstances.get(wiId).addProperty(ontModel.getOntProperty(Settings.eventOntologyNameSpace + "#generatedBySystem"), systems.get(systemId));
    }

    public void createActivityInstance(String id) {
        if (activityInstances.get(id) == null) {
            // Create Resource
            Resource r = dataModel.createResource(baseURI + "#ActivityInstance" + id).addProperty(RDF.type, ontModel.getOntClass(Settings.eventOntologyNameSpace + "#ActivityInstance"));
            // Add to HashMap
            activityInstances.put(id, r);
        }

    }

    public void connectActivityInstance(String aiId, String adId, String wiId) {
        activityInstances.get(aiId).addProperty(ontModel.getOntProperty(Settings.eventOntologyNameSpace + "#partOfWorkflowInstance"), workflowInstances.get(wiId));
        activityInstances.get(aiId).addProperty(ontModel.getOntProperty(Settings.eventOntologyNameSpace + "#definedByActivity"), activityDefinitions.get(adId));
    }

    public void createEvent(String eventID, String eventType, String producer, String time) {
        if (events.get(eventID) == null) {
            //Create Event
            Resource event = dataModel.createResource(baseURI + "#" + producer + "Event" + eventID);

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

            event.addLiteral(ontModel.getOntProperty(Settings.eventOntologyNameSpace + "#hasTimeMilli"), ResourceFactory.createTypedLiteral(time, XSDDatatype.XSDinteger));

            this.events.put(eventID, event);
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

    public void createSystem(String sId, String systemType, String systemName) {
        if (this.systems.get(sId) == null) {
            Resource system = dataModel.createResource(baseURI + "#System" + sId);

            switch (systemType) {
                case "WFMSystem":
                    system.addProperty(RDF.type, ontModel.getOntClass(Settings.systemOntologyNameSpace + "#WFMSystem"));
                    break;
                case "CRMSystem":
                    system.addProperty(RDF.type, ontModel.getOntClass(Settings.systemOntologyNameSpace + "#CRMSystem"));
                    break;

            }

            system.addLiteral(ontModel.getOntProperty(Settings.systemOntologyNameSpace + "#hasName"), ResourceFactory.createTypedLiteral(systemName, XSDDatatype.XSDstring));
            // Add to HashMap
            this.systems.put(sId, system);
        }
    }

}
