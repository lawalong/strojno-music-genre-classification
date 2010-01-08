/**
 * 
 */
package hr.fer.su.mgc.test;

import hr.fer.su.mgc.Config;
import hr.fer.su.mgc.features.FeatureExtractor;

import java.io.File;

/**
 * @author Tomek
 */
public class FeatureExtractorTester {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		Config.init();
		
		FeatureExtractor featureExtractor = new FeatureExtractor(
				new String[] { "blues", "classical", "country",
						"disco", "hiphop", "jazz", "metal", "pop",
						"reggae", "rock" });
		File songFile = new File("C:/Users/Tomek/Documents/workspace/strojno-music-classification/dataset/blues/blues.00036.au");
		featureExtractor.extractSongFeatures(new File[] {songFile});
	}

}
