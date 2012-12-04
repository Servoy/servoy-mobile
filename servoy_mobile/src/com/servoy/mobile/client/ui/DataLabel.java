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

import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasText;
import com.servoy.mobile.client.MobileClient;
import com.servoy.mobile.client.dataprocessing.IDisplayData;
import com.servoy.mobile.client.persistence.GraphicalComponent;
import com.servoy.mobile.client.scripting.IRuntimeComponent;
import com.servoy.mobile.client.scripting.RuntimeDataLabel;
import com.sksamuel.jqm4gwt.JQMWidget;
import com.sksamuel.jqm4gwt.html.FormLabel;
import com.sksamuel.jqm4gwt.html.Heading;

/**
 * Label UI
 *
 * @author gboros
 */
public class DataLabel extends JQMWidget implements HasText, IDisplayData, IGraphicalComponent, ISupportDataText
{
	private final FormLabel labelFor;
	private final Heading label;
	private final FlowPanel flow;
	protected final Executor executor;
	private final MobileClient application;
	private final RuntimeDataLabel scriptable;

	public DataLabel(GraphicalComponent gc, Executor executor, MobileClient application)
	{
		this.executor = executor;
		this.application = application;

		String id = Document.get().createUniqueId();

		flow = new FlowPanel();
		initWidget(flow);

		labelFor = new FormLabel();
		labelFor.setFor(id);

		label = new Heading(gc.getMobileProperties() != null ? gc.getMobileProperties().getHeaderSize() : 4,
			application.getI18nProvider().getI18NMessageIfPrefixed(gc.getText() != null ? gc.getText() : "")); //$NON-NLS-1$
		label.getElement().setId(id);

		flow.add(labelFor);
		flow.add(label);

		setDataRole("fieldcontain"); //$NON-NLS-1$
		addStyleName("jqm4gwt-fieldcontain"); //$NON-NLS-1$
		labelFor.addStyleName("label-labelfor"); //$NON-NLS-1$
		label.addStyleName("label-heading"); //$NON-NLS-1$
		setId();

		this.scriptable = new RuntimeDataLabel(application, executor, this, gc);
	}

	/*
	 * @see com.servoy.mobile.client.dataprocessing.IDisplayData#getValueObject()
	 */
	@Override
	public Object getValueObject()
	{
		return getText();
	}

	/*
	 * @see com.servoy.mobile.client.dataprocessing.IDisplayData#setValueObject(java.lang.Object)
	 */
	@Override
	public void setValueObject(Object data)
	{
		scriptable.setText(data);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.mobile.client.scripting.IScriptableProvider#getScriptObject()
	 */
	@Override
	public IRuntimeComponent getRuntimeComponent()
	{
		return scriptable;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.gwt.event.dom.client.HasClickHandlers#addClickHandler(com.google.gwt.event.dom.client.ClickHandler)
	 */
	@Override
	public HandlerRegistration addClickHandler(ClickHandler handler)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.gwt.user.client.ui.HasText#getText()
	 */
	@Override
	public String getText()
	{
		return label.getText();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.gwt.user.client.ui.HasText#setText(java.lang.String)
	 */
	@Override
	public void setText(String text)
	{
		label.setText(text);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.mobile.client.ui.ISupportDataText#setDataText(java.lang.String)
	 */
	@Override
	public void setDataText(String dataText)
	{
		labelFor.setText(dataText);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.mobile.client.ui.ISupportDataText#getDataText()
	 */
	@Override
	public String getDataText()
	{
		return labelFor.getText();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.mobile.client.ui.IComponent#isEnabled()
	 */
	@Override
	public boolean isEnabled()
	{
		// TODO Auto-generated method stub
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.mobile.client.ui.IComponent#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(boolean enabled)
	{
		// TODO Auto-generated method stub

	}
}