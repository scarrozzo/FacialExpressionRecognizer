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

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class HomeActivity extends ActionBarActivity {

	private static final String TAG = "HomeActivity";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
			
		Button recFrExBtn = (Button) findViewById(R.id.RecFromEx_btn);

		
		recFrExBtn.setText(Html.fromHtml("<b>" + getResources().getString(R.string.recFromEx) + "</b>"
										+  "<br />" +
										"<small><i>" + getResources().getString(R.string.recFromExDetails) + "</i></small>"));
		recFrExBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(v.getId() == R.id.RecFromEx_btn)
				{
					Intent recIntent = new Intent(HomeActivity.this, RecognizeFromExActivity.class);
					startActivity(recIntent);
				}	
			}
		});
		
		
		Button capAndRec = (Button) findViewById(R.id.TakeAndRec_btn);
		capAndRec.setText(Html.fromHtml("<b>" + getResources().getString(R.string.takeAndRec) + "</b>"
				+  "<br />" +
				"<small><i>" + getResources().getString(R.string.takeAndRecDetails) + "</i></small>"));
		capAndRec.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(v.getId() == R.id.TakeAndRec_btn)
				{
					Intent recIntent = new Intent(HomeActivity.this, CaptureAndRecognizeActivity.class);
					startActivity(recIntent);
				}	
			}
		});
		
		
		Button savedExpressions = (Button) findViewById(R.id.savedExp_btn);
		savedExpressions.setText(Html.fromHtml("<b>" + getResources().getString(R.string.SavedExpr) + "</b>"
				+  "<br />" +
				"<small><i>" + getResources().getString(R.string.SavedExprDetails) + "</i></small>"));
		savedExpressions.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(v.getId() == R.id.savedExp_btn)
				{
					Intent savedIntent = new Intent(HomeActivity.this, SavedExpressionsActivity.class);
					startActivity(savedIntent);
				}
				
			}
		});
		
		Button productInfo = (Button) findViewById(R.id.productInfo_btn);
		productInfo.setText(Html.fromHtml("<b>" + getResources().getString(R.string.productInfo) + "</b>"));
		productInfo.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				final Dialog dialog = new Dialog(HomeActivity.this);
	        	dialog.setContentView(R.layout.custom_dialog_about);
	        	dialog.setTitle(R.string.dialog_title_product_info);
	        	
	        	// set the custom dialog components - text, image and button
	        	String content = getResources().getString(R.string.product_info);
				TextView text = (TextView) dialog.findViewById(R.id.help_page);
				text.setText(content);
				
				
				Button dialogButton = (Button) dialog.findViewById(R.id.dialogCustomBtnOK);
				
				// if button is clicked, close the custom dialog
				dialogButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
	 
				dialog.show();
			}
		});
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.home, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			
			Intent goSettingsIntent = new Intent(HomeActivity.this, SettingsActivity.class);
			startActivity(goSettingsIntent);
			
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
