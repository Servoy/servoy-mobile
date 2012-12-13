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
import org.timepedia.exporter.client.Getter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.servoy.mobile.client.MobileClient;
import com.servoy.mobile.client.scripting.solutionhelper.SolutionHelper;

/**
 * @author lvostinar
 *
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

	public void syncData()
	{
		client.sync();
	}

	public native int getUsedStorageSpace()
	/*-{
		return unescape(encodeURIComponent(JSON.stringify(localStorage))).length * 2;
	}-*/;

	public native void getCurrentPosition(JavaScriptObject successCallback, JavaScriptObject errorHandler, String options)
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
}
