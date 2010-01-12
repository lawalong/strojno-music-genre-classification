package hr.fer.su.mgc.test;

import hr.fer.su.mgc.classifier.ClassifierAdapter;
import hr.fer.su.mgc.classifier.ClassifierConstants;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import weka.classifiers.Evaluation;

public class HammingWindowOptimise {

	private static String originalFilePattern = "C:/Users/Tomek/Desktop/extractedFeatures/test_ham%.2f.arff";
	private static String newFilePattern = "C:/Users/Tomek/Desktop/extractedFeatures/test_ham%.2f_%.2f_%.2f_%.2f_%.2f.arff";

	public static void main(String[] args) throws Exception {

		float hamWid[] = { 1.0f, 0.1f, 0.1f, 0.8f, 1.0f };

		ClassifierAdapter smo = new ClassifierAdapter(
				ClassifierConstants.SMO);

		for (int i = 0; i < 5; i++) {
			double best = 0.d;
			float bestHamWid = 1.0f;
			for (float j = 0.1f; j < 2.01f; j += 0.1f) {
				hamWid[i] = j;
				File combFile = generateCombFile(hamWid);

				System.out.println(combFile.getName());
				smo.setTrainData(combFile);
				Evaluation eval = smo.buildModel(10);
				System.out.println(eval.toSummaryString());
				double correct = eval.correct();
				if (correct > best) {
					best = correct;
					bestHamWid = j;
				}
			}
			System.out.println("Najbolji rezultat " + best
					+ " je postignut sa sirinom hammingovog prozora "
					+ bestHamWid + " za " + i + "-ti parametar.");
			System.out.println();
			hamWid[i] = bestHamWid;
		}
	}

	private static File generateCombFile(float[] hamWid) {
		File res = new File(String.format(newFilePattern, hamWid[0],
				hamWid[1], hamWid[2], hamWid[3], hamWid[4]));

		BufferedWriter writer = null;
		BufferedReader[] reader = new BufferedReader[5];
		try {
			writer = new BufferedWriter(new FileWriter(res));
			for (int i = 0; i < 5; i++)
				reader[i] = new BufferedReader(new FileReader(String
						.format(originalFilePattern, hamWid[i])));
			writer.write(String.format(
					"@RELATION test_ham%.2f_%.2f_%.2f_%.2f_%.2f",
					hamWid[0], hamWid[1], hamWid[2], hamWid[3],
					hamWid[4]));
			writer.newLine();
			String line = null;
			String[] lineTokens = null;
			String[] resTokens = null;
			for (int i = 0; i < 75; i++) {
				for (BufferedReader r : reader) {
					line = r.readLine();
				}
				if (i == 0)
					continue;
				writer.write(line);
				writer.newLine();
			}
			do {
				resTokens = null;
				for (int i = 0; i < 5; i++) {
					line = reader[i].readLine();
					if (line == null)
						break;
					lineTokens = line.split(", ");
					if (lineTokens.length < 2)
						break;
					if (resTokens == null) {
						resTokens = new String[lineTokens.length];
						for (int j = 0; j < 7; j++)
							resTokens[j] = lineTokens[j];
					} else {
						for (int j = 3 + 4 * i; j < 7 + 4 * i; j++)
							resTokens[j] = lineTokens[j];
					}
				}
				if (line == null)
					break;
				for (int j = 23; j < lineTokens.length; j++)
					resTokens[j] = lineTokens[j];
				writer.write(resTokens[0]);
				for (int i = 1; i < resTokens.length; i++) {
					writer.write(", ");
					writer.write(resTokens[i]);
				}
				writer.newLine();
			} while (line != null);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (writer != null)
					writer.close();
				for (BufferedReader r : reader)
					r.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return res;
	}
}
