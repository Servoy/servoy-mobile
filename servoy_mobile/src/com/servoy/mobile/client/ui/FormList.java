package com.servoy.mobile.client.ui;

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

import com.google.gwt.core.client.JsArray;
import com.servoy.mobile.client.persistence.Component;
import com.servoy.mobile.client.persistence.Form;
import com.servoy.mobile.client.persistence.GraphicalComponent;
import com.servoy.mobile.client.persistence.BaseComponent;
import com.sksamuel.jqm4gwt.list.JQMList;

/**
 * List based on form UI
 * 
 * @author gboros
 */
public class FormList extends JQMList
{
	private String listItemButtonDP, listItemAsideDP, listItemCountDP, listItemImageDP;
	
	public FormList(Form form)
	{
		JsArray<Component> formComponents = form.getComponents();
	
		GraphicalComponent component;
		BaseComponent.MobileProperties mobileProperties;

		for(int i = 0; i < formComponents.length(); i++)
		{
			component = formComponents.get(i).isGraphicalComponent();
			if(component != null)
			{
				mobileProperties = component.getMobileProperties();
				if(mobileProperties != null)
				{
					if(mobileProperties.isListItemButton())
					{
						listItemButtonDP = component.getDataProviderID();
						//component.getActionMethodID()
					}
					else if(mobileProperties.isListItemAside())
					{
						listItemAsideDP = component.getDataProviderID();
					}
					else if(mobileProperties.isListItemCount())
					{
						listItemCountDP = component.getDataProviderID();
					}
					else if(mobileProperties.isListItemImage())
					{
						listItemImageDP = component.getDataProviderID();
					}
				}
			}
		}
	}
}
