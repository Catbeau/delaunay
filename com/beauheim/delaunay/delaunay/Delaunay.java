/*
 * Copyright 2013 Catherine Beauheim
 * Delaunay.java
 * 
 */

package com.beauheim.delaunay.delaunay;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import com.beauheim.delaunay.delaunay.Triangle.Circle;

/**
 * $Id: Exp $
 * @author cate
 */
public  class Delaunay {

    protected TriangleTree tree;
    /**
     * These hash tables.  lineHash :
     * for vertices A, B, C.  Get vertex A based on the hash key hashCode(B) - hashCode(C)
     * triangleHash:  The key for triangle is hashCode(A) ^ hashCode(B) ^ hashCode(C)
     *
     */
    protected HashMap <Long, MyPoint> lineHash = new HashMap<Long, MyPoint>();
    protected HashMap <Long, Triangle> triangleHash = new HashMap<Long, Triangle>();
    private Map <Long,MyPoint[]>allLines = Collections.synchronizedMap(new HashMap<Long, MyPoint[]>());
   // private Onion onion;
    //private DrawingFrame frame;
 //   private ArrayList<Triangle> triangleQueue = new ArrayList<Triangle>();
    private ArrayDeque <Triangle> triangleStack = new ArrayDeque<Triangle>();
    private static int A=0;
    private static int B=1;
    private static int C=2;
    private int nextTask=0;
    public static boolean DEBUG_1 = false;
    private int npts;
   
    
   private float delx, dely;
   
    private float[][] data;

   //private MyPoint[] boundingBox;
    private MyPoint[] boundingtri;
 
    static final float fuzz  = 1.0e-6F;
    static final float bigscale = 10.F;
    Random randomfuzz = new Random(314159265);
    private float minx=1000000000, maxx=0;
    private float miny=1000000000, maxy=0;
    private Map<Long, MyPoint> pointList = new HashMap<Long, MyPoint>();
   
      		

    

    
    public Delaunay ( float[][] data){
       // System.out.println (data.length + "  " +data[0].length);
        this.data = data;
        
        boundingtri = init();   
        processData(data);
   
        
        /**Onion onion = new Onion (tree, pointList, allLines);
        onion.assignLevelsByPoints();**/
        printLines();
       // tree.printTree();
      
    }
    
   
    private MyPoint[] init(){
        for (int i=0; i < data.length; i++){
            if (data[i][0] < minx)minx=data[i][0];
            else if (data[i][0] > maxx) maxx = data[i][0];
            if (data[i][1] < miny) miny = data[i][1];
            else if (data[i][1] > maxy) maxy= data[i][1];
            
        }
        
        MyPoint[] trifp = createBoundingBox (minx, maxx, miny,  maxy);
       
        for (int i=0; i < trifp.length; i++){
            if (trifp[i].getX() < minx) minx=trifp[i].getX();
            if (trifp[i].getY() > maxx) maxx=trifp[i].getX();
            if (trifp[i].getY() < miny) miny = trifp[i].getY();
            if (trifp[i].getY() > maxy) maxy = trifp[i].getY();
        }
        
         
        try{
	        Triangle tri = new Triangle (trifp[0], trifp[1], trifp[2]);
	        trifp[0].setName("A");
	        trifp[1].setName("B");

	        trifp[2].setName("C");

	        pointList.put(trifp[0].getId(), trifp[0]);
	        pointList.put(trifp[1].getId(), trifp[1]);
	        pointList.put(trifp[2].getId(), trifp[2]);
	        tree = new TriangleTree(tri);
	    
	       /** frame.addPoint (new MyPoint (470, 10));
	        frame.addPoint (new Point2D (10, 540));
	        frame.addPoint (new Point2D (700, 810));**/
	        addNewLine (trifp[A],trifp[B]);
	        addNewLine (trifp[B], trifp[C]);
	        addNewLine (trifp[C], trifp[A]);
	        hashATriangle (tri);
	  }catch (Circle.ColinearPointsException e){
		  System.out.println(e.getMessage());
		  return null;
	  }
        return trifp;
    }

     


