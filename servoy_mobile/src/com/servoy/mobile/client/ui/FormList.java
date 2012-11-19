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
import com.servoy.j2db.util.ITagResolver;
import com.servoy.j2db.util.TagParser;
import com.servoy.mobile.client.FormController;
import com.servoy.mobile.client.dataprocessing.DataAdapterList;
import com.servoy.mobile.client.dataprocessing.FoundSet;
import com.servoy.mobile.client.dataprocessing.IDisplayRelatedData;
import com.servoy.mobile.client.dataprocessing.Record;
import com.servoy.mobile.client.persistence.BaseComponent;
import com.servoy.mobile.client.persistence.Component;
import com.servoy.mobile.client.persistence.Relation;
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
	private final Relation relation;
	private String listItemTextDP, listItemSubtextDP, listItemCountDP, listItemImageDP, listItemHeaderDP;
	private String listItemStaticText, listItemStaticSubtext, listItemStaticHeader;
	private String listItemOnAction;
	private String listItemDataIcon;

	public FormList(FormController formController, DataAdapterList dal)
	{
		this(formController, dal, null);
	}

	public FormList(FormController formController, DataAdapterList dal, Relation relation)
	{
		this.formController = formController;
		this.dal = dal;
		this.relation = relation;

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
						listItemDataIcon = mobileProperties.getDataIcon();
					}
					else if (mobileProperties.isListItemSubtext())
					{
						listItemSubtextDP = component.isGraphicalComponent().getDataProviderID();
						listItemStaticSubtext = component.isGraphicalComponent().getText();
					}
					else if (mobileProperties.isListItemCount())
					{
						listItemCountDP = component.isField().getDataProviderID();
					}
					else if (mobileProperties.isListItemImage())
					{
						listItemImageDP = component.isField().getDataProviderID();
					}
					else if (mobileProperties.isListItemHeader())
					{
						listItemHeaderDP = component.isGraphicalComponent().getDataProviderID();
						listItemStaticHeader = component.isGraphicalComponent().getText();
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
		if (relation != null)
		{
			relatedFoundset = relation.isSelfRef() ? parentRecord.getParent() : parentRecord.getRelatedFoundSet(relation.getName());
			formController.setModel(relatedFoundset);
			refreshList();
		}
	}

	public void refreshList()
	{
		createList(relatedFoundset != null ? relatedFoundset : formController.getFormModel());
		forceRefresh(getId());
	}

	protected native void forceRefresh(String id) /*-{
													$wnd.$("#" + id).listview('refresh', true);
													}-*/;

	private void createList(FoundSet foundset)
	{
		clear();
		int foundsetSize = foundset.getSize();
		JQMListItem listItem;
		Record listItemRecord;
		Object dpValue;

		int listWidgetCount = 0;

		dpValue = dal.getRecordValue(null, listItemHeaderDP);
		if (dpValue == null)
		{
			dpValue = TagParser.processTags(listItemStaticHeader, dal, formController.getApplication().getI18nProvider());
			if (dpValue != null && dpValue.toString().startsWith("i18n:"))
			{
				dpValue = formController.getApplication().getI18nProvider().getI18NMessageIfPrefixed(listItemStaticHeader);
			}
		}
		if (dpValue != null)
		{
			addDivider(dpValue.toString());
			listWidgetCount = 1;
		}

		for (int i = 0; i < foundsetSize; i++)
		{
			listItemRecord = foundset.getRecord(i);
			listItemTagResolver.setRecord(listItemRecord);

			dpValue = dal.getRecordValue(listItemRecord, listItemTextDP);
			if (dpValue == null)
			{
				dpValue = TagParser.processTags(listItemStaticText, listItemTagResolver, formController.getApplication().getI18nProvider());
				if (dpValue != null && dpValue.toString().startsWith("i18n:"))
				{
					dpValue = formController.getApplication().getI18nProvider().getI18NMessageIfPrefixed(listItemStaticText);
				}
			}
			listItem = addItem(listWidgetCount, dpValue != null ? dpValue.toString() : ""); //$NON-NLS-1$
			listWidgetCount++;

			dpValue = dal.getRecordValue(listItemRecord, listItemCountDP);
			if (dpValue instanceof Integer) listItem.setCount((Integer)dpValue);
			else if (dpValue instanceof Double) listItem.setCount(Integer.valueOf(((Double)dpValue).intValue()));

			dpValue = dal.getRecordValue(listItemRecord, listItemImageDP);
			if (dpValue != null) listItem.setImage(dpValue.toString(), false);

			final int selIndex = i + 1;
			listItem.addClickHandler(new ClickHandler()
			{
				@Override
				public void onClick(ClickEvent event)
				{
					formController.setSelectedIndex(selIndex);
					if (listItemOnAction != null) formController.getExecutor().fireEventCommand(IJSEvent.ACTION, listItemOnAction, null, null);
				}
			});

			dpValue = dal.getRecordValue(listItemRecord, listItemSubtextDP);
			if (dpValue == null)
			{
				dpValue = TagParser.processTags(listItemStaticSubtext, listItemTagResolver, formController.getApplication().getI18nProvider());
				if (dpValue != null && dpValue.toString().startsWith("i18n:"))
				{
					dpValue = formController.getApplication().getI18nProvider().getI18NMessageIfPrefixed(listItemStaticSubtext);
				}
			}
			if (dpValue != null) listItem.addText(dpValue.toString());

			if (listItemDataIcon != null) listItem.getElement().setAttribute("data-icon", listItemDataIcon); //$NON-NLS-1$
		}
	}

	class ListItemTagResolver implements ITagResolver
	{

		Record record;

		void setRecord(Record rec)
		{
			record = rec;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.servoy.j2db.util.ITagResolver#getStringValue(java.lang.String)
		 */
		@Override
		public String getStringValue(String name)
		{
			Object valueObj = dal.getRecordValue(record, name);
			return valueObj != null ? valueObj.toString() : null;
		}
	}

	private final ListItemTagResolver listItemTagResolver = new ListItemTagResolver();
}
