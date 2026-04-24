/**
 * Titanium SDK
 * Copyright TiDev, Inc. 04/07/2022-Present. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */
package ti.modules.titanium.locale;

import android.icu.text.LocaleDisplayNames;
import android.icu.text.LocaleDisplayNames.DialectHandling;
import android.icu.util.Currency;
import android.icu.util.ULocale;
import java.util.Locale;
import java.util.Map;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.util.TiPlatformHelper;

/**
 * Implements the JavaScript "Intl.DisplayNames" type.
 * Used to generate localized display names for languages, regions, scripts, and currencies.
 */
@Kroll.proxy(creatableInModule = LocaleModule.class)
public class DisplayNamesProxy extends KrollProxy
{
	private static final String TAG = "DisplayNamesProxy";

	private LocaleDisplayNames displayNames;
	private Locale locale;
	private String type;
	private String style;
	private String fallback;
	private String languageDisplay;
	private KrollDict resolvedOptions = new KrollDict();

	@Override
	public void handleCreationDict(KrollDict properties)
	{
		super.handleCreationDict(properties);

		// Fetch the optional "locale" and "options" properties.
		this.locale = null;
		Map options = null;
		if (properties != null) {
			Object value = properties.get(TiC.PROPERTY_LOCALE);
			if (value instanceof String) {
				this.locale = TiPlatformHelper.getInstance().getLocale((String) value);
			}
			value = properties.get(TiC.PROPERTY_OPTIONS);
			if (value instanceof Map) {
				options = (Map) value;
			}
		}
		if (this.locale == null) {
			this.locale = Locale.getDefault();
		}
		if (options == null) {
			options = new KrollDict();
		}

		// Read the required "type" option.
		this.type = TiConvert.toString(options.get("type"), null);
		if (this.type == null) {
			throw new IllegalArgumentException("Required option 'type' not provided.");
		}
		if ("calendar".equals(this.type) || "dateTimeField".equals(this.type)) {
			throw new IllegalArgumentException("Unsupported type: " + this.type);
		}
		if (!"language".equals(this.type) && !"region".equals(this.type)
			&& !"script".equals(this.type) && !"currency".equals(this.type)) {
			throw new IllegalArgumentException("Invalid type: " + this.type);
		}

		// Read the "style" option.
		this.style = TiConvert.toString(options.get("style"), "long");
		if (!"long".equals(this.style) && !"short".equals(this.style) && !"narrow".equals(this.style)) {
			this.style = "long";
		}

		// Read the "fallback" option.
		this.fallback = TiConvert.toString(options.get("fallback"), "code");
		if (!"code".equals(this.fallback) && !"none".equals(this.fallback)) {
			this.fallback = "code";
		}

		// Read the "languageDisplay" option.
		this.languageDisplay = TiConvert.toString(options.get("languageDisplay"), "dialect");

		// Create the LocaleDisplayNames instance.
		DialectHandling dialectHandling = "standard".equals(this.languageDisplay)
			? DialectHandling.STANDARD_NAMES
			: DialectHandling.DIALECT_NAMES;
		this.displayNames = LocaleDisplayNames.getInstance(
			ULocale.forLocale(this.locale), dialectHandling);

		// Store resolved options.
		this.resolvedOptions = new KrollDict();
		this.resolvedOptions.put(TiC.PROPERTY_LOCALE, this.locale.toString().replace("_", "-"));
		this.resolvedOptions.put("type", this.type);
		this.resolvedOptions.put("style", this.style);
		this.resolvedOptions.put("fallback", this.fallback);
		if ("language".equals(this.type)) {
			this.resolvedOptions.put("languageDisplay", this.languageDisplay);
		}
	}

	@Kroll.method(name = "of")
	public String getDisplayName(String code)
	{
		if (code == null || code.isEmpty()) {
			throw new IllegalArgumentException("Invalid code: " + code);
		}

		String result = null;
		switch (this.type) {
			case "language":
				result = this.displayNames.languageDisplayName(code);
				break;
			case "region":
				result = this.displayNames.regionDisplayName(code);
				break;
			case "script":
				result = this.displayNames.scriptDisplayName(code);
				break;
			case "currency":
				try {
					Currency currency = Currency.getInstance(code);
					result = currency.getDisplayName(this.locale);
				} catch (Exception e) {
					result = null;
				}
				break;
		}

		// Handle fallback behavior.
		if (result == null || result.isEmpty() || result.equals(code)) {
			if ("none".equals(this.fallback)) {
				return null;
			}
			return code;
		}
		return result;
	}

	@Kroll.method
	public KrollDict resolvedOptions()
	{
		return this.resolvedOptions;
	}

	@Override
	public String getApiName()
	{
		return "Ti.Locale.DisplayNames";
	}
}
