/**
 * Titanium SDK
 * Copyright TiDev, Inc. 04/07/2022-Present. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */
package ti.modules.titanium.ui.widget;

import java.lang.ref.SoftReference;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.TiDimension;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.util.TiFileHelper;
import org.appcelerator.titanium.util.TiUIHelper;
import org.appcelerator.titanium.view.TiUIView;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Rect;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.view.Gravity;
import android.widget.SeekBar;

public class TiUISlider extends TiUIView implements SeekBar.OnSeekBarChangeListener
{
	private static final String TAG = "TiUISlider";

	private int min;
	private int max;
	private float pos;
	private int offset;
	private int minRange;
	private int maxRange;
	private int scaleFactor;
	private ClipDrawable rightClipDrawable;
	private float[] steps;
	private boolean snapToSteps = false;
	private float lastFiredValue = Float.NaN;
	private boolean stepValues = false;

	private SoftReference<Drawable> thumbDrawable;

	public TiUISlider(final TiViewProxy proxy)
	{
		super(proxy);
		Log.d(TAG, "Creating a seekBar", Log.DEBUG_MODE);

		layoutParams.autoFillsWidth = true;

		this.min = 0;
		this.max = 1;
		this.pos = 0;

		SeekBar seekBar = new SeekBar(proxy.getActivity()) {
			@Override
			protected void onLayout(boolean changed, int left, int top, int right, int bottom)
			{
				super.onLayout(changed, left, top, right, bottom);
				TiUIHelper.firePostLayoutEvent(proxy);
			}
		};
		seekBar.setOnSeekBarChangeListener(this);
		setNativeView(seekBar);
	}

	@Override
	public void processProperties(KrollDict d)
	{
		super.processProperties(d);

		SeekBar seekBar = (SeekBar) getNativeView();
		Activity activity = proxy.getActivity();

		if (d.containsKey(TiC.PROPERTY_VALUE)) {
			pos = TiConvert.toFloat(d, TiC.PROPERTY_VALUE, 0);
		}
		if (d.containsKey(TiC.PROPERTY_MIN)) {
			min = TiConvert.toInt(d.get(TiC.PROPERTY_MIN), 0);
		}
		if (d.containsKey(TiC.PROPERTY_MAX)) {
			max = TiConvert.toInt(d.get(TiC.PROPERTY_MAX), 0);
		}
		if (d.containsKey("minRange")) {
			minRange = TiConvert.toInt(d.get("minRange"), 0);
		} else {
			minRange = min;
		}
		if (d.containsKey("maxRange")) {
			maxRange = TiConvert.toInt(d.get("maxRange"), 0);
		} else {
			maxRange = max;
		}
		if (d.containsKey("thumbImage")) {
			updateThumb(seekBar, d);
		}
		if (d.containsKey("thumbSize")) {
			// thumbSize will be applied in updateThumb if thumbImage is also present
			// or we can set a default thumb with custom size
			if (!d.containsKey("thumbImage")) {
				updateThumb(seekBar, d);
			}
		}
		if (d.containsKey(TiC.PROPERTY_SPLIT_TRACK)) {
			seekBar.setSplitTrack(TiConvert.toBoolean(d.get(TiC.PROPERTY_SPLIT_TRACK)));
		}
		if (d.containsKey("leftTrackImage") || d.containsKey("rightTrackImage")) {
			updateTrackingImages(seekBar, d);
		}
		if (d.containsKeyAndNotNull(TiC.PROPERTY_TINT_COLOR)) {
			handleSetTintColor(TiConvert.toColor(d, TiC.PROPERTY_TINT_COLOR, activity));
		}
		if (d.containsKeyAndNotNull(TiC.PROPERTY_TRACK_TINT_COLOR)) {
			handleSetTrackTintColor(TiConvert.toColor(d, TiC.PROPERTY_TRACK_TINT_COLOR, activity));
		}
		if (d.containsKey("steps")) {
			setSteps(d.get("steps"));
		}
		if (d.containsKey("stepValues")) {
			stepValues = TiConvert.toBoolean(d.get("stepValues"), false);
		}
		updateRange();
		updateControl();
		updateRightDrawable();
	}

	private void updateRightDrawable()
	{
		if (rightClipDrawable != null) {
			SeekBar seekBar = (SeekBar) getNativeView();
			double percent = (double) seekBar.getProgress() / (double) seekBar.getMax();
			int level = 10000 - (int) Math.floor(percent * 10000);
			rightClipDrawable.setLevel(level);
		}
	}

