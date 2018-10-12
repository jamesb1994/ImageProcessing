package loader.model;

import java.util.HashSet;
import java.util.Set;

public class TestFun {

	public static void main(String[] args) {
		
		Set<Pixel>pixels = new HashSet<Pixel>();

		Pixel pixel1 = new Pixel(2, 3);
		Pixel pixel2 = new Pixel(2, 3);
		pixels.add(pixel1);
		
		System.out.println(pixel1.equals(pixel2));
		System.out.println(pixels.contains(pixel2));

	}

}
