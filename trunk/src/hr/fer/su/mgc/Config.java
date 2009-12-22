package hr.fer.su.mgc;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Config {
	
	private static Map<String, String> properties;
	
	public static void init() throws IOException {
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

}
