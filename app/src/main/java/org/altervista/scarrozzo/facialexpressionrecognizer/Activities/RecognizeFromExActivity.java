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

package org.altervista.scarrozzo.facialexpressionrecognizer.Activities;

import java.io.ByteArrayOutputStream;

import org.altervista.scarrozzo.facialexpressionrecognizer.R;
import org.altervista.scarrozzo.facialexpressionrecognizer.Model.ExpressionContentProvider;
import org.altervista.scarrozzo.facialexpressionrecognizer.recognitionAlgorithms.Recognizer;
import org.altervista.scarrozzo.facialexpressionrecognizer.util.FaceAdapter;

import uk.co.senab.photoview.PhotoViewAttacher;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.Toast;


public class RecognizeFromExActivity extends ActionBarActivity {

	private static final String TAG = "RecognizeFromExActivity";
	private static final int SELECT_PICTURE_ACTIVITY_REQUEST_CODE = 0;
	
	// gallery view adapter
	private FaceAdapter imgAdapt;

	//variable to store the currently selected image
	private int currentPic = 0;
	
	//gallery object
	@SuppressWarnings("deprecation")
	private Gallery faceGallery;
	
	//image view for larger display
	private ImageView faceView;
	
	private Recognizer recognizer;
	
	private String title;
	
