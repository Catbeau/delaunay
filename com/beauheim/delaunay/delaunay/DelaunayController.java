/*
 * DelaunayController.java
 * copyright 2013 Catherine Beauheim
 * This implementation of the Delaunay Triangulation is based on the description in Numerical Recipes, Third Edition.
 * The data are 100 random numbers between 100 and 199 and 100 random numbers from the Gaussian distribution
 * that are between 300 and 500.  The first four data points are the four corners of an 800 x 800 pixel
 * canvas.  The bounding triangle is calculated but is not shown on the screen.
 * The result is written to delaunay.html to view.  
 */

package com.beauheim.delaunay.delaunay;


//import com.beauheim.delaunay.drawing.DrawingFrame;

import java.util.Random;


/**
 * $Id: Exp $
 * @author cate
 */
public class DelaunayController {

   // private float[][] data;
    //private DrawingFrame frame;
    private Delaunay delaunay;
    private int startdata=4;
    private int NPTS=100;
    		

    DelaunayController() {
    	float[][]mydata = initData();
       
        compute(mydata);
    }
    
    private float[][] initData(){
    	float [][] mydata = new float[NPTS*2+startdata][2];
    	Random random = new Random (314128759);
    	//for the purpose of this demonstration, I am putting the four
    	//corner points of the screen in mydata.  
    	mydata[0][0] = random.nextFloat();//was 1,2,799,800
    	mydata[0][1] = random.nextFloat();
    	mydata[1][0] = random.nextFloat();
    	mydata[1][1] = random.nextFloat() + 800;
    	mydata[2][0] = random.nextFloat() + 800;
    	mydata[2][1] = random.nextFloat();
    	mydata[3][0] = random.nextFloat()+800;
    	mydata[3][1] = random.nextFloat()+800;
    	
        for (int i=startdata; i < NPTS+startdata; i++){
            mydata[i][0] = random.nextFloat()*100+100;
            mydata[i][1] = random.nextFloat()*100+100;
            //System.out.println (i + ". "+ mydata[i][0] + ","+ mydata[i][1]);
            mydata[i+NPTS][0] = (float)random.nextGaussian()*100 +400;
            mydata[i+NPTS][1] = (float)random.nextGaussian()*100+400;
        
        }
        
        return mydata;
    }
    
    private void compute(float[][]mydata) {
    	delaunay = new Delaunay (mydata);
    	delaunay.printHtmlFile ("./delaunay.html");
    	System.out.println ("Done");
    }
    
  
    public static void main (String[] args){
    	
   
         new DelaunayController();
    }

}
