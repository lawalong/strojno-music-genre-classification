package hr.fer.su.mgc.test;

import java.io.IOException;

import javax.sound.sampled.UnsupportedAudioFileException;

import com.meapsoft.DSP;


public class Tester {


	public static void main(String[] args) throws UnsupportedAudioFileException, IOException {
		
		double[][] a = new double[100][100];
		
		for(int i = 0; i < 100; i++)
			for(int j = 0; j < 100; j++)
				a[i][j] = -i - 5*j;
		
		DSP.imagesc(a);

	}

}
