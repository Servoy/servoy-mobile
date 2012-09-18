package com.servoy.mobile.client;

import com.google.gwt.i18n.client.Messages;
import com.google.gwt.i18n.client.LocalizableResource.DefaultLocale;
import com.google.gwt.i18n.client.LocalizableResource.Generate;

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

	@DefaultMessage("Cannot work without a primary key")
	String cannotWorkWithoutPK();

	@DefaultMessage("Login")
	String loginTitle();

	@DefaultMessage("Authentication required, please login.")
	String authenticationRequired();

	@DefaultMessage("Email")
	String userUid();

	@DefaultMessage("Password")
	String password();

	@DefaultMessage("Login")
	String login();
}
