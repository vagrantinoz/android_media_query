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
import java.util.Random;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.ObjectOutputStream;

import android.app.Activity;
import android.net.Uri;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
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
	public KrollDict queryPhotos(Integer offset, Integer limit)
	{
		Log.d(TAG, "queryPhotos called: ");

		//
		if (offset == null) offset = 0;
		if (limit == null) limit = 100;
		
		String where = MediaStore.Images.Media.SIZE + " > 0"; //  The size of the file in bytes가 0 이상인 경우만 query
		String orderBy = MediaStore.Images.Media.DATE_TAKEN + " DESC LIMIT " + limit + " OFFSET " + offset;
		
		String[] projection = new String[] {
			MediaStore.Images.Media._ID,
			MediaStore.Images.Media.DATA,
			MediaStore.Images.Media.DATE_TAKEN,
			MediaStore.Images.Media.LATITUDE,
			MediaStore.Images.Media.LONGITUDE,
			MediaStore.Images.Media.SIZE,
			MediaStore.Images.Media.DATE_ADDED,
		};
		
		String[] projection2 = {
			MediaStore.Images.Thumbnails.DATA,
			MediaStore.Images.Thumbnails.IMAGE_ID,
			MediaStore.Images.Thumbnails.HEIGHT,
			MediaStore.Images.Thumbnails.WIDTH,
			MediaStore.Images.Thumbnails.KIND,
		};
        
		
		Log.d(TAG, orderBy);
		
        // make managedQuery:
		Activity activity = this.getActivity();
		Cursor c = activity.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, where, null, orderBy);
		
		Log.d(TAG, "Media.images query result count = " + c.getCount());
        
		
		// formatting result
		KrollDict result = new KrollDict(c.getCount());
		HashMap<String, String> obj = new HashMap<String, String>();
        
		if (c.getCount() > 0) {
    		c.moveToFirst();
            
			for (Integer i=0; !c.isAfterLast(); i++) {
				
				String _id = c.getString(c.getColumnIndex(MediaStore.Images.Media._ID));
				String path = c.getString(c.getColumnIndex(MediaStore.Images.Media.DATA));
				
				// id, path, date_taken
				obj.put("id", _id);
				obj.put("path", path);
				obj.put("size", c.getString(c.getColumnIndex(MediaStore.Images.Media.SIZE)));
				obj.put("dateTaken", c.getString(c.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN)));
				// gps info
				obj.put("lat", Float.toString(c.getFloat(c.getColumnIndex(MediaStore.Images.Media.LATITUDE))));
				obj.put("lon", Float.toString(c.getFloat(c.getColumnIndex(MediaStore.Images.Media.LONGITUDE))));
				
				// query thumbnail
				Cursor cursor = activity.getContentResolver().query(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI,
					projection2,
					MediaStore.Images.Thumbnails.IMAGE_ID + " = " + _id,
					null,
					null
				);
				cursor.moveToFirst();
				
				if (cursor.getCount() > 0) {
					obj.put("thumbnail", cursor.getString(cursor.getColumnIndex(MediaStore.Images.Thumbnails.DATA)));
					obj.put("thumbnail_width", cursor.getString(cursor.getColumnIndex(MediaStore.Images.Thumbnails.WIDTH)));
					obj.put("thumbnail_height", cursor.getString(cursor.getColumnIndex(MediaStore.Images.Thumbnails.HEIGHT)));
				}
				else {
					Log.d(TAG, "get Thumbnail - ERROR");
					Log.d(TAG, "(" + _id + ") thumbnail not exist");
				}
				
				cursor.close();
				
				// exif
				try {
					ExifInterface exif = new ExifInterface(path);
					// width, height
					obj.put("width", exif.getAttribute("ImageWidth"));
					obj.put("height", exif.getAttribute("ImageLength"));
					// gps processing method
					obj.put("gpsMethod", exif.getAttribute("GPSProcessingMethod"));
					// gps timestamp
					obj.put("gpsDate", exif.getAttribute("GPSDateStamp"));
					obj.put("gpsTime", exif.getAttribute("GPSTimeStamp"));
					// gps location
					float[] latlong = new float [] { 0.0f, 0.0f };
					exif.getLatLong(latlong);
					obj.put("exif_lat", Float.toString(latlong[0]));
					obj.put("exif_lon", Float.toString(latlong[1]));
					// orientation
					int orientation = Integer.parseInt(exif.getAttribute("Orientation"));
					obj.put("orientation", Integer.toString(orientation));
					obj.put("rotate", (orientation == 6 || orientation == 8) ? "1" : "0");
				}
				catch (Exception e) {
					Log.d(TAG, "Exif - ERROR");
					Log.d(TAG, e.getMessage());
				}
                
				result.put(_id, new KrollDict(obj)); //add the item

				c.moveToNext();
			}
		}
        
		c.close();
		
		
		return result;
	}
	
	
	// @Kroll.method
	// public TiBlob getThumbnail(Integer id, String fileName)
	// {
	// 	int orientation = 0;
	// 	Matrix rotateMatrix = new Matrix();
	// 	float degreeToRotate = 0.0f;
	// 	
	// 	try {
	// 		ExifInterface exif = new ExifInterface(fileName);
	// 		orientation = Integer.parseInt(exif.getAttribute("Orientation"));
	// 
	// 		if (orientation == 6) { // 90 degree
	// 			degreeToRotate = 90.0f;
	// 		}
	// 		else if (orientation == 3) { // 180 degree
	// 			degreeToRotate = 180.0f;
	// 		}
	// 		else if (orientation == 8) { // 270 degree
	// 			degreeToRotate = 270.0f;
	// 		}
	// 		
	// 		rotateMatrix.postRotate(degreeToRotate);
	// 	}
	// 	catch(Exception e) {
	// 		Log.d(TAG, "Exif - ERROR");
	// 		Log.d(TAG, e.getMessage());
	// 	}
	// 	
	// 	// create thumbnail
	// 	Activity activity = this.getActivity();
	// 	Bitmap th = MediaStore.Images.Thumbnails.getThumbnail(activity.getContentResolver(), id.intValue(), MediaStore.Images.Thumbnails.MICRO_KIND, null);
	// 	if (th != null) {
	// 		Bitmap rotated = Bitmap.createBitmap(th, 0, 0, th.getWidth(), th.getHeight(), rotateMatrix, false);
	// 		
	// 		TiBlob blob = TiBlob.blobFromImage(rotated);
	// 		th.recycle();
	// 		
	// 		return blob;
	// 	}
	// 	
	// 	Log.d(TAG, "MediaStore.Images.Thumbnails.getThumbnail() returns null:");
	// 	return null;
	// }
	
	// public static int calculateInSampleSize(int width, int reqWidth) {
	// 	int inSampleSize = 1;
	// 	
	// 	if (width > reqWidth) {
	// 
	// 		final int widthRatio = Math.round((float) width / (float) reqWidth);
	// 		inSampleSize = widthRatio;//heightRatio < widthRatio ? heightRatio : widthRatio;
	// 	}
	// 			
	// 	return inSampleSize;
	// }
	// 
	// public Bitmap decodeSampledBitmapFromResourceMemOpt(File f, int reqWidth) {
	// 	try {
	// 		final BitmapFactory.Options options = new BitmapFactory.Options();
	// 		options.inJustDecodeBounds = true;
	// 		BitmapFactory.decodeStream(new FileInputStream(f), null, options);
	// 
	// 		options.inSampleSize = calculateInSampleSize(options.outWidth, reqWidth); // width만 사용한다.
	// 		options.inPurgeable = true;
	// 		options.inInputShareable = true;
	// 		options.inJustDecodeBounds = false;
	// 		options.inPreferredConfig = Bitmap.Config.RGB_565; // ARGB_8888
	// 		return BitmapFactory.decodeStream(new FileInputStream(f), null, options);
	// 
	// 	} catch (Exception e) {
	// 		e.printStackTrace();
	// 		return null;
	// 	}
	// }
	// 			
	// @Kroll.method
	// public String createResizedImage(String fileName, Integer reqWidth)
	// {	
	// 	
	// 	if (reqWidth == null || reqWidth == 0) {
	// 		reqWidth = 640;
	// 	}
	// 	
	// 	fileName = fileName.replaceFirst("file://", "");
	// 	
	// 	int orientation = 0;
	// 	
	// 	try {
	// 		ExifInterface exif = new ExifInterface(fileName);
	// 		orientation = Integer.parseInt(exif.getAttribute("Orientation"));
	// 	} catch(Exception e) {
	// 		Log.d(TAG, "ExifInterface - ERROR");
	// 		Log.d(TAG, e.getMessage());
	// 	}
	// 	
	// 	//
	// 	Bitmap th = null;
	// 	FileInputStream inputStream;
	// 	File file = new File(fileName);
	// 	
	// 	try {
	// 		th = decodeSampledBitmapFromResourceMemOpt(file, reqWidth);
	// 	}
	// 	catch (Exception e) {
	// 		Log.d(TAG, "File Not Found");
	// 		e.printStackTrace();
	// 		return null;
	// 	}
	// 	
	// 	// Orientation에 따라 사진 회전
	// 	Matrix rotateMatrix = new Matrix();
	// 	float degreeToRotate = 0.0f;
	// 
	// 	if (orientation == 6) { // 90 degree
	// 		degreeToRotate = 90.0f;
	// 	}
	// 	else if (orientation == 3) { // 180 degree
	// 		degreeToRotate = 180.0f;
	// 	}
	// 	else if (orientation == 8) { // 270 degree
	// 		degreeToRotate = 270.0f;
	// 	}
	// 	
	// 	rotateMatrix.postRotate(degreeToRotate);
	// 	
	// 	Bitmap rotated = Bitmap.createBitmap(th, 0, 0, th.getWidth(), th.getHeight(), rotateMatrix, true);
	// 
	// 	try {
	// 		// mimetype을 지정하기 위함
	// 		String cacheDir = this.getActivity().getCacheDir().getPath();
	// 		Random random = new Random();
	// 		int i = random.nextInt(9999);
	// 
	// 		// File에다 저장하고 FilePath를 리턴한다. 메모리이슈때문에.
	// 		String ouputFileName = cacheDir + "_" + i + ".jpg";
	// 		FileOutputStream stream = new FileOutputStream(ouputFileName);
	// 		boolean compressed = rotated.compress(Bitmap.CompressFormat.JPEG, 100, stream);
	// 		stream.flush();
	// 		stream.close();
	// 		stream = null;
	// 		
	// 		if (compressed) {
	// 			th.recycle();
	// 			th = null;
	// 			rotated.recycle();
	// 			rotated = null;
	// 			
	// 			return ouputFileName;
	// 		}
	// 	}
	// 	catch(Exception e) {
	// 		Log.d(TAG, "ByteArrayOutputStream - ERROR");
	// 		Log.d(TAG, e.getMessage());
	// 	}
	// 	
	// 	return null;
	// }
	// 
	// 
	// @Kroll.method
	// public TiBlob replaceMimeType(Object blob) {
	// 	
	// 	byte[] byteArray;
	// 	Bitmap image;
	// 	ByteArrayOutputStream outputStream = outputStream = new ByteArrayOutputStream();
	// 	
	// 	if (blob instanceof TiBlob) {
	// 		
	// 		TiBlob temp = (TiBlob) blob;
	// 		
	// 		InputStream inputStream = temp.getInputStream();
	// 		image = BitmapFactory.decodeStream(inputStream);
	// 		image.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
	// 		
	// 		byteArray = outputStream.toByteArray();
	// 		
	// 		try{
	// 			outputStream.close();
	// 			outputStream = null;
	// 
	// 			inputStream.close();
	// 			inputStream = null;
	// 			
	// 		} catch(Exception e) {
	// 			Log.d(TAG, "Stream Close - ERROR");
	// 			Log.d(TAG, e.getMessage());
	// 
	// 			outputStream = null;
	// 			inputStream = null;
	// 		}
	// 		
	// 	}
	// 	else {
	// 		
	// 		Log.d(TAG, "not Matched type - ERROR");
	// 		
	// 		return null;
	// 	}
	// 	
	// 	image.recycle();
	// 	image = null;
	// 	
	// 	return TiBlob.blobFromData(byteArray, "image/jpeg");
	// }
	
}

