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
import org.xml.sax.SAXException;

import javax.annotation.Nonnull;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;

/**
 * Gather infos from the build and apply Spoon to the job
 */
public class ProjectInfoGetter extends Builder implements SimpleBuildStep {

    private boolean debug;
    private int compliance;
    private boolean noClasspath;
    private boolean noCopyResources;
    private String processors;
    private String processors1;

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public ProjectInfoGetter(boolean debug, int compliance, boolean noClasspath, boolean noCopyResources, String processors, String processors1) {
        this.debug = debug;
        this.compliance = compliance;
        this.noClasspath = noClasspath;
        this.noCopyResources = noCopyResources;
        this.processors = processors;
        this.processors1 = processors1;
    }


    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }


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
    public void perform(Run<?, ?> build, @Nonnull FilePath workspace, @Nonnull Launcher launcher, @Nonnull TaskListener listener) throws IOException, InterruptedException {

        //get pre spoon infos about the build
        InfoGetter infos = new InfoGetter(new POMGetter(workspace), workspace, listener, build);
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
            if (!pm.insertSpoonPlugin( debug, compliance, noClasspath, noCopyResources, processors,processors1)) {
                listener.getLogger().println("\n\n insertion Failed ! \n\n");
            }
        } catch (ParserConfigurationException | TransformerException | SAXException | InvalidFileFormatException e) {
            e.printStackTrace();
        }
    }



    /**
     * Getter used by <tt>config.jelly</tt>.
     */
    public int getCompliance() {
        return compliance;
    }

    /**
     * Getter used by <tt>config.jelly</tt>.
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * Getter used by <tt>config.jelly</tt>.
     */
    public boolean isNoClasspath() {
        return noClasspath;
    }

    /**
     * Getter used by <tt>config.jelly</tt>.
     */
    public boolean isNoCopyResources() {
        return noCopyResources;
    }

    /**
     * Getter used by <tt>config.jelly</tt>.
     */
    public String getProcessors() {
        return processors;
    }

    /**
     * Getter used by <tt>config.jelly</tt>.
     */
    public String getProcessors1() {
        return processors1;
    }


    @Extension
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

