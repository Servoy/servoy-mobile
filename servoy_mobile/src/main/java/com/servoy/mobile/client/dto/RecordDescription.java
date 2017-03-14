package com.servoy.mobile.client.dto;

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

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;

/**
 * @author jblok
 */
public class RecordDescription extends JavaScriptObject
{
	protected RecordDescription()
	{
	}

	public final native void setPK(int pk)
	/*-{
		this.pk = pk;
	}-*/;

	public final native void setPK(Object pk)
	/*-{
		this.pk = pk;
	}-*/;

	public final native Object getPK()
	/*-{
		if (typeof (this.pk) == 'number') {
			//do manual boxing
			return $wnd.internal.Utils.wrapIfPrimitive(this.pk);
		}
		return this.pk;
	}-*/;

	public final native JsArrayString getRFS()
	/*-{
		if (!this.rfs)
			this.rfs = new Array();
		return this.rfs;
	}-*/;

	public final native void clearRFS()
	/*-{
		this.rfs = null;
	}-*/;

	public static RecordDescription newInstance(int pk)
	{
		RecordDescription rd = JavaScriptObject.createObject().cast();
		rd.setPK(pk);
		return rd;
	}

	public static RecordDescription newInstance(Object pk)
	{
		RecordDescription rd = JavaScriptObject.createObject().cast();
		rd.setPK(pk);
		return rd;
	}
}
