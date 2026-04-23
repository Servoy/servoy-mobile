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

package com.servoy.mobile.client.ui;

import java.util.ArrayList;
import java.util.List;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.Exportable;
import org.timepedia.exporter.client.NoExport;

import com.servoy.mobile.client.FormView;

/**
 * @author lvostinar
 *
 */
@Export
public class WebRuntimeLayoutContainer implements Exportable
{
	@NoExport
	private final FormView formView;
	@NoExport
	private final String name;
	@NoExport
	private final List<String> cssClasses;

	public WebRuntimeLayoutContainer(FormView formView, String name, List<String> cssClasses)
	{
		this.formView = formView;
		this.name = name;
		this.cssClasses = cssClasses != null ? cssClasses : new ArrayList<String>();
	}

	public boolean addStyleClasses(String cls)
	{
		if (!cssClasses.contains(cls))
		{
			cssClasses.add(cls);
			formView.addContainerStyleClass(name, cls);
			return true;
		}
		return false;
	}

	public boolean removeStyleClasses(String cls)
	{
		if (cssClasses.remove(cls))
		{
			formView.removeContainerStyleClass(name, cls);
			return true;
		}
		return false;
	}

	public boolean hasStyleClasses(String cls)
	{
		return cssClasses.contains(cls);
	}

	public void setCSSStyle(String key, String value)
	{
		formView.addContainerCSSStyle(name, key, value);
	}

	public void removeCSSStyle(String key)
	{
		formView.removeContainerCSSStyle(name, key);
	}

	public String[] getStyleClasses()
	{
		return cssClasses.toArray(new String[0]);
	}
}
