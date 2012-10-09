package com.servoy.mobile.client;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.Exportable;
import org.timepedia.exporter.client.Getter;
import org.timepedia.exporter.client.Setter;

import com.servoy.mobile.client.dataprocessing.FoundSet;
import com.servoy.mobile.client.dataprocessing.FoundSetManager;
import com.servoy.mobile.client.dataprocessing.IFoundSetSelectionListener;
import com.servoy.mobile.client.persistence.Form;
import com.servoy.mobile.client.scripting.FormScope;
import com.servoy.mobile.client.ui.ComponentFactory;
import com.servoy.mobile.client.ui.FormPage;
import com.servoy.mobile.client.ui.IFormDisplay;

/**
 * Representation of a form
 * 
 * @author gboros
 */
public class FormController implements Exportable, IFoundSetSelectionListener
{
	private final IFormDisplay formDisplay;
	private FoundSet foundSet;
	private final FormScope scope;
	private final Form form;

	public FormController(MobileClient mc, Form form)
	{
		this.form = form;
		formDisplay = ComponentFactory.createFormDisplay(mc, this);
		String dataSource = form.getDataSource();
		if (dataSource != null)
		{
			foundSet = mc.getFoundSetManager().getFoundSet(FoundSetManager.getEntityFromDataSource(dataSource));
			if (foundSet != null) foundSet.addSelectionListener(this);
		}
		scope = new FormScope(mc, this);
	}

	public Form getForm()
	{
		return form;
	}

	public String getName()
	{
		return form.getName();
	}

	@Export
	@Getter
	public boolean isEnabled()
	{
		return formDisplay.getDisplayPage().isEnabled();
	}

	@Export
	@Setter
	public void setEnabled(boolean enabled)
	{
		formDisplay.getDisplayPage().setEnabled(enabled);
	}

	public FormPage getPage()
	{
		return formDisplay.getDisplayPage();
	}

	public FoundSet getFormModel()
	{
		return foundSet;
	}

	public FormScope getFormScope()
	{
		return scope;
	}

	public void cleanup()
	{
		formDisplay.getDisplayPage().removeFromParent();
	}

	/*
	 * @see com.servoy.mobile.client.dataprocessing.IFoundSetSelectionListener#valueChanged()
	 */
	@Override
	public void valueChanged()
	{
		formDisplay.refreshRecord(foundSet.getSelectedRecord());
	}
}