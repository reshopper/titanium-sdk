/**
 * Titanium SDK
 * Copyright TiDev, Inc. 04/07/2022-Present
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */

package org.appcelerator.titanium.util;

import java.io.InputStream;
import java.util.Arrays;

import org.appcelerator.kroll.common.Log;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.RectF;
import androidx.exifinterface.media.ExifInterface;

/**
 * Utility class for image manipulations.
 */
public class TiImageHelper
{
	private static final String TAG = "TiImageHelper";

	/**
	 * Add an alpha channel to the given image if it does not already have one.
	 *
	 * @param image
	 *            the image to add an alpha channel to.
	 * @return a copy of the given image with an alpha channel. If the image already have the alpha channel, return the
	 *         image itself.
	 */
	public static Bitmap imageWithAlpha(Bitmap image)
	{
		if (image == null) {
			return null;
		}
		if (image.hasAlpha()) {
			return image;
		}
		return image.copy(Bitmap.Config.ARGB_8888, true);
	}

	/**
	 * Create a copy of the given image with rounded corners and a transparent border around its edges.
	 *
	 * @param image
	 *            the image to add rounded corners to.
	 * @param cornerRadius
	 *            the radius of the rounded corners.
	 * @param borderSize
	 *            the size of the border to be added.
	 * @return a copy of the given image with rounded corners and a transparent border. If the cornerRadius <= 0 or
	 *         borderSize < 0, return the image itself.
	 */
	public static Bitmap imageWithRoundedCorner(Bitmap image, float cornerRadius, float borderSize)
	{
		if (image == null) {
			return null;
		}
		if (cornerRadius <= 0 || borderSize < 0) {
			Log.w(TAG, "Unable to add rounded corners. Invalid corner radius or borderSize for imageWithRoundedCorner");
			return image;
		}

		int width = image.getWidth();
		int height = image.getHeight();
		Bitmap imageRoundedCorner = Bitmap.createBitmap(width + (int) (borderSize * 2), height + (int) (borderSize * 2),
														Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(imageRoundedCorner);

		Path clipPath = new Path();
		RectF imgRect = new RectF(borderSize, borderSize, width + borderSize, height + borderSize);

		float[] radii = new float[8];
		Arrays.fill(radii, cornerRadius);
		clipPath.addRoundRect(imgRect, radii, Direction.CW);

		// This still happens sometimes when hw accelerated so, catch and warn
		try {
			canvas.clipPath(clipPath);
		} catch (Exception e) {
			Log.e(TAG, "Unable to create the image with rounded corners. clipPath failed on canvas: " + e.getMessage());
			canvas.clipRect(imgRect);
		}

		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setFilterBitmap(true);
		paint.setDither(true);
		canvas.drawBitmap(imageWithAlpha(image), borderSize, borderSize, paint);
		return imageRoundedCorner;
	}

	/**
	 * Add a transparent border to the given image around its edges.
	 *
	 * @param image
	 *            the image to add a transparent border to.
	 * @param borderSize
	 *            the size of the border to be added.
	 * @return a copy of the given image with a transparent border. If the borderSize <= 0, return the image itself.
	 */
	public static Bitmap imageWithTransparentBorder(Bitmap image, int borderSize)
	{
		if (image == null) {
			return null;
		}
		if (borderSize <= 0) {
			Log.w(TAG, "Unable to add a transparent border. Invalid border size for imageWithTransparentBorder.");
			return image;
		}

		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setFilterBitmap(true);
		paint.setDither(true);

		int width = image.getWidth();
		int height = image.getHeight();
		Bitmap imageBorder =
			Bitmap.createBitmap(width + borderSize * 2, height + borderSize * 2, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(imageBorder);
		canvas.drawBitmap(imageWithAlpha(image), borderSize, borderSize, paint);
		return imageBorder;
	}

	/**
	 * Fetches the orientation of the given image in case it is not displayed upright.
	 * <p>
	 * This typically needs to be done with JPEG files whose EXIF information provides
	 * the photo's "orientation" (aka: rotation) relative to the camera's mounting orientation.
	 * @param path Path to an image file or URL.
	 * @return
	 * Returns the orientation of the image in degrees, counter-clockwise.
	 * <p>
	 * Will only return values 0, 90, 180, and 270.
	 * <p>
	 * A value of 0 indicates that the image is upright or if this method was unable to fetch
	 * orientation information from the image.
	 */
	public static int getOrientation(String path)
	{
		// Validate argument.
		if ((path == null) || path.isEmpty()) {
			String message = "Path of image file could not determined. "
							 + "Could not create an exifInterface from an invalid path.";
			Log.e(TAG, message);
			return 0;
		}

		// Attempt to fetch the EXIF orientation from the given file/url path.
		int orientation = 0;
		try (InputStream stream = TiFileHelper.getInstance().openInputStream(path, false)) {
			if (stream != null) {
				orientation = getOrientation(stream);
			}
		} catch (Exception ex) {
		}
		return orientation;
	}

	/**
	 * Fetches the orientation of the given image in case it is not displayed upright.
	 * <p>
	 * This typically needs to be done with JPEG files whose EXIF information provides
	 * the photo's "orientation" (aka: rotation) relative to the camera's upright orientation.
	 * @param stream
	 * An open input stream to an encoded image file, such as a JPEG.
	 * <p>
	 * This stream should not reference the raw decoded pixels of a bitmap since it would not
	 * contain any EXIF orientation metadata.
	 * <p>
	 * This method will not close the given stream. That is the caller's responsibility.
	 * @return
	 * Returns the orientation of the image in degrees, counter-clockwise.
	 * <p>
	 * Will only return values 0, 90, 180, and 270.
	 * <p>
	 * A value of 0 indicates that the image is upright or if this method was unable to fetch
	 * orientation information from the image.
	 */
	public static int getOrientation(InputStream stream)
	{
		int orientation = 0;
		try {
			if (stream != null) {
				ExifInterface exifInterface = new ExifInterface(stream);
				orientation = TiExifOrientation.from(exifInterface).getDegreesCounterClockwise();
			}
		} catch (Exception ex) {
			Log.e(TAG, "Unable to find orientation", ex);
		}
		return orientation;
	}

	/**
	 * Fetches the orientation of the given image in case it is not displayed upright.
	 * <p>
	 * This typically needs to be done with JPEG files whose EXIF information provides
	 * the photo's "orientation" (aka: rotation) relative to the camera's upright orientation.
	 * @param stream
	 * An open input stream to an encoded image file, such as a JPEG.
	 * <p>
	 * This stream should not reference the raw decoded pixels of a bitmap since it would not
	 * contain any EXIF orientation metadata.
	 * <p>
	 * This method will not close the given stream. That is the caller's responsibility.
	 * @return
	 * Returns an exif orientation object indicating if image needs to be rotated and/or mirrored.
	 * <p>
	 * returns null if given a null argument.
	 */
	public static TiExifOrientation getExifOrientation(InputStream stream)
	{
		if (stream == null) {
			return null;
		}

		TiExifOrientation exifOrientation = TiExifOrientation.UPRIGHT;
		try {
			ExifInterface exifInterface = new ExifInterface(stream);
			exifOrientation = TiExifOrientation.from(exifInterface);
		} catch (Exception ex) {
			Log.e(TAG, "Unable to fetch EXIF orientation", ex);
		}
		return exifOrientation;
	}

	/**
	 * Rotate the image
	 * @param bm source bitmap
	 * @param rotation degree of rotation
	 * @return return the rotated bitmap
	 */
	public static Bitmap rotateImage(Bitmap bm, int rotation)
	{
		Matrix matrix = new Matrix();
		matrix.postRotate(rotation);
		return Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
	}

	/**
	 * Copies EXIF metadata from source image to destination image file.
	 * <p>
	 * This preserves EXIF data such as camera settings, GPS coordinates, timestamps, etc.
	 * when images are resized or processed. Dimension-related EXIF tags are updated to match
	 * the new image dimensions.
	 * </p>
	 * @param sourceStream InputStream to the source image file containing EXIF data.
	 *                     This stream will not be closed by this method.
	 * @param destFilePath Path to the destination image file where EXIF data should be copied.
	 * @param newWidth New width of the destination image (for updating dimension tags).
	 * @param newHeight New height of the destination image (for updating dimension tags).
	 * @return Returns true if EXIF data was successfully copied, false otherwise.
	 */
	public static boolean copyExifData(InputStream sourceStream, String destFilePath, int newWidth, int newHeight)
	{
		if (sourceStream == null || destFilePath == null || destFilePath.isEmpty()) {
			return false;
		}

		try {
			// Read EXIF data from source image
			ExifInterface sourceExif = new ExifInterface(sourceStream);
			ExifInterface destExif = new ExifInterface(destFilePath);

			// Copy all EXIF attributes from source to destination
			// Note: We'll update dimension-related tags separately
			String[] exifTags = {
				ExifInterface.TAG_ARTIST,
				ExifInterface.TAG_BITS_PER_SAMPLE,
				ExifInterface.TAG_COMPRESSION,
				ExifInterface.TAG_COPYRIGHT,
				ExifInterface.TAG_DATETIME,
				ExifInterface.TAG_DATETIME_DIGITIZED,
				ExifInterface.TAG_DATETIME_ORIGINAL,
				ExifInterface.TAG_EXPOSURE_TIME,
				ExifInterface.TAG_FLASH,
				ExifInterface.TAG_FOCAL_LENGTH,
				ExifInterface.TAG_GPS_ALTITUDE,
				ExifInterface.TAG_GPS_ALTITUDE_REF,
				ExifInterface.TAG_GPS_AREA_INFORMATION,
				ExifInterface.TAG_GPS_DATESTAMP,
				ExifInterface.TAG_GPS_DEST_BEARING,
				ExifInterface.TAG_GPS_DEST_BEARING_REF,
				ExifInterface.TAG_GPS_DEST_DISTANCE,
				ExifInterface.TAG_GPS_DEST_DISTANCE_REF,
				ExifInterface.TAG_GPS_DEST_LATITUDE,
				ExifInterface.TAG_GPS_DEST_LATITUDE_REF,
				ExifInterface.TAG_GPS_DEST_LONGITUDE,
				ExifInterface.TAG_GPS_DEST_LONGITUDE_REF,
				ExifInterface.TAG_GPS_DIFFERENTIAL,
				ExifInterface.TAG_GPS_DOP,
				ExifInterface.TAG_GPS_IMG_DIRECTION,
				ExifInterface.TAG_GPS_IMG_DIRECTION_REF,
				ExifInterface.TAG_GPS_LATITUDE,
				ExifInterface.TAG_GPS_LATITUDE_REF,
				ExifInterface.TAG_GPS_LONGITUDE,
				ExifInterface.TAG_GPS_LONGITUDE_REF,
				ExifInterface.TAG_GPS_MAP_DATUM,
				ExifInterface.TAG_GPS_MEASURE_MODE,
				ExifInterface.TAG_GPS_PROCESSING_METHOD,
				ExifInterface.TAG_GPS_SATELLITES,
				ExifInterface.TAG_GPS_SPEED,
				ExifInterface.TAG_GPS_SPEED_REF,
				ExifInterface.TAG_GPS_STATUS,
				ExifInterface.TAG_GPS_TIMESTAMP,
				ExifInterface.TAG_GPS_TRACK,
				ExifInterface.TAG_GPS_TRACK_REF,
				ExifInterface.TAG_GPS_VERSION_ID,
				ExifInterface.TAG_IMAGE_DESCRIPTION,
				ExifInterface.TAG_IMAGE_LENGTH,
				ExifInterface.TAG_IMAGE_WIDTH,
				ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT,
				ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT_LENGTH,
				ExifInterface.TAG_MAKE,
				ExifInterface.TAG_MODEL,
				ExifInterface.TAG_ORIENTATION,
				ExifInterface.TAG_PHOTOMETRIC_INTERPRETATION,
				ExifInterface.TAG_PIXEL_X_DIMENSION,
				ExifInterface.TAG_PIXEL_Y_DIMENSION,
				ExifInterface.TAG_PLANAR_CONFIGURATION,
				ExifInterface.TAG_PRIMARY_CHROMATICITIES,
				ExifInterface.TAG_REFERENCE_BLACK_WHITE,
				ExifInterface.TAG_RESOLUTION_UNIT,
				ExifInterface.TAG_ROWS_PER_STRIP,
				ExifInterface.TAG_SAMPLES_PER_PIXEL,
				ExifInterface.TAG_SATURATION,
				ExifInterface.TAG_SCENE_CAPTURE_TYPE,
				ExifInterface.TAG_SCENE_TYPE,
				ExifInterface.TAG_SENSING_METHOD,
				ExifInterface.TAG_SHARPNESS,
				ExifInterface.TAG_SOFTWARE,
				ExifInterface.TAG_SPATIAL_FREQUENCY_RESPONSE,
				ExifInterface.TAG_SPECTRAL_SENSITIVITY,
				ExifInterface.TAG_STRIP_BYTE_COUNTS,
				ExifInterface.TAG_STRIP_OFFSETS,
				ExifInterface.TAG_SUBJECT_AREA,
				ExifInterface.TAG_SUBJECT_DISTANCE,
				ExifInterface.TAG_SUBJECT_DISTANCE_RANGE,
				ExifInterface.TAG_SUBJECT_LOCATION,
				ExifInterface.TAG_SUBSEC_TIME,
				ExifInterface.TAG_SUBSEC_TIME_DIGITIZED,
				ExifInterface.TAG_SUBSEC_TIME_ORIGINAL,
				ExifInterface.TAG_THUMBNAIL_IMAGE_LENGTH,
				ExifInterface.TAG_THUMBNAIL_IMAGE_WIDTH,
				ExifInterface.TAG_TRANSFER_FUNCTION,
				ExifInterface.TAG_USER_COMMENT,
				ExifInterface.TAG_WHITE_BALANCE,
				ExifInterface.TAG_WHITE_POINT,
				ExifInterface.TAG_X_RESOLUTION,
				ExifInterface.TAG_Y_CB_CR_COEFFICIENTS,
				ExifInterface.TAG_Y_CB_CR_POSITIONING,
				ExifInterface.TAG_Y_CB_CR_SUB_SAMPLING,
				ExifInterface.TAG_Y_RESOLUTION
			};

			// Copy each EXIF attribute from source to destination
			for (String tag : exifTags) {
				try {
					String value = sourceExif.getAttribute(tag);
					if (value != null) {
						destExif.setAttribute(tag, value);
					}
				} catch (Exception ex) {
					// Ignore tags that don't exist or can't be copied
					// This can happen if a tag constant exists but isn't supported
				}
			}

			// Update dimension-related tags to match new image size
			if (newWidth > 0 && newHeight > 0) {
				destExif.setAttribute(ExifInterface.TAG_IMAGE_WIDTH, String.valueOf(newWidth));
				destExif.setAttribute(ExifInterface.TAG_IMAGE_LENGTH, String.valueOf(newHeight));
				destExif.setAttribute(ExifInterface.TAG_PIXEL_X_DIMENSION, String.valueOf(newWidth));
				destExif.setAttribute(ExifInterface.TAG_PIXEL_Y_DIMENSION, String.valueOf(newHeight));
			}

			// Set orientation to normal (upright) since ImageDecoder/BitmapFactory
			// already applies rotation to the bitmap pixels
			destExif.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(ExifInterface.ORIENTATION_NORMAL));

			// Save the EXIF data to the destination file
			destExif.saveAttributes();
			return true;
		} catch (Exception ex) {
			Log.w(TAG, "Failed to copy EXIF data from source to destination: " + ex.getMessage());
			return false;
		}
	}
}
