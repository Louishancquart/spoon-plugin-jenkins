package MavenPluginTest.mavenspoon;

import hudson.FilePath;
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
 * Created by Louis on 01.03.16.
 */
public class POMGetter {

    private final FilePath workspace;
    private static final String BUILD_FILE = "pom.xml";

    public POMGetter(FilePath workspace) {
        this.workspace = workspace;
    }

    public String getInfo(String expression_to_compile) {


        String info = null;

        Document document = null;
        try {
            document = getPom(workspace);
        } catch (InvalidBuildFileFormatException | IOException e) {
            e.printStackTrace();
        }

        XPath xPath = XPathFactory.newInstance().newXPath();
        XPathExpression expression;
        try {
            expression = xPath.compile("/project/"+expression_to_compile);
            info = expression.evaluate(document);

        } catch (XPathExpressionException e) {
            try {
                assert document != null;
                throw new InvalidBuildFileFormatException(document.getBaseURI()
                        + " is not a valid POM file.");
            } catch (InvalidBuildFileFormatException e1) {
                e1.printStackTrace();
            }


        }

        if (info == null || info.length() == 0) {
            try {
                assert document != null;
                throw new InvalidBuildFileFormatException(
                        "No info information found in " + document.getBaseURI());
            } catch (InvalidBuildFileFormatException e) {
                e.printStackTrace();
            }
        }
        return info;
    }


    public Boolean hasPlugin(String pluginArtifactId) {

        String info = null;
        Document document = null;

        try {
            document = getPom(workspace);
        } catch (InvalidBuildFileFormatException | IOException e) {
            e.printStackTrace();
        }

        XPath xPath = XPathFactory.newInstance().newXPath();
        XPathExpression expression;
        try {
            expression = xPath.compile("/project/build/plugins/plugin[artifactId='pluginArtifactId']");
            info = expression.evaluate(document);

        } catch (XPathExpressionException e) {
            try {
                assert document != null;
                throw new InvalidBuildFileFormatException(document.getBaseURI()
                        + " is not a valid POM file.");
            } catch (InvalidBuildFileFormatException e1) {
                e1.printStackTrace();
            }


        }

        if (info == null || info.length() == 0) {
            try {
                assert document != null;
                throw new InvalidBuildFileFormatException(
                        "No info information found in " + document.getBaseURI());
            } catch (InvalidBuildFileFormatException e) {
                e.printStackTrace();
            }
        }
        return info.contains(pluginArtifactId);
    }


    public String getJavaVersion(String pluginArtifactId) {

        String info = null;
        Document document = null;

        try {
            document = getPom(workspace);
        } catch (InvalidBuildFileFormatException | IOException e) {
            e.printStackTrace();
        }

        XPath xPath = XPathFactory.newInstance().newXPath();
        XPathExpression expression;
        try {
            expression = xPath.compile("/project/build/plugins/plugin[artifactId='pluginArtifactId']/configuration/version");
            info = expression.evaluate(document);

        } catch (XPathExpressionException e) {
            try {
                assert document != null;
                throw new InvalidBuildFileFormatException(document.getBaseURI()
                        + " is not a valid POM file.");
            } catch (InvalidBuildFileFormatException e1) {
                e1.printStackTrace();
            }


        }

        if (info == null || info.length() == 0) {
            try {
                assert document != null;
                throw new InvalidBuildFileFormatException(
                        "No info information found in " + document.getBaseURI());
            } catch (InvalidBuildFileFormatException e) {
                e.printStackTrace();
            }
        }
        return info;
    }

    private Document getPom(FilePath workspace)
            throws InvalidBuildFileFormatException, IOException {

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
            throw new InvalidBuildFileFormatException(e.getMessage());
        }

        return pomDocument;
    }



    private static class InvalidBuildFileFormatException extends Exception {
        public InvalidBuildFileFormatException(String message) {
            super(message);
        }
    }
}
