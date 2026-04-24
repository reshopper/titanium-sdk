/**
 * Titanium SDK
 * Copyright TiDev, Inc. 04/07/2022-Present. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */
package ti.modules.titanium.ui;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollEventCallback;
import org.appcelerator.kroll.KrollPromise;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.proxy.TiWindowProxy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Kroll.proxy(creatableInModule = UIModule.class)
public class NavigationWindowProxy extends WindowProxy
{
	private static final String TAG = "NavigationWindowProxy";

	private final List<TiWindowProxy> windows = new ArrayList<>();
	private final Map<TiWindowProxy, Integer> closeListenerIds = new HashMap<>();

	public NavigationWindowProxy()
	{
		super();
	}

	@Kroll.getProperty
	public TiWindowProxy[] getWindows()
	{
		return windows.toArray(new TiWindowProxy[0]);
	}

	@Override
	@Kroll.method
	public KrollPromise<Void> open(@Kroll.argument(optional = true) Object arg)
	{
		if (opened) {
			return KrollPromise.create((promise) -> {
				promise.resolve(null);
			});
		}

		clearWillCloseFiredFlag();
		opened = true;

		if (getProperties().containsKeyAndNotNull(TiC.PROPERTY_WINDOW)) {
			Object rootView = getProperties().get(TiC.PROPERTY_WINDOW);
			if (rootView instanceof WindowProxy || rootView instanceof TabGroupProxy) {
				openWindow(rootView, arg);
			}
		}

		fireEvent(TiC.EVENT_OPEN, null);
		return KrollPromise.create((promise) -> {
			promise.resolve(null);
		});
	}

	@SuppressWarnings("unchecked")
	@Override
	@Kroll.method
	public KrollPromise<Void> close(@Kroll.argument(optional = true) Object arg)
	{
		if (!opened) {
			return KrollPromise.create((promise) -> {
				promise.resolve(null);
			});
		}

		return KrollPromise.create((promise) -> {
			KrollDict options;
			if (arg instanceof HashMap<?, ?>) {
				options = new KrollDict((HashMap<String, Object>) arg);
			} else {
				options = new KrollDict();
			}

			opened = false;
			popToRootWindow(options);
			if (!windows.isEmpty()) {
				TiWindowProxy rootWindow = windows.get(0);
				rootWindow.close(options);
			}
			fireEvent(TiC.EVENT_CLOSE, null);
			promise.resolve(null);
		});
	}

	@Kroll.method
	public void popToRootWindow(@Kroll.argument(optional = true) Object arg)
	{
		for (int i = windows.size() - 1; i > 0; i--) {
			TiWindowProxy window = windows.get(i);
			window.close(arg);
		}
	}

	@Kroll.method
	public void openWindow(Object childToOpen, @Kroll.argument(optional = true) Object arg)
	{
		if (!opened) {
			open(null);
		}

		if (!(childToOpen instanceof TiWindowProxy)) {
			return;
		}

		TiWindowProxy child = (TiWindowProxy) childToOpen;
		windows.add(child);
		child.setNavigationWindow(this);

		int listenerId = child.addEventListener(TiC.EVENT_CLOSE, new KrollEventCallback() {
			@Override
			public void call(Object data)
			{
				removeWindowFromStack(child);
			}
		});
		closeListenerIds.put(child, listenerId);

		if (childToOpen instanceof WindowProxy) {
			((WindowProxy) childToOpen).open(arg);
		} else if (childToOpen instanceof TabGroupProxy) {
			((TabGroupProxy) childToOpen).callPropertySync(TiC.PROPERTY_OPEN, new Object[] { arg });
		}
	}

	@Kroll.method
	public void closeWindow(Object childToClose, @Kroll.argument(optional = true) Object arg)
	{
		if (!(childToClose instanceof TiWindowProxy)) {
			return;
		}

		TiWindowProxy window = (TiWindowProxy) childToClose;
		window.close(arg);
	}

	private void removeWindowFromStack(TiWindowProxy window)
	{
		windows.remove(window);
		Integer listenerId = closeListenerIds.remove(window);
		if (listenerId != null) {
			window.removeEventListener(TiC.EVENT_CLOSE, listenerId);
		}
		if (window.getNavigationWindow() == this) {
			window.setNavigationWindow(null);
		}
	}

	@Override
	public String getApiName()
	{
		return "Ti.UI.NavigationWindow";
	}

	public TiWindowProxy getRootTiWindowProxy()
	{
		if (!windows.isEmpty()) {
			return windows.get(0);
		}
		return null;
	}
}
