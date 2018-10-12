package loader.model;


public class Pixel implements Comparable<Pixel> {
	
	private int x;
	private int y;
	private double edgeStrength;
	private int storedParticle;
	
	public Pixel (int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public int getXCoordinate() {
		return this.x;
	}
	
	public int getYCoordinate() {
		return this.y;
	}
	
	public void setX(int x) {
		this.x = x;
	}
	
	public Pixel returnThis() {
		return this;
	}
	
	public void setY (int y) {
		this.y = y;
	}
	
	@Override
	public String toString() {
		return this.x + ":" + this.y;
	}
	
	@Override
	public boolean equals (Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		
		Pixel other = (Pixel) obj;
		if (this.x != other.getXCoordinate()) {
			return false;
		}
		
		if (this.y != other.getYCoordinate()) {
			return false;
		}
		return true;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
		return result;
	}


	public double getEdgeStrength() {
		return this.edgeStrength;
	}
	
	public void setEdgeStrength(Double edgeStrength) {
		this.edgeStrength = edgeStrength;
	}
	
	public int getStoredParticle() {
		return this.storedParticle;
	}
	
	public void setStoredParticle(int value) {
		this.storedParticle = value;
	}

	@Override
	public int compareTo(Pixel pixelComparing) {
		return this.storedParticle - pixelComparing.getStoredParticle();
	}

	

}
