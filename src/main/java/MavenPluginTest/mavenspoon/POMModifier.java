package MavenPluginTest.mavenspoon;

import hudson.FilePath;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;

/**
 * Created by SDS-PCSS on 2016-04-01.
 */
public class POMModifier {
    private final POMGetter pom;
    private final TaskListener listener;
    private final FilePath workspace;
    private final Run<?, ?> build;
    private Element p;
    private NodeList nodes;
    private Node newNode;

    public POMModifier(POMGetter pomGetter, TaskListener listener, FilePath workspace, Run<?, ?> build) {
        this.pom = pomGetter;
        this.listener = listener;
        this.workspace = workspace;
        this.build = build;

    }


    protected boolean insertSpoonPlugin() throws ParserConfigurationException, IOException, SAXException, TransformerException, POMGetter.InvalidBuildFileFormatException {


        Document doc = pom.getPom(this.workspace);

        //create plugin nodes if doesn't exist
        if (doc.getElementsByTagName("build").getLength()<1) {
            p = doc.createElement("build");
            newNode = doc.getLastChild().appendChild(p);
        }else{
            nodes = doc.getElementsByTagName("build");
            newNode =  nodes.item(0);
        }

        if (doc.getElementsByTagName("plugins").getLength()<1) {
            p = doc.createElement("plugins");
            newNode = newNode.appendChild(p);
        }else{
            nodes = doc.getElementsByTagName("plugins");
            newNode =  nodes.item(0);
        }

//      Add spoon plugin in the DOM

//        <plugin>
        p = doc.createElement("plugin");
        newNode = newNode.appendChild(p);

//        <groupId>fr.inria.gforge.spoon</groupId>
        p = doc.createElement("groupId");
        Text innerXML = doc.createTextNode("fr.inria.gforge.spoon");
        p.appendChild(innerXML);
        newNode.appendChild(p);

//        <artifactId>spoon-maven-plugin</artifactId>
        p = doc.createElement("artifactId");
        innerXML = doc.createTextNode("spoon-maven-plugin");
        p.appendChild(innerXML);
        newNode.appendChild(p);

//        <version>2.2</version>
        p = doc.createElement("version");
        innerXML = doc.createTextNode("2.2");
        p.appendChild(innerXML);
        newNode.appendChild(p);

//        <executions>
        p = doc.createElement("executions");
        newNode = newNode.appendChild(p);

//        <execution>
        p = doc.createElement("execution");
        newNode = newNode.appendChild(p);

//        <phase>generate-sources</phase>
        p = doc.createElement("phase");
        innerXML = doc.createTextNode("generate-sources");
        p.appendChild(innerXML);
        newNode.appendChild(p);


//        <goals>
        p = doc.createElement("goals");
        newNode = newNode.appendChild(p);


//        <goal>generate</goal>
        p = doc.createElement("goal");
        innerXML = doc.createTextNode("generate");
        p.appendChild(innerXML);
        newNode.appendChild(p);

        doc.normalizeDocument();


//      Get the XML to string

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        StreamResult result = new StreamResult(new StringWriter());
        DOMSource source = new DOMSource(doc);
        transformer.transform(source, result);

        String xmlOutput = result.getWriter().toString();
        listener.getLogger().println(xmlOutput);


//      Write XML in the file

        try {
            writeToFile(xmlOutput);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }




        return true;
    }

    public void writeToFile(String source) throws IOException, InterruptedException {

       BufferedWriter bw = null;


        try {

            FilePath pomfile = new FilePath(workspace, "pom.xml");
            File file = new File( build.getEnvironment(listener).get("WORKSPACE")+"\\"+pomfile.getName());

            if (!file.delete()) {
                listener.getLogger().println("new pom not deleted");
            } else if (!file.createNewFile()) {
                listener.getLogger().println("new pom not created");
            }
             
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));

            bw.write(source);
            bw.flush();

            listener.getLogger().println(source);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // releases any system resources associated with the stream
            if (bw != null) {
                bw.close();
            }
        }
    }
}
