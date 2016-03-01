package MavenPluginTest.mavenspoon;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Builder;
import jenkins.tasks.SimpleBuildStep;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Sample {@link Builder}.
 * <p>
 * <p>
 * When the user configures the project and enables this builder,
 * {@link DescriptorImpl#newInstance(StaplerRequest)} is invoked
 * and a new {@link ProjectInfoGetter} is created. The created
 * instance is persisted to the project configuration XML by using
 * XStream, so this allows you to use instance fields
 * to remember the configuration.
 * <p>
 * <p>
 * When a build is performed, the {@link #perform} method will be invoked.
 *
 * @author Kohsuke Kawaguchi
 */
public class ProjectInfoGetter extends Builder implements SimpleBuildStep {

    // private final String name;

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public ProjectInfoGetter() {
//        this.name = name;

    }


//
//    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
//    @DataBoundConstructor
//    public POM_analyzer() {
//    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
//        return BuildStepMonitor.BUILD;
        return BuildStepMonitor.NONE;
    }


    /*


        /**
        * We'll use this from the <tt>config.jelly</tt>.
        * /
        public String getName() {
        return name;
        }

        */
    @Override
    public void perform(Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener) {

        // This is where you 'build' the project.
        POMGetter pomGetter = new POMGetter(workspace);

        //output POM infos
        listener.getLogger().println("\t Actifact ID        : " + pomGetter.getInfo("artifactId"));
        listener.getLogger().println("\t Version            : " + pomGetter.getInfo("version"));
        listener.getLogger().println("\t Modules            : " + pomGetter.getInfo("modules")); // liste !

        listener.getLogger().println("\n PLUGINS: ");
//        Analyser le pom du projet pour vérifier si le projet utilise le plugin checkstyle.
        Boolean mc = pomGetter.hasPlugin("maven-compiler-plugin");
        listener.getLogger().println("\t Has Maven Compiler : " + mc.toString());
        if (mc) {
            listener.getLogger().println("\t Java Version   : " + pomGetter.getJavaVersion("maven-compiler-plugin"));
        }

        listener.getLogger().println("\t Has PMD            : " + pomGetter.hasPlugin("PMD").toString());
        listener.getLogger().println("\t Has checkstyle     : " + pomGetter.hasPlugin("checkstyle").toString());

//        L'identifiant du commit Git du projet.
        if(listener.getLogger().toString().contains("GIT_COMMIT")){
            listener.getLogger().print("contains GIT");
        }
        try {
            listener.getLogger().print(build.getEnvironment(listener));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

//        try {
//
//            String content =
//                    "<section name=\"\">\n" +
//                    "  <table>\n" +
//                    "    <tr>\n" +
//                    "      <td fontattribute=\"bold\">Module</td>\n" +
//                    "      <td fontattribute=\"bold\">Commit id version</td>\n" +
//                    "    </tr>\n" +
//                    "    <tr>\n" +
//                    "      <td>wayback-cdx-server-core</td>\n" +
//                    "      <td>1ee2444a8e11cc78a16d07f43c28c522b56cf0ee</td>\n" +
//                    "    </tr>\n" +
//                    "    <tr>\n" +
//                    "      <td>wayback-core</td>\n" +
//                    "      <td>1ee2444a8e11cc78a16d07f43c28c522b56cf0ee</td>\n" +
//                    "    </tr>\n" +
//                    "  </table>\n" +
//                    "</section>";
//
//            File file = new File(workspace+"/target/spoon-reports/result-spoon.xml");
//
//            // if file doesnt exists, then create it
//            if (!file.exists()) {
//               System.out.println("\n\n\n\n\n \n"+file.toURI().toString());
//                if(!file.createNewFile()){
//                    listener.getLogger().println("file not created");
//                }
//            }
//
//            FileWriter fw = new FileWriter(file.getAbsoluteFile());
//            BufferedWriter bw = new BufferedWriter(fw);
//            bw.write(content);
//            bw.close();
//
//            System.out.println("Done");
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }


    /**
     * Descriptor for {@link ProjectInfoGetter}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     * <p>
     * <p>
     * See <tt>src/main/resources/hudson/plugins/hello_world/ProjectInfoGetter/*.jelly</tt>
     * for the actual HTML fragment for the configuration screen.
     */
    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        /**
         * In order to load the persisted global configuration, you have to
         * call load() in the constructor.
         */
        public DescriptorImpl() {
            load();
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
            return "Analyse POM file";
        }

    }
}

