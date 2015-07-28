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

package org.altervista.scarrozzo.facialexpressionrecognizer.recognitionAlgorithms;

import static com.googlecode.javacv.cpp.opencv_core.cvCreateImage;
import static com.googlecode.javacv.cpp.opencv_core.cvSize;
import static com.googlecode.javacv.cpp.opencv_highgui.cvLoadImage;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_BGR2GRAY;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_BGR2RGBA;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCvtColor;
import static com.googlecode.javacv.cpp.opencv_core.cvRect;
import static com.googlecode.javacv.cpp.opencv_core.cvCopy;
import static com.googlecode.javacv.cpp.opencv_core.cvResetImageROI;
import static com.googlecode.javacv.cpp.opencv_core.cvSetImageROI;
import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_core.cvReleaseImage;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.altervista.scarrozzo.facialexpressionrecognizer.R;
import org.altervista.scarrozzo.facialexpressionrecognizer.Activities.HomeActivity;
import org.altervista.scarrozzo.facialexpressionrecognizer.util.FaceAdapter;
import org.altervista.scarrozzo.facialexpressionrecognizer.util.ImageUtil;
import org.encog.Encog;
import org.encog.ml.data.MLData;
import org.encog.ml.data.MLDataPair;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.neural.networks.BasicNetwork;
import org.encog.persist.EncogDirectoryPersistence;

import com.googlecode.javacv.cpp.opencv_core.CvSize;
import com.googlecode.javacv.cpp.opencv_core.IplImage;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.Toast;

public final class Recognizer extends AsyncTask<Void, Integer, Void> {

	private static String classifier;
	private final Activity parent;
	private ProgressDialog prog;
	private FaceAdapter imgAdapt;
	private Gallery faceGallery;
	private ImageView faceView;
	private IplImage srcImg;
	private String filePath;
	private IplImage[] faces;
	private String[] progStr;
	private String ioError;

	private static ArrayList<Integer> progr = new ArrayList<Integer>();	
	private int index;
	
	private String expressions[];

	private double[][] results;

	private int[] colors = {Color.BLUE, Color.BLACK, Color.RED, Color.GREEN, Color.CYAN, Color.MAGENTA,
							   Color.YELLOW, Color.WHITE};
	
	private Uri mCapturedImageURI;
	
	private boolean deleteFlag;
	
	private static final String filename = "expression.png";
	
	public Recognizer(final Activity parent, org.altervista.scarrozzo.facialexpressionrecognizer.util.FaceAdapter imgAdapt,
			Gallery faceGallery, ImageView faceView, String filePath,
			String[] progStr, Uri mCapturedImageURI) {
		this.parent = parent;
		this.imgAdapt = imgAdapt;
		this.faceGallery = faceGallery;
		this.faceView = faceView;
		this.filePath = filePath;
		this.progStr = progStr;
		this.ioError = null;
		this.mCapturedImageURI = mCapturedImageURI;
		
		this.expressions = new String[]{ parent.getResources().getString(R.string.exp_neutral), 
										 parent.getResources().getString(R.string.exp_happy),
										 parent.getResources().getString(R.string.exp_surprise),
										 parent.getResources().getString(R.string.exp_anger),
										 parent.getResources().getString(R.string.exp_fear),
										 parent.getResources().getString(R.string.exp_sad),
										 parent.getResources().getString(R.string.exp_disgust)
									   };
		this.deleteFlag = false;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();

		progr.add(10);
		progr.add(40);
		
		prog = new ProgressDialog(parent);
		prog.setTitle(progStr[0]);
		prog.setMessage(progStr[1]);
		prog.setIndeterminate(false);
		prog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		prog.setCanceledOnTouchOutside(false);
		prog.setOnCancelListener(new DialogInterface.OnCancelListener()
		{
		    @Override
		    public void onCancel(DialogInterface dialog)
		    {
		    	 parent.finish();
		         Recognizer.this.cancel(true);
		    }
		});
		prog.show();

		index = 0;
	}

