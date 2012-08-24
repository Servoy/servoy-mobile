package com.servoy.mobile.client.solutionmodel;

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

/**
 * @author gboros
 */
public class JSSolutionModel extends JavaScriptObject
{
	public static final int FORM_SEARCH_BY_NAME = 0;
	public static final int FORM_SEARCH_BY_UUID = 1;
	
	protected JSSolutionModel() {}

	public final native int length() /*-{ return this.length; }-*/;
	public final native JSForm get(int i) /*-{ return this[i];     }-*/;
	
	public final JSForm getForm(int searchMode, String searchValue)
	{
		JSForm form;
		for(int i = 0; i < length(); i++)
		{
			form = get(i);
			switch(searchMode)
			{
				case FORM_SEARCH_BY_NAME:
					if(form.getName().equals(searchValue)) return form;
					break;
				case FORM_SEARCH_BY_UUID:
					if(form.getUUID().equals(searchValue)) return form;
					break;
			}
		}
		
		return null;
	}
}
