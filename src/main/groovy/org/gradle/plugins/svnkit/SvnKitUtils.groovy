package org.gradle.plugins.svnkit

import org.gradle.plugins.svnkit.svnclient.WorkingCopy;


class SvnKitUtils {
	private SvnKitUtils() {} // cannot intantiate this class
	
	public static void copy( final GroovySvnKitPluginConvention svnkit, final String srcPath, final String dstPath, final boolean isMove ){
		assert svnkit != null
		assert srcPath != null
		assert dstPath != null
		
		
		def svn_wc = new WorkingCopy(
			svnkit.svn_root,
			svnkit.svn_project,
			srcPath, // from path
			dstPath, // to path
			svnkit.getProject().projectDir.path,
			svnkit.svn_username,
			svnkit.svn_password)
		
		svn_wc.copy(isMove)

	}
}