	@Override
	protected Void doInBackground(Void... params) {

		OUTERMOST: while(!deleteFlag){
		
		// scale bitmap and put the image in the right position
	    try {
	    	
	    	 //set the width and height we want to use as maximum display
            int targetWidth = 1024;
            int targetHeight = 768;
            //create bitmap options to calculate and use sample size
            BitmapFactory.Options bmpOptions = new BitmapFactory.Options();
            //first decode image dimensions only - not the image bitmap itself
            bmpOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(filePath, bmpOptions);
            //image width and height before sampling
            int currHeight = bmpOptions.outHeight;
            int currWidth = bmpOptions.outWidth;
            //variable to store new sample size
            int sampleSize = 1;
            //calculate the sample size if the existing size is larger than target size
            if (currHeight>targetHeight || currWidth>targetWidth) 
             {
                 //use either width or height
                 if (currWidth>currHeight)
                     sampleSize = Math.round((float)currHeight/(float)targetHeight);
                 else
                     sampleSize = Math.round((float)currWidth/(float)targetWidth);
             }
           //use the new sample size
           bmpOptions.inSampleSize = sampleSize;
           //now decode the bitmap using sample options
           bmpOptions.inJustDecodeBounds = false;
           //get the file as a bitmap
           File imgFile = new File(filePath);
           Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath(), bmpOptions);
            	
		    
            ExifInterface exif = new ExifInterface(imgFile.getAbsolutePath());
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);

            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
            }
            else if (orientation == 3) {
                matrix.postRotate(180);
            }
            else if (orientation == 8) {
                matrix.postRotate(270);
            }
            myBitmap = Bitmap.createBitmap(myBitmap, 0, 0, myBitmap.getWidth(), myBitmap.getHeight(), matrix, true); // rotating bitmap
            
