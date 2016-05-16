package MavenPluginTest.mavenspoon;

import hudson.FilePath;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import org.jenkinsci.remoting.RoleChecker;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.lang.AutoCloseable;

/**
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
     * @return module list
     */
    public String[] getInfos() throws IOException, InterruptedException, InvalidBuildFileFormatException {

        //infos projet
        listener.getLogger().println("\t Actifact ID        : " + pom.getInfo("artifactId"));
        listener.getLogger().println("\t Version            : " + pom.getInfo("version"));

        //get  modules list
        String modules = pom.getInfo("modules");
        listener.getLogger().println("\t Modules            : " + modules);


        listener.getLogger().println("\n PLUGINS: ");

            Boolean mc = pom.hasPlugin("maven-compiler-plugin");
        listener.getLogger().println("\t Has Maven Compiler : " + mc.toString());
        if (mc) {
            listener.getLogger().println("\t Java Version   : " + pom.getJavaVersion("maven-compiler-plugin"));
        }

        listener.getLogger().println("\t Has PMD            : " + pom.hasPlugin("pmd").toString());
        listener.getLogger().println("\t Has checkstyle     : " + pom.hasPlugin("checkstyle").toString());

        listener.getLogger().println("\t Git Commit id: " + build.getEnvironment(listener).get("GIT_COMMIT"));


        listener.getLogger().println("\t Workspace : " + build.getEnvironment(listener).get("WORKSPACE"));

        listener.getLogger().println("");
        //return a module list
        return modules.trim().split("\\n");
    }


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
            } catch (InvalidBuildFileFormatException e) {
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

        file = new File(build.getEnvironment(listener).get("WORKSPACE") + "/target/spoon-reports", "result-spoon.txt");
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

    public String getTestsInfos(String module)
            throws IOException, InterruptedException, InvalidBuildFileFormatException {

        int tests = 0, errors = 0, skipped = 0, failures = 0;
        FilePath dir;


        dir = new FilePath(workspace, module + "/target/surefire-reports");
        listener.getLogger().println(" NAME Parent DIR: "+module);
//        listener.getLogger().println(" DIR list : "+dir.list().toString());

        for (FilePath f : dir.list()) {
            if( f.getName().endsWith("txt")){
                break;
            }else {
//                listener.getLogger().println(" NAME: "+f.getName());
                Document doc = getTestsDocumentFile(f);

                //get test results parsing
                tests += getTestsResults("tests", doc);
                errors += getTestsResults("errors", doc);
                skipped += getTestsResults("skipped", doc);
                failures += getTestsResults("failures", doc);
            }
        }

        return "tests: " + tests + " errors: " + errors + " skipped: " + skipped + " failures: " + failures;
    }




    public int getTestsResults(String attribute, Document document) throws InvalidBuildFileFormatException, IOException {

        int info = -1;
//        Document document = null;
//
//        document = getTestsDocumentFile(workspace);

        XPath xPath = XPathFactory.newInstance().newXPath();
        XPathExpression expression;
        try {
//            +<testsuite name="tyre.commandline.CacheCreateRequirementsCommandTest" tests="3" skipped="0" errors="0" time="0.072" failures="0">
            expression = xPath.compile("/testsuite/@" + attribute);
            info = Integer.parseInt(expression.evaluate(document));

        } catch (XPathExpressionException e) {
            assert document != null;
            throw new InvalidBuildFileFormatException(document.getBaseURI()
                    + " is not a valid POM file.");
        }

        if (info == -1) {
            assert document != null;
            throw new InvalidBuildFileFormatException(
                    "No info information found in " + document.getBaseURI());
        }
        return info;
    }


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
                throw new InvalidBuildFileFormatException(e.getMessage());
            } catch (InvalidBuildFileFormatException e1) {
                listener.getLogger().println("InvalidBuildFileFormatException ");
                e1.printStackTrace();
            }
        }

        return testsDocument;
    }


}
