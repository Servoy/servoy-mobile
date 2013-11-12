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

import java.util.ArrayList;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Widget;
import com.servoy.base.persistence.IMobileProperties;
import com.servoy.base.scripting.api.IJSEvent;
import com.servoy.base.util.ITagResolver;
import com.servoy.base.util.TagParser;
import com.servoy.mobile.client.FormController;
import com.servoy.mobile.client.dataprocessing.DataAdapterList;
import com.servoy.mobile.client.dataprocessing.FoundSet;
import com.servoy.mobile.client.dataprocessing.FoundSetManager;
import com.servoy.mobile.client.dataprocessing.IDisplayRelatedData;
import com.servoy.mobile.client.dataprocessing.IFoundSetListener;
import com.servoy.mobile.client.dataprocessing.Record;
import com.servoy.mobile.client.dto.EntityDescription;
import com.servoy.mobile.client.dto.RelationDescription;
import com.servoy.mobile.client.persistence.AbstractBase;
import com.servoy.mobile.client.persistence.Component;
import com.servoy.mobile.client.persistence.Field;
import com.servoy.mobile.client.persistence.GraphicalComponent;
import com.servoy.mobile.client.scripting.IRuntimeComponent;
import com.sksamuel.jqm4gwt.events.TapEvent;
import com.sksamuel.jqm4gwt.events.TapHandlerForPageSwitch;
import com.sksamuel.jqm4gwt.list.JQMList;
import com.sksamuel.jqm4gwt.list.JQMListItem;

/**
 * List based on form UI
 *
 * @author gboros
 */
public class FormList extends JQMList implements IDisplayRelatedData, IFoundSetListener
{
	private FormController formController;
	private DataAdapterList dal;
	private final String relationName;
	private ArrayList<RelationDescription> relation;
	private String listItemTextDP, listItemSubtextDP, listItemCountDP, listItemImageDP, listItemHeaderDP;
	private String listItemStaticText, listItemStaticSubtext, listItemStaticHeader, listItemHeaderStyleclass;
	private String listItemOnAction;
	private String listItemDataIcon;
	private String listItemStyleclass;
	private final ArrayList<HandlerRegistration> tapRegistrations = new ArrayList<HandlerRegistration>();
	private HandlerRegistration listClickHandler;

