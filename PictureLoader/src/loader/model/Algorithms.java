package loader.model;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public class Algorithms {

	private int thresholdValue;
	private int edgeMagnitude;
	private int particleNumber;
	private int validEdgePixels;
	private Color black = Color.BLACK;
	private Color white = Color.WHITE;
	private Color red = Color.RED;
	private Map<Integer, ArrayList<Pixel>> pixelCategoryMap;
	private AllBlackPixels allBlackPixels;
	private ParticleDatabase particleDatabase;
	private BufferedImage originalImage;
	private Image binaryImage;
	private PixelReader reader;

	public Algorithms() {

		this.pixelCategoryMap = new HashMap<Integer, ArrayList<Pixel>>();
		this.allBlackPixels = new AllBlackPixels();
		this.particleDatabase = new ParticleDatabase();

	}

	public void setOriginalImage(BufferedImage originalImage) {
		this.originalImage = originalImage;
	}

	public void setBinaryImage(Image binaryImage) {
		this.binaryImage = binaryImage;
	}

	/*
	 * Current State :
	 * 
	 * Links pixels via connected component method. Double pass. Produces a list of
	 * each particle object, with an arraylist of pixels as constructor Finds edges
	 * using horizontal and vertical pass. Edge finding updated from being next to a
	 * white pixel to using the highest and lowest in each row and column Only
	 * returns in focus particles now
	 * 
	 * 
	 * 
	 * To do :
	 * 
	 * 
	 * 1. Add something where only full particles are measured, not ones where
	 * particle boundary is off screen. 2. Fill in everything inside edges so a
	 * pixel count returns an accurate area 3. Sliders for threshold and edge
	 * strength. Update so sliding threshold reloads the image. 4. Particle
	 * analysis. Size and shape to start 5. Particle classes 6. Potentially look
	 * into data raster for connecting components rather than nested for loops
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 */

	public void setThresholdValue(Integer thresholdValue) {
		this.thresholdValue = thresholdValue;
	}

	public void setEdgeMagnitude(Integer edgeMagnitude) {
		this.edgeMagnitude = edgeMagnitude;
	}

	public int greyScaleConversion(int x, int y, BufferedImage bufferedImage) {
		int red = (bufferedImage.getRGB(x, y) >> 16) & 0xFF;
		int green = (bufferedImage.getRGB(x, y) >> 8) & 0xFF;
		int blue = (bufferedImage.getRGB(x, y) >> 0) & 0xFF;

		int grey = (77 * red + 150 * green + 29 * blue) >> 8;
		return grey;
	}

	public WritableImage backgroundDifferences(BufferedImage picture, BufferedImage background) {
		/*
		 * 3 Clears to delete all information from the previous thresholded images
		 */

		this.allBlackPixels.clear();
		this.particleDatabase.clear();
		this.pixelCategoryMap.clear();

		/*
		 * This returns the thresholded image, where in the image a red pixel is one
		 * where the difference between the picture of interest and the background is
		 * greater than the set threshold value If it is the pixel is coloured red. If
		 * not the pixel reader recalls the original colour of that pixel
		 */

		WritableImage imageDifference = new WritableImage(picture.getWidth(), picture.getHeight());
		Image imageToProcess = SwingFXUtils.toFXImage(picture, null);
		PixelWriter writer = imageDifference.getPixelWriter();
		PixelReader reader = imageToProcess.getPixelReader();
		for (int y = 0; y < picture.getHeight(); y++) {
			for (int x = 0; x < picture.getWidth(); x++) {
				if ((Math.abs(greyScaleConversion(x, y, picture)
						- greyScaleConversion(x, y, background)) >= thresholdValue)) {
					writer.setColor(x, y, red);
				} else {
					writer.setColor(x, y, reader.getColor(x, y));
				}
			}
		}
		return imageDifference;
	}

	public WritableImage binaryImageAfterThresh(BufferedImage picture, BufferedImage background) {

		/*
		 * This returns a binary image, where black pixels are particles / foreground
		 * (based on their difference with the background image), and white is the
		 * background
		 */

		WritableImage binaryImage = new WritableImage(picture.getWidth(), picture.getHeight());
		PixelWriter writer = binaryImage.getPixelWriter();
		for (int y = 0; y < picture.getHeight(); y++) {
			for (int x = 0; x < picture.getWidth(); x++) {
				if ((Math.abs(greyScaleConversion(x, y, picture)
						- greyScaleConversion(x, y, background)) >= thresholdValue)) {
					writer.setColor(x, y, black);
				} else {
					writer.setColor(x, y, white);
				}
			}
		}

		/*
		 * LinkConnectedComponents is called. This runs the connected pixel algorithm,
		 * which eventually gives us particles made up of connected black pixels
		 * 
		 * 
		 */

		BufferedImage buffBinary = SwingFXUtils.fromFXImage(binaryImage, null);
		setBinaryImage(binaryImage);
		this.reader = this.binaryImage.getPixelReader();
		linkConnectedComponents(buffBinary);

		return binaryImage;
	}

	public WritableImage imageWithEdges(BufferedImage originalImage) {

		/*
		 * Clear previous edge finding data so that multiple button presses will work
		 */

		this.particleDatabase.getValidParticleList().clear();
		this.particleDatabase.getAllEdgePixels().clear();
		this.particleDatabase.getAllPixels().clear();

		/*
		 * Run the algorithm for checking if a particle is in focus. If it is, the edge
		 * pixels of said particle are added to a global list and upon writing this
		 * image, every pixel in the global edge list is written as red. Else the pixel
		 * is it's original colour
		 */

		checkValidParticles();

		WritableImage imageAfterEdges = new WritableImage(originalImage.getWidth(), originalImage.getHeight());
		Image originalToRead = SwingFXUtils.toFXImage(originalImage, null);
		PixelReader readOriginalImage = originalToRead.getPixelReader();
		PixelWriter writeImageEdges = imageAfterEdges.getPixelWriter();

		for (int y = 0; y < originalImage.getHeight(); y++) {
			for (int x = 0; x < originalImage.getWidth(); x++) {
				Pixel pixel = new Pixel(x, y);

				if (this.particleDatabase.getAllEdgePixels().contains(pixel)) {
					writeImageEdges.setColor(x, y, red);
				} else {
					writeImageEdges.setColor(x, y, readOriginalImage.getColor(x, y));
				}
			}
		}

		return imageAfterEdges;

	}

	public int gradientMagnitude(BufferedImage imageOriginal, Pixel edgePixel) {

		/*
		 * Doing gradient magnitude with a 3x3 pixel matrix, where pixel of interest is the centre pixel.
		 * Using a sobel filter, horizontal and vertical: 
		 * 
		 * @formatter:off
		 * 
		 * 1	0	-1			 1	 2	 1
		 * 2	0	-2			 0	 0	 0
		 * 1	0	-1			-1	-2	-1
		 * 
		 * @formatter:on
		 * 
		 * Greater edge magnitudes indicate a higher gradient at the pixel, Math.Abs used to give
		 * a positive value as gradient could be either direction. Final value is the square root of 
		 * the sum of horizontal and vertical squared. 
		 * 
		 * 
		 */

		int upRightGrey = greyScaleConversion(edgePixel.getXCoordinate() + 1, edgePixel.getYCoordinate() - 1,
				imageOriginal);
		int upGrey = greyScaleConversion(edgePixel.getXCoordinate(), edgePixel.getYCoordinate() - 1, imageOriginal);
		int upLeftGrey = greyScaleConversion(edgePixel.getXCoordinate() + 1, edgePixel.getYCoordinate() - 1,
				imageOriginal);
		int leftGrey = greyScaleConversion(edgePixel.getXCoordinate() - 1, edgePixel.getYCoordinate(), imageOriginal);
		int rightGrey = greyScaleConversion(edgePixel.getXCoordinate() + 1, edgePixel.getYCoordinate(), imageOriginal);
		int bottomRightGrey = greyScaleConversion(edgePixel.getXCoordinate() + 1, edgePixel.getYCoordinate() + 1,
				imageOriginal);
		int bottomGrey = greyScaleConversion(edgePixel.getXCoordinate(), edgePixel.getYCoordinate() + 1, imageOriginal);
		int bottomLeftGrey = greyScaleConversion(edgePixel.getXCoordinate() - 1, edgePixel.getYCoordinate() + 1,
				imageOriginal);

		int verticalGradient = (1 * (upLeftGrey + bottomLeftGrey) + 2 * (leftGrey) - 1 * (upRightGrey + bottomRightGrey)
				- 2 * (rightGrey));
		int horizontalGradient = (1 * (upLeftGrey + upRightGrey) + 2 * (upGrey) - 1 * (bottomLeftGrey + bottomRightGrey)
				- 2 * (bottomGrey));

		return (int) Math.sqrt(Math.pow(verticalGradient, 2) + Math.pow(horizontalGradient, 2));

	}

	public void linkConnectedComponents(BufferedImage binaryImage) {

		/*
		 * Used to connect black pixels into particles. Two passes, first pass adds all
		 * black pixels to a database class. Uses a map of integer key and list<Pixel>
		 * keyset.
		 * 
		 * First step is to check if map is empty, and if so to put the first pixel into
		 * the map at integer number 1. We also tell the pixels what integer they belong
		 * to using setStoredParticle. Low Particle first pass method is called, which
		 * returns the integer key to store this pixel in.
		 */

		Image binaryToRead = SwingFXUtils.toFXImage(binaryImage, null);
		PixelReader readBinary = binaryToRead.getPixelReader();
		this.particleNumber = 2;

		for (int y = 0; y <= binaryImage.getHeight(); y++) {
			for (int x = 0; x <= binaryImage.getWidth(); x++) {
				if (x > 0 && y > 0 && x < binaryImage.getWidth() - 1 && y < binaryImage.getHeight() - 1) {

					Pixel pixel = new Pixel(x, y);

					if (readBinary.getColor(x, y).equals(black)) {
						this.allBlackPixels.add(pixel);

						if (isPixelEdge(pixel)) {
							pixel.setEdgePixel(true);
						}

						if (this.pixelCategoryMap.isEmpty()) {
							this.pixelCategoryMap.put(1, new ArrayList<Pixel>());
							this.pixelCategoryMap.get(1).add(pixel);
							pixel.setStoredParticle(1);

						} else {
							int key = lowParticleFirstPass(pixel, this.particleNumber);
							pixel.setStoredParticle(key);
						}

					}
				}
			}

		}

		/*
		 * For pass 2 use the global black pixel list generated in pass one. First
		 * reverse the list. This is because pass 1 tends to get pixels in the top left
		 * of a particle wrong, so we want to arrive at those pixels through pixels that
		 * are correctly classified.
		 */

		this.allBlackPixels.reverse();

		for (Pixel pixel : this.allBlackPixels.getAllBlackPixels()) {
			passTwo(pixel);
		}

		/*
		 * CleanUp is to remove empty lists from the integer map after pass two
		 * GenerateParticleList uses the map to make a list of particles in the class
		 * particle database. Can now clear the pixel category map as it is no longer
		 * required.
		 */

		cleanUp();
		generateParticleList();

		this.pixelCategoryMap.clear();

	}

	public int lowParticleFirstPass(Pixel pixel, int currentParticle) {

		/*
		 * This method receives the pixel we're investigating, and the current integer
		 * we're at in the map. Create 3 new pixels, one above, one left, and one above
		 * right. In almost all scenarios these are all we need to look for already
		 * allocated pixels on the first pass.
		 */

		Pixel up = new Pixel(pixel.getXCoordinate(), pixel.getYCoordinate() - 1);
		Pixel upRight = new Pixel(pixel.getXCoordinate() + 1, pixel.getYCoordinate() - 1);
		Pixel left = new Pixel(pixel.getXCoordinate() - 1, pixel.getYCoordinate());

		/*
		 * 3 pixels put in a list. Then check if the map of integers and pixels contains
		 * the 3 pixels. If yes, we add the integer it was found at to a list of
		 * integers called particles containing.
		 * 
		 */

		List<Pixel> pixelList = new ArrayList<Pixel>();
		List<Integer> particlesContaining = new ArrayList<Integer>();

		pixelList.add(up);
		pixelList.add(upRight);
		pixelList.add(left);

		for (Pixel pixelToCheck : pixelList) {
			for (Integer particleKey : this.pixelCategoryMap.keySet()) {
				if (this.pixelCategoryMap.get(particleKey).contains(pixelToCheck)) {
					particlesContaining.add(particleKey);
				}
			}
		}

		/*
		 * If the list of integers is empty, we have a new particle. None of it's
		 * neighbours has been classified, hence it is a pixel in a new particle. Make a
		 * new entry in the map, add the pixel to the list at integer one larger than
		 * the highest so far, and return this integer.
		 * 
		 * If the list isn't empty then at least one of the neighbours has been
		 * classified before. Sort the integer list smallest to largest, and add the
		 * pixel to the smallest integer in the list. Return this integer value.
		 */

		if (particlesContaining.isEmpty()) {
			currentParticle++;
			this.pixelCategoryMap.put(currentParticle, new ArrayList<Pixel>());
			this.pixelCategoryMap.get(currentParticle).add(pixel);
			pixel.setStoredParticle(currentParticle);

			this.particleNumber++;

			return currentParticle;

		} else {
			Collections.sort(particlesContaining);
			this.pixelCategoryMap.get(particlesContaining.get(0)).add(pixel);
			pixel.setStoredParticle(particlesContaining.get(0));
			return particlesContaining.get(0);
		}

	}

	public void passTwo(Pixel pixel) {

		/*
		 * Create four particles around the pixel of interest, and put them in a list. A
		 * very inefficient way of checking the stored integer value of the four
		 * neighbours.
		 */

		Pixel up = new Pixel(pixel.getXCoordinate(), pixel.getYCoordinate() - 1);
		Pixel right = new Pixel(pixel.getXCoordinate() + 1, pixel.getYCoordinate());
		Pixel left = new Pixel(pixel.getXCoordinate() - 1, pixel.getYCoordinate());
		Pixel down = new Pixel(pixel.getXCoordinate(), pixel.getYCoordinate() + 1);

		List<Pixel> listedPixels = new ArrayList<Pixel>();

		for (Pixel pixelInGlobalBlackList : this.allBlackPixels.getAllBlackPixels()) {
			if (pixelInGlobalBlackList.equals(up)) {
				up.setStoredParticle(pixelInGlobalBlackList.getStoredParticle());
				listedPixels.add(up);
			} else if (pixelInGlobalBlackList.equals(right)) {
				right.setStoredParticle(pixelInGlobalBlackList.getStoredParticle());
				listedPixels.add(right);
			}
			if (pixelInGlobalBlackList.equals(left)) {
				left.setStoredParticle(pixelInGlobalBlackList.getStoredParticle());
				listedPixels.add(left);
			}
			if (pixelInGlobalBlackList.equals(down)) {
				down.setStoredParticle(pixelInGlobalBlackList.getStoredParticle());
				listedPixels.add(down);
			}
		}

		/*
		 * Sort the integer values of each pixel neighbour smallest to largest. If the
		 * smallest value is less than the stored value of the pixel of interest, then
		 * add the pixel to the new lowest integer value, remove it from it's current
		 * integer value, and set the stored particle number to it's new value.
		 * 
		 * There's a check for if the list of integers is empty as in the case of 1
		 * pixel particles, trying to get(0) threw an exception.
		 */

		Collections.sort(listedPixels);

		if (listedPixels.isEmpty()) {

		} else {
			if (listedPixels.get(0).getStoredParticle() < pixel.getStoredParticle()) {

				this.pixelCategoryMap.get(listedPixels.get(0).getStoredParticle()).add(pixel);

				Iterator<Pixel> pixelIterator = this.pixelCategoryMap.get(pixel.getStoredParticle()).iterator();

				while (pixelIterator.hasNext()) {
					if (pixelIterator.next().equals(pixel)) {
						pixelIterator.remove();
						continue;
					}
				}
				pixel.setStoredParticle(listedPixels.get(0).getStoredParticle());
			}
		}

	}

	public void checkValidParticles() {

		/*
		 * Checking that the particle is properly in focus. Use a helper integer to
		 * track the number of edge particles that are in focus, with focus determined
		 * from the gradient magnitude method and the edge magnitude value.
		 * 
		 * A particle is determined in focus if 80% of it's edges are greater than the
		 * edge magnitude value. This is a random number and could probably use some
		 * actual investigation but...why bother. Set the validity boolean of the
		 * particle to true even though I don't think it's actually used, and add the
		 * particle to the list of valid particles in global particle database.
		 */

		this.validEdgePixels = 0;

		for (Particle particleCheck : this.particleDatabase.getParticleList()) {
			for (Pixel pixel : particleCheck.getEdgePixels()) {
				if (gradientMagnitude(this.originalImage, pixel) >= this.edgeMagnitude) {
					validEdgePixels++;
				}

			}

			try {
				if (validEdgePixels / particleCheck.getEdgePixels().size() >= 0.8) {
					particleCheck.setValidParticle(true);
					if (particleCheck.completeEdge()) {
						this.particleDatabase.addValid(particleCheck);
						particleCheck.setIndex(this.particleDatabase.getValidParticleList().indexOf(particleCheck) + 1);
					}
				}
			}

			catch (ArithmeticException e) {
			}

			this.validEdgePixels = 0;
		}

		this.particleDatabase.addToStorage();

	}

	/*
	 * Check black pixels for white neighbours. Method receives pixel of interest,
	 * generate neighbours in the 4 component pattern, check if the neighbours are
	 * white. White neighbour means edge pixel.
	 */

	public boolean isPixelEdge(Pixel pixel) {

		List<Pixel> helperPixelList = new ArrayList<Pixel>();
		boolean isEdge = false;
		int iteration = 2;

		try {

			Pixel up = new Pixel(pixel.getXCoordinate(), pixel.getYCoordinate() - 1);
			Pixel down = new Pixel(pixel.getXCoordinate(), pixel.getYCoordinate() + 1);
			Pixel left = new Pixel(pixel.getXCoordinate() - 1, pixel.getYCoordinate());
			Pixel right = new Pixel(pixel.getXCoordinate() + 1, pixel.getYCoordinate());

			Collections.addAll(helperPixelList, right, up, left, down);
		}

		catch (Exception e) {
			System.out.println("Out of Bounds in edge pixels");

		}

		for (Pixel pixelCheck : helperPixelList) {

			if (reader.getColor(pixelCheck.getXCoordinate(), pixelCheck.getYCoordinate()).equals(white)) {

				isEdge = true;

				if (iteration % 2 == 0) {
					pixel.setEdgePixelHorizontal(true);
				} else {
					pixel.setEdgePixelVertical(true);
				}
			}

			iteration++;
		}

		if (isEdge) {
			return true;
		}

		return false;

	}

	public void cleanUp() {

		/*
		 * Iterate through the map of integer and pixel lists and remove everything
		 * where the list is empty
		 */

		Iterator<Map.Entry<Integer, ArrayList<Pixel>>> mapIterator = this.pixelCategoryMap.entrySet().iterator();

		while (mapIterator.hasNext()) {
			if (mapIterator.next().getValue().isEmpty()) {
				mapIterator.remove();
			}
		}

	}

	public void generateParticleList() {
		for (Integer key : this.pixelCategoryMap.keySet()) {
			Particle particle = new Particle(this.pixelCategoryMap.get(key));
			this.particleDatabase.add(particle);
			particle.setOriginalImage(originalImage);
		}

	}

	public ParticleDatabase getDatabase() {
		return this.particleDatabase;
	}
}