    private MyPoint[] createBoundingBox(float minx, float maxx, float miny, float maxy){
        boundingtri = new MyPoint[3];
        delx = maxx - minx;
        dely = maxy - miny;
        float x1 = (float)0.5 * (minx + maxx);
        float y1 = maxy + bigscale * dely;
        boundingtri[0] = new MyPoint (x1, y1 );
        boundingtri[0].setLevel(0);
        
        
        float x2 =minx- (float)0.5* bigscale*delx;
        float y2 = miny- (float)0.5*bigscale*dely;
        boundingtri[1] = new MyPoint (x2, y2);
        boundingtri[1].setLevel(0);

        
        float x3 = maxx + (float) 0.5*bigscale * delx;
        float y3 = miny - (float) 0.5*bigscale * dely;
        boundingtri[2] = new MyPoint (x3, y3);
        boundingtri[2].setLevel(0);

        
        if (DEBUG_1){
	        System.out.println ("  Bounding Triangle ");
	        for (int i=0; i < boundingtri.length; i++){
	            System.out.println (boundingtri[i].toString());
	
	        }
        }

        return boundingtri;

    }
    /**
     * 
     * @param data
     * @param trifp  This is the bounding box. 
     */
    public void processData (float[][] data) {

    	npts = data.length;
        for (int i=0; i < npts; i++){
            
            MyPoint fp = new MyPoint (data[i][0], data[i][1]);
            fp.setName(""+ i);
            pointList.put(fp.getId(), fp);
          
            addNewPoint (fp);
            
        
        }
        
    }
  

/**
 * 
 * @param pt
 * From the list of points, add them one at a time.  Find the containing triangle, make the new
 * triangles if the distance is correct, adding the new triangles.  If the distance is not correct,
 * then do the edge flip, add the new triangles to the testing queue and continue until the change
 * has stopped propagating.
 */
    private void addNewPoint (MyPoint pt){
        //find the containing triangle

        nextTask = 0;
        triangleStack = null;
        triangleStack = new ArrayDeque<Triangle>();
        Triangle containing = tree.containingTriangle (pt);
        
        if (containing == null){
            System.out.println ("Oh no -- There is no containing triangle.  "+ pt.toString());
            //try to fuzz it three times before returning
            int j = 0;
            while (j < 2 && containing == null){
            	System.out.println ("fuzzing here " + pt.toString());
            	float fuzzx = (float)(fuzz * delx * randomfuzz.nextFloat() -0.5);
            	float fuzzy = (float)(fuzz * dely * randomfuzz.nextFloat() - 0.5);
            	j++;
            	pt.setX(  pt.getX()+fuzzx);
            	pt.setY( pt.getY()+fuzzy);
            	containing = tree.containingTriangle (pt);
            	
            }
            if (j == 2){
            	System.out.println ("even after fuzzing , no containing triangle found. "+pt.toString());
            return;
            }
        }
        MyPoint[][] newlines = containing.makeDaughters (pt);
        for (int j=0; j < newlines.length; j++){
        	addNewLine (newlines[j][0], newlines[j][1]);
        }
        Triangle[] daughters = containing.getDaughters();
        
        for (int i=0; i < daughters.length; i++){
            hashATriangle (daughters[i]);
          //  triangleQueue.add (daughters[i]);
            triangleStack.push(daughters[i]);
         
        }

        //erase the old triangle
        //update the hash table
        Long key = new Long (containing.getId());
        if (triangleHash.containsKey(key)){
            Triangle t = triangleHash.get (key);
            t.markLiveStatus (false);
            
        }


        //while there are triangles to test
      //  while (nextTask < triangleQueue.size()){
          while ( !triangleStack.isEmpty()){

            Triangle newone=null;
            Triangle newtwo=null;
            // look up the 4th point.  points[A] is this pt what we are testing against
            MyPoint fourth = null;
           // Triangle testTri = triangleQueue.get(nextTask);
            Triangle testTri = triangleStack.pop();
         
            if (testTri.getLiveStatus()) {
                MyPoint[] testpts = testTri.getPoints();
                Long keypt = makeHashCodeForLine (testpts[C], testpts[B]);
            
	            if (lineHash.containsKey (keypt)) {
	                 fourth = lineHash.get (keypt);
	                 if (fourth.equals(testpts[A])){
	                	 System.out.println ("  This is an error !! ");
	                 }
	                if (testForDistance (fourth, testTri)){
	                    //create two new triangles
	                	try{
		                    newone = new Triangle(testpts[A], testpts[B], fourth);
		                    newtwo = new Triangle (testpts[A], fourth, testpts[C]);  
		                    
	                	}catch (Circle.ColinearPointsException e){
	                		System.out.println ("(1) " +e.getMessage());
	                	}
	                	if (newone != null && newtwo != null ){
		                    addNewLine (testpts[A], testpts[B]);
		                    addNewLine (testpts[B], fourth);
		                    addNewLine (fourth, testpts[A]);
		//                    System.out.println ("new one ? " + newone.toString());
		                    hashATriangle (newone);
		                
		                    addNewLine (testpts[A], fourth);
		                    addNewLine (fourth, testpts[C]);
		                    addNewLine (testpts[C], testpts[A]);
		                    hashATriangle (newtwo);             
     
		                    //erase the two old triangles and add the new daughters
		                    key = new Long (testTri.getId());
		                    eraseTriangle(key, testTri, newone, newtwo);
		                    checkTheQueue (key, testTri, nextTask);
		                	
		                    Triangle oldtriangle = getHashTriangle (fourth, testpts[C], testpts[B]);
   
		                    if (oldtriangle != null) {
		                	    oldtriangle.markLiveStatus(false);
		                        key = oldtriangle.getId();
		                        eraseTriangle(key, oldtriangle, newone, newtwo);
		                        checkTheQueue (key, oldtriangle, nextTask);
		                    }
	                  
		                //erase the line in both directions
		                   removeLineFromHash (testpts[B], testpts[C]);
	
		                //add two new triangles to the queue.
		   
		                   triangleStack.push (newone);
		                   triangleStack.push (newtwo);
		                }
	                }
   
	            }
            }

        }

    }
    
    
    /**  
     * Given 2 new points, create a new line and add it to the hashmap of lines
     **************************************************************************
     */
    private void addNewLine (MyPoint A, MyPoint B){
    	if (A != null && B != null){
	    	Long key = makeHashCodeForLine (A, B);
	    	Long keyneg = 0-key;
	    	MyPoint[] newline={A,B};
	    	if (!allLines.containsKey(key) && !allLines.containsKey(keyneg)){
	    		allLines.put(key, newline);
	    		//System.out.println ("addNewLine "+ ptToString(A) + ", "+ ptToString(B));
	    	}
	    	
    	}
    	
    }
    
