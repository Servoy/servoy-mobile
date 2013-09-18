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

import com.servoy.base.scripting.api.IJSSecurity;
import com.servoy.mobile.client.MobileClient;

/**
 * @author jcompagner
 *
 */
@Export
public class JSSecurity implements Exportable, IJSSecurity
{
	private final MobileClient application;

	public JSSecurity(MobileClient application)
	{
		this.application = application;

		export(ExporterUtil.wrap(this));
		// TODO are there any methods relevant for mobile?
	}

	private native void export(Object object)
	/*-{
		$wnd.security = object;
	}-*/;

	@Override
	public Object authenticate(String authenticator_solution, String method, Object[] credentials)
	{
		if (credentials != null && credentials.length == 2)
		{
			application.doLogin((String)credentials[0], (String)credentials[1]);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.base.scripting.api.IJSSecurity#logout()
	 */
	@Override
	public void logout()
	{
		application.clearCredentials();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.base.scripting.api.IJSSecurity#getUserName()
	 */
	@Override
	public String getUserName() throws Exception
	{
		String[] credentials = application.getFoundSetManager().getCredentials();
		if (credentials != null && credentials.length > 0) return credentials[0];
		return null;
	}
}
