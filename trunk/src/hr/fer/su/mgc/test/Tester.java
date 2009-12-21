package hr.fer.su.mgc.test;

import hr.fer.su.mgc.matlab.MatlabEngineWin;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.UnsupportedAudioFileException;


public class Tester {


	public static void main(String[] args) throws UnsupportedAudioFileException, IOException {
		
		String result = MatlabEngineWin.runScript(new File("./matlab/test.m"));
		System.out.println(result);
	}

}
