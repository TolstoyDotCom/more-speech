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
package com.tolstoy.censorship.twitter.checker.app;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.tolstoy.basic.api.utils.*;
import com.tolstoy.basic.app.utils.*;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class BundleTest extends TestCase {
	private static final Logger logger = LogManager.getLogger( BundleTest.class );

	private IResourceBundleWithFormatting bundle = null;

	/**
	 * Create the test case
	 *
	 * @param testName name of the test case
	 */
	public BundleTest( String testName ) {
		super( testName );
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite( BundleTest.class );
	}

	protected void setUp() throws Exception {
		bundle = new ResourceBundleWithFormatting( "GUI" );
	}

	protected void tearDown() throws Exception {
		bundle = null;
	}

	/**
	 */
	public void testBundle() throws Exception {
		assertEquals( bundle.getString( "ddddd" ), "unknown" );
		assertEquals( bundle.getString( "test_value" ), "test value" );
		assertEquals( bundle.getString( "test_value", "123" ), "test value" );
		assertEquals( bundle.getString( "test_value", "123", new StringBuffer( "abc" ) ), "test value" );

		assertEquals( bundle.getString( "test_value1" ), "test %s value" );
		assertEquals( bundle.getString( "test_value1", "123" ), "test 123 value" );
		assertEquals( bundle.getString( "test_value1", "123", new StringBuffer( "abc" ) ), "test 123 value" );

		assertEquals( bundle.getString( "test_value2" ), "test %s value %s" );
		assertEquals( bundle.getString( "test_value2", "123" ), "test %s value %s" );
		assertEquals( bundle.getString( "test_value2", "123", new StringBuffer( "abc" ) ), "test 123 value abc" );
	}
}
