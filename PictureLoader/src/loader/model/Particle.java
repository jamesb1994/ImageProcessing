package loader.model;

import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
	private boolean completeEdge;
	private double perimeter;
	private Set<Pixel> perimeterPixels;
	private Set<Pixel> freemanPerimeterPixels;
	private List<Integer> freemanChainCode;
	private Set<Pixel> allPixels;
	private List<Pixel> listOfAllPixels;
	private Map<Integer, List<Pixel>> pixelRowMap;
	private Map<Integer, List<Pixel>> pixelColumnMap;
	private int centreX;
	private int centreY;
	private double area;
	private double shapeFactor;
	private int largestX;
	private int smallestX;
	private int largestY;
	private int smallestY;
	private double horizontalDiameter = 0;
	private double verticalDiameter = 0;
	private double diagonal45Diameter = 0;
	private double diagonal135Diameter = 0;
	private double size;
	private Pixel startingPixel;
	private ComparePixelByX compareByX;
	private ComparePixelByY compareByY;
	private BufferedImage originalImage;

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
		pixelList = pixels;
		pixelCount = 0;
		edgePixels = new HashSet<Pixel>();
		orderedEdges = new ArrayList<Pixel>();
		perimeterPixels = new HashSet<Pixel>();
		freemanPerimeterPixels = new HashSet<Pixel>();
		freemanChainCode = new ArrayList<Integer>();
		allPixels = new HashSet<Pixel>();
		listOfAllPixels = new ArrayList<Pixel>();
		inFocus = false;
		completeEdge = true;
		compareByX = new ComparePixelByX();
		compareByY = new ComparePixelByY();
		pixelRowMap = new HashMap<Integer, List<Pixel>>();
		pixelColumnMap = new HashMap<Integer, List<Pixel>>();
		createRowMap();

	}

	public int getPixelCount() {
		return pixelCount;
	}

	public IntegerProperty getPixelCountProperty() {
		pixelCount = listOfAllPixels.size();
		pixelCountProperty = new SimpleIntegerProperty(pixelCount);
		return pixelCountProperty;
	}

	public DoubleProperty getPerimeterProperty() {
		Double perimeterRounded = roundToNPlaces(perimeter, 3);
		DoubleProperty perimeterProperty = new SimpleDoubleProperty(perimeterRounded);
		return perimeterProperty;
	}

	public DoubleProperty getSizeProperty() {
		Double sizeRounded = roundToNPlaces(size, 3);
		DoubleProperty sizeProperty = new SimpleDoubleProperty(sizeRounded);
		return sizeProperty;
	}

	public DoubleProperty getShapeFactorProperty() {
		Double shapeFactorRounded = roundToNPlaces(shapeFactor, 3);
		DoubleProperty shapeFactorProperty = new SimpleDoubleProperty(shapeFactorRounded);
		return shapeFactorProperty;
	}

	public void setIndex(int index) {
		particleIndex = index;
	}

	public IntegerProperty getParticleIndex() {
		IntegerProperty particleIndexProp = new SimpleIntegerProperty(particleIndex);
		return particleIndexProp;
	}

	public DoubleProperty getAreaProperty() {
		areaProperty = new SimpleDoubleProperty(area);
		return areaProperty;
	}

	public void setOriginalImage(BufferedImage bufferedImage) {
		originalImage = bufferedImage;
	}

	@Override
	public String toString() {
		pixelCount = listOfAllPixels.size();
		return "This particle has " + pixelCount + " pixels";
	}

	public void add(Pixel pixel) {
		pixelList.add(pixel);
	}

	public List<Pixel> getPixelList() {
		return pixelList;
	}

	public double roundToNPlaces(double number, int places) {
		BigDecimal originalNumber = new BigDecimal(number);
		BigDecimal roundedNumber = originalNumber.setScale(places, RoundingMode.HALF_UP);

		return roundedNumber.doubleValue();

	}

	public void createRowMap() {

		/*
		 * Order into rows. Create an arrayList for each row. Fill map of integer rows
		 * and pixel list. Top left will always be on the edge? So do freeman here, then
		 * delete all other edges contained within
		 */

		Collections.sort(pixelList, compareByX);

		int currentRow = 1;

		pixelRowMap.put(1, new ArrayList<Pixel>());
		pixelRowMap.get(1).add(pixelList.get(0));

		if (pixelList.size() == 1) {
			listOfAllPixels.add(pixelList.get(0));
			orderedEdges.add(pixelList.get(0));
			shapeFactor = 1;
			perimeter = 0.375;
			area = 0.1406;
			size = 0.375;
			return;
		}

		for (int i = 1; i < pixelList.size(); i++) {
			Pixel currentPixel = pixelList.get(i);
			if (currentPixel.getYCoordinate() == pixelList.get(i - 1).getYCoordinate()) {
				pixelRowMap.get(currentRow).add(currentPixel);
			} else {
				currentRow++;
				pixelRowMap.put(currentRow, new ArrayList<Pixel>());
				pixelRowMap.get(currentRow).add(currentPixel);
			}
		}

		fillParticle();

	}

	public void createColumnMap() {

		int size = pixelList.size();

		/*
		 * Order into rows. Create an arrayList for each row. Fill map of integer rows
		 * and pixel list. Top left will always be on the edge? So do freeman here, then
		 * delete all other edges contained within
		 */

		Collections.sort(pixelList, compareByY);

		int currentColumn = 1;

		pixelColumnMap.put(1, new ArrayList<Pixel>());
		pixelColumnMap.get(1).add(pixelList.get(0));

		if (pixelList.size() == 1) {
			return;
		}

		for (int i = 1; i < size; i++) {
			Pixel currentPixel = pixelList.get(i);
			if (currentPixel.getXCoordinate() == pixelList.get(i - 1).getXCoordinate()) {
				pixelColumnMap.get(currentColumn).add(currentPixel);
			} else {
				currentColumn++;
				pixelColumnMap.put(currentColumn, new ArrayList<Pixel>());
				pixelColumnMap.get(currentColumn).add(currentPixel);
			}
		}

	}

	public void fillParticle() {

		/*
		 * For each key in the map which corresponds to a particle row, find all the
		 * edge pixels in that row (classified as having a white neighbour during
		 * thresholding), and add them to a mini list.
		 */

		for (Integer rowKey : pixelRowMap.keySet()) {
			List<Pixel> row = pixelRowMap.get(rowKey);
			List<Pixel> edgeInRow = new ArrayList<Pixel>();
			for (Pixel pixel : row) {
				if (pixel.getEdgePixelHorizontal()) {
					edgeInRow.add(pixel);
				}
				if (pixel.getEdgePixel()) {
					edgePixels.add(pixel);
				}
			}

			/*
			 * If the row is made of one pixel, add it to the list of all pixels and move to
			 * next row.
			 */

			if (edgeInRow.size() == 1) {
				listOfAllPixels.add(edgeInRow.get(0));
				edgePixels.add(edgeInRow.get(0));
				continue;
			}

			/*
			 * We want to add all pixels between each set of edge pixels in a row. So find
			 * how many edge pixels there are, divide by 2 to know the number of edge
			 * segments we have to iterate over
			 */

			int internalPortions = edgeInRow.size() / 2;
			int pixelsToAdd = 0;

			/*
			 * For standard 2 edges, start pixel is get(0), aka iteration 0 get (i * 2) and
			 * get (0+1). For 4 edges, start is still get (0), second start is for iteration
			 * i = 1, get (i * 2)
			 */

			for (int i = 0; i < internalPortions; i++) {

				Pixel startOfRow = edgeInRow.get(i * 2);
				int startOfRowX = startOfRow.getXCoordinate();
				int startOfRowY = startOfRow.getYCoordinate();

				pixelsToAdd = edgeInRow.get((i * 2) + 1).getXCoordinate() - startOfRow.getXCoordinate() + 1;

				for (int j = 0; j < pixelsToAdd; j++) {
					listOfAllPixels.add(new Pixel(startOfRowX + j, startOfRowY));
				}
			}
		}

		/*
		 * At this point I have a list of all pixels that were thresholded as black, and
		 * now need to apply a method to fill in the white interiors that are completely
		 * contained.
		 */

		findEdges();

	}

	public void findEdges() {

		for (Pixel pixel : edgePixels) {
			orderedEdges.add(pixel);
		}

		freemanPerimeter();

	}

	public void orderEdges() {
		for (Pixel pixel : edgePixels) {
			orderedEdges.add(pixel);
		}

		Collections.sort(orderedEdges, new ComparePixelByX());
	}

	public void particleProperties() {
		if (!inFocus) {
			// return;
		}

		size();
		area();
		calcShapeFactor();

	}

	public void freemanPerimeter() {

		freemanPerimeterPixels.clear();
		freemanChainCode.clear();
		startingPixel = new Pixel(0, 0);

		int iterations = 2;

		Collections.sort(orderedEdges, compareByX);

		Pixel edgePixel = orderedEdges.get(0);
		startingPixel = orderedEdges.get(0);

		freemanPerimeterPixels.add(startingPixel);

		edgePixel = (freemanNeighbour(edgePixel));
		freemanPerimeterPixels.add(edgePixel);

		while (!edgePixel.equals(startingPixel)) {
			edgePixel = (freemanNeighbour(edgePixel));
			freemanPerimeterPixels.add(edgePixel);

			if (iterations > orderedEdges.size() * 1.1) {
				completeEdge = false;
				break;
			}
			iterations++;
		}

		if (completeEdge) {
			if (orderedEdges.size() != freemanPerimeterPixels.size()) {

				internalShapes();

			}
			Iterator<Pixel> perimeterIterator = orderedEdges.iterator();

			while (perimeterIterator.hasNext()) {
				if (!freemanPerimeterPixels.contains(perimeterIterator.next())) {
					perimeterIterator.remove();
				}
			}
			freemanPerimeterLength();
			particleProperties();
		}

	}

	public void internalShapes() {

		List<Pixel> otherEdges = new ArrayList<Pixel>();

		for (Pixel edgePixel : orderedEdges) {
			if (!freemanPerimeterPixels.contains(edgePixel)) {
				otherEdges.add(edgePixel);
			}
		}

		Iterator<Pixel> otherEdgesIterator = otherEdges.iterator();

		while (otherEdgesIterator.hasNext()) {
			if (!otherEdgesIterator.next().getEdgePixelHorizontal()) {
				otherEdgesIterator.remove();
			}
		}

		if (otherEdges.size() == 1) {
			return;
		}

		int edgePairs = otherEdges.size();
		int pixelsToAdd = 0;

		if (edgePairs % 2 != 0) {
			if (otherEdges.get(0).getYCoordinate() != otherEdges.get(1).getYCoordinate()) {
				otherEdges.remove(0);
			} else {
				otherEdges.remove(edgePairs - 1);
			}
		}
		
		edgePairs = otherEdges.size();

		for (int i = 0; i < edgePairs; i += 2) {
			Pixel lowEdge = otherEdges.get(i);
			int lowEdgeX = lowEdge.getXCoordinate();
			int lowEdgeY = lowEdge.getYCoordinate();

			try {
				pixelsToAdd = otherEdges.get((i) + 1).getXCoordinate() - lowEdge.getXCoordinate() + 1;

				for (int j = 0; j < pixelsToAdd; j++) {
					listOfAllPixels.add(new Pixel(lowEdgeX + j, lowEdgeY));
				}
			} catch (IndexOutOfBoundsException e) {
			}

		}

	}

	public boolean completeEdge() {
		return completeEdge;
	}

	public void size() {

		for (Integer rowKey : pixelRowMap.keySet()) {
			List<Pixel> currentRow = pixelRowMap.get(rowKey);
			int intermediateDiameter = currentRow.get(currentRow.size() - 1).getXCoordinate() -
					currentRow.get(0).getXCoordinate() + 1;

			if (intermediateDiameter > horizontalDiameter) {
				horizontalDiameter = intermediateDiameter;
			}
		}

		createColumnMap();

		for (Integer rowKey : pixelColumnMap.keySet()) {
			List<Pixel> currentColumn = pixelColumnMap.get(rowKey);
			int intermediateDiameter = currentColumn.get(currentColumn.size() - 1).getYCoordinate() -
					currentColumn.get(0).getYCoordinate() + 1;

			if (intermediateDiameter > verticalDiameter) {
				verticalDiameter = intermediateDiameter;
			}
		}

		setCentre();
		draw45Line();
		draw135Line();

		/*
		 * Add diagonal measures. "Draw" a 45 degree line, increment it away until none
		 * of it's pixels equal the pixels of the shape
		 */

		horizontalDiameter *= 0.375;
		verticalDiameter *= 0.375;
		diagonal45Diameter *= 0.375;
		diagonal135Diameter *= 0.375;

		/*
		 * size = ((horizontalDiameter + verticalDiameter + diagonal45Diameter +
		 * diagonal135Diameter) / 4);
		 */

		size = (horizontalDiameter + verticalDiameter) / 2;
	}

	public void draw45Line() {
		Pixel centrePixel = new Pixel(centreX, centreY);
		int segmentLength = (int) ((horizontalDiameter + verticalDiameter) / 4 * 1.5);
		List<Pixel> pixel45Line = new ArrayList<Pixel>();
		pixel45Line.add(centrePixel);

		for (int i = 1; i < segmentLength; i++) {
			pixel45Line.add(new Pixel(centreX + i, centreY - i));
			pixel45Line.add(new Pixel(centreX - i, centreY + i));
		}
		Collections.sort(pixel45Line, compareByX);

		diameter45(pixel45Line);

	}

	public void draw135Line() {
		Pixel centrePixel = new Pixel(centreX, centreY);
		int segmentLength = (int) ((horizontalDiameter + verticalDiameter) / 4 * 1.5);
		List<Pixel> pixel135Line = new ArrayList<Pixel>();
		pixel135Line.add(centrePixel);

		for (int i = 1; i < segmentLength; i++) {
			pixel135Line.add(new Pixel(centreX - i, centreY - i));
			pixel135Line.add(new Pixel(centreX + i, centreY + i));
		}
		Collections.sort(pixel135Line, compareByX);

		diameter135(pixel135Line);

	}

	public void diameter45(List<Pixel> pixel45Line) {
		List<Pixel> pixel45LineUp = createPixelList(pixel45Line);
		List<Pixel> pixel45LineDown = createPixelList(pixel45Line);

		boolean lineTouching = true;
		int pixelsInLine = pixel45Line.size();
		int upSteps = 0;
		int downSteps = 0;

		while (lineTouching) {
			int notTouchingPixels = 0;
			for (Pixel linePixel : pixel45LineUp) {
				if (!listOfAllPixels.contains(linePixel)) {
					notTouchingPixels++;
				}
			}

			if (notTouchingPixels == pixelsInLine) {
				break;
			}

			for (Pixel linePixel : pixel45LineUp) {
				linePixel.setX(linePixel.getXCoordinate() - 1);
				linePixel.setY(linePixel.getYCoordinate() - 1);

			}
			upSteps++;
		}

		while (lineTouching) {
			int notTouchingPixels = 0;
			for (Pixel linePixel : pixel45LineDown) {
				if (!listOfAllPixels.contains(linePixel)) {
					notTouchingPixels++;
				}
			}

			if (notTouchingPixels == pixelsInLine) {
				lineTouching = false;
				break;
			}

			for (Pixel linePixel : pixel45LineDown) {
				linePixel.setX(linePixel.getXCoordinate() + 1);
				linePixel.setY(linePixel.getYCoordinate() + 1);

			}
			downSteps++;
		}

		diagonal45Diameter = (Math.sqrt(2) * (upSteps + downSteps));

	}

	public void diameter135(List<Pixel> pixel135Line) {
		List<Pixel> pixel135LineUp = createPixelList(pixel135Line);
		List<Pixel> pixel135LineDown = createPixelList(pixel135Line);

		boolean lineTouching = true;
		int pixelsInLine = pixel135Line.size();
		int upSteps = 0;
		int downSteps = 0;

		while (lineTouching) {
			int notTouchingPixels = 0;
			for (Pixel linePixel : pixel135LineUp) {
				if (!listOfAllPixels.contains(linePixel)) {
					notTouchingPixels++;
				}
			}

			if (notTouchingPixels == pixelsInLine) {
				break;
			}

			for (Pixel linePixel : pixel135LineUp) {
				linePixel.setX(linePixel.getXCoordinate() + 1);
				linePixel.setY(linePixel.getYCoordinate() - 1);

			}
			upSteps++;
		}

		while (lineTouching) {
			int notTouchingPixels = 0;
			for (Pixel linePixel : pixel135LineDown) {
				if (!listOfAllPixels.contains(linePixel)) {
					notTouchingPixels++;
				}
			}

			if (notTouchingPixels == pixelsInLine) {
				lineTouching = false;
				break;
			}

			for (Pixel linePixel : pixel135LineDown) {
				linePixel.setX(linePixel.getXCoordinate() - 1);
				linePixel.setY(linePixel.getYCoordinate() + 1);

			}
			downSteps++;
		}

		diagonal135Diameter = (Math.sqrt(2) * (upSteps + downSteps));

	}

	public List<Pixel> createPixelList(List<Pixel> listToCopy) {
		List<Pixel> copy = new ArrayList<Pixel>();
		for (Pixel copyPixel : listToCopy) {
			copy.add(new Pixel(copyPixel.getXCoordinate(), copyPixel.getYCoordinate()));
		}
		return copy;
	}

	public void setCentre() {
		double xTotal = 0;
		double yTotal = 0;

		for (Pixel pixelInAll : listOfAllPixels) {
			xTotal += pixelInAll.getXCoordinate();
			yTotal += pixelInAll.getYCoordinate();
		}

		centreX = (int) (xTotal / listOfAllPixels.size());
		centreY = (int) (yTotal / listOfAllPixels.size());

	}

	public Double calcShapeFactor() {
		shapeFactor = ((4 * Math.PI * area) / (Math.pow(perimeter, 2)));

		return shapeFactor;
	}

	public void area() {
		area = listOfAllPixels.size();
		area *= 0.140625;
	}

	public Double getPerimeter() {
		return perimeter;
	}

	public Double getArea() {
		return area;
	}

	public List<Pixel> getAllPixels() {
		return listOfAllPixels;
	}

	public List<Pixel> getEdgePixels() {
		return orderedEdges;
	}

	public void printEdges() {
		for (Pixel pixel : orderedEdges) {
			System.out.println(pixel);
		}
	}

	public boolean validParticle() {
		return inFocus;
	}

	public void setValidParticle(boolean validity) {
		inFocus = validity;

		// particleProperties();
	}

	public Pixel freemanNeighbour(Pixel pixel) {

		List<Pixel> helperPixelEven = generateEvenNeighbourList(pixel);
		List<Pixel> helperPixelOdd = generateOddNeighbourList(pixel);
		List<Pixel> helperPixelAll = generateNeighbourList(pixel);
		int freemanPosition = 0;

		for (int i = 0; i < 4; i++) {
			if (orderedEdges.contains(helperPixelEven.get(i))) {
				if (!freemanPerimeterPixels.contains(helperPixelEven.get(i))) {
					freemanPosition = i * 2;
					freemanChainCode.add(freemanPosition);
					return helperPixelEven.get(i);
				}
			}
		}

		for (int i = 0; i < 4; i++) {
			if (orderedEdges.contains(helperPixelOdd.get(i))) {
				if (!freemanPerimeterPixels.contains(helperPixelOdd.get(i))) {
					freemanPosition = i * 2 + 1;
					freemanChainCode.add(freemanPosition);
					return helperPixelOdd.get(i);
				}
			}
		}

		for (int i = 0; i < 8; i++) {
			if (helperPixelAll.get(i).equals(startingPixel)) {
				freemanPosition = i;
				freemanChainCode.add(freemanPosition);
				return startingPixel;
			}
		}

		for (int i = 0; i < 4; i++) {
			if (orderedEdges.contains(helperPixelEven.get(i))) {
				return helperPixelEven.get(i);
			}
		}

		for (int i = 0; i < 4; i++) {
			if (orderedEdges.contains(helperPixelOdd.get(i))) {
				return helperPixelOdd.get(i);
			}
		}

		return new Pixel(1234123, 1234123);
	}

	public void freemanPerimeterLength() {
		int straight = 0;
		int diagonal = 0;
		int cornerCount = cornerCount(freemanChainCode);

		for (int freemanCode : freemanChainCode) {
			if (freemanCode % 2 == 0 || freemanCode == 0) {
				straight++;
			} else {
				diagonal++;
			}
		}

		// perimeter = (straight + (Math.sqrt(2) * diagonal)) * 0.375;

		/*
		 * Formula from
		 * "METHODS TO ESTIMATE AREAS AND PERIMETERS OF BLOB-LIKE OBJECTS: A COMPARISON"
		 */

		//perimeter = (0.980 * straight + 1.406 * diagonal - 0.091 * cornerCount) * 0.375;
		perimeter = (0.980 * straight + 1.406 * diagonal) * 0.375;
	}

	public int cornerCount(List<Integer> freemanCode) {
		List<Integer> listToCount = freemanCode;
		int sizeCode = listToCount.size();
		if (sizeCode == 1) {
			return 0;
		}
		int cornerCount = 0;

		for (int i = 1; i < sizeCode; i++) {
			if (listToCount.get(i) != listToCount.get(i - 1)) {
				cornerCount++;
			}
		}

		return cornerCount;

	}

	public List<Pixel> generateNeighbourList(Pixel pixel) {
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

		return helperPixelList;
	}

	public List<Pixel> generateEvenNeighbourList(Pixel pixel) {
		Pixel up = new Pixel(pixel.getXCoordinate(), pixel.getYCoordinate() - 1);
		Pixel down = new Pixel(pixel.getXCoordinate(), pixel.getYCoordinate() + 1);
		Pixel left = new Pixel(pixel.getXCoordinate() - 1, pixel.getYCoordinate());
		Pixel right = new Pixel(pixel.getXCoordinate() + 1, pixel.getYCoordinate());

		List<Pixel> helperPixelList = new ArrayList<Pixel>();

		Collections.addAll(helperPixelList, right, up, left, down);

		return helperPixelList;
	}

	public List<Pixel> generateOddNeighbourList(Pixel pixel) {
		Pixel upRight = new Pixel(pixel.getXCoordinate() + 1, pixel.getYCoordinate() - 1);
		Pixel upLeft = new Pixel(pixel.getXCoordinate() - 1, pixel.getYCoordinate() - 1);
		Pixel downRight = new Pixel(pixel.getXCoordinate() + 1, pixel.getYCoordinate() + 1);
		Pixel downLeft = new Pixel(pixel.getXCoordinate() - 1, pixel.getYCoordinate() + 1);

		List<Pixel> helperPixelList = new ArrayList<Pixel>();

		Collections.addAll(helperPixelList, upRight, upLeft, downLeft, downRight);

		return helperPixelList;
	}

}
