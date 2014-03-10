package tools;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;

public class Grass {
	private static Shape grassShape;

	public static Shape grassFor(Shape s, int h, int w, int gap) {
		grassShape = getGrass(h, w);
		return new ShapeStroke(grassShape, gap).createStrokedShape(s);
	}

	private static Shape getGrass(int h, int w) {
		GeneralPath gp = new GeneralPath();
		gp.moveTo(0, h);
		gp.quadTo(0, h / 2, w / 2, 0);
		gp.quadTo(w / 2, h / 2, w, h);
		gp.closePath();
		AffineTransform at = AffineTransform.getRotateInstance(20 * Math.PI / 180);
		return at.createTransformedShape(gp);
	}
}
