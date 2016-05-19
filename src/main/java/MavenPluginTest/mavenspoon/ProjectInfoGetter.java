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
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.xml.sax.SAXException;

import javax.annotation.Nonnull;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;

/**
 * Gather infos from the build and apply Spoon to the job
 */
public class ProjectInfoGetter extends Builder implements SimpleBuildStep {

    private  boolean debug =false;
    private  int compliance = 1;
    private  boolean noClasspath = false;
    private  boolean noCopyResources = false;
    private  String processor1 ="proc1";
    private  String processor2 = "proc2";

    @DataBoundConstructor
    public ProjectInfoGetter( boolean debug, int compliance, boolean noClasspath, boolean noCopyResources, String processor1, String processor2) {
        this.debug = debug;
        this.compliance = compliance;
        this.noClasspath = noClasspath;
        this.noCopyResources = noCopyResources;
        this.processor1 = processor1;
        this.processor2 = processor2;
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
            modules = infos.printInfos();
        } catch (InvalidFileFormatException e) {
            e.printStackTrace();
            listener.getLogger().println("\n\n info Reading  Failed ! \n\n");
        }

        infos.writeToFile(modules);


        //insert spoon-plugin in the pom of the project
        POMModifier pm = new POMModifier(new POMGetter(workspace), listener, workspace, build);
        try {
            if (!pm.insertSpoonPlugin(
                    String.valueOf(getDescriptor().isDebug()),
                    String.valueOf(getDescriptor().getCompliance()),
                    String.valueOf(getDescriptor().isNoClasspath()),
                    String.valueOf(getDescriptor().isNoCopyResources())
                    )){
                listener.getLogger().println("\n\n insertion Failed ! \n\n");
            }
        } catch (ParserConfigurationException | TransformerException | SAXException | InvalidFileFormatException e) {
            e.printStackTrace();
        }

        
    }


    @Override
    public DescriptorImpl getDescriptor() {
        // see Descriptor javadoc for more about what a descriptor is.
        return (DescriptorImpl)super.getDescriptor();
    }






    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        private boolean debug;
        private boolean noCopyResources;
        private boolean noClasspath;
        private int compliance;




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

        @Override
        public boolean configure(StaplerRequest staplerRequest, JSONObject json) throws FormException {
            debug = json.getBoolean("isDebug");
            noCopyResources = json.getBoolean("isNoCopyResources");
            noClasspath = json.getBoolean("isNoClasspath");
            compliance = json.getInt("getCompliance");

            save();
            return true; // indicate that everything is good so far
        }


        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
            return "Spoon the Project";
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

    }
}