    /**
     *  this point is the third in the triangle  
     *  */
    public MyPoint getLineHash (MyPoint a, MyPoint b){
    	
    	
        MyPoint point = null;
        Long hashcode = new Long (a.hashCode() - b.hashCode());

        if (lineHash.containsKey (hashcode)){
            point = lineHash.get (hashcode);
        }
        
   
        return point;
        
    }
    /**
     * 
     * I think this check is not necessary.  
     * @param key
     * @param tri
     * @param nextTask
     */
    private void checkTheQueue (Long key, Triangle tri, int nextTask){
    	
    	Iterator<Triangle> it = triangleStack.iterator();
    	if (DEBUG_1){
	    	System.out.println("Check the Queue ");
	    	System.out.println ("\t" + tri.toString() + tri.getId());
    	}
    	while (it.hasNext()){
    		Triangle qtri = it.next();
    		//System.out.println (qtri.toString() + qtri.getId());
    		
    		if (qtri.getId() == tri.getId()){
    			qtri.setLiveStatus(false);
    			return;
    		}
    	}
    	
    }
    
    /**
     *   Inactivate it in the TriangleTree after setting
     *  its daughters.  
     */
    private void eraseTriangle (Long key, Triangle old, Triangle daughter1, Triangle daughter2){
    	
    	
    	if (old!= null){
	    	old.markLiveStatus( false);
	    	old.makeOneDaughter(daughter1);
	    	old.makeOneDaughter(daughter2);
	    	
    	}	
  
    	
    }
    /**
     * 
     * @param tri
     * Add the new triangle to the HashMap of triangles.
     */
    public final void hashATriangle(Triangle tri){
        Long hint = new Long (tri.getId());
        triangleHash.put (hint, tri);
        MyPoint[] points = tri.getPoints();
        Long linehash = makeHashCodeForLine (points[A], points[B]);
        lineHash.put (linehash, points[C]);
        linehash = makeHashCodeForLine (points[B], points[C]);
        lineHash.put (linehash, points[A]);
        linehash = makeHashCodeForLine (points[C], points[A]);
        lineHash.put (linehash, points[B]);


    }
    
   

