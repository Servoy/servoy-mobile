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
import com.servoy.mobile.client.scripting.ScriptEngine;
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
	boolean showFormExecutedInCode = false; // this will cause showFirstForm to ignore the call if a showForm was called before in onSolutionOpen for ex

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

	protected MobileClient getApplication()
	{
		return application;
	}

	protected FormController getFirstForm()
	{
		Form jsForm = application.getFlattenedSolution().getFirstForm();
		return getForm(jsForm.getName());
	}


	private JQMPage getLogin()
	{
		if (login == null)
		{
			login = new Login(application);
		}
		login.init();
		return login;
	}

	public boolean showForm(String formName)
	{
		FormController form = getForm(formName);
		if (form != null)
		{
			return showForm(form);
		}
		return false;
	}

	public FormController getForm(String name)
	{
		FormController formController = formControllerMap.get(name);
		if (formController == null)
		{
			Form form = application.getFlattenedSolution().getForm(name);
			if (form != null)
			{
				formController = new FormController(application, form);
				formControllerMap.put(name, formController);
				initFormScope(name, formController.getFormScope(), null);
				formController.createView();
				formController.executeOnLoadMethod();
			}
		}
		return formController;
	}

	public boolean showForm(FormController formController)
	{
		return showForm(formController, false);
	}

	private boolean isChangingFormPage;

	public void setChangingFormPage(boolean isChangingFormPage)
	{
		this.isChangingFormPage = isChangingFormPage;
	}

	public boolean showForm(FormController formController, boolean restoreScrollPosition)
	{
		if (!formController.getName().equals(application.getFlattenedSolution().getLoginForm()))
		{
			showFormExecutedInCode = true;
		}
		if (currentForm == formController) return true;
		if (isChangingFormPage) return false;
		formControllerMap.put(formController.getName(), formController);
		String currentNavigatorName = null;
		if (currentForm != null)
		{
			if (!currentForm.executeOnHideMethod()) return false;
			currentForm.getView().getDisplayPage().saveScrollTop();
			currentNavigatorName = currentForm.getNavigator();
		}
		currentForm = formController;
		currentForm.updateNavigator(currentNavigatorName);
		history.add(formController);
		if (!restoreScrollPosition && currentForm != null) currentForm.getView().getDisplayPage().clearScrollTop();
		JQMContext.changePage(formController.getView().getDisplayPage());
		return true;
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
			FormController fc = it.next();
			if (!fc.getView().isShow())
			{
				fc.cleanup();
			}
			else
			{
				fc.markForCleanup();
			}

		}
		formControllerMap.clear();
		currentForm = null;
	}

	public boolean removeForm(String formName)
	{
		if (isVisible(formName))
		{
			return false;
		}
		FormController formController = formControllerMap.get(formName);
		if (formController != null)
		{
			if (!formController.getView().isShow())
			{
				formController.cleanup();
			}
			else
			{
				formController.markForCleanup();
			}
			formControllerMap.remove(formName);
		}
		return true;
	}

	public boolean isVisible(String formName)
	{
		if (currentForm != null && currentForm.getName().equals(formName))
		{
			return true;
		}
		return false;
	}

	public void showFirstForm()
	{
		// if showing the first form (when startup or after a sync)
		// first just clear all existing forms to be fully refreshed.


		if (!isChangingFormPage && !showFormExecutedInCode)
		{
			showForm(getFirstForm());
		}
	}

	public void showLogin()
	{
		currentForm = null;
		if (application.getFlattenedSolution().getLoginForm() != null)
		{
			if (showForm(application.getFlattenedSolution().getLoginForm()))
			{
				return;
			}
		}
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

	private void hashChanged(String hash)
	{
		getHistory().hashChanged(hash);

	}

	private native void defineStandardFormVariables(FormScope formScope)
	/*-{
		$wnd._ServoyUtils_.defineStandardFormVariables(formScope);
	}-*/;


	public native void export()
	/*-{
		var formManager = this;
		$wnd._ServoyUtils_.getFormScope = function(name) {
			return formManager.@com.servoy.mobile.client.FormManager::getFormScope(Ljava/lang/String;)(name);
		}
		$wnd._ServoyUtils_.reloadFormScope = function(name) {
			formManager.@com.servoy.mobile.client.FormManager::reloadScopeIfInitialized(Ljava/lang/String;)(name);
		}
		$wnd
				.$($wnd)
				.bind(
						'hashchange',
						function() {
							;
							formManager.@com.servoy.mobile.client.FormManager::hashChanged(Ljava/lang/String;)($wnd.location.hash);
						});
	}-*/;

	public void reloadScopeIfInitialized(String formName)
	{
		FormController fc;
		if ((fc = formControllerMap.remove(formName)) != null)
		{
			FormScope oldScope = fc.recreateScope();
			FormScope newScope = fc.getFormScope();
			initFormScope(formName, newScope, oldScope);
		}
	}

	private void initFormScope(String formName, FormScope newScope, FormScope oldScope)
	{
		ScriptEngine.initScope(ScriptEngine.FORMS, formName, newScope, oldScope);
		defineStandardFormVariables(newScope);
	}

}
