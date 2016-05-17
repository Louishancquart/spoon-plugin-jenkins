package MavenPluginTest.mavenspoon;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.*;
import jenkins.tasks.SimpleBuildStep;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.xml.sax.SAXException;

import javax.annotation.Nonnull;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
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

    /**
     * method called to start the plugin: contain the main steps of the plugin behaviour.
     *
     * @param build
     * @param workspace
     * @param launcher
     * @param listener
     * @throws IOException
     * @throws InterruptedException
     */
    @Override
    public void perform( Run<?, ?> build, @Nonnull FilePath workspace, @Nonnull Launcher launcher, @Nonnull TaskListener listener) throws IOException, InterruptedException {

        //get pre spoon infos about the build
        InfoGetter infos = new InfoGetter(new POMGetter(workspace),workspace, listener, build);
        String[] modules = new String[0];

        try {
            infos.printInfos();
            modules = infos.getModules();
        } catch (InvalidFileFormatException e) {
            e.printStackTrace();
            listener.getLogger().println("\n\n info Reading  Failed ! \n\n");
        }

        infos.writeToFile(modules);

        //insert spoon-plugin in the pom of the project
        POMModifier pm = new POMModifier(new POMGetter(workspace), listener, workspace, build);
        try {
            if (!pm.insertSpoonPlugin()) {
                listener.getLogger().println("\n\n insertion Failed ! \n\n");
            }
        } catch (ParserConfigurationException | TransformerException | SAXException | InvalidFileFormatException e) {
            e.printStackTrace();
        }
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
            return "Spoon the Project";
        }

    }
}

