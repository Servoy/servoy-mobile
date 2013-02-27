package com.servoy.mobile.test.server;

import com.google.gwt.dev.cfg.ModuleDef;
import com.google.gwt.junit.JUnitShell.Strategy;
import com.google.gwt.junit.PropertyDefiningStrategy;
import com.google.gwt.junit.client.GWTTestCase;

/**
 * This is the server-side code for test cases.
 * 
 * @author acostescu
 */
public abstract class MobileSolutionTestCase extends GWTTestCase
{


	@Override
	public String getModuleName()
	{
		return "com.servoy.mobile.test.MobileTestClient";
	}

	// not really used yet (just for debug); but this class might be needed by SVY-3939 (Step 4)
	@Override
	protected Strategy createStrategy()
	{
		return new PropertyDefiningStrategy(this)
		{
			@Override
			public void processModule(ModuleDef module)
			{
				super.processModule(module);
//				module.getScripts().iterator().next()
			}
		};
	}

}
