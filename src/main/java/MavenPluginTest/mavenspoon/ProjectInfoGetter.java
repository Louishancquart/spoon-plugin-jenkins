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
    @Override
    public void perform(Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener) {
        // This is where you 'build' the project.
        // Since this is a dummy, we just say 'hello world' and call that a build.

        // This also shows how you can consult the global configuration of the builder
        listener.getLogger().println("\n\nBonjour!!!!!!!!!!!!!!!!!!\n\n");

        POMGetter pomGetter = new POMGetter(workspace);


//
//      l'artifact id.
//      version du projet.
//      la liste des modules du projet (si le projet n'est pas multi module, avoir une liste avec un seul élément)
//        Analyser le pom du projet pour connaitre la version de Java qu'il utilise (si le plugin maven-compiler-plugin est utilisé).
//        Analyser le pom du projet pour vérifier si le projet utilise le plugin checkstyle.
//        Analyser le pom du projet pour vérifier si le projet utilise le plugin PMD.
//        L'identifiant du commit Git du projet.


        listener.getLogger().println("\n\n VERSION : " + pomGetter.getInfo("version") + "!\n\n");
    }

    // Overridden for better type safety.
    // If your plugin doesn't really define any property on Descriptor,
    // you don't have to do this.
//    @Override
//    public DescriptorImpl getDescriptor() {
//        return (DescriptorImpl)super.getDescriptor();
//    }

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
//        /**
//         * To persist global configuration information,
//         * simply store it in a field and call save().
//         *
//         * <p>
//         * If you don't want fields to be persisted, use <tt>transient</tt>.
//         */
//        private boolean useFrench;

        /**
         * In order to load the persisted global configuration, you have to
         * call load() in the constructor.
         */
        public DescriptorImpl() {
            /*load();*/
        }

//        /**
//         * Performs on-the-fly validation of the form field 'name'.
//         *
//         * @param value
//         * @return
//         *      Indicates the outcome of the validation. This is sent to the browser.
//         *      <p>
//         *      Note that returning {@link FormValidation#error(String)} does not
//         *      prevent the form from being saved. It just means that a message
//         *      will be displayed to the user.
//         */
//        public FormValidation doCheckName(@QueryParameter String value)
//                throws IOException, ServletException {
//            if (value.length() == 0)
//                return FormValidation.error("Please set a name");
//            if (value.length() < 4)
//                return FormValidation.warning("Isn't the name too short?");
//            return FormValidation.ok();
//        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types 
            return true;
        }

        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
            return "Analyse POM file";
        }

//        @Override
//        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
//            // To persist global configuration information,
//            // set that to properties and call save().
//            useFrench = formData.getBoolean("useFrench");
//            // ^Can also use req.bindJSON(this, formData);
//            //  (easier when there are many fields; need set* methods for this, like setUseFrench)
//            save();
//            return super.configure(req,formData);
//        }

        /**
         * This method returns true if the global configuration says we should speak French.
         *
         * The method name is bit awkward because global.jelly calls this method to determine
         * the initial state of the checkbox by the naming convention.
         */
//        public boolean getUseFrench() {
//            return useFrench;
//        }
    }
}

