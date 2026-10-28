/*
 This file belongs to the Servoy development and deployment environment, Copyright (C) 1997-2026 Servoy BV

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

package com.servoy.mobile.client.properties;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.junit.client.GWTTestCase;
import com.servoy.mobile.client.MobileClient;
import com.servoy.mobile.client.SolutionI18nProvider;
import com.servoy.mobile.client.persistence.FlattenedSolution;
import com.servoy.mobile.client.persistence.Solution;
import com.servoy.mobile.client.ui.PropertySpec;
import com.servoy.mobile.client.ui.WebRuntimeComponent;

/**
 * Unit tests for {@link TagStringConvertor}, run as a {@link GWTTestCase} (HtmlUnit) through the {@code gwttest} maven profile.
 */
public class TagStringConvertorTest extends GWTTestCase
{
	/**
	 * A {@link MobileClient} that is never initialized (no entry point, no exported solution); only the i18n provider is
	 * under the control of the test, and, as in the real client, it can be set after the converter was created.
	 */
	private static class TestClient extends MobileClient
	{
		private SolutionI18nProvider provider;

		@Override
		public SolutionI18nProvider getI18nProvider()
		{
			return provider;
		}

		void setProvider(SolutionI18nProvider provider)
		{
			this.provider = provider;
		}
	}

	@Override
	public String getModuleName()
	{
		return "com.servoy.mobile.MobileClientTest";
	}

	private static SolutionI18nProvider createProvider(String locale, String... keysAndValues)
	{
		Solution solution = JavaScriptObject.createObject().cast();
		for (int i = 0; i < keysAndValues.length; i += 2)
		{
			solution.setI18nValue(keysAndValues[i], keysAndValues[i + 1]);
		}
		return new SolutionI18nProvider(new FlattenedSolution(solution), locale);
	}

	private static TestClient createClient()
	{
		TestClient client = new TestClient();
		client.setProvider(createProvider("en", "en.test.button.text", "Hello button", "en.test.other", "Other text"));
		return client;
	}

	private static Object forClient(TagStringConvertor convertor, Object value)
	{
		return convertor.convertForClient(value, null, null);
	}

	public void testLeadingPrefixIsTranslated()
	{
		TagStringConvertor convertor = new TagStringConvertor(createClient());
		assertEquals("i18n:test.button.text must be replaced by its message", "Hello button", forClient(convertor, "i18n:test.button.text"));
		assertEquals("every key is looked up, not only the first one", "Other text", forClient(convertor, "i18n:test.other"));
	}

	public void testTranslationFollowsProviderLocale()
	{
		TestClient client = new TestClient();
		client.setProvider(createProvider("nl", "nl.test.other", "Andere tekst", "en.test.other", "Other text"));
		assertEquals("message of the provider locale must be used", "Andere tekst", forClient(new TagStringConvertor(client), "i18n:test.other"));
	}

	public void testUnknownKeyGivesBangKeyBang()
	{
		TagStringConvertor convertor = new TagStringConvertor(createClient());
		assertEquals("an unknown key shows !key! like in NGClient", "!does.not.exist!", forClient(convertor, "i18n:does.not.exist"));
	}

	public void testPlainTextIsUnchanged()
	{
		TagStringConvertor convertor = new TagStringConvertor(createClient());
		assertEquals("text without prefix must stay as is", "Plain text", forClient(convertor, "Plain text"));
		assertEquals("empty string must stay empty", "", forClient(convertor, ""));
	}

	public void testNullIsUnchanged()
	{
		TagStringConvertor convertor = new TagStringConvertor(createClient());
		assertNull("null must stay null (and not throw)", forClient(convertor, null));
	}

	public void testNonStringValuesAreUnchanged()
	{
		TagStringConvertor convertor = new TagStringConvertor(createClient());
		Integer number = Integer.valueOf(42);
		assertSame("an Integer must be passed through", number, forClient(convertor, number));
		assertSame("a Boolean must be passed through", Boolean.TRUE, forClient(convertor, Boolean.TRUE));
		JavaScriptObject jsObject = JavaScriptObject.createObject();
		assertSame("a JS object must be passed through", jsObject, forClient(convertor, jsObject));
	}

	public void testPrefixNotAtStartIsUnchanged()
	{
		TagStringConvertor convertor = new TagStringConvertor(createClient());
		assertEquals("prefix inside the text must not be translated", "abc i18n:test.other", forClient(convertor, "abc i18n:test.other"));
		assertEquals("embedded %%i18n:key%% is out of scope and must not be translated", "%%i18n:test.other%%",
			forClient(convertor, "%%i18n:test.other%%"));
		assertEquals("embedded key inside text must not be translated", "Hello %%i18n:test.other%%", forClient(convertor, "Hello %%i18n:test.other%%"));
		assertEquals("leading whitespace means the prefix is not at the start", " i18n:test.other", forClient(convertor, " i18n:test.other"));
	}

	public void testPrefixIsCaseSensitive()
	{
		TagStringConvertor convertor = new TagStringConvertor(createClient());
		assertEquals("only the lowercase i18n: prefix is translated", "I18N:test.other", forClient(convertor, "I18N:test.other"));
	}

	public void testConvertFromClientReturnsSameValue()
	{
		TagStringConvertor convertor = new TagStringConvertor(createClient());
		assertEquals("text typed by the user must be kept", "typed by user", convertor.convertFromClient("text", "typed by user", null, null));
		assertEquals("an i18n: value coming from the client must not be translated or replaced", "i18n:test.other",
			convertor.convertFromClient("text", "i18n:test.other", null, null));
		assertEquals("empty text from the client must be kept", "", convertor.convertFromClient("text", "", null, null));
		Integer number = Integer.valueOf(7);
		assertSame("a non String value must be returned as is", number, convertor.convertFromClient("text", number, null, null));
	}

	public void testConvertFromClientNullStaysNull()
	{
		TagStringConvertor convertor = new TagStringConvertor(createClient());
		assertNull("null from the client is returned as null", convertor.convertFromClient("text", null, null, null));
	}

	public void testNullComponentAndNullPropertyTypeDoNotThrow()
	{
		TagStringConvertor convertor = new TagStringConvertor(createClient());
		WebRuntimeComponent component = null;
		PropertySpec propertyType = null;
		// the service argument path (MobileClient.convertValueForType) passes a null component
		assertEquals("translation must work without a component and property type", "Other text",
			convertor.convertForClient("i18n:test.other", component, propertyType));
		assertEquals("convertFromClient must work without a component and property type", "x", convertor.convertFromClient("text", "x", component, propertyType));
	}

	public void testProviderIsReadAtConversionTime()
	{
		// the real client registers its converters before the i18n provider exists
		TestClient client = new TestClient();
		TagStringConvertor convertor = new TagStringConvertor(client);

		assertEquals("plain text must not need the provider", "Plain text", forClient(convertor, "Plain text"));
		assertNull("null must not need the provider", forClient(convertor, null));

		client.setProvider(createProvider("en", "en.test.other", "Other text"));
		assertEquals("provider set after the converter was created must be used", "Other text", forClient(convertor, "i18n:test.other"));

		client.setProvider(createProvider("en", "en.test.other", "Replaced text"));
		assertEquals("a replaced provider must be used by the same converter", "Replaced text", forClient(convertor, "i18n:test.other"));
	}
}
