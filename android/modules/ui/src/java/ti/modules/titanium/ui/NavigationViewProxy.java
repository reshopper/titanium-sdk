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
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.proxy.TiWindowProxy;
import org.appcelerator.titanium.view.TiUIView;

import ti.modules.titanium.ui.widget.TiUINavigationView;

/**
 * Embeddable navigation container that manages a stack of WindowProxy
 * instances rendered as Fragments, rather than separate Activities.
 *
 * Unlike NavigationWindowProxy (which is Activity-based), this proxy
 * can be added as a child view to any container such as BottomSheetDialog.
 *
 * JavaScript usage:
 *   var navView = Ti.UI.createNavigationView({ window: rootWin });
 *   bottomSheet.add(navView);
 *   navView.openWindow(detailWin);
 *   navView.closeWindow(detailWin);
 *   navView.popToRootWindow();
 */
@Kroll.proxy(creatableInModule = UIModule.class,
	propertyAccessors = {
		TiC.PROPERTY_WINDOW
	})
public class NavigationViewProxy extends TiViewProxy
{
	private static final String TAG = "NavigationViewProxy";

	private TiUINavigationView navigationView;
	private boolean rootShown = false;

	public NavigationViewProxy()
	{
		super();
	}

	@Override
	public TiUIView createView(Activity activity)
	{
		navigationView = new TiUINavigationView(this);
		showRootIfReady();
		return navigationView;
	}

	@Override
	public void handleCreationDict(KrollDict options)
	{
		super.handleCreationDict(options);
	}

	/**
	 * Show the root window once the view has been created and
	 * the 'window' property is available.
	 */
	private void showRootIfReady()
	{
		if (rootShown || navigationView == null) {
			return;
		}

		Object windowObj = getProperty(TiC.PROPERTY_WINDOW);
		if (windowObj instanceof TiWindowProxy) {
			rootShown = true;
			navigationView.getNativeView().post(() -> {
				navigationView.showRootWindow((TiWindowProxy) windowObj);
			});
		}
	}

	@Kroll.method
	public void openWindow(Object windowToOpen,
		@Kroll.argument(optional = true) Object arg)
	{
		if (!(windowToOpen instanceof TiWindowProxy)) {
			Log.w(TAG, "openWindow() requires a Window proxy.");
			return;
		}

		if (navigationView == null) {
			getOrCreateView();
			showRootIfReady();
		}

		if (navigationView != null) {
			navigationView.pushWindow((TiWindowProxy) windowToOpen);
		}
	}

	@Kroll.method
	public void closeWindow(Object windowToClose,
		@Kroll.argument(optional = true) Object arg)
	{
		if (!(windowToClose instanceof TiWindowProxy)) {
			Log.w(TAG, "closeWindow() requires a Window proxy.");
			return;
		}

		if (navigationView != null) {
			navigationView.popWindow((TiWindowProxy) windowToClose);
		}
	}

	@Kroll.method
	public void popToRootWindow(@Kroll.argument(optional = true) Object arg)
	{
		if (navigationView != null) {
			navigationView.popToRoot();
		}
	}

	@Kroll.getProperty
	public TiWindowProxy[] getWindows()
	{
		if (navigationView != null) {
			return navigationView.getWindowStack()
				.toArray(new TiWindowProxy[0]);
		}
		return new TiWindowProxy[0];
	}

	public TiWindowProxy getRootTiWindowProxy()
	{
		if (navigationView != null
			&& !navigationView.getWindowStack().isEmpty()) {
			return navigationView.getWindowStack().get(0);
		}
		return null;
	}

	@Override
	public String getApiName()
	{
		return "Ti.UI.NavigationView";
	}
}
