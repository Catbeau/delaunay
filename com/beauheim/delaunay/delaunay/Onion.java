package com.beauheim.delaunay.delaunay;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/**
 * Now I have the triangles.  I have to assign the level points.  Then from the bottom up,
 * find the triangles/convex hulls that have different levels.  Those points that are of
 * the same level -- that is the same number of steps to the outside -- are in the same
 * cluster.  The points/lines that have different levels are the boundary points between
 * the clusters.  
 * @author cate2
 *
 */
public class Onion {
	
	Map<Long, MyPoint[]> lineList;
	TriangleTree tree;
	Map <Long, MyPoint> pointList = new HashMap<Long,MyPoint>();
	static int A=0;
	static int B=1;
	static int C=2;
	static int ONHULL=0;
	static int INSIDE=-1;
	static int POINT_ONE=1;
	static int POINT_TWO=2;
	static int NOT_ASSIGNED=-1;
	
	ConvexHullTwo convexHull = new ConvexHullTwo();
	//static int 
	ArrayDeque <Triangle> queue = new ArrayDeque<Triangle>();
	ArrayDeque <MyPoint> pointQueue = new ArrayDeque<MyPoint>();
	
	protected class LevelCompare implements Comparator<OneLine> {
        @Override
        public int compare(OneLine o1, OneLine o2) 
        {
                return (new Integer(o1.one.getLevel())).compareTo(new Integer(o2.one.getLevel()));
        }
}
	
	class OneLine implements Comparable { //throw ClassCastException{
		MyPoint one, two;
		Long id;
		int status;
		
		OneLine (MyPoint[] line){
			one = line[0];
			two = line[1];
			id = makeHashCodeForLine(one,two);
			status=NOT_ASSIGNED;
		}
		
		OneLine (MyPoint A, MyPoint B){
			one = A;
			two = B;
			id = makeHashCodeForLine(one,two);
			status=NOT_ASSIGNED;

		}
		@Override
		/*This is sorting by the first point.  Sorting by Name*/
		public int compareTo(Object arg0) {
			// TODO Auto-generated method stub
			if (arg0 instanceof OneLine){
				OneLine line = (OneLine) arg0;
				return (one.compareTo(line.one));
			}
			else if (arg0 instanceof MyPoint){
				MyPoint pt = (MyPoint) arg0;
				return (one.compareTo(pt));
			}
			return 0;
			
			
		}
		private Long makeHashCodeForLine (MyPoint a, MyPoint b){
	        int h = 0;

	        h = a.hashCode() - b.hashCode();
	        return new Long (h);
	    }

		
		public String toString() {
			StringBuilder buf = new StringBuilder (one.toString()).append(", ").append(two.toString());
			return buf.toString();
		}
		
