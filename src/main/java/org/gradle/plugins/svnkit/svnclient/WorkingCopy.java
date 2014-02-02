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
 * ====================================================================
 * Copyright (c) 2004-2010 TMate Software Ltd.  All rights reserved.
 *
 * This software is licensed as described in the file COPYING, which
 * you should have received as part of this distribution.  The terms
 * are also available at http://svnkit.com/license.html
 * If newer versions of this license are posted there, you may use a
 * newer version instead, at your option.
 * ====================================================================
 */
package org.gradle.plugins.svnkit.svnclient;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.internal.util.SVNPathUtil;
import org.tmatesoft.svn.core.internal.wc.DefaultSVNOptions;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.ISVNEventHandler;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNCopySource;
import org.tmatesoft.svn.core.wc.SVNInfo;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

public class WorkingCopy {

    private static SVNClientManager ourClientManager = null;
    private static ISVNEventHandler myCommitEventHandler = null;
    private static ISVNEventHandler myUpdateEventHandler = null;
    private static ISVNEventHandler myWCEventHandler = null;
	private SVNURL repositoryURL = null;
	private String myWorkingCopyPath = null;
	private SVNURL url = null;
	private SVNURL copyURL = null;
    
    public WorkingCopy(
    		final String svnRootURL, 
    		final String svnPrjName, 
    		final String svnPath, 
    		final String svnCopyPath, 
    		final String wcPath, 
    		final String name, 
    		final String password) throws SVNException {
    	
    	if( svnRootURL == null) {
            error("The Subversion root property must be defined", null);
    	}
    	if( svnPrjName == null) {
            error("The Subversion project name property must be defined", null);
    	}
    	
    	if( svnPath == null && wcPath == null ){
    		error("The Subversion project path or the working copy path must be defined", null);
    	}
    	
    	myWorkingCopyPath = wcPath;
    	
        /*
         * Initializes the library (it must be done before ever using the library 
         * itself)
         */
        setupLibrary();

        /*
         * Default values:
         */
        
        /*
         * Assuming that svnRootURL is an existing repository path.
         * SVNURL is a wrapper for URL strings that refer to repository locations.
         */
        try {
			repositoryURL = SVNURL.parseURIEncoded(svnRootURL+'/'+svnPrjName);
		} catch (SVNException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    /*
         * That's where a new directory will be created
         */
		if( svnPath != null ){
			url = repositoryURL.appendPath(svnPath, false);
		}
        /*
         * That's where '/MyRepos' will be copied to (branched)
         */
        copyURL = repositoryURL.appendPath(svnCopyPath, false);
        /*
         * Creating custom handlers that will process events
         */
        myCommitEventHandler = new CommitEventHandler();
        
        myUpdateEventHandler = new UpdateEventHandler();
        
        myWCEventHandler = new WCEventHandler();
        
        /*
         * Creates a default run-time configuration options driver. Default options 
         * created in this way use the Subversion run-time configuration area (for 
         * instance, on a Windows platform it can be found in the '%APPDATA%\Subversion' 
         * directory). 
         * 
         * readonly = true - not to save  any configuration changes that can be done 
         * during the program run to a config file (config settings will only 
         * be read to initialize; to enable changes the readonly flag should be set
         * to false).
         * 
         * SVNWCUtil is a utility class that creates a default options driver.
         */
        DefaultSVNOptions options = SVNWCUtil.createDefaultOptions(true);
        
        /*
         * Creates an instance of SVNClientManager providing authentication
         * information (name, password) and an options driver
         */
        ourClientManager = SVNClientManager.newInstance(options, name, password);
        
        /*
         * Sets a custom event handler for operations of an SVNCommitClient 
         * instance
         */
        ourClientManager.getCommitClient().setEventHandler(myCommitEventHandler);
        
        /*
         * Sets a custom event handler for operations of an SVNUpdateClient 
         * instance
         */
        ourClientManager.getUpdateClient().setEventHandler(myUpdateEventHandler);

        /*
         * Sets a custom event handler for operations of an SVNWCClient 
         * instance
         */
        ourClientManager.getWCClient().setEventHandler(myWCEventHandler);

        if( svnPath == null ){
        	url = getWCServerURL(new File(wcPath) );
        }
	}
    
    public String useWCServerURL() {
    	url = getWCServerURL();
    	
    	return url.toString();
    }
    public SVNURL getWCServerURL() {
    	File wcDir = new File(myWorkingCopyPath);
    	
        if (wcDir.exists() == false) {
            error("the destination directory '"
                    + wcDir.getAbsolutePath() + "' does not exists!", null);
        }
        SVNURL url = null;
    	try {
			url = getWCServerURL(wcDir);
		} catch (SVNException e) {
			error("Cannot get the server URL of the working copy", e);
		}
		return url;
    }
    
    public void checkout(final boolean force) {
        /*
         * creates a local directory where the working copy will be checked out into
         */
        File wcDir = new File(myWorkingCopyPath);
        if (wcDir.exists() && force == false) {
            error("the destination directory '"
                    + wcDir.getAbsolutePath() + "' already exists!", null);
        }
        if (wcDir.exists() == false ){
        	wcDir.mkdirs();
        }

        System.out.println("Checking out a working copy from '" + url + "'...");
        try {
            /*
             * recursively checks out a working copy from url into wcDir.
             * SVNRevision.HEAD means the latest revision to be checked out. 
             */
            checkout(url, SVNRevision.HEAD, wcDir, true, force);
        } catch (SVNException svne) {
            error("error while checking out a working copy for the location '"
                            + url + "'", svne);
        }
        System.out.println();

    }

    public void info() {
    	File wcDir = new File(myWorkingCopyPath);
    	
        if (wcDir.exists() == false) {
            error("the destination directory '"
                    + wcDir.getAbsolutePath() + "' does not exists!", null);
        }
        /*
         * recursively displays info for wcDir at the current working revision 
         * in the manner of 'svn info -R' command
         */
        try {
            showInfo(wcDir, SVNRevision.WORKING, true);
        } catch (SVNException svne) {
            error("error while recursively getting info for the working copy at'"
                    + wcDir.getAbsolutePath() + "'", svne);
        }
        System.out.println();

    }
    public void log() {
        /*
         * Default values:
         */
        long startRevision = 0;
        long endRevision = -1;//HEAD (the latest) revision

        SVNRepository repository = null;
        
        try {
            /*
             * Creates an instance of SVNRepository to work with the repository.
             * All user's requests to the repository are relative to the
             * repository location used to create this SVNRepository.
             * SVNURL is a wrapper for URL strings that refer to repository locations.
             */
            repository = ourClientManager.createRepository(url, false);
        } catch (SVNException svne) {
            /*
             * Perhaps a malformed URL is the cause of this exception.
             */
            System.err
                    .println("error while creating an SVNRepository for the location '"
                            + url + "': " + svne.getMessage());
            System.exit(1);
        }


        /*
         * Gets the latest revision number of the repository
         */
        try {
            endRevision = repository.getLatestRevision();
        } catch (SVNException svne) {
            System.err.println("error while fetching the latest repository revision: " + svne.getMessage());
            System.exit(1);
        }

        Collection logEntries = null;
        try {
            /*
             * Collects SVNLogEntry objects for all revisions in the range
             * defined by its start and end points [startRevision, endRevision].
             * For each revision commit information is represented by
             * SVNLogEntry.
             * 
             * the 1st parameter (targetPaths - an array of path strings) is set
             * when restricting the [startRevision, endRevision] range to only
             * those revisions when the paths in targetPaths were changed.
             * 
             * the 2nd parameter if non-null - is a user's Collection that will
             * be filled up with found SVNLogEntry objects; it's just another
             * way to reach the scope.
             * 
             * startRevision, endRevision - to define a range of revisions you are
             * interested in; by default in this program - startRevision=0, endRevision=
             * the latest (HEAD) revision of the repository.
             * 
             * the 5th parameter - a boolean flag changedPath - if true then for
             * each revision a corresponding SVNLogEntry will contain a map of
             * all paths which were changed in that revision.
             * 
             * the 6th parameter - a boolean flag strictNode - if false and a
             * changed path is a copy (branch) of an existing one in the repository
             * then the history for its origin will be traversed; it means the 
             * history of changes of the target URL (and all that there's in that 
             * URL) will include the history of the origin path(s).
             * Otherwise if strictNode is true then the origin path history won't be
             * included.
             * 
             * The return value is a Collection filled up with SVNLogEntry Objects.
             */
            logEntries = repository.log(new String[] {""}, null,
                    startRevision, endRevision, true, true);

        } catch (SVNException svne) {
            System.out.println("error while collecting log information for '"
                    + url + "': " + svne.getMessage());
            System.exit(1);
        }
        for (Iterator entries = logEntries.iterator(); entries.hasNext();) {
            /*
             * gets a next SVNLogEntry
             */
            SVNLogEntry logEntry = (SVNLogEntry) entries.next();
            System.out.println("---------------------------------------------");
            /*
             * gets the revision number
             */
            System.out.println("revision: " + logEntry.getRevision());
            /*
             * gets the author of the changes made in that revision
             */
            System.out.println("author: " + logEntry.getAuthor());
            /*
             * gets the time moment when the changes were committed
             */
            System.out.println("date: " + logEntry.getDate());
            /*
             * gets the commit log message
             */
            System.out.println("log message: " + logEntry.getMessage());
            /*
             * displaying all paths that were changed in that revision; cahnged
             * path information is represented by SVNLogEntryPath.
             */
            if (logEntry.getChangedPaths().size() > 0) {
                System.out.println();
                System.out.println("changed paths:");
                /*
                 * keys are changed paths
                 */
                Set changedPathsSet = logEntry.getChangedPaths().keySet();

                for (Iterator changedPaths = changedPathsSet.iterator(); changedPaths
                        .hasNext();) {
                    /*
                     * obtains a next SVNLogEntryPath
                     */
                    SVNLogEntryPath entryPath = (SVNLogEntryPath) logEntry
                            .getChangedPaths().get(changedPaths.next());
                    /*
                     * SVNLogEntryPath.getPath returns the changed path itself;
                     * 
                     * SVNLogEntryPath.getType returns a charecter describing
                     * how the path was changed ('A' - added, 'D' - deleted or
                     * 'M' - modified);
                     * 
                     * If the path was copied from another one (branched) then
                     * SVNLogEntryPath.getCopyPath &
                     * SVNLogEntryPath.getCopyRevision tells where it was copied
                     * from and what revision the origin path was at.
                     */
                    System.out.println(" "
                            + entryPath.getType()
                            + "	"
                            + entryPath.getPath()
                            + ((entryPath.getCopyPath() != null) ? " (from "
                                    + entryPath.getCopyPath() + " revision "
                                    + entryPath.getCopyRevision() + ")" : ""));
                }
            }
        }
    }

    public void status() {
        boolean isRecursive = true;
        boolean isRemote = true;
        boolean isReportAll = false;
        boolean isIncludeIgnored = true;
        boolean isCollectParentExternals = false;
    	File wcDir = new File(myWorkingCopyPath);
    	
        if (wcDir.exists() == false) {
            error("the destination directory '"
                    + wcDir.getAbsolutePath() + "' does not exists!", null);
        }
        System.out.println("Status for '" + wcDir.getAbsolutePath() + "':");
        try {
            /*
             * gets and shows status information for the WC directory.
             * status will be recursive on wcDir, will also cover the repository, 
             * won't cover unmodified entries, will disregard 'svn:ignore' property 
             * ignores (if any), will ignore externals definitions.
             */
            showStatus(wcDir, isRecursive, isRemote, isReportAll,
                    isIncludeIgnored, isCollectParentExternals);
        } catch (SVNException svne) {
            error("error while recursively performing status for '"
                    + wcDir.getAbsolutePath() + "'", svne);
        }
        System.out.println();

    }

    public void update() {
    	File wcDir = new File(myWorkingCopyPath);
    	
        if (wcDir.exists() == false) {
            error("the destination directory '"
                    + wcDir.getAbsolutePath() + "' does not exists!", null);
        }
        System.out.println("Updating '" + wcDir.getAbsolutePath() + "'...");
        try {
            /*
             * recursively updates wcDir to the latest revision (SVNRevision.HEAD)
             */
            update(wcDir, SVNRevision.HEAD, true);
        } catch (SVNException svne) {
            error("error while recursively updating the working copy at '"
                    + wcDir.getAbsolutePath() + "'", svne);
        }
        System.out.println("");

    }
    public void revert() {
    	File wcDir = new File(myWorkingCopyPath);
    	
        if (wcDir.exists() == false) {
            error("the destination directory '"
                    + wcDir.getAbsolutePath() + "' does not exists!", null);
        }
        System.out.println("Reverting '" + wcDir.getAbsolutePath() + "'...");
        try {
            /*
             * recursively revert wcDir to the latest revision (SVNRevision.HEAD)
             */
            revert(wcDir, true);
        } catch (SVNException svne) {
            error("error while recursively reverting the working copy at '"
                    + wcDir.getAbsolutePath() + "'", svne);
        }
        System.out.println("");
    }
    
    public void commit(final String message) {
    	File wcDir = new File(myWorkingCopyPath);
    	
        if (wcDir.exists() == false) {
            error("the destination directory '"
                    + wcDir.getAbsolutePath() + "' does not exists!", null);
        }
        long committedRevision = -1;
        System.out.println("Committing changes for '" + wcDir.getAbsolutePath() + "'...");
        try {
            /*
             * commits changes in wcDir to the repository with not leaving items 
             * locked (if any) after the commit succeeds; this will add aNewDir & 
             * aNewFile to the repository. 
             */
            committedRevision = commit(wcDir, false,
                    message)
                    .getNewRevision();
        } catch (SVNException svne) {
            error("error while committing changes to the working copy at '"
                    + wcDir.getAbsolutePath()
                    + "'", svne);
        }
        System.out.println("Committed to revision " + committedRevision);
        System.out.println();

    }
    public void copy() {
    	copy(false);
    }
    
    public void copy(final boolean isMoving ) {
        long committedRevision = -1;
        
        if( isMoving == true )
            System.out.println("Moving '" + url + "' to '" + copyURL + "'...");
        else
        	System.out.println("Copying '" + url + "' to '" + copyURL + "'...");
        try {
            /*
             * makes a branch of url at copyURL - that is URL->URL copying
             * with history
             */
            committedRevision = copy(url, copyURL, isMoving,
                    "remotely copying '" + url + "' to '" + copyURL + "'")
                    .getNewRevision();
        } catch (SVNException svne) {
            error("error while copying '" + url + "' to '"
                    + copyURL + "'", svne);
        }
       /*
        * displays what revision the repository was committed to
        */
        System.out.println("Committed to revision " + committedRevision);
        System.out.println();

    }
    
    public void switchWorkingCopy() {
    	File wcDir = new File(myWorkingCopyPath);
    	
        if (wcDir.exists() == false) {
            error("the destination directory '"
                    + wcDir.getAbsolutePath() + "' does not exists!", null);
        }

	    System.out.println("Switching '" + wcDir.getAbsolutePath() + "' to '"
	            + copyURL + "'...");
	    try {
	        /*
	         * recursively switches wcDir to copyURL in the latest revision 
	         * (SVNRevision.HEAD)
	         */
	        switchToURL(wcDir, copyURL, SVNRevision.HEAD, true);
	    } catch (SVNException svne) {
	        error("error while switching '"
	                + wcDir.getAbsolutePath() + "' to '" + copyURL + "'", svne);
	    }
	    System.out.println();

    }
    
    /*
     * Initializes the library to work with a repository via 
     * different protocols.
     */
    private static void setupLibrary() {
        /*
         * For using over http:// and https://
         */
        DAVRepositoryFactory.setup();
        /*
         * For using over svn:// and svn+xxx://
         */
        SVNRepositoryFactoryImpl.setup();
        
        /*
         * For using over file:///
         */
        FSRepositoryFactory.setup();
    }

    /*
     * Creates a new version controlled directory (doesn't create any intermediate
     * directories) right in a repository. Like 'svn mkdir URL -m "some comment"' 
     * command. It's done by invoking 
     * 
     * SVNCommitClient.doMkDir(SVNURL[] urls, String commitMessage) 
     * 
     * which takes the following parameters:
     * 
     * urls - an array of URLs that are to be created;
     * 
     * commitMessage - a commit log message since a URL-based directory creation is 
     * immediately committed to a repository.
     */
    private static SVNCommitInfo makeDirectory(SVNURL url, String commitMessage) throws SVNException{
        /*
         * Returns SVNCommitInfo containing information on the new revision committed 
         * (revision number, etc.) 
         */
        return ourClientManager.getCommitClient().doMkDir(new SVNURL[]{url}, commitMessage);
    }
    
    /*
     * Imports an unversioned directory into a repository location denoted by a
     * destination URL (all necessary parent non-existent paths will be created 
     * automatically). This operation commits the repository to a new revision. 
     * Like 'svn import PATH URL (-N) -m "some comment"' command. It's done by 
     * invoking 
     * 
     * SVNCommitClient.doImport(File path, SVNURL dstURL, String commitMessage, boolean recursive) 
     * 
     * which takes the following parameters:
     * 
     * path - a local unversioned directory or singal file that will be imported into a 
     * repository;
     * 
     * dstURL - a repository location where the local unversioned directory/file will be 
     * imported into; this URL path may contain non-existent parent paths that will be 
     * created by the repository server;
     * 
     * commitMessage - a commit log message since the new directory/file are immediately
     * created in the repository;
     * 
     * recursive - if true and path parameter corresponds to a directory then the directory
     * will be added with all its child subdirictories, otherwise the operation will cover
     * only the directory itself (only those files which are located in the directory).  
     */
    private static SVNCommitInfo importDirectory(File localPath, SVNURL dstURL, String commitMessage, 
            boolean isRecursive) throws SVNException{
        /*
         * Returns SVNCommitInfo containing information on the new revision committed 
         * (revision number, etc.) 
         */
        return ourClientManager.getCommitClient().doImport(localPath, dstURL, commitMessage, null, true, false, 
                SVNDepth.fromRecurse(isRecursive));
    }
    /*
     * Committs changes in a working copy to a repository. Like 
     * 'svn commit PATH -m "some comment"' command. It's done by invoking 
     * 
     * SVNCommitClient.doCommit(File[] paths, boolean keepLocks, String commitMessage, 
     * boolean force, boolean recursive) 
     * 
     * which takes the following parameters:
     * 
     * paths - working copy paths which changes are to be committed;
     * 
     * keepLocks - if true then doCommit(..) won't unlock locked paths; otherwise they will
     * be unlocked after a successful commit; 
     * 
     * commitMessage - a commit log message;
     * 
     * force - if true then a non-recursive commit will be forced anyway;  
     * 
     * recursive - if true and a path corresponds to a directory then doCommit(..) recursively 
     * commits changes for the entire directory, otherwise - only for child entries of the 
     * directory;
     */
    private static SVNCommitInfo commit(File wcPath, boolean keepLocks, String commitMessage)
            throws SVNException {
        /*
         * Returns SVNCommitInfo containing information on the new revision committed 
         * (revision number, etc.) 
         */
        return ourClientManager.getCommitClient().doCommit(new File[] { wcPath }, keepLocks, commitMessage, 
                null, null, false, false, SVNDepth.INFINITY);
    }

    /*
     * Checks out a working copy from a repository. Like 'svn checkout URL[@REV] PATH (-r..)'
     * command; It's done by invoking 
     * 
     * SVNUpdateClient.doCheckout(SVNURL url, File dstPath, SVNRevision pegRevision, 
     * SVNRevision revision, boolean recursive)
     * 
     * which takes the following parameters:
     * 
     * url - a repository location from where a working copy is to be checked out;
     * 
     * dstPath - a local path where the working copy will be fetched into;
     * 
     * pegRevision - an SVNRevision representing a revision to concretize
     * url (what exactly URL a user means and is sure of being the URL he needs); in other
     * words that is the revision in which the URL is first looked up;
     * 
     * revision - a revision at which a working copy being checked out is to be; 
     * 
     * recursive - if true and url corresponds to a directory then doCheckout(..) recursively 
     * fetches out the entire directory, otherwise - only child entries of the directory;   
     */
    private static long checkout(SVNURL url,
            SVNRevision revision, File destPath, boolean isRecursive, boolean allowUnversionedObstructions)
            throws SVNException {

        SVNUpdateClient updateClient = ourClientManager.getUpdateClient();
        /*
         * sets externals not to be ignored during the checkout
         */
        updateClient.setIgnoreExternals(false);
        /*
         * returns the number of the revision at which the working copy is 
         */
        return updateClient.doCheckout(url, destPath, revision, revision, SVNDepth.fromRecurse(isRecursive), 
                allowUnversionedObstructions);
    }
    
    /*
     * Updates a working copy (brings changes from the repository into the working copy). 
     * Like 'svn update PATH' command; It's done by invoking 
     * 
     * SVNUpdateClient.doUpdate(File file, SVNRevision revision, boolean recursive) 
     * 
     * which takes the following parameters:
     * 
     * file - a working copy entry that is to be updated;
     * 
     * revision - a revision to which a working copy is to be updated;
     * 
     * recursive - if true and an entry is a directory then doUpdate(..) recursively 
     * updates the entire directory, otherwise - only child entries of the directory;   
     */
    private static long update(File wcPath, SVNRevision updateToRevision, boolean isRecursive) throws SVNException {

        SVNUpdateClient updateClient = ourClientManager.getUpdateClient();
        /*
         * sets externals not to be ignored during the update
         */
        updateClient.setIgnoreExternals(false);
        /*
         * returns the number of the revision wcPath was updated to
         */
        return updateClient.doUpdate(wcPath, updateToRevision, SVNDepth.fromRecurse(isRecursive), false, false);
    }
    
    /*
     * Updates a working copy to a different URL. Like 'svn switch URL' command.
     * It's done by invoking 
     * 
     * SVNUpdateClient.doSwitch(File file, SVNURL url, SVNRevision revision, boolean recursive) 
     * 
     * which takes the following parameters:
     * 
     * file - a working copy entry that is to be switched to a new url;
     * 
     * url - a target URL a working copy is to be updated against;
     * 
     * revision - a revision to which a working copy is to be updated;
     * 
     * recursive - if true and an entry (file) is a directory then doSwitch(..) recursively 
     * switches the entire directory, otherwise - only child entries of the directory;   
     */
    private static long switchToURL(File wcPath, SVNURL url, SVNRevision updateToRevision, boolean isRecursive) throws SVNException {
        SVNUpdateClient updateClient = ourClientManager.getUpdateClient();
        /*
         * sets externals not to be ignored during the switch
         */
        updateClient.setIgnoreExternals(false);
        /*
         * returns the number of the revision wcPath was updated to
         */
        return updateClient.doSwitch(wcPath, url, SVNRevision.UNDEFINED, updateToRevision, 
                SVNDepth.getInfinityOrFilesDepth(isRecursive), false, false);
    }

    /*
     * Collects status information on local path(s). Like 'svn status (-u) (-N)' 
     * command. It's done by invoking 
     * 
     * SVNStatusClient.doStatus(File path, boolean recursive, 
     * boolean remote, boolean reportAll, boolean includeIgnored, 
     * boolean collectParentExternals, ISVNStatusHandler handler) 
     * 
     * which takes the following parameters:
     * 
     * path - an entry which status info to be gathered;
     * 
     * recursive - if true and an entry is a directory then doStatus(..) collects status 
     * info not only for that directory but for each item inside stepping down recursively;
     * 
     * remote - if true then doStatus(..) will cover the repository (not only the working copy)
     * as well to find out what entries are out of date;
     * 
     * reportAll - if true then doStatus(..) will also include unmodified entries;
     * 
     * includeIgnored - if true then doStatus(..) will also include entries being ignored; 
     * 
     * collectParentExternals - if true then externals definitions won't be ignored;
     * 
     * handler - an implementation of ISVNStatusHandler to process status info per each entry
     * doStatus(..) traverses; such info is collected in an SVNStatus object and
     * is passed to a handler's handleStatus(SVNStatus status) method where an implementor
     * decides what to do with it.  
     */
    private static void showStatus(File wcPath, boolean isRecursive, boolean isRemote, boolean isReportAll,
            boolean isIncludeIgnored, boolean isCollectParentExternals)
            throws SVNException {
        /*
         * StatusHandler displays status information for each entry in the console (in the 
         * manner of the native Subversion command line client)
         */
        ourClientManager.getStatusClient().doStatus(wcPath, SVNRevision.HEAD, SVNDepth.fromRecurse(isRecursive), 
                isRemote, isReportAll, isIncludeIgnored, isCollectParentExternals, new StatusHandler(isRemote), 
                null);
    }

    /*
     * Collects information on local path(s). Like 'svn info (-R)' command.
     * It's done by invoking 
     * 
     * SVNWCClient.doInfo(File path, SVNRevision revision,
     * boolean recursive, ISVNInfoHandler handler) 
     * 
     * which takes the following parameters:
     * 
     * path - a local entry for which info will be collected;
     * 
     * revision - a revision of an entry which info is interested in; if it's not
     * WORKING then info is got from a repository;
     * 
     * recursive - if true and anconvention.plugins.svnkit.svn_username entry is a directory then doInfo(..) collects info 
     * not only for that directory but for each item inside stepping down recursively;
     * 
     * handler - an implementation of ISVNInfoHandler to process info per each entry
     * doInfo(..) traverses; such info is collected in an SVNInfo object and
     * is passed to a handler's handleInfo(SVNInfo info) method where an implementor
     * decides what to do with it.     
     */
    private static void showInfo(File wcPath, SVNRevision revision, boolean isRecursive) throws SVNException {
        /*
         * InfoHandler displays information for each entry in the console (in the manner of
         * the native Subversion command line client)
         */
        ourClientManager.getWCClient().doInfo(wcPath, SVNRevision.UNDEFINED, revision, 
                SVNDepth.getInfinityOrEmptyDepth(isRecursive), null, new InfoHandler());
    }
    private static SVNInfo getWCInfo(File wcPath) throws SVNException {
        return ourClientManager.getWCClient().doInfo(wcPath, SVNRevision.WORKING);
    }
    private static SVNURL getWCServerURL(File wcPath) throws SVNException {
        SVNInfo info =  ourClientManager.getWCClient().doInfo(wcPath, SVNRevision.WORKING);
        return info.getURL();
    }
    
    /*
     * Puts directories and files under version control scheduling them for addition
     * to a repository. They will be added in a next commit. Like 'svn add PATH' 
     * command. It's done by invoking 
     * 
     * SVNWCClient.doAdd(File path, boolean force, 
     * boolean mkdir, boolean climbUnversionedParents, boolean recursive) 
     * 
     * which takes the following parameters:
     * 
     * path - an entry to be scheduled for addition;
     * 
     * force - set to true to force an addition of an entry anyway;
     * 
     * mkdir - if true doAdd(..) creates an empty directory at path and schedules
     * it for addition, like 'svn mkdir PATH' command;
     * 
     * climbUnversionedParents - if true and the parent of the entry to be scheduled
     * for addition is not under version control, then doAdd(..) automatically schedules
     * the parent for addition, too;
     * 
     * recursive - if true and an entry is a directory then doAdd(..) recursively 
     * schedules all its inner dir entries for addition as well. 
     */
    private static void addEntry(File wcPath) throws SVNException {
        ourClientManager.getWCClient().doAdd(wcPath, false, false, false, SVNDepth.INFINITY, false, false);
    }
    
    /*
     * Locks working copy paths, so that no other user can commit changes to them.
     * Like 'svn lock PATH' command. It's done by invoking 
     * 
     * SVNWCClient.doLock(File[] paths, boolean stealLock, String lockMessage) 
     * 
     * which takes the following parameters:
     * 
     * paths - an array of local entries to be locked;
     * 
     * stealLock - set to true to steal the lock from another user or working copy;
     * 
     * lockMessage - an optional lock comment string.
     */
    private static void lock(File wcPath, boolean isStealLock, String lockComment) throws SVNException {
        ourClientManager.getWCClient().doLock(new File[] { wcPath }, isStealLock, lockComment);
    }
    
    /*
     * Schedules directories and files for deletion from version control upon the next
     * commit (locally). Like 'svn delete PATH' command. It's done by invoking 
     * 
     * SVNWCClient.doDelete(File path, boolean force, boolean dryRun) 
     * 
     * which takes the following parameters:
     * 
     * path - an entry to be scheduled for deletion;
     * 
     * force - a boolean flag which is set to true to force a deletion even if an entry
     * has local modifications;
     * 
     * dryRun - set to true not to delete an entry but to check if it can be deleted;
     * if false - then it's a deletion itself.  
     */
    private static void delete(File wcPath, boolean force) throws SVNException {
        ourClientManager.getWCClient().doDelete(wcPath, force, false);
    }
    /*
     * Restores the pristine version of working copy
     * 
     * SVNWCClient.doRevert(File [] paths, boolean force, boolean dryRun) 
     * 
     * which takes the following parameters:
     * 
     * path - an entry to be scheduled for revertion;
     * recursive - a boolean flag which is set to true to revert recursively
     * 
     */
    private static void revert(File wcPath, boolean recursive) throws SVNException {
        ourClientManager.getWCClient().doRevert(new File[] { wcPath }, SVNDepth.getInfinityOrEmptyDepth(recursive), null);
    }
    
    /*
     * Duplicates srcURL to dstURL (URL->URL)in a repository remembering history.
     * Like 'svn copy srcURL dstURL -m "some comment"' command. It's done by
     * invoking 
     * 
     * doCopy(SVNURL srcURL, SVNRevision srcRevision, SVNURL dstURL, 
     * boolean isMove, String commitMessage) 
     * 
     * which takes the following parameters:
     * 
     * srcURL - a source URL that is to be copied;
     * 
     * srcRevision - a definite revision of srcURL 
     * 
     * dstURL - a URL where srcURL will be copied; if srcURL & dstURL are both 
     * directories then there are two cases: 
     * a) dstURL already exists - then doCopy(..) will duplicate the entire source 
     * directory and put it inside dstURL (for example, 
     * consider srcURL = svn://localhost/rep/MyRepos, 
     * dstURL = svn://localhost/rep/MyReposCopy, in this case if doCopy(..) succeeds 
     * MyRepos will be in MyReposCopy - svn://localhost/rep/MyReposCopy/MyRepos); 
     * b) dstURL doesn't exist yet - then doCopy(..) will create a directory and
     * recursively copy entries from srcURL into dstURL (for example, consider the same
     * srcURL = svn://localhost/rep/MyRepos, dstURL = svn://localhost/rep/MyReposCopy, 
     * in this case if doCopy(..) succeeds MyRepos entries will be in MyReposCopy, like:
     * svn://localhost/rep/MyRepos/Dir1 -> svn://localhost/rep/MyReposCopy/Dir1...);  
     * 
     * isMove - if false then srcURL is only copied to dstURL what
     * corresponds to 'svn copy srcURL dstURL -m "some comment"'; but if it's true then
     * srcURL will be copied and deleted - 'svn move srcURL dstURL -m "some comment"'; 
     * 
     * commitMessage - a commit log message since URL->URL copying is immediately 
     * committed to a repository.
     */
    private static SVNCommitInfo copy(SVNURL srcURL, SVNURL dstURL, boolean isMove, String commitMessage) throws SVNException {
        /*
         * SVNRevision.HEAD means the latest revision.
         * Returns SVNCommitInfo containing information on the new revision committed 
         * (revision number, etc.) 
         */        
        return ourClientManager.getCopyClient().doCopy(new SVNCopySource[] {new SVNCopySource(SVNRevision.HEAD, SVNRevision.HEAD, srcURL)},
                dstURL, isMove, true, false, commitMessage, null);
    }
    
    /*
     * Displays error information and exits. 
     */
    private static void error(String message, Exception e){
        System.err.println(message+(e!=null ? ": "+e.getMessage() : ""));
        System.err.println("");
        System.exit(1);
    }
    
    /*
     * This method does not relate to SVNKit API. Just a method which creates
     * local directories and files :)
     */
    private static final void createLocalDir(File aNewDir, File[] localFiles, String[] fileContents){
        if (!aNewDir.mkdirs()) {
            error("failed to create a new directory '" + aNewDir.getAbsolutePath() + "'.", null);
        }
        for(int i=0; i < localFiles.length; i++){
	        File aNewFile = localFiles[i];
            try {
	            if (!aNewFile.createNewFile()) {
	                error("failed to create a new file '"
	                        + aNewFile.getAbsolutePath() + "'.", null);
	            }
	        } catch (IOException ioe) {
	            aNewFile.delete();
	            error("error while creating a new file '"
	                    + aNewFile.getAbsolutePath() + "'", ioe);
	        }
	
	        String contents = null;
	        if(i > fileContents.length-1){
	            continue;
	        }
            contents = fileContents[i];
	        
	        /*
	         * writing a text into the file
	         */
	        FileOutputStream fos = null;
	        try {
	            fos = new FileOutputStream(aNewFile);
	            fos.write(contents.getBytes());
	        } catch (FileNotFoundException fnfe) {
	            error("the file '" + aNewFile.getAbsolutePath() + "' is not found", fnfe);
	        } catch (IOException ioe) {
	            error("error while writing into the file '"
	                    + aNewFile.getAbsolutePath() + "'", ioe);
	        } finally {
	            if (fos != null) {
	                try {
	                    fos.close();
	                } catch (IOException ioe) {
	                    //
	                }
	            }
	        }
        }
    }
}