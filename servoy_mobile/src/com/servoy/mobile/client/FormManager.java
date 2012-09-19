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
import com.servoy.mobile.client.ui.FormPage;
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

	private final LinkedHashMap<String, FormPage> pageMap = new LinkedHashMap<String, FormPage>()
	{
		@Override
		protected boolean removeEldestEntry(Entry<String, FormPage> eldest)
		{
			if (size() > 16)
			{
				FormPage oldPage = eldest.getValue();
				removePage(oldPage);
				return true;
			}
			return false;
		}
	};

	private FormPage currentPage = null;

	protected FormManager(MobileClient mc)
	{
		this.application = mc;
		export();
	}

	protected FormPage getFirstForm()
	{
		Form jsForm = application.getSolution().get(0);
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

	public void showForm(String formName, Object data)
	{

		FormPage form = getForm(formName);
		if (form != null)
		{
			showForm(form);
		}

	}

	protected FormPage getForm(String name)
	{
		FormPage formPage = pageMap.get(name);
		if (formPage == null)
		{
			Form form = application.getSolution().getForm(name);
			if (form != null)
			{
				formPage = new FormPage(application, form);
				pageMap.put(name, formPage);

			}
		}
		return formPage;
	}

	public void showForm(FormPage page)
	{
		pageMap.put(page.getName(), page);
		currentPage = page;
		JQMContext.changePage(page);
		history.add(page);
	}

	public FormPage getCurrentPage()
	{
		return currentPage;
	}

	private void removePage(JQMPage oldPage)
	{
		oldPage.removeFromParent();//keep dom small
	}

	public JSHistory getHistory()
	{
		return history;
	}

	public void removeAllForms()
	{
		Iterator<FormPage> it = pageMap.values().iterator();
		while (it.hasNext())
		{
			JQMPage oldPage = it.next();
			removePage(oldPage);
		}
		pageMap.clear();
		currentPage = null;
	}

	public void showFirstForm()
	{
		showForm(getFirstForm());
	}

	public void showLogin()
	{
		JQMContext.changePage(getLogin());
	}

	public FormScope getFormScope(String name)
	{
		FormPage form = getForm(name);
		if (form != null)
		{
			return form.getFormScope();
		}
		return null;
	}

	public native void export() /*-{
		var formManager = this;
		$wnd._ServoyUtils_.getFormScope = function(name) {
			return formManager.@com.servoy.mobile.client.FormManager::getFormScope(Ljava/lang/String;)(name);
		}
	}-*/;
}
