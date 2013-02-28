package com.servoy.mobile.client;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.Exportable;
import org.timepedia.exporter.client.Getter;
import org.timepedia.exporter.client.Setter;

import com.servoy.base.scripting.api.IJSController;
import com.servoy.base.scripting.api.IJSEvent;
import com.servoy.base.scripting.api.IJSFoundSet;
import com.servoy.mobile.client.dataprocessing.FoundSet;
import com.servoy.mobile.client.dataprocessing.FoundSetManager;
import com.servoy.mobile.client.dataprocessing.IFoundSetSelectionListener;
import com.servoy.mobile.client.persistence.Form;
import com.servoy.mobile.client.scripting.FormScope;
import com.servoy.mobile.client.scripting.JSEvent;
import com.servoy.mobile.client.ui.ComponentFactory;
import com.servoy.mobile.client.ui.Executor;
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
	private FormScope scope;
	private final Form form;
	private final MobileClient mc;
	private final Executor executor;
	private boolean didOnShowOnce = false;

	public FormController(MobileClient mc, Form form)
	{
		this.mc = mc;
		this.form = form;
		this.executor = new Executor(this);
		String dataSource = form.getDataSource();
		if (dataSource != null)
		{
			foundSet = mc.getFoundSetManager().getFoundSet(FoundSetManager.getEntityFromDataSource(dataSource), true);
			if (foundSet != null) foundSet.addSelectionListener(this);
		}
		scope = new FormScope(mc, this);
		formDisplay = ComponentFactory.createFormDisplay(mc, this);
	}

	public FormScope recreateScope()
	{
		FormScope old = scope;
		this.scope = new FormScope(mc, this);
		return old;
	}

	/**
	 * @return the executor
	 */
	public Executor getExecutor()
	{
		return executor;
	}

	public Form getForm()
	{
		return form;
	}

	@Override
	@Export
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

	public MobileClient getApplication()
	{
		return mc;
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
	public void selectionChanged()
	{
		if (foundSet != null) formDisplay.refreshRecord(foundSet.getSelectedRecord());
		executeOnRecordSelect();
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
	 * @see com.servoy.j2db.scripting.api.IJSController#showRecords(com.servoy.j2db.scripting.api.IJSFoundSet)
	 */
	@Override
	public void showRecords(IJSFoundSet foundset) throws Exception
	{
		showRecords((FoundSet)foundset);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.IJSController#showRecords(com.servoy.j2db.scripting.api.IJSFoundSet)
	 */
	@Export
	public void showRecords(FoundSet foundset) throws Exception
	{
		setModel(foundset);
		mc.getFormManager().showForm(this);
		selectionChanged();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.api.IJSController#show()
	 */
	@Override
	@Export
	public void show() throws Exception
	{
		mc.getFormManager().showForm(this);
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
		return foundSet != null ? foundSet.jsFunction_getSelectedIndex() : -1;
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
		if (foundSet != null) foundSet.jsFunction_setSelectedIndex(index);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.base.scripting.api.IJSController#getDataSource()
	 */
	@Override
	@Export
	public String getDataSource()
	{
		return form.getDataSource();
	}

	/**
	 * @param relatedFoundset
	 */
	public void setModel(FoundSet foundset)
	{
		if (this.foundSet != null) this.foundSet.removeSelectionListener(this);
		this.foundSet = foundset;
		if (this.foundSet != null) this.foundSet.addSelectionListener(this);
	}

	public void executeOnShowMethod()
	{
		if (form.getOnShowCall() != null)
		{
			Object[] arguments = new Object[] { !didOnShowOnce };
			didOnShowOnce = true;
			Executor.callFunction(form.getOnShowCall(), arguments, getName(), new JSEvent(IJSEvent.FORM, getFormScope(), getName(), null));
		}
	}

	public boolean executeOnHideMethod()
	{
		if (form.getOnHideCall() != null)
		{
			Object hide = Executor.callFunction(form.getOnHideCall(), null, getName(), new JSEvent(IJSEvent.FORM, getFormScope(), getName(), null));
			if (hide instanceof Boolean && !((Boolean)hide).booleanValue())
			{
				return false;
			}
		}
		return true;
	}

	private boolean didOnload;

	public void executeOnLoadMethod()
	{
		if (!didOnload && form.getOnLoadCall() != null)
		{
			didOnload = true;
			Executor.callFunction(form.getOnLoadCall(), null, getName(), new JSEvent(IJSEvent.FORM, getFormScope(), getName(), null));
		}
	}

	private void executeOnRecordSelect()
	{
		if (form.getOnRecordSelectionCall() != null && mc.getFormManager().isVisible(getName()))
		{
			Executor.callFunction(form.getOnRecordSelectionCall(), null, getName(), new JSEvent(IJSEvent.FORM, getFormScope(), getName(), null));
		}
	}

	@Override
	public String toString()
	{
		return "FormController[" + getName() + "," + foundSet + "]";
	}
}