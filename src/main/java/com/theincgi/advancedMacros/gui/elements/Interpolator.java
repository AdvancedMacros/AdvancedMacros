package com.theincgi.advancedMacros.gui.elements;

import static java.lang.Math.PI;
import static java.lang.Math.cos;

public class Interpolator {
	public static final Interpolator linear = new Interpolator();
	public static final Interpolator smooth = new Interpolator(){
		@Override
		public double value(double p) {
			double m =  .5-(cos(PI*p)/2);
			return cap(m);
		}
	};
	public static final Interpolator constant = new Interpolator(){
		@Override
		public double value(double p) {
			return p>=.5?1:0;
		}
	};
	
	
	/**turns uninterpolated value from linear to *** type*/
	public double value(double p){
		return cap(p);
	}
	
	public double cap(double v){
		return Math.max(0, Math.min(v, 1));
	}
	
}