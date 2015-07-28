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

import java.util.Vector;

import org.altervista.scarrozzo.facialexpressionrecognizer.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

public class FaceAdapter extends BaseAdapter {

	int defaultItemBackground;
	         
	private Context galleryContext;
	 
	private Vector<Bitmap> imageBitmaps;

	
	public FaceAdapter(Context c) {
		 
	    galleryContext = c;

	    imageBitmaps  = new Vector<Bitmap>();
	    
	    TypedArray styleAttrs = galleryContext.obtainStyledAttributes(R.styleable.faceGallery);
	     
	    defaultItemBackground = styleAttrs.getResourceId(
	    		R.styleable.faceGallery_android_galleryItemBackground, 0);
	    styleAttrs.recycle();
	}
	
	
	@Override
	public int getCount() {
		return imageBitmaps.size();
	}

	@Override
	public Object getItem(int arg0) {
	    return arg0;
	}

	@Override
	public long getItemId(int arg0) {
	    return arg0;
	}

	@SuppressWarnings("deprecation")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
	 
	    ImageView imageView = new ImageView(galleryContext);
	    imageView.setImageBitmap(imageBitmaps.get(position));
	    imageView.setLayoutParams(new Gallery.LayoutParams(300, 200));
	    imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
	    imageView.setBackgroundResource(defaultItemBackground);
	    return imageView;
	}
	
	//helper method to add a bitmap to the gallery when the user chooses one
	public void addPic(Bitmap newPic, int position)
	{
	    //set at currently selected index
		imageBitmaps.add(position, newPic);
	}
	
	//return bitmap at specified position for larger display
	public Bitmap getPic(int posn)
	{
		return imageBitmaps.get(posn);
	}
	 
}
