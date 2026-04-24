/**
 * Titanium SDK
 * Copyright TiDev, Inc. 04/07/2022-Present. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */
package ti.modules.titanium.ui.widget;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.proxy.TiWindowProxy;
import org.appcelerator.titanium.util.TiColorHelper;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.view.TiUIView;

import java.util.ArrayList;
import java.util.List;

/**
 * Embeddable navigation view that manages a stack of WindowProxy content
 * by swapping native views in a FrameLayout container. Does not use
 * Fragments, so it works correctly inside Dialogs (e.g. BottomSheetDialog)
 * which have their own window separate from the Activity.
 */
public class TiUINavigationView extends TiUIView
{
	private static final String TAG = "TiUINavigationView";

	private LinearLayout rootLayout;
	private MaterialToolbar toolbar;
	private FrameLayout container;
	private final List<TiWindowProxy> windowStack = new ArrayList<>();
	private final List<View> viewStack = new ArrayList<>();
	private OnBackPressedCallback backPressedCallback;

	public TiUINavigationView(TiViewProxy proxy)
	{
		super(proxy);

		Activity activity = proxy.getActivity();

		rootLayout = new LinearLayout(activity);
		rootLayout.setOrientation(LinearLayout.VERTICAL);
		rootLayout.setLayoutParams(new ViewGroup.LayoutParams(
			ViewGroup.LayoutParams.MATCH_PARENT,
			ViewGroup.LayoutParams.MATCH_PARENT));

		toolbar = new MaterialToolbar(activity);
		toolbar.setLayoutParams(new LinearLayout.LayoutParams(
			ViewGroup.LayoutParams.MATCH_PARENT,
			ViewGroup.LayoutParams.WRAP_CONTENT));
		toolbar.setVisibility(View.GONE);
		rootLayout.addView(toolbar);

		container = new FrameLayout(activity);
		container.setLayoutParams(new LinearLayout.LayoutParams(
			ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f));
		rootLayout.addView(container);

		setNativeView(rootLayout);
	}

	/**
	 * Show the root window content in the container.
	 */
	public void showRootWindow(TiWindowProxy rootWindow)
	{
		if (rootWindow == null) {
			return;
		}

		windowStack.clear();
		viewStack.clear();
		container.removeAllViews();

		View nativeView = resolveNativeView(rootWindow);
		if (nativeView == null) {
			return;
		}

		windowStack.add(rootWindow);
		viewStack.add(nativeView);
		container.addView(nativeView, new FrameLayout.LayoutParams(
			ViewGroup.LayoutParams.MATCH_PARENT,
			ViewGroup.LayoutParams.MATCH_PARENT));

		updateToolbar(rootWindow);
		registerBackPressedCallback();

		rootWindow.fireEvent(TiC.EVENT_OPEN, null);
	}

	/**
	 * Push a new window onto the navigation stack with a slide-in animation.
	 */
	public void pushWindow(TiWindowProxy window)
	{
		if (window == null) {
			return;
		}

		View nativeView = resolveNativeView(window);
		if (nativeView == null) {
			return;
		}

		// Hide current top view.
		if (!viewStack.isEmpty()) {
			View current = viewStack.get(viewStack.size() - 1);
			current.setVisibility(View.GONE);
		}

		windowStack.add(window);
		viewStack.add(nativeView);

		container.addView(nativeView, new FrameLayout.LayoutParams(
			ViewGroup.LayoutParams.MATCH_PARENT,
			ViewGroup.LayoutParams.MATCH_PARENT));

		Animation slideIn = AnimationUtils.loadAnimation(
			proxy.getActivity(), android.R.anim.slide_in_left);
		nativeView.startAnimation(slideIn);

		updateToolbar(window);
		updateBackPressedCallback();

		window.fireEvent(TiC.EVENT_OPEN, null);
	}

	/**
	 * Pop the given window from the navigation stack.
	 */
	public void popWindow(TiWindowProxy window)
	{
		if (windowStack.size() <= 1) {
			return;
		}

		int index = windowStack.indexOf(window);
		if (index < 1) {
			return;
		}

		View removedView = viewStack.get(index);
		windowStack.remove(index);
		viewStack.remove(index);

		Animation slideOut = AnimationUtils.loadAnimation(
			proxy.getActivity(), android.R.anim.slide_out_right);
		slideOut.setAnimationListener(new Animation.AnimationListener()
		{
			@Override
			public void onAnimationStart(Animation animation)
			{
			}

			@Override
			public void onAnimationEnd(Animation animation)
			{
				container.removeView(removedView);
			}

			@Override
			public void onAnimationRepeat(Animation animation)
			{
			}
		});
		removedView.startAnimation(slideOut);

		// Reveal previous view.
		if (!viewStack.isEmpty()) {
			View previous = viewStack.get(viewStack.size() - 1);
			previous.setVisibility(View.VISIBLE);
		}

		TiWindowProxy current = getCurrentWindow();
		if (current != null) {
			updateToolbar(current);
		}
		updateBackPressedCallback();

		window.fireEvent(TiC.EVENT_CLOSE, null);
	}

