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
import static com.googlecode.javacv.cpp.opencv_core.cvLoad;
import static com.googlecode.javacv.cpp.opencv_core.cvSize;
import static com.googlecode.javacv.cpp.opencv_objdetect.CV_HAAR_DO_CANNY_PRUNING;
import static com.googlecode.javacv.cpp.opencv_objdetect.cvHaarDetectObjects;
import static com.googlecode.javacv.cpp.opencv_core.cvClearMemStorage;
import static com.googlecode.javacv.cpp.opencv_core.cvGetSeqElem;
import static com.googlecode.javacv.cpp.opencv_core.cvRect;
import static com.googlecode.javacv.cpp.opencv_core.cvSetImageROI;
import static com.googlecode.javacv.cpp.opencv_core.cvCopy;
import static com.googlecode.javacv.cpp.opencv_core.cvResetImageROI;



import android.util.Log;

import com.googlecode.javacv.cpp.opencv_core.CvMemStorage;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_objdetect.CvHaarClassifierCascade;
import com.googlecode.javacv.cpp.opencv_core.CvScalar;




public final class HaarFaceDetectionStrategy implements FaceDetectionStrategy{
	
	private String classifier;
	private static CvScalar colors[] = new CvScalar[]{CvScalar.BLUE, CvScalar.BLACK, CvScalar.RED, 
		CvScalar.GREEN, CvScalar.CYAN, CvScalar.MAGENTA, CvScalar.YELLOW, CvScalar.WHITE, };

	public HaarFaceDetectionStrategy(String classifier){
		this.classifier = classifier;
	}
	
	@Override
	public IplImage[] detectAndCropFace(IplImage srcImg) {
		
	    /*	begin face detections	*/
	    CvHaarClassifierCascade cascade = new CvHaarClassifierCascade(cvLoad(classifier));
	    
	    CvMemStorage storage = CvMemStorage.create();
	    
		CvSeq sign = cvHaarDetectObjects(srcImg, cascade, storage, 
										 1.1, 3, CV_HAAR_DO_CANNY_PRUNING);

		cvClearMemStorage(storage);
		
		// total number of faces
		int total_Faces = sign.total();
		/*	end face detections	*/
		
		if(total_Faces>0){
			
			IplImage[] faces = new IplImage[total_Faces];
			
			for(int i = 0; i < total_Faces; i++){
				// crop face
				CvRect r = new CvRect(cvGetSeqElem(sign, i));
				
				cvSetImageROI(srcImg, cvRect(r.x(), r.y(), r.width(), r.height()));
				IplImage img3 = cvCreateImage(cvSize(r.width(), r.height()),
	                    srcImg.depth(),
	                    srcImg.nChannels());
				cvCopy(srcImg, img3);
				cvResetImageROI(srcImg);
				// end crop

				faces[i] = img3;
				
			}
			
			return faces;
		}else{
			return null;
		}
		
	}
	
	public static CvScalar[] getColors() {
		return colors;
	}

}
