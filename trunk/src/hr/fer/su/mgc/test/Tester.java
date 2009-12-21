package hr.fer.su.mgc.test;

import java.io.IOException;
import java.util.GregorianCalendar;

import javax.sound.sampled.UnsupportedAudioFileException;


public class Tester {


	public static void main(String[] args) throws UnsupportedAudioFileException, IOException {
		
		long time = GregorianCalendar.getInstance().getTimeInMillis();
		
		StringBuffer sb = new StringBuffer();
		
		for(int i = 0; i < 10000000; i++) {
			sb.append(i);
			sb.setLength(0);
		}
		
		System.out.println("Time passed: " + 
				((float)(GregorianCalendar.getInstance().getTimeInMillis()-time)/1000) + " seconds.");
	}

}
