package com.servoy.mobile.client;

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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import com.servoy.mobile.client.persistence.Form;
import com.servoy.mobile.client.scripting.FormScope;
import com.servoy.mobile.client.scripting.JSHistory;
import com.sksamuel.jqm4gwt.JQMContext;
import com.sksamuel.jqm4gwt.JQMPage;

/**
 * The main form manager, should be subclassed
 * @author jblok
 */
public class FormManager
{
	private final JSHistory history = new JSHistory(this);
	private final MobileClient application;
	private Login login;

	private final LinkedHashMap<String, FormController> formControllerMap = new LinkedHashMap<String, FormController>()
	{
		@Override
		protected boolean removeEldestEntry(Entry<String, FormController> eldest)
		{
			if (size() > 16)
			{
				FormController oldController = eldest.getValue();
				oldController.cleanup();
				return true;
			}
			return false;
		}
	};

	private FormController currentForm;

	protected FormManager(MobileClient mc)
	{
		this.application = mc;
		export();
	}

	protected FormController getFirstForm()
	{
		Form jsForm = application.getSolution().getForm(0);
		return getForm(jsForm.getName());
	}


	private JQMPage getLogin()
	{
		if (login == null)
		{
			login = new Login(application);
		}
		return login;
	}

	public void showForm(String formName)
	{
		FormController form = getForm(formName);
		if (form != null)
		{
			showForm(form);
		}

	}

	public FormController getForm(String name)
	{
		FormController formController = formControllerMap.get(name);
		if (formController == null)
		{
			Form form = application.getSolution().getForm(name);
			if (form != null)
			{
				formController = new FormController(application, form);
				formControllerMap.put(name, formController);
				initFormScope(name, formController.getFormScope());

			}
		}
		return formController;
	}

	public void showForm(FormController formController)
	{
		formControllerMap.put(formController.getName(), formController);
		currentForm = formController;
		history.add(formController);
		JQMContext.changePage(formController.getPage());
	}

	public FormController getCurrentForm()
	{
		return currentForm;
	}

	public JSHistory getHistory()
	{
		return history;
	}

	public void removeAllForms()
	{
		Iterator<FormController> it = formControllerMap.values().iterator();
		while (it.hasNext())
		{
			it.next().cleanup();
		}
		formControllerMap.clear();
		currentForm = null;
	}

	public boolean removeForm(String formName)
	{
		if (currentForm != null && currentForm.getName().equals(formName))
		{
			return false;
		}
		FormController formController = formControllerMap.get(formName);
		if (formController != null)
		{
			formController.cleanup();
			formControllerMap.remove(formName);
		}
		return true;
	}

	public void showFirstForm()
	{
		// if showing the first form (when startup or after a sync)
		// first just clear all existing forms to be fully refreshed.
		removeAllForms();
		showForm(getFirstForm());
	}

	public void showLogin()
	{
		JQMContext.changePage(getLogin());
	}

	public FormScope getFormScope(String name)
	{
		FormController form = getForm(name);
		if (form != null)
		{
			return form.getFormScope();
		}
		return null;
	}

	private native void initFormScope(String formName, FormScope formScope)
	/*-{
		$wnd._ServoyInit_.forms["_$" + formName + "$"](formScope);
	}-*/;


	public native void export() /*-{
		var formManager = this;
		$wnd._ServoyUtils_.getFormScope = function(name) {
			return formManager.@com.servoy.mobile.client.FormManager::getFormScope(Ljava/lang/String;)(name);
		}
	}-*/;
}
