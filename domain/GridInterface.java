package rustGuardian.domain;

import java.awt.Point;

public interface GridInterface<E> {
	public int length();

	public int width();

	public int height();

	public void fill();

	public E unitAt(Point loc);
}