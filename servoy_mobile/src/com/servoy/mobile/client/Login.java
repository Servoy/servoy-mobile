package com.servoy.mobile.client;

import com.google.gwt.core.client.JavaScriptObject;
import com.sksamuel.jqm4gwt.JQMPage;
import com.sksamuel.jqm4gwt.form.SubmissionHandler;
import com.sksamuel.jqm4gwt.html.Paragraph;
import com.sksamuel.jqm4gwt.toolbar.JQMHeader;

public class Login extends JQMPage implements SubmissionHandler<LoginData>
{
	private final MobileClient application;
	private final JavaScriptObject successCallback;
	private final JavaScriptObject errorHandler;

	public Login(MobileClient mc, JavaScriptObject successCallback, JavaScriptObject errorHandler)
	{
		this.application = mc;
		this.successCallback = successCallback;
		this.errorHandler = errorHandler;

		JQMHeader h = new JQMHeader(application.getI18nMessageWithFallback("loginTitle"));
		h.setTheme("b");
		add(h);

		add(new Paragraph(application.getI18nMessageWithFallback("authenticationRequired")));

		LoginData form = new LoginData(application, this);
		add(form);
	}

	public void onSubmit(LoginData form)
	{
		form.hideFormProcessingDialog();
		form.clearValidationErrors();
		application.setLoginCredentials(form.getEmailInputText(), form.getPasswordInputText());
		application.sync(successCallback, errorHandler);
	}
}