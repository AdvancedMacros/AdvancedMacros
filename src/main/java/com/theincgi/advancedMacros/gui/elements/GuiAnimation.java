package com.theincgi.advancedMacros.gui.elements;

import com.theincgi.advancedMacros.AdvancedMacros;

public class GuiAnimation {
	Interpolator interpolator;
	private long start = -1;
	private long dur = -1;
	private double pos=0;
	private Runnable onFinish;
	private boolean reverse = false;
	int cycle = 1;
	boolean reverseOnFinish = false;
	private int shownCycles = 0;
	
	
	public GuiAnimation(long l) {
		interpolator = Interpolator.linear;
		dur = l;
	}
	public GuiAnimation(long l, Interpolator i) {
		interpolator = i;
		dur = l;
	}
	public void setPos(double p){
		pos = Math.min(1, Math.max(0, p));
	}

	public void start(){
		start = System.currentTimeMillis();
		shownCycles = 0;
	}

	public void stop(){
		start = -1;
		
	}
	public boolean isPlaying(){
		return start!=-1;
	}
	public double doInterpolate(){
		if(start!=-1){
			pos = interpolator.value(map(System.currentTimeMillis(), start, start+dur, reverse));

			//onFrame(pos);
			//System.out.println("Animate: "+pos);
			if(start+dur<System.currentTimeMillis()){
				if(++shownCycles==cycle && cycle>0){
					stop();
					if(reverse)
						pos = 0;
					else
						pos = 1;
					if(onFinish!=null){
						onFinish.run();
					}
				}else{
					start = System.currentTimeMillis();
					if(reverseOnFinish)
						reverse = !reverse;
				}
			}
		}//else{
			//onFrame(pos); //dont move if not animating atm
		//}
		return pos;
	}
	/**@param i 0 or less infinite<br>
	 **/
	public GuiAnimation setCycleCount(int i){
		cycle = i;
		return this;
	}
	/**Will set the reverse flag when the animation finishes*/
	public GuiAnimation setReverseOnFinish(boolean doReverse){
		reverseOnFinish = doReverse;
		return this;
	}
	
	public void setOnFinish(Runnable r){
		onFinish = r;
	}
	public void changeDurration(long dur){
		this.dur = dur;
	}
	public void setReverse(boolean r){
		reverse = r;
	}
	private double map(long x, long a, long b, boolean rev){
		double c = rev?1:0, d = rev?0:1;
		return (x-a)*(d-c)/(b-a)+c;
	}
	public static double map(double x, double a, double b, double c, double d){
		return (x-a)*(d-c)/(b-a)+c;
	}
	public static class Durration {
		public static long seconds(double d){
			return (long)(d/ 1000);
		}
		public static long minutes(double d){
			return (long)(d/(60000));
		}

	}
	public boolean isAtStart() {
		return pos==0;
	}
}