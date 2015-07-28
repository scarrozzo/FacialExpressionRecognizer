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
import java.io.FileInputStream;

import org.altervista.scarrozzo.facialexpressionrecognizer.R;
import org.altervista.scarrozzo.facialexpressionrecognizer.Model.ExpressionContentProvider;

import uk.co.senab.photoview.PhotoViewAttacher;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Images;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ExpressionDetailViewActivity extends ActionBarActivity{
	
	private static final String TAG = "ExpressionDetailViewActivity";
	
	private ImageView iv;
	private Bitmap expression;
	private String title;
	private String description;
	private String content;	
	private long id_row;
	
	private PhotoViewAttacher mAttacher;
	
	private String tempPath;
	
	private static final int SHARE_PICTURE_ACTIVITY_REQUEST_CODE = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_expression_detail);
	    
	    
	    Intent recvIntent = getIntent();
	    	
		    String filename = getIntent().getStringExtra("image");
		    try {
		        FileInputStream is = this.openFileInput(filename);
		        expression = BitmapFactory.decodeStream(is);
		        is.close();
		    } catch (Exception e) {
		        e.printStackTrace();
		    }
	    
	    	iv = (ImageView) findViewById(R.id.faceImageRec_imgviewDetail);
	    	iv.setImageBitmap(expression);
	    	
	    	 // Attach a PhotoViewAttacher, which takes care of all of the zooming functionality.
	        mAttacher = new PhotoViewAttacher(iv);
	        
	        content = recvIntent.getStringExtra("content");
	        title = recvIntent.getStringExtra("title");
	        id_row = recvIntent.getLongExtra("id", -1);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    
	    switch (requestCode) {
        case SHARE_PICTURE_ACTIVITY_REQUEST_CODE:
        	
            // remove temp file
			 getApplicationContext().getContentResolver().delete(Uri.parse(tempPath), null, null);
			 break;
	    }
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.expression_detail_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		switch(id){
			case(R.id.Details):{
				final Dialog dialog = new Dialog(this);
	        	dialog.setContentView(R.layout.custom_dialog_details);
	        	dialog.setCancelable(false);
	        	dialog.setTitle(title);
	        	
	        	// set the custom dialog components - text, image and button
				TextView text = (TextView) dialog.findViewById(R.id.textCustomDialog);
				text.setText(content);

				
				Button dialogButton = (Button) dialog.findViewById(R.id.dialogCustomButtonOK);
				
				// if button is clicked, close the custom dialog
				dialogButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
	 
				dialog.show();

				return true;
			}
			case(R.id.saveGallery):{				
				Toast statusToast;
				if(Images.Media.insertImage(getContentResolver(), expression, title, description) == null)
					statusToast = Toast.makeText(this, R.string.failToSave, Toast.LENGTH_LONG);
				else
					statusToast = Toast.makeText(this, R.string.saveSuccess, Toast.LENGTH_LONG);
				statusToast.show();
				return true;
			}
			case(R.id.delete):{
				
				DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                          switch (which) {
                          case DialogInterface.BUTTON_POSITIVE:
                                 // Yes button clicked
                            Toast status;
              				
              				ContentResolver cr = getContentResolver();
              				
              				Uri rowAddress = ContentUris.withAppendedId(ExpressionContentProvider.CONTENT_URI, id_row);
              				
              				if(cr.delete(rowAddress, null, null) != 0){
              					status = Toast.makeText(ExpressionDetailViewActivity.this, R.string.deleteSuccess, Toast.LENGTH_SHORT);
              					status.show();
              					
              					Intent goBackIntent = new Intent(ExpressionDetailViewActivity.this, SavedExpressionsActivity.class);
              					startActivity(goBackIntent);
              				}else{
              					status = Toast.makeText(ExpressionDetailViewActivity.this, R.string.failToDelete, Toast.LENGTH_SHORT);
              					status.show();
              				}
                        	  
                        	  
								break;

                          case DialogInterface.BUTTON_NEGATIVE:
                                 // No button clicked
                                 // do nothing
                                 break;
                          }
                    }
	        	};
	        	
	        	 AlertDialog.Builder builder = new AlertDialog.Builder(this);
	             builder.setMessage(R.string.savedExpressions_sureToDelete)
	                          .setPositiveButton(R.string.yes, dialogClickListener)
	                          .setNegativeButton(R.string.no, dialogClickListener).show();
	        	
				return true;
			}
			case(R.id.shareIcon):{
			 ByteArrayOutputStream bos = new ByteArrayOutputStream();  
   			 expression.compress(Bitmap.CompressFormat.PNG, 0, bos);  

   			 // temporary file. It will be removed in onActivityResult callback
   			 tempPath = Images.Media.insertImage(getApplicationContext().getContentResolver(), expression, title, description);
   			 
   			 Intent intent = new Intent(); 
   			 intent.setAction(Intent.ACTION_SEND); 
   			 intent.setType("image/png"); 
   			 intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(tempPath));
   			 startActivityForResult(intent, SHARE_PICTURE_ACTIVITY_REQUEST_CODE);
			 return true;
			}
			default:{
				return super.onOptionsItemSelected(item);
			}
		}
	}
}
