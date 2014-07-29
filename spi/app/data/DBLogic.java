package data;


import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.reasoner.ValidityReport;
import com.hp.hpl.jena.tdb.TDBFactory;
import other.Settings;
import org.mindswap.pellet.jena.PelletReasonerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DBLogic {

    public static Model getDefaultModel(String db)
    {
        Dataset dataset = TDBFactory.createDataset(db);
        dataset.begin(ReadWrite.READ);
        Model model = dataset.getDefaultModel();
        dataset.end();
        return model;
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
}
