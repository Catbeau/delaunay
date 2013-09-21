/**
 * Copyright 2013 Catherine Beauheim
 * MyPoint.java
 */
		

package com.beauheim.delaunay.delaunay;

public class MyPoint implements Comparable{
	
	static final int X=0;
	static final int Y=1;
	private float[] point= new float[2];
	private int[] screen = new int[2];
	protected final static int UND=-1;
	protected final static int OUT=0;
	protected final static int ON = 1;
	protected final static int IN=2;
	protected int  loc = UND;
	protected int frequency=1;
	protected int level=UND;
	protected Long id;
	protected int clusterId;
	protected String name="";
	
	public MyPoint (float x, float y){
		point[X] = x;
		point[Y] = y;
		id = new Long( hashCode());
	//	System.out.println ("MyPoint "+ id + ", "+ x + ", "+ y);
		screen[X]= (int)x;
		screen[Y]= (int)y;
	}
	public void addOne(){
		frequency++;
	}
	
	public boolean equals (MyPoint pt){
    	if (point[X] == pt.getX()){
    		if (point[Y] == pt.getY()){
    			
				return true;	
    		}
    	}
    	return false;
    }
	
	public MyPoint clone() {
		MyPoint newone = new MyPoint(point[X], point[Y]);
		newone.name = name;
		newone.frequency = frequency;
		return newone;
	}
	
	protected void setName(String n){
		name = n;
	}
	
	protected String getName(){
		return name;
	}
	
	public String toString(){
		
		return ""+name + ", " +point[X]+", "+ point[Y] +", " + level;
	}
	
	protected int getClusterId() {
		return clusterId;
	}
 	
	public int getFreq(){
		return frequency;
	}
	
	public Long getId() {
		return id;
	}
	
	public int getLevel(){
		return level;
	}
	
	public int getLoc (){
		return loc;
	}
	
	
	public float getX(){
		return point[X];
	}
	
	public float getY(){
		return point[Y];
	}
	
	public int getSX(){
		return screen[X];
	}
	
	public int getSY(){
		return screen[Y];
	}

	public int hashCode(){
		 int hash;   
	     hash = java.lang.Float.floatToIntBits (point[X]) ^ java.lang.Float.floatToIntBits(point[Y]);
	     id = new Long(hash);
	    
	     return hash;
		}
	
	protected void setClusterId (int i){
		clusterId = i;
	}
	protected void setLevel(int l){
		level = l;
		//System.out.println (toString() + "  "+ level);
	}
	
	public void setLoc (int loc){
		this.loc = loc;
	}

	public void setX(int x){
		screen[X] = x;
	}
	
	public void setY(int y){
		screen[Y]=y;
	}
	
	public void setX (float x){
		point[X]= x;
	}
	
	public void setY (float y){
		point[Y]= y;
	}
	
	
		@Override
	/*  sort on name */
	public int compareTo(Object arg0) {
		// TODO Auto-generated method stub
		if (arg0 instanceof MyPoint){
			MyPoint pt = (MyPoint) arg0;
		   return (name.compareTo(pt.name));
		}
		return 0;
	}

}
