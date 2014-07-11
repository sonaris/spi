/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package logic;

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
    
    public static String ontologyFile = "";
    public static String eventOntologyNameSpace = "";
    public static String workflowOntologyNameSpace = "";
    public static String businessPartnerOntologyNameSpace = "";
    public static String economicObjectOntologyNameSpace = "";
    public static String systemOntologyNameSpace = "";

    public static void readSettings() {
        Properties prop = new Properties();
        InputStream input = null;

        try {

            input = new FileInputStream("config.properties");

            // load a properties file
            prop.load(input);

            // get the property value and print it out
            Settings.ontologyFile = prop.getProperty("ontologyFile");
            Settings.eventOntologyNameSpace = prop.getProperty("eventOntologyNameSpace");
            Settings.workflowOntologyNameSpace = prop.getProperty("workflowOntologyNameSpace");
            Settings.businessPartnerOntologyNameSpace = prop.getProperty("businessPartnerOntologyNameSpace");
            Settings.economicObjectOntologyNameSpace = prop.getProperty("economicObjectOntologyNameSpace");
            Settings.systemOntologyNameSpace = prop.getProperty("systemOntologyNameSpace");

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

    public static void writeSettings(String ontologyFileURI, String event_ns, String wf_ns, String bp_ns, String eo_ns, String s_ns) {
        Properties prop = new Properties();
        OutputStream output = null;

        try {

            output = new FileOutputStream("config.properties");
            
            Settings.ontologyFile = ontologyFileURI;
            Settings.eventOntologyNameSpace = event_ns;
            Settings.workflowOntologyNameSpace = wf_ns;
            Settings.businessPartnerOntologyNameSpace = bp_ns;
            Settings.economicObjectOntologyNameSpace = eo_ns;
            Settings.systemOntologyNameSpace = s_ns;

            // set the properties value
            prop.setProperty("ontologyFile", ontologyFileURI);
            prop.setProperty("eventOntologyNameSpace", event_ns);
            prop.setProperty("workflowOntologyNameSpace", wf_ns);
            prop.setProperty("businessPartnerOntologyNameSpace", bp_ns);
            prop.setProperty("economicObjectOntologyNameSpace", eo_ns);
            prop.setProperty("systemOntologyNameSpace", s_ns);

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
