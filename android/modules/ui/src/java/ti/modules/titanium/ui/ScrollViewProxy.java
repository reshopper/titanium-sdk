/**
 * Titanium SDK
 * Copyright TiDev, Inc. 04/07/2022-Present. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */
package ti.modules.titanium.ui;

import android.app.Activity;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.view.TiUIView;

import java.util.HashMap;

import ti.modules.titanium.ui.widget.TiUIScrollView;

@Kroll.proxy(creatableInModule = UIModule.class,
	propertyAccessors = {
		TiC.PROPERTY_CONTENT_HEIGHT,
		TiC.PROPERTY_CONTENT_WIDTH,
		TiC.PROPERTY_SHOW_HORIZONTAL_SCROLL_INDICATOR,
		TiC.PROPERTY_SHOW_VERTICAL_SCROLL_INDICATOR,
		TiC.PROPERTY_SCROLL_TYPE,
		TiC.PROPERTY_CAN_CANCEL_EVENTS,
		TiC.PROPERTY_OVER_SCROLL_MODE,
		TiC.PROPERTY_REFRESH_CONTROL
	})
public class ScrollViewProxy extends TiViewProxy
{
	private static final int MSG_FIRST_ID = TiViewProxy.MSG_LAST_ID + 1;
	protected static final int MSG_LAST_ID = MSG_FIRST_ID + 999;

	public ScrollViewProxy()
	{
		super();
		defaultValues.put(TiC.PROPERTY_OVER_SCROLL_MODE, 0);
		KrollDict offset = new KrollDict();
		offset.put(TiC.EVENT_PROPERTY_X, 0);
		offset.put(TiC.EVENT_PROPERTY_Y, 0);
		defaultValues.put(TiC.PROPERTY_CONTENT_OFFSET, offset);
	}

	@Override
	public TiUIView createView(Activity activity)
	{
		return new TiUIScrollView(this);
	}

	public TiUIScrollView getScrollView()
	{
		return (TiUIScrollView) getOrCreateView();
	}

	@Kroll.getProperty
	public KrollDict getContentOffset()
	{
		Object value = getProperty(TiC.PROPERTY_CONTENT_OFFSET);
		if (value instanceof KrollDict) {
			return (KrollDict) value;
		}
		KrollDict offset = new KrollDict();
		offset.put(TiC.EVENT_PROPERTY_X, 0);
		offset.put(TiC.EVENT_PROPERTY_Y, 0);
		return offset;
	}

	@Kroll.setProperty
	public void setContentOffset(Object value)
	{
		getScrollView().setContentOffset(value);
		setProperty(TiC.PROPERTY_CONTENT_OFFSET, value);
	}

	@Kroll.method
	public void scrollTo(int x, int y, @Kroll.argument(optional = true) HashMap args)
	{
		boolean animated = false;
		int duration = -1;
		Integer curve = null;
		if (args != null) {
			animated = TiConvert.toBoolean(args.get("animated"), false);
			if (args.containsKey("duration")) {
				duration = TiConvert.toInt(args.get("duration"), -1);
			}
			if (args.containsKey(TiC.PROPERTY_CURVE)) {
				curve = TiConvert.toInt(args, TiC.PROPERTY_CURVE);
			}
		}
		handleScrollTo(x, y, animated, duration, curve);
	}

	@Kroll.getProperty
	public boolean getScrollingEnabled()
	{
		return getScrollView().getScrollingEnabled();
	}

	@Kroll.setProperty
	public void setScrollingEnabled(Object enabled)
	{
		getScrollView().setScrollingEnabled(enabled);
	}

	@Kroll.method
	public void scrollToBottom(@Kroll.argument(optional = true) HashMap args)
	{
		boolean animated = false;
		if (args != null) {
			animated = TiConvert.toBoolean(args.get("animated"), false);
		}
		handleScrollToBottom(animated);
	}

	@Kroll.method
	public void scrollToTop(@Kroll.argument(optional = true) HashMap args)
	{
		boolean animated = false;
		if (args != null) {
			animated = TiConvert.toBoolean(args.get("animated"), false);
		}
		handleScrollToTop(animated);
	}

	@Kroll.method
	public void setContentOffsetWithAnimation(Object offset, @Kroll.argument(optional = true) HashMap args)
	{
		boolean animated = false;
		if (args != null) {
			animated = TiConvert.toBoolean(args.get("animated"), false);
		}
		handleSetContentOffsetAnimated(offset, animated);
	}

	@Kroll.method
	public void setContentOffset(Object[] args)
	{
		if (args == null || args.length == 0) {
			return;
		}
		
		Object offset = args[0];
		boolean animated = false;
		
		if (args.length > 1 && args[1] instanceof HashMap) {
			HashMap options = (HashMap) args[1];
			animated = TiConvert.toBoolean(options.get("animated"), false);
		}
		
		handleSetContentOffsetAnimated(offset, animated);
	}

	@Kroll.setProperty
	public void setContentInsets(Object insets)
	{
		if (insets == null) {
			return;
		}
		
		// Store as property for consistency
		setProperty(TiC.PROPERTY_CONTENT_INSETS, insets);
		
		// Call the existing implementation in TiUIScrollView
		getScrollView().setContentInsets(insets);
	}

	@Kroll.method
	public void setContentInsets(Object[] args)
	{
		if (args == null || args.length == 0) {
			return;
		}
		
		Object insets = args[0];
		// Optional animated parameter for future compatibility (currently not used on Android)
		// HashMap options = args.length > 1 && args[1] instanceof HashMap ? (HashMap) args[1] : null;
		
		// Store as property for consistency
		setProperty(TiC.PROPERTY_CONTENT_INSETS, insets);
		
		getScrollView().setContentInsets(insets);
	}

	@Kroll.getProperty
	public Object getContentInsets()
	{
		return getScrollView().getContentInsets();
	}

	public void handleScrollTo(int x, int y, boolean smoothScroll)
	{
		// Backwards compatibility: no duration specified
		getScrollView().scrollTo(x, y, smoothScroll);
	}

	public void handleScrollTo(int x, int y, boolean smoothScroll, int duration)
	{
		getScrollView().scrollTo(x, y, smoothScroll, duration);
	}

	public void handleScrollTo(int x, int y, boolean smoothScroll, int duration, Integer curve)
	{
		getScrollView().scrollTo(x, y, smoothScroll, duration, curve);
	}

	public void handleScrollToBottom(boolean animated)
	{
		getScrollView().scrollToBottom(animated);
	}

	public void handleScrollToTop(boolean animated)
	{
		getScrollView().scrollToTop(animated);
	}

	public void handleSetContentOffsetAnimated(Object offset, boolean animated)
	{
		TiUIScrollView scrollView = getScrollView();
		scrollView.setContentOffset(offset);
		
		if (offset instanceof HashMap) {
			KrollDict offsetDict = new KrollDict((HashMap) offset);
			int x = 0, y = 0;
			
			if (offsetDict.containsKeyAndNotNull(TiC.PROPERTY_X)) {
				x = TiConvert.toInt(offsetDict.get(TiC.PROPERTY_X), 0);
			}
			if (offsetDict.containsKeyAndNotNull(TiC.PROPERTY_Y)) {
				y = TiConvert.toInt(offsetDict.get(TiC.PROPERTY_Y), 0);
			}
			
			scrollView.scrollTo(x, y, animated);
		}
	}

	@Override
	public String getApiName()
	{
		return "Ti.UI.ScrollView";
	}
}
