package tools;

import java.awt.Point;

public class ComparablePoint extends Point implements Comparable<Point> {
	ComparePointBy compare = ComparePointBy.AVERAGE;
	/**
	 * 
	 */
	private static final long serialVersionUID = 8399005428213244890L;

	public ComparablePoint(ComparePointBy compare) {
		super();
		if (compare != null) {
			this.compare = compare;
		}
	}

	public ComparablePoint(Point p, ComparePointBy compare) {
		super(p);
		if (compare != null) {
			this.compare = compare;
		}
	}

	public ComparablePoint(int x, int y, ComparePointBy compare) {
		super(x, y);
		if (compare != null) {
			this.compare = compare;
		}
	}

	public static enum ComparePointBy {
		X, Y, AVERAGE;
	}

	@Override
	public int compareTo(Point o) {
		if (o == null) {
			throw new NullPointerException("Comparing object cannot be null");
		}
		if (this.equals(o)) {
			return 0;
		}
		int result = 0;
		switch (compare) {
		case X:
			result = compareByX(o.x);
			break;
		case Y:
			result = compareByY(o.y);
			break;
		case AVERAGE:
			result = compareByAverage(o.x, o.y);
			break;
		}
		return result;
	}

	public int compareByX(int x) {
		if (x < this.x) {
			return 1;
		} else if (x == this.x) {
			return 0;
		} else if (x > this.x) {
			return -1;
		}
		return 0;
	}

	public int compareByY(int y) {
		if (y < this.y) {
			return 1;
		} else if (y == this.y) {
			return 0;
		} else if (y > this.y) {
			return -1;
		}
		return 0;
	}

	public int compareByAverage(int x, int y) {
		int average = (x + y) / 2;
		int thisAverage = (this.x + this.y) / 2;
		if (average < thisAverage) {
			return 1;
		} else if (average == thisAverage) {
			return 0;
		} else if (average > thisAverage) {
			return -1;
		}
		return 0;
	}
}