	private void updateRange()
	{
		minRange = Math.max(minRange, min);
		minRange = Math.min(minRange, max);
		proxy.setProperty("minRange", minRange);

		maxRange = Math.min(maxRange, max);
		maxRange = Math.max(maxRange, minRange);
		proxy.setProperty("maxRange", maxRange);
	}

	private void updateControl()
	{
		offset = -min;
		scaleFactor = 100;
		int length = (int) Math.floor(Math.sqrt(Math.pow(max - min, 2)));
		if ((length > 0) && (Integer.MAX_VALUE / length < scaleFactor)) {
			scaleFactor = Integer.MAX_VALUE / length;
			scaleFactor = (scaleFactor == 0) ? 1 : scaleFactor;
		}
		length *= scaleFactor;
		SeekBar seekBar = (SeekBar) getNativeView();
		int curPos = (int) Math.floor(scaleFactor * (pos + offset));
		//On Android 4.0+ this will result in a callback to the listener. So set length after calculating position
		seekBar.setMax(length);
		seekBar.setProgress(curPos);
	}

	private void updateThumb(SeekBar seekBar, KrollDict d)
	{
		Object thumbImageObj = d.get("thumbImage");
		Object thumbSizeObj = d.get("thumbSize");

		if (thumbImageObj != null || thumbSizeObj != null) {
			Drawable thumb = null;

			if (thumbImageObj != null) {
				// ✅ First check if it's a resource ID (integer)
				if (thumbImageObj instanceof Number) {
					int resourceId = ((Number) thumbImageObj).intValue();
					try {
						Context context = seekBar.getContext();
						thumb = context.getResources().getDrawable(resourceId, context.getTheme());
						Log.d(TAG, "Successfully loaded thumb from resource ID: " + resourceId);
					} catch (Exception e) {
						Log.e(TAG, "Unable to load thumb from resource ID: " + resourceId
							+ " - " + e.getMessage());
					}
				} else {
					// ✅ For strings and other types, use TiDrawableReference
					try {
						org.appcelerator.titanium.view.TiDrawableReference drawableRef =
							org.appcelerator.titanium.view.TiDrawableReference.fromObject(proxy, thumbImageObj);
						thumb = drawableRef.getDrawable();

						if (thumb != null) {
							Log.d(TAG, "Successfully loaded thumb drawable using TiDrawableReference");
						} else {
							Log.w(TAG, "TiDrawableReference returned null drawable for: " + thumbImageObj);
						}
					} catch (Exception e) {
						Log.e(TAG, "Error loading thumb with TiDrawableReference: " + e.getMessage());
					}
				}
			}

			// ✅ Apply custom sizing if specified
			if (thumbSizeObj != null) {
				thumb = applyThumbSize(thumb, thumbSizeObj, seekBar);
			}

			// ✅ Apply the thumb drawable
			if (thumb != null) {
				thumbDrawable = new SoftReference<>(thumb);
				seekBar.setThumb(thumb);
				Log.d(TAG, "Thumb applied successfully");
			} else {
				Log.e(TAG, "Unable to load thumb drawable from: " + thumbImageObj);
			}
		} else {
			seekBar.setThumb(null);
		}
	}

	/**
	 * Applies custom sizing to a thumb drawable.
	 * @param thumb The original drawable (can be null for default thumb)
	 * @param thumbSizeObj The size specification (number for both dimensions, or object with width/height)
	 * @param seekBar The SeekBar for context
	 * @return Sized drawable or null if sizing failed
	 */
	private Drawable applyThumbSize(Drawable thumb, Object thumbSizeObj, SeekBar seekBar)
	{
		try {
			int width = 0, height = 0;

			if (thumbSizeObj instanceof Number) {
				// Single number = both width and height
				int size = TiConvert.toInt(thumbSizeObj);
				TiDimension widthDim = TiConvert.toTiDimension(size, TiDimension.TYPE_WIDTH);
				width = height = (int) widthDim.getAsPixels(seekBar);
			} else if (thumbSizeObj instanceof KrollDict) {
				// Object with width and height properties
				KrollDict sizeDict = (KrollDict) thumbSizeObj;
				if (sizeDict.containsKey("width")) {
					TiDimension widthDim = TiConvert.toTiDimension(sizeDict.get("width"),
						TiDimension.TYPE_WIDTH);
					width = (int) widthDim.getAsPixels(seekBar);
				}
				if (sizeDict.containsKey("height")) {
					TiDimension heightDim = TiConvert.toTiDimension(sizeDict.get("height"),
						TiDimension.TYPE_HEIGHT);
					height = (int) heightDim.getAsPixels(seekBar);
				}
			}

			if (width > 0 && height > 0) {
				if (thumb == null) {
					// Create a default circular thumb if no image provided
					thumb = createDefaultThumb(seekBar, width, height);
				} else {
					// Resize existing drawable
					thumb.setBounds(0, 0, width, height);
				}
				Log.d(TAG, "Applied thumb size: " + width + "x" + height);
			}

		} catch (Exception e) {
			Log.e(TAG, "Error applying thumb size: " + e.getMessage());
		}

		return thumb;
	}

