package logic;

import java.io.FileWriter;
import java.io.IOException;

import com.hp.hpl.jena.rdf.model.Model;

public class Helper {

	public static void createFile(String fileName, Model model) throws IOException
	{
		FileWriter out = new FileWriter( fileName );
		try {
		    model.write( out, "RDF/XML-ABBREV" );
		}
		finally {
		   try {
		       out.close();
		       System.out.println("File "+ fileName+"successfully created");
		   }
		   catch (IOException closeException) {}
		}
	}
}