		/*  Return 0 if this line is a boundary line.
		 *  Return 1 one point is a boundary and one is inside
		 *  Return -1 when neither point is a boundary point
		 *  ONHULL=0;
	     *  INSIDE=-1;
	     *  POINT_ONE=1;
	     *  POINT_TWO=2;
		 *  */
		protected int onBoundary (OneLine[] boundary){
			
			int i=0;
			int flag = INSIDE;
			while ( i < boundary.length){
				System.out.println (boundary[i].id + ", "+ this.id);
				if (Math.abs(boundary[i].id) == Math.abs(this.id) ){
					flag = ONHULL;
					break;
				}
				if (one.getName().equals (boundary[i].one.getName()))
					flag=POINT_ONE;
				else if (one.getName().equals(boundary[i].two.getName())){
					flag = POINT_TWO;
					
				}
				i++;
			}
			return flag;
		}
	}
	
	
	public Onion ( TriangleTree tree,  Map<Long, MyPoint> pointList, Map<Long, MyPoint[]> lineList){
		
		this.pointList = pointList;
		this.lineList = lineList;
		this.tree = tree;
		//setLevels();
		//constructHull();
		//assignLevelsByPoints();
		
		//printLineList();
	}
	

	
	
	
	private void printResults (ArrayList<String> results){
		FileOutputStream fw=null;
	   try{
		   File f = new File ("./DataOut/hull.txt");
    	   fw= new FileOutputStream (f);
    	   String h = "x1\ty1\tx2\ty2\tLevel\n";
    	   fw.write(h.getBytes());
    	   for (String s: results){
    		   fw.write(s.getBytes());
    	   }
    	   fw.close();
	   } catch (IOException e){
        System.out.println (" file io exception ");
	   } finally {
        
	   }	
	}
	
	
	/**
	private ArrayList<MyPoint> linesConnectedToTriangle(Triangle t, Map<Long, MyPoint[]> lineList){
		
		ArrayList<MyPoint> connectingPoints = new ArrayList<MyPoint>();
		Collection <MyPoint[]> lines = lineList.values();
		
		MyPoint[] tpoints = t.getPoints();
		OneLine[] trilines = new OneLine[6];
		for (int i=0; i < 3; i++){
			int j=i*2;
			trilines[j]= new OneLine (tpoints[i], tpoints[(i+1)%3]);
			trilines[j+1] = new OneLine (tpoints[(i+1)%3], tpoints[i]);
		}
		
		System.out.println ("-----------------------------------------------------");
		System.out.println ("\t"+ tpoints[0].toString() + ", "+ tpoints[0].getLevel());
		System.out.println ( "\t " + tpoints[1].toString() + ", " +tpoints[1].getLevel());
		System.out.println ("\t"+ tpoints[2].toString() + ", "+ tpoints[2].getLevel());
		
		
	    OneLine[] mylines = new OneLine[lines.size()];
	    int i=0;
	    for (MyPoint[] pts : lines){
	    	mylines[i++] = new OneLine (pts[0], pts[1]);
	       // mylines[i++] = new OneLine (pts[1], pts[0]);
	    }
	    int index=0, low, high;
	    Arrays.sort(mylines);
	  //  MyPoint[] pt = new MyPoint[2];
	 //   for ( i=0; i < tpoints.length; i++){
	    for ( i=0; i < trilines.length; i++){
	    	//pt[0] = trilines[i].one;
	    	//pt[1] = trilines[i].two;
	    	index = Arrays.binarySearch(mylines, trilines[i]);
	    	if (index>-1 & index< mylines.length){
	    	low = index;
	    	while (mylines[index].one.getName().equals(mylines[low].one.getName()) && low > 0){
	    		low--;
	    		**if (low < 0) {
	    			System.out.println("Not found low is < 0 ");
	    			return null;
	    		}**
	    	}
	    	
	    	high = index;
	    	while (mylines[index].one.getName().equals(mylines[high].one.getName()) && high < mylines.length-1){
	    		high++;
	    		**if (high == mylines.length){
	    			System.out.println ("Not found, high is > mylines.length");
	    			return null;
	    		}**
	    	}
	    	System.out.println ("low, high "+ low + ", "+ high);
	    	
	    	for (int j=low+1; j < high; j++){
	    		if (notTrianglePoint (tpoints, mylines[j].two)){
	    			if (mylines[j].two.getLevel() == -1  ){
	    				mylines[j].two.setLevel(mylines[j].one.getLevel()+1);
	    			}
	    			connectingPoints.add(mylines[j].two);
	    		}
	    	}
	    	}
	    	else
	    		System.out.println("index is out of bounds for the line "+ trilines[i].toString());
	    }
	    
    
		return connectingPoints;
	}**/
	
	private boolean notTrianglePoint (MyPoint[] triangle, MyPoint pt){
		boolean flag=false;
		if (!pt.equals(triangle[0]) ){
			if (!pt.equals(triangle[1])){
				if (!pt.equals(triangle[2]))
					flag = true;
			}
		}
		//System.out.println ("Not a Triangle point returns "+ flag + "  " + triangle[0].toString() + ", "+ triangle[1].toString() + ", "+ triangle[2].toString() + ", "+ pt.toString());
		return flag;
	}
	
	
	
	private void newLevelAssignment() {
		MyPoint[]boundingBox = tree.treeRoot.getPoints();
		OneLine[] boundinglines = new OneLine[boundingBox.length];
		for (int i=0; i < boundingBox.length; i++){
			
			OneLine onel = new OneLine (boundingBox[i], boundingBox[(i+1)%boundingBox.length]);
			boundinglines[i] = onel;
		}
		Collection<MyPoint[]>lineValues =  lineList.values();
		OneLine[] lines = new OneLine[lineValues.size()*2];
		int i=0;
		/* Create an array of lines that is double the size of the line list because I
		 * are putting them in twice, once at one = A, two = B and one as the reverse.
		 * Because I am sorting them by the 'name' of the first point.  Then 
		 * Sort them by the 'first' point.*/
		for (MyPoint[] pts: lineValues){
			lines[i] = new OneLine (pts);
			i++;
			
			lines[i] = new OneLine(pts[1], pts[0]);
			i++;
		}
		Arrays.sort(lines);
		//ArrayList<MyPoint[]> levelone = new ArrayList<MyPoint[]>();
		for ( i=0; i < boundinglines.length; i++){
			/**MyPoint[] oneline = new MyPoint[2];
			oneline[0] = boundingBox[i];
			oneline[1] = boundingBox[(i+1)%boundingBox.length];
			OneLine onel = new OneLine (boundingBox[i],boundingBox[(i+1)%boundingBox.length] );
			boundinglines[i] = onel;*/
			System.out.println (boundingBox[i].toString());
		//	int index = Arrays.binarySearch(lines, boundingBox[i]);
			int index = Arrays.binarySearch(lines, boundinglines[i]);

			int[] range = getRange (lines, boundingBox[i].getName(), index);
			System.out.println (boundingBox[i].getName()  + " index is "+index + "  " + range[0] + " --- "+ range[1]);
			for (int j= range[0]; j <= range[1]; j++){
				int flag = lines[j].onBoundary(boundinglines);
				lines[j].status = flag;
				System.out.println ("flag is  "+flag + ", " + lines[j].toString());
					
			}
			
		}
		System.out.println ("  Lines sorted by name?  ");
		i=0;
		for (OneLine li: lines){
			System.out.println (i + ". " + li.toString() + "-->" + li.status);
			i++;
		}
	}
	
