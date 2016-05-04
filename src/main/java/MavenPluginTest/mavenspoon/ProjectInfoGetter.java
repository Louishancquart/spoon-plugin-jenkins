package MavenPluginTest.mavenspoon;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.console.AnnotatedLargeText;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import hudson.tasks.*;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.remoting.RoleChecker;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.annotation.Nonnull;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

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
public class ProjectInfoGetter extends Publisher implements SimpleBuildStep {

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
        return BuildStepMonitor.BUILD;
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
    public void perform( Run<?, ?> build, @Nonnull FilePath workspace, @Nonnull Launcher launcher, @Nonnull TaskListener listener) throws IOException, InterruptedException {



        //get pre spoon infos about the build
        InfoGetter infos = new InfoGetter(new POMGetter(workspace), listener, build);
        String[] modules = infos.getInfos();
        infos.writeToFile(modules);

        //insert spoon-plugin in the pom of the project
        POMModifier pm = new POMModifier(new POMGetter(workspace), listener, workspace, build);
        try {
            if (!pm.insertSpoonPlugin()) {
                listener.getLogger().println("\n\n insertion Failed ! \n\n");
            }
        } catch (ParserConfigurationException | POMGetter.InvalidBuildFileFormatException | TransformerException | SAXException e) {
            e.printStackTrace();
        }


//        listener.getLogger().println("\n\n BUILD succeed : " + build.getResult().toString());
//
//    listener.getLogger().println("\n\n BUILD time : " + build.getDurationString());
//    listener.getLogger().println("\n\n BUILD tests : " + build.getLogText().toString());


//        build.getResult();
//        build.getDurationString();

        //take the time
        // build.doBuildTimestamp();


        // run the project

        // take the time
        // build.doBuildTimestamp();

       //publish infos
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
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

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

