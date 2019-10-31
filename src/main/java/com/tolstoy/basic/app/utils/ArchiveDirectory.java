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
package com.tolstoy.basic.app.utils;

import java.io.File;
import java.text.Format;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.commons.lang.time.FastDateFormat;

import com.tolstoy.basic.api.utils.IArchiveDirectory;

/**
 * Creates a directory and saves files to it.
 */
final public class ArchiveDirectory implements IArchiveDirectory {
	private static final Logger logger = LogManager.getLogger( ArchiveDirectory.class );

	private static final Format dateFormat = FastDateFormat.getInstance( "yyyyMMdd_HHmmss_SSS" );

	private final File directory;
	private final String defaultFilePrefix, defaultFileSuffix;

	public ArchiveDirectory( final File parent ) throws Exception {
		this( parent, "", "", "", "" );
	}

	public ArchiveDirectory( final File parent, final String directoryPrefix, final String directorySuffix, final String filePrefix, final String fileSuffix )
	throws Exception {
		this.defaultFilePrefix = filePrefix;
		this.defaultFileSuffix = fileSuffix;

		directory = createDirectory( parent, directoryPrefix, directorySuffix );
	}

	@Override
	public String put( final String contents ) throws Exception {
		return put( contents, defaultFilePrefix, defaultFileSuffix, StandardCharsets.UTF_8 );
	}

	@Override
	public String put( final String contents, final Charset charset ) throws Exception {
		return put( contents, defaultFilePrefix, defaultFileSuffix, charset );
	}

	@Override
	public String put( final String contents, final String prefix, final String suffix ) throws Exception {
		return put( contents, prefix, suffix, StandardCharsets.UTF_8 );
	}

	@Override
	public String put( final String contents, final String prefix, final String suffix, final Charset charset ) throws Exception {
		final Date now = new Date();
		final Random random = new Random();
		File newFile = null;
		String name = "";

		for ( int i = 0; i < 100; i++ ) {
			name = prefix + dateFormat.format( now ) + "__" + random.nextInt( 100000000 ) + suffix;

			newFile = new File( directory, name );
			if ( !newFile.exists() ) {
				FileUtils.writeStringToFile( newFile, contents, charset );

				return newFile.getName();
			}
		}

		throw new RuntimeException( "Cannot create file " + newFile + " in " + directory );
	}

	private File createDirectory( final File parent, final String directoryPrefix, final String directorySuffix ) throws Exception {
		final Date now = new Date();
		final Random random = new Random();
		File newDirectory = null;
		String name = "";

		for ( int i = 0; i < 100; i++ ) {
			name = directoryPrefix + dateFormat.format( now ) + "__" + random.nextInt( 100000000 ) + directorySuffix;

			newDirectory = new File( parent, name );
			if ( !newDirectory.exists() && newDirectory.mkdir() ) {
				return newDirectory;
			}
		}

		throw new RuntimeException( "Cannot create directory " + name + " in " + parent );
	}
}
