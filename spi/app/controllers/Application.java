package controllers;


import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.update.UpdateAction;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateRequest;
import com.hp.hpl.jena.vocabulary.RDF;
import data.DBLogic;
import logic.*;
import other.Settings;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import static other.CustomReasoning.reasonOverModel;

public class Application extends Controller {

    public static Result saveSettings()
    {
        try {
            Settings.readSettings();

            Map<String, String[]> values = request().body().asFormUrlEncoded();

            Settings.eventOntologyFile = values.get("eventOnt")[0];
            Settings.spaOntologyFile = values.get("spaOnt")[0];
            Settings.eventOntologyNameSpace = values.get("eventOntURI")[0];
            Settings.workflowOntologyNameSpace = values.get("workflowOntURI")[0];
            Settings.systemOntologyNameSpace = values.get("systemOntURI")[0];
            Settings.businessPartnerOntologyNameSpace = values.get("businessPartnerOntURI")[0];
            Settings.economicObjectOntologyNameSpace = values.get("economicObjectOntURI")[0];
            Settings.queryOntologyNameSpace = values.get("queryOntURI")[0];
            Settings.spaOntologyNameSpace = values.get("spaOntURI")[0];

            Settings.writeSettings();

            return ok(views.html.settings.render(
                    Settings.eventOntologyFile,
                    Settings.spaOntologyFile,
                    Settings.eventOntologyNameSpace,
                    Settings.workflowOntologyNameSpace,
                    Settings.systemOntologyNameSpace,
                    Settings.businessPartnerOntologyNameSpace,
                    Settings.economicObjectOntologyNameSpace,
                    Settings.queryOntologyNameSpace,
                    Settings.spaOntologyNameSpace,
                    "success"));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return ok(views.html.settings.render(
                    Settings.eventOntologyFile,
                    Settings.spaOntologyFile,
                    Settings.eventOntologyNameSpace,
                    Settings.workflowOntologyNameSpace,
                    Settings.systemOntologyNameSpace,
                    Settings.businessPartnerOntologyNameSpace,
                    Settings.economicObjectOntologyNameSpace,
                    Settings.queryOntologyNameSpace,
                    Settings.spaOntologyNameSpace,
                    e.toString()));
        }
    }


    public static Result emptyTripleStore()
    {
        Settings.readSettings();

        DBLogic.emptyTripleStore("DB_Analysis");

        return ok("{}");

    }

    public static Result reasonOverTripleStore()
    {
        Settings.readSettings();

        reasonOverModel("DB_Analysis");

        return ok("{}");

    }

    public static Result serializeData()
    {
        Map<String, String[]> values = request().body().asFormUrlEncoded();

        String format = values.get("format")[0];

        String result = DBLogic.serializeTripleStore("DB_Analysis", format);

        return ok(result);
    }

