/**
 * This file was auto-generated by the Titanium Module SDK helper for Android
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2013 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 *
 */
package com.oxgcp.androidmediaquery;

import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.KrollDict;

import org.appcelerator.titanium.TiApplication;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiBlob;

import java.util.HashMap;
import java.util.ArrayList;
import java.io.ByteArrayOutputStream;

import android.app.Activity;
import android.net.Uri;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.media.ExifInterface;
import android.graphics.Matrix;



@Kroll.module(name="Androidmediaquery", id="com.oxgcp.androidmediaquery")
public class AndroidmediaqueryModule extends KrollModule
{

	// Standard Debugging variables
	private static final String TAG = "AndroidmediaqueryModule";

	// You can define constants with @Kroll.constant, for example:
	// @Kroll.constant public static final String EXTERNAL_NAME = value;
	
	public AndroidmediaqueryModule()
	{
		super();
	}

	@Kroll.onAppCreate
	public static void onAppCreate(TiApplication app)
	{
		Log.d(TAG, "inside onAppCreate");
		// put module init code that needs to run when the application is created
	}

	// Methods
	@Kroll.method
	public KrollDict queryPhotos(String mode, Integer id, Integer limit)
	{
		Log.d(TAG, "queryPhotos called: " + mode);

        // query condition
		Uri externalPhotosUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

		if (id == null) id = 0;
		String[] whereParams = { id.toString() };

		String where = "";
		if (mode.equals("match")) {
			Log.d(TAG, "where: match = ");
		 	where = MediaStore.Images.Media._ID + " = ?";
		}
		else if (mode.equals("less_than")) {
			Log.d(TAG, "where: less_than < ");
			where = MediaStore.Images.Media._ID + " < ?";
		}
		else {
			Log.d(TAG, "where(default): greater_than > ");
			where = MediaStore.Images.Media._ID + " > ?";
		}
		
		where += " AND " + MediaStore.Images.Media.SIZE + " > 0"; //  The size of the file in bytes가 0 이상인 경우만 query
   
		String orderBy;
		if (limit == null) {
			orderBy = MediaStore.Images.Media._ID + " DESC";
		} else {
			orderBy = MediaStore.Images.Media._ID + " DESC LIMIT " + limit;
		}

		String[] projection = new String[] {
			MediaStore.Images.Media._ID,
			MediaStore.Images.Media.DATA,
			MediaStore.Images.Media.DATE_TAKEN,
			MediaStore.Images.Media.LATITUDE,
			MediaStore.Images.Media.LONGITUDE,
			MediaStore.Images.Media.SIZE,
			// MediaStore.Images.Media.MIME_TYPE,
			// "width", // MediaStore.Images.Media.WIDTH나 HEIGHT를 못찾음
			// "height",
		};
        
        // make managedQuery:
		Activity activity = this.getActivity();
		Cursor c = MediaStore.Images.Media.query(activity.getContentResolver(), externalPhotosUri, projection, where, whereParams, orderBy);
		
		Log.d(TAG, "Media.images query result count = " + c.getCount());
        
		// formatting result
		KrollDict result = new KrollDict(c.getCount());
		HashMap<String, String> obj = new HashMap<String, String>();
        
		if (c.getCount() > 0) {
    		c.moveToFirst();
            
			for (Integer i=0; !c.isAfterLast(); i++) {
				
				String path = c.getString(c.getColumnIndex(MediaStore.Images.Media.DATA));
				// id, path, date_taken
				obj.put("id", c.getString(c.getColumnIndex(MediaStore.Images.Media._ID)));
				obj.put("path", path);
				obj.put("dateTaken", c.getString(c.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN)));
				// gps info
				obj.put("lat", Float.toString(c.getFloat(c.getColumnIndex(MediaStore.Images.Media.LATITUDE))));
				obj.put("lon", Float.toString(c.getFloat(c.getColumnIndex(MediaStore.Images.Media.LONGITUDE))));
				
				try {
					ExifInterface exif = new ExifInterface(path);
					// width, height
					// String width = c.getInt(c.getColumnIndex("width")) > 0 ? "" + c.getInt(c.getColumnIndex("width")) :  exif.getAttribute("ImageWidth"); // media query 에서 가져오지 못했다고 판단될 경우
					// String height = c.getInt(c.getColumnIndex("height")) > 0 ? "" + c.getInt(c.getColumnIndex("height")) :  exif.getAttribute("ImageLength"); // exif 에서 가져옴
					
					obj.put("width", exif.getAttribute("ImageWidth"));
					obj.put("height", exif.getAttribute("ImageLength"));
					// gps processing method
					obj.put("gpsMethod", exif.getAttribute("GPSProcessingMethod"));
					// gps timestamp
					obj.put("gpsTime", exif.getAttribute("GPSDateStamp"));
					obj.put("gpsDate", exif.getAttribute("GPSTimeStamp"));
					// gps location
					float[] latlong = new float [] { 0.0f, 0.0f };
					exif.getLatLong(latlong);
					obj.put("exif_lat", Float.toString(latlong[0]));
					obj.put("exif_lon", Float.toString(latlong[1]));
				}
				catch (Exception e) {
					Log.d(TAG, "Exif - ERROR");
					Log.d(TAG, e.getMessage());
				}
                
				result.put(i.toString(), new KrollDict(obj)); //add the item

				c.moveToNext();
			}
		}
        
		c.close();
        