	/**
	 * Creates a default circular thumb drawable with specified dimensions.
	 */
	private Drawable createDefaultThumb(SeekBar seekBar, int width, int height)
	{
		try {
			android.graphics.drawable.GradientDrawable drawable = new android.graphics.drawable.GradientDrawable();
			drawable.setShape(android.graphics.drawable.GradientDrawable.OVAL);
			drawable.setSize(width, height);
			drawable.setColor(0xFF2196F3); // Default blue color
			drawable.setStroke(3, 0xFFFFFFFF); // White border
			return drawable;
		} catch (Exception e) {
			Log.e(TAG, "Error creating default thumb: " + e.getMessage());
			return null;
		}
	}

	private void updateTrackingImages(SeekBar seekBar, KrollDict d)
	{
		String leftImage = TiConvert.toString(d, "leftTrackImage");
		String rightImage = TiConvert.toString(d, "rightTrackImage");

		Drawable leftDrawable = null;
		Drawable rightDrawable = null;
		TiFileHelper tfh = new TiFileHelper(seekBar.getContext());

		if (leftImage != null) {
			String leftUrl = proxy.resolveUrl(null, leftImage);
			if (leftUrl != null) {
				leftDrawable = tfh.loadDrawable(leftUrl, false, true);
				if (leftDrawable == null) {
					Log.e(TAG, "Unable to locate left image for progress bar: " + leftUrl);
				}
			}
		}

		if (rightImage != null) {
			String rightUrl = proxy.resolveUrl(null, rightImage);
			if (rightUrl != null) {
				rightDrawable = tfh.loadDrawable(rightUrl, false, true);
				if (rightDrawable == null) {
					Log.e(TAG, "Unable to locate right image for progress bar: " + rightUrl);
				}
			}
		}

		if (leftDrawable != null || rightDrawable != null) {
			LayerDrawable ld = null;
			if (rightDrawable == null) {
				Drawable[] lda = { new ClipDrawable(leftDrawable, Gravity.LEFT, ClipDrawable.HORIZONTAL) };
				ld = new LayerDrawable(lda);
				ld.setId(0, android.R.id.progress);
			} else if (leftDrawable == null) {
				rightClipDrawable = new ClipDrawable(rightDrawable, Gravity.RIGHT, ClipDrawable.HORIZONTAL);
				Drawable[] lda = { rightClipDrawable };
				ld = new LayerDrawable(lda);
				ld.setId(0, android.R.id.secondaryProgress);
			} else {
				Drawable[] lda = {
					rightDrawable,
					new ClipDrawable(leftDrawable, Gravity.LEFT, ClipDrawable.HORIZONTAL)
				};
				ld = new LayerDrawable(lda);
				ld.setId(0, android.R.id.background);
				ld.setId(1, android.R.id.progress);
			}
			seekBar.setProgressDrawable(ld);
		} else {
			Log.w(TAG, "Custom tracking images could not be loaded.");
		}
	}

