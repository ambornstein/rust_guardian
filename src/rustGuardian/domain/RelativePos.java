package rustGuardian.domain;

import java.awt.Point;

import javafx.geometry.Point3D;

/**
 * A utility class which stores a position based on chunk coordinates and the
 * coordinates within the chunk. It can be used to determine whether the
 * position is in the world bounds. A RelativePos can be converted to a Point
 * which represents a position in the world irrespective of chunk points. Static
 * method toRel() returns a RelativePos from a Point. RelativePos always
 * requires a static generator to determine the length and width of chunks.
 *
 * RelativePos are stored as literal integers, meaning 1 for 1, not 0 for 1 They
 * used to be stored as start-at-0 integers, although when converting to
 * absolute, 1 integer was lost for every chunk that was present within the
 * position (chunk width 11: world width 3: absolute 33; chunk width 12: world
 * width 3: absolute 36) Giving true ints instead of start-at-0 ints makes a lot
 * of difference
 * 
 * @author ambor
 */
public class RelativePos {
	private Point chunkPoint;
	private Point3D tilePoint;
	private static Generator generator;
	private static Generator zero = new Generator(0, 0, 0, 0, 0);

	/**
	 * Getter for chunkPoint
	 * 
	 * @return chunkPoint The coordinate location of the chunk in which this point
	 *         resides
	 */
	public Point chunkPoint() {
		return chunkPoint;
	}

	/**
	 * Getter for tilePoint
	 * 
	 * @return tilePoint The coordinate location of the tile in which this point
	 *         resides, with regard to its chunk's origin
	 */
	public Point3D tilePoint() {
		return tilePoint;
	}

	public static void setGenerator(Generator newGenerator) {
		generator = newGenerator;
	}

	public static Generator generator() {
		return generator;
	}

	public RelativePos(int chunkX, int chunkY, int tileX, int tileY, int tileZ) {
		this.chunkPoint = new Point(chunkX, chunkY);
		this.tilePoint = new Point3D(tileX, tileY, tileZ);
	}

	public RelativePos(Point chunkPoint, Point tilePoint) {
		this.chunkPoint = chunkPoint;
		this.tilePoint = new Point3D(tilePoint.x, tilePoint.y, 5);
	}

	public RelativePos(Point chunkPoint, Point3D tilePoint) {
		this.chunkPoint = chunkPoint;
		this.tilePoint = tilePoint;
	}

	public RelativePos(RelativePos same) {
		this.chunkPoint = same.chunkPoint();
		this.tilePoint = same.tilePoint();
	}

	/**
	 * Determines the whether the test object falls within, on the outside, on the
	 * border, or short of this object
	 *
	 * @param testRelative The other RelativePos object
	 * @return n Signifies the result of the comparison (1 = test is greater than
	 *         the boundaries, 0 = test is equal to the boundaries, -1 = test is
	 *         within the boundaries, -2 = test is less than origin (0,0,0))
	 */
	public int compare(RelativePos testRelative) { // returns the value of testRelative in relation to this
		if (testRelative.chunkPoint.x <= 0 || testRelative.chunkPoint.y <= 0 || testRelative.tilePoint.getX() < 0
				|| testRelative.tilePoint.getY() < 0 || testRelative.tilePoint.getZ() < 0) { // -2 = rel less than 0
			return -2;
		} else if (testRelative.chunkPoint.equals(chunkPoint) && testRelative.tilePoint.equals(tilePoint)) {
			// 0 = this rel and testRel equal
			return 0;
		} else if (testRelative.chunkPoint().x <= chunkPoint.x && testRelative.chunkPoint().y <= chunkPoint.y
				&& testRelative.tilePoint().getX() <= tilePoint.getX()
				&& testRelative.tilePoint().getY() <= tilePoint.getY()
				&& testRelative.tilePoint().getZ() <= tilePoint.getZ()) { // -1 = testRel is less than this rel
			return -1;
		} else {
			return 1;
		} // testRel is greater than this rel
	}

