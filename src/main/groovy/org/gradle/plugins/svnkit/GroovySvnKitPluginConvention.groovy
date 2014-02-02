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

import org.gradle.api.Project
import org.gradle.plugins.svnkit.svnclient.WorkingCopy;

class GroovySvnKitPluginConvention {
	String svn_root = null
	String svn_project = null
	String svn_trunk = "trunk"
	String svn_branches = "branches"
	String svn_tags = "tags"
	String svn_message = null
	String svn_username = ""
	String svn_password = "" // TODO: do not store the password
	String svn_branch_name
	String svn_tag_name
	
	private Project _project = null
	
	public GroovySvnKitPluginConvention( final Project project ){
		_project = project 
	}
	
	public Project getProject() {
		return _project
	}
	
	public void copy( final String srcPath, final String dstPath, final boolean isMove ){
		assert srcPath != null
		assert dstPath != null
		
		
		def svn_wc = new WorkingCopy(
			svn_root,
			svn_project,
			srcPath, // from path
			dstPath, // to path
			getProject().projectDir.path,
			svn_username,
			svn_password)
		
		svn_wc.copy(isMove)
	}

	  def svnkit(Closure closure) {
		  closure.delegate = this
		  closure()
	  }
	
}
