package mud.utils;

import java.util.*;
import java.awt.image.BufferedImage;

public class AsciiImages
{
	/**
	 * Convert a loaded ascii image into lines of text with ansi
	 * color coding included to color the ascii
	 * 
	 * @param image
	 * @return
	 */
	public static String[] ImageToAscii(BufferedImage image) {
		int[] pixelData = null;
		int[] rgb = new int[1000];
		
		int height = image.getHeight();
		int width = image.getWidth();
		
		ArrayList<String> strings = new ArrayList<String>(width);
		
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				pixelData = image.getRGB(x, y, 1, 1, rgb, 0, 1);
				System.out.println(pixelData);
			}
		}
		
		return (String[]) strings.toArray();
	}

}