	@Override
	public void propertyChanged(String key, Object oldValue, Object newValue, KrollProxy proxy)
	{
		if (Log.isDebugModeEnabled()) {
			Log.d(TAG, "Property: " + key + " old: " + oldValue + " new: " + newValue, Log.DEBUG_MODE);
		}

		SeekBar seekBar = (SeekBar) getNativeView();
		if (seekBar == null) {
			return;
		}

		if (key.equals(TiC.PROPERTY_VALUE)) {
			pos = TiConvert.toFloat(newValue);
			int curPos = (int) Math.floor(scaleFactor * (pos + offset));
			seekBar.setProgress(curPos);
			onProgressChanged(seekBar, curPos, false);
		} else if (key.equals("min")) {
			min = TiConvert.toInt(newValue);
			minRange = min;
			updateRange();
			if (pos < minRange) {
				pos = minRange;
			}
			updateControl();
			int curPos = (int) Math.floor(scaleFactor * (pos + offset));
			onProgressChanged(seekBar, curPos, false);
		} else if (key.equals("minRange")) {
			minRange = TiConvert.toInt(newValue);
			updateRange();
			if (pos < minRange) {
				pos = minRange;
			}
			updateControl();
			int curPos = (int) Math.floor(scaleFactor * (pos + offset));
			onProgressChanged(seekBar, curPos, false);
		} else if (key.equals("max")) {
			max = TiConvert.toInt(newValue);
			maxRange = max;
			updateRange();
			if (pos > maxRange) {
				pos = maxRange;
			}
			updateControl();
			int curPos = (int) Math.floor(scaleFactor * (pos + offset));
			onProgressChanged(seekBar, curPos, false);
		} else if (key.equals("maxRange")) {
			maxRange = TiConvert.toInt(newValue);
			updateRange();
			if (pos > maxRange) {
				pos = maxRange;
			}
			updateControl();
			int curPos = (int) Math.floor(scaleFactor * (pos + offset));
			onProgressChanged(seekBar, curPos, false);
		} else if (key.equals(TiC.PROPERTY_TINT_COLOR)) {
			// TODO: reset to default value when property is null
			if (newValue != null) {
				handleSetTintColor(TiConvert.toColor(newValue, proxy.getActivity()));
			}
		} else if (key.equals(TiC.PROPERTY_TRACK_TINT_COLOR)) {
			// TODO: reset to default value when property is null
			if (newValue != null) {
				handleSetTrackTintColor(TiConvert.toColor(newValue, proxy.getActivity()));
			}
		} else if (key.equals("thumbImage")) {
			//updateThumb(seekBar, proxy.getDynamicProperties());
			//seekBar.invalidate();
			Log.i(TAG, "Dynamically changing thumbImage is not yet supported. Native control doesn't draw");
		} else if (key.equals("thumbSize")) {
			// For dynamic thumbSize changes, we need to recreate the thumb
			Log.i(TAG, "Dynamically changing thumbSize is not yet supported. Native control doesn't draw");
		} else if (key.equals(TiC.PROPERTY_SPLIT_TRACK)) {
			seekBar.setSplitTrack(TiConvert.toBoolean(newValue));
		} else if (key.equals("leftTrackImage") || key.equals("rightTrackImage")) {
			//updateTrackingImages(seekBar, proxy.getDynamicProperties());
			//seekBar.invalidate();
			String infoMessage
				= "Dynamically changing leftTrackImage or rightTrackImage is not yet supported. "
				+ "Native control doesn't draw.";
			Log.i(TAG, infoMessage);
		} else if (key.equals("steps")) {
			setSteps(newValue);
		} else if (key.equals("stepValues")) {
			stepValues = TiConvert.toBoolean(newValue, false);
		} else {
			super.propertyChanged(key, oldValue, newValue, proxy);
		}
	}

	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
	{
		pos = seekBar.getProgress() * 1.0f / scaleFactor;

		// Range check
		int actualMinRange = minRange + offset;
		int actualMaxRange = maxRange + offset;

		if (pos < actualMinRange) {
			seekBar.setProgress(actualMinRange * scaleFactor);
			pos = minRange;
		} else if (pos > actualMaxRange) {
			seekBar.setProgress(actualMaxRange * scaleFactor);
			pos = maxRange;
		}

		float finalValue = pos + min;

		// ✅ Apply snapping if steps are enabled and this is user input
		if (snapToSteps && fromUser) {
			float snappedValue = findNearestStep(finalValue);
			if (Math.abs(finalValue - snappedValue) > 0.001f) {
				// Need to snap to different value
				pos = snappedValue - min;
				int curPos = (int) Math.floor(scaleFactor * (pos + offset));
				// Update seekbar without triggering listener recursion
				seekBar.setOnSeekBarChangeListener(null);
				seekBar.setProgress(curPos);
				seekBar.setOnSeekBarChangeListener(this);
				finalValue = snappedValue;
			}
		}

		updateRightDrawable();

		// ✅ Only fire change event if value actually changed from last fired value
		boolean shouldFireEvent = Float.isNaN(lastFiredValue) || Math.abs(finalValue - lastFiredValue) > 0.001f;

		if (shouldFireEvent) {
			lastFiredValue = finalValue;

			// ✅ Determine the value to return - step index or actual value
			float returnValue = finalValue;
			if (stepValues && snapToSteps) {
				returnValue = findNearestStepIndex(finalValue);
			}

			Drawable thumb = (thumbDrawable != null) ? thumbDrawable.get() : null;
			KrollDict offset = new KrollDict();
			offset.put(TiC.EVENT_PROPERTY_X, 0);
			offset.put(TiC.EVENT_PROPERTY_Y, 0);
			KrollDict size = new KrollDict();
			size.put(TiC.PROPERTY_WIDTH, 0);
			size.put(TiC.PROPERTY_HEIGHT, 0);
			if (thumb != null) {
				Rect thumbBounds = thumb.getBounds();
				if (thumbBounds != null) {
					offset.put(TiC.EVENT_PROPERTY_X, thumbBounds.left - seekBar.getThumbOffset());
					offset.put(TiC.EVENT_PROPERTY_Y, thumbBounds.top);
					size.put(TiC.PROPERTY_WIDTH, thumbBounds.width());
					size.put(TiC.PROPERTY_HEIGHT, thumbBounds.height());
				}
			}
			KrollDict data = new KrollDict();
			Log.d(TAG,
				  "Progress " + seekBar.getProgress() + " ScaleFactor " + scaleFactor + " Calculated Position " + pos
					  + " FinalValue " + finalValue + " ReturnValue " + returnValue + " Min " + min + " Max " + max
					  + " MinRange " + minRange + " MaxRange " + maxRange,
				  Log.DEBUG_MODE);
			data.put(TiC.PROPERTY_VALUE, returnValue);
			data.put(TiC.EVENT_PROPERTY_THUMB_OFFSET, offset);
			data.put(TiC.EVENT_PROPERTY_THUMB_SIZE, size);
			data.put("isTrusted", fromUser);
			proxy.setProperty(TiC.PROPERTY_VALUE, returnValue);

			fireEvent(TiC.EVENT_CHANGE, data);
		}
	}

