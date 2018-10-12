package loader.model;

import java.util.Comparator;

public class ComparePixelByY implements Comparator <Pixel>{

	@Override
	public int compare(Pixel pixel1, Pixel pixel2) {
		
		if (pixel1.getXCoordinate() != pixel2.getXCoordinate()){
			return pixel1.getXCoordinate() - pixel2.getXCoordinate();
		}else {
			return pixel1.getYCoordinate() - pixel2.getYCoordinate();
		}
	}

}
