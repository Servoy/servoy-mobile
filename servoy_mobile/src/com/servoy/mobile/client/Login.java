package com.servoy.mobile.client;

import com.sksamuel.jqm4gwt.JQMPage;
import com.sksamuel.jqm4gwt.form.SubmissionHandler;
import com.sksamuel.jqm4gwt.html.Paragraph;
import com.sksamuel.jqm4gwt.toolbar.JQMHeader;

public class Login extends JQMPage implements SubmissionHandler<LoginData>
{
	private final MobileClient application;
	private final LoginData form;

	public Login(MobileClient mc)
	{
		this.application = mc;
		setTheme("b");
		JQMHeader h = new JQMHeader(application.getI18nMessageWithFallback("loginTitle"));
		h.setTheme("b");
		add(h);

		add(new Paragraph(application.getI18nMessageWithFallback("authenticationRequired")));

		form = new LoginData(application, this);
		add(form);
	}

	public void init()
	{
		form.init();
	}

	public void onSubmit(LoginData f)
	{
		f.hideFormProcessingDialog();
		f.clearValidationErrors();
		application.doLogin(f.getEmailInputText(), f.getPasswordInputText());
	}
}