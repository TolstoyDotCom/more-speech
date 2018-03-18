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

public class AppDirectories implements IAppDirectories {
	private static final Logger logger = LogManager.getLogger( AppDirectories.class );

	private File installDirectory, databaseParentDirectory, databaseDirectory, reportsDirectory;
	private String databaseParentDirectoryName, databaseDirectoryName, reportsDirectoryName;
	private int levelsUp;

	public File getInstallDirectory() {
		return installDirectory;
	}

	public File getDatabaseParentDirectory() {
		return databaseParentDirectory;
	}

	public File getDatabaseDirectory() {
		return databaseDirectory;
	}

	public File getReportsDirectory() {
		return reportsDirectory;
	}

	public File getSubdirectory( String name ) {
		if ( installDirectory == null ) {
			return null;
		}

		File container;

		if ( levelsUp == 1 ) {
			container = installDirectory.getParentFile();
		}
		else {
			container = installDirectory.getParentFile().getParentFile();
		}

		return new File( container, name );
	}

	public AppDirectories( int levelsUp, String databaseParentDirectoryName, String databaseDirectoryName, String reportsDirectoryName ) {
		this.levelsUp = levelsUp;
		this.databaseParentDirectoryName = databaseParentDirectoryName;
		this.databaseDirectoryName = databaseDirectoryName;
		this.reportsDirectoryName = reportsDirectoryName;

		this.databaseParentDirectory = null;
		this.databaseDirectory = null;
		this.reportsDirectory = null;

		this.installDirectory = FileUtils.urlToFile( ClassUtils.getLocation( getClass() ) );
		if ( this.installDirectory == null || this.installDirectory.getParentFile() == null ) {
			return;
		}

		this.databaseParentDirectory = getOrMakeDirectory( this.installDirectory, this.databaseParentDirectoryName );
		if ( this.databaseParentDirectory != null ) {
			this.reportsDirectory = getOrMakeDirectory( this.installDirectory, this.reportsDirectoryName );
		}

		if ( this.databaseParentDirectory != null ) {
			this.databaseDirectory = new File( this.databaseParentDirectory, this.databaseDirectoryName );
		}
	}

	protected File getOrMakeDirectory( File start, String name ) {
		File container, par;

		if ( levelsUp == 1 ) {
			container = start.getParentFile();
		}
		else {
			container = start.getParentFile().getParentFile();
		}

		par = new File( container, name );

		if ( par.exists() ) {
			return par;
		}

		par.mkdir();

		return par.exists() ? par : null;
	}
}

