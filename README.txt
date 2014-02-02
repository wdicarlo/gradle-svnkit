This is the README.txt file for the SvnKit Gradle plugin.

Version: 0.5
Author: Walter Di Carlo


___DESCRIPTION___

The SvnKit Gradle plugin allows Gradle scripts to interact with Subversion repositories.
This is possible thanks to the SVNKit library (http://svnkit.com/) which is a pure Java Subversion client library. 

Please, note that this software is in it initial state of development. 

Any suggestion/question can be sent to the address walter@di-carlo.it

___INSTALLATION___

The following are the components used to develop the plugin.
- JRE 1.6 (used the new Console class to request the password)
- Gradle 0.9 rc1
- SvnKit 1.3.4

Build gradle-svnkit.jar from the Gradle SvnKit project.

Once, all required components have been downloaded and installed, then:
- Copy the gradle-svnkit.jar and the svnkit.jar in the plugins folder of gradle (<gradle-root>/lib/plugins)
- Add the plugin to the projects that should be stored in a Subversion repository
	apply plugin: 'svnkit'
- Define the properties of the project for the Subversion account
	svnkit {
		svn_root = 'https://x.y.w.z/svn/'
		svn_project = 'myprj'
		svn_username = 'myuser'
	}
- Run gradle with one of the tasks added by the SvnKit plugin. Here is an example of output 

	gradle -t
	:report

	------------------------------------------------------------
	Root Project
	------------------------------------------------------------
	Default tasks: svnkit_properties

	Tasks
	-----
	:svnkit_branch
	:svnkit_checkout_branch
	:svnkit_checkout_trunk
	:svnkit_commit
	:svnkit_info
	:svnkit_log
	:svnkit_properties
	:svnkit_revert
	:svnkit_status
	:svnkit_switchto_branch
	:svnkit_switchto_tag
	:svnkit_switchto_trunk
	:svnkit_tag
	:svnkit_update

	BUILD SUCCESSFUL
	 
___SVNKIT_PLUGIN_TASKS___

Here is a brief description of each task

	:svnkit_branch		Branch the WC to the path in svn_branch 
	:svnkit_checkout_branch Checkout from the path in svn_branch
	:svnkit_checkout_trunk	Checkout from the path in svn_trunk
	:svnkit_commit		Commit the changes of the WC
	:svnkit_info		List information of the WC
	:svnkit_log		List the log messages of the WC
	:svnkit_properties	List the properties of the SvnKit plugin
	:svnkit_revert		Remove all changes
	:svnkit_status		Show the status of the WC	
	:svnkit_switchto_branch	Switch the WC to the path in svn_branch
	:svnkit_switchto_tag	Switch the WC to the path in svn_tag
	:svnkit_switchto_trunk	Switch the WC to the path in svn_trunk
	:svnkit_tag		Copy the WC to the path in svn_tag
	:svnkit_update		Update the WC

___KNOW_LIMITATIONS___

- Not all tasks and protocols have been tested 
- The password is requested even if it is not necessary
- ...
 
___TODO___

- Refactor the code to make it more efficient
	* The instance of the WorkingCopy could be shared between the tasks
- Test other protocols (svn, svn+ssh, etc)
- Improve the account management
- add task to generate the workspace.csv file from current configuration
- ...

___LICENSING___

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 

