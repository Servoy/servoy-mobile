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
import com.google.gwt.core.client.JsArray;

/**
 * @author jblok
 */
public class OfflineDataDescription extends JavaScriptObject
{
	protected OfflineDataDescription()
	{
	}

	public final native String getEntityPrefix()
	/*-{
		if (this.entityPrefix) {
			return this.entityPrefix;
		} else {
			return null;
		}
	}-*/;

	public final native JsArray<EntityDescription> getEntities()
	/*-{
		return this.entities;
	}-*/;

	public final native JsArray<FoundSetDescription> getFoundSets()
	/*-{
		if (this.foundSets) {
			return this.foundSets;
		} else {
			return null;
		}
	}-*/;
}
