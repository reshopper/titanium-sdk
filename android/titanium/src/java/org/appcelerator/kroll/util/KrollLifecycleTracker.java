/**
 * Titanium SDK
 * Copyright TiDev, Inc. 04/07/2022-Present
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */
package org.appcelerator.kroll.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.appcelerator.kroll.common.Log;

/**
 * Debug utility for tracking proxy and view lifecycle during development.
 * Helps identify memory leaks and lifecycle issues.
 */
public class KrollLifecycleTracker
{
	private static final String TAG = "KrollLifecycleTracker";
	private static final boolean DEBUG_ENABLED = Log.isDebugModeEnabled();
	
	private static final AtomicInteger totalProxyCount = new AtomicInteger(0);
	private static final AtomicInteger totalViewCount = new AtomicInteger(0);
	
	private static final Map<String, AtomicInteger> proxyClassCounts = new HashMap<>();
	private static final Map<String, AtomicInteger> viewClassCounts = new HashMap<>();
	
	/**
	 * Track proxy creation
	 */
	public static void trackProxyCreated(Object proxy)
	{
		if (!DEBUG_ENABLED) return;
		
		String className = proxy.getClass().getSimpleName();
		int total = totalProxyCount.incrementAndGet();
		
		// Get additional info for better tracking
		String proxyInfo = getProxyInfo(proxy);
		
		synchronized (proxyClassCounts)
		{
			AtomicInteger count = proxyClassCounts.get(className);
			if (count == null) {
				count = new AtomicInteger(0);
				proxyClassCounts.put(className, count);
			}
			count.incrementAndGet();
		}
		
		Log.d(TAG, "PROXY CREATED: " + className + proxyInfo + " - Total: " + total, Log.DEBUG_MODE);
	}
	
	/**
	 * Track proxy destruction
	 */
	public static void trackProxyDestroyed(Object proxy)
	{
		if (!DEBUG_ENABLED) return;
		
		String className = proxy.getClass().getSimpleName();
		int total = totalProxyCount.decrementAndGet();
		
		synchronized (proxyClassCounts)
		{
			AtomicInteger count = proxyClassCounts.get(className);
			if (count != null) {
				count.decrementAndGet();
			}
		}
		
		Log.d(TAG, "PROXY DESTROYED: " + className + " (" + proxy.hashCode() + ") - Total: " + total, Log.DEBUG_MODE);
	}
	
	/**
	 * Track view creation
	 */
	public static void trackViewCreated(Object view)
	{
		if (!DEBUG_ENABLED) return;
		
		String className = view.getClass().getSimpleName();
		int total = totalViewCount.incrementAndGet();
		
		// Get additional info for better tracking
		String viewInfo = getViewInfo(view);
		
		synchronized (viewClassCounts)
		{
			AtomicInteger count = viewClassCounts.get(className);
			if (count == null) {
				count = new AtomicInteger(0);
				viewClassCounts.put(className, count);
			}
			count.incrementAndGet();
		}
		
		Log.d(TAG, "VIEW CREATED: " + className + viewInfo + " - Total: " + total, Log.DEBUG_MODE);
	}
	
	/**
	 * Track view destruction
	 */
	public static void trackViewDestroyed(Object view)
	{
		if (!DEBUG_ENABLED) return;
		
		String className = view.getClass().getSimpleName();
		int total = totalViewCount.decrementAndGet();
		
		synchronized (viewClassCounts)
		{
			AtomicInteger count = viewClassCounts.get(className);
			if (count != null) {
				count.decrementAndGet();
			}
		}
		
		Log.d(TAG, "VIEW DESTROYED: " + className + " (" + view.hashCode() + ") - Total: " + total, Log.DEBUG_MODE);
	}
	
