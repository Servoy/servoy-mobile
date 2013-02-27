package com.servoy.mobile.test.client;

import com.allen_sauer.gwt.log.client.Log;

/**
 * This is the one containing browser-side code.
 * 
 * @author acostescu
 */
public class MobileSolutionTestCase extends com.servoy.mobile.test.server.MobileSolutionTestCase
{

	private TestMobileClient testClient;

	@Override
	protected void gwtSetUp() throws Exception
	{
		if (testClient == null)
		{
			Log.error("[TestCase SetUp] Creating TestMobileClient (TMC)");

			testClient = new TestMobileClient();
			testClient.onModuleLoad();
		}
	}

	public void testSimple()
	{
		assertTrue(true);
	}

}
