package MavenPluginTest.mavenspoon;

import hudson.model.Run;
import hudson.model.TaskListener;

import java.io.*;

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
    public String[] getInfos(){

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

        try {
            listener.getLogger().println("\t Git Commit id: " + build.getEnvironment(listener).get("GIT_COMMIT"));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //return a module list
        return modules.split("\\\\r?\\\\n");
    }



    public void writeToFile(String[] modules) throws IOException {

        String idVersionGit = null;
        try {
            idVersionGit = build.getEnvironment(listener).get("GIT_COMMIT");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        StringWriter sw = null;
        BufferedWriter bw = null;

        StringBuilder str = new StringBuilder("<section name=\"\">\n" +
                        "  <table>\n"+
                "    <tr>\n" +
                "      <td fontattribute=\"bold\">Module</td>\n" +
                "      <td fontattribute=\"bold\"></td>\n" +
                "    </tr>\n");


        for ( String module : modules){
            str.append("    <tr>\n" +
                    "      <td>"+module+"</td>\n" +
                    "      <td>"+idVersionGit+"</td>\n" +
                    "    </tr>\n");
        }

        str.append(" </table> \n </section>\n");


        try {

            File file = new File("target/spoon-reports/");
            if (!file.mkdirs()) {
                listener.getLogger().println("dirs 'target/spoon-reports/' not created");
            }

            file = new File("target/spoon-reports/","result-spoon.txt");
            if (!file.createNewFile()) {
                listener.getLogger().println("file \"result-spoon.txt\" not created");
            }

            if (!file.exists()) {
                listener.getLogger().println("\n\n\n\n\n \n" + file.getAbsolutePath().toString() + "\n\n");
                if (!file.createNewFile()) {
                    listener.getLogger().println("file not created");
                }
            }

            sw = new StringWriter();
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
            StringBuffer sb = sw.getBuffer();
//            bw.write(sb.toString());
            bw.write(str.toString());
            bw.flush();

//            BufferedWriter bwriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
//            bwriter.write(str.toString());

            listener.getLogger().println(sb);

//            //Close writer
//            writer.close();

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
