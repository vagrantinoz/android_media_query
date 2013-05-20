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

        String where;
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
		
		public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth) {
		    // Raw height and width of image
		    final int height = options.outHeight;
		    final int width = options.outWidth;
		    int inSampleSize = 1;
				
				
		    if (width > reqWidth) {

		        // Calculate ratios of height and width to requested height and width
		        // final int heightRatio = Math.round((float) height / (float) reqHeight);
		        final int widthRatio = Math.round((float) width / (float) reqWidth);

		        // Choose the smallest ratio as inSampleSize value, this will guarantee
		        // a final image with both dimensions larger than or equal to the
		        // requested height and width.
		        inSampleSize = widthRatio;//heightRatio < widthRatio ? heightRatio : widthRatio;
		    }
				
		    return inSampleSize;
		}
		
    @Kroll.method
    public TiBlob getThumbnail(Integer id, String fileName)
    {
				// fileName = fileName.replaceFirst("file://", "");
			
        try {
            ExifInterface exif = new ExifInterface(fileName);
            
            if (exif.hasThumbnail()) {
                byte[] thumbnail = exif.getThumbnail();
                Log.d(TAG, "thumbnail's (EXIF) length = " + thumbnail.length);

								return TiBlob.blobFromData(thumbnail, "image/jpeg"); // http://developer.android.com/reference/android/media/ExifInterface.html
                // return TiBlob.blobFromImage(BitmapFactory.decodeByteArray(thumbnail, 0, thumbnail.length));
            }

            Log.d(TAG, "no exif-thumbnail()");
        }
        catch(Exception e) {
            Log.d(TAG, "Exif - ERROR");
            Log.d(TAG, e.getMessage());
        }

        // create thumbnail
        Log.d(TAG, fileName);

		// Activity activity = this.getActivity();
        // Bitmap th = MediaStore.Images.Thumbnails.getThumbnail(activity.getContentResolver(), id.intValue(), MediaStore.Images.Thumbnails.MICRO_KIND, null);
        
				final BitmapFactory.Options bmOptions = new BitmapFactory.Options();
				BitmapFactory.decodeFile(fileName, bmOptions);
				
				final int height = bmOptions.outHeight;
		    final int width = bmOptions.outWidth;
				
				bmOptions.inSampleSize = calculateInSampleSize(bmOptions, 640);
				
				Bitmap th;
				if (width > 640) {
					Bitmap temp = BitmapFactory.decodeFile(fileName, bmOptions);
					th = Bitmap.createScaledBitmap(temp, 640, Math.round((float) height * (float) 640 / (float) width), false);
					
					temp.recycle();
					temp = null;
				}
				else {
					th = BitmapFactory.decodeFile(fileName, bmOptions);
				}
				
				if (th != null) {
						
						try {
		            ByteArrayOutputStream stream = new ByteArrayOutputStream();

								th.compress(Bitmap.CompressFormat.JPEG, 100, stream);
								th.recycle();

								byte[] byteArray = stream.toByteArray();
								stream.close();
								stream = null;

		            TiBlob blob = TiBlob.blobFromData(byteArray, "image/jpeg");

		            return blob;
		        }
		        catch(Exception e) {
		            Log.d(TAG, "ByteArrayOutputStream - ERROR");
		            Log.d(TAG, e.getMessage());
		        }
        }
        
        Log.d(TAG, "MediaStore.Images.Thumbnails.getThumbnail() returns null:");
        return null;
    }
}

