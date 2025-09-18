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
				} else if (target.hasProperty(prop)) {
					return target.getProperty(prop);
				}
				return Reflect.get(target, prop, receiver);
			},
			set: function(target, prop, value) {
				if (target.hasProperty(prop)) {
					return target.setProperty(prop, value);
				}
				return Reflect.get(target, prop, receiver);
			}
		}
	}-*/;

}