	public void onStartTrackingTouch(SeekBar seekBar)
	{
		KrollDict data = new KrollDict();
		data.put(TiC.PROPERTY_VALUE, scaledValue());
		fireEvent(TiC.EVENT_START, data, false);
	}

	public void onStopTrackingTouch(SeekBar seekBar)
	{
		// Note: Continuous snapping now handles step adjustment during dragging
		// so no additional snapping is needed here

		KrollDict data = new KrollDict();
		data.put(TiC.PROPERTY_VALUE, scaledValue());
		fireEvent(TiC.EVENT_STOP, data, false);
	}

	protected void handleSetTintColor(int color)
	{
		SeekBar seekBar = (SeekBar) getNativeView();

		ColorStateList singleColorStateList = ColorStateList.valueOf(color);
		seekBar.setProgressTintList(singleColorStateList);
	}

	protected void handleSetTrackTintColor(int color)
	{
		SeekBar seekBar = (SeekBar) getNativeView();

		ColorStateList singleColorStateList = ColorStateList.valueOf(color);
		seekBar.setProgressBackgroundTintList(singleColorStateList);
	}

	private float scaledValue()
	{
		return pos + min;
	}

	private void setSteps(Object value)
	{
		if (value == null) {
			steps = null;
			snapToSteps = false;
			return;
		}

		if (value instanceof Object[]) {
			// Array of specific step values
			Object[] stepArray = (Object[]) value;
			steps = new float[stepArray.length];
			for (int i = 0; i < stepArray.length; i++) {
				steps[i] = TiConvert.toFloat(stepArray[i]);
			}
			snapToSteps = steps.length > 0;

			// Sort steps array to ensure proper ordering
			if (snapToSteps) {
				java.util.Arrays.sort(steps);
			}
		} else if (value instanceof Number) {
			// Count of equal steps to divide the range
			int stepCount = TiConvert.toInt(value);
			if (stepCount > 1) {
				steps = new float[stepCount];
				float range = max - min;
				for (int i = 0; i < stepCount; i++) {
					steps[i] = min + (range * i / (stepCount - 1));
				}
				snapToSteps = true;
			} else {
				Log.w(TAG, "Step count must be greater than 1");
				steps = null;
				snapToSteps = false;
			}
		} else {
			Log.w(TAG, "Steps property must be an array of numbers or a step count number");
			steps = null;
			snapToSteps = false;
		}
	}

	private float findNearestStep(float value)
	{
		if (!snapToSteps || steps == null || steps.length == 0) {
			return value;
		}

		float nearestStep = steps[0];
		float minDistance = Math.abs(value - nearestStep);

		for (float step : steps) {
			float distance = Math.abs(value - step);
			if (distance < minDistance) {
				minDistance = distance;
				nearestStep = step;
			}
		}

		return nearestStep;
	}

	private int findNearestStepIndex(float value)
	{
		if (!snapToSteps || steps == null || steps.length == 0) {
			return 0;
		}

		int nearestIndex = 0;
		float minDistance = Math.abs(value - steps[0]);

		for (int i = 1; i < steps.length; i++) {
			float distance = Math.abs(value - steps[i]);
			if (distance < minDistance) {
				minDistance = distance;
				nearestIndex = i;
			}
		}

		return nearestIndex;
	}
}
