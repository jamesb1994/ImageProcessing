package loader.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AllBlackPixels {
	
	private List<Pixel> allBlackPixels;
	
	public AllBlackPixels() {
		this.allBlackPixels = new ArrayList<Pixel>();
	}
	
	public void add(Pixel pixel) {
		this.allBlackPixels.add(pixel);
	}
	
	public int test() {
		return this.allBlackPixels.get(this.allBlackPixels.size()-20).getStoredParticle();
	}
	
	public List<Pixel>getAllBlackPixels(){
		return this.allBlackPixels;
	}
	
	public void reverse() {
		Collections.reverse(allBlackPixels);
	}
	
	public void clear() {
		this.allBlackPixels.clear();
	}

}
