package data;


import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.reasoner.ValidityReport;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.vocabulary.RDF;
import other.Settings;
import org.mindswap.pellet.jena.PelletReasonerFactory;

import java.io.*;
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

    public static void readRDF(FileInputStream stream, String db)
    {
        Dataset dataset = TDBFactory.createDataset(db);
        dataset.begin(ReadWrite.WRITE);
        Model model = dataset.getDefaultModel();
        model.read(stream, "http://www.test.com");
        dataset.commit();
        dataset.end();
    }

    public static void createTestUser()
    {
        Settings.readSettings();

        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RULE_INF);
        ontModel.read(Settings.spaOntologyFile);

        //Create Test User
        Dataset dataset = TDBFactory.createDataset("DB_User");
        dataset.begin(ReadWrite.WRITE);
        Model dataModel = dataset.getDefaultModel();
        String baseURI = "www.spa.com/internal.rdf";

        Resource r1 = dataModel.createResource(baseURI + "#ada.admin@test.com").addProperty(RDF.type, ontModel.getOntClass(Settings.systemOntologyNameSpace + "#Account"));
        r1.addLiteral(ontModel.getOntProperty(Settings.systemOntologyNameSpace + "#hasAccountUserName"), ResourceFactory.createTypedLiteral("Ada Admin", XSDDatatype.XSDstring));
        r1.addLiteral(ontModel.getOntProperty(Settings.systemOntologyNameSpace + "#hasAccountName"), ResourceFactory.createTypedLiteral("ada.admin@test.com", XSDDatatype.XSDstring));
        r1.addLiteral(ontModel.getOntProperty(Settings.systemOntologyNameSpace + "#hasAccountPassword"), ResourceFactory.createTypedLiteral("test", XSDDatatype.XSDstring));
        r1.addLiteral(ontModel.getOntProperty(Settings.systemOntologyNameSpace + "#hasAccountRole"), ResourceFactory.createTypedLiteral("admin", XSDDatatype.XSDstring));

        Resource r2 = dataModel.createResource(baseURI + "#adam.analyst@test.com").addProperty(RDF.type, ontModel.getOntClass(Settings.systemOntologyNameSpace + "#Account"));
        r2.addLiteral(ontModel.getOntProperty(Settings.systemOntologyNameSpace + "#hasAccountUserName"), ResourceFactory.createTypedLiteral("Adam Analyst", XSDDatatype.XSDstring));
        r2.addLiteral(ontModel.getOntProperty(Settings.systemOntologyNameSpace + "#hasAccountName"), ResourceFactory.createTypedLiteral("adam.analyst@test.com", XSDDatatype.XSDstring));
        r2.addLiteral(ontModel.getOntProperty(Settings.systemOntologyNameSpace + "#hasAccountPassword"), ResourceFactory.createTypedLiteral("test", XSDDatatype.XSDstring));
        r2.addLiteral(ontModel.getOntProperty(Settings.systemOntologyNameSpace + "#hasAccountRole"), ResourceFactory.createTypedLiteral("analyst", XSDDatatype.XSDstring));

        dataset.commit();
        dataset.end();
    }

    public static String[] isValidUser(String accountName, String userPassword, String db)
    {
        Settings.readSettings();

        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RULE_INF);
        ontModel.read(Settings.spaOntologyFile);

        Dataset dataset = TDBFactory.createDataset(db);
        dataset.begin(ReadWrite.READ);
        Model model = dataset.getDefaultModel();
        ResIterator resources = model.listResourcesWithProperty(ontModel.getProperty(Settings.systemOntologyNameSpace + "#hasAccountName"), accountName);
        dataset.end();
        if (resources.hasNext())
        {
            Resource instance = resources.nextResource();
            String password = instance.getProperty(ontModel.getProperty(Settings.systemOntologyNameSpace + "#hasAccountPassword")).getString();
            if (password.equals(userPassword))
            {
                String userName = instance.getProperty(ontModel.getProperty(Settings.systemOntologyNameSpace + "#hasAccountUserName")).getString();
                String userRole = instance.getProperty(ontModel.getProperty(Settings.systemOntologyNameSpace + "#hasAccountRole")).getString();
                return new String[] {userRole,userName,accountName};
            }
            else return null;
        }

        return null;
    }

    public static String getSavedQueries(String account)
    {
        Model model = DBLogic.getDefaultModel("DB_User");

        String prefix = "PREFIX query: <http://www.spi.com/query.owl#>\n " +
                        "PREFIX spa: <http://www.spi.com/spa.owl#>\n " +
                        "PREFIX system: <http://www.spi.com/system.owl#>";

            String query =
                    "SELECT ?query ?name ?content \n"
                   +"WHERE { \n"
                   +"?query a query:Query; query:hasQueryName ?name; query:hasQueryContent ?content. \n"
                   +"?query spa:createdByAccount ?account. \n"
                   +"?account system:hasAccountName ?accountName. \n"
                   + "FILTER (str(?accountName) = \""+account+"\"). \n"
                   +"}";
            QueryExecution qExec = QueryExecutionFactory.create(prefix+query, model);
            ResultSet rs = qExec.execSelect();

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ResultSetFormatter.outputAsJSON(os, rs);

            String result = "{}";

            try {
                result = new String(os.toByteArray(),"UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            return result;


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