    public static Result getQueryResult()
    {
        Map<String, String[]> values = request().body().asFormUrlEncoded();

        String query = values.get("query")[0];


        Model model = DBLogic.getDefaultModel("DB_Analysis");
        try
        {
            QueryExecution qExec = QueryExecutionFactory.create(query, model);
            ResultSet rs = qExec.execSelect();

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ResultSetFormatter.outputAsJSON(os, rs);

            String result = "{}";

            try {
                result = new String(os.toByteArray(),"UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            return ok(result);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return internalServerError(e.getMessage());
        }
    }

    public static Result saveQuery()
    {
        try {
            Map<String, String[]> values = request().body().asFormUrlEncoded();

            String name = values.get("name")[0];
            String query = values.get("query")[0];

            String baseURI = "www.spa.com/internal.rdf";

            Settings.readSettings();

            OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RULE_INF);
            ontModel.read(Settings.spaOntologyFile);

            Dataset dataset = TDBFactory.createDataset("DB_User");
            dataset.begin(ReadWrite.WRITE);
            Model dataModel = dataset.getDefaultModel();

            //Create new content
            Resource r = dataModel.createResource(baseURI + "#Query_" + name).addProperty(RDF.type, ontModel.getOntClass(Settings.queryOntologyNameSpace + "#Query"));
            r.addLiteral(ontModel.getOntProperty(Settings.queryOntologyNameSpace + "#hasQueryName"), ResourceFactory.createTypedLiteral(name, XSDDatatype.XSDstring));
            r.addLiteral(ontModel.getOntProperty(Settings.queryOntologyNameSpace + "#hasQueryContent"), ResourceFactory.createTypedLiteral(query, XSDDatatype.XSDstring));

            dataset.commit();
            dataset.end();

            return ok("{}");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return internalServerError(e.toString());
        }
    }

    public static Result deleteQuery()
    {
        Settings.readSettings();

        Map<String, String[]> values = request().body().asFormUrlEncoded();

        String uri = values.get("queryURI")[0];

        Dataset dataset = TDBFactory.createDataset("DB_User");
        dataset.begin(ReadWrite.WRITE);
        Model dataModel = dataset.getDefaultModel();
        try
        {
            UpdateRequest request = UpdateFactory.create();
            String action =
                     "DELETE WHERE { <" + uri + "> ?p ?o };"
                    +"DELETE WHERE { ?p <" + uri + "> ?o };"
                    +"DELETE WHERE { ?p ?o <" + uri + ">}";

            // And perform the operations.
            UpdateAction.parseExecute( action, dataModel );

            dataset.commit();
            dataset.end();

            return ok("{}");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return internalServerError(e.getMessage());
        }
    }

    public static Result getQueries()
    {
        Map<String, String[]> values = request().body().asFormUrlEncoded();

        String query = values.get("query")[0];

        Model model = DBLogic.getDefaultModel("DB_User");
        try
        {
            QueryExecution qExec = QueryExecutionFactory.create(query, model);
            ResultSet rs = qExec.execSelect();

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ResultSetFormatter.outputAsJSON(os, rs);

            String result = "{}";

            try {
                result = new String(os.toByteArray(),"UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            return ok(result);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return internalServerError(e.getMessage());
        }
    }

    public static Result importSynthetic() {

        try {
            Settings.readSettings();

            Http.MultipartFormData body = request().body().asMultipartFormData();

            Http.MultipartFormData.FilePart file1 = body.getFile("filePart1");
            Http.MultipartFormData.FilePart file2 = body.getFile("filePart2");
            Http.MultipartFormData.FilePart file3 = body.getFile("filePart3");

            if (file1 != null || file2 != null || file3 != null) {

                String baseURI = body.asFormUrlEncoded().get("rdfURI")[0];
                String systemName = body.asFormUrlEncoded().get("systemName")[0];

                boolean r = false;
                try {
                    String reasoning = body.asFormUrlEncoded().get("reasoning")[0];
                    if (reasoning.equals("on")) r = true;
                } catch (Exception e) {
                    r = false;
                }

                String workflowPath = file1.getFile().getPath();
                String eventPath = file2.getFile().getPath();
                String contextPath = file3.getFile().getPath();

                //Create Model
                SyntheticImporter s = new SyntheticImporter();
                s.setFilePaths(workflowPath, eventPath, contextPath);
                Model initialEventLogModel = s.createModel(baseURI, systemName, "BPMSystem");

                //Save models to database
                DBLogic.saveModelToTripleStore("DB_Analysis", initialEventLogModel);

                if (r == true) reasonOverModel("DB_Analysis");

                return ok(views.html.data.render("success"));
            } else {
                return ok(views.html.data.render("Files could not be loaded"));
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return ok(views.html.data.render(e.toString()));
        }
    }

    public static Result importStardust() {
        try {
            Settings.readSettings();

            Map<String, String[]> values = request().body().asFormUrlEncoded();

            String dbURL = values.get("dbURL")[0];
            String baseURI = values.get("rdfURI")[0];
            String systemName = values.get("systemName")[0];

            boolean r = false;
            try {
                String reasoning = values.get("reasoning")[0];
                if (reasoning.equals("on")) r = true;
            } catch (Exception e) {
                r = false;
            }

            //Create Model
            StardustImporter s = new StardustImporter();
            s.createConnection(dbURL);
            Model initialEventLogModel = s.createModel(baseURI, systemName, "BPMSystem");
            s.shutdown();

            //Save models to database
            DBLogic.saveModelToTripleStore("DB_Analysis", initialEventLogModel);

            if (r == true) reasonOverModel("DB_Analysis");

            return ok(views.html.data.render("success"));
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return ok(views.html.data.render(e.toString()));
        }
    }

    public static Result importCamunda() {
        try {
            Settings.readSettings();

            Map<String, String[]> values = request().body().asFormUrlEncoded();

            String restURL = values.get("restURL")[0];
            String baseURI = values.get("rdfURI")[0];
            String systemName = values.get("systemName")[0];

            boolean r = false;
            try {
                String reasoning = values.get("reasoning")[0];
                if (reasoning.equals("on")) r = true;
            } catch (Exception e) {
                r = false;
            }

            //Create Model
            CamundaImporter s = new CamundaImporter(restURL);
            Model initialEventLogModel = s.createModel(baseURI, systemName, "BPMSystem");

            //Save models to database
            DBLogic.saveModelToTripleStore("DB_Analysis", initialEventLogModel);

            if (r == true) reasonOverModel("DB_Analysis");

            return ok(views.html.data.render("success"));
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return ok(views.html.data.render(e.toString()));
        }
    }

    private static String getStackTraceAsString(Exception e)
    {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String result = sw.toString();

        return result;
    }
    
}
