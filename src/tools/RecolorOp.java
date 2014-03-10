package tools;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.image.BufferedImage;

import com.jhlabs.image.AbstractBufferedImageOp;

public class RecolorOp extends AbstractBufferedImageOp {
	Color color;
	float maskOpacity;
	BufferedImage img;
	boolean replaceHue;

	public RecolorOp(Color color, float maskOpacity, BufferedImage img, boolean replaceHue) {
		this.color = color;
		this.maskOpacity = maskOpacity;
		this.img = img;
		this.replaceHue = replaceHue;
	}

	@Override
	public BufferedImage filter(BufferedImage src, BufferedImage dest) {
		if (!replaceHue) {
			return new BufferedImageCombineOp(getMask(), AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f), 0, 0).filter(src, dest);
		} else {
			return replaceHue();
		}
	}

	public BufferedImage getMask() {
		BufferedImage mask = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
		int alpha;
		int argb;
		for (int y = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++) {
				argb = img.getRGB(x, y);
				alpha = (argb >> 24) & 0xFF;
				alpha *= maskOpacity;
				mask.setRGB(x, y, ((alpha & 0xFF) << 24) | ((color.getRed() & 0xFF) << 16) | ((color.getGreen() & 0xFF) << 8) | ((color.getBlue() & 0xFF) << 0));
			}
		}
		return mask;
	}

	public BufferedImage replaceHue() {
		BufferedImage newImage = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
		float[] hsb, newHSB;
		int argb, newARGB;
		for (int x = 0; x < img.getHeight(); x++) {
			for (int y = 0; y < img.getWidth(); y++) {
				argb = img.getRGB(x, y);
				newARGB = color.getRGB();
				hsb = Color.RGBtoHSB((argb >> 16) & 0xFF, (argb >> 8) & 0xFF, (argb >> 0) & 0xFF, null);
				newHSB = Color.RGBtoHSB((newARGB >> 16) & 0xFF, (newARGB >> 8) & 0xFF, (newARGB >> 0) & 0xFF, null);
				newHSB[1] = hsb[1];
				newHSB[2] = hsb[2];
				newARGB = HSBtoRGBwithAlpha(newHSB[0], newHSB[1], newHSB[2], ((argb >> 24) & 0xFF));
				newImage.setRGB(x, y, newARGB);
			}
		}
		return newImage;
	}

	public int HSBtoRGBwithAlpha(float hue, float saturation, float brightness, int alpha) {
		int r = 0, g = 0, b = 0;
		if (saturation == 0) {
			r = g = b = (int) (brightness * 255.0f + 0.5f);
		} else {
			float h = (hue - (float) Math.floor(hue)) * 6.0f;
			float f = h - (float) java.lang.Math.floor(h);
			float p = brightness * (1.0f - saturation);
			float q = brightness * (1.0f - saturation * f);
			float t = brightness * (1.0f - (saturation * (1.0f - f)));
			switch ((int) h) {
			case 0:
				r = (int) (brightness * 255.0f + 0.5f);
				g = (int) (t * 255.0f + 0.5f);
				b = (int) (p * 255.0f + 0.5f);
				break;
			case 1:
				r = (int) (q * 255.0f + 0.5f);
				g = (int) (brightness * 255.0f + 0.5f);
				b = (int) (p * 255.0f + 0.5f);
				break;
			case 2:
				r = (int) (p * 255.0f + 0.5f);
				g = (int) (brightness * 255.0f + 0.5f);
				b = (int) (t * 255.0f + 0.5f);
				break;
			case 3:
				r = (int) (p * 255.0f + 0.5f);
				g = (int) (q * 255.0f + 0.5f);
				b = (int) (brightness * 255.0f + 0.5f);
				break;
			case 4:
				r = (int) (t * 255.0f + 0.5f);
				g = (int) (p * 255.0f + 0.5f);
				b = (int) (brightness * 255.0f + 0.5f);
				break;
			case 5:
				r = (int) (brightness * 255.0f + 0.5f);
				g = (int) (p * 255.0f + 0.5f);
				b = (int) (q * 255.0f + 0.5f);
				break;
			}
		}
		return ((alpha & 0xFF) << 24) | (r << 16) | (g << 8) | (b << 0);
	}
}
