package MavenPluginTest.mavenspoon;

import hudson.FilePath;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
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
    private Element p;
    private NodeList nodes;

    public POMModifier(POMGetter pomGetter, TaskListener listener, FilePath worspace) {
        this.pom = pomGetter;
        this.listener = listener;
        this.workspace = worspace;
    }


    protected boolean insertSpoonPlugin() throws ParserConfigurationException, IOException, SAXException, TransformerException, POMGetter.InvalidBuildFileFormatException {


//        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
//        domFactory.setIgnoringComments(true);
//        DocumentBuilder builder = domFactory.newDocumentBuilder();
//        Document doc = builder.parse(new File("pom.xml"));

        Document doc = pom.getPom(this.workspace);

        //create plugin nodes if doesn't exist
        if( !pom.hasNode("build"))
        {
          p = doc.createElement("build");
            doc.getParentNode().insertBefore(p,doc.getParentNode());
        }

        if(!pom.hasNode("plugins"))
        {
            p = doc.createElement("plugins");
            nodes = doc.getElementsByTagName("build");
            nodes.item(0).getParentNode().insertBefore(p, nodes.item(0));
        }

//      Add spoon plugin in the DOM

//        <plugin>
        p = doc.createElement("plugin");
        nodes = doc.getElementsByTagName("plugins");
        nodes.item(0).getParentNode().insertBefore(p, nodes.item(0));

        nodes = doc.getElementsByTagName("plugin");

//        <groupId>fr.inria.gforge.spoon</groupId>
        p = doc.createElement("groupId");
        Text innerXML = doc.createTextNode("fr.inria.gforge.spoon");
        p.appendChild(innerXML);
        nodes.item(0).getParentNode().insertBefore(p, nodes.item(0));

//        <artifactId>spoon-maven-plugin</artifactId>
        p = doc.createElement("artifactId");
        innerXML = doc.createTextNode("spoon-maven-plugin");
        p.appendChild(innerXML);
        nodes.item(0).getParentNode().insertBefore(p, nodes.item(1));

//        <version>2.2</version>
        p = doc.createElement("version");
        innerXML = doc.createTextNode("2.2");
        p.appendChild(innerXML);
        nodes.item(0).getParentNode().insertBefore(p, nodes.item(2));

//        <executions>
        p = doc.createElement("executions");
        nodes.item(0).getParentNode().insertBefore(p, nodes.item(0));

//        <execution>
        nodes = doc.getElementsByTagName("executions");
        p = doc.createElement("execution");
        nodes.item(0).getParentNode().insertBefore(p, nodes.item(0));

//        <phase>generate-sources</phase>
        nodes = doc.getElementsByTagName("execution");
        p = doc.createElement("phase");
        innerXML = doc.createTextNode("generate-sources");
        p.appendChild(innerXML);
        nodes.item(0).getParentNode().insertBefore(p, nodes.item(0));

//        <goals>
        p = doc.createElement("goals");
        nodes.item(0).getParentNode().insertBefore(p, nodes.item(1));

//        <goal>generate</goal>
        nodes = doc.getElementsByTagName("goals");
        p = doc.createElement("goal");
        innerXML = doc.createTextNode("generate");
        p.appendChild(innerXML);
        nodes.item(0).getParentNode().insertBefore(p, nodes.item(0));


//      Get the XML to string

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        StreamResult result = new StreamResult(new StringWriter());
        DOMSource source = new DOMSource(doc);
        transformer.transform(source, result);

        String xmlOutput = result.getWriter().toString();
        System.out.println(xmlOutput);


//      Write XML in the file



        return true;
    }

    public void writeToFile(String source) throws IOException {

        StringWriter sw = null;
        BufferedWriter bw = null;


        try {
            FilePath pomfile = new FilePath(workspace, "pom.xml");

            File file = new File(String.valueOf(pomfile)); // A tester ..
            if(!file.delete()){
                listener.getLogger().println("new pom not created");
            }else if (!file.createNewFile()) {
                listener.getLogger().println("new pom not created");
            }


        sw = new StringWriter();
        bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
        StringBuffer sb = sw.getBuffer();
        bw.write(sb.toString());
        bw.flush();


        listener.getLogger().println(sb);


    } catch (IOException e) {
        e.printStackTrace();
        } finally {
            // releases any system resources associated with the stream
            if (sw != null)
                sw.close();
            if (bw != null)
                bw.close();


        }
    }

}