	public FormList(FormController formController, JsArray<Component> formComponents, DataAdapterList dal, String relationName)
	{
		this.formController = formController;
		this.dal = dal;
		this.relationName = relationName;

		this.formController.addFoundsetListener(this);
		if (relationName != null && formController.getForm().getDataSource() != null)
		{
			String[] relationItems = relationName.split("\\."); //$NON-NLS-1$
			String entity = FoundSetManager.getEntityFromDataSource(formController.getForm().getDataSource());
			EntityDescription entityDescription = formController.getApplication().getFoundSetManager().getEntityDescription(entity);
			relation = new ArrayList<RelationDescription>();
			RelationDescription relationDesc;
			for (String relationItem : relationItems)
			{
				if (entityDescription == null)
				{
					relationDesc = null;
					Log.warn("No entity description found for " + entity);
				}
				else
				{
					relationDesc = entityDescription.getPrimaryRelation(relationItem);
				}

				if (relationDesc != null)
				{
					relation.add(relationDesc);
					entity = relationDesc.getForeignEntityName();
					entityDescription = formController.getApplication().getFoundSetManager().getEntityDescription(entity);
				}
				else
				{
					relation.clear();
					Log.warn("No primary relation found for " + relationItem + " on " + entity);
					break;
				}
			}
			if (relation.size() == 0) relation = null;
		}

		for (int i = 0; i < formComponents.length(); i++)
		{
			Component component = formComponents.get(i);
			if (component != null)
			{
				AbstractBase.MobileProperties mobileProperties = component.getMobileProperties();
				if (mobileProperties != null)
				{
					if (mobileProperties.getPropertyValue(IMobileProperties.LIST_ITEM_BUTTON).booleanValue())
					{
						listItemTextDP = fixRelatedDataproviderID(GraphicalComponent.castIfPossible(component).getDataProviderID());
						listItemOnAction = GraphicalComponent.castIfPossible(component).getOnActionMethodCall();
						listItemStaticText = GraphicalComponent.castIfPossible(component).getText();
						listItemDataIcon = mobileProperties.getPropertyValue(IMobileProperties.DATA_ICON);
						listItemStyleclass = GraphicalComponent.castIfPossible(component).getStyleClass();
					}
					else if (mobileProperties.getPropertyValue(IMobileProperties.LIST_ITEM_SUBTEXT).booleanValue())
					{
						listItemSubtextDP = fixRelatedDataproviderID(GraphicalComponent.castIfPossible(component).getDataProviderID());
						listItemStaticSubtext = GraphicalComponent.castIfPossible(component).getText();
					}
					else if (mobileProperties.getPropertyValue(IMobileProperties.LIST_ITEM_COUNT).booleanValue())
					{
						listItemCountDP = fixRelatedDataproviderID(Field.castIfPossible(component).getDataProviderID());
					}
					else if (mobileProperties.getPropertyValue(IMobileProperties.LIST_ITEM_IMAGE).booleanValue())
					{
						listItemImageDP = fixRelatedDataproviderID(Field.castIfPossible(component).getDataProviderID());
					}
					else if (mobileProperties.getPropertyValue(IMobileProperties.LIST_ITEM_HEADER).booleanValue())
					{
						listItemHeaderDP = fixRelatedDataproviderID(GraphicalComponent.castIfPossible(component).getDataProviderID());
						listItemStaticHeader = GraphicalComponent.castIfPossible(component).getText();
						listItemHeaderStyleclass = GraphicalComponent.castIfPossible(component).getStyleClass();
					}
				}
			}
		}

		setInset(true);
		listClickHandler = addHandler(new ClickHandler()
		{
			@Override
			public void onClick(ClickEvent event)
			{
				EventTarget target = event.getNativeEvent().getEventTarget();
				Element element = Element.as(target);
				for (JQMListItem listItem : getItems())
				{
					if (listItem != null && listItem.getElement().isOrHasChild(element))
					{
						listItem.fireEvent(event);
						break;
					}
				}
			}
		}, ClickEvent.getType());
	}

	private String fixRelatedDataproviderID(String dataProviderID)
	{
		if (relation != null && dataProviderID != null && dataProviderID.startsWith(relationName + '.'))
		{
			return dataProviderID.substring(relationName.length() + 1);
		}
		return dataProviderID;
	}

	private FoundSet relatedFoundset;
	private FoundSet foundSet;

	/*
	 * @see com.servoy.mobile.client.dataprocessing.IDisplayRelatedData#setRecord(com.servoy.mobile.client.dataprocessing.Record)
	 */
	@Override
	public void setRecord(Record parentRecord)
	{
		if (relation != null)
		{
			Record record = parentRecord;
			if (record == null)
			{
				relatedFoundset = null;
			}
			else
			{
				for (int i = 0; i < relation.size(); i++)
				{
					RelationDescription relationItem = relation.get(i);
					relatedFoundset = relationItem.isSelfRef() ? record.getParent() : record.getRelatedFoundSet(relationItem.getName());
					if (relatedFoundset == null) break;
					if (i < relation.size() - 1)
					{
						record = relatedFoundset.getSelectedRecord();
						if (record == null)
						{
							relatedFoundset = null;
							break;
						}
					}
				}
			}
			refreshList();
		}
	}

	@Override
	public void contentChanged()
	{
		refreshList();
	}

	public void destroy()
	{
		listClickHandler.removeHandler();
		listClickHandler = null;
		clear();
		removeFromParent();
		if (this.foundSet != null) this.foundSet.removeFoundSetListener(this);
		formController.removeFoundsetListener(this);
		formController = null;
		dal = null;
	}

	public void refreshList()
	{
		createList(relatedFoundset != null ? relatedFoundset : formController.getFormModel());
		forceRefresh(getId());
	}

	protected native void forceRefresh(String id) /*-{
													$wnd.$("#" + id).listview('refresh', true);
													}-*/;


