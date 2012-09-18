package com.servoy.mobile.client;

import com.sksamuel.jqm4gwt.JQMPage;
import com.sksamuel.jqm4gwt.form.SubmissionHandler;
import com.sksamuel.jqm4gwt.html.Paragraph;
import com.sksamuel.jqm4gwt.toolbar.JQMHeader;

public class Login extends JQMPage implements SubmissionHandler<LoginData>
{
	private final MobileClient application;

	public Login(MobileClient mc)
	{
		this.application = mc;

		JQMHeader header = new JQMHeader(application.getMessages().loginTitle());
		header.setTheme("b");
		add(header);

		add(new Paragraph(application.getMessages().authenticationRequired()));

		LoginData form = new LoginData(application, this);
		add(form);
	}

	public void onSubmit(LoginData form)
	{
		form.hideFormProcessingDialog();
		form.clearValidationErrors();
		application.setLoginCredentials(form.getEmailInputText(), form.getPasswordInputText());
		application.sync();
	}
}