
/*
 * Copyright 2010 Walter Di Carlo.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package org.gradle.plugins.svnkit

import org.gradle.api.*
import org.gradle.api.plugins.*

import org.gradle.plugins.svnkit.svnclient.WorkingCopy;




class SvnKitPlugin implements Plugin<Project> {
	def void apply(Project project) {
		project.convention.plugins.svnkit = new GroovySvnKitPluginConvention(project)
		
		project.task('svnkit_properties') << { task ->
			println "SvnKit Plugin Properties"
			println "Svn Root: "+project.convention.plugins.svnkit.svn_root
			println "Svn Project: "+project.convention.plugins.svnkit.svn_project
			println "Svn Trunk: "+project.convention.plugins.svnkit.svn_trunk
			println "Svn Branches: "+project.convention.plugins.svnkit.svn_branches
			println "Svn Tags: "+project.convention.plugins.svnkit.svn_tags
			println "Svn Branch Name: "+project.convention.plugins.svnkit.svn_branch_name
			println "Svn Tag Name: "+project.convention.plugins.svnkit.svn_tag_name
			println "Svn Username: "+project.convention.plugins.svnkit.svn_username
			println "Svn Working Copy path: "+project.projectDir.path
			svnkit = task.project.convention.plugins.svnkit
			svn_wc = new WorkingCopy(
				svnkit.svn_root, 
				svnkit.svn_project, 
				svnkit.svn_trunk, 
				svnkit.svn_branches, 
				task.project.projectDir.path, 
				svnkit.svn_username, 
				svnkit.svn_password)
			println "Svn Working Copy URL: "+ svn_wc.getWCServerURL().toString()
			
		}

		project.task('svnkit_info') << { task ->
			svnkit = task.project.convention.plugins.svnkit
			assert svnkit.svn_root != null
			assert svnkit.svn_project != null
			svn_wc = new WorkingCopy(
				svnkit.svn_root, 
				svnkit.svn_project, 
				svnkit.svn_trunk, 
				svnkit.svn_branches, 
				task.project.projectDir.path, 
				svnkit.svn_username, 
				svnkit.svn_password)
			svn_wc.info()
		}
		
		project.task('svnkit_log') << { task ->
			svnkit = task.project.convention.plugins.svnkit
			assert svnkit.svn_root != null
			assert svnkit.svn_project != null
			svn_wc = new WorkingCopy(
				svnkit.svn_root, 
				svnkit.svn_project, 
				null, 
				svnkit.svn_branches, 
				task.project.projectDir.path, 
				svnkit.svn_username, 
				svnkit.svn_password)
			svn_wc.log()
		}
		project.task('svnkit_checkout_trunk') << { task ->
			svnkit = task.project.convention.plugins.svnkit
			assert svnkit.svn_root != null
			assert svnkit.svn_project != null
			svn_wc = new WorkingCopy(
				svnkit.svn_root, 
				svnkit.svn_project, 
				svnkit.svn_trunk, 
				svnkit.svn_branches, 
				task.project.projectDir.path, 
				svnkit.svn_username, 
				svnkit.svn_password)
			svn_wc.checkout(true)
		}
		project.task('svnkit_checkout_branch') << { task ->
			svnkit = task.project.convention.plugins.svnkit
			assert svnkit.svn_root != null
			assert svnkit.svn_project != null
			assert svnkit.svn_branch_name != null
			svn_wc = new WorkingCopy(
				svnkit.svn_root, 
				svnkit.svn_project, 
				svnkit.svn_branch_name, 
				svnkit.svn_branches, 
				task.project.projectDir.path, 
				svnkit.svn_username, 
				svnkit.svn_password)
			svn_wc.checkout(true)
		}
		project.task('svnkit_status') << { task ->
			svnkit = task.project.convention.plugins.svnkit
			assert svnkit.svn_root != null
			assert svnkit.svn_project != null
			svn_wc = new WorkingCopy(
				svnkit.svn_root, 
				svnkit.svn_project, 
				svnkit.svn_trunk, 
				svnkit.svn_branches, 
				task.project.projectDir.path, 
				svnkit.svn_username, 
				svnkit.svn_password)
			svn_wc.status()
		}
		project.task('svnkit_update') << { task ->
			svnkit = task.project.convention.plugins.svnkit
			assert svnkit.svn_root != null
			assert svnkit.svn_project != null
			svn_wc = new WorkingCopy(
				svnkit.svn_root, 
				svnkit.svn_project, 
				svnkit.svn_trunk, 
				svnkit.svn_branches, 
				task.project.projectDir.path, 
				svnkit.svn_username, 
				svnkit.svn_password)
			svn_wc.update()
		}
		project.task('svnkit_revert') << { task ->
			svnkit = task.project.convention.plugins.svnkit
			assert svnkit.svn_root != null
			assert svnkit.svn_project != null
			svn_wc = new WorkingCopy(
				svnkit.svn_root, 
				svnkit.svn_project, 
				svnkit.svn_trunk, 
				svnkit.svn_branches, 
				task.project.projectDir.path, 
				svnkit.svn_username, 
				svnkit.svn_password)
			svn_wc.revert()
		}
		project.task('svnkit_commit') << { task ->
			svnkit = task.project.convention.plugins.svnkit
			assert svnkit.svn_root != null
			assert svnkit.svn_project != null
			svn_wc = new WorkingCopy(
				svnkit.svn_root, 
				svnkit.svn_project, 
				svnkit.svn_trunk, 
				svnkit.svn_branches, 
				task.project.projectDir.path, 
				svnkit.svn_username, 
				svnkit.svn_password)
			if( svnkit.svn_message == null ){
				// input message
				println "\nPlease, enter message for the commit: "
				input = new Scanner(System.in)
				answer = input.nextLine()
				if( "".equals(answer) == true ) {
				  return
				}
				svnkit.svn_message = answer
			}
			
			svn_wc.commit(svnkit.svn_message)
		}
		project.task('svnkit_switchto_trunk') << { task ->
			svnkit = task.project.convention.plugins.svnkit
			assert svnkit.svn_root != null
			assert svnkit.svn_project != null
			svn_wc = new WorkingCopy(
				svnkit.svn_root, 
				svnkit.svn_project, 
				null, // use the working copy URL
				svnkit.svn_trunk, 
				task.project.projectDir.path, 
				svnkit.svn_username, 
				svnkit.svn_password)
			svn_wc.switchWorkingCopy()
		}
		project.task('svnkit_switchto_branch') << { task ->
			svnkit = task.project.convention.plugins.svnkit
			assert svnkit.svn_root != null
			assert svnkit.svn_project != null
			assert svnkit.svn_branch_name != null
			svn_wc = new WorkingCopy(
				svnkit.svn_root, 
				svnkit.svn_project, 
				null, // use the working copy URL
				svnkit.svn_branches + '/' + svnkit.svn_branch_name, 
				task.project.projectDir.path, 
				svnkit.svn_username, 
				svnkit.svn_password)
			svn_wc.switchWorkingCopy()
		}
		project.task('svnkit_switchto_tag') << { task ->
			svnkit = task.project.convention.plugins.svnkit
			assert svnkit.svn_root != null
			assert svnkit.svn_project != null
			assert svnkit.svn_tag_name != null
			svn_wc = new WorkingCopy(
				svnkit.svn_root, 
				svnkit.svn_project, 
				null, // use the working copy URL
				svnkit.svn_tags + '/' + svnkit.svn_tag_name, 
				task.project.projectDir.path, 
				svnkit.svn_username, 
				svnkit.svn_password)
			svn_wc.switchWorkingCopy()
		}
		project.task('svnkit_branch') << { task ->
			svnkit = task.project.convention.plugins.svnkit
			assert svnkit.svn_root != null
			assert svnkit.svn_project != null
			assert svnkit.svn_branch_name != null
			if( svnkit.svn_branch_name == null ){
				// input message
				println "\nPlease, enter branch name: "
				input = new Scanner(System.in)
				answer = input.nextLine()
				if( "".equals(answer) == true ) {
				  return
				}
				svnkit.svn_branch_name = answer
			}
			if( svnkit.svn_message == null ){
				// input message
				println "\nPlease, enter message for the commit: "
				input = new Scanner(System.in)
				answer = input.nextLine()
				if( "".equals(answer) == true ) {
				  return
				}
				svnkit.svn_message = answer
			}
			svn_wc = new WorkingCopy(
				svnkit.svn_root, 
				svnkit.svn_project, 
				null, // use the working copy URL
				svnkit.svn_branches + '/' + svnkit.svn_branch_name, 
				task.project.projectDir.path, 
				svnkit.svn_username, 
				svnkit.svn_password)
			svn_wc.copy();
		}
		project.task('svnkit_tag') << { task ->
			svnkit = task.project.convention.plugins.svnkit
			assert svnkit.svn_root != null
			assert svnkit.svn_project != null
			if( svnkit.svn_tag_name == null ){
				// input message
				println "\nPlease, enter tag name: "
				input = new Scanner(System.in)
				answer = input.nextLine()
				if( "".equals(answer) == true ) {
				  return
				}
				svnkit.svn_tag_name = answer
			}
			if( svnkit.svn_message == null ){
				// input message
				println "\nPlease, enter message for the commit: "
				input = new Scanner(System.in)
				answer = input.nextLine()
				if( "".equals(answer) == true ) {
				  return
				}
				svnkit.svn_message = answer
			}
			svn_wc = new WorkingCopy(
				svnkit.svn_root, 
				svnkit.svn_project, 
				null, // use the working copy URL
				svnkit.svn_tags + '/' + svnkit.svn_tag_name, 
				task.project.projectDir.path, 
				svnkit.svn_username, 
				svnkit.svn_password)
			svn_wc.copy();
		}
	}
}
