package loader.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class Particle {

	private int pixelCount;
	private List<Pixel> pixelList;
	private Set<Pixel> edgePixels;
	private List<Pixel> orderedEdges;
	private boolean inFocus;
	private double perimeter;
	private Set<Pixel> perimeterPixels;
	private Set<Pixel> freemanPerimeterPixels;
	private List<Integer> freemanChainCode;
	private Set<Pixel> allPixels;
	private List<Pixel> listOfAllPixels;
	private double centreX;
	private double centreY;
	private double area;
	private double shapeFactor;
	private int largestX;
	private int smallestX;
	private int largestY;
	private int smallestY;
	private double horizontalDiameter = 0;
	private double verticalDiameter = 0;
	private double size;

	private IntegerProperty pixelCountProperty;
	private DoubleProperty areaProperty;
	private int particleIndex;

	/*
	 * Calculation Order:
	 * 
	 * 1. Particle is created from a list of thresholded pixels which don't
	 * necessarily correspond to every pixel present in the particle (white centres
	 * makes donuts).
	 * 
	 * 2. Find edges is automatically called, generating a list of all edge pixels
	 * which is fed back to algorithms and used in the focus calculations.
	 * 
	 * 3. Clicking find edges in the program (handleFindEdges) sets the boolean
	 * inFocus for the particle. This calls particle properties which in turn calls
	 * the methods that do specific calculations (Area, size, etc) if the particle
	 * is in focus.
	 */

	public Particle(List<Pixel> pixels) {
		this.pixelList = pixels;
		this.pixelCount = 0;
		this.edgePixels = new HashSet<Pixel>();
		this.orderedEdges = new ArrayList<Pixel>();
		this.perimeterPixels = new HashSet<Pixel>();
		this.freemanPerimeterPixels = new HashSet<Pixel>();
		this.freemanChainCode = new ArrayList<Integer>();
		this.allPixels = new HashSet<Pixel>();
		this.listOfAllPixels = new ArrayList<Pixel>();
		this.inFocus = false;
		// findEdges();
		fillAll();

	}

	public int getPixelCount() {
		return this.pixelCount;
	}

	public IntegerProperty getPixelCountProperty() {
		this.pixelCount = this.listOfAllPixels.size();
		this.pixelCountProperty = new SimpleIntegerProperty(this.pixelCount);
		return this.pixelCountProperty;
	}

	public DoubleProperty getPerimeterProperty() {
		DoubleProperty perimeterProperty = new SimpleDoubleProperty(this.perimeter);
		return perimeterProperty;
	}

	public DoubleProperty getSizeProperty() {
		DoubleProperty sizeProperty = new SimpleDoubleProperty(this.size);
		return sizeProperty;
	}

	public DoubleProperty getShapeFactorProperty() {
		DoubleProperty shapeFactorProperty = new SimpleDoubleProperty(this.shapeFactor);
		return shapeFactorProperty;
	}

	public void setIndex(int index) {
		this.particleIndex = index;
	}

	public IntegerProperty getParticleIndex() {
		IntegerProperty particleIndexProp = new SimpleIntegerProperty(this.particleIndex);
		return particleIndexProp;
	}

	@Override
	public String toString() {
		this.pixelCount = this.listOfAllPixels.size();
		return "This particle has " + this.pixelCount + " pixels";
	}

	public void add(Pixel pixel) {
		this.pixelList.add(pixel);
	}

	public List<Pixel> getPixelList() {
		return this.pixelList;
	}

	public void fillAll() {

		int helperHorizontal = 0;
		int helperVertical = 0;

		Collections.sort(this.pixelList, new ComparePixelByX());
		List<Pixel> edgeByRow = new ArrayList<Pixel>();
		int particlePosition = 0;

		/*
		 * List is sorted by X. Largest Y is end of list, smallest is start of list
		 */

		this.largestY = this.pixelList.get(this.pixelList.size() - 1).getYCoordinate();
		this.smallestY = this.pixelList.get(0).getYCoordinate();

		/*
		 * For all pixels in the thresholded list
		 */

		for (Pixel pixelInRow : this.pixelList) {

			/*
			 * If only 1 pixel in the particle, add it to edge and all list, then exit the
			 * for loop
			 */
			if (this.pixelList.size() == 1) {
				this.allPixels.add(pixelInRow);
				this.largestX = 1;
				continue;
			}

			/*
			 * If it's the last particle in the list, it's an edge. If the last particle is
			 * in a row of one break the loop
			 */

			if (particlePosition == this.pixelList.size() - 1) {
				edgeByRow.add(pixelInRow);

				if (edgeByRow.size() == 1) {
					this.horizontalDiameter = 1;
					continue;
				}

				helperHorizontal = edgeByRow.get(edgeByRow.size() - 1).getXCoordinate() -
						edgeByRow.get(0).getXCoordinate();

				if (helperHorizontal > this.horizontalDiameter) {
					this.horizontalDiameter = helperHorizontal;
				}

				/*
				 * Still in the if for the bottom row. Add all pixels between highest and lowest
				 * in row to the all pixels list. Then add the last pixel to the all pixels
				 * list.
				 */

				for (int i = 0; i < (edgeByRow.get(edgeByRow.size() - 1).getXCoordinate() -
						edgeByRow.get(0).getXCoordinate() + 1); i++) {

					this.allPixels.add(new Pixel(edgeByRow.get(0).getXCoordinate() + i,
							edgeByRow.get(0).getYCoordinate()));
				}

				this.allPixels.add(pixelInRow);

				continue;
			}

			/*
			 * If y coord of current and next particle are the same, they're on the same
			 * row. Else add 0 and last pixel to the edge pixel list, add all pixels in
			 * betwen to the all pixels list. Clear the row. Increment particle index.
			 */

			if (pixelInRow.getYCoordinate() == this.pixelList.get(particlePosition + 1).getYCoordinate()) {
				edgeByRow.add(pixelInRow);
			} else {

				edgeByRow.add(pixelInRow);

				helperHorizontal = edgeByRow.get(edgeByRow.size() - 1).getXCoordinate() -
						edgeByRow.get(0).getXCoordinate();

				if (helperHorizontal > this.horizontalDiameter) {
					this.horizontalDiameter = helperHorizontal;
				}

				for (int i = 0; i < (edgeByRow.get(edgeByRow.size() - 1).getXCoordinate() -
						edgeByRow.get(0).getXCoordinate() + 1); i++) {
					this.allPixels.add(new Pixel(edgeByRow.get(0).getXCoordinate() + i,
							edgeByRow.get(0).getYCoordinate()));
				}

				edgeByRow.clear();
			}
			particlePosition++;

		}
		
		edgeByRow.clear();
		particlePosition = 0;

		Collections.sort(this.pixelList, new ComparePixelByY());

		this.largestX = this.pixelList.get(this.pixelList.size() - 1).getXCoordinate();
		this.smallestX = this.pixelList.get(0).getXCoordinate();

		for (Pixel pixelInRow : this.pixelList) {
			if (particlePosition == this.pixelList.size() - 1) {

				if (edgeByRow.size() <= 1) {
					helperVertical = 1;
				} else {
					helperVertical = edgeByRow.get(edgeByRow.size() - 1).getYCoordinate() -
							edgeByRow.get(0).getYCoordinate();
				}

				if (helperVertical > this.verticalDiameter) {
					this.verticalDiameter = helperVertical;
				}

				continue;
			}
			if (pixelInRow.getXCoordinate() == this.pixelList.get(particlePosition + 1).getXCoordinate()) {
				edgeByRow.add(pixelInRow);
			} else {
				if (edgeByRow.isEmpty()) {
					edgeByRow.add(pixelInRow);
				}
				edgeByRow.add(pixelInRow);

				helperVertical = edgeByRow.get(edgeByRow.size() - 1).getYCoordinate() -
						edgeByRow.get(0).getYCoordinate();

				if (helperVertical > this.verticalDiameter) {
					this.verticalDiameter = helperVertical;
				}

				edgeByRow.clear();
			}
			particlePosition++;

		}

		for (Pixel pixel : this.allPixels) {
			this.listOfAllPixels.add(pixel);
		}

		Collections.sort(this.listOfAllPixels, new ComparePixelByX());
		this.area = this.allPixels.size() - (0.5 * this.edgePixels.size());
		this.area *= 0.140625;

		edgeByRow.clear();
		edgeByWhite();

	}
	
	public void edgeByWhite() {
		
	}

	public void findEdges() {

		/*
		 * Sort all pixels by X coordinate. Create a helper list where each pixel in
		 * each row is stored. Go through the ordered list, when the y coordinate of the
		 * next pixel doesn't equal the y coordinate of the current pixel it's the end
		 * of the row. In that row the edges are the pixel at position 0, and position
		 * size - 1. Do for rows and columns (by ordering by Y), to get all edge pixels
		 * of particle. Last particle in the list is definitely an edge so is added
		 * separately to avoid fun with out of bounds exceptions.
		 */

		/*
		 * 
		 */

		int helperHorizontal = 0;
		int helperVertical = 0;

		Collections.sort(this.pixelList, new ComparePixelByX());
		List<Pixel> edgeByRow = new ArrayList<Pixel>();
		int particlePosition = 0;

		/*
		 * List is sorted by X. Largest Y is end of list, smallest is start of list
		 */

		this.largestY = this.pixelList.get(this.pixelList.size() - 1).getYCoordinate();
		this.smallestY = this.pixelList.get(0).getYCoordinate();

		/*
		 * For all pixels in the thresholded list
		 */

		for (Pixel pixelInRow : this.pixelList) {

			/*
			 * If only 1 pixel in the particle, add it to edge and all list, then exit the
			 * for loop
			 */
			if (this.pixelList.size() == 1) {
				this.edgePixels.add(pixelInRow);
				this.allPixels.add(pixelInRow);
				this.largestX = 1;
				continue;
			}

			/*
			 * If it's the last particle in the list, it's an edge. If the last particle is
			 * in a row of one break the loop
			 */
			if (particlePosition == this.pixelList.size() - 1) {
				this.edgePixels.add(pixelInRow);
				edgeByRow.add(pixelInRow);

				if (edgeByRow.size() == 1) {
					this.horizontalDiameter = 1;
					continue;
				}

				helperHorizontal = edgeByRow.get(edgeByRow.size() - 1).getXCoordinate() -
						edgeByRow.get(0).getXCoordinate();

				if (helperHorizontal > this.horizontalDiameter) {
					this.horizontalDiameter = helperHorizontal;
				}

				/*
				 * Still in the if for the bottom row. Add all pixels between highest and lowest
				 * in row to the all pixels list. Then add the last pixel to the all pixels
				 * list.
				 */

				try

				{
					for (int i = 0; i < (edgeByRow.get(edgeByRow.size() - 1).getXCoordinate() -
							edgeByRow.get(0).getXCoordinate() + 1); i++) {

						this.allPixels.add(new Pixel(edgeByRow.get(0).getXCoordinate() + i,
								edgeByRow.get(0).getYCoordinate()));
					}

					this.allPixels.add(pixelInRow);

				}

				catch (IndexOutOfBoundsException e) {
					e.printStackTrace();
					System.out.println(this.pixelList.indexOf(pixelInRow) + " " + this.pixelList.size());
					System.out.println(pixelInRow);
				}

				continue;
			}

			/*
			 * If y coord of current and next particle are the same, they're on the same
			 * row. Else add 0 and last pixel to the edge pixel list, add all pixels in
			 * betwen to the all pixels list. Clear the row. Increment particle index.
			 */

			if (pixelInRow.getYCoordinate() == this.pixelList.get(particlePosition + 1).getYCoordinate()) {
				edgeByRow.add(pixelInRow);
			} else {

				edgeByRow.add(pixelInRow);
				this.edgePixels.add(edgeByRow.get(0));
				this.edgePixels.add(edgeByRow.get(edgeByRow.size() - 1));

				helperHorizontal = edgeByRow.get(edgeByRow.size() - 1).getXCoordinate() -
						edgeByRow.get(0).getXCoordinate();

				if (helperHorizontal > this.horizontalDiameter) {
					this.horizontalDiameter = helperHorizontal;
				}

				for (int i = 0; i < (edgeByRow.get(edgeByRow.size() - 1).getXCoordinate() -
						edgeByRow.get(0).getXCoordinate() + 1); i++) {
					this.allPixels.add(new Pixel(edgeByRow.get(0).getXCoordinate() + i,
							edgeByRow.get(0).getYCoordinate()));
				}

				edgeByRow.clear();
			}
			particlePosition++;

		}

		edgeByRow.clear();
		particlePosition = 0;

		Collections.sort(this.pixelList, new ComparePixelByY());

		this.largestX = this.pixelList.get(this.pixelList.size() - 1).getXCoordinate();
		this.smallestX = this.pixelList.get(0).getXCoordinate();

		for (Pixel pixelInRow : this.pixelList) {
			if (particlePosition == this.pixelList.size() - 1) {
				this.edgePixels.add(pixelInRow);

				if (edgeByRow.size() <= 1) {
					helperVertical = 1;
				} else {
					helperVertical = edgeByRow.get(edgeByRow.size() - 1).getYCoordinate() -
							edgeByRow.get(0).getYCoordinate();
				}

				if (helperVertical > this.verticalDiameter) {
					this.verticalDiameter = helperVertical;
				}

				continue;
			}
			if (pixelInRow.getXCoordinate() == this.pixelList.get(particlePosition + 1).getXCoordinate()) {
				edgeByRow.add(pixelInRow);
			} else {
				if (edgeByRow.isEmpty()) {
					edgeByRow.add(pixelInRow);
				}
				edgeByRow.add(pixelInRow);
				this.edgePixels.add(edgeByRow.get(0));
				this.edgePixels.add(edgeByRow.get(edgeByRow.size() - 1));

				helperVertical = edgeByRow.get(edgeByRow.size() - 1).getYCoordinate() -
						edgeByRow.get(0).getYCoordinate();

				if (helperVertical > this.verticalDiameter) {
					this.verticalDiameter = helperVertical;
				}

				edgeByRow.clear();
			}
			particlePosition++;

		}

		for (Pixel pixel : this.allPixels) {
			this.listOfAllPixels.add(pixel);
		}

		Collections.sort(this.listOfAllPixels, new ComparePixelByX());
		this.area = this.allPixels.size() - (0.5 * this.edgePixels.size());
		this.area *= 0.140625;

		orderEdges();
		edgeByRow.clear();

	}

	public void orderEdges() {
		for (Pixel pixel : this.edgePixels) {
			this.orderedEdges.add(pixel);
		}

		Collections.sort(this.orderedEdges, new ComparePixelByX());
	}

	public void particleProperties() {
		if (!this.inFocus) {
			return;
		}

		// perimeter();
		// freemanPerimeter();
		setCentre();
		calcShapeFactor();
		size();

	}

	public void perimeter() {

		this.perimeter = 0;
		this.perimeterPixels.clear();
		int straight = 0;
		int diagonal = 0;

		/*
		 * For the list of edge pixels. Start at 0. Find a neighbour. If they are
		 * horizontal or vertical neighbours add 1 to the perimeter. If not, add root 2
		 * to the perimeter.
		 */

		if (inFocus) {
			for (Pixel pixel : this.orderedEdges) {
				findNeighbour(pixel);

				if (pixel.getXCoordinate() == findNeighbour(pixel).getXCoordinate()
						|| pixel.getYCoordinate() == findNeighbour(pixel).getYCoordinate()) {
					// this.perimeter += 1;
					straight++;
				} else {
					// this.perimeter += Math.sqrt(2);
					diagonal++;
				}
				this.perimeterPixels.add(pixel);

			}

			this.perimeter = (straight + Math.sqrt(2) * diagonal) * 0.375;

			// this.perimeter = (straight * 0.948 + diagonal * 1.340) * 0.375;

			// this.perimeter = (Math.PI/8) * (1 + Math.sqrt(2)) * (straight + (Math.sqrt(2)
			// * diagonal)) * 0.375;

		}

	}

	public void freemanPerimeter() {

		Collections.sort(this.orderedEdges, new ComparePixelByX());

		Pixel edgePixel = this.orderedEdges.get(0);
		Pixel startingPixel = this.orderedEdges.get(0);
		Pixel secondPixel = new Pixel(0, 0);
		Pixel thirdPixel = new Pixel(0, 0);
		Pixel fourthPixel = new Pixel(0, 0);
		Pixel fifthPixel = new Pixel(0, 0);
		List<Pixel> startingPixels = new ArrayList<Pixel>();
		boolean completeParticle = false;

		this.freemanPerimeterPixels.add(startingPixel);

		for (int i = 0; i < this.orderedEdges.size() - 1; i++) {
			edgePixel = (freemanNeighbour(edgePixel));
			this.freemanPerimeterPixels.add(edgePixel);
			if (i == 0) {
				secondPixel = edgePixel;
			}
			if (i == 1) {
				thirdPixel = edgePixel;
			}
			if (i == 2) {
				fourthPixel = edgePixel;
			}
			if (i == 3) {
				fifthPixel = edgePixel;
			}
		}

		Collections.addAll(startingPixels, startingPixel, secondPixel, thirdPixel, fourthPixel, fifthPixel);

		for (Pixel starters : startingPixels) {
			if (freemanFinal(starters, startingPixel)) {
				System.out.println("Particle Complete");
				completeParticle = true;
				break;
			}
		}

		if (!completeParticle) {
			System.out.println("Freeman Fail");
		}

		System.out.println(this.freemanChainCode);

		freemanPerimeterLength();
	}

	public void size() {
		this.horizontalDiameter *= 0.375;
		this.verticalDiameter *= 0.375;
		this.size = ((this.horizontalDiameter + this.verticalDiameter) / 2);

		System.out.println("Horizontal = " + this.horizontalDiameter);
		System.out.println("Vertical = " + this.verticalDiameter);
		System.out.println("Size = " + this.size);
	}

	public void setCentre() {
		double xTotal = 0;
		double yTotal = 0;

		for (Pixel pixelInAll : this.listOfAllPixels) {
			xTotal += pixelInAll.getXCoordinate();
			yTotal += pixelInAll.getYCoordinate();
		}

		this.centreX = xTotal / this.listOfAllPixels.size();
		this.centreY = yTotal / this.listOfAllPixels.size();

	}

	public Double calcShapeFactor() {
		this.shapeFactor = ((4 * Math.PI * this.area) / (Math.pow(perimeter, 2)));
		return this.shapeFactor;
	}

	public Double getPerimeter() {
		return this.perimeter;
	}

	public Double getArea() {
		return this.area;
	}

	public DoubleProperty getAreaProperty() {
		this.areaProperty = new SimpleDoubleProperty(this.area);
		return this.areaProperty;
	}

	public List<Pixel> getAllPixels() {
		return this.listOfAllPixels;
	}

	public List<Pixel> getEdgePixels() {
		return this.orderedEdges;
	}

	public void printEdges() {
		for (Pixel pixel : this.orderedEdges) {
			System.out.println(pixel);
		}
	}

	public boolean validParticle() {
		return this.inFocus;
	}

	public void setValidParticle(boolean validity) {
		this.inFocus = validity;

		particleProperties();
	}

	public Pixel freemanNeighbour(Pixel pixel) {

		Pixel up = new Pixel(pixel.getXCoordinate(), pixel.getYCoordinate() - 1);
		Pixel down = new Pixel(pixel.getXCoordinate(), pixel.getYCoordinate() + 1);
		Pixel left = new Pixel(pixel.getXCoordinate() - 1, pixel.getYCoordinate());
		Pixel right = new Pixel(pixel.getXCoordinate() + 1, pixel.getYCoordinate());
		Pixel upRight = new Pixel(pixel.getXCoordinate() + 1, pixel.getYCoordinate() - 1);
		Pixel upLeft = new Pixel(pixel.getXCoordinate() - 1, pixel.getYCoordinate() - 1);
		Pixel downRight = new Pixel(pixel.getXCoordinate() + 1, pixel.getYCoordinate() + 1);
		Pixel downLeft = new Pixel(pixel.getXCoordinate() - 1, pixel.getYCoordinate() + 1);

		List<Pixel> helperPixelEven = new ArrayList<Pixel>();
		List<Pixel> helperPixelOdd = new ArrayList<Pixel>();
		int freemanPosition = 0;

		Collections.addAll(helperPixelEven, right, up, left, down);
		Collections.addAll(helperPixelOdd, upRight, upLeft, downLeft, downRight);

		/*
		 * Potential issues with sharp corners where edges have more than 2 edge
		 * neighbours. Need to check that the pixel I'm going to next has a valid
		 * neighbour. Try checking that adding a pixel doesn't isolate a corner. Try
		 * prioritisng straight pixels
		 */

		for (int i = 0; i < 4; i++) {
			if (this.orderedEdges.contains(helperPixelEven.get(i))) {
				if (!this.freemanPerimeterPixels.contains(helperPixelEven.get(i))) {
					freemanPosition = i * 2;
					this.freemanChainCode.add(freemanPosition);
					return helperPixelEven.get(i);
				}
			}
		}

		for (int i = 0; i < 4; i++) {
			if (this.orderedEdges.contains(helperPixelOdd.get(i))) {
				if (!this.freemanPerimeterPixels.contains(helperPixelOdd.get(i))) {
					freemanPosition = i * 2 + 1;
					this.freemanChainCode.add(freemanPosition);
					return helperPixelOdd.get(i);
				}
			}
		}

		System.out.println(this.freemanPerimeterPixels);
		System.out.println(this.freemanChainCode);
		System.out.println("You Messed Up At: " + pixel);

		return null;
	}

	public boolean freemanFinal(Pixel pixel, Pixel startingPoint) {

		Pixel up = new Pixel(pixel.getXCoordinate(), pixel.getYCoordinate() - 1);
		Pixel down = new Pixel(pixel.getXCoordinate(), pixel.getYCoordinate() + 1);
		Pixel left = new Pixel(pixel.getXCoordinate() - 1, pixel.getYCoordinate());
		Pixel right = new Pixel(pixel.getXCoordinate() + 1, pixel.getYCoordinate());
		Pixel upRight = new Pixel(pixel.getXCoordinate() + 1, pixel.getYCoordinate() - 1);
		Pixel upLeft = new Pixel(pixel.getXCoordinate() - 1, pixel.getYCoordinate() - 1);
		Pixel downRight = new Pixel(pixel.getXCoordinate() + 1, pixel.getYCoordinate() + 1);
		Pixel downLeft = new Pixel(pixel.getXCoordinate() - 1, pixel.getYCoordinate() + 1);

		List<Pixel> helperPixelList = new ArrayList<Pixel>();

		Collections.addAll(helperPixelList, right, upRight, up, upLeft, left, downLeft, down, downRight);

		for (int i = 0; i <= 7; i++) {
			if (helperPixelList.get(i).equals(startingPoint)) {
				this.freemanChainCode.add(i);
				return true;
			}
		}

		return false;

	}

	public void freemanPerimeterLength() {
		int straight = 0;
		int diagonal = 0;

		for (int freemanCode : this.freemanChainCode) {
			if (freemanCode % 2 == 0 || freemanCode == 0) {
				straight++;
			} else {
				diagonal++;
			}
		}

		this.perimeter = (straight + (Math.sqrt(2) * diagonal)) * 0.375;
	}

	public Pixel findNeighbour(Pixel pixel) {
		Pixel up = new Pixel(pixel.getXCoordinate(), pixel.getYCoordinate() - 1);
		Pixel down = new Pixel(pixel.getXCoordinate(), pixel.getYCoordinate() + 1);
		Pixel left = new Pixel(pixel.getXCoordinate() - 1, pixel.getYCoordinate());
		Pixel right = new Pixel(pixel.getXCoordinate() + 1, pixel.getYCoordinate());
		Pixel upRight = new Pixel(pixel.getXCoordinate() + 1, pixel.getYCoordinate() - 1);
		Pixel upLeft = new Pixel(pixel.getXCoordinate() - 1, pixel.getYCoordinate() - 1);
		Pixel downRight = new Pixel(pixel.getXCoordinate() + 1, pixel.getYCoordinate() + 1);
		Pixel downLeft = new Pixel(pixel.getXCoordinate() - 1, pixel.getYCoordinate() + 1);

		List<Pixel> helperPixelList = new ArrayList<Pixel>();

		Collections.addAll(helperPixelList, up, down, left, right, upRight, upLeft, downRight, downLeft);

		for (Pixel helpPixel : helperPixelList) {
			if (this.orderedEdges.contains(helpPixel) && !this.perimeterPixels.contains(helpPixel)) {
				return helpPixel;
			}
		}

		return pixel;

	}

}
