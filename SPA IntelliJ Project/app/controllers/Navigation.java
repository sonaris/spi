package controllers;

import data.DBLogic;
import other.Settings;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.Map;

/**
 * Created by Dave on 7/17/2014.
 */
public class Navigation extends Controller {

    //Basic navigation

    public static Result home() {
        String role = session("role");
        String user = session("user");

        if(role != null && (role.equals("admin") || role.equals("analyst"))) return ok(views.html.home.render(user));
        else return redirect(routes.Navigation.login());
    }

    public static Result login() {
        DBLogic.createTestUser();
        return ok(views.html.login.render("empty"));
    }

    public static Result logout() {
        session("role", "");
        session("user", "");

        return redirect(routes.Navigation.login());
    }

    public static Result verifyLogin() {
        Map<String, String[]> values = request().body().asFormUrlEncoded();

        String email = values.get("email")[0];
        String password = values.get("password")[0];

        String[] result = DBLogic.isValidUser(email, password, "DB_User");

        if (result != null)
        {
            session("role", result[0]);
            session("user", result[1]);
            session("account", result[2]);
            return redirect(routes.Navigation.home());
        }
        else return redirect(routes.Navigation.login());
    }

    public static Result data() {
        String role = session("role");
        String user = session("user");

        if(role != null && role.equals("admin")) return ok(views.html.data.render("empty"));
        else return redirect(routes.Navigation.login());
    }

    public static Result export() {
        String role = session("role");
        String user = session("user");

        if(role != null && (role.equals("admin") || role.equals("analyst"))) return ok(views.html.export.render("empty"));
        else return redirect(routes.Navigation.login());
    }

    public static Result summary() {
        String role = session("role");
        String user = session("user");

        if(role != null && (role.equals("admin") || role.equals("analyst"))) return ok(views.html.summary.render("empty"));
        else return redirect(routes.Navigation.login());
    }

    public static Result statistics_quality() {
        String role = session("role");
        String user = session("user");

        if(role != null && (role.equals("admin") || role.equals("analyst"))) return ok(views.html.statistics_quality.render("empty"));
        else return redirect(routes.Navigation.login());
    }

    public static Result statistics_time() {
        String role = session("role");
        String user = session("user");

        if(role != null && (role.equals("admin") || role.equals("analyst"))) return ok(views.html.statistics_time.render("empty"));
        else return redirect(routes.Navigation.login());
    }

    public static Result statistics_controlflow() {
        String role = session("role");
        String user = session("user");

        if(role != null && (role.equals("admin") || role.equals("analyst"))) return ok(views.html.statistics_controlflow.render("empty"));
        else return redirect(routes.Navigation.login());
    }

    public static Result query() {
        String role = session("role");
        String user = session("user");

        if(role != null && (role.equals("admin") || role.equals("analyst"))) return ok(views.html.query.render("empty"));
        else return redirect(routes.Navigation.login());
    }

    public static Result settings() {
        String role = session("role");
        String user = session("user");

        if(role != null && (role.equals("admin") || role.equals("analyst")))
        {
            Settings.readSettings();
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
                    "empty"));
        }
        else return redirect(routes.Navigation.login());
    }
}
