package com.servoy.mobile.client.angular;

public class Handler
{
	public static native Handler create() /*-{
	  return {
		  	get: function(target, prop, receiver) {
		  	if (target.hasApi(prop)) {
		  		return function() {
		  			return target.executeApi(prop, arguments);
		  		}
		  	}
		    return Reflect.get(target, prop, receiver);
		  }
		}
	}-*/;

}