	@Override
	public void clear()
	{
//		Iterator<JQMListItem> listItemIte = getItems().iterator();
//		while (listItemIte.hasNext())
//			listItemIte.next().removeFromParent();
		for (int i = 0; i < tapRegistrations.size(); i++)
			tapRegistrations.get(i).removeHandler();
		tapRegistrations.clear();
		super.clear();
	}

	private void createList(FoundSet foundset)
	{
		if (this.foundSet != null) this.foundSet.removeFoundSetListener(this);
		this.foundSet = foundset;
		if (this.foundSet != null) this.foundSet.addFoundSetListener(this);
		clear();
		if (foundset != null)
		{
			int foundsetSize = foundset.getSize();
			int listWidgetCount = 0;

			Object dpValue = dal.getRecordValue(null, listItemHeaderDP);
			if (dpValue == null)
			{
				dpValue = TagParser.processTags(formController.getApplication().getI18nProvider().getI18NMessageIfPrefixed(listItemStaticHeader), dal,
					formController.getApplication().getI18nProvider());
			}
			if (dpValue != null)
			{
				HasText divider = addDivider(dpValue.toString());
				if (divider instanceof Widget) setWidgetTheme((Widget)divider, listItemHeaderStyleclass);
				listWidgetCount = 1;
			}

			for (int i = 0; i < foundsetSize; i++)
			{
				Record listItemRecord = foundset.getRecord(i);
				listItemTagResolver.setRecord(listItemRecord);

				dpValue = dal.getRecordValue(listItemRecord, listItemTextDP);
				if (dpValue == null)
				{
					dpValue = TagParser.processTags(formController.getApplication().getI18nProvider().getI18NMessageIfPrefixed(listItemStaticText),
						listItemTagResolver, formController.getApplication().getI18nProvider());
				}
				JQMListItem listItem = addItem(listWidgetCount, dpValue != null ? dpValue.toString() : ""); //$NON-NLS-1$
				listWidgetCount++;

				dpValue = dal.getRecordValue(listItemRecord, listItemCountDP);
				if (dpValue instanceof Integer) listItem.setCount((Integer)dpValue);
				else if (dpValue instanceof Double) listItem.setCount(Integer.valueOf(((Double)dpValue).intValue()));

				dpValue = dal.getRecordValue(listItemRecord, listItemImageDP);
				if (dpValue != null) listItem.setImage(dpValue.toString(), false);

				final int selIndex = i;
				if (listItemOnAction != null)
				{
					tapRegistrations.add(listItem.addTapHandler(new TapHandlerForPageSwitch()
					{
						@Override
						public void onSafeTap(TapEvent event)
						{
							foundSet.setSelectedIndexInternal(selIndex);
							formController.getExecutor().fireEventCommand(IJSEvent.ACTION, listItemOnAction, getRuntimeComponent(), null);
						}
					}));
				}
				dpValue = dal.getRecordValue(listItemRecord, listItemSubtextDP);
				if (dpValue == null)
				{
					dpValue = TagParser.processTags(formController.getApplication().getI18nProvider().getI18NMessageIfPrefixed(listItemStaticSubtext),
						listItemTagResolver, formController.getApplication().getI18nProvider());
				}
				if (dpValue != null) listItem.addText(dpValue.toString());

				if (listItemDataIcon != null) listItem.getElement().setAttribute("data-icon", listItemDataIcon); //$NON-NLS-1$

				setWidgetTheme(listItem, listItemStyleclass);
			}
		}
	}

	public IRuntimeComponent getRuntimeComponent()
	{
		return null; // overridden in when FormList is used as component
	}

	public static void setWidgetTheme(Widget widget, String styleClass)
	{
		if (styleClass == null) widget.getElement().removeAttribute("data-theme");
		else widget.getElement().setAttribute("data-theme", styleClass);
	}

	class ListItemTagResolver implements ITagResolver
	{
		private Record record;

		void setRecord(Record rec)
		{
			record = rec;
		}

		@Override
		public String getStringValue(String name)
		{
			Object valueObj = dal.getRecordValue(record, name);
			return valueObj != null ? valueObj.toString() : null;
		}
	}

	private final ListItemTagResolver listItemTagResolver = new ListItemTagResolver();
}