    public void removeLineFromHash (MyPoint a, MyPoint b){

    	if (DEBUG_1){
	        System.out.println ("..........removeLine from hash " + ptToString(a) + ","+ ptToString(b));
	        System.out.println ("............................." + a.hashCode() + ", " + b.hashCode());
    	}

        Long keyi = new Long (a.hashCode() - b.hashCode());
        
        if (lineHash.containsKey (keyi)){
           MyPoint p = lineHash.remove(keyi);
         
        }
        else
            System.out.println (" This key does not exist in the line hash " + keyi.toString());
        
        //remove from the list of all lines
        if (allLines.containsKey(keyi)){
        	allLines.remove(keyi);
        }

        keyi = new Long (b.hashCode() - a.hashCode());
       
        
        if (lineHash.containsKey (keyi)){

            MyPoint p = lineHash.remove (keyi);
         //   frame.eraseLine (b, a);

        }
        else {
            System.out.println ("This key does not exist in the lineHash " + keyi.toString());
        }
        //remove from the list of all lines
        if (allLines.containsKey(keyi)){
        	allLines.remove(keyi);
        }
        
  
    }

    /*
     * Test the radius of this triangle to the point.  Returns true
     * when the distance to the pt is less than the radius, thus
     * signaling the need to flip the edges.
     * positive is inside
     * 0 is on the line
     * negative is outside of the circle.
     * point d, point a, b,c
     * Circle cc = circumcircle(a,b,c);
	Doub radd = SQR(d.x[0]-cc.center.x[0]) + SQR(d.x[1]-cc.center.x[1]);
	return (SQR(cc.radius) - radd);
     */
    private boolean testForDistance (MyPoint pt, Triangle onetri){
        boolean flag= false;
        double radius = 0;
        double distance = 0.;
        double radd = 0.;

        if (pt != null){
        	if(onetri.getCircle() == null)
        		return false;
            radius = onetri.getCircle().getRadius();
            MyPoint center = onetri.getCircle().getCenter();
            if (center == null){
                System.out.println ("  For some reason, this triangle has no center. " + onetri.toString());
                return false;
            }
            radd = (pt.getX() - center.getX())*(pt.getX()-center.getX())+ (pt.getY()-center.getY())*(pt.getY()-center.getY());
            distance = radius*radius - radd;
            if (distance == 0){
                System.out.println ("Points are co-linear!!!");
            }
            if (distance > 0)
                flag = true;
        }
     //   System.out.println ("testForDistance "+ flag + " radius = "+radius + ", distance = "+ distance + ", distance pt to center " + radd);
        return flag;

    }
    
  
    /**
     * print the lines in a format that my R script can read for display
     */
    private void printLines() {
    	
    //	System.out.println (lineHash.size());
    	Set<Long> keys = lineHash.keySet();
    	Iterator<Long>it = keys.iterator();
    	//MyPoint []pts = (MyPoint[]) allLines.get (key);
    	File f = new File ("./printRR.r");
        FileOutputStream fw = null;
       // printDataFrame
        try{
	        	fw= new FileOutputStream (f);
	        	String h = "x1\ty1\tLevel1\tx2\ty2\tLevel2\n";
	        	//System.out.print(h);
	            fw.write(h.getBytes());
	            //include the bounding triangle
	            while(it.hasNext()){
	            	Long key = it.next();
	            	if (allLines.containsKey(key)){
		        		MyPoint[] pts = allLines.get(key);
		        		if (pts != null && inBounds(pts)){
		        		    StringBuilder buf = new StringBuilder (pts[0].getX()+"\t"+pts[0].getY());
		        		    buf.append("\t").append(pts[0].getLevel());
			        		buf.append("\t").append(pts[1].getX()).append("\t").append(pts[1].getY());
			        		buf.append("\t").append(pts[1].getLevel()).append("\n");
			        		
			                fw.write(buf.toString().getBytes());
		        		}
	            	}
	            }
	        
	        	fw.close();
        	
        } catch (IOException e){
            System.out.println (" file io exception ");
        } finally {
            
        }
    	
    	
    }
    /**
     * 
     * @param fn
     * Given a filename, print the results as an html file for display.
     * ctx.fillStyle="#FF0000";
ctx.beginPath();
ctx.arc(50, 25, 4, 0,2*Math.PI);
ctx.fill();
ctx.stroke();
     */
    public void printHtmlFile(String fn) {
    	Set<Long> keys = lineHash.keySet();
    	Iterator<Long>it = keys.iterator();
    	System.out.println (fn);
    	//MyPoint []pts = (MyPoint[]) allLines.get (key);
    	File f = new File (fn);
        FileOutputStream fw = null;
       try{
        	fw= new FileOutputStream (f);
        	//
        	StringBuilder buf = new StringBuilder();
        	buf.append ("<!DOCTYPE html>\n");
        	buf.append("<html>\n");
        	buf.append("<body>\n");
        	buf.append("<canvas id='mycanvas' width='800' height='800' style='border:1px solid #000000; ");
        	buf.append("'>\n");
            buf.append("</canvas>\n");
            
            buf.append("<script>\n");
            buf.append("var c=document.getElementById('mycanvas');\n");
            buf.append("var ctx = c.getContext('2d');\n");
            buf.append("ctx.fillStyle='#FF0000';\n");
            while (it.hasNext()){
            	Long key = it.next();
            	if (allLines.containsKey(key)){
            		MyPoint[] pts = allLines.get(key);
            		if (pts != null && inBounds(pts)){
            			buf.append ("ctx.moveTo(").append(pts[0].getSX()).append(",").append(pts[0].getSY()).append(");\n");
            			buf.append("ctx.lineTo(").append(pts[1].getSX()).append(",").append(pts[1].getSY()).append(");\n");
            			buf.append("ctx.stroke();\n");
            			/**if (pts[0].getLevel() > 1){
            				buf.append ("ctx.beginPath(); \nctx.arc(").append(pts[0].getSX()-1);
            				buf.append(", ").append(pts[0].getSY()-1).append(",3,0,2*Math.PI);\n");
            				buf.append("ctx.fill();\nctx.stroke()\n");
            			}
            			if (pts[1].getLevel() > 1){
            				buf.append ("ctx.beginPath();\n ctx.arc(").append(pts[1].getSX()-1);
            				buf.append(", ").append(pts[1].getSY()-1).append(",3,0,2*Math.PI);\n");
            				buf.append("ctx.fill();\nctx.stroke()\n");
            			}**/
            		}
            	}
            }
            buf.append("</script>\n");
        	buf.append("</body>\n");
        	buf.append("</html>\n");
        	//System.out.println (buf.toString());

            fw.write(buf.toString().getBytes());
            //include the bounding triangle
          
        
        	fw.close();
    	
    } catch (IOException e){
        System.out.println (" file io exception ");
    } finally {
        System.out.println (" finally");
    }
    System.out.println ("finished with printing html file");    
    	
    }
    
 
  
    
    /*
     * ---------------------------------------------------------------------
     */
    
    
    public float[][] transformToLog (float[][]data){
        float[][] logdata = new float[data.length][data[0].length];
        System.out.println (" ------------- tranformToLogi-----------");
        for (int i=0; i < data.length; i++){
             for (int j=0; j < data[i].length; j++){
                 logdata[i][j] = (float) Math.log(data[i][j]);
                // System.out.println (data[i][j] + "  "+ logdata[i][j]);
             }
             
        }
        return logdata;
    }
    
    
    
  
   /**
    * 
    * @param a
    * @param b
    * @param c
    * @return
    */

