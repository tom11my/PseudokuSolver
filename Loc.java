
public class Loc {
	
	private int x, y;
	
	public Loc(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public Loc () {
		this.x = 0; 
		this.y = 0;
	}
	
	public int getX () {
		return x;
	}
	
	public int getY () {
		return y;
	}
	
	public void setX (int x) {
		this.x = x;
	}
	
	public void setY (int y) {
		this.y = y;
	}
	
	public String toString () {
		return "x: " + x + " y: " +y;
	}
}
