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
import java.io.FileOutputStream;
import java.util.ArrayList;

import org.altervista.scarrozzo.facialexpressionrecognizer.R;
import org.altervista.scarrozzo.facialexpressionrecognizer.Model.ExpressionContentProvider;
import org.altervista.scarrozzo.facialexpressionrecognizer.util.GridViewAdapter;
import org.altervista.scarrozzo.facialexpressionrecognizer.util.ImageAdapter;
import org.altervista.scarrozzo.facialexpressionrecognizer.util.ImageItem;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Images;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

public class SavedExpressionsActivity extends ActionBarActivity{

	private static final String TAG = "SavedExpressionsActivity";
	
	private GridView gridview;
	private GridViewAdapter imgAdpt;
	private ArrayList<ImageItem> imageItems;
	private int selected;
	private ArrayList<Long> ids;
	
	// extract from db
    private String[] title, description;
    private String[] expr;
    private double expr_perc[][];
    
    private String tempPath;
	private static final int SHARE_PICTURE_ACTIVITY_REQUEST_CODE = 0;
	
	private boolean flag = false;
	
	private ArrayList<Integer> multiselect;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_saved_expressions);

	    imageItems = new ArrayList<ImageItem>();
	    multiselect = new ArrayList<Integer>();
	    
	    gridview = (GridView) findViewById(R.id.gridview);
	    
	    ids = new ArrayList<Long>();
	    
	    final ContentResolver cr = getContentResolver();
	    
	    String[] result_columns = new String[] {
	    		ExpressionContentProvider.KEY_ID, 
	    		ExpressionContentProvider.KEY_COLUMN_1_NAME,
	    		ExpressionContentProvider.KEY_COLUMN_2_NAME,
	    		ExpressionContentProvider.KEY_COLUMN_3_NAME,
	    		ExpressionContentProvider.KEY_COLUMN_4_NAME,
	    		ExpressionContentProvider.KEY_COLUMN_5_NAME,
	    		ExpressionContentProvider.KEY_COLUMN_6_NAME,
	    		ExpressionContentProvider.KEY_COLUMN_7_NAME,
	    		ExpressionContentProvider.KEY_COLUMN_8_NAME,
	    		ExpressionContentProvider.KEY_COLUMN_9_NAME,
	    		ExpressionContentProvider.KEY_COLUMN_10_NAME};

	    String where = null;
	    
	    // Replace these with valid SQL statements as necessary. 
	    String whereArgs[] = null;
	    String order = null;
	    
	    Bitmap bmp_expr;
	    
	    Cursor resultCursor = cr.query(ExpressionContentProvider.CONTENT_URI,
	    		result_columns, where, whereArgs, order);

	    int n = resultCursor.getCount();
	    title = new String[n];
	    description = new String[n];
	    expr_perc = new double[n][7];
	    int j = 0;
	    while (resultCursor.moveToNext()) {
	    		    	
	    	title[j] = resultCursor.getString(resultCursor.getColumnIndex(ExpressionContentProvider.KEY_COLUMN_1_NAME));
	    	description[j] = resultCursor.getString(resultCursor.getColumnIndex(ExpressionContentProvider.KEY_COLUMN_2_NAME));
	    	
	    	expr = new String[]{ExpressionContentProvider.KEY_COLUMN_3_NAME, ExpressionContentProvider.KEY_COLUMN_4_NAME,
	    			ExpressionContentProvider.KEY_COLUMN_5_NAME, ExpressionContentProvider.KEY_COLUMN_6_NAME,
	    			ExpressionContentProvider.KEY_COLUMN_7_NAME, ExpressionContentProvider.KEY_COLUMN_8_NAME,
	    			ExpressionContentProvider.KEY_COLUMN_9_NAME};
	    		    	
	    	for(int i = 0; i < 7; i++)
	    		expr_perc[j][i] = resultCursor.getDouble(resultCursor.getColumnIndex(expr[i]));
	    		
	    	byte imgByte[] = resultCursor.getBlob(resultCursor.getColumnIndex(ExpressionContentProvider.KEY_COLUMN_10_NAME));
	    	bmp_expr = BitmapFactory.decodeByteArray(imgByte, 0, imgByte.length);
	    	
	    	ids.add(j, resultCursor.getLong((resultCursor.getColumnIndex(ExpressionContentProvider.KEY_ID))));
	    	imageItems.add(new ImageItem(bmp_expr));
	    	j++;
	    }

	    resultCursor.close();
	    	
	    imgAdpt = new GridViewAdapter(this, R.layout.row_grid, imageItems);
	    
	    gridview.setAdapter(imgAdpt);
	    
	    gridview.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE_MODAL);
	    gridview.setMultiChoiceModeListener(new MultiChoiceModeListener());
	    
	    gridview.setOnItemClickListener(new OnItemClickListener() {
	        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
	            
	            selected = position;
	            Intent detailViewIntent = new Intent(SavedExpressionsActivity.this, ExpressionDetailViewActivity.class);
	           
	            
	            String content = "";
				for(int i=0;i<7;i++){
					content += expr[i]+": "+expr_perc[selected][i]+"%\n";
				}
				content += "\n";
				content += description[selected];
				
				try{
					String filename = "bitmap.png";
				    FileOutputStream stream = openFileOutput(filename, Context.MODE_PRIVATE);
				    imageItems.get(selected).getImage().compress(Bitmap.CompressFormat.PNG, 0, stream);
		            
				    //Cleanup
				    stream.close();
				    
				    detailViewIntent.putExtra("image", filename);
				    detailViewIntent.putExtra("content", content);
		            detailViewIntent.putExtra("title", title[selected]);
		            detailViewIntent.putExtra("id", ids.get(selected));
		            startActivity(detailViewIntent);
				}catch(Exception e){
					e.printStackTrace();
				}
	            
	        }
	    });
	    
	    gridview.setOnItemLongClickListener(new OnItemLongClickListener(){

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				 selected = position;
		        	Log.d(TAG, "Selected aggiornato. Valore di selected: "+selected+". Con id: "+ids.get(selected));

				return false;
			}
	    	
	    });
	    
	    
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
		getMenuInflater().inflate(R.menu.menu_gridview, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		switch(id){
			case(R.id.deleteAllSaved):{
				
				DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                          switch (which) {
                          case DialogInterface.BUTTON_POSITIVE:
                                 // Yes button clicked
								ContentResolver cr = getContentResolver();
		
								if(cr.delete(ExpressionContentProvider.CONTENT_URI, null, null) == 0){
									Toast nothingToDelete = Toast.makeText(getApplicationContext(), 
															R.string.savedExpressions_nothingToDelete, Toast.LENGTH_SHORT);
									nothingToDelete.show();
								}
		
								for (int i = 0; i < imgAdpt.getCount(); i++)
									imageItems.remove(i);
		
								ImageAdapter tmp = new ImageAdapter(SavedExpressionsActivity.this);
								
								gridview.setAdapter(tmp);
                                
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
			default:{
				return super.onOptionsItemSelected(item);
			}
		}
	}
	
	private void deleteImage(){
		Toast status;
		
		ContentResolver cr = getContentResolver();
		
		Uri rowAddress = ContentUris.withAppendedId(ExpressionContentProvider.CONTENT_URI, ids.get(selected));
		
		
		if(cr.delete(rowAddress, null, null) != 0){
			
			ids.remove(selected);

			imageItems.remove(selected);
			
			gridview.setAdapter(imgAdpt);
			
			status = Toast.makeText(this, R.string.deleteSuccess, Toast.LENGTH_SHORT);
			status.show();

		}else{
			status = Toast.makeText(this, R.string.failToDelete, Toast.LENGTH_SHORT);
			status.show();
		}
	}

	
	private void multiDeleteImage(){
		Toast status;
		
		ContentResolver cr = getContentResolver();
		
		Uri rowAddress = ContentUris.withAppendedId(ExpressionContentProvider.CONTENT_URI, ids.get(selected));
		
		
		if(cr.delete(rowAddress, null, null) != 0){
			
			ids.remove(selected);

			imageItems.remove(selected);
			
			gridview.setAdapter(imgAdpt);
			

		}else{
			status = Toast.makeText(this, R.string.failToDelete+" image "+selected, Toast.LENGTH_SHORT);
			status.show();
		}
	}

	
	private class MultiChoiceModeListener implements
			GridView.MultiChoiceModeListener {
		
		@Override
		public boolean onCreateActionMode(android.view.ActionMode mode,
				Menu menu) {
			
			mode.setTitle(R.string.grid_context_menuTitle_selItems);
            mode.setSubtitle(R.string.grid_context_menuSubTitle_selItems);
            return true;
		}

		@Override
		public boolean onPrepareActionMode(android.view.ActionMode mode,
				Menu menu) {
			menu.clear();
			MenuInflater inflater = getMenuInflater();
			
			if(!flag){
				inflater.inflate(R.menu.context_menu_gridview, menu);
			}else{
				inflater.inflate(R.menu.context_menu_gridview2, menu);
			}

			return true;
		}

		@Override
		public boolean onActionItemClicked(final android.view.ActionMode mode,
				MenuItem item) {
						
			 int selectCount = gridview.getCheckedItemCount();
			
			 if(selectCount==1){
				 
				
				switch (item.getItemId()) {
		    	case R.id.share:
		    	{
		    			
		    			 ByteArrayOutputStream bos = new ByteArrayOutputStream();  
		    			 imageItems.get(selected).getImage().compress(Bitmap.CompressFormat.PNG, 0, bos);
	
		    			 // temporary file. It will be removed in onActivityResult callback
		    			 tempPath = Images.Media.insertImage(getApplicationContext().getContentResolver(), /*imgAdpt.getPic(selected)*/imageItems.get(selected).getImage(), title[selected], description[selected]);
		    			 
		    			 Intent intent = new Intent(); 
		    			 intent.setAction(Intent.ACTION_SEND); 
		    			 intent.setType("image/png"); 
		    			 intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(tempPath));
		    			 startActivityForResult(intent, SHARE_PICTURE_ACTIVITY_REQUEST_CODE);	
		    		return true;
		    	}
		    	case(R.id.saveGallery):{
					Bitmap b = imageItems.get(selected).getImage();
					
					Toast statusToast;
					if(Images.Media.insertImage(getContentResolver(), b, title[selected], description[selected]) == null)
						statusToast = Toast.makeText(SavedExpressionsActivity.this, R.string.failToSave, Toast.LENGTH_LONG);
					else
						statusToast = Toast.makeText(SavedExpressionsActivity.this, R.string.saveSuccess, Toast.LENGTH_LONG);
					statusToast.show();
					return true;
				}
		        case R.id.delete:
		        {
		        	DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int which) {
	                          switch (which) {
	                          case DialogInterface.BUTTON_POSITIVE:
	                                 // Yes button clicked
	                        	  	deleteImage();
	                        	  	mode.finish();
									break;
	
	                          case DialogInterface.BUTTON_NEGATIVE:
	                                 // No button clicked
	                                 // do nothing
	                                 break;
	                          }
	                    }
		        	};
		        	
		        	 AlertDialog.Builder builder = new AlertDialog.Builder(SavedExpressionsActivity.this);
		             builder.setMessage(R.string.savedExpressions_sureToDelete)
		                          .setPositiveButton(R.string.yes, dialogClickListener)
		                          .setNegativeButton(R.string.no, dialogClickListener).show();
		        	
		            return true;
		        }
		        case R.id.details:
		        {	
		        	final Dialog dialog = new Dialog(SavedExpressionsActivity.this);
		        	dialog.setContentView(R.layout.custom_dialog_details);
		        	dialog.setCancelable(false);
		        	dialog.setTitle(title[selected]);
		        	
		        	// set the custom dialog components - text, image and button
					TextView text = (TextView) dialog.findViewById(R.id.textCustomDialog);
					String content = "";
					for(int i=0;i<7;i++){
						content += expr[i]+": "+expr_perc[selected][i]+"%\n";
					}
					content += "\n";
					content += description[selected];
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
		        default:
		            return false;
		      }
			 }else{
				 
				 switch (item.getItemId()) {
			    	case R.id.multiDelete:
			    	{
			    		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
		                    public void onClick(DialogInterface dialog, int which) {
		                          switch (which) {
		                          case DialogInterface.BUTTON_POSITIVE:
		                                 // Yes button clicked
		                        	  	int tmp = selected;
		                        	  	int i=0;
		                        	  	while(i<multiselect.size()){
		                        	  		selected = multiselect.get(i);
		                        	    	multiDeleteImage();
											
		                        	    	for(int j=0;j<multiselect.size();j++)
		                        	    		if(multiselect.get(j)>selected)
		                        	    			multiselect.set(j, multiselect.get(j)-1);
		                        	    	i++;
		                        	    }
		                        	    selected = tmp;
		                        	    multiselect.clear();
		                        	    
		                        	    Toast status = Toast.makeText(SavedExpressionsActivity.this, R.string.deleteSuccess, Toast.LENGTH_SHORT);
		                    			status.show();
		                        	    
		                        	  	mode.finish();
										break;
		
		                          case DialogInterface.BUTTON_NEGATIVE:
		                                 // No button clicked
		                                 // do nothing
		                                 break;
		                          }
		                    }
			        	};
			        	
			        	 AlertDialog.Builder builder = new AlertDialog.Builder(SavedExpressionsActivity.this);
			             builder.setMessage(R.string.savedExpressions_sureToDelete)
			                          .setPositiveButton(R.string.yes, dialogClickListener)
			                          .setNegativeButton(R.string.no, dialogClickListener).show();
			    		
			    		return true;
			    	}
			    	default:
			    		return false;
				 }
			 }	
		}

		@Override
		public void onDestroyActionMode(android.view.ActionMode mode) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onItemCheckedStateChanged(android.view.ActionMode mode,
				int position, long id, boolean checked) {
			
			if(checked){
				selected = position;
				multiselect.add(position);
			}else{
				for(int i = 0; i < multiselect.size(); i++)
					if(multiselect.get(i)==position)
						multiselect.remove(i);
			}
			
			Menu menu = mode.getMenu();
			 int selectCount = gridview.getCheckedItemCount();
	            switch (selectCount) {
	            case 1:
	            	// restore the menu 
					if(menu.size()==1){
						flag = false;
						mode.invalidate();
					}
	                mode.setSubtitle(R.string.grid_context_menu_oneItemSelected);
	                break;
	            default:
					 if(menu.size()>1){
						 flag = true;
						 mode.invalidate();
					 }
	                mode.setSubtitle("" + selectCount + " "+getResources().getString(R.string.grid_context_menu_moreItemsSelected));
	                break;
	            }
			
		}
	}
}