	/**
	 * Pop all windows except the root.
	 */
	public void popToRoot()
	{
		if (windowStack.size() <= 1) {
			return;
		}

		List<TiWindowProxy> closed = new ArrayList<>(
			windowStack.subList(1, windowStack.size()));

		// Remove all views except the root.
		for (int i = viewStack.size() - 1; i > 0; i--) {
			container.removeView(viewStack.get(i));
			viewStack.remove(i);
		}
		windowStack.subList(1, windowStack.size()).clear();

		// Show root.
		if (!viewStack.isEmpty()) {
			viewStack.get(0).setVisibility(View.VISIBLE);
		}

		TiWindowProxy current = getCurrentWindow();
		if (current != null) {
			updateToolbar(current);
		}
		updateBackPressedCallback();

		for (TiWindowProxy w : closed) {
			w.fireEvent(TiC.EVENT_CLOSE, null);
		}
	}

	public List<TiWindowProxy> getWindowStack()
	{
		return windowStack;
	}

	public TiWindowProxy getCurrentWindow()
	{
		if (windowStack.isEmpty()) {
			return null;
		}
		return windowStack.get(windowStack.size() - 1);
	}

	// -- Resolve native view from proxy --

	private View resolveNativeView(TiViewProxy viewProxy)
	{
		viewProxy.setActivity(proxy.getActivity());
		TiUIView tiView = viewProxy.getOrCreateView();
		if (tiView == null) {
			return null;
		}

		View nativeView = tiView.getOuterView();
		if (nativeView == null) {
			nativeView = tiView.getNativeView();
		}

		if (nativeView != null && nativeView.getParent() instanceof ViewGroup) {
			((ViewGroup) nativeView.getParent()).removeView(nativeView);
		}

		return nativeView;
	}

	// -- Toolbar --

	private void updateToolbar(TiWindowProxy window)
	{
		if (toolbar == null || window == null) {
			return;
		}

		KrollDict props = window.getProperties();
		String title = props.optString(TiC.PROPERTY_TITLE, null);
		boolean isRoot = (windowStack.indexOf(window) == 0);
		boolean hidesBackButton = props.optBoolean(
			TiC.PROPERTY_HIDES_BACK_BUTTON, false);

		if (title != null) {
			toolbar.setTitle(title);
			toolbar.setVisibility(View.VISIBLE);
		} else {
			toolbar.setTitle("");
			toolbar.setVisibility(View.GONE);
		}

		if (!isRoot && !hidesBackButton) {
			toolbar.setNavigationIcon(
				androidx.appcompat.R.drawable.abc_ic_ab_back_material);
			toolbar.setNavigationOnClickListener(v -> {
				TiWindowProxy top = getCurrentWindow();
				if (top != null && windowStack.size() > 1) {
					popWindow(top);
				}
			});
		} else {
			toolbar.setNavigationIcon(null);
			toolbar.setNavigationOnClickListener(null);
		}

		if (props.containsKeyAndNotNull(TiC.PROPERTY_BAR_COLOR)) {
			int color = TiColorHelper.parseColor(
				TiConvert.toString(props.get(TiC.PROPERTY_BAR_COLOR)),
				proxy.getActivity());
			toolbar.setBackgroundColor(color);
			toolbar.setVisibility(View.VISIBLE);
		}
	}

	// -- Back press --

	private void registerBackPressedCallback()
	{
		Activity activity = proxy.getActivity();
		if (!(activity instanceof AppCompatActivity)) {
			return;
		}

		backPressedCallback = new OnBackPressedCallback(false)
		{
			@Override
			public void handleOnBackPressed()
			{
				TiWindowProxy top = getCurrentWindow();
				if (top != null && windowStack.size() > 1) {
					popWindow(top);
				}
			}
		};

		((AppCompatActivity) activity).getOnBackPressedDispatcher()
			.addCallback(backPressedCallback);
	}

	private void updateBackPressedCallback()
	{
		if (backPressedCallback != null) {
			backPressedCallback.setEnabled(windowStack.size() > 1);
		}
	}

	@Override
	public void release()
	{
		if (backPressedCallback != null) {
			backPressedCallback.remove();
			backPressedCallback = null;
		}
		windowStack.clear();
		viewStack.clear();
		super.release();
	}
}