	private PhotoViewAttacher mAttacher;
				
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recognize_from_ex);
		
		// back to home button
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		//get the large image view
		faceView = (ImageView) findViewById(R.id.faceImageRec_imgview);
				
		//get the gallery view
		faceGallery = (Gallery) findViewById(R.id.gallery);
		
		//create a new adapter
		imgAdapt = new FaceAdapter(this);
		 
		//set the gallery adapter
		faceGallery.setAdapter(imgAdapt);
		
		//set the click listener for each item in the thumbnail gallery
		faceGallery.setOnItemClickListener(new OnItemClickListener() {
		    //handle clicks
		    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		        //set the larger image view to display the chosen bitmap calling method of adapter class
		    	currentPic = position;
		        faceView.setImageBitmap(imgAdapt.getPic(position));
		        // Attach a PhotoViewAttacher, which takes care of all of the zooming functionality.
		        mAttacher = new PhotoViewAttacher(faceView);
		    }
		});
		
		Intent intent = new Intent(Intent.ACTION_PICK);
		intent.setType("image/*");
		startActivityForResult(intent, SELECT_PICTURE_ACTIVITY_REQUEST_CODE);
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
	    super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
	    
	    switch (requestCode) {
	        case SELECT_PICTURE_ACTIVITY_REQUEST_CODE:
	        	
	            if (resultCode == RESULT_OK) {
	                Uri selectedImage = imageReturnedIntent.getData();
	                String[] filePathColumn = {MediaStore.Images.Media.DATA};
	                Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
	                if (cursor.moveToFirst()) {
	                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
	                    String filePath = cursor.getString(columnIndex);
	             
	                    title = "Image F.E.R.";
						
	                    String[] progStr = new String[4];
	                    progStr[0] = getString(R.string.progressDialog_Title);
	                    progStr[1] = getString(R.string.progressDialog_loadImg);
	                    progStr[2] = getString(R.string.progressDialog_faces);
	                    progStr[3] = getString(R.string.progressDialog_expr);
	                    
	                    // start recognition as asynctask
	                    recognizer = new Recognizer(this, imgAdapt, faceGallery, 
                 			   faceView, filePath, progStr, null);
	                    recognizer.execute();
	                }
	                cursor.close();    

	            }
	            else{
	            	finish();
	            }
	            break;
	    }
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.expression_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		switch(id){
			case(R.id.showExprPerc):{
				Toast expToast;
				if(currentPic!=0){
					String text = "";
					for(int i = 0; i < 7; i++)
						text += recognizer.getExpressions()[i]+(double)Math.round(recognizer.getResults()[currentPic-1][i]*100)/100+"%          \n";
					expToast = Toast.makeText(this, text, Toast.LENGTH_LONG);
				}else{
					expToast = Toast.makeText(this, getResources().getString(R.string.noFaceSelected), Toast.LENGTH_SHORT);
				}
				expToast.show();
				return true;
			}
			case(R.id.saveToGallery):{
				Bitmap b = imgAdapt.getPic(currentPic);
				Long tsLong = System.currentTimeMillis()/1000;
				title += " - "+tsLong.toString()+" - "+currentPic+"";
				
				Toast statusToast;
				if(Images.Media.insertImage(getContentResolver(), b, title, "Saved with Facial Expression Recognition app") == null)
					statusToast = Toast.makeText(this, R.string.failToSave, Toast.LENGTH_LONG);
				else
					statusToast = Toast.makeText(this, R.string.saveSuccess, Toast.LENGTH_LONG);
				statusToast.show();
				
				title = "Image F.E.R.";
				return true;
			}
			case(R.id.save):{
				Toast statusToast;
				
				ContentResolver cr = getContentResolver();

				
				for(int i = 1; i < imgAdapt.getCount(); i++)
				{
					Bitmap b = imgAdapt.getPic(i);
					
					ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				    b.compress(CompressFormat.PNG, 0, outputStream);  
				    
					Long tsLong = System.currentTimeMillis()/1000;
					title += " - "+tsLong.toString()+" - "+i+"";
					
					ContentValues values = new ContentValues();
					values.put(ExpressionContentProvider.KEY_COLUMN_1_NAME, title);
					values.put(ExpressionContentProvider.KEY_COLUMN_2_NAME, "Saved with Facial Expression Recognition app");
					values.put(ExpressionContentProvider.KEY_COLUMN_3_NAME, (double)Math.round(recognizer.getResults()[i-1][0]*100)/100);
					values.put(ExpressionContentProvider.KEY_COLUMN_4_NAME, (double)Math.round(recognizer.getResults()[i-1][1]*100)/100);
					values.put(ExpressionContentProvider.KEY_COLUMN_5_NAME, (double)Math.round(recognizer.getResults()[i-1][2]*100)/100);
					values.put(ExpressionContentProvider.KEY_COLUMN_6_NAME, (double)Math.round(recognizer.getResults()[i-1][3]*100)/100);
					values.put(ExpressionContentProvider.KEY_COLUMN_7_NAME, (double)Math.round(recognizer.getResults()[i-1][4]*100)/100);
					values.put(ExpressionContentProvider.KEY_COLUMN_8_NAME, (double)Math.round(recognizer.getResults()[i-1][5]*100)/100);
					values.put(ExpressionContentProvider.KEY_COLUMN_9_NAME, (double)Math.round(recognizer.getResults()[i-1][6]*100)/100);
					values.put(ExpressionContentProvider.KEY_COLUMN_10_NAME, outputStream.toByteArray());
	
					cr.insert(ExpressionContentProvider.CONTENT_URI, values);
					
					title = "Image F.E.R.";
				}
				
				
				statusToast = Toast.makeText(this, R.string.saveSuccess, Toast.LENGTH_LONG);
				statusToast.show();
				
				return true;
			}
			case(R.id.saveface):{
				
				Toast statusToast;
				
				if(currentPic!=0){
					ContentResolver cr = getContentResolver();
					Bitmap b = imgAdapt.getPic(currentPic);
					ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				    b.compress(CompressFormat.PNG, 0, outputStream);  
					
					
					Long tsLong = System.currentTimeMillis()/1000;
					title += " - "+tsLong.toString()+" - "+currentPic+"";
					
					ContentValues values = new ContentValues();
					values.put(ExpressionContentProvider.KEY_COLUMN_1_NAME, title);
					values.put(ExpressionContentProvider.KEY_COLUMN_2_NAME, "Saved with Facial Expression Recognition app");
					values.put(ExpressionContentProvider.KEY_COLUMN_3_NAME, (double)Math.round(recognizer.getResults()[currentPic-1][0]*100)/100);
					values.put(ExpressionContentProvider.KEY_COLUMN_4_NAME, (double)Math.round(recognizer.getResults()[currentPic-1][1]*100)/100);
					values.put(ExpressionContentProvider.KEY_COLUMN_5_NAME, (double)Math.round(recognizer.getResults()[currentPic-1][2]*100)/100);
					values.put(ExpressionContentProvider.KEY_COLUMN_6_NAME, (double)Math.round(recognizer.getResults()[currentPic-1][3]*100)/100);
					values.put(ExpressionContentProvider.KEY_COLUMN_7_NAME, (double)Math.round(recognizer.getResults()[currentPic-1][4]*100)/100);
					values.put(ExpressionContentProvider.KEY_COLUMN_8_NAME, (double)Math.round(recognizer.getResults()[currentPic-1][5]*100)/100);
					values.put(ExpressionContentProvider.KEY_COLUMN_9_NAME, (double)Math.round(recognizer.getResults()[currentPic-1][6]*100)/100);
					values.put(ExpressionContentProvider.KEY_COLUMN_10_NAME, outputStream.toByteArray());
	
					cr.insert(ExpressionContentProvider.CONTENT_URI, values);
					
					title = "Image F.E.R.";
					
					statusToast = Toast.makeText(this, R.string.saveSuccess, Toast.LENGTH_LONG);
				}else{
					statusToast = Toast.makeText(this, getResources().getString(R.string.noFaceSelected), Toast.LENGTH_SHORT);
				}
				statusToast.show();
			}
			default:{
				return super.onOptionsItemSelected(item);
			}
		}
	}
}
