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
package com.tolstoy.basic.app.storage;

import java.util.*;
import java.sql.*;
import java.time.Instant;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.commons.dbcp2.BasicDataSource;
import com.tolstoy.basic.app.utils.*;
import com.tolstoy.basic.api.storage.*;

public class StorageEmbeddedDerby implements IStorage {
	private static final Logger logger = LogManager.getLogger( StorageEmbeddedDerby.class );

	private BasicDataSource connectionPool;
	private List<String> tableNames;
	private String connectionString;

	public StorageEmbeddedDerby( String connectionString, List<String> tableNames ) throws Exception {
		this.connectionString = connectionString;
		this.tableNames = tableNames;
		this.connectionPool = null;
	}

	@Override
	public void connect() throws Exception {
		Class.forName( "org.apache.derby.jdbc.EmbeddedDriver" ).newInstance();

		//connection = DriverManager.getConnection( connectionString );
		connectionPool = new BasicDataSource();

		connectionPool.setDriverClassName( "org.apache.derby.jdbc.EmbeddedDriver" );
		connectionPool.setUrl( connectionString );
	}

	@Override
	public void ensureTables() throws Exception {
		for ( String tableName : tableNames ) {
			createTableInternalIgnoreIfExists( tableName );
		}
	}

	@Override
	public void dropTables() throws Exception {
		for ( String tableName : tableNames ) {
			dropTableInternal( tableName );
		}
	}

	@Override
	public IStorable getRecordByID( IStorageTable table, long id ) throws Exception {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		String tablename = table.getTablename();

		try {
			connection = getConnection();
			ps = connection.prepareStatement( "SELECT * FROM " + tablename + " WHERE id=?" );
			ps.setLong( 1, id );

			rs = ps.executeQuery();

			while ( rs.next() ) {
				return readRecord( rs );
			}
		}
		finally {
			if ( rs != null ) {
				rs.close();
			}
			if ( ps != null ) {
				ps.close();
			}
			if ( connection != null ) {
				connection.close();
			}
		}

		return null;
	}

	@Override
	public List<IStorable> getRecords( IStorageTable table, StorageOrdering ordering, int max ) throws Exception {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<IStorable> ret = new ArrayList<IStorable>( max );

		String tablename = table.getTablename();

		try {
			connection = getConnection();
			ps = connection.prepareStatement( "SELECT * FROM " + tablename + " " + getOrdering( ordering ) );

			ps.setMaxRows( max );
			rs = ps.executeQuery();

			while ( rs.next() ) {
				IStorable storable = readRecord( rs );
				if ( storable != null ) {
					ret.add( storable );
				}
			}
		}
		finally {
			if ( rs != null ) {
				rs.close();
			}
			if ( ps != null ) {
				ps.close();
			}
			if ( connection != null ) {
				connection.close();
			}
		}

		return ret;
	}

	@Override
	public List<IStorable> getRecords( IStorageTable table, String searchkey, StorageOrdering ordering, int max ) throws Exception {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<IStorable> ret = new ArrayList<IStorable>( max );

		String tablename = table.getTablename();

		try {
			connection = getConnection();
			ps = connection.prepareStatement( "SELECT * FROM " + tablename + " WHERE searchkey= ? " + getOrdering( ordering ) );

			ps.setString( 1, searchkey );

			ps.setMaxRows( max );
			rs = ps.executeQuery();

			while ( rs.next() ) {
				ret.add( readRecord( rs ) );
			}
		}
		finally {
			if ( rs != null ) {
				rs.close();
			}
			if ( ps != null ) {
				ps.close();
			}
			if ( connection != null ) {
				connection.close();
			}
		}

		return ret;
	}

