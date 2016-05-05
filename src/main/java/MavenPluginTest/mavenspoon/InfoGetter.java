package MavenPluginTest.mavenspoon;

import hudson.model.Run;
import hudson.model.TaskListener;

import java.io.*;
import java.lang.AutoCloseable;

/**
 * Created by Louis on 04.03.16.
 */
public class InfoGetter {
    private final POMGetter pom;
    private final TaskListener listener;
    private Run<?, ?>  build;

    public InfoGetter(POMGetter pomGetter, TaskListener listener, Run<?, ?> build) {
        this.pom = pomGetter;
        this.build = build;
        this.listener = listener;
    }

    /**
     * Get POM infos
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


        listener.getLogger().println("\t Workspace : " +build.getEnvironment(listener).get("WORKSPACE"));


        //return a module list
        return modules.split("\\\\r?\\\\n");
    }



    public void writeToFile(String[] modules) throws IOException, InterruptedException {

        String idVersionGit = null;
        idVersionGit = build.getEnvironment(listener).get("GIT_COMMIT");


        StringBuffer sb = new StringBuffer();

        sb.append("<section name=\"\">\n" +
                "  <table>\n" +
                "    <tr>\n" +
                "       <td fontattribute=\"bold\">Module</td>\n" +
                "       <td fontattribute=\"bold\"></td>\n" +
                "       <td fontattribute=\"bold\">Project spooned compiles</td>\n" +
                "       <td fontattribute=\"bold\">Project spooned tests run</td>\n" +
                "       <td fontattribute=\"bold\">Time to spoon</td>\n" +
                "    </tr>\n");


        //TODO: add proper info
        for (String module : modules) {
            sb.append("    <tr>\n" +
                    "      <td>" + module + "</td>\n" +
                    "      <td>" + idVersionGit + "</td>\n" +
                    "      <td>" + idVersionGit + "</td>\n" +
                    "      <td>" + idVersionGit + "</td>\n" +
                    "      <td>" + idVersionGit + "</td>\n" +
                    "    </tr>\n");
        }


        sb.append(" </table> \n </section>\n");


        File file = new File("target/spoon-reports/");

        try ( BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"))) {

            if (!file.mkdirs()) {
                listener.getLogger().println("dirs 'target/spoon-reports/' not created");
            }

            file = new File(build.getEnvironment(listener).get("WORKSPACE")+"/target/spoon-reports/", "result-spoon.txt");
            if (!file.createNewFile()) {
                listener.getLogger().println("file \"result-spoon.txt\" not created");
            }

            if (!file.exists()) {
                listener.getLogger().println("\n\n\n\n\n \n" + file.getAbsolutePath() + "\n\n");
                if (!file.createNewFile()) {
                    listener.getLogger().println("file not created");
                }
            }



            bw.write(sb.toString());
            bw.flush();

            listener.getLogger().println(sb);
        }
    }

}
