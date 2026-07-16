package com.servoy.mobile.client.properties;

import com.servoy.mobile.client.ui.PropertySpec;
import com.servoy.mobile.client.ui.WebRuntimeComponent;

public class FormConvertor implements IPropertyConverter
{

	@Override
	public Object convertForClient(Object value, WebRuntimeComponent component, PropertySpec propertyType)
	{
		return extractFormName(value);
	}

	@Override
	public Object convertFromClient(String key, Object value, WebRuntimeComponent component, PropertySpec propertyType)
	{
		return value;
	}

	public static native String extractFormName(Object value) /*-{
		if (value == null) return null;
		if (typeof value === 'string') return value;
		if (value.controller && typeof value.controller.getName === 'function') {
			return value.controller.getName();
		}
		if (value._formname_) return value._formname_;
		return null;
	}-*/;
}
