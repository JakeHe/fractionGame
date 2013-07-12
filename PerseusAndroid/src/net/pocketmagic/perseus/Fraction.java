package net.pocketmagic.perseus;

public class Fraction {
	int top;
	int bottom;
	
	public Fraction(int top, int bottom) {
		this.top = top;
		this.bottom = bottom;
	}
	
	public int compareTo(Fraction that){
		if (this.top * that.bottom > that.top * this.bottom)
			return 1;
		else if (this.top * that.bottom < that.top * this.bottom)
			return -1;
		else return 0;
	}
	public void setTop(int top) {
		this.top = top;
	}
	public void setBottom(int bottom) {
		this.bottom = bottom;
	}
}