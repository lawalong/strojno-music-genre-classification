/**
 * 
 */
package hr.fer.su.mgc.test;

import hr.fer.su.mgc.Config;
import hr.fer.su.mgc.classifier.ClassifierAdapter;
import hr.fer.su.mgc.classifier.ClassifierConstants;
import hr.fer.su.mgc.classifier.IClassifier;
import hr.fer.su.mgc.features.FeatureExtractor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

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
		
		String[] genres = Config.grabGenres();
		
		FeatureExtractor featureExtractor = new FeatureExtractor(genres);
		
		File songFile = new File("dataset/blues/blues.00037.au");
		
		File song = featureExtractor.extractSongFeatures(new File[] {songFile});
		
		System.out.println("Feature Extraction completed...");
		
		//podesiti putanju do uzoraka
		File samples = new File("data/allFeatures.arff");

		ClassifierAdapter smo = new ClassifierAdapter(ClassifierConstants.SMO);
		smo.setTrainData(samples);
		smo.setTestData(samples);
		smo.setValidation(IClassifier.NO_VALIDATION);
		smo.buildModel();

		File hypoFile = new File("hypothesis/default.hyp");
		ObjectOutputStream objStreamOut;
		ObjectInputStream objStreamIn;
		ClassifierAdapter loaded;
		try {
			objStreamOut = new ObjectOutputStream(
					new FileOutputStream(hypoFile));
			
			System.out.print("Dumping weka to file ... ");

			objStreamOut.writeObject(smo);
			objStreamOut.flush();
			objStreamOut.close();
			
			System.out.println("done");
			
			System.out.print("Loading weka from file ... ");

			objStreamIn = new ObjectInputStream(new FileInputStream(hypoFile));
			loaded = (ClassifierAdapter) objStreamIn.readObject();
			
			System.out.println("done");
			
			long time = System.currentTimeMillis();

			double[] res = loaded.classifyInstances(song).get(0);
			System.out.println("\nPerforming instance test:");

			double max = 0;
			int ind = -1;
			for (int i = 0; i < res.length; ++i) {
				System.out.printf("%.3f", (float) res[i]);
				System.out.print(" ");
				if (max < res[i]) {
					max = res[i];
					ind = i;
				}
			}
			System.out.println(" -> " + genres[ind] + " in " + 
					(System.currentTimeMillis() - time)/1000f + " seconds.");
			
		} catch (IOException e) {
			System.err.println("Things not going as planned.");
			e.printStackTrace();
		}

	}

}