	@Override
	public void saveRecord( IStorageTable table, IStorable record ) throws Exception {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		String tablename = table.getTablename();
		String query;

		try {
			String json = Utils.getDefaultObjectMapper().writeValueAsString( record );

			connection = getConnection();
			Blob blob = connection.createBlob();
			blob.setBytes( 1, json.getBytes() );

			if ( record.getID() == 0 ) {
				query = "INSERT INTO " + tablename + "( searchkey, created, modified, payload ) VALUES( ?, ?, ?, ? )";
				ps = connection.prepareStatement( query, Statement.RETURN_GENERATED_KEYS );

				ps.setString( 1, record.getSearchKey() );
				ps.setObject( 2, instantToTimestamp( record.getCreateTime() ) );
				ps.setObject( 3, instantToTimestamp( record.getModifyTime() ) );
				ps.setBlob( 4, blob );
				logger.info( "about to insert to " + tablename );

				ps.executeUpdate();

				rs = ps.getGeneratedKeys();

				if ( rs.next() ) {
					record.setID( rs.getLong( 1 ) );
				}
			}
			else {
				query = "UPDATE " + tablename + " SET searchkey = ?, created = ?, modified = ?, payload = ? WHERE id = ?";
				ps = connection.prepareStatement( query );

				ps.setString( 1, record.getSearchKey() );
				ps.setObject( 2, instantToTimestamp( record.getCreateTime() ) );
				ps.setObject( 3, instantToTimestamp( record.getModifyTime() ) );
				ps.setBlob( 4, blob );
				ps.setLong( 5, record.getID() );
				logger.info( "about to update " + record.getID() + " in " + tablename );

				ps.executeUpdate();
			}
		}
		finally {
			if ( rs != null ) {
				rs.close();
			}
			if ( ps != null ) {
				ps.close();
			}
			if ( connection != null ) {
				connection.close();
			}
		}
	}

	protected IStorable readRecord( ResultSet rs ) throws Exception {
		byte[] bytes = rs.getBytes( "payload" );
		try {
			return (IStorable) Utils.getDefaultObjectMapper().readValue( new String( bytes ), Object.class );
		}
		catch ( Exception e ) {
			logger.error( "can't read record", e );
			//logger.info( "bytes=" + new String( bytes ) );
			return null;
		}
	}

	protected void createTableInternalIgnoreIfExists( String tablename ) throws Exception {
		Connection connection = null;
		Statement stmt = null;

		String definition = "CREATE TABLE " + tablename + "( " +
							" id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
							" searchkey VARCHAR(255)," +
							" created TIMESTAMP," +
							" modified TIMESTAMP," +
							" payload BLOB(16M)," +
							" CONSTRAINT pk" + tablename + " PRIMARY KEY (id) )";

		try {
			connection = getConnection();
			stmt = connection.createStatement();
			stmt.executeUpdate( definition );
			logger.info( "created table " + tablename );
		}
		catch ( SQLException e ) {
			String s = e.toString();
			if ( s.indexOf( "exists" ) < 0 ) {
				logger.error( "while creating table " + tablename, e );
				throw e;
			}
		}
		finally {
			if ( stmt != null ) {
				stmt.close();
			}
			if ( connection != null ) {
				connection.close();
			}
		}
	}

	protected void dropTableInternal( String tablename ) throws Exception {
		Connection connection = null;
		Statement stmt = null;

		try {
			connection = getConnection();
			stmt = connection.createStatement();
			stmt.executeUpdate( "DROP TABLE " + tablename );
			logger.info( "dropped table " + tablename );
		}
		catch ( SQLException e ) {
			logger.error( "could not drop table " + tablename, e );
			throw e;
		}
		finally {
			if ( stmt != null ) {
				stmt.close();
			}
		}
	}

	protected String getOrdering( StorageOrdering ordering ) {
		if ( ordering == StorageOrdering.DESC ) {
			return " ORDER BY modified DESC ";
		}
		else if ( ordering == StorageOrdering.ASC ) {
			return " ORDER BY modified ASC ";
		}
		else {
			return "";
		}
	}

	protected Connection getConnection() throws Exception {
		if ( connectionPool == null ) {
			throw new RuntimeException( "Not connected to the database" );
		}

		return connectionPool.getConnection();
	}

	protected Timestamp instantToTimestamp( Instant inst ) {
		return inst != null ? Timestamp.from( inst ) : Timestamp.from( Instant.now() );
	}
}
