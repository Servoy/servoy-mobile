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

package com.servoy.mobile.client;

import com.sksamuel.jqm4gwt.form.JQMForm;
import com.sksamuel.jqm4gwt.form.JQMSubmit;
import com.sksamuel.jqm4gwt.form.SubmissionHandler;
import com.sksamuel.jqm4gwt.form.elements.JQMEmail;
import com.sksamuel.jqm4gwt.form.elements.JQMPassword;

/**
 * @author jblok
 */
public class LoginData extends JQMForm
{
	private final JQMEmail emailInput;
	private final JQMPassword passwordInput;
	private final MobileClient application;

	protected LoginData(MobileClient mc, SubmissionHandler<LoginData> handler)
	{
		super(handler);
		this.application = mc;

		emailInput = new JQMEmail(application.getMessages().userUid());
		addRequired(emailInput);

		passwordInput = new JQMPassword(application.getMessages().password());
		addRequired(passwordInput);

		JQMSubmit submit = new JQMSubmit(application.getMessages().login());
		add(submit);
	}

	public String getEmailInputText()
	{
		return emailInput.getValue();
	}

	public String getPasswordInputText()
	{
		return passwordInput.getValue();
	}
}
