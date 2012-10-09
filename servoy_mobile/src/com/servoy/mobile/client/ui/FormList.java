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
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.servoy.j2db.scripting.api.IJSEvent;
import com.servoy.mobile.client.FormController;
import com.servoy.mobile.client.dataprocessing.DataAdapterList;
import com.servoy.mobile.client.dataprocessing.FoundSet;
import com.servoy.mobile.client.dataprocessing.IDisplayRelatedData;
import com.servoy.mobile.client.dataprocessing.Record;
import com.servoy.mobile.client.persistence.BaseComponent;
import com.servoy.mobile.client.persistence.Component;
import com.sksamuel.jqm4gwt.list.JQMList;
import com.sksamuel.jqm4gwt.list.JQMListItem;

/**
 * List based on form UI
 * 
 * @author gboros
 */
public class FormList extends JQMList implements IDisplayRelatedData
{
	private final FormController formController;
	private final DataAdapterList dal;
	private final Executor executor;
	private final String relationName;
	private String listItemTextDP, listItemAsideDP, listItemCountDP, listItemImageDP;
	private String listItemStaticText, listItemStaticAside;
	private String listItemOnAction;

	public FormList(FormController formController, DataAdapterList dal, Executor executor)
	{
		this(formController, dal, executor, null);
	}

	public FormList(FormController formController, DataAdapterList dal, Executor executor, String relationName)
	{
		this.formController = formController;
		this.dal = dal;
		this.executor = executor;
		this.relationName = relationName;

		JsArray<Component> formComponents = formController.getForm().getComponents();

		Component component;
		BaseComponent.MobileProperties mobileProperties;

		for (int i = 0; i < formComponents.length(); i++)
		{
			component = formComponents.get(i);
			if (component != null)
			{
				mobileProperties = component.getMobileProperties();
				if (mobileProperties != null)
				{
					if (mobileProperties.isListItemButton())
					{
						listItemTextDP = component.isGraphicalComponent().getDataProviderID();
						listItemOnAction = component.isGraphicalComponent().getActionMethodID();
						listItemStaticText = component.isGraphicalComponent().getText();
					}
					else if (mobileProperties.isListItemAside())
					{
						listItemAsideDP = component.isField().getDataProviderID();
						listItemStaticAside = component.isField().getText();
					}
					else if (mobileProperties.isListItemCount())
					{
						listItemCountDP = component.isField().getDataProviderID();
					}
					else if (mobileProperties.isListItemImage())
					{
						listItemImageDP = component.isField().getDataProviderID();
					}
				}
			}
		}

		setInset(true);
	}

	private FoundSet relatedFoundset;

	/*
	 * @see com.servoy.mobile.client.dataprocessing.IDisplayRelatedData#setRecord(com.servoy.mobile.client.dataprocessing.Record)
	 */
	@Override
	public void setRecord(Record parentRecord)
	{
		if (relationName != null)
		{
			relatedFoundset = parentRecord.getRelatedFoundSet(relationName);
			refreshList();
		}
	}

	public void refreshList()
	{
		createList(relatedFoundset != null ? relatedFoundset : formController.getFormModel());
		refresh();
	}

	private void createList(FoundSet foundset)
	{
		clear();
		int foundsetSize = foundset.getSize();
		JQMListItem listItem;
		Record listItemRecord;
		Object dpValue;
		for (int i = 0; i < foundsetSize; i++)
		{
			listItemRecord = foundset.getRecord(i);
			dpValue = listItemStaticText;
			dpValue = dal.getRecordValue(listItemRecord, listItemTextDP);
			listItem = addItem(dpValue != null ? dpValue.toString() : ""); //$NON-NLS-1$

			dpValue = listItemStaticAside;
			dpValue = dal.getRecordValue(listItemRecord, listItemAsideDP);
			if (dpValue != null) listItem.setAside(dpValue.toString());

			dpValue = dal.getRecordValue(listItemRecord, listItemCountDP);
			if (dpValue instanceof Integer) listItem.setCount((Integer)dpValue);
			else if (dpValue instanceof Double) listItem.setCount(Integer.valueOf(((Double)dpValue).intValue()));

			dpValue = dal.getRecordValue(listItemRecord, listItemImageDP);
			if (dpValue != null) listItem.setImage(dpValue.toString(), false);

			if (listItemOnAction != null)
			{
				listItem.addClickHandler(new ClickHandler()
				{
					@Override
					public void onClick(ClickEvent event)
					{
						executor.fireEventCommand(IJSEvent.ACTION, listItemOnAction, null, null);
					}
				});
			}
		}
	}
}
