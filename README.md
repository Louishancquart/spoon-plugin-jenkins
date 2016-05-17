# spoon-plugin-jenkins

##requirements:
Java jdk 1.7 or newer 

Maven3

Jenkins 1.5

## Installation

#1 Download or clone the repository
git clone https://github.com/Louishancquart/spoon-plugin-jenkins.git

#2 Compile the project 
cd spoon-plugin-jenkins

mvn install

#3 Upload the plugin into your jenkins

##commandline:
cd ..

cp -rf spoon-plugin-jenkins $JENKINS_HOME/plugins

##via Jenkins interface:
In the Jenkins interface, go to "Manage Jenkins -> Manage Plugins -> Advanced"

Then "Upload Plugin" and select the "spoon-jenkins.hpi" file from "spoon-plugin-jenkins/target"


if you have an older version of the plugin, you might need to restart Jenkins.

#How to use the plugin:
1/ In the job configuration: add a post-build process and select : "Spoon the Project"

2/ Run the configured job
