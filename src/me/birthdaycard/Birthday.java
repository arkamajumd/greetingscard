package me.birthdaycard;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.io.IOException;
import java.net.URL;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

import com.jhlabs.image.ShadowFilter;

import tools.Callback;
import tools.ComparablePoint;
import tools.ComparablePoint.ComparePointBy;
import tools.Grass;
import tools.TextStroke;

/**
 * Made during the summer of 2014.
 * 
 * @author Arka Majumdar
 */
public class Birthday implements Callback {
	public BufferedImage img = null;
	public JPanel p;
	public final Random rnd = new Random();
	// The "Dear ______," that will be written at the top of the card
	public static String dear = "Dear Person,";
	// Text that will appear on the grass
	public static String giant = "MESSAGE";
	// Title of the JFrame
	public static String title = "Dear Person...";
	private boolean doneLoading = false;

	private Birthday() {
		new Start(this).setVisible(true);
	}

	public static void main(String[] args) {
		if (args.length > 0) {
			dear = args[0];
			if (args.length > 1) {
				giant = args[1];
				if (args.length > 2) {
					title = args[2];
				}
			}
		}
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new Birthday();
			}
		});
	}

	@Override
	public void start() {
		JFrame anim = new JFrame(title);
		p = new JPanel() {
			private static final long serialVersionUID = 1L;

			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				if (!doneLoading) {
					URL waitURL = getClass().getResource("wait.png");
					if (waitURL == null) {
						System.err.println("wait.png not found");
					}
					ImageIcon wait = new ImageIcon(waitURL);
					wait.paintIcon(p, g, p.getWidth() / 2 - wait.getIconWidth() / 2, p.getHeight() / 2 - wait.getIconHeight() / 2);
				}
				((Graphics2D) g).drawImage(img, null, 0, 0);
			}
		};
		anim.setContentPane(p);
		anim.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		anim.setExtendedState(Frame.MAXIMIZED_BOTH);
		anim.setVisible(true);
		anim.setMinimumSize(anim.getSize());
		new TimingPainter();
	}

	private boolean randomCloudsOnce = false;
	public Point[] cloudPoints = new Point[3];

	public void randomCloudsOnce(JComponent c, Graphics2D g) {
		if (randomCloudsOnce) {
			return;
		} else {
			randomCloudsOnce = true;
			for (int i = 0; i < cloudPoints.length; i++) {
				int x = rnd.nextInt(c.getWidth() - LandscapePainter.cloud.getIconWidth());
				int y = rnd.nextInt((int) (c.getHeight() * 0.375) - LandscapePainter.cloud.getIconHeight());
				LandscapePainter.paintCloudOnto(c, x, y, g, 0.0f);
				cloudPoints[i] = new Point(x, y);
			}
		}
	}

	private boolean randomFlowersOnce = false;
	public ComparablePoint[] flowerPoints = new ComparablePoint[45];
	public LandscapePainter.FlowerType[] flowerTypes = new LandscapePainter.FlowerType[flowerPoints.length];// Since we're being random about a lot, we have to keep track of things

	public void randomFlowersOnce(JComponent c, Graphics2D g) {
		if (randomFlowersOnce) {
			return;
		} else {
			BufferedImage bi = new BufferedImage(c.getWidth(), c.getHeight(), BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2d = bi.createGraphics();
			g2d.setClip(LandscapePainter.getFlowerAreaNow(c));
			randomFlowersOnce = true;
			int x, y;
			LandscapePainter.FlowerType flowertype = null;
			for (int i = 0; i < flowerPoints.length; i++) {
				x = rnd.nextInt(c.getWidth() - LandscapePainter.averageFlowerSize().width);
				final int height = (int) (c.getHeight() * 0.625 - LandscapePainter.averageFlowerSize().height);
				y = rnd.nextInt(c.getHeight() - LandscapePainter.averageFlowerSize().height - height) + height;
				if (!LandscapePainter.getFlowerAreaNow(c).contains(new Point.Double(x, y))) {
					y = bringDownToGround(c, x, y).y;
				}
				switch (rnd.nextInt(4)) {
				case 0:
					flowertype = LandscapePainter.FlowerType.WHITE;
					break;
				case 1:
					flowertype = LandscapePainter.FlowerType.PINK;
					break;
				case 2:
					flowertype = LandscapePainter.FlowerType.RED;
					break;
				case 3:
					flowertype = LandscapePainter.FlowerType.BLUE;
					break;
				}
				LandscapePainter.paintFlowerOnto(c, x, y, flowertype, g2d, 0.0f);
				flowerPoints[i] = new ComparablePoint(x, y, ComparePointBy.Y);
				flowerTypes[i] = flowertype;
			}
			g.drawImage(bi, null, 0, 0);
			/* // */Arrays.sort(flowerPoints);
			// flowerTypes isn't Comparable and it's supposed to be random so it doesn't matter
		}
	}

	private Point bringDownToGround(JComponent c, int x, int y) {
		int newY = y;
		if (x >= 0 && x < c.getWidth() * 0.25) {
			newY = (int) (c.getHeight() * 0.75 - LandscapePainter.averageFlowerSize().height);
		} else if (x >= c.getWidth() * 0.25 && x <= c.getWidth() * 0.75) {
			final int curveLinearWidth = (int) (c.getWidth() * 0.75 - c.getWidth() * 0.25);
			final int t = (int) ((x - c.getWidth() * 0.25) / curveLinearWidth);
			final int u = 1 - t;
			final int p0 = (int) (c.getHeight() * 0.25);// y of 1st cubic bezier curve control point
			final int p1 = (int) (c.getHeight() * 0.75);// y of 2nd cubic bezier curve control point
			final int p2 = (int) (c.getHeight() * 0.625);// y of 3rd cubic bezier curve control point
			final int p3 = p2;// y of 4st cubic bezier curve control point
			newY = u * u * u * p0 + 3 * u * u * t * p1 + 3 * u * t * t * p2 + t * t * t * p3;// cubic bezier curve formula as specified by Wikipedia (explicit cubic form)
		} else if (x > c.getWidth() * 0.75 && x <= c.getWidth()) {
			newY = (int) (c.getHeight() * 0.625 - LandscapePainter.averageFlowerSize().height);
		}
		return new Point(x, newY);
	}

	// These are not used. Instead, I used the implementations that came with Java.
	// public static Point calculateCubicBezierPoint(Point p0, Point p1, Point p2, Point p3, int segmentsPerCurve, int seg) {
	// int x = 0;
	// int y = 0;
	// final int t = seg / segmentsPerCurve;
	// final int u = 1 - t;
	// x = u*u*u*p0.x + 3*u*u*t*p1.x + 3*u*t*t*p2.x + t*t*t*p3.x;
	// y = u*u*u*p0.y + 3*u*u*t*p1.y + 3*u*t*t*p2.y + t*t*t*p3.y;
	// return new Point(x, y);
	// }
	// public static Point calculateQuadraticBezierPoint(Point p0, Point p1, Point p2, int segmentsPerCurve, int seg) {
	// int x = 0;
	// int y = 0;
	// final int t = seg / segmentsPerCurve;
	// final int u = 1 - t;
	// x = u*u*p0.x + 2*u*t*p1.x + t*t*p2.x;
	// y = u*u*p0.y + 2*u*t*p1.y + t*t*p2.y;
	// return new Point(x, y);
	// }
	public Point[] getsquirrelMovements(JComponent c) {
		ArrayList<Point> points = new ArrayList<Point>();
		GeneralPath moves = new GeneralPath();
		for (int i = 0; i < 5; i++) {
			if (i == 0) {
				moves.moveTo(-LandscapePainter.squirrel.getIconWidth(), (int) (rnd.nextDouble() * c.getHeight() * 0.25));
			}
			moves.quadTo((i + 1) * (c.getWidth() / 5), (int) (rnd.nextDouble() * c.getHeight() * 0.25), (i + 2) * (c.getWidth() / 5), (int) (rnd.nextDouble() * c.getHeight() * 0.25));
		}
		FlatteningPathIterator f = new FlatteningPathIterator(moves.getPathIterator(null), 0.1);
		double[] pts = new double[6];
		while (!f.isDone()) {
			switch (f.currentSegment(pts)) {
			case PathIterator.SEG_MOVETO:
			case PathIterator.SEG_LINETO:
				Point p = new Point((int) pts[0], (int) pts[1]);
				p.translate(0, (int) (c.getHeight() * 0.75) - LandscapePainter.squirrel.getIconHeight());
				points.add(p);
				break;
			}
			f.next();
		}
		Point[] ptsArray = new Point[points.size()];
		return points.toArray(ptsArray);
	}

	private BufferedImage stars = null;
	private boolean alreadyInitializedTheStars = false;

	public void paintStars(JComponent c, Graphics2D g2d, float opacity, int num) {
		if (!alreadyInitializedTheStars) {
			stars = new BufferedImage(c.getWidth(), c.getHeight(), BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2d2 = stars.createGraphics();
			g2d2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
			int x, y;
			double scale;
			for (int i = 0; i < num; i++) {
				x = (int) (rnd.nextDouble() * c.getWidth());
				y = (int) (rnd.nextDouble() * c.getHeight());
				scale = rnd.nextDouble();
				LandscapePainter.star.setScale(scale);
				LandscapePainter.star.paintIcon(c, g2d2, x, y);
			}
			g2d2.dispose();
			alreadyInitializedTheStars = true;
		}
		Graphics2D g2d3 = (Graphics2D) g2d.create();
		g2d3.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
		g2d3.drawImage(stars, null, 0, 0);
		g2d3.dispose();
	}

	public static class LandscapePainter {// Completely STATIC!!!
		private static final GeneralPath shape = new GeneralPath();
		private static final GeneralPath grassArea = new GeneralPath();
		public static final AlreadyLoadedImageIcon cloud = new AlreadyLoadedImageIcon(LandscapePainter.class.getResource("cloud.png"));
		public static final AlreadyLoadedImageIcon whiteFlower = new AlreadyLoadedImageIcon(LandscapePainter.class.getResource("white_flower.png"));
		public static final AlreadyLoadedImageIcon pinkFlower = new AlreadyLoadedImageIcon(LandscapePainter.class.getResource("pink_flower.png"));
		public static final AlreadyLoadedImageIcon redFlower = new AlreadyLoadedImageIcon(LandscapePainter.class.getResource("red_flower.png"));
		public static final AlreadyLoadedImageIcon blueFlower = new AlreadyLoadedImageIcon(LandscapePainter.class.getResource("blue_flower.png"));
		public static final AlreadyLoadedImageIcon bird1 = new AlreadyLoadedImageIcon(LandscapePainter.class.getResource("bird_animation/bird1.png"));
		public static final AlreadyLoadedImageIcon bird2 = new AlreadyLoadedImageIcon(LandscapePainter.class.getResource("bird_animation/bird2.png"));
		public static final AlreadyLoadedImageIcon bird3 = new AlreadyLoadedImageIcon(LandscapePainter.class.getResource("bird_animation/bird3.png"));
		public static final AlreadyLoadedImageIcon bird4 = new AlreadyLoadedImageIcon(LandscapePainter.class.getResource("bird_animation/bird4.png"));
		public static final AlreadyLoadedImageIcon bird5 = bird4;
		public static final AlreadyLoadedImageIcon bird6 = bird3;
		public static final AlreadyLoadedImageIcon bird7 = bird2;
		public static final AlreadyLoadedImageIcon bird8 = bird1;
		public static final AlreadyLoadedImageIcon moon = new AlreadyLoadedImageIcon(LandscapePainter.class.getResource("moon.png"));
		public static final ScaledImageIcon star = new ScaledImageIcon(LandscapePainter.class.getResource("star.png"), 0.225/* average of 0.15 and 0.3 */);
		public static final PixelResizeImageIcon squirrel = new PixelResizeImageIcon(LandscapePainter.class.getResource("squirrel.gif"/* "bunny.gif" */), 1.5);
		public static final ScaledImageIcon whiteBird1 = new ScaledImageIcon(LandscapePainter.class.getResource("bird_animation/white1.png"), 1);
		public static final ScaledImageIcon whiteBird2 = new ScaledImageIcon(LandscapePainter.class.getResource("bird_animation/white2.png"), 1);
		public static final ScaledImageIcon whiteBird3 = new ScaledImageIcon(LandscapePainter.class.getResource("bird_animation/white3.png"), 1);
		public static final ScaledImageIcon whiteBird4 = new ScaledImageIcon(LandscapePainter.class.getResource("bird_animation/white4.png"), 1);
		public static final ScaledImageIcon whiteBird5 = new ScaledImageIcon(LandscapePainter.class.getResource("bird_animation/white5.png"), 1);
		public static final ScaledImageIcon whiteBird6 = whiteBird4;
		public static final ScaledImageIcon whiteBird7 = whiteBird3;
		public static final ScaledImageIcon whiteBird8 = whiteBird2;
		public static final ScaledImageIcon whiteBird9 = whiteBird1;

		public static java.awt.Dimension averageFlowerSize() {
			return new java.awt.Dimension((whiteFlower.getIconWidth() + pinkFlower.getIconWidth() + redFlower.getIconWidth() + blueFlower.getIconWidth()) / 4, (whiteFlower.getIconHeight() + pinkFlower.getIconHeight() + redFlower.getIconHeight() + blueFlower.getIconHeight()) / 4);
		}

		public static AlreadyLoadedImageIcon getBirdFor(int frame) {
			switch (frame) {
			case 1:
				return bird1;
			case 2:
				return bird2;
			case 3:
				return bird3;
			case 4:
				return bird4;
			case 5:
				return bird5;
			case 6:
				return bird6;
			case 7:
				return bird7;
			case 8:
				return bird8;
			}
			return null;
		}

		public static AlreadyLoadedImageIcon getWhiteBirdFor(int frame) {
			switch (frame) {
			case 1:
				return whiteBird1;
			case 2:
				return whiteBird2;
			case 3:
				return whiteBird3;
			case 4:
				return whiteBird4;
			case 5:
				return whiteBird5;
			case 6:
				return whiteBird6;
			case 7:
				return whiteBird7;
			case 8:
				return whiteBird8;
			case 9:
				return whiteBird9;
			}
			return null;
		}

		public static java.awt.Dimension averageBirdSize() {
			return new java.awt.Dimension((bird1.getIconWidth() + bird2.getIconWidth() + bird3.getIconWidth() + bird4.getIconWidth()) / 4, (bird1.getIconHeight() + bird2.getIconHeight() + bird3.getIconHeight() + bird4.getIconHeight()) / 4);
		}

		public static java.awt.Dimension maxWhiteBirdSize() {
			int resultHeight = whiteBird1.getIconHeight();
			resultHeight = Math.max(resultHeight, whiteBird2.getIconHeight());
			resultHeight = Math.max(resultHeight, whiteBird3.getIconHeight());
			resultHeight = Math.max(resultHeight, whiteBird4.getIconHeight());
			resultHeight = Math.max(resultHeight, whiteBird5.getIconHeight());
			int resultWidth = whiteBird1.getIconWidth();
			resultWidth = Math.max(resultWidth, whiteBird2.getIconWidth());
			resultWidth = Math.max(resultWidth, whiteBird3.getIconWidth());
			resultWidth = Math.max(resultWidth, whiteBird4.getIconWidth());
			resultWidth = Math.max(resultWidth, whiteBird5.getIconWidth());
			return new java.awt.Dimension(resultWidth, resultHeight);
		}

		public static java.awt.Dimension maxBirdSize() {
			int resultHeight = bird1.getIconHeight();
			resultHeight = Math.max(resultHeight, bird2.getIconHeight());
			resultHeight = Math.max(resultHeight, bird3.getIconHeight());
			resultHeight = Math.max(resultHeight, bird4.getIconHeight());
			int resultWidth = bird1.getIconWidth();
			resultWidth = Math.max(resultWidth, bird2.getIconWidth());
			resultWidth = Math.max(resultWidth, bird3.getIconWidth());
			resultWidth = Math.max(resultWidth, bird4.getIconWidth());
			return new java.awt.Dimension(resultWidth, resultHeight);
		}

		public static java.awt.Dimension minBirdSize() {
			int resultHeight = bird1.getIconHeight();
			resultHeight = Math.min(resultHeight, bird2.getIconHeight());
			resultHeight = Math.min(resultHeight, bird3.getIconHeight());
			resultHeight = Math.min(resultHeight, bird4.getIconHeight());
			int resultWidth = bird1.getIconWidth();
			resultWidth = Math.min(resultWidth, bird2.getIconWidth());
			resultWidth = Math.min(resultWidth, bird3.getIconWidth());
			resultWidth = Math.min(resultWidth, bird4.getIconWidth());
			return new java.awt.Dimension(resultWidth, resultHeight);
		}

		public static void paintCloudOnto(JComponent c, int x, int y, Graphics2D g2d, float opacity) {
			Graphics2D g2d2 = (Graphics2D) g2d.create();
			g2d2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
			cloud.paintIcon(c, g2d2, x, y);
			g2d2.dispose();
		}

		private static enum FlowerType {
			WHITE, PINK, RED, BLUE;
		}

		public static void paintFlowerOnto(JComponent c, int x, int y, FlowerType type, Graphics2D g2d, float opacity) {
			Graphics2D g2d2 = (Graphics2D) g2d.create();
			try {
				g2d2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
			} catch (IllegalArgumentException iae) {
				// System.out.println("Out of bounds opacity: " + opacity + ", clamped to 0.0f - 1.0f");
				// System.out.println(opacity);
				if (opacity > 1.0f) {
					opacity = 1.0f;
				} else if (opacity < 0.0f) {
					opacity = 0.0f;
				}
				g2d2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
			}
			switch (type) {
			case WHITE:
				whiteFlower.paintIcon(c, g2d2, x, y);
				break;
			case PINK:
				pinkFlower.paintIcon(c, g2d2, x, y);
				break;
			case RED:
				redFlower.paintIcon(c, g2d2, x, y);
				break;
			case BLUE:
				blueFlower.paintIcon(c, g2d2, x, y);
				break;
			}
			g2d2.dispose();
		}

		public static void paintMoon(JComponent c, Graphics2D g2d, int x, int y, float opacity) {
			Graphics2D g2d2 = (Graphics2D) g2d.create();
			g2d2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
			moon.paintIcon(c, g2d2, x, y);
			g2d2.dispose();
		}

		private static boolean alreadyInitializedTheGrassArea = false;
		private static boolean alreadyInitializedTheRollingHillsShape = false;

		public static int getYOnHillForX(JComponent c, int x) {
			int y = 0;
			if (x >= 0 && x < c.getWidth() * 0.25) {
				y = (int) (c.getHeight() * 0.75 - LandscapePainter.averageFlowerSize().height);
			} else if (x >= c.getWidth() * 0.25 && x <= c.getWidth() * 0.75) {
				final int curveLinearWidth = (int) (c.getWidth() * 0.75 - c.getWidth() * 0.25);
				final int t = (int) ((x - c.getWidth() * 0.25) / curveLinearWidth);
				final int u = 1 - t;
				final int p0 = (int) (c.getHeight() * 0.25);// y of 1st cubic bezier curve control point
				final int p1 = (int) (c.getHeight() * 0.75);// y of 2nd cubic bezier curve control point
				final int p2 = (int) (c.getHeight() * 0.625);// y of 3rd cubic bezier curve control point
				final int p3 = p2;// y of 4st cubic bezier curve control point
				y = u * u * u * p0 + 3 * u * u * t * p1 + 3 * u * t * t * p2 + t * t * t * p3;// cubic bezier curve formula as specified by Wikipedia (explicit cubic form)
			} else if (x > c.getWidth() * 0.75 && x <= c.getWidth()) {
				y = (int) (c.getHeight() * 0.625 - LandscapePainter.averageFlowerSize().height);
			}
			return y;
		}

		public static void paintRollingHills(JComponent c, Graphics2D g2d) {
			if (!alreadyInitializedTheGrassArea) {
				grassArea.moveTo(0, c.getHeight() * 0.75);
				grassArea.lineTo(c.getWidth() * 0.25, c.getHeight() * 0.75);
				grassArea.curveTo(c.getWidth() * 0.5, c.getHeight() * 0.75, c.getWidth() * 0.5, c.getHeight() * 0.625, c.getWidth() * 0.75, c.getHeight() * 0.625);
				grassArea.lineTo(c.getWidth(), c.getHeight() * 0.625);
				alreadyInitializedTheGrassArea = true;
			}
			if (!alreadyInitializedTheRollingHillsShape) {
				shape.moveTo(0, c.getHeight() * 0.75);
				shape.lineTo(c.getWidth() * 0.25, c.getHeight() * 0.75);
				shape.curveTo(c.getWidth() * 0.5, c.getHeight() * 0.75, c.getWidth() * 0.5, c.getHeight() * 0.625, c.getWidth() * 0.75, c.getHeight() * 0.625);
				shape.lineTo(c.getWidth(), c.getHeight() * 0.625);
				shape.lineTo(c.getWidth(), c.getHeight());
				shape.lineTo(0, c.getHeight());
				shape.closePath();
				alreadyInitializedTheRollingHillsShape = true;
			}
			Graphics2D g2d2 = (Graphics2D) g2d.create();
			g2d2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
			g2d2.setPaint(new RadialGradientPaint(new Point.Double(c.getWidth(), c.getHeight()), (float) (c.getHeight() * 0.625), new float[] { 0.0f, 1.0f }, new Color[] { new Color(51, 204, 51), new Color(0, 128, 0) }));
			g2d2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d2.fill(Grass.grassFor(grassArea, 25, 15, 5));
			g2d2.fill(shape);
			g2d2.dispose();
		}

		public static Shape getGrassArea(JComponent c) {
			if (!alreadyInitializedTheGrassArea) {
				grassArea.moveTo(0, c.getHeight() * 0.75);
				grassArea.lineTo(c.getWidth() * 0.25, c.getHeight() * 0.75);
				grassArea.curveTo(c.getWidth() * 0.5, c.getHeight() * 0.75, c.getWidth() * 0.5, c.getHeight() * 0.625, c.getWidth() * 0.75, c.getHeight() * 0.625);
				grassArea.lineTo(c.getWidth(), c.getHeight() * 0.625);
				alreadyInitializedTheGrassArea = true;
			}
			return grassArea;
		}

		public static Shape getRollingHillsNow(JComponent c) {
			if (!alreadyInitializedTheRollingHillsShape) {
				shape.moveTo(0, c.getHeight() * 0.75);
				shape.lineTo(c.getWidth() * 0.25, c.getHeight() * 0.75);
				shape.curveTo(c.getWidth() * 0.5, c.getHeight() * 0.75, c.getWidth() * 0.5, c.getHeight() * 0.625, c.getWidth() * 0.75, c.getHeight() * 0.625);
				shape.lineTo(c.getWidth(), c.getHeight() * 0.625);
				shape.lineTo(c.getWidth(), c.getHeight());
				shape.lineTo(0, c.getHeight());
				shape.closePath();
				alreadyInitializedTheRollingHillsShape = true;
			}
			return shape;
		}

		private static boolean alreadyInitializedTheFlowerAreaShape = false;
		private static final GeneralPath flowerArea = new GeneralPath();

		public static Shape getFlowerAreaNow(JComponent c) {
			if (!alreadyInitializedTheFlowerAreaShape) {
				flowerArea.moveTo(0, c.getHeight() * 0.75 - averageFlowerSize().height);
				flowerArea.lineTo(c.getWidth() * 0.25, c.getHeight() * 0.75 - averageFlowerSize().height);
				flowerArea.curveTo(c.getWidth() * 0.5, c.getHeight() * 0.75 - averageFlowerSize().height, c.getWidth() * 0.5, c.getHeight() * 0.625 - averageFlowerSize().height, c.getWidth() * 0.75, c.getHeight() * 0.625 - averageFlowerSize().height);
				flowerArea.lineTo(c.getWidth(), c.getHeight() * 0.625 - averageFlowerSize().height);
				flowerArea.lineTo(c.getWidth(), c.getHeight());
				flowerArea.lineTo(0, c.getHeight());
				flowerArea.closePath();
				alreadyInitializedTheFlowerAreaShape = true;
			}
			return flowerArea;
		}

		public static void paintSky(JComponent c, Graphics2D g2d, Paint skyPaint) {
			Graphics2D g2d2 = (Graphics2D) g2d.create();
			g2d2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
			g2d2.setPaint(skyPaint);
			g2d2.fillRect(0, 0, c.getWidth(), c.getHeight());
			g2d2.dispose();
		}

		public static void paintText(JComponent c, Font f, String text, BufferedImageOp op, Paint outline, float outlineWidth, Paint fill, float alpha, int x, int y, boolean center, Graphics2D g2d) {
			Graphics2D g2d2 = (Graphics2D) g2d.create();
			BufferedImage bi = null;
			if (op != null) {
				bi = new BufferedImage(/* g2d2.getClipBounds().width */c.getWidth(), /* g2d2.getClipBounds().height */c.getHeight(), BufferedImage.TYPE_INT_ARGB);
				g2d2 = bi.createGraphics();
			}
			g2d2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g2d2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d2.setFont(f);
			g2d2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
			if (outline != null && fill != null) {
				g2d2.setPaint(fill);
				g2d2.drawString(text, (int) (center ? c.getWidth() / 2 - f.getStringBounds(text, g2d2.getFontRenderContext()).getWidth() / 2 : x), (int) (f.getStringBounds(text, g2d2.getFontRenderContext()).getHeight() + y));
				g2d2.setPaint(outline);
				GlyphVector vect = f.createGlyphVector(g2d2.getFontRenderContext(), text);
				Shape s = vect.getOutline((int) (center ? c.getWidth() / 2 - f.getStringBounds(text, g2d2.getFontRenderContext()).getWidth() / 2 : x), (int) (f.getStringBounds(text, g2d2.getFontRenderContext()).getHeight() + y));
				g2d2.setStroke(new BasicStroke(outlineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, null, 0.0f));
				g2d2.draw(s);
			} else if (outline == null && fill == null) {
				g2d.dispose();
				return;
			} else if (outline == null && fill != null) {
				g2d2.setPaint(fill);
				g2d2.drawString(text, (int) (center ? c.getWidth() / 2 - f.getStringBounds(text, g2d2.getFontRenderContext()).getWidth() / 2 : x), (int) (f.getStringBounds(text, g2d2.getFontRenderContext()).getHeight() + y));
			} else if (outline != null && fill == null) {
				g2d2.setPaint(outline);
				GlyphVector vect = f.createGlyphVector(g2d2.getFontRenderContext(), text);
				Shape s = vect.getOutline(/* /* */(int) (center ? c.getWidth() / 2 - f.getStringBounds(text, g2d2.getFontRenderContext()).getWidth() / 2 : x), (int) (f.getStringBounds(text, g2d2.getFontRenderContext()).getHeight() + y)/**/);
				g2d2.setStroke(new BasicStroke(outlineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, null, 0.0f));
				g2d2.draw(s);
			}
			if (op != null) {
				Graphics2D g2d3 = (Graphics2D) g2d.create();
				g2d3.drawImage(bi, /* null */op, 0, 0);
			}
		}

		public static void paintFormattedShape(JComponent c, Shape shape, BufferedImageOp op, Paint outline, float outlineWidth, Paint fill, float alpha, int tx, int ty, Graphics2D g2d) {
			Graphics2D g2d2 = (Graphics2D) g2d.create();
			BufferedImage bi = null;
			if (op != null) {
				bi = new BufferedImage(/* g2d2.getClipBounds().width */c.getWidth(), /* g2d2.getClipBounds().height */c.getHeight(), BufferedImage.TYPE_INT_ARGB);
				g2d2 = bi.createGraphics();
			}
			g2d2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g2d2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
			if (outline != null && fill != null) {
				g2d2.setPaint(fill);
				g2d2.fill(AffineTransform.getTranslateInstance(tx, ty).createTransformedShape(shape));
				g2d2.setPaint(outline);
				g2d2.setStroke(new BasicStroke(outlineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, null, 0.0f));
				g2d2.draw(shape);
			} else if (outline == null && fill == null) {
				g2d.dispose();
				return;
			} else if (outline == null && fill != null) {
				g2d2.setPaint(fill);
				g2d2.fill(AffineTransform.getTranslateInstance(tx, ty).createTransformedShape(shape));
			} else if (outline != null && fill == null) {
				g2d2.setPaint(outline);
				g2d2.setStroke(new BasicStroke(outlineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, null, 0.0f));
				g2d2.draw(shape);
			}
			if (op != null) {
				Graphics2D g2d3 = (Graphics2D) g2d.create();
				g2d3.drawImage(bi, /* null */op, 0, 0);
			}
		}
	}

	public static class AlreadyLoadedImageIcon extends ImageIcon {
		private static final long serialVersionUID = 1L;

		public AlreadyLoadedImageIcon(URL location) {
			super(location);
		}

		@Override
		public synchronized void paintIcon(Component c, Graphics g2d, int x, int y) {
			if (getImageObserver() == null) {
				g2d.drawImage(getImage(), x, y, null);
			} else {
				g2d.drawImage(getImage(), x, y, null);// Put null anyway
			}
		}
	}

	/**
	 *
	 * Prefers a good trade-off between quality and speed. Uses the scaling method provided by Java: <br/>
	 * <code>g2d.drawImage(src,0,0,(int)(src.getWidth()*scaleFactor),(int)(src.getHeight()*scaleFactor),null);</code><br/>
	 * <br/>
	 *
	 * Has bilinear interpolation and antialiasing.
	 *
	 * @author Arka Majumdar
	 *
	 */
	public static class ScaledImageIcon extends AlreadyLoadedImageIcon {
		private static final long serialVersionUID = 1L;
		protected double scale = 2;

		public ScaledImageIcon(URL location, double scale) {
			super(location);
			this.scale = scale;
		}

		@Override
		public synchronized void paintIcon(Component c, Graphics g2d, int x, int y) {
			BufferedImage src = new BufferedImage(getImage().getWidth(null), getImage().getWidth(null), BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2d2 = src.createGraphics();
			if (getImageObserver() == null) {
				g2d2.drawImage(getImage(), 0, 0, null);
			} else {
				g2d2.drawImage(getImage(), 0, 0, null);
			}
			((Graphics2D) g2d).drawImage(getScaledImage(src, scale), null, x, y);
		}

		public static BufferedImage getScaledImage(BufferedImage src, double scaleFactor) {
			int w = (int) (src.getWidth() * scaleFactor);
			int h = (int) (src.getHeight() * scaleFactor);
			if (w <= 0) {
				w = 1;
			}
			if (h <= 0) {
				h = 1;
			}
			BufferedImage scaled = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2d = scaled.createGraphics();
			g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_DEFAULT);
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.drawImage(src, 0, 0, w, h, null);
			return scaled;
		}

		@Override
		public int getIconWidth() {
			return (int) (super.getIconWidth() * scale);
		}

		@Override
		public int getIconHeight() {
			return (int) (super.getIconHeight() * scale);
		}

		public synchronized void setScale(double scale) {
			this.scale = scale;
		}

		public double getScale() {
			return scale;
		}
	}

	/**
	 * Quicker than {@link ScaledImgeIcon}. No antialiasing and will pixelate. Uses a different method than provided by Java. Works best with scale factors divisible by two. If not, it will round scaling to an <code>int</code>, so it will scale where it can. Scaling method similar to MS Paint.
	 *
	 * @author Arka Majumdar
	 *
	 */
	public static class PixelResizeImageIcon extends ScaledImageIcon {
		private static final long serialVersionUID = 1L;

		public PixelResizeImageIcon(URL location, double scale) {
			super(location, scale);
		}

		@Override
		public synchronized void paintIcon(Component c, Graphics g2d, int x, int y) {
			BufferedImage src = new BufferedImage(getImage().getWidth(null), getImage().getWidth(null), BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2d2 = src.createGraphics();
			if (getImageObserver() == null) {
				g2d2.drawImage(getImage(), 0, 0, null);
			} else {
				g2d2.drawImage(getImage(), 0, 0, null);
			}
			((Graphics2D) g2d).drawImage(getResizedImage(src, scale), null, x, y);
		}

		public static BufferedImage getResizedImage(BufferedImage src, double scaleFactor) {
			int w = (int) (src.getWidth() * scaleFactor);
			int h = (int) (src.getHeight() * scaleFactor);
			if (w <= 0) {
				w = 1;
			}
			if (h <= 0) {
				h = 1;
			}
			BufferedImage resized = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
			for (int y = 0; y < resized.getHeight(); y++) {
				for (int x = 0; x < resized.getWidth(); x++) {
					resized.setRGB(x, y, src.getRGB((int) (x / scaleFactor), (int) (y / scaleFactor)));
				}
			}
			return resized;
		}

		@Override
		public int getIconWidth() {
			return (int) (super.getIconWidth() * scale);
		}

		@Override
		public int getIconHeight() {
			return (int) (super.getIconHeight() * scale);
		}
	}

	private final class TimingPainter implements ActionListener {
		private final Timer animTime = new Timer(1, this);
		private int count = 1;
		private BufferedImage dearTmp;
		// private BufferedImage giantMsg;
		private BufferedImage[] giantMsgLetters;
		private ShadowFilter dropShadow;
		private int squirrelSegmentNow;
		private Point[] squirrelMovements;
		private boolean flowerEnd = false;
		private boolean reversedFlowers = false;
		public float[] flowerAlphas = new float[50];// Image + TYPE_INT_ARGB + Fading + Up direction + Timers + Multiple Alphas = trouble!
		public int nowFadeFlower = 0;// Image + TYPE_INT_ARGB + Fading + Up direction + Timers + Multiple Alphas = trouble!
		public short birdFrame, whiteFrame;
		public ComparablePoint[] birdPoints = new ComparablePoint[6];
		public ComparablePoint[] whitePoints = new ComparablePoint[6];
		public short birdNum = 0, whiteBirdNum = 0;

		public TimingPainter() {
			start();
		}

		public void start() {
			dropShadow = new ShadowFilter();
			dropShadow.setAngle(-45.0f);
			dropShadow.setDistance(5.0f);
			dropShadow.setRadius(5.0f);
			dropShadow.setOpacity(0.75f);
			p.repaint();
			animTime.start();
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			if (count == 1) {
				p.repaint();
				img = new BufferedImage(p.getWidth(), p.getHeight(), BufferedImage.TYPE_INT_ARGB);
				Graphics2D g2d = img.createGraphics();
				URL waitURL = getClass().getResource("wait.png");
				if (waitURL == null) {
					System.err.println("wait.png not found");
				}
				ImageIcon wait = new ImageIcon(waitURL);
				wait.paintIcon(p, g2d, p.getWidth() / 2 - wait.getIconWidth() / 2, p.getHeight() / 2 - wait.getIconHeight() / 2);
				p.repaint();
				// giantMsg = new BufferedImage(p.getWidth(), p.getHeight(), BufferedImage.TYPE_INT_ARGB);
				giantMsgLetters = new BufferedImage[giant.length() + 2];
				TextStroke ts = new TextStroke(" " + giant + " ", new Font(Font.SANS_SERIF, Font.PLAIN, 100));
				// LandscapePainter.paintText(p, new Font(Font.SANS_SERIF, Font.PLAIN, 70), "HAPPY BIRTHDAY!", dropShadow, Color.yellow, 3f, null, 1.0f, 0, 20, true, giantMsg.createGraphics());
				// LandscapePainter.paintFormattedShape(p, ts.createStrokedShape(LandscapePainter.getGrassArea(p)), dropShadow, Color.yellow, 3f, null, 1.0f, 0, 0, giantMsg.createGraphics());
				ts.createStrokedShape(LandscapePainter.getGrassArea(p));
				for (int i = 0; i < giant.length() + 2; i++) {
					giantMsgLetters[i] = new BufferedImage(p.getWidth(), p.getHeight(), BufferedImage.TYPE_INT_ARGB);
					LandscapePainter.paintFormattedShape(p, ts.getLettersArray()[i], dropShadow, Color.yellow, 3f, null, 1.0f, 0, 0, giantMsgLetters[i].createGraphics());
				}
				dearTmp = new BufferedImage(p.getWidth(), p.getHeight(), BufferedImage.TYPE_INT_ARGB);
				try {
					LandscapePainter.paintText(p, getDearFont(), dear, null, /* Color.black */null, /* 1f */0f, new Color(214, 0, 147), 1.0f, 10, 10, false, dearTmp.createGraphics());
				} catch (FontFormatException e) {
				} catch (IOException e) {
				}
				int maxBirdHeight = LandscapePainter.maxBirdSize().height;
				birdPoints[0] = new ComparablePoint(ComparePointBy.X);
				birdPoints[1] = new ComparablePoint(ComparePointBy.X);
				birdPoints[2] = new ComparablePoint(ComparePointBy.X);
				birdPoints[3] = new ComparablePoint(ComparePointBy.X);
				birdPoints[4] = new ComparablePoint(ComparePointBy.X);
				birdPoints[5] = new ComparablePoint(ComparePointBy.X);
				birdPoints[0].y = (int) (rnd.nextDouble() * (int) (p.getHeight() * 0.375) - maxBirdHeight);
				birdPoints[1].y = (int) (rnd.nextDouble() * (int) (p.getHeight() * 0.375) - maxBirdHeight);
				birdPoints[2].y = (int) (rnd.nextDouble() * (int) (p.getHeight() * 0.375) - maxBirdHeight);
				birdPoints[3].y = (int) (rnd.nextDouble() * (int) (p.getHeight() * 0.375) - maxBirdHeight);
				birdPoints[4].y = (int) (rnd.nextDouble() * (int) (p.getHeight() * 0.375) - maxBirdHeight);
				birdPoints[5].y = (int) (rnd.nextDouble() * (int) (p.getHeight() * 0.375) - maxBirdHeight);
				int maxBirdWidth = LandscapePainter.maxBirdSize().width;
				birdPoints[0].x = (int) -(rnd.nextDouble() * (maxBirdWidth * 5));
				birdPoints[1].x = (int) -(rnd.nextDouble() * (maxBirdWidth * 5));
				birdPoints[2].x = (int) -(rnd.nextDouble() * (maxBirdWidth * 5));
				birdPoints[3].x = (int) -(rnd.nextDouble() * (maxBirdWidth * 5));
				birdPoints[4].x = (int) -(rnd.nextDouble() * (maxBirdWidth * 5));
				birdPoints[5].x = (int) -(rnd.nextDouble() * (maxBirdWidth * 5));
				birdNum = (short) (rnd.nextInt(6) + 1);
				int maxWhiteBirdHeight = LandscapePainter.maxWhiteBirdSize().height;
				whitePoints[0] = new ComparablePoint(ComparePointBy.X);
				whitePoints[1] = new ComparablePoint(ComparePointBy.X);
				whitePoints[2] = new ComparablePoint(ComparePointBy.X);
				whitePoints[3] = new ComparablePoint(ComparePointBy.X);
				whitePoints[4] = new ComparablePoint(ComparePointBy.X);
				whitePoints[5] = new ComparablePoint(ComparePointBy.X);
				whitePoints[0].y = (int) (rnd.nextDouble() * (int) (p.getHeight() * 0.375) - maxWhiteBirdHeight);
				whitePoints[1].y = (int) (rnd.nextDouble() * (int) (p.getHeight() * 0.375) - maxWhiteBirdHeight);
				whitePoints[2].y = (int) (rnd.nextDouble() * (int) (p.getHeight() * 0.375) - maxWhiteBirdHeight);
				whitePoints[3].y = (int) (rnd.nextDouble() * (int) (p.getHeight() * 0.375) - maxWhiteBirdHeight);
				whitePoints[4].y = (int) (rnd.nextDouble() * (int) (p.getHeight() * 0.375) - maxWhiteBirdHeight);
				whitePoints[5].y = (int) (rnd.nextDouble() * (int) (p.getHeight() * 0.375) - maxWhiteBirdHeight);
				int maxWhiteBirdWidth = LandscapePainter.maxWhiteBirdSize().width;
				whitePoints[0].x = (int) -(rnd.nextDouble() * (maxWhiteBirdWidth * 5));
				whitePoints[1].x = (int) -(rnd.nextDouble() * (maxWhiteBirdWidth * 5));
				whitePoints[2].x = (int) -(rnd.nextDouble() * (maxWhiteBirdWidth * 5));
				whitePoints[3].x = (int) -(rnd.nextDouble() * (maxWhiteBirdWidth * 5));
				whitePoints[4].x = (int) -(rnd.nextDouble() * (maxWhiteBirdWidth * 5));
				whitePoints[5].x = (int) -(rnd.nextDouble() * (maxWhiteBirdWidth * 5));
				whiteBirdNum = (short) (rnd.nextInt(6) + 1);
				squirrelMovements = getsquirrelMovements(p);
				squirrelSegmentNow = squirrelMovements.length - 1;
				doneLoading = true;
			}
			birdFrame++;
			if (birdFrame >= 9) {
				birdFrame = 1;
			}
			whiteFrame++;
			if (whiteFrame >= 10) {
				whiteFrame = 1;
			}
			Graphics2D g2d = img.createGraphics();
			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
			if (count < 250) {
				if (count < 100) {
					LandscapePainter.paintSky(p, g2d, new Color(0f, 0f, 0f));
					paintStars(p, g2d, 1.0f, 100);
					LandscapePainter.paintMoon(p, g2d, (int) (p.getWidth() * 0.75), (int) (p.getHeight() * 0.15), 1.0f);
				} else {
					LandscapePainter.paintSky(p, g2d, new RadialGradientPaint(new Point.Double(p.getWidth() / 2, p.getHeight()), (float) (p.getHeight() * 0.625), new float[] { 0.0f, 1.0f }, new Color[] { new Color(198, 217, 241), new Color(85, 142, 213) }
					/*
					 * Dawn colors mixColors(Color.red, Color.orange), Color.pink
					 */
					));
					LandscapePainter.paintSky(p, g2d, new Color(0f, 0f, 0f, 1.0f - (float) (count - 100) / 150));
					paintStars(p, g2d, 1.0f - (float) (count - 100) / 150, 100);
					LandscapePainter.paintMoon(p, g2d, (int) (p.getWidth() * 0.75), (int) (p.getHeight() * 0.15), 1.0f - (float) (count - 100) / 150);
				}
			} else {
				LandscapePainter.paintSky(p, g2d, new RadialGradientPaint(new Point.Double(p.getWidth() / 2, p.getHeight()), (float) (p.getHeight() * 0.625), new float[] { 0.0f, 1.0f }, new Color[] { new Color(198, 217, 241), new Color(85, 142, 213) }));
			}
			// if (!randomCloudsOnce) {
			randomCloudsOnce(p, g2d);
			// }else{
			BufferedImage bi = new BufferedImage(LandscapePainter.cloud.getIconWidth(), LandscapePainter.cloud.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
			for (int i = 0; i < cloudPoints.length; i++) {
				if (cloudPoints[i].x + 1 > p.getWidth()) {
					if (count % 4 == 0) {
						cloudPoints[i] = new Point(0, cloudPoints[i].y);
					}
					LandscapePainter.paintCloudOnto(p, 0, 0, bi.createGraphics(), count < 300 && count > 250 ? (float) (count - 250) / 50 : count >= 300 ? 1.0f : 0.0f);
					g2d.drawImage(bi, null, cloudPoints[i].x, cloudPoints[i].y);
					p.repaint();
				} else {
					if (count % 4 == 0) {
						cloudPoints[i] = new Point(cloudPoints[i].x + 1, cloudPoints[i].y);
					}
					LandscapePainter.paintCloudOnto(p, 0, 0, bi.createGraphics(), count < 300 && count > 250 ? (float) (count - 250) / 50 : count >= 300 ? 1.0f : 0.0f);
					g2d.drawImage(bi, null, cloudPoints[i].x, cloudPoints[i].y);
					p.repaint();
				}
			}
			// }
			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
			try {
				if (count <= 100 && count > 1 && (int) (getDearFont().getStringBounds(dear, g2d.getFontRenderContext()).getWidth() * ((float) count / 100)) != 0) {
					g2d.drawImage(dearTmp.getSubimage(0, 0, (int) (getDearFont().getStringBounds(dear, g2d.getFontRenderContext()).getWidth() * ((float) count / 100)) + 10, dearTmp.getHeight()), null, 0, 0);
				} else if (count > 100) {
					g2d.drawImage(dearTmp, null, 0, 0);
				}
			} catch (Exception e) {
			}
			for (int i = 0; i < giantMsgLetters.length; i++) {
				if (count < 250 + (i + 1) * 12 && count > 250) {
					g2d.drawImage(giantMsgLetters[i], null, 0, (int) (100f - (count - (250f + i * 12f)) / 12f * 115));
				} else if (count >= 250 + (i + 1) * 12) {
					g2d.drawImage(giantMsgLetters[i], null, 0, -15);
				}
			}
			LandscapePainter.paintRollingHills(p, g2d);
			randomFlowersOnce(p, g2d);
			if (!reversedFlowers) {
				@SuppressWarnings("rawtypes")
				AbstractList list = (AbstractList) Arrays.asList(flowerPoints);
				Collections.reverse(list);
				flowerPoints = (ComparablePoint[]) list.toArray();
				reversedFlowers = true;
			}
			Shape oldClip = g2d.getClip();
			g2d.setClip(LandscapePainter.getFlowerAreaNow(p));
			if (count > 250 + giantMsgLetters.length * 12) {
				int i;
				for (i = 0; i < flowerPoints.length; i++) {
					if (flowerAlphas[i] >= /* Safety */1.0f) {
						LandscapePainter.paintFlowerOnto(p, flowerPoints[i].x, flowerPoints[i].y, flowerTypes[i], g2d, 1.0f);
						continue;
					}
					if (i != nowFadeFlower)
					 {
						continue;// Don't you just love asynchronous stuff (literally)
					}
					if (flowerAlphas[nowFadeFlower] >= /* Safety */1.0f) {
						LandscapePainter.paintFlowerOnto(p, flowerPoints[nowFadeFlower].x, flowerPoints[nowFadeFlower].y, flowerTypes[nowFadeFlower], g2d, 1.0f);
						nowFadeFlower++;// Don't you just love asynchronous stuff (literally)
						continue;
					}
					if (count - (250 + (float) giantMsgLetters.length * 12) - 25 * (float) nowFadeFlower <= 25) {
						flowerAlphas[nowFadeFlower] += 0.1;
					} else {
						nowFadeFlower++;// Don't you just love asynchronous stuff (literally)
						continue;
					}
					if (flowerAlphas[nowFadeFlower] >= /* Safety */1.0f || flowerAlphas[nowFadeFlower] < 0.0f) {
						LandscapePainter.paintFlowerOnto(p, flowerPoints[nowFadeFlower].x, flowerPoints[nowFadeFlower].y, flowerTypes[nowFadeFlower], g2d, 1.0f);
						nowFadeFlower++;// Don't you just love asynchronous stuff (literally)
						continue;
					}
					if (!(flowerAlphas[nowFadeFlower] > 1.0f)) {
						LandscapePainter.paintFlowerOnto(p, flowerPoints[nowFadeFlower].x, flowerPoints[nowFadeFlower].y, flowerTypes[nowFadeFlower], g2d, flowerAlphas[nowFadeFlower]);
					}
					// System.out.println(nowFadeFlower);
					if (nowFadeFlower >= flowerPoints.length - 1) {
						flowerEnd = true;
					}
				}
			}
			g2d.setClip(oldClip);
			if (count > 300) {
				for (int i = 0; i < birdNum; i++) {
					if (isLastBirdPoint(birdPoints[i]) && birdPoints[i].x + 1 > p.getWidth()) {
						birdPoints[i] = new ComparablePoint(birdPoints[i].x + 1, birdPoints[i].y, ComparePointBy.X);
						LandscapePainter.getBirdFor(birdFrame).paintIcon(p, g2d, birdPoints[i].x, birdPoints[i].y);
						p.repaint();
						birdNum = (short) (rnd.nextInt(6) + 1);
						int maxBirdHeight = LandscapePainter.maxBirdSize().height;
						int maxBirdWidth = LandscapePainter.maxBirdSize().width;
						for (int i1 = 0; i1 < birdNum; i1++) {
							birdPoints[i1].y = (int) (rnd.nextDouble() * (int) (p.getHeight() * 0.375) - maxBirdHeight);
							birdPoints[i1].x = (int) -(rnd.nextDouble() * (maxBirdWidth * 5));
						}
						LandscapePainter.getBirdFor(birdFrame).paintIcon(p, g2d, birdPoints[i].x, birdPoints[i].y);
						p.repaint();
					} else {
						birdPoints[i] = new ComparablePoint(birdPoints[i].x + 1, birdPoints[i].y, ComparePointBy.X);
						LandscapePainter.getBirdFor(birdFrame).paintIcon(p, g2d, birdPoints[i].x, birdPoints[i].y);
						p.repaint();
					}
				}
				if (count > 400) {
					if (count > 550) {
						LandscapePainter.whiteBird1.setScale(0.875);
						LandscapePainter.whiteBird2.setScale(0.875);
						LandscapePainter.whiteBird3.setScale(0.875);
						LandscapePainter.whiteBird4.setScale(0.875);
						LandscapePainter.whiteBird5.setScale(0.875);
						LandscapePainter.whiteBird6.setScale(0.875);
						LandscapePainter.whiteBird7.setScale(0.875);
						LandscapePainter.whiteBird8.setScale(0.875);
						LandscapePainter.whiteBird9.setScale(0.875);
					} else {
						double scale = ((double) count - (double) 400) / 150;
						scale *= 0.875;
						LandscapePainter.whiteBird1.setScale(scale);
						LandscapePainter.whiteBird2.setScale(scale);
						LandscapePainter.whiteBird3.setScale(scale);
						LandscapePainter.whiteBird4.setScale(scale);
						LandscapePainter.whiteBird5.setScale(scale);
						LandscapePainter.whiteBird6.setScale(scale);
						LandscapePainter.whiteBird7.setScale(scale);
						LandscapePainter.whiteBird8.setScale(scale);
						LandscapePainter.whiteBird9.setScale(scale);
						// If you want the white birds to move down as they come in uncomment this:
						// for (int i3 = 0 ; i3 < whiteBirdNum ; i3++)
						// whitePoints[i3].y++;
					}
					for (int i2 = 0; i2 < 2; i2++) {
						for (int i = 0; i < whiteBirdNum; i++) {
							if (isLastWhiteBirdPoint(whitePoints[i]) && whitePoints[i].x + 1 > p.getWidth()) {
								whitePoints[i] = new ComparablePoint(whitePoints[i].x + 1, whitePoints[i].y, ComparePointBy.X);
								LandscapePainter.getWhiteBirdFor(whiteFrame).paintIcon(p, g2d, whitePoints[i].x, whitePoints[i].y);
								p.repaint();
								whiteBirdNum = (short) (rnd.nextInt(6) + 1);
								int maxWhiteBirdHeight = LandscapePainter.maxWhiteBirdSize().height;
								int maxWhiteBirdWidth = LandscapePainter.maxWhiteBirdSize().width;
								for (int i1 = 0; i1 < whiteBirdNum; i1++) {
									whitePoints[i1].y = (int) (rnd.nextDouble() * (int) (p.getHeight() * 0.375) - maxWhiteBirdHeight);
									whitePoints[i1].x = (int) -(rnd.nextDouble() * (maxWhiteBirdWidth * 5));
								}
								LandscapePainter.getWhiteBirdFor(whiteFrame).paintIcon(p, g2d, whitePoints[i].x, whitePoints[i].y);
								p.repaint();
							} else {
								whitePoints[i] = new ComparablePoint(whitePoints[i].x + 1, whitePoints[i].y, ComparePointBy.X);
								LandscapePainter.getWhiteBirdFor(whiteFrame).paintIcon(p, g2d, whitePoints[i].x, whitePoints[i].y);
								p.repaint();
							}
						}
					}
				}
			}
			if (flowerEnd) {
				if (count % 2 == 0) {
					if (squirrelSegmentNow > squirrelMovements.length - 1 || squirrelSegmentNow < 0) {
						if (squirrelSegmentNow < 0) {
							squirrelMovements = getsquirrelMovements(p);
							squirrelSegmentNow = squirrelMovements.length - 1;
							LandscapePainter.squirrel.paintIcon(p, g2d, squirrelMovements[squirrelSegmentNow].x, squirrelMovements[squirrelSegmentNow].y);
							squirrelSegmentNow--;
						}
					} else {
						LandscapePainter.squirrel.paintIcon(p, g2d, squirrelMovements[squirrelSegmentNow].x, squirrelMovements[squirrelSegmentNow].y);
						squirrelSegmentNow--;
					}
				} else {
					if (squirrelSegmentNow > squirrelMovements.length - 1 || squirrelSegmentNow < 0) {
						if (squirrelSegmentNow < 0) {
							squirrelMovements = getsquirrelMovements(p);
							squirrelSegmentNow = squirrelMovements.length - 1;
							LandscapePainter.squirrel.paintIcon(p, g2d, squirrelMovements[squirrelSegmentNow].x, squirrelMovements[squirrelSegmentNow].y);
						}
					} else {
						LandscapePainter.squirrel.paintIcon(p, g2d, squirrelMovements[squirrelSegmentNow].x, squirrelMovements[squirrelSegmentNow].y);
					}
				}
			}
			p.repaint();
			count++;
		}

		// private boolean blackBirdsSorted;
		@SuppressWarnings("unused")
		@Deprecated
		public Point getLastBirdPoint() {
			Arrays.sort(birdPoints);
			return birdPoints[0];
		}

		// private boolean whiteBirdsSorted;
		@SuppressWarnings("unused")
		@Deprecated
		public Point getLastWhiteBirdPoint() {
			Arrays.sort(whitePoints);
			return whitePoints[0];
		}

		public boolean isLastWhiteBirdPoint(ComparablePoint point) {
			if (whiteBirdNum == 1) {
				return true;
			}
			ComparablePoint result = whitePoints[0];
			for (short i = 1; i < whiteBirdNum; i++) {
				switch (result.compareTo(whitePoints[i])) {// no Math.signum needed - see {@link ComparablePoint}
				case -1:
					break;
				case 0:
					break;
				case 1:
					result = whitePoints[i];
					break;
				}
			}
			return result.equals(point);
		}

		public boolean isLastBirdPoint(ComparablePoint point) {
			if (birdNum == 1) {
				return true;
			}
			ComparablePoint result = birdPoints[0];
			for (short i = 1; i < birdNum; i++) {
				switch (result.compareTo(birdPoints[i])) {// no Math.signum needed - see {@link ComparablePoint}
				case -1:
					break;
				case 0:
					break;
				case 1:
					result = birdPoints[i];
					break;
				}
			}
			return result.equals(point);
		}
	}

	public static Font getDearFont() throws FontFormatException, IOException {
		return Font.createFont(Font.PLAIN, LandscapePainter.class.getResourceAsStream("precious.ttf")).deriveFont(60f);
	}

	public static Color mixColors(Color one, Color two) {
		float[] rgbOne = one.getRGBColorComponents(new float[3]);
		float[] rgbTwo = two.getRGBColorComponents(new float[3]);
		return new Color(rgbOne[0] * rgbTwo[0], rgbOne[1] * rgbTwo[1], rgbOne[2] * rgbTwo[2]);
	}
}
