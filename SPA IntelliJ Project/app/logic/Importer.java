package logic;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * Created by Dave on 7/18/2014.
 */
public abstract class Importer {
    protected ModelCreator mc;

    public Model createModel(String baseURI, String systemName, String systemType) {
        this.mc = new ModelCreator(baseURI);
        this.mc.createSystem("1",systemName, systemType);

        createWorkflowModel();
        createEventAndContextData();

        return mc.getModel();
    }

    abstract protected void createWorkflowModel();

    abstract protected void createEventAndContextData();
}
