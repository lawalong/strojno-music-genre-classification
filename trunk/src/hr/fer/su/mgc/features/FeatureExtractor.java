/**
 * 
 */
package hr.fer.su.mgc.features;

import hr.fer.su.mgc.Config;
import hr.fer.su.mgc.matlab.MatlabException;
import hr.fer.su.mgc.matlab.SmartMatlabEngine;

import java.io.File;

/**
 * @author Tomek
 */
public class FeatureExtractor {
	private String relation;
	private String genres;

	public FeatureExtractor(String[] genres, String relation) {
		this.relation = relation;
		this.genres = serialize(genres);
	}

	public FeatureExtractor(String[] genres) {
		this(genres, "default");
	}

	/**
	 * Izvlaci znacajke iz dataseta u file prikladan za ucenje wekinog
	 * klasifikatora.
	 * 
	 * @param datasetPath
	 *              relativna ili apsolutna putanja do dataseta
	 * @param genres
	 *              lista zanrova koje zelimo obuhvatiti u izvlacenju
	 * @return
	 * @throws MatlabException
	 * @throws Exception
	 */
	public File extractDatasetFeatures(File datasetPath)
			throws MatlabException, Exception {
		Config.init();
		SmartMatlabEngine engine = SmartMatlabEngine.getInstance("matlab");
		engine.open();
		File features = engine.runScript("extractFeatures", new String[] {
				"'" + datasetPath.getAbsolutePath() + "'",
				"'" + relation + "'", genres });
		engine.close();
		return features;
	}

	/**
	 * Izvlaci znacajke iz liste pjesama
	 * 
	 * @param songList
	 *              lista pjesama
	 * @return
	 * @throws Exception
	 */
	public File extractSongFeatures(File[] songList) throws Exception {
		Config.init();
		SmartMatlabEngine engine = SmartMatlabEngine.getInstance("matlab");
		engine.open();
		File features = engine
				.runScript("extractSongsFeatures", new String[] {
						serialize(songList), genres });
		engine.close();
		return features;
	}

	private String serialize(String[] array) {
		String result = "{";
		boolean first = true;
		for (String element : array) {
			if (first)
				first = false;
			else
				result += ", ";
			result += "'" + element + "'";
		}
		result += "}";
		return result;
	}

	private String serialize(File[] array) {
		String result = "{";
		boolean first = true;
		for (File element : array) {
			if (first)
				first = false;
			else
				result += ", ";
			result += "'" + element.getAbsolutePath() + "'";
		}
		result += "}";
		return result;
	}
}
