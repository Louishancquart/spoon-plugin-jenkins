# spoon-plugin-jenkins

##requirements:
Java jdk 1.7 or newer 

Maven3

Jenkins 1.5


## Installation

#1 Download or clone from the repository url:
git clone https://github.com/Louishancquart/spoon-plugin-jenkins.git

#2 Compile the project
cd spoon-plugin-jenkins

mvn install

#3 Upload the plugin into your jenkins


##via Jenkins interface:
In the Jenkins interface, go to "Manage Jenkins -> Manage Plugins -> Advanced"

Then "Upload Plugin" and select the "spoon-jenkins.hpi" file from "spoon-plugin-jenkins/target"


if you have an older version of the plugin, you might need to restart Jenkins.

#How to use the plugin:
1/ In the job configuration:

check "Spoon the Project"

Add parameters if needed

Save


2/ Run the configured job