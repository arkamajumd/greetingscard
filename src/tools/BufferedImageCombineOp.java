package tools;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import com.jhlabs.image.AbstractBufferedImageOp;

public class BufferedImageCombineOp extends AbstractBufferedImageOp {
	private BufferedImage combineWith;
	private AlphaComposite rule;
	private int x, y;

	public BufferedImageCombineOp(BufferedImage combineWith, AlphaComposite rule, int x, int y) {
		this.combineWith = combineWith;
		this.rule = rule;
		this.x = x;
		this.y = y;
	}

	@Override
	public BufferedImage filter(BufferedImage src, BufferedImage dest) {
		Graphics2D g2d = dest.createGraphics();
		g2d.drawImage(src, null, 0, 0);
		g2d.setComposite(rule);
		g2d.drawImage(combineWith, null, x, y);
		return dest;
	}
}
