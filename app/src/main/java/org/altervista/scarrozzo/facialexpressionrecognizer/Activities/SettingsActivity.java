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

import org.altervista.scarrozzo.facialexpressionrecognizer.R;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.PopupMenu.OnMenuItemClickListener;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;

public class SettingsActivity extends ActionBarActivity{

	private static final String TAG = "SettingsActivity";
	
	private RadioButton radioCasual;
	private RadioButton radioCustom;

	private Button labelButton;
	private Button barButton;
	
	private static final String MyPREFERENCES = "MyPrefs";
	private Editor editor;
	private String keys[];
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_settings);
	    
	    SharedPreferences sharedpreferences = getSharedPreferences(MyPREFERENCES, 
	    												Context.MODE_PRIVATE);
	    
	    keys = new String[3];
	    
	    if(sharedpreferences.getString("favorites", null)!=null){
		    String temp[] = sharedpreferences.getString("favorites", null).split(",");
		    for(int i = 0; i < temp.length; i++){
		    	keys[i] = temp[i];
		    }
	    }
	    
	    editor = sharedpreferences.edit();
	    
	    radioCasual = (RadioButton) findViewById(R.id.radio0);
	    radioCustom = (RadioButton) findViewById(R.id.radio1);
	    labelButton = (Button) findViewById(R.id.btn_labelColor);
	    barButton = (Button) findViewById(R.id.btn_barsColor);

	    
	    labelButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				PopupMenu pum = new PopupMenu(SettingsActivity.this, findViewById(R.id.btn_labelColor));
	            pum.inflate(R.menu.button_chooser_popup);
	            pum.show();
	            
	            pum.setOnMenuItemClickListener(new OnMenuItemClickListener(){

					@Override
					public boolean onMenuItemClick(MenuItem arg0) {
						
						labelButton.setText(arg0.getTitle());
						
						keys[1] = String.valueOf(arg0.getTitle());
						
						editor.putString("favorites", convertArrayToString(keys));
						
						//Log.d(TAG, "ID valore cliccato: "+String.valueOf(arg0.getItemId()));
					    editor.commit();
						
						return true;
						//return false;
					}
	            	
	            });
			}
		});
	    
	    barButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				PopupMenu pum = new PopupMenu(SettingsActivity.this, findViewById(R.id.btn_barsColor));
	            pum.inflate(R.menu.button_chooser_popup);
	            pum.show();
	            
	            pum.setOnMenuItemClickListener(new OnMenuItemClickListener(){

					@Override
					public boolean onMenuItemClick(MenuItem arg0) {
						
						barButton.setText(arg0.getTitle());
						
						keys[2] = String.valueOf(arg0.getTitle());
						
						editor.putString("favorites", convertArrayToString(keys));
						
						//Log.d(TAG, "ID valore cliccato: "+String.valueOf(arg0.getTitle()));
					    editor.commit();
						
						return true;
						//return false;
					}
	            	
	            });
			}
		});
	    
	    
	    if(keys[0]==null || keys[0].equals("random"))
	    	radioCasual.setChecked(true);
	    else
	    	radioCustom.setChecked(true);
	    
	    if(keys[1]!=null && !(keys[1].equals("null")))
	    	labelButton.setText(keys[1]);
	    else
	    	labelButton.setText("-----");
	    
	    if(keys[2]!=null && !(keys[2].equals("null")))
	    	barButton.setText(keys[2]);
	    else
	    	barButton.setText("-----");
	    
	    // disable buttons when colors are casual
	    if(radioCasual.isChecked()){
	    		labelButton.setEnabled(false);
		    	barButton.setEnabled(false);
	    }
	    
	    radioCasual.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				keys[0] = "random";
				
				editor.putString("favorites", convertArrayToString(keys));
				
			    editor.commit();
				
				labelButton.setEnabled(false);
		    	barButton.setEnabled(false);
		    	
		    	
			}
		});
	    
	    radioCustom.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				keys[0] = "norandom";
				
				editor.putString("favorites", convertArrayToString(keys));
				
			    editor.commit();
				
				labelButton.setEnabled(true);
		    	barButton.setEnabled(true);
			}
		});
	    
	}
	
	
	private String convertArrayToString(String[] array){
		String result = "";
		for(int i=0;i<array.length;i++){
			result += array[i];
			if(i<array.length-1)
				result += ",";
		}
		
		return result;
	}
}