            FileOutputStream fOut = parent.openFileOutput(filename, Context.MODE_PRIVATE);
            myBitmap.compress(Bitmap.CompressFormat.PNG, 50, fOut);
            fOut.flush();
            fOut.close();
            myBitmap.recycle();
	    }
        catch (Exception e) {
        	e.printStackTrace();
        }	
		// end orientation
		
	    if(isCancelled()){
			if(mCapturedImageURI != null)
				parent.getApplicationContext().getContentResolver().delete(mCapturedImageURI, null, null);
			break OUTERMOST;
		}
	    
		// Load Image
		srcImg = cvLoadImage(parent.getFileStreamPath(filename).getAbsolutePath());
		
		publishProgress();
		
		if(isCancelled()){
			if(mCapturedImageURI != null)
				parent.getApplicationContext().getContentResolver().delete(mCapturedImageURI, null, null);
			break OUTERMOST;
		}	
			
		
		AssetManager assetManager = parent.getAssets();
		try {
				File learnedInputFile = new File(parent.getApplicationContext().getFilesDir()
												 +"haarcascade_frontalface_default.xml");
				if(!learnedInputFile.exists()){
					InputStream learnedDataInputStream = assetManager.open("haarcascade_frontalface_default.xml");
					FileOutputStream learnedDataOutputStream = new FileOutputStream(learnedInputFile);
			
					// copy file from asset to internal location
					byte[] buffer = new byte[300];
					int n = 0;
					while(-1 != (n = learnedDataInputStream.read(buffer))){
						learnedDataOutputStream.write(buffer, 0, n);
					}
					learnedDataOutputStream.close();
				}
			classifier = learnedInputFile.getAbsolutePath();
		} catch (IOException exception) {
			exception.printStackTrace();
		}

		if (classifier != null) {
			
			// begin face detections
			FaceDetectionStrategy faceRec = new HaarFaceDetectionStrategy(
					classifier);

			if(isCancelled()){
				if(mCapturedImageURI != null)
					parent.getApplicationContext().getContentResolver().delete(mCapturedImageURI, null, null);
				break OUTERMOST;
			}	
			
			faces = faceRec.detectAndCropFace(srcImg);
			
			publishProgress();
			// end face detections

			if(isCancelled()){
				if(mCapturedImageURI != null)
					parent.getApplicationContext().getContentResolver().delete(mCapturedImageURI, null, null);
				break OUTERMOST;
			}	
			
			
			if (faces != null) {
				
				// variable for grayscale convertion
				CvSize cvSize;
				IplImage gry;
				IplImage faceTest = null;
				
				// Feature extraction algorithm
				FeaturesStrategy lbp = new LBPfeaturesStrategy();
				
				// load saved neural network
				InputStream is = parent.getResources().openRawResource(R.raw.encognet);
				BasicNetwork network = (BasicNetwork) EncogDirectoryPersistence.loadObject(is);
						
				this.results = new double[faces.length][7];
				
				// foreach face detected
				for (int i = 0; i < faces.length; i++) {

					if(isCancelled()){
						if(mCapturedImageURI != null)
							parent.getApplicationContext().getContentResolver().delete(mCapturedImageURI, null, null);
						break OUTERMOST;
					}	
					
					progr.add(50/faces.length);
					
					if(faces[i].nChannels() != 1){
						cvSize = cvSize(faces[i].width(), faces[i].height());
						gry = cvCreateImage(cvSize, faces[i].depth(), 1);
						cvCvtColor(faces[i], gry, CV_BGR2GRAY);
						faceTest = gry;
					}else{
						faceTest = faces[i];
					}
					
					// begin feature extractions

					int wres = 160;
					int hres = 160;
					faceTest = ImageUtil.getScaledInstance(faceTest, wres,
							hres, true);

					double[][] hist = new double[16][59];
					int x = 0, y = 0;
					int w = wres / 4, h = hres / 4;


					IplImage iplRegion;

					int k;
					for (k = 0; k < 16; k++) {
						if (x == wres) {
							x = 0;
							y += h;
						}

						iplRegion = IplImage.create(w, h, faceTest.depth(),
								faceTest.nChannels());
						cvSetImageROI(faceTest, cvRect(x, y, w, h));
						cvCopy(faceTest, iplRegion);
						cvResetImageROI(faceTest);

						hist[k] = lbp.getFeatures(iplRegion, null);

						x += w;
					}

					double features[] = new double[944];
					k = 0;
					for (int s = 0; s < 16; s++)
						for (int j = 0; j < 59; j++)
							features[k++] = (double) hist[s][j];

					// end feature extractions

					
					// begin recognize expression

					// create test set (new face expressions)
					MLDataSet testSet = new BasicMLDataSet();
					MLData input = null;

					input = new BasicMLData(features);
					testSet.add(input);
					
					if(isCancelled()){
						if(mCapturedImageURI != null)
							parent.getApplicationContext().getContentResolver().delete(mCapturedImageURI, null, null);
						break OUTERMOST;
					}	
					else
						prog.setCancelable(false);
					
					for (MLDataPair pair : testSet) {
						final MLData output = network.compute(pair.getInput());
						double[] networkOutput = output.getData();
						
						for(int t=0;t<7;t++)
							results[i][t] = networkOutput[t]*100;
						
					}
					
					
					// end recognize expression
					publishProgress();
				}
				
				Encog.getInstance().shutdown();
				
			}

		} else {
			ioError = "classErr";
		}

			deleteFlag = true;
		}
	
		if(!deleteFlag)
			parent.finish();
	
		return null;
	}

	@Override
	protected void onProgressUpdate(final Integer... values) {
		super.onProgressUpdate(values);

		if (index == 0)
			prog.setMessage(progStr[2]);
		else
			prog.setMessage(progStr[3]);

		prog.incrementProgressBy(progr.get(index));
		index++;
	}

	@Override
	protected void onPostExecute(final Void result) {
		super.onPostExecute(result);

		if(!isCancelled()){
		if (ioError == null) {
			if (faces == null) {
				prog.dismiss();
				Toast toast = Toast.makeText(parent, parent.getResources().getString(R.string.noFaceRecognized),
						Toast.LENGTH_LONG);
				toast.show();
				
				if(mCapturedImageURI != null)
					parent.getApplicationContext().getContentResolver().delete(mCapturedImageURI, null, null);
				
				Intent backToHomeIntent = new Intent(parent, HomeActivity.class);
				parent.startActivity(backToHomeIntent);
			} else {
				
				Bitmap srcImgBitmap = Bitmap.createBitmap(srcImg.width(), srcImg.height(), Bitmap.Config.ARGB_8888);
				Bitmap[] facesBitmap = new Bitmap[faces.length];
				
				IplImage comp = IplImage.create(srcImg.width(), srcImg.height(), IPL_DEPTH_8U, 4);
				cvCvtColor(srcImg, comp, CV_BGR2RGBA);
				srcImgBitmap.copyPixelsFromBuffer(comp.getByteBuffer());

				imgAdapt.addPic(srcImgBitmap, 0);
				
				final String MyPREFERENCES = "MyPrefs";
				SharedPreferences sharedpreferences = parent.getSharedPreferences(MyPREFERENCES, 
						Context.MODE_PRIVATE);
				int cols[] = new int[2];
				
				for (int i = 0; i < faces.length; i++){
					
					// make expression label as image					
					Bitmap exprImg = Bitmap.createBitmap(200, 150, Bitmap.Config.ARGB_8888);
					Canvas canvas = new Canvas(exprImg); 
					cols = getColors(sharedpreferences, i);
					canvas.drawColor(cols[0]);
					Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
					paint.setColor(cols[1]);
					paint.setTextSize(17);
					Rect bounds = new Rect();
					String text = "";
					for(int j = 0; j < expressions.length; j++)
						text += expressions[j]+"\n";
					
					paint.getTextBounds(text, 0, text.length(), bounds);
					int x_text = 5;
					int y_text = 16;
					int j = 0;
					for(String line: text.split("\n")){
					      canvas.drawText(line, x_text, y_text, paint);
					      canvas.drawRect(x_text+80, y_text-10, x_text+80+(float)Math.round(results[i][j++]*100)/100, y_text, paint);
					      y_text +=	 paint.descent()-paint.ascent()+2;
					}
					
					// convert face from IplImage to Bitmap
					comp = IplImage.create(faces[i].width(), faces[i].height(), IPL_DEPTH_8U, 4);
					facesBitmap[i] = Bitmap.createBitmap(faces[i].width(), faces[i].height(), Bitmap.Config.ARGB_8888);
					cvCvtColor(faces[i], comp, CV_BGR2RGBA);
					facesBitmap[i].copyPixelsFromBuffer(comp.getByteBuffer());
					
					// merge face and label
					int max_width = facesBitmap[i].getWidth() > exprImg.getWidth() ? facesBitmap[i].getWidth() : exprImg.getWidth();
					Bitmap res = Bitmap.createBitmap(max_width, facesBitmap[i].getHeight()+150, facesBitmap[i].getConfig());
	                canvas = new Canvas(res);
	                canvas.drawBitmap(facesBitmap[i], 0, 0, null);
	                canvas.drawBitmap(exprImg, 0, facesBitmap[i].getHeight(), null);
					
	                // save result image in Gallery object using adapter
					imgAdapt.addPic(res, i + 1);
				}

				faceGallery.setAdapter(imgAdapt);

				faceView.setImageBitmap(srcImgBitmap);

				faceView.setScaleType(ImageView.ScaleType.FIT_CENTER);

				if(mCapturedImageURI != null)
					parent.getApplicationContext().getContentResolver().delete(mCapturedImageURI, null, null);
				
				prog.dismiss();
			}
		} else if (ioError.equals("classErr")) {
			prog.dismiss();
			Toast toast = Toast.makeText(parent,
					R.string.classErr,
					Toast.LENGTH_LONG);
			toast.show();
			Intent backToHomeIntent = new Intent(parent, HomeActivity.class);
			parent.startActivity(backToHomeIntent);
		}
		
		cvReleaseImage(srcImg);
		}
	}
	
	
	public double[][] getResults() {
		return results;
	}
	
	public String[] getExpressions() {
		return expressions;
	}
	
	private int[] getColors(SharedPreferences sharedpreferences, int i){
		
		// index 0 for label and index 1 for bars
		int colors[] = new int[2];
		String[] values;
		try{
			values = sharedpreferences.getString("favorites", null).split(",");
		}catch(Exception e){
			values = null;
		}
		if(values!=null && values[0]!=null && values[0].equals("norandom") && values[1]!=null && values[2]!=null){
			
			if(values[1].equals("Blue") || values[1].equals("Blu"))
				colors[0] = Color.BLUE;
			else if(values[1].equals("Black") || values[1].equals("Nero"))
				colors[0] = Color.BLACK;
			else if(values[1].equals("Red") || values[1].equals("Rosso"))
				colors[0] = Color.RED;
			else if(values[1].equals("Green") || values[1].equals("Verde"))
				colors[0] = Color.GREEN;
			else if(values[1].equals("Cyan") || values[1].equals("Ciano"))
				colors[0] = Color.CYAN;
			else if(values[1].equals("Magenta"))
				colors[0] = Color.MAGENTA;
			else if(values[1].equals("Yellow") || values[1].equals("Giallo"))
				colors[0] = Color.YELLOW;
			else if(values[1].equals("White") || values[1].equals("Bianco"))
				colors[0] = Color.WHITE;
			else
				colors[0] = Color.rgb(200, 200, 200);
			
			if(values[2].equals("Blue") || values[2].equals("Blu"))
				colors[1] = Color.BLUE;
			else if(values[2].equals("Black") || values[2].equals("Nero"))
				colors[1] = Color.BLACK;
			else if(values[2].equals("Red") || values[2].equals("Rosso"))
				colors[1] = Color.RED;
			else if(values[2].equals("Green") || values[2].equals("Verde"))
				colors[1] = Color.GREEN;
			else if(values[2].equals("Cyan") || values[2].equals("Ciano"))
				colors[1] = Color.CYAN;
			else if(values[2].equals("Magenta"))
				colors[1] = Color.MAGENTA;
			else if(values[2].equals("Yellow") || values[2].equals("Giallo"))
				colors[1] = Color.YELLOW;
			else if(values[2].equals("White") || values[2].equals("Bianco"))
				colors[1] = Color.WHITE;
			else
				colors[1] = this.colors[i%8];
			
		}else{
			colors[0] = Color.rgb(200, 200, 200);
			colors[1] = this.colors[i%8];
		}
		
		return colors;
	}

}
