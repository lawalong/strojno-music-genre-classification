package hr.fer.su.mgc.swing.image;

import hr.fer.su.mgc.MGC;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 * Convenience class for loading icons.
 */
public class ImageUtils {

	/**
	 * Map that contains all used icons.
	 */
	private static Map<String, ImageIcon> allIcons;

	/**
	 * Map that contains all used image components.
	 */
	private static Map<String, ImageComponent> allImageComponents;
	

	public static void loadIconsAndComponents() {

		allIcons = new HashMap<String, ImageIcon>();

		allImageComponents = new HashMap<String, ImageComponent>();

//		ImageIcon wumpusLogo = createImageIcon("res/pics/wumpusLogo.png", "...");
//		allIcons.put("wumpusLogo", wumpusLogo);
//		ImageIcon bulbOn = createImageIcon("res/pics/bulb_on.jpg", "...");
//		allIcons.put("bulbOn", bulbOn);

//		ImageComponent visited = createImageComponent("res/pics/open_small.png");
//		allImageComponents.put("visited", visited);
		
	}

	/**
	 * Returns an ImageIcon, or null if the path was invalid.
	 */
	public static ImageIcon createImageIcon(String path, String description) {
		java.net.URL imgURL = MGC.class.getResource(path);
		if (imgURL != null) {
			return new ImageIcon(imgURL, description);
		} else {
			System.err.println("Couldn't find file: " + path);
			return null;
		}
	}

	/**
	 * Returns an Image, or null if the path was invalid.
	 */
	public static ImageComponent createImageComponent(String path) {
		java.net.URL imgURL = MGC.class.getResource(path);

		if (imgURL == null) {
			System.err.println("Couldn't find file: " + path);
			return null;
		}

		try {
			BufferedImage image = ImageIO.read(imgURL);
			return new ImageComponent(image);
		} catch (IOException ex) {
			System.err.println("Couldn't find file: " + path);
			return null;
		}
	}
	
	public static ImageIcon getIcon(String iconName) {
		return allIcons.get(iconName);
	}
	
	public static ImageComponent getImageComponent(String imageName) {
		return allImageComponents.get(imageName);
	}

}