	/**
	 * Print current lifecycle statistics
	 */
	public static void printStats()
	{
		if (!DEBUG_ENABLED) return;
		
		Log.d(TAG, "=== LIFECYCLE STATS ===", Log.DEBUG_MODE);
		Log.d(TAG, "Total Proxies: " + totalProxyCount.get(), Log.DEBUG_MODE);
		Log.d(TAG, "Total Views: " + totalViewCount.get(), Log.DEBUG_MODE);
		
		Log.d(TAG, "Proxy breakdown:", Log.DEBUG_MODE);
		synchronized (proxyClassCounts)
		{
			for (Map.Entry<String, AtomicInteger> entry : proxyClassCounts.entrySet()) {
				int count = entry.getValue().get();
				if (count > 0) {
					Log.d(TAG, "  " + entry.getKey() + ": " + count, Log.DEBUG_MODE);
				}
			}
		}
		
		Log.d(TAG, "View breakdown:", Log.DEBUG_MODE);
		synchronized (viewClassCounts)
		{
			for (Map.Entry<String, AtomicInteger> entry : viewClassCounts.entrySet()) {
				int count = entry.getValue().get();
				if (count > 0) {
					Log.d(TAG, "  " + entry.getKey() + ": " + count, Log.DEBUG_MODE);
				}
			}
		}
		Log.d(TAG, "=======================", Log.DEBUG_MODE);
	}
	
	/**
	 * Get current proxy count
	 */
	public static int getLiveProxyCount()
	{
		return totalProxyCount.get();
	}
	
	/**
	 * Get current view count
	 */
	public static int getLiveViewCount()
	{
		return totalViewCount.get();
	}
	
	/**
	 * Reset all counters
	 */
	public static void reset()
	{
		if (!DEBUG_ENABLED) return;
		
		totalProxyCount.set(0);
		totalViewCount.set(0);
		
		synchronized (proxyClassCounts)
		{
			proxyClassCounts.clear();
		}
		
		synchronized (viewClassCounts)
		{
			viewClassCounts.clear();
		}
		
		Log.d(TAG, "Stats reset", Log.DEBUG_MODE);
	}
	
	/**
	 * Get additional proxy information for tracking
	 */
	private static String getProxyInfo(Object proxy)
	{
		StringBuilder info = new StringBuilder();
		info.append(" (hash: ").append(proxy.hashCode()).append(")");
		
		try {
			// Try to get proxy ID if it's a KrollProxy
			if (proxy instanceof org.appcelerator.kroll.KrollProxy) {
				org.appcelerator.kroll.KrollProxy krollProxy = (org.appcelerator.kroll.KrollProxy) proxy;
				String proxyId = krollProxy.getProxyId();
				if (proxyId != null) {
					info.append(" [ID: ").append(proxyId).append("]");
				}
				
				// Try to get custom properties that might identify the proxy
				Object customId = krollProxy.getProperty("id");
				if (customId != null) {
					info.append(" [customId: ").append(customId).append("]");
				}
			}
		} catch (Exception e) {
			// Ignore errors, just use hash
		}
		
		return info.toString();
	}
	
	/**
	 * Get additional view information for tracking
	 */
	private static String getViewInfo(Object view)
	{
		StringBuilder info = new StringBuilder();
		info.append(" (hash: ").append(view.hashCode()).append(")");
		
		try {
			// Try to get Android View ID if it's a TiUIView
			if (view instanceof org.appcelerator.titanium.view.TiUIView) {
				org.appcelerator.titanium.view.TiUIView tiView = (org.appcelerator.titanium.view.TiUIView) view;
				android.view.View nativeView = tiView.getNativeView();
				if (nativeView != null) {
					int androidId = nativeView.getId();
					if (androidId != android.view.View.NO_ID) {
						info.append(" [androidId: ").append(androidId).append("]");
					}
					
					// Try to get tag if set
					Object tag = nativeView.getTag();
					if (tag != null) {
						info.append(" [tag: ").append(tag).append("]");
					}
				}
				
				// Try to get proxy info
				org.appcelerator.titanium.proxy.TiViewProxy proxy = tiView.getProxy();
				if (proxy != null) {
					String proxyId = proxy.getProxyId();
					if (proxyId != null) {
						info.append(" [proxyId: ").append(proxyId).append("]");
					}
					
					// Check for custom ID
					Object customId = proxy.getProperty("id");
					if (customId != null) {
						info.append(" [customId: ").append(customId).append("]");
					}
				}
			}
		} catch (Exception e) {
			// Ignore errors, just use hash
		}
		
		return info.toString();
	}
}