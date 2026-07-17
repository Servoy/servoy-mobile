/*
 This file belongs to the Servoy development and deployment environment, Copyright (C) 1997-2024 Servoy BV

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

import com.servoy.mobile.client.ui.PropertySpec;
import com.servoy.mobile.client.ui.WebRuntimeComponent;

import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

/**
 * Converter for the "valuelistConfig" property type.
 * <p>
 * Mirrors the server side <code>com.servoy.j2db.server.ngclient.property.types.ValuelistConfigPropertyType</code>
 * / <code>ValuelistConfigTypeSabloValue</code>: the raw (unconverted) value of such a property is a plain JSON
 * object with "filterType", "filterDestination" and "allowNewEntries" keys. The valuelistConfig property itself
 * is not rendered/used by the client on its own, so this converter just forwards those 3 values, same as the
 * server does in <code>ValuelistConfigPropertyType.toJSON</code>.
 * <p>
 * It also exposes static helpers (mirroring <code>ValuelistConfigTypeSabloValue#useFilterOnRealValues()</code>,
 * <code>#useFilterWithContains()</code> and <code>#getAllowNewEntries()</code>) so that {@link ValuelistConvertor}
 * can consult the same settings, through its sibling "config" property, to drive filtering behavior for its
 * "valuelist" typed property - mirroring the pattern used server side in
 * <code>ValueListTypeSabloValue#initializeIfPossibleAndNeeded()</code>.
 *
 * @author jcomp
 */
public class ValueListConfigConverter implements IPropertyConverter
{
	public static final String STARTS_WITH = "STARTS_WITH";
	public static final String CONTAINS = "CONTAINS";
	public static final String DISPLAY_VALUE = "DISPLAY_VALUE";
	public static final String DISPLAY_AND_REAL_VALUE = "DISPLAY_AND_REAL_VALUE";

	@Override
	public Object convertForClient(Object value, WebRuntimeComponent component, PropertySpec propertyType)
	{
		JsPropertyMap<Object> map = JsPropertyMap.of();
		map.set("filterType", getFilterType(value));
		map.set("filterDestination", getFilterDestination(value));
		map.set("allowNewEntries", Boolean.valueOf(getAllowNewEntries(value)));
		return map;
	}

	@Override
	public Object convertFromClient(String key, Object value, WebRuntimeComponent component, PropertySpec propertyType)
	{
		// we do not allow changes coming in from the client, same as ValuelistConfigPropertyType.fromJSON
		return component != null ? component.getProperty(key) : null;
	}

	/**
	 * @param rawConfigValue the raw (unconverted) value of a "valuelistConfig" typed property, may be null
	 * @return the configured filter type ({@link #STARTS_WITH} or {@link #CONTAINS}), or null if not configured
	 */
	public static String getFilterType(Object rawConfigValue)
	{
		JsPropertyMap<Object> config = asMap(rawConfigValue);
		return config != null && config.has("filterType") ? Js.asString(config.get("filterType")) : null;
	}

	/**
	 * @param rawConfigValue the raw (unconverted) value of a "valuelistConfig" typed property, may be null
	 * @return the configured filter destination ({@link #DISPLAY_VALUE} or {@link #DISPLAY_AND_REAL_VALUE}), or null if not configured
	 */
	public static String getFilterDestination(Object rawConfigValue)
	{
		JsPropertyMap<Object> config = asMap(rawConfigValue);
		return config != null && config.has("filterDestination") ? Js.asString(config.get("filterDestination")) : null;
	}

	/**
	 * @param rawConfigValue the raw (unconverted) value of a "valuelistConfig" typed property, may be null
	 * @return whether new entries (values typed by the user that are not part of the valuelist) are allowed; defaults to
	 *         true, same as <code>ValuelistConfigTypeSabloValue</code>'s default
	 */
	public static boolean getAllowNewEntries(Object rawConfigValue)
	{
		JsPropertyMap<Object> config = asMap(rawConfigValue);
		return config == null || !config.has("allowNewEntries") || Js.asBoolean(config.get("allowNewEntries"));
	}

	/**
	 * @param rawConfigValue the raw (unconverted) value of a "valuelistConfig" typed property, may be null
	 * @return true if the filter should also search real values (in addition to display values), same as
	 *         <code>ValuelistConfigTypeSabloValue#useFilterOnRealValues()</code>
	 */
	public static boolean useFilterOnRealValues(Object rawConfigValue)
	{
		return DISPLAY_AND_REAL_VALUE.equals(getFilterDestination(rawConfigValue));
	}

	/**
	 * @param rawConfigValue the raw (unconverted) value of a "valuelistConfig" typed property, may be null
	 * @return true if the filter should use "contains" matching instead of "starts with", same as
	 *         <code>ValuelistConfigTypeSabloValue#useFilterWithContains()</code>
	 */
	public static boolean useFilterWithContains(Object rawConfigValue)
	{
		return CONTAINS.equals(getFilterType(rawConfigValue));
	}

	private static JsPropertyMap<Object> asMap(Object rawConfigValue)
	{
		return rawConfigValue != null ? Js.<JsPropertyMap<Object>> uncheckedCast(rawConfigValue) : null;
	}
}
