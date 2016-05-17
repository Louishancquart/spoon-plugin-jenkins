package MavenPluginTest.mavenspoon;

import hudson.FilePath;
import hudson.model.Run;
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
import java.io.*;

/**
 * Gather infos from pom files and write it on standard out and target/spoon-report/spoon-report.xml
 * Created by Louis Hancquart on 04.03.16.
 */
public class InfoGetter {
    private final POMGetter pom;
    private final TaskListener listener;
    private FilePath workspace;
    private Run<?, ?> build;

    public InfoGetter(POMGetter pomGetter, FilePath workspace, TaskListener listener, Run<?, ?> build) {
        this.pom = pomGetter;
        this.workspace = workspace;
        this.build = build;
        this.listener = listener;
    }

    /**
     * Get POM infos
     *
     * Gather from the  pom file :
     *  artifact id
     *  project version
     *  Git commit id
     *
     *  check presence of the plugins:
     *      maven-compiler-plugin dependency is used ( if yes : check java version )
     *      checkstyle
     *      PMD

     */
    public void printInfos() throws IOException, InterruptedException,InvalidFileFormatException {
        listener.getLogger().println("\t Actifact ID        : " + pom.getInfo("artifactId"));
        listener.getLogger().println("\t Version            : " + pom.getInfo("version"));

        //check plugins
        listener.getLogger().println("\n PLUGINS: ");
        Boolean mc = null;
        mc = pom.hasPlugin("maven-compiler-plugin");
        listener.getLogger().println("\t Has Maven Compiler : " + mc.toString());
        if (mc) {
            listener.getLogger().println("\t Java Version       : " + build.getEnvironment(listener).get("JAVA_HOME"));
        }
        listener.getLogger().println("\t Has PMD            : " + pom.hasPlugin("maven-pmd-plugin").toString());
        listener.getLogger().println("\t Has checkstyle     : " + pom.hasPlugin("maven-checkstyle-plugin").toString());
        listener.getLogger().println("\t Git Commit id      : " + build.getEnvironment(listener).get("GIT_COMMIT"));
        listener.getLogger().println("\t Workspace          : " + build.getEnvironment(listener).get("WORKSPACE"));


    }

    /**
     *Call printInfos() for each modules found in the pom file:
     *
     * @return module list
     * @throws IOException
     * @throws InvalidFileFormatException
     * @throws InterruptedException
     */
    public String[] getModules() throws IOException, InvalidFileFormatException, InterruptedException {
        //get  modules list
        listener.getLogger().println("\t MODULES            : ");
        String[] modules = pom.getInfo("modules").trim().split("\\n");

        for(String m : modules){
            m = m.replaceAll("\\s+","");
            listener.getLogger().println("\n\t                      " + m.toUpperCase()+"\n");
            pom.module = m+"/";
            this.printInfos();
        }
        pom.module = "";
        return modules;
    }

