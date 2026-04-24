/**
 * Titanium SDK
 * Copyright TiDev, Inc. 04/07/2022-Present
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */
package org.appcelerator.titanium.proxy;

import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;

import androidx.annotation.ColorInt;

@Kroll.proxy
/**
 * This is a proxy representation of the Android Color type.
 * Refer to <a href="https://developer.android.com/reference/android/graphics/Color">Android Color</a> for more details.
 */
public class ColorProxy extends KrollProxy
{
	private final @ColorInt int color;

	public ColorProxy(@ColorInt int colorInt)
	{
		this.color = colorInt;
	}

	@Kroll.method
	public String toHex()
	{
		// Match iOS behavior: omit alpha when fully opaque; otherwise return AARRGGBB
		int alpha = (this.color >>> 24) & 0xFF;
		int red = (this.color >>> 16) & 0xFF;
		int green = (this.color >>> 8) & 0xFF;
		int blue = this.color & 0xFF;
		if (alpha == 0xFF) {
			return String.format("#%02X%02X%02X", red, green, blue);
		}
		return String.format("#%02X%02X%02X%02X", alpha, red, green, blue);
	}

	@Kroll.method
	@Override
	public String toString()
	{
		return toHex();
	}

	@Override
	public String getApiName()
	{
		return "Ti.UI.Color";
	}
}
