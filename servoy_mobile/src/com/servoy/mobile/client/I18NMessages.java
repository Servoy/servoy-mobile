package com.servoy.mobile.client;

import com.google.gwt.i18n.client.ConstantsWithLookup;
import com.google.gwt.i18n.client.LocalizableResource.DefaultLocale;
import com.google.gwt.i18n.client.LocalizableResource.Generate;

@Generate(format = "com.google.gwt.i18n.rebind.format.PropertiesFormat")
@DefaultLocale("en_US")
public interface I18NMessages extends ConstantsWithLookup
{
	/*
	 * If you add a new key here, add it also in messages.properties with servoy.mobile.mynewkey so that it shows in developer i18n editor.
	 */
	@DefaultStringValue("Syncing")
	String syncing();

	@DefaultStringValue("You are currently not connected to a network")
	String noNetwork();

	@DefaultStringValue("Discard local changes?")
	String discardLocalChanges();

	@DefaultStringValue("Couldn`t retrieve JSON")
	String cannotLoadJSON();

	@DefaultStringValue("Couldn`t put JSON")
	String cannotSaveJSON();

	@DefaultStringValue("Couldn`t delete record")
	String cannotDeleteRecord();

	@DefaultStringValue("Cannot work without a primary key")
	String cannotWorkWithoutPK();

	@DefaultStringValue("Authenticate")
	String loginTitle();

	@DefaultStringValue("Authentication required, please login.")
	String authenticationRequired();

	@DefaultStringValue("Authentication failed.")
	String authenticationFailed();

	@DefaultStringValue("Request timed out.")
	String requestTimeout();

	@DefaultStringValue("Requested url is not available.")
	String serviceNotAvailable();

	@DefaultStringValue("Email")
	String userUid();

	@DefaultStringValue("Password")
	String password();

	@DefaultStringValue("Login")
	String login();
}