	private int[] getRange (OneLine[]lines, String name, int index ){
		int[] range = new int[2];
		int low = index;
		while (lines[low].one.getName().equals(name) && low >= 0) low--;
		range[0] = low+1;
		int high = index;
		while (lines[high].one.getName().equals(name)&& high < lines.length-1) high++;
		if (high == lines.length-1 && lines[high].one.getName().equals(name))
			range[1] = high;
		else
		    range[1] = high-1;
		
		return range;
	}
	
	
	
	protected void assignLevelsByPoints() {
		Collection<MyPoint[]>lineValues =  lineList.values();
		OneLine[] mylines = new OneLine[lineValues.size()];
		int i=0;
		Iterator<MyPoint[]> it = lineValues.iterator();
		while (it.hasNext()){
			MyPoint[] one = it.next();
			mylines[i] = new OneLine (one[0], one[1]);
			i++;
		}
	    
	    int index=0, low, high;
	    int level=0;
	    //pointQueue
	    Arrays.sort(mylines);  ///this is for lookup
	    //start with the points in the bounding box.
	  //  ClusterTree ctree = new ClusterTree (tree.treeRoot.getPoints());
	   // ctree.buildClusters ( mylines);
	   // System.out.println ("--------------------------------------------------------");
	   // ctree.printClusterTree();
	   // System.out.println ("--------------------------------------------------------");

	  //  ClusterNode clusterRoot = new ClusterNode(tree.treeRoot.getPoints(), mylines);
	    MyPoint[] boundingbox = tree.treeRoot.getPoints();
	    for (MyPoint pt: boundingbox){
	    	pt.setLevel(level);
	    	pointQueue.addLast(pt);
	    }
	    while (!pointQueue.isEmpty()){
	    	MyPoint pt = pointQueue.removeFirst();
	    	System.out.println (pt.toString());
	    	index = Arrays.binarySearch(mylines, pt); // this is not predictable, must find range.
	    	if (index >-1 && index < mylines.length){
	    		low = index;
	    		while (mylines[index].one.getName().equals (mylines[low].one.getName()) && low > 0)
	    			low--;
	    	
		    	high = index;
		    	while(mylines[index].one.getName().equals(mylines[high].one.getName())&& high < mylines.length-1)
		    			high++;
		    	//now I have the range
		    	System.out.println ("low, high "+ low + ", "+ high);
		    	for (int j=low+1; j < high; j++){
		    		
	    			if (mylines[j].two.getLevel() == NOT_ASSIGNED ){
	    				mylines[j].two.setLevel(mylines[j].one.getLevel()+1);
	    				pointQueue.addLast(mylines[j].two);
	    			}
	    			else if (mylines[j].two.getLevel()+1 < mylines[j].one.getLevel()){
	    				mylines[j].one.setLevel(mylines[j].two.getLevel()+1);
	    				pointQueue.addLast(mylines[j].one); // this value changed -- do others need to be reevaluated?
	    			}
	    			else if (mylines[j].two.getLevel() > mylines[j].one.getLevel()+1){
	    				mylines[j].two.setLevel(mylines[j].one.getLevel()+1);
	    				pointQueue.addLast(mylines[j].two);
	    			}
		    				
		    	}
		    	
	    	}
	    	else {
	    		System.out.println ("--------------index is out of range for point "+ pt.toString());
	    	}
	    }
	    
	    for ( i=0; i < mylines.length; i++){
	    	if (mylines[i].one.getLevel() == NOT_ASSIGNED && mylines[i].two.getLevel() > NOT_ASSIGNED){
	    		mylines[i].one.setLevel(mylines[i].two.getLevel()+1);
	    	}
	    	//System.out.println (mylines[i].toString());
	    }
	    
	    Arrays.sort(mylines, new LevelCompare());
	    System.out.println ("-----------------------------------------------");
	    for ( i=0; i < mylines.length; i++){
	    	if (mylines[i].one.getLevel() > 0 && mylines[i].two.getLevel() > 0) {
	    	    System.out.println (mylines[i].toString());
	    	     if (mylines[i].one.getLevel() == mylines[i].two.getLevel()){
	    	    	 mylines[i].one.setClusterId(mylines[i].one.getLevel());
	    	    	 mylines[i].two.setClusterId(mylines[i].two.getLevel());
	    	    	 
	    	     }
	    	}
	    }
		
	}
	
	
	public Map<Long, MyPoint[]> getLineList() {
		return lineList;
	}
	
	public  boolean lineExists (MyPoint A, MyPoint B){
		boolean exists = false;
		
		Long key = new Long( A.hashCode() - B.hashCode());
		exists = lineList.containsKey(key);
		//if (lineList.containsKey(key)){
		//	System.out.println("lineList contains key " + A.toString() + ", "+ B.toString());
		//}
		return exists;
		
	}
	
	public Map<Long, MyPoint> getPointList(){
		return pointList;
	}
	
	

}
