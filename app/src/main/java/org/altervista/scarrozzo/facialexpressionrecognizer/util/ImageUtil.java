/*
 * 	 Copyright 2014 Sergio Carrozzo
 * 
 *   This file is part of Facial Expression Recognizer.
 * 
 *   Facial Expression Recognizer is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Facial Expression Recognizer is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with Facial Expression Recognizer.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.altervista.scarrozzo.facialexpressionrecognizer.util;

import java.util.ArrayList;

import com.googlecode.javacv.cpp.opencv_core.IplImage;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvResize;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_INTER_CUBIC;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

public class ImageUtil {

	public static Bitmap getScaledInstance(Bitmap img, int targetWidth,
			int targetHeight, boolean higherQuality) {
		
		Bitmap ret = (Bitmap) img;
		
		int w, h;
		if (higherQuality) {
			// Use multi-step technique: start with original size, then
			// scale down in multiple passes with drawImage()
			// until the target size is reached
			w = img.getWidth();
			h = img.getHeight();
		} else {
			// Use one-step technique: scale directly from original
			// size to target size with a single drawImage() call
			w = targetWidth;
			h = targetHeight;
		}

		int k = 0;
		do {
			if (higherQuality && w > targetWidth) {
				w /= 2;
				if (w < targetWidth) {
					w = targetWidth;
				}
			}

			if (higherQuality && h > targetHeight) {
				h /= 2;
				if (h < targetHeight) {
					h = targetHeight;
				}
			}

			if(k==10000)
			{
				w = targetWidth;
				h = targetHeight;
			}
			
			Bitmap tmp = Bitmap.createScaledBitmap(img, w, h, true);
			
			ret = tmp;
			
			k++;
		} while (w != targetWidth || h != targetHeight);

		return ret;
	}
	
	
	
	public static IplImage getScaledInstance(IplImage img, int targetWidth,
			int targetHeight, boolean higherQuality) {

		IplImage ret = img;
		int w, h;
		if (higherQuality) {
			// Use multi-step technique: start with original size, then
			// scale down in multiple passes with drawImage()
			// until the target size is reached
			w = img.width();
			h = img.height();
		} else {
			// Use one-step technique: scale directly from original
			// size to target size with a single drawImage() call
			w = targetWidth;
			h = targetHeight;
		}

		int k = 0;
		do {
			if (higherQuality && w > targetWidth) {
				w /= 2;
				if (w < targetWidth) {
					w = targetWidth;
				}
			}

			if (higherQuality && h > targetHeight) {
				h /= 2;
				if (h < targetHeight) {
					h = targetHeight;
				}
			}

			if(k==10000)
			{
				w = targetWidth;
				h = targetHeight;
			}
			
			IplImage tmp = IplImage.create(w, h, img.depth(), img.nChannels());
			cvResize(ret, tmp, CV_INTER_CUBIC);
			ret = tmp;
			
			k++;
		} while (w != targetWidth || h != targetHeight);

		return ret;
	}
	
	
	
	
	public static Bitmap toGrayscale(Bitmap bmpOriginal)
	{        
	    int width, height;
	    height = bmpOriginal.getHeight();
	    width = bmpOriginal.getWidth();    

	    Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
	    Canvas c = new Canvas(bmpGrayscale);
	    Paint paint = new Paint();
	    ColorMatrix cm = new ColorMatrix();
	    cm.setSaturation(0);
	    ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
	    paint.setColorFilter(f);
	    c.drawBitmap(bmpOriginal, 0, 0, paint);
	    return bmpGrayscale;
	}
	
	
	// Cobine Multi Image Into One
	public static Bitmap combineImageIntoOne(ArrayList<Bitmap> bitmapArray, int numPatchs) {
	                int w = 0, h = 0;
	                w = bitmapArray.get(0).getWidth()*(bitmapArray.size()/numPatchs);
	                h = bitmapArray.get(0).getHeight()*(bitmapArray.size()/numPatchs);

	                Bitmap temp = Bitmap.createBitmap(w, h, bitmapArray.get(0).getConfig());
	                Canvas canvas = new Canvas(temp);
	                int top = 0;
	                int left = 0;
	                for (int i = 0; i < bitmapArray.size(); i++) {
	                	if (left == w) {
	    					left = 0;
	    					top += bitmapArray.get(i).getHeight();
	    				}
	                	
	                        canvas.drawBitmap(bitmapArray.get(i), left, top, null);
	                        
	                        left += bitmapArray.get(i).getWidth();
	                }
	                return temp;
	        }
	
	
	public static Bitmap drawTextToBitmap(Bitmap bitmap, String gText, int color, Context gContext) {
		Resources resources = gContext.getResources();
	    float scale = resources.getDisplayMetrics().density;
		
		android.graphics.Bitmap.Config bitmapConfig = bitmap.getConfig();
		// set default bitmap config if none
		if (bitmapConfig == null) {
			bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
		}
		// resource bitmaps are imutable,
		// so we need to convert it to mutable one
		bitmap = bitmap.copy(bitmapConfig, true);

		Canvas canvas = new Canvas(bitmap);
		// new antialised Paint
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		// text color - #3D3D3D
		paint.setColor(color);
		// text size in pixels
		int size = determineMaxTextSize(gText, bitmap.getWidth(), bitmap.getHeight());
		paint.setTextSize(size);
		// text shadow

		// draw text to the Canvas center
		Rect bounds = new Rect();
		paint.getTextBounds(gText, 0, gText.length(), bounds);
		int x = (int)Math.round(bitmap.getWidth()*0.05);
		int y = (int)Math.round(bitmap.getHeight()*0.06);

		for(String line: gText.split("\n")){
		      canvas.drawText(line, x, y, paint);
		      y+=paint.descent()-paint.ascent();
		}

		return bitmap;
	}
	
	
	private static int determineMaxTextSize(String str, float maxWidth, float maxHeight)
	{
	    int size = 0;       
	    Paint paint = new Paint();

	    do {
	        paint.setTextSize(++size);
	    } while(paint.measureText(str) < maxWidth && paint.measureText(str) < maxHeight);

	    return size;
	}
	
	private static void setTextSizeForWidth(Paint paint, float desiredWidth,
	        String text) {

	    // Pick a reasonably large value for the test. Larger values produce
	    // more accurate results, but may cause problems with hardware
	    // acceleration. But there are workarounds for that, too; refer to
	    // http://stackoverflow.com/questions/6253528/font-size-too-large-to-fit-in-cache
	    final float testTextSize = 48f;

	    // Get the bounds of the text, using our testTextSize.
	    paint.setTextSize(testTextSize);
	    Rect bounds = new Rect();
	    paint.getTextBounds(text, 0, text.length(), bounds);

	    // Calculate the desired size as a proportion of our testTextSize.
	    float desiredTextSize = testTextSize * desiredWidth / bounds.width();

	    // Set the paint for that size.
	    paint.setTextSize(desiredTextSize);
	}
}
