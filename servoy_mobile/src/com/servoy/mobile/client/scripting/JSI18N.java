/*
 This file belongs to the Servoy development and deployment environment, Copyright (C) 1997-2012 Servoy BV

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

package com.servoy.mobile.client.scripting;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.Exportable;
import org.timepedia.exporter.client.ExporterUtil;

import com.servoy.base.scripting.api.IJSI18N;
import com.servoy.mobile.client.SolutionI18nProvider;

@Export
public class JSI18N implements Exportable, IJSI18N
{
	private final SolutionI18nProvider i18nProvider;

	public JSI18N(SolutionI18nProvider i18nProvider)
	{
		this.i18nProvider = i18nProvider;
		export(ExporterUtil.wrap(this));
	}

	public String getCurrentLanguage()
	{
		return i18nProvider.getLocale();
	}

	public void setLocale(String language, String country)
	{
		i18nProvider.setLocale(language, country);
	}

	@Override
	public String getI18NMessage(String i18nKey)
	{
		return i18nProvider.getI18NMessage(i18nKey);
	}

	@Override
	public String getI18NMessage(String i18nKey, Object[] dynamicValues)
	{
		return i18nProvider.getI18NMessage(i18nKey, dynamicValues);
	}

	@Override
	public void setI18NMessage(String i18nKey, String value)
	{
		i18nProvider.setI18NMessage(i18nKey, value);

	}

	private native void export(Object object)
	/*-{
		$wnd.i18n = object;
	}-*/;
}
