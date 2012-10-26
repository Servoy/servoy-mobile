package com.servoy.mobile.client;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.Exportable;
import org.timepedia.exporter.client.Getter;
import org.timepedia.exporter.client.Setter;

import com.servoy.j2db.scripting.api.IJSController;
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
public class FormController implements Exportable, IFoundSetSelectionListener, IJSController
{
	private final IFormDisplay formDisplay;
	private FoundSet foundSet;
	private final FormScope scope;
	private final Form form;

	public FormController(MobileClient mc, Form form)
	{
		this.form = form;
		String dataSource = form.getDataSource();
		if (dataSource != null)
		{
			foundSet = mc.getFoundSetManager().getFoundSet(FoundSetManager.getEntityFromDataSource(dataSource));
			if (foundSet != null) foundSet.addSelectionListener(this);
		}
		scope = new FormScope(mc, this);
		formDisplay = ComponentFactory.createFormDisplay(mc, this);
	}

	public Form getForm()
	{
		return form;
	}

	public String getName()
	{
		return form.getName();
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
		formDisplay.getDisplayPage().getDataAdapterList().destroy();

	}

	/*
	 * @see com.servoy.mobile.client.dataprocessing.IFoundSetSelectionListener#valueChanged()
	 */
	@Override
	public void valueChanged()
	{
		formDisplay.refreshRecord(foundSet.getSelectedRecord());
	}

	@Getter
	public boolean getEnabled()
	{
		return formDisplay.getDisplayPage().isEnabled();
	}

	@Setter
	public void setEnabled(boolean enabled)
	{
		formDisplay.getDisplayPage().setEnabled(enabled);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.IJSController#getSelectedIndex()
	 */
	@Override
	@Export
	public int getSelectedIndex()
	{
		// call +1 method of foundset
		return 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.IJSController#setSelectedIndex(int)
	 */
	@Override
	@Export
	public void setSelectedIndex(int index)
	{
		// call +1 method of foundset
		foundSet.jsFunction_setSelectedIndex(index);
	}
}