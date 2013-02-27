package com.servoy.mobile.test.client;

import com.allen_sauer.gwt.log.client.Log;
import com.servoy.mobile.client.MobileClient;

/**
 * Mobile client that is able to automate some tasks for unit testing.
 * 
 * @author acostescu
 */
public class TestMobileClient extends MobileClient
{

	@Override
	public void onModuleLoad()
	{
		Log.error("[TMC] initialize");
		super.initialize();
		Log.error("[TMC] initialize finished waiting for jQuery mobile page activation"); //$NON-NLS-1$
	}

	@Override
	protected void onStartPageShown()
	{
		// avoid trial page
		getFlattenedSolution().setSkipConnect(true);

		super.onStartPageShown();
	}

	@Override
	public void sync()
	{
		Log.error("[TMC] logging in automatically & syncing");
		// automatically login in case of test client
		if (getFlattenedSolution().getMustAuthenticate() && !getOfflineDataProxy().hasCredentials())
		{
			setLoginCredentials("demo", "demo"); // TODO ac make this configurable - for unit testing
		}

		super.sync();
	}

}
