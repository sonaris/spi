package other;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.reasoner.ValidityReport;
import com.hp.hpl.jena.tdb.TDBFactory;
import data.DBLogic;
import org.mindswap.pellet.jena.PelletReasonerFactory;

import java.util.Iterator;

/**
 * Created by Dave on 7/17/2014.
 */
public class CustomReasoning {

    public static void reasonOverModel(String db)
    {
        Dataset dataset = TDBFactory.createDataset(db);
        dataset.begin(ReadWrite.WRITE);
        Model modelStandard = dataset.getDefaultModel();
        dataset.end();
        try
        {
            InfModel inferredModel = createInferredModel(modelStandard, Settings.eventOntologyFile);

            dataset.begin(ReadWrite.WRITE);
            DBLogic.removeData(dataset);
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

    public static InfModel createInferredModel(Model model, String ontologyFile) {

        // create inferred model
        OntModel ontModel = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC, model);
        ontModel.read(ontologyFile);

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
}
