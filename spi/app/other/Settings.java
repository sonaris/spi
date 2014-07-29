/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package other;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

/**
 *
 * @author Dave
 */
public class Settings {

    public static String eventOntologyFile = "";
    public static String spaOntologyFile = "";
    public static String eventOntologyNameSpace = "";
    public static String workflowOntologyNameSpace = "";
    public static String businessPartnerOntologyNameSpace = "";
    public static String economicObjectOntologyNameSpace = "";
    public static String systemOntologyNameSpace = "";
    public static String spaOntologyNameSpace = "";
    public static String queryOntologyNameSpace = "";

    public static void readSettings() {
        Properties prop = new Properties();
        InputStream input = null;

        try {

            input = new FileInputStream("config.properties");

            // load a properties file
            prop.load(input);

            // get the property value and print it out
            Settings.eventOntologyFile = prop.getProperty("eventOntologyFile");
            Settings.spaOntologyFile = prop.getProperty("spaOntologyFile");
            Settings.eventOntologyNameSpace = prop.getProperty("eventOntologyNameSpace");
            Settings.workflowOntologyNameSpace = prop.getProperty("workflowOntologyNameSpace");
            Settings.businessPartnerOntologyNameSpace = prop.getProperty("businessPartnerOntologyNameSpace");
            Settings.economicObjectOntologyNameSpace = prop.getProperty("economicObjectOntologyNameSpace");
            Settings.systemOntologyNameSpace = prop.getProperty("systemOntologyNameSpace");
            Settings.spaOntologyNameSpace = prop.getProperty("spaOntologyNameSpace");
            Settings.queryOntologyNameSpace = prop.getProperty("queryOntologyNameSpace");

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void writeSettings() {
        Properties prop = new Properties();
        OutputStream output = null;

        try {

            output = new FileOutputStream("config.properties");

            // set the properties value
            prop.setProperty("eventOntologyFile", Settings.eventOntologyFile);
            prop.setProperty("spaOntologyFile", Settings.spaOntologyFile);
            prop.setProperty("eventOntologyNameSpace", Settings.eventOntologyNameSpace);
            prop.setProperty("workflowOntologyNameSpace", Settings.workflowOntologyNameSpace);
            prop.setProperty("businessPartnerOntologyNameSpace", Settings.businessPartnerOntologyNameSpace);
            prop.setProperty("economicObjectOntologyNameSpace", Settings.economicObjectOntologyNameSpace);
            prop.setProperty("systemOntologyNameSpace", Settings.systemOntologyNameSpace);
            prop.setProperty("spaOntologyNameSpace", Settings.spaOntologyNameSpace);
            prop.setProperty("queryOntologyNameSpace", Settings.queryOntologyNameSpace);

            // save properties to project root folder
            prop.store(output, null);

        } catch (IOException io) {
            io.printStackTrace();
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }
}
