package hr.fer.su.mgc;

import hr.fer.su.mgc.classifier.ClassifierAdapter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Config {
	
	private static Map<String, String> properties;
	
	private static Map<String, File> hypotheses;
	
	public static void init() throws IOException {
		
		initConfig();
		
		initHypotheses();
		
	}

	private static void initConfig() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader("./config/matlab.cfg"));
		
		properties = new HashMap<String, String>();
		
		String line; String[] parts;
		while((line = reader.readLine()) != null) {
			parts = line.split("=");
			
			if(parts[0].equals("") || parts[1].equals(""))
					continue;
			
			properties.put(parts[0], parts[1]);
		}
		
		reader.close();
	}
	
	
	public static String getProperty(String key) {
		return properties.get(key);
	}
	
	
	public static String getProperties() {
		String ret = "";
		for(String key : properties.keySet())
			ret += key + "=" + properties.get(key) + "\n";
		return ret;
	}
	
	
	private static void initHypotheses() {
		
		hypotheses = new HashMap<String, File>();
		
		for(File hypFile : new File("hypothesis").listFiles())
			if(hypFile.isFile()) hypotheses.put(hypFile.getName(), hypFile);
	}
	
	public static File getHypothesisAsFile(String name) {
		return hypotheses.get(name);
	}

	public static ClassifierAdapter getHypothesis(String name) throws Exception {
		return loadHypothesis(hypotheses.get(name));
	}
	
	public static ClassifierAdapter loadHypothesis(File hypothesis) throws Exception {
		
		// Read hypotheses file...
		ObjectInputStream input = 
			new ObjectInputStream(new FileInputStream(hypothesis));
		
		ClassifierAdapter weka = (ClassifierAdapter) input.readObject();
		input.close();
		
		return weka;
	}
	
	public static String[] getAllHypothesesNames() {
		if(hypotheses.size() == 0) return new String[0];
		String[] ret = new String[hypotheses.size()];
		int i = 0;
		for(String name : hypotheses.keySet()) ret[i++] = name;
		return ret;
	}
	
	/**
	 * Grab default genre array from internal dataset.
	 * @return genre array
	 * @throws Exception in case dataset path doesn't exist 
	 * or is not a directory
	 */
	public static String[] grabGenres() throws Exception {
		return grabGenres(new File("./dataset"));
	}
	
	/**
	 * Returns genres array from specified dataset
	 * @param datasetPath dataset path
	 * @return genre array
	 * @throws Exception in case dataset path doesn't exist 
	 * or is not a directory
	 */
	public static String[] grabGenres(File datasetPath) throws Exception {
		List<String> tmpList = new ArrayList<String>();
		
		if(datasetPath.isDirectory()) throw new Exception("Dataset path is invalid!");
		
		for(File genre : datasetPath.listFiles())
			if(genre.isDirectory()) tmpList.add(genre.getName());
		
		String[] ret = new String[tmpList.size()];
		for(int i = 0; i < tmpList.size(); i++) ret[i] = tmpList.get(i);
		
		return ret;
	}

}
