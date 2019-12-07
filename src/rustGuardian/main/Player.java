package rustGuardian.main;

import javafx.geometry.Point3D;

/**
 * Player class which currently represents the character that can be controlled.
 * <p>
 * Can only move now. Stores position in a {@link java.awt.Point}
 */
public class Player extends AbstractMoveable {
	private int sightRad;

	public int sightRad() {
		return sightRad;
	}

	public Player(Point3D initPos) {
		super(initPos, true, '@', false);
		sightRad = 7;
	}
}
