/*
 * Copyright 2018 Chris Kelly
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.tolstoy.censorship.twitter.checker.app.helpers;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.scijava.util.ClassUtils;
import org.scijava.util.FileUtils;
import com.tolstoy.basic.app.utils.Utils;
import com.tolstoy.censorship.twitter.checker.api.installation.IAppDirectories;

public class AppDirectories implements IAppDirectories {
	private static final Logger logger = LogManager.getLogger( AppDirectories.class );

	private final File installDirectory, databaseParentDirectory, databaseDirectory, reportsDirectory;
	private final String databaseParentDirectoryName, databaseDirectoryName, reportsDirectoryName;

	public AppDirectories( final String databaseParentDirectoryName, final String databaseDirectoryName, final String reportsDirectoryName ) throws Exception {
		this.databaseParentDirectoryName = databaseParentDirectoryName;
		this.databaseDirectoryName = databaseDirectoryName;
		this.reportsDirectoryName = reportsDirectoryName;

		final File start = FileUtils.urlToFile( ClassUtils.getLocation( getClass() ) );
		final File userscriptsDirectory = Utils.findFileGoingUp( start, "userscripts" );
		if ( userscriptsDirectory == null ) {
			throw new RuntimeException( "Cannot find userscripts directory starting at " + start );
		}

		this.installDirectory = userscriptsDirectory.getParentFile();
		if ( this.installDirectory == null ) {
			throw new RuntimeException( "Cannot find install directory starting at " + userscriptsDirectory );
		}

		this.databaseParentDirectory = getOrMakeDirectory( this.installDirectory, this.databaseParentDirectoryName );
		this.databaseDirectory = new File( this.databaseParentDirectory, this.databaseDirectoryName );

		this.reportsDirectory = getOrMakeDirectory( this.installDirectory, this.reportsDirectoryName );
	}

	@Override
	public File getInstallDirectory() {
		return installDirectory;
	}

	@Override
	public File getDatabaseParentDirectory() {
		return databaseParentDirectory;
	}

	@Override
	public File getDatabaseDirectory() {
		return databaseDirectory;
	}

	@Override
	public File getReportsDirectory() {
		return reportsDirectory;
	}

	@Override
	public File getSubdirectory( final String name ) {
		return new File( installDirectory, name );
	}

	protected File getOrMakeDirectory( final File start, final String name ) throws Exception {
		File dir;

		dir = new File( start, name );

		if ( dir.exists() ) {
			if ( dir.isDirectory() ) {
				return dir;
			}

			throw new RuntimeException( "" + dir + " exists as a file when a directory was expected." );
		}

		dir.mkdir();

		if ( !dir.exists() || !dir.isDirectory() ) {
			throw new RuntimeException( "" + dir + " could not be created." );
		}

		return dir;
	}
}

