package com.servoy.mobile.client;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

import com.servoy.mobile.client.persistence.Form;
import com.servoy.mobile.client.scripting.FormScope;
import com.servoy.mobile.client.scripting.JSHistory;
import com.servoy.mobile.client.scripting.ScriptEngine;

/**
 * The main form manager, should be subclassed
 * @author jblok
 */
public class FormManager
{
	private final JSHistory history = new JSHistory(this);
	private final MobileClient application;
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
		return showForm(formController, false, false);
	}

	private boolean isChangingFormPage;

	public void setChangingFormPage(boolean isChangingFormPage)
	{
		this.isChangingFormPage = isChangingFormPage;
	}

	public boolean showForm(FormController formController, boolean restoreScrollPosition)
	{
		return showForm(formController, restoreScrollPosition, false);
	}

	private boolean showForm(FormController formController, boolean restoreScrollPosition, boolean isLoginForm)
	{
		if (!isLoginForm)
		{
			showFormExecutedInCode = true;
		}
		if (currentForm == formController)
		{
//			if (currentForm != null) currentForm.getView().getDisplayPage().closeNavigator();
			return true;
		}
		if (isChangingFormPage) return false;
		formControllerMap.put(formController.getName(), formController);
		String currentNavigatorName = null;
		if (currentForm != null)
		{
			if (!currentForm.executeOnHideMethod()) return false;
			currentForm.setVisible(false);
//			currentForm.getView().getDisplayPage().saveScrollTop();
			currentNavigatorName = currentForm.getNavigator();
		}
		currentForm = formController;
		currentForm.setVisible(true);
		currentForm.executeOnShowMethod();
		currentForm.updateNavigator(currentNavigatorName);
		history.add(formController);
//		if (!restoreScrollPosition && currentForm != null) currentForm.getView().getDisplayPage().clearScrollTop();

		application.getAngularBridge().getWindowService().switchForm(currentForm);
//		JQMContext.changePage(formController.getView().getDisplayPage());
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
		for (FormController fc : formControllerMap.values())
		{
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
		showFormExecutedInCode = false;
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
			FormController form = getForm(application.getFlattenedSolution().getLoginForm());
			if (form != null && showForm(form, false, true))
			{
				return;
			}
		}
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
