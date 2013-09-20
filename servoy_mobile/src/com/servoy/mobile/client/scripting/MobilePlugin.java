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
import org.timepedia.exporter.client.Exporter;
import org.timepedia.exporter.client.ExporterUtil;
import org.timepedia.exporter.client.Getter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.servoy.base.scripting.api.IJSRecord;
import com.servoy.mobile.client.MobileClient;
import com.servoy.mobile.client.dataprocessing.FoundSet;
import com.servoy.mobile.client.scripting.solutionhelper.SolutionHelper;
import com.sksamuel.jqm4gwt.Mobile;

/**
 * Mobile implementation side for the plugin seen in Servoy developer
 * @author lvostinar
 */
@Export
public class MobilePlugin implements Exportable
{

	private SolutionHelper solutionHelper;
	private final MobileClient client;

	private final Exporter solutionHelperExport;

	public MobilePlugin(MobileClient client)
	{
		this.client = client;
		solutionHelperExport = (Exporter)GWT.create(SolutionHelper.class);
	}

	@Getter
	public SolutionHelper getSolutionHelper()
	{
		if (solutionHelper == null) solutionHelper = new SolutionHelper(client.getSolutionModel());
		return solutionHelper;
	}

	@Getter("SolutionHelper")
	public JavaScriptObject getSolutionHelperConstants()
	{
//		return solutionHelperExport;
//		return getSolutionHelper();
		return solutionHelperExport.getJsConstructor();
//		return getSolutionHelperExportedClass();
	}

//	private final native JavaScriptObject getSolutionHelperExportedClass() /*-{
//		return $wnd.SolutionHelper;
//	}-*/;


	public boolean isOnline()
	{
		return client.isOnline();
	}

	public void clearLocalData()
	{
		client.getFoundSetManager().clearLocalStorage();
	}

	public void loadData()
	{
		loadData(null, null);
	}

	public void loadData(JavaScriptObject successCallback, JavaScriptObject errorHandler)
	{
		Mobile.showLoadingDialog(client.getI18nMessageWithFallback("loading"));
		client.load(successCallback, errorHandler);
	}

	public void remoteSearch(FoundSet foundset, JavaScriptObject successCallback, JavaScriptObject errorHandler)
	{
		if (foundset.isInFind())
		{
			client.load(foundset, successCallback, errorHandler);
		}
		else
		{
			throw new RuntimeException("Foundset must be in find mode when calling remoteSearch()");
		}
	}

	public void syncData()
	{
		client.sync();
	}

	public void syncData(JavaScriptObject successCallback, JavaScriptObject errorHandler)
	{
		client.sync(successCallback, errorHandler);
	}

	public native int getUsedStorageSpace()
	/*-{
		return unescape(encodeURIComponent(JSON.stringify(localStorage))).length * 2;
	}-*/;

	public native void getCurrentPosition(JavaScriptObject successCallback, JavaScriptObject errorHandler, Object options)
	/*-{
		if (navigator.geolocation) {
			navigator.geolocation.getCurrentPosition(successCallback,
					errorHandler, options);
		} else {
			alert("Sorry, browser does not support geolocation!");
		}
	}-*/;


	public void call(String telNumber)
	{
		simulateClick("tel:" + telNumber); //$NON-NLS-1$
	}

	public void email(String emailAddress)
	{
		simulateClick("mailto:" + emailAddress); //$NON-NLS-1$
	}

	private native void simulateClick(String link)/*-{
		if ($wnd.$.mobile.activePage) {
			if ($wnd.$("#servoyanchor").length < 1) {
				$wnd.$.mobile.activePage.append("<a id='servoyanchor'></a>");
			}
			$wnd.$("#servoyanchor").attr('href', link);
			$wnd._ServoyUtils_.simulateClick($wnd.$("#servoyanchor").get(0));
		}
	}-*/;

	public String getMarkupId(Object element)
	{
		Object gwtInstance = ExporterUtil.gwtInstance(element);
		if (gwtInstance instanceof AbstractRuntimeBaseComponent< ? , ? >)
		{
			return ((AbstractRuntimeBaseComponent< ? , ? >)gwtInstance).getComponent().getId();
		}
		return null;
	}

	public Object getUUIDPKValueAsString(IJSRecord record)
	{
		Object[] pks = record.getPKs();
		String[] ret = new String[pks.length];
		for (int i = 0; i < pks.length; i++)
		{
			Object pk = pks[i];
			if (pk instanceof Number)
			{
				String uuid = client.getFoundSetManager().getUUIDPKValueAsString(((Number)pk).intValue());
				if (uuid != null)
				{
					ret[i] = uuid;
				}
				else
				{
					ret[i] = pk.toString();
				}
			}
			else
			{
				ret[i] = pk.toString();
			}
		}
		if (ret.length == 1)
		{
			return ret[0];
		}
		else
		{
			return ret;
		}
	}
}
