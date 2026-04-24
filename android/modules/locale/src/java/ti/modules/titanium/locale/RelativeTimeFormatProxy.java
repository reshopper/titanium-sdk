/**
 * Titanium SDK
 * Copyright TiDev, Inc. 04/07/2022-Present. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */
package ti.modules.titanium.locale;

import android.icu.text.DisplayContext;
import android.icu.text.RelativeDateTimeFormatter;
import android.icu.text.RelativeDateTimeFormatter.RelativeDateTimeUnit;
import android.icu.text.RelativeDateTimeFormatter.Style;
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
 * Implements the JavaScript "Intl.RelativeTimeFormat" type.
 * Used to generate localized relative time strings such as "3 days ago" or "in 2 hours".
 */
@Kroll.proxy(creatableInModule = LocaleModule.class)
public class RelativeTimeFormatProxy extends KrollProxy
{
	private static final String TAG = "RelativeTimeFormatProxy";

	private RelativeDateTimeFormatter formatter;
	private KrollDict resolvedOptions = new KrollDict();
	private String numericOption = "always";

	@Override
	public void handleCreationDict(KrollDict properties)
	{
		super.handleCreationDict(properties);

		// Fetch the optional "locale" and "options" properties.
		Locale locale = null;
		Map options = null;
		if (properties != null) {
			Object value = properties.get(TiC.PROPERTY_LOCALE);
			if (value instanceof String) {
				locale = TiPlatformHelper.getInstance().getLocale((String) value);
			}
			value = properties.get(TiC.PROPERTY_OPTIONS);
			if (value instanceof Map) {
				options = (Map) value;
			}
		}
		if (locale == null) {
			locale = Locale.getDefault();
		}
		if (options == null) {
			options = new KrollDict();
		}

		// Read the "style" option.
		String styleId = TiConvert.toString(options.get("style"), "long");
		Style style;
		switch (styleId) {
			case "short":
				style = Style.SHORT;
				break;
			case "narrow":
				style = Style.NARROW;
				break;
			case "long":
			default:
				styleId = "long";
				style = Style.LONG;
				break;
		}

		// Read the "numeric" option.
		this.numericOption = TiConvert.toString(options.get("numeric"), "always");
		if (!"auto".equals(this.numericOption)) {
			this.numericOption = "always";
		}

		// Create the formatter.
		this.formatter = RelativeDateTimeFormatter.getInstance(
			ULocale.forLocale(locale), null, style, DisplayContext.CAPITALIZATION_NONE);

		// Store resolved options.
		this.resolvedOptions = new KrollDict();
		this.resolvedOptions.put(TiC.PROPERTY_LOCALE, locale.toString().replace("_", "-"));
		this.resolvedOptions.put("style", styleId);
		this.resolvedOptions.put("numeric", this.numericOption);
		this.resolvedOptions.put("numberingSystem", "latn");
	}

	@Kroll.method
	public String format(double value, String unit)
	{
		RelativeDateTimeUnit rdtUnit = toRelativeDateTimeUnit(unit);
		if (rdtUnit == null) {
			throw new IllegalArgumentException("Invalid unit: " + unit);
		}

		if ("always".equals(this.numericOption)) {
			return this.formatter.formatNumeric(value, rdtUnit);
		} else {
			return this.formatter.format(value, rdtUnit);
		}
	}

	@Kroll.method
	public KrollDict resolvedOptions()
	{
		return this.resolvedOptions;
	}

	private RelativeDateTimeUnit toRelativeDateTimeUnit(String unit)
	{
		if (unit == null) {
			return null;
		}
		switch (unit) {
			case "year":
			case "years":
				return RelativeDateTimeUnit.YEAR;
			case "quarter":
			case "quarters":
				return RelativeDateTimeUnit.QUARTER;
			case "month":
			case "months":
				return RelativeDateTimeUnit.MONTH;
			case "week":
			case "weeks":
				return RelativeDateTimeUnit.WEEK;
			case "day":
			case "days":
				return RelativeDateTimeUnit.DAY;
			case "hour":
			case "hours":
				return RelativeDateTimeUnit.HOUR;
			case "minute":
			case "minutes":
				return RelativeDateTimeUnit.MINUTE;
			case "second":
			case "seconds":
				return RelativeDateTimeUnit.SECOND;
			default:
				return null;
		}
	}

	@Override
	public String getApiName()
	{
		return "Ti.Locale.RelativeTimeFormat";
	}
}
