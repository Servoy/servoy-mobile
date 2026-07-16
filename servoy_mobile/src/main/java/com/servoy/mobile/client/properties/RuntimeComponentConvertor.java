package com.servoy.mobile.client.properties;

import com.servoy.mobile.client.ui.PropertySpec;
import com.servoy.mobile.client.ui.WebRuntimeComponent;

public class RuntimeComponentConvertor implements IPropertyConverter
{

	@Override
	public Object convertForClient(Object value, WebRuntimeComponent component, PropertySpec propertyType)
	{
		return extractComponentName(value);
	}

	@Override
	public Object convertFromClient(String key, Object value, WebRuntimeComponent component, PropertySpec propertyType)
	{
		return value;
	}

	public static native String extractComponentName(Object value) /*-{
		if (value == null) return null;
		if (typeof value === 'string') return value;
		if (typeof value.getName === 'function') {
			return value.getName();
		}
		return null;
	}-*/;
}