	public static Point3D correctOutOfBounds(Point3D pos) {
		Point3D copyPos = new Point3D((int) pos.getX(), (int) pos.getY(), (int) pos.getZ());
		Point3D generatorAbs = generator.toAbs(); // shortname to store world bounds
		if (generator.compare(toRel(pos)) == 1) { // Correcting for greater than bounds positions
			// System.out.println("Out of Bounds");
			Point3D maxModPos = new Point3D(pos.getX() % generatorAbs.getX(), pos.getY() % generatorAbs.getY(),
					pos.getZ() % generatorAbs.getZ());
			// remainder from the point being divided by the map's absolute dimensions
			double correctX = maxModPos.getX() == copyPos.getX() ? copyPos.getX()
					: (copyPos.getX() - maxModPos.getX()) / ((copyPos.getX() - maxModPos.getX()) / generatorAbs.getX());
			// only subtract the mod if it is different from the original value
			double correctY = maxModPos.getY() == copyPos.getY() ? copyPos.getY()
					: (copyPos.getY() - maxModPos.getY()) / ((copyPos.getY() - maxModPos.getY()) / generatorAbs.getY());
			// and make sure to correct any multiples of absoluteDims that mod missed
			double correctZ = maxModPos.getZ() == copyPos.getZ() ? copyPos.getZ()
					: (copyPos.getZ() - maxModPos.getZ()) / ((copyPos.getZ() - maxModPos.getZ()) / generatorAbs.getZ());
			copyPos = new Point3D(correctX, correctY, correctZ);
		} else if (generator.compare(toRel(pos)) == -2) { // Correcting for less than bounds positions (-1,-1)
			// System.out.println("Less than Bounds");
			Point3D absValPos = new Point3D(Math.abs(pos.getX()), Math.abs(pos.getY()), Math.abs(pos.getZ()));
			// Adding the absolute val of a negative integer to itself if it is negative
			// makes the result 0
			copyPos = new Point3D(copyPos.getX() + absValPos.getX(), copyPos.getY() + absValPos.getY(),
					copyPos.getZ() + absValPos.getZ()); // Negative values in copypos are now 0, but positive values are
														// now doubled
			copyPos = new Point3D(copyPos.getX() / 2, copyPos.getY() / 2, copyPos.getZ() / 2);
			// The doubled positive integers are now halved and normal, but the 0 values are
			// unaffected
		} else { // no need to correct what is in bounds
			// System.out.println("In Bounds");
			return pos;
		}
		return copyPos;
	}

	@Override
	public boolean equals(Object o) {
		if (o.getClass() == getClass()) {
			if (((RelativePos) o).tilePoint().equals(tilePoint) && ((RelativePos) o).chunkPoint().equals(chunkPoint)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public RelativePos clone() {
		return new RelativePos((Point) chunkPoint.clone(),
				new Point3D(tilePoint.getX(), tilePoint.getY(), tilePoint.getZ()));
	}

	/**
	 * A method to convert an absolute Point to a RelChunkPos object
	 *
	 * @param absPoint Represents an absolute position within the World. To be
	 *                 converted to a RelativePos object.
	 * @return The RelChunkPos derived from an absolute Point.
	 */
	public static RelativePos toRel(Point3D absPoint) {
		Point chunkLoc = new Point((int) Math.floor((absPoint.getX()) / generator.tilePoint().getX()) + 1, // Finds out
																											// what
																											// chunk the
																											// absPoint
																											// falls in
				(int) Math.floor((absPoint.getY()) / generator.tilePoint().getY()) + 1); // Can't use ceiling because
																							// the ceiling of 0 is 0
		Point3D tileLoc = new Point3D(((absPoint.getX() + 1) - ((generator.tilePoint().getX() - 1) * (chunkLoc.x - 1))),
				((absPoint.getY() + 1) - ((generator.tilePoint().getY() - 1) * (chunkLoc.y - 1))), absPoint.getZ() + 1);
		// Cuts off the amount of tiles that are outside this chunk
		return new RelativePos(chunkLoc, tileLoc);
	}

	/**
	 * A method to convert this into an absolute Point
	 *
	 * @return The RelChunkPos object's position expressed as an absolute Point in
	 *         the World, irrespective of chunk
	 */
	public Point3D toAbs() {
		Point chunkShift = new Point((int) ((chunkPoint.x - 1) * generator.tilePoint().getX()),
				(int) ((chunkPoint.y - 1) * generator.tilePoint().getY()));
		Point3D absReturn = new Point3D(tilePoint.getX() - 1 + chunkShift.x, tilePoint.getY() - 1 + chunkShift.y,
				tilePoint.getZ() - 1); // .add for Point3D is just broken
		return absReturn;
	}

	/**
	 * Shifts the position of this by the amount given in arguments
	 *
	 * @param xShift How much to shift on the x axis
	 * @param yShift How much to shift on the y axis
	 */
	public void shift(Point3D pointShift) {
		Point3D utilAbs = toAbs();
		utilAbs.add(pointShift.getX(), pointShift.getY(), pointShift.getZ());
		RelativePos.toRel(utilAbs);
	}

	/**
	 * Returns a clone of this RelativePos which is shifted by the desired amount
	 *
	 * @param xShift How much to shift on the x axis
	 * @param yShift How much to shift on the y axis
	 * @return A clone of this object shifted by specified amounts
	 */
	public RelativePos readOnlyShift(int xShift, int yShift, int zShift) {
		RelativePos returnPos = clone();
		Point3D utilAbs = returnPos.toAbs();
		utilAbs.add(xShift, yShift, zShift);
		return RelativePos.toRel(utilAbs);
	}

	@Override
	public String toString() {
		return "Chunk:" + chunkPoint.x + "," + chunkPoint.y + "; Tile:" + tilePoint.getX() + "," + tilePoint.getY()
				+ "," + tilePoint.getZ() + ";";
	}
}