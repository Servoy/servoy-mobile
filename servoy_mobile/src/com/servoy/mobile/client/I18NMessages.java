package com.servoy.mobile.client;

import com.google.gwt.i18n.client.LocalizableResource.DefaultLocale;
import com.google.gwt.i18n.client.LocalizableResource.Generate;
import com.google.gwt.i18n.client.Messages;

@Generate(format = "com.google.gwt.i18n.rebind.format.PropertiesFormat")
@DefaultLocale("en_US")
public interface I18NMessages extends Messages
{
	@DefaultMessage("Syncing")
	String syncing();

	@DefaultMessage("You are currently not connected to a network")
	String noNetwork();

	@DefaultMessage("Discard local changes?")
	String discardLocalChanges();

	@DefaultMessage("Couldn`t retrieve JSON")
	String cannotLoadJSON();

	@DefaultMessage("Couldn`t put JSON")
	String cannotSaveJSON();

	@DefaultMessage("Couldn`t delete record")
	String cannotDeleteRecord();

	@DefaultMessage("Cannot work without a primary key")
	String cannotWorkWithoutPK();

	@DefaultMessage("Authenticate")
	String loginTitle();

	@DefaultMessage("Authentication required, please login.")
	String authenticationRequired();

	@DefaultMessage("Authentication failed.")
	String authenticationFailed();

	@DefaultMessage("Request timed out.")
	String requestTimeout();

	@DefaultMessage("Requested url is not available.")
	String serviceNotAvailable();

	@DefaultMessage("Email")
	String userUid();

	@DefaultMessage("Password")
	String password();

	@DefaultMessage("Login")
	String login();
}