		return result;
	}
	
	
	@Kroll.method
	public TiBlob getThumbnail(Integer id, String fileName)
	{
		
		int orientation = 0;
		Matrix rotateMatrix = new Matrix();
		float degreeToRotate = 0.0f;
		
		try {
			ExifInterface exif = new ExifInterface(fileName);
			orientation = Integer.parseInt(exif.getAttribute("Orientation"));
			
			if (orientation == 6) { // 90 degree
				degreeToRotate = 90.0f;
			}
			else if (orientation == 3) { // 180 degree
				degreeToRotate = 180.0f;
			}
			else if (orientation == 8) { // 270 degree
				degreeToRotate = 270.0f;
			}

			rotateMatrix.postRotate(degreeToRotate);
			
			
			if (exif.hasThumbnail()) {
				byte[] thumbnail = exif.getThumbnail();
				Log.d(TAG, "thumbnail's (EXIF) length = " + thumbnail.length);
				
				Bitmap th = BitmapFactory.decodeByteArray(thumbnail, 0, thumbnail.length);
				th = Bitmap.createBitmap(th, 0, 0, th.getWidth(), th.getHeight(), rotateMatrix, false);
				
				TiBlob blob = TiBlob.blobFromImage(th);
				th.recycle();
				
				return blob;
			}

			Log.d(TAG, "no exif-thumbnail()");
		}
		catch(Exception e) {
			Log.d(TAG, "Exif - ERROR");
			Log.d(TAG, e.getMessage());
		}

 		// create thumbnail
		Log.d(TAG, fileName);

		Activity activity = this.getActivity();
		Bitmap th = MediaStore.Images.Thumbnails.getThumbnail(activity.getContentResolver(), id.intValue(), MediaStore.Images.Thumbnails.MICRO_KIND, null);
      
		
		if (th != null) {
				
			try {
				
				th = Bitmap.createBitmap(th, 0, 0, th.getWidth(), th.getHeight(), rotateMatrix, false);
				
				TiBlob blob = TiBlob.blobFromImage(th);
				th.recycle();

				return blob;
			}
			catch(Exception e) {
				Log.d(TAG, "ByteArrayOutputStream - ERROR");
				Log.d(TAG, e.getMessage());
				return null;
			}
		}
      
		Log.d(TAG, "MediaStore.Images.Thumbnails.getThumbnail() returns null:");
		return null;
	}
		
	public static int calculateInSampleSize(int width, int reqWidth) {
		int inSampleSize = 1;
		
		if (width > reqWidth) {

			final int widthRatio = Math.round((float) width / (float) reqWidth);
			inSampleSize = widthRatio;//heightRatio < widthRatio ? heightRatio : widthRatio;
		}
				
		return inSampleSize;
	}
	
	@Kroll.method
	public TiBlob createResizedImage(String fileName)
	{	
		
		fileName = fileName.replaceFirst("file://", "");
		
		int orientation = 0;
		
		try {
			ExifInterface exif = new ExifInterface(fileName);
			orientation = Integer.parseInt(exif.getAttribute("Orientation"));
		} catch(Exception e) {
			Log.d(TAG, "ExifInterface - ERROR");
			Log.d(TAG, e.getMessage());
		}
		
		BitmapFactory.Options opts = new BitmapFactory.Options(); // 실제 적용을 위한 bitmap option
		final BitmapFactory.Options bmOptions = new BitmapFactory.Options(); // image size를 알기 위한 bitmap option
		bmOptions.inJustDecodeBounds = true;
		
		// if (width == 0 || height == 0){
		Bitmap justBound = BitmapFactory.decodeFile(fileName, bmOptions);
		int height = bmOptions.outHeight;
		int width = bmOptions.outWidth;
		justBound.recycle();
		
		// init
		Bitmap th = null;
		Bitmap temp = null;
		
		
		// resize
		if (orientation == 6 || orientation == 8) { // 90도나 270도로 돌아가있는 경우
			opts.inSampleSize = calculateInSampleSize(height, 640);
			
			if (width > 640) {
				temp = BitmapFactory.decodeFile(fileName, opts);
				th = Bitmap.createScaledBitmap(temp, Math.round((float) width * (float) 640 / (float) height), 640, false);
			}
			else {
				th = BitmapFactory.decodeFile(fileName, opts);
			}
			
		}
		else {
			opts.inSampleSize = calculateInSampleSize(width, 640);
			
			if (width > 640) {
				temp = BitmapFactory.decodeFile(fileName, opts);
				th = Bitmap.createScaledBitmap(temp, 640, Math.round((float) height * (float) 640 / (float) width), false);
			}
			else {
				th = BitmapFactory.decodeFile(fileName, opts);
			}
		}
		
		// return
		if (th != null) {
			try {
				
				// Orientation에 따라 사진 회전

				Matrix rotateMatrix = new Matrix();
				float degreeToRotate = 0.0f;

				if (orientation == 6) { // 90 degree
					degreeToRotate = 90.0f;
				}
				else if (orientation == 3) { // 180 degree
					degreeToRotate = 180.0f;
				}
				else if (orientation == 8) { // 270 degree
					degreeToRotate = 270.0f;
				}

				rotateMatrix.postRotate(degreeToRotate);
				
				Bitmap rotated = Bitmap.createBitmap(th, 0, 0, th.getWidth(), th.getHeight(), rotateMatrix, false);
				
				// mimetype을 지정하기 위함
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				rotated.compress(Bitmap.CompressFormat.JPEG, 100, stream);

				th.recycle();
				rotated.recycle();
				if (temp != null) temp.recycle();
				
				
				byte[] byteArray = stream.toByteArray();
				stream.close();
				stream = null;
		
				TiBlob blob = TiBlob.blobFromData(byteArray, "image/jpeg");
		
				return blob;
			}
			catch(Exception e) {
				Log.d(TAG, "ByteArrayOutputStream - ERROR");
				Log.d(TAG, e.getMessage());
		
				return null;
			}
		}
		
		return null;
	}
}

