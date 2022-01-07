package com.theincgi.advancedMacros.misc;

import java.util.Arrays;

import net.minecraft.util.math.MathHelper;

public class Matrix {
	/**Stored as [rows][cols]*/
	public Double[][] data;
	public final int ROWS, COLS;
	public Matrix(int rows, int cols) {
		if(rows<=0 || cols<=0) {
			throw new RuntimeException("Matrix is too small");
		}
		this.ROWS = rows;
		this.COLS = cols;
		this.data = new Double[rows][cols];
	}
	public Matrix mult(Matrix b) {
		if(this.COLS!=b.ROWS) {throw new RuntimeException(String.format("Can't multipy matrix sizes <R: %d, C: %d> * <R: %d, C: %d>",this.ROWS, this.COLS, b.ROWS, b.COLS));}
		Matrix out = new Matrix(this.ROWS, b.COLS);
		for(int r = 0; r<ROWS; r++) {
			for(int c = 0; c<b.COLS; c++) {//cols of b matrix
				double sum = 0;
				for(int i = 0; i<this.COLS; i++) {//cols of this, rows of b
					sum += this.data[r][i] * b.data[i][c];
				}
				out.data[r][c] = sum;
			}
		}
		return out;
	}
	public Matrix scalar(double m) {
		Matrix out = new Matrix(ROWS, COLS);
		for(int r = 0; r<ROWS; r++) {
			for(int c = 0; c<COLS; c++) {
				out.data[r][c] = this.data[r][c] * m;
			}
		}
		return out;
	}
	public Matrix add(Matrix b) {
		if(this.COLS!=b.COLS && this.ROWS!=b.ROWS) {throw new RuntimeException(String.format("Can't add matrix sizes <R: %d, C: %d> + <R: %d, C: %d>",this.ROWS, this.COLS, b.ROWS, b.COLS));}
		Matrix out = new Matrix(this.ROWS, b.COLS);
		for(int r = 0; r<ROWS; r++) {
			for(int c = 0; c<COLS; c++) {//cols of b matrix
				out.data[r][c] = this.data[r][c] + b.data[r][c];
			}
		}
		return out;
	}
	public Matrix add(double m) {
		Matrix out = new Matrix(ROWS, COLS);
		for(int r = 0; r<ROWS; r++) {
			for(int c = 0; c<COLS; c++) {
				out.data[r][c] = this.data[r][c] + m;
			}
		}
		return out;
	}
	public Matrix transpose() {
		Matrix out = new Matrix(COLS, ROWS);//swapped dimensions
		for(int r = 0; r<ROWS; r++) {
			for(int c = 0; c<COLS; c++) {
				out.data[c][r] = this.data[r][c];
			}
		}
		return out;
	}
	@Override
	public Matrix clone() {
		Matrix cloned = new Matrix(ROWS, COLS);
		for(int r = 0; r<ROWS; r++) {
			for(int c = 0; c<COLS; c++) {
				cloned.data[r][c] = (double)this.data[r][c];//should make a new object
			}
		}
		return cloned;
	}
	@Override
	public String toString() {
		return Arrays.deepToString(data);
	}
	
	/**Utility function for loading test data quickly*/
	public Matrix load(double... v) {
		if(v.length!=ROWS*COLS) {throw new RuntimeException("Invalid amount of data");}
		for(int r = 0; r<ROWS; r++) {
			for(int c = 0; c<COLS; c++) {
				this.data[r][c] = v[c+r*COLS];
			}
		}
		return this;
	}
	
	
	/**Null unless matrix is 1 col, 3 row*/
	public Matrix rotate(Axis axis, float angle) {
		if(this.COLS==1 && this.ROWS==3) {
			Matrix rot = new Matrix(3, 3);
			if(axis.equals(Axis.X)) {
				rot.load(
						1, 						0, 						0,
						0,   MathHelper.cos(angle), -MathHelper.sin(angle),
						0,   MathHelper.sin(angle), MathHelper.cos(angle));
			}else if(axis.equals(Axis.Y)) {
				rot.load(
						MathHelper.cos(angle), 0, MathHelper.sin(angle),
						0,1,0,
						-MathHelper.sin(angle),0,MathHelper.cos(angle));
			}else if(axis.equals(Axis.Z)) {
				rot.load(
						MathHelper.cos(angle), -MathHelper.sin(angle), 0,
						MathHelper.sin(angle), MathHelper.cos(angle), 0,
						0,0,1);
			}else {throw new RuntimeException("Unknown axis enum");}
			return rot.mult(this);
		}else{
			return null;
		}
	}
	public static Matrix vector(float x, float y, float z) {
		return new Matrix(3, 1).load(x,y,z);
	}
	public double vectorX() {
		return data[0][0];
	}
	public double vectorY() {
		return data[1][0];
	}
	public double vectorZ() {
		return data[2][0];
	}
	public static enum Axis{
		X,Y,Z;
	}
}