    public Triangle getHashTriangle (MyPoint a, MyPoint b, MyPoint c) {
        Triangle tri = null;
//        System.out.println ("getHashTriangle ");
        
        
        long hashcode = a.hashCode() ^ b.hashCode() ^ c.hashCode();
//        System.out.print ("GetHashTriangle Hashcode = " + hashcode + a.toString() + ", "+ b.toString() + ", " + c.toString());
        if (triangleHash.containsKey (new Long (hashcode))){
            tri = triangleHash.get (new Long (hashcode));
            if (tri == null)
               System.out.println (" No triangle found for key = " + hashcode);
        }
        return tri;
    }
    
    /**
     * Called when printing.  If not inbounds, the line is not printed
     * @param line
     * @return
     */
    
    private boolean inBounds(MyPoint[] line){
    	boolean in = true;
    	if (line[0].getLoc() == MyPoint.OUT){
    		in = false;
    	}
    	else if (line[1].getLoc() == MyPoint.OUT){
    		in = false;
    	}
    	
    	return in;
    }

    private Long makeHashCodeForLine (MyPoint a, MyPoint b){
        int h = 0;

        h = a.hashCode() - b.hashCode();
        return new Long (h);
    }
    
   
     private String ptToString(MyPoint pt) {
         String s = "("+pt.getName() + ": " + pt.getX()+","+pt.getY()+")";
         return s;
     }
     
    
}
