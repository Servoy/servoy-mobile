package com.servoy.mobile.client.angular;

import com.google.gwt.core.client.JavaScriptObject;

public class Proxy
{
	public static native Proxy create(JavaScriptObject target, Handler handler) /*-{
	    return new Proxy(target,handler);
	  }-*/;

}