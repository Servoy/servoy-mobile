/*
 This file belongs to the Servoy development and deployment environment, Copyright (C) 1997-2026 Servoy BV

 This program is free software; you can redistribute it and/or modify it under
 the terms of the GNU Affero General Public License as published by the Free
 Software Foundation; either version 3 of the License, or (at your option) any
 later version.

 This program is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License along
 with this program; if not, see http://www.gnu.org/licenses or write to the Free
 Software Foundation,Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301
*/

package com.servoy.mobile.client.properties;

import com.servoy.mobile.client.MobileClient;
import com.servoy.mobile.client.ui.PropertySpec;
import com.servoy.mobile.client.ui.WebRuntimeComponent;

/**
 * Converter for the {@code tagstring} property type. Mirrors the leading {@code i18n:} handling of the server side
 * {@code TagStringPropertyType}: a value starting with {@code i18n:} is replaced by the translated message.
 */
public class TagStringConvertor implements IPropertyConverter
{
	private static final String I18N_PREFIX = "i18n:";

	private final MobileClient application;

	public TagStringConvertor(MobileClient application)
	{
		this.application = application;
	}

	@Override
	public Object convertForClient(Object value, WebRuntimeComponent component, PropertySpec propertyType)
	{
		// the i18n provider is not available yet when the converters are registered, so look it up at conversion time.
		// component is null when called through MobileClient.convertValueForType.
		if (value instanceof String && ((String)value).startsWith(I18N_PREFIX))
		{
			return application.getI18nProvider().getI18NMessageIfPrefixed((String)value);
		}
		return value;
	}

	@Override
	public Object convertFromClient(String key, Object value, WebRuntimeComponent component, PropertySpec propertyType)
	{
		return value;
	}

}
