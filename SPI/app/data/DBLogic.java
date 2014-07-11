package data;


import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.reasoner.ValidityReport;
import com.hp.hpl.jena.tdb.TDBFactory;
import logic.Settings;
import org.mindswap.pellet.jena.PelletReasonerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DBLogic {

    public static InfModel createInferredModel(Model model, String ontologyFile) {

        // create inferred model
        OntModel ontModel = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC, model);
        ontModel.read(ontologyFile);
        
        //Model ontology = ModelFactory.createDefaultModel();
        //ontology.read(ontologyFile);
        
        //Reasoner owlReasoner = ReasonerRegistry.getOWLReasoner();
        //Reasoner pelletReasoner = PelletReasonerFactory.theInstance().create();
        //pelletReasoner.bindSchema(ontology);
        
        //InfModel infModel = ModelFactory.createInfModel(pelletReasoner, model);

        // some validation
        ValidityReport vr = ontModel.validate();
        if (vr.isValid() == false) {
            System.out.print("ontology model validation failed.");
            for (Iterator i = vr.getReports(); i.hasNext();) {
                System.out.println("-" + i.next());
            }
            return null;
        }

        return ontModel;
    }

    public static Model getDefaultModel(String db)
    {
        Dataset dataset = TDBFactory.createDataset(db);
        dataset.begin(ReadWrite.READ);
        Model model = dataset.getDefaultModel();
        dataset.end();
        return model;
    }

    public static Model createModelFromFiles(String[] files) {
        Model model = ModelFactory.createDefaultModel();

        //fill with data from files
        for (String f : files) {
            model.read(f);
        }

        return model;

    }
    
    public static void reasonOverModel(String db)
    {
        Dataset dataset = TDBFactory.createDataset(db);
        dataset.begin(ReadWrite.WRITE);
        Model modelStandard = dataset.getDefaultModel();
        dataset.end();
        try
        {
            InfModel inferredModel = DBLogic.createInferredModel(modelStandard, Settings.ontologyFile);

            dataset.begin(ReadWrite.WRITE);
            removeData(dataset);
            Model existing = dataset.getDefaultModel();
            existing.add(inferredModel);
            dataset.commit();
            dataset.end();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    public static void emptyTripleStore(String name) {
        Dataset dataset = TDBFactory.createDataset(name);
        //Remove existing data first

        dataset.begin(ReadWrite.WRITE);
        removeData(dataset);
        dataset.commit();
        dataset.end();
    }

    public static Dataset saveModelToTripleStore(String dbString, Model newModel) {
        //Make a TDB-backed dataset
        Dataset dataset = TDBFactory.createDataset(dbString);

        dataset.begin(ReadWrite.WRITE);
        Model model = dataset.getDefaultModel();
        model.add(newModel);
        dataset.commit();
        dataset.end();

        return dataset;
    }

    public static void writeInferredModel(Dataset dataset, String[] files, String ontologyFile) {
        dataset.begin(ReadWrite.WRITE);

        Model model = dataset.getDefaultModel();

        // remove existing data first
        removeData(dataset);

        //fill with data from files
        for (String f : files) {
            model.read(f);
        }

        // create inferred model
        OntModel ontModel = ModelFactory.createOntologyModel(
                OntModelSpec.OWL_MEM_RULE_INF, model);
        ontModel.read(ontologyFile);

        //replace existing model with inferred model
        model = ontModel;

        // some validation
        ValidityReport vr = ontModel.validate();
        if (vr.isValid() == false) {
            System.out.print("ontology model validation failed.");
            for (Iterator i = vr.getReports(); i.hasNext();) {
                System.out.println("-" + i.next());
            }

        }

        dataset.commit();

        dataset.end();
    }

    public static void removeData(Dataset dataset) {
        Model model = dataset.getDefaultModel();
        model.removeAll();
    }
    
    public static String serializeTripleStore(String db, String format)
    {
        Dataset dataset = TDBFactory.createDataset(db);
        try {
            dataset.begin(ReadWrite.READ);
            Model model = dataset.getDefaultModel();
            dataset.end();
            
            StringWriter writer = new StringWriter();
            
            model.write(writer, format);
            String content = writer.toString();
            writer.close();
            
            return content;
            
        } catch (IOException ex) {
            Logger.getLogger(DBLogic.class.getName()).log(Level.SEVERE, null, ex);
            return "";
        }
        
        
    }

    public static Model readDefaultModel(Dataset dataset) {
        dataset.begin(ReadWrite.READ);
        Model data = dataset.getDefaultModel();
        dataset.end();

        return data;
    }

    public static void writeModel(Dataset dataset, String[] files) {
        dataset.begin(ReadWrite.WRITE);
        Model model = dataset.getDefaultModel();

        // remove existing data first
        removeData(dataset);

        for (String f : files) {
            model.read(f);
        }

        dataset.commit();

        dataset.end();
    }

    public static void printTest(Model model) {
        String ns = "<http://dl.dropboxusercontent.com/u/983997/data/test1.rdf#ProcessDef_SupportCase1>";
        String ns2 = "<http://dl.dropboxusercontent.com/u/983997/ontologies/event.owl#produced>";
        String ns3 = "<http://dl.dropboxusercontent.com/u/983997/ontologies/event.owl#ActivityInstanceOrigin>";
        try {
            QueryExecution qExec = QueryExecutionFactory.create(
                    "SELECT * {  ?p ?s ?o  }", model);
            ResultSet rs = qExec.execSelect();
            try {
                ResultSetFormatter.outputAsJSON(rs);
            } finally {
                qExec.close();
            }
        } finally {

        }
    }

    public static void printNumberOfTriples(Model model) {

        try {
            QueryExecution qExec = QueryExecutionFactory.create(
                    "SELECT (COUNT(*) AS ?noOfTriples) { ?s ?p ?o  }", model);
            ResultSet rs = qExec.execSelect();
            try {
                ResultSetFormatter.out(rs);
            } finally {
                qExec.close();
            }
        } finally {

        }
    }

    public static void printAllTriples(Model model) {

        try {
            QueryExecution qExec = QueryExecutionFactory.create(
                    "SELECT * {?s ?p ?o}", model);
            ResultSet rs = qExec.execSelect();
            try {
                ResultSetFormatter.out(rs);
            } finally {
                qExec.close();
            }
        } finally {

        }
    }

    public static void printDataset(Dataset dataset) {
        dataset.begin(ReadWrite.READ);
        try {
            QueryExecution qExec = QueryExecutionFactory.create(
                    "SELECT * {?s ?p ?o}", dataset);
            ResultSet rs = qExec.execSelect();
            try {
                ResultSetFormatter.out(rs);
            } finally {
                qExec.close();
            }
        } finally {
            dataset.end();
        }
    }

}
