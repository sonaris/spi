package controllers;


import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.vocabulary.RDF;
import data.DBLogic;
import logic.*;
import play.api.mvc.MultipartFormData;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

public class Application extends Controller {

    //Basic navigation

    public static Result home() {
        String user = session("connected");
        if(user != null) {
            return ok(views.html.home.render(user));
        } else {
            session("connected", "user@gmail.com");
            return unauthorized("Oops, you are not connected");
        }
    }

    public static Result data() {
        return ok(views.html.data.render("empty"));
    }

    public static Result export() {
        return ok(views.html.export.render("empty"));
    }

    public static Result summary() {
        return ok(views.html.summary.render("empty"));
    }

    public static Result statistics() {
        return ok(views.html.statistics.render("empty"));
    }

    public static Result query() {
        return ok(views.html.query.render("empty"));
    }

    //Logic
    public static Result getSettings()
    {
        Settings.readSettings();

        return ok("{\"ontologyFile\" : \""+Settings.ontologyFile+"\"}");

    }

    public static Result emptyTripleStore()
    {
        Settings.readSettings();

        DBLogic.emptyTripleStore("DB_Analysis");

        return redirect(routes.Application.data());

    }

    public static Result reasonOverTripleStore()
    {
        Settings.readSettings();

        DBLogic.reasonOverModel("DB_Analysis");

        return redirect(routes.Application.data());

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
            return internalServerError(e.getMessage());
        }
    }

    public static Result saveQuery()
    {
        Map<String, String[]> values = request().body().asFormUrlEncoded();

        String name = values.get("name")[0];
        String query = values.get("query")[0];

        String userNS = "http://www.spi.com/user";
        String baseURI = "www.test.com/user.rdf";

        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RULE_INF);
        ontModel.read("http://dl.dropboxusercontent.com/u/983997/ontologies/user.rdf");

        Dataset dataset = TDBFactory.createDataset("DB_User");
        dataset.begin(ReadWrite.WRITE);
        Model dataModel = dataset.getDefaultModel();

        //Create new content
        Resource r = dataModel.createResource(baseURI + "#Query1").addProperty(RDF.type, ontModel.getOntClass(userNS + "#Query"));
        r.addLiteral(ontModel.getOntProperty(userNS + "#hasName"), ResourceFactory.createTypedLiteral(name, XSDDatatype.XSDstring));
        r.addLiteral(ontModel.getOntProperty(userNS + "#hasContent"), ResourceFactory.createTypedLiteral(query, XSDDatatype.XSDstring));

        dataset.commit();
        dataset.end();

        return redirect(routes.Application.query());
    }

    public static Result getQueries()
    {
        Map<String, String[]> values = request().body().asFormUrlEncoded();

        Model model = DBLogic.getDefaultModel("DB_User");
        try
        {
            String ns = "http://www.spi.com/user";

            String query =
                    "SELECT ?name ?content " +
                    "WHERE" +
                    "{?query a <http://www.spi.com/user#Query>; <http://www.spi.com/user#hasName> ?name; <http://www.spi.com/user#hasContent> ?content }";

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
            return internalServerError(e.getMessage());
        }
    }

    public static Result importSynthetic() {
        //Read Settings
        Settings.readSettings();

        Http.MultipartFormData body = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart file1 = body.getFile("filePart1");
        Http.MultipartFormData.FilePart file2 = body.getFile("filePart2");
        Http.MultipartFormData.FilePart file3 = body.getFile("filePart3");
        if (file1 != null && file2 != null && file3 != null) {

            String workflowPath = file1.getFile().getPath();
            String eventPath = file2.getFile().getPath();
            String contextPath = file3.getFile().getPath();

            //Create Model
            Synthetic s = new Synthetic("http://www.synthetic.com/model.rdf", workflowPath, eventPath, contextPath);
            Model initialEventLogModel = s.createModel();

            //Save models to database
            DBLogic.saveModelToTripleStore("DB_Analysis", initialEventLogModel);

            return redirect(routes.Application.summary());
        } else {
            flash("error", "Missing file");
            return redirect(routes.Application.home());
        }
    }

    public static Result importStardust() {
        //Read Settings
        Settings.readSettings();

        //Create Model
        Stardust s = new Stardust("http://www.stardust.com/model.rdf", "1");
        Model initialEventLogModel = s.createModel();

        //Save models to database
        DBLogic.saveModelToTripleStore("DB_Analysis", initialEventLogModel);

        return redirect(routes.Application.summary());
    }

    public static Result importCamunda() {
        //Read Settings
        Settings.readSettings();

        //Create Model
        Camunda s = new Camunda("http://www.camunda.com/model.rdf", "2");
        Model initialEventLogModel = s.createModel();

        //Save models to database
        DBLogic.saveModelToTripleStore("DB_Analysis", initialEventLogModel);

        return redirect(routes.Application.summary());
    }
    
}
