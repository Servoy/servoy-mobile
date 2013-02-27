package com.servoy.mobile.test.server;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.google.gwt.junit.tools.GWTTestSuite;
import com.servoy.mobile.test.client.MobileSolutionTestCase;

/**
 * Mobile test suite (runs server side, even if testcases run in browser).
 * 
 * @author acostescu
 */
public class MobileSolutionTestSuite
{

	public static Test suite()
	{
		TestSuite suite = new GWTTestSuite("This is a test suite");
//		throw new RuntimeException("[TMC]");
		System.out.println("TMC Suite started");

//		suite.addTestSuite(StockWatcherTest.class);
		suite.addTestSuite(MobileSolutionTestCase.class);

		return suite;
	}

}
