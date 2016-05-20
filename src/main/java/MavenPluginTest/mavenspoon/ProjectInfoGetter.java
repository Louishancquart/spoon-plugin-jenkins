package MavenPluginTest.mavenspoon;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildWrapperDescriptor;
import jenkins.tasks.SimpleBuildWrapper;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;

/**
 * Gather infos from the build and apply Spoon to the job
 */
public class ProjectInfoGetter extends SimpleBuildWrapper {

//    private  boolean debug =false;
//    private  int compliance = 1;
//    private  boolean noClasspath = false;
//    private  boolean noCopyResources = false;

    @DataBoundConstructor
    public ProjectInfoGetter( boolean debug, int compliance, boolean noClasspath, boolean noCopyResources, String processor1, String processor2) {
//        this.debug = debug;
//        this.compliance = compliance;
//        this.noClasspath = noClasspath;
//        this.noCopyResources = noCopyResources;
//        this.processor1 = processor1;
//        this.processor2 = processor2;
    }

//
//    @Override
//    public BuildStepMonitor getRequiredMonitorService() {
//        return BuildStepMonitor.BUILD;
//    }


//    /**
//     * method called to start the plugin: contain the main steps of the plugin behaviour.
//     *
//     * @param build
//     * @param workspace
//     * @param launcher
//     * @param listener
//     * @throws IOException
//     * @throws InterruptedException
//     */
//    @Override
//    public void perform(Run<?, ?> build, @Nonnull FilePath workspace, @Nonnull Launcher launcher, @Nonnull TaskListener listener) throws IOException, InterruptedException {
//
//
//
//    }


    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    @Override
    public void setUp(Context context, Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener, EnvVars envVars) throws IOException, InterruptedException {

        //get pre spoon infos about the build
        InfoGetter infos = new InfoGetter(new POMGetter(workspace, listener), workspace, listener, build);
        String[] modules = new String[0];

        try {
            modules = infos.printInfos();
        } catch (InvalidFileFormatException e) {
            e.printStackTrace();
            listener.getLogger().println("\n\n info Reading  Failed ! \n\n");
        }

        infos.writeToFile(modules);

        //insert spoon-plugin in the pom of the project
        POMModifier pm = new POMModifier(new POMGetter(workspace, listener), listener, workspace, build);
        try {
            if (!pm.insertSpoonPlugin()
            ){
                listener.getLogger().println("\n\n insertion Failed ! \n\n");
            }
        } catch (ParserConfigurationException | TransformerException | SAXException | InvalidFileFormatException e) {
            e.printStackTrace();
        }

        context.setDisposer(new Disposer() {
            @Override
                public void tearDown(Run<?, ?> build, FilePath workspace,Launcher launcher, TaskListener listener) throws IOException, InterruptedException {
                    //get pre spoon infos about the build
                    InfoGetter infos = new InfoGetter(new POMGetter(workspace, listener), workspace, listener, build);
                    String[] modules = new String[0];

                    try {
                        modules = infos.printInfos();
                    } catch (InvalidFileFormatException e) {
                        e.printStackTrace();
                        listener.getLogger().println("\n\n info Reading  Failed ! \n\n");
                    }

                    infos.writeToFileAfterBuild(modules);
            }


        });

    }


//    /**
//     * Getter used by <tt>config.jelly</tt>.
//     */
//    public int getCompliance() {
//        return compliance;
//    }
//
//    /**
//     * Getter used by <tt>config.jelly</tt>.
//     */
//    public boolean isDebug() {
//        return debug;
//    }
//
//    /**
//     * Getter used by <tt>config.jelly</tt>.
//     */
//    public boolean isNoClasspath() {
//        return noClasspath;
//    }
//
//    /**
//     * Getter used by <tt>config.jelly</tt>.
//     */
//    public boolean isNoCopyResources() {
//        return noCopyResources;
//    }
//





    @Extension
    public static final class DescriptorImpl extends BuildWrapperDescriptor {

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

        @Override
        public boolean isApplicable(AbstractProject<?, ?> abstractProject) {
            return true;
        }


        @Override
        public boolean configure(StaplerRequest staplerRequest, JSONObject json) throws FormException {

////            json = json.getJSONObject("spoon");
//            this.debug = json.getBoolean("debug");
//            noCopyResources = json.getBoolean("noCopyResources");
//            noClasspath = json.getBoolean("noClasspath");
//            compliance = json.getInt("compliance");


            save();
            return true; // indicate that everything is good so far
        }


        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
            return "Spoon the Project";
        }


    }
}

