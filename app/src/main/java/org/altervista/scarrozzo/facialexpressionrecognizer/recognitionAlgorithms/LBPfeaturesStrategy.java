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

import java.nio.ByteBuffer;

import com.googlecode.javacv.cpp.opencv_core.IplImage;


public final class LBPfeaturesStrategy implements FeaturesStrategy{

	private int radius;
	private int numPoints;
	private int lookup[];
	
	public LBPfeaturesStrategy(){
		this.radius = 1;
		this.numPoints = 8;
		this.lookup = new int[]{
				0, 1, 2, 3, 4, 58, 5, 6, 7, 58, 58, 58, 8, 58, 9, 10,
				11, 58, 58, 58, 58, 58, 58, 58, 12, 58, 58, 58, 13, 58, 14, 15,
				16, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58,
				17, 58, 58, 58, 58, 58, 58, 58, 18, 58, 58, 58, 19, 58, 20, 21,
				22, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58,
				58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58,
				23, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58,
				24, 58, 58, 58, 58, 58, 58, 58, 25, 58, 58, 58, 26, 58, 27, 28,
				29, 30, 58, 31, 58, 58, 58, 32, 58, 58, 58, 58, 58, 58, 58, 33,
				58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 34,
				58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58,
				58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 35,
				36, 37, 58, 38, 58, 58, 58, 39, 58, 58, 58, 58, 58, 58, 58, 40,
				58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 58, 41,
				42, 43, 58, 44, 58, 58, 58, 45, 58, 58, 58, 58, 58, 58, 58, 46,
				47, 48, 58, 49, 58, 58, 58, 50, 51, 52, 58, 53, 54, 55, 56, 57 };
	}
	
	@Override
	public double[] getFeatures(IplImage srcImg, IplImage lbpImage) {
		return histUniform(srcImg, lbpImage);
	}
	
	// hist of one subregion using classic LBP
	private int[] hist(IplImage srcImg, IplImage lbpImage) {

		int features[] = getLBPFeatures(srcImg, lbpImage);

		int hist[] = new int[256];
		for (int i = 0; i < features.length; i++)
			hist[features[i]]++;

		return hist;
	}
	
	// hist of one subregion using uniform LBP
	private double[] histUniform(IplImage srcImg, IplImage lbpImage) {

		int features[] = getLBPFeatures(srcImg, lbpImage);

		double hist[] = new double[59];
		int index;
		for (int i = 0; i < features.length; i++) {
			index = lookup[features[i]];
			if (index != 58)
				hist[index]++;
			else
				hist[58]++;
		}

		// normalize histogram
		for (int i = 0; i < hist.length; i++)
			hist[i] /= features.length;


		return hist;
	}
	
	
	// LBP features of one subregion
	private int[] getLBPFeatures(IplImage srcImg, IplImage lbpImage) {

		int features[] = new int[(srcImg.width() - 1) * (srcImg.height() - 1)];
		int k = 0;
		
		ByteBuffer bufferIpl = srcImg.getByteBuffer();
		ByteBuffer bufferLbp = null;
		if(lbpImage!=null)
			bufferLbp = lbpImage.getByteBuffer();
		
		int center = 0;   
		int center_lbp = 0;  
		
		for(int y = 1; y < srcImg.height()-1; y++) {
		    for(int x = 1; x < srcImg.width()-1; x++) {
		        int index = y * srcImg.widthStep() + x * srcImg.nChannels();

		        // Used to read the pixel value - the 0xFF is needed to cast from
		        // an unsigned byte to an int.
		        center = bufferIpl.get(index) & 0xFF;

		        center_lbp = 0;   
			    
			    if ( center <= (bufferIpl.get((y-1)*srcImg.widthStep()+(x-1)*srcImg.nChannels())& 0xFF) )   
			      center_lbp += 1;   

			    if ( center <= (bufferIpl.get((y)*srcImg.widthStep()+(x-1)*srcImg.nChannels())& 0xFF) )   
			      center_lbp += 2;   

			    if ( center <= (bufferIpl.get((y+1)*srcImg.widthStep()+(x-1)*srcImg.nChannels())& 0xFF) )   
			      center_lbp += 4;   

			    if ( center <= (bufferIpl.get((y+1)*srcImg.widthStep()+(x)*srcImg.nChannels())& 0xFF) )
			    	center_lbp += 8;
			    
			    if ( center <= (bufferIpl.get((y+1)*srcImg.widthStep()+(x+1)*srcImg.nChannels())& 0xFF) )   
			        center_lbp += 16;  
			    
			    if ( center <= (bufferIpl.get((y)*srcImg.widthStep()+(x+1)*srcImg.nChannels())& 0xFF) )   
			        center_lbp += 32; 
			    
			    if ( center <= (bufferIpl.get((y-1)*srcImg.widthStep()+(x+1)*srcImg.nChannels())& 0xFF) )   
			        center_lbp += 64; 
			    
			    if ( center <= (bufferIpl.get((y-1)*srcImg.widthStep()+(x)*srcImg.nChannels())& 0xFF) )   
			        center_lbp += 128; 
		        
			    features[k++] = center_lbp;
			    if(bufferLbp!=null)
			    	bufferLbp.put(index, (byte)center_lbp);
		    }
		}
		
		return features;
	}

}
