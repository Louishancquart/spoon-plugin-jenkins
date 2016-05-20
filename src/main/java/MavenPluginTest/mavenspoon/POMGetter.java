package MavenPluginTest.mavenspoon;

import hudson.FilePath;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import org.jenkinsci.remoting.RoleChecker;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;


/**
 * This class is about to get the pom file as a Document and parse this document to gather some piece of information.
 * Created by Louis on 01.03.16.
 */
public class POMGetter {

    public final FilePath workspace;
    private final String BUILD_FILE = "pom.xml";
    private final TaskListener listener;


    public POMGetter(FilePath workspace, TaskListener listener) {
        this.workspace = workspace;
        this.listener = listener;
    }

    /**
     * Look for an info under the root "project" in the pom file
     *
     * @param expressionToCompile
     * @return true if the pom file contains the node entered in parameter
     * @throws InvalidFileFormatException
     * @throws IOException
     */
    public String getInfo(String expressionToCompile) throws InvalidFileFormatException, IOException {
        String info = null;

        listener.getLogger().println("WORKSPACE:"+workspace.toString());



        Document document = getPom(workspace);

        XPath xPath = XPathFactory.newInstance().newXPath();
        XPathExpression expression;

        try {
            expression = xPath.compile("/project/"+expressionToCompile);
            info = expression.evaluate(document);
        } catch (XPathExpressionException e) {
            assert document != null;
            throw new InvalidFileFormatException(document.getBaseURI()
                        + " is not a valid POM file.");
        }

        if (info == null || info.length() == 0) {
                assert document != null;
               info ="";
        }
        return info;
    }

    /**
     * Check if the project has a plugin associated with the artifactId in parameter
     *
     * @param pluginArtifactId
     * @return true if the pom file contains the plugin artifact in parameter
     * @throws InvalidFileFormatException
     * @throws IOException
     */
    public Boolean hasPlugin(String pluginArtifactId) throws InvalidFileFormatException, IOException {

        if(pluginArtifactId == null){
            pluginArtifactId = "";
        }

        String info = null;
        Document document = null;

        try {
            document = getPom(workspace);
        } catch (InvalidFileFormatException | IOException e) {
            e.printStackTrace();
        }

        XPath xPath = XPathFactory.newInstance().newXPath();
        XPathExpression expression;
        try {
            expression = xPath.compile("/project/build/plugins/plugin[artifactId = '"+pluginArtifactId+"']");
            info = expression.evaluate(document);
        } catch (XPathExpressionException e) {
            try {
                assert document != null;
                throw new InvalidFileFormatException(document.getBaseURI()
                        + " is not a valid POM file.");
            } catch (InvalidFileFormatException e1) {
                e1.printStackTrace();
            }
        }
        if (info == null){
            return false;
        }
        return info.length()> 0;
    }

    /**
     * get the pom file as a parsable document
     *
     * @param workspace
     * @return the pom file as a Document
     * @throws InvalidFileFormatException
     * @throws IOException
     */
    public Document getPom(FilePath workspace)
            throws InvalidFileFormatException, IOException {

        FilePath pom;
        pom = new FilePath(workspace, BUILD_FILE);
        Document pomDocument;
        try {
            pomDocument = pom.act(new FilePath.FileCallable<Document>() {
                public Document invoke(File pom, VirtualChannel channel)
                        throws IOException, InterruptedException {
                    try {
                        DocumentBuilder documentBuilder;
                        documentBuilder = DocumentBuilderFactory.newInstance()
                                .newDocumentBuilder();
                        return documentBuilder.parse(pom);
                    } catch (SAXException | ParserConfigurationException e) {
                        throw new InterruptedException(pom
                                .getAbsolutePath()
                                + " is not a valid POM file.");
                    }
                }
                public void checkRoles(RoleChecker arg0) throws SecurityException {
                }
            });
        } catch (InterruptedException e) {
            throw new InvalidFileFormatException(e.getMessage());
        }

        return pomDocument;
    }

}