    /**
     * Writes in /target/spoon-reports/result-spoon.xml :
     *
     * Modules
     * Commit id version
     * Project spooned compiles
     * Project spooned tests run
     * Time to spoon
     *
     * @param modules
     * @throws IOException
     * @throws InterruptedException
     */
    public void writeToFile(String[] modules) throws IOException, InterruptedException {

        String idVersionGit = build.getEnvironment(listener).get("GIT_COMMIT");
        StringBuilder sb = new StringBuilder("<section name=\"\">\n" +
                "  <table>\n" +
                "    <tr>\n" +
                "       <td fontattribute=\"bold\">Module</td>\n" +
                "       <td fontattribute=\"bold\">Commit id version</td>\n" +
                "       <td fontattribute=\"bold\">Project spooned compiles</td>\n" +
                "       <td fontattribute=\"bold\">Project spooned tests run</td>\n" +
                "       <td fontattribute=\"bold\">Time to spoon</td>\n" +
                "    </tr>\n");




        for (String module : modules) {
            module = module.replaceAll("\\s+","");

            sb.append("    <tr>\n");
            sb.append("      <td>");
            sb.append(module);
            sb.append("</td>\n");
            sb.append("      <td>");
            sb.append(idVersionGit);
            sb.append("</td>\n");
            sb.append("      <td>");

            String temp;
            if(build.getResult() == null){
                temp = "KO";
            }else{
                temp = "OK";
            }
            sb.append(temp);
            sb.append("</td>\n");
            sb.append("      <td>");
            try {
                sb.append(getTestsInfos(module));
            } catch (InvalidFileFormatException e) {
                e.printStackTrace();
            }
            sb.append("</td>\n");
            sb.append("      <td>");
            sb.append(build.getDurationString().replaceAll("and counting", ""));
            sb.append("</td>\n");
            sb.append("    </tr>\n");
        }


        sb.append(" </table> \n </section>\n");


        File file = new File(build.getEnvironment(listener).get("WORKSPACE") + "/target/spoon-reports/");

        if (!file.mkdirs()) {
            listener.getLogger().println("dirs 'target/spoon-reports/' not created");
        }

        file = new File(build.getEnvironment(listener).get("WORKSPACE") + "/target/spoon-reports", "result-spoon.xml");
        listener.getLogger().println(" file:"+ file.getAbsolutePath());
        if (!file.createNewFile()) {
            listener.getLogger().println("file \"result-spoon.txt\" not created");
        }

        if (!file.exists()) {
            listener.getLogger().println("\n\n\n\n\n \n" + file.getAbsolutePath() + "\n\n");
            if (!file.createNewFile()) {
                listener.getLogger().println("file not created");
            }
        }

        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"))) {
            bw.write(sb.toString());
            bw.flush();

            listener.getLogger().println(sb);
        }
    }

    /**
     * Gather results of tests after compilation from  module/target/surefire-reports :
     * tests
     * errors
     * skipped
     * failures
     *
     * @param module
     * @return
     * @throws IOException
     * @throws InterruptedException
     * @throws InvalidFileFormatException
     */
    public String getTestsInfos(String module)
            throws IOException, InterruptedException, InvalidFileFormatException {

        int tests = 0, errors = 0, skipped = 0, failures = 0;
        FilePath dir;

        if(module != "") {
            dir = new FilePath(workspace, module + "/target/surefire-reports");
        }else{
            dir = new FilePath(workspace, "target/surefire-reports");
        }

        for (FilePath f : dir.list()) {
            if( f.getName().endsWith("txt")){
                break;
            }else {
                Document doc = getTestsDocumentFile(f);

                //get test results parsing
                tests += getTestResult("tests", doc);
                errors += getTestResult("errors", doc);
                skipped += getTestResult("skipped", doc);
                failures += getTestResult("failures", doc);
            }
        }

        return "tests: " + tests + " errors: " + errors + " skipped: " + skipped + " failures: " + failures;
    }


    /**
     * Get result from a test in input parameter according to a document file
     *
     * @param attribute
     * @param document
     * @return the int read from the document
     * @throws InvalidFileFormatException
     * @throws IOException
     */
    public int getTestResult(String attribute, Document document) throws InvalidFileFormatException, IOException {

        int info = -1;

        XPath xPath = XPathFactory.newInstance().newXPath();
        XPathExpression expression;

        try {
            expression = xPath.compile("/testsuite/@" + attribute);
            info = Integer.parseInt(expression.evaluate(document));

        } catch (XPathExpressionException e) {
            assert document != null;
            throw new InvalidFileFormatException(document.getBaseURI()
                    + " is not a valid POM file.");
        }

        if (info == -1) {
            assert document != null;
            throw new InvalidFileFormatException(
                    "No info information found in " + document.getBaseURI());
        }
        return info;
    }

    /**
     * Get the Test Document as a Document
     *
     * @param file
     * @return the test Document from the FilePath
     * @throws IOException
     */
    public Document getTestsDocumentFile(FilePath file) throws IOException {
        Document testsDocument = null;
        try {
            testsDocument = file.act(new FilePath.FileCallable<Document>() {
                public Document invoke(File file, VirtualChannel channel)
                        throws IOException, InterruptedException {

                    try {
                        DocumentBuilder documentBuilder;
                        documentBuilder = DocumentBuilderFactory.newInstance()
                                .newDocumentBuilder();
                        return documentBuilder.parse(file);

                    } catch (SAXException | ParserConfigurationException e) {
                        listener.getLogger().println(file
                                .getAbsolutePath()
                                + " is not a valid Test file.");
                        throw new InterruptedException(file
                                .getAbsolutePath()
                                + " is not a valid Test file.");
                    }
                }

                public void checkRoles(RoleChecker arg0) throws SecurityException {
                }

            });
        } catch (
                InterruptedException e
                )

        {
            try {
                throw new InvalidFileFormatException(e.getMessage());
            } catch (InvalidFileFormatException e1) {
                listener.getLogger().println("InvalidFileFormatException ");
                e1.printStackTrace();
            }
        }

        return testsDocument;
    }


}
