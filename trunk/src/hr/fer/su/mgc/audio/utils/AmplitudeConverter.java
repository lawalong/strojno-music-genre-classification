package hr.fer.su.mgc.audio.utils;

import java.io.File;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.tritonus.dsp.ais.AmplitudeAudioInputStream;

public class AmplitudeConverter {

	public static void main(String[] args) throws Exception {
		boolean bAmplitudeIsLog = false;
		int nArgIndex = -1;
		if (args.length == 1) {
			if (args[0].equals("-h")) {
				printUsageAndExit();
			} else {
				printUsageAndExit();
			}
		} else if (args.length == 4) {
			if (args[0].equals("--lin")) {
				bAmplitudeIsLog = false;
			} else if (args[0].equals("--log")) {
				bAmplitudeIsLog = true;
			} else {
				printUsageAndExit();
			}
			nArgIndex = 1;
		} else if (args.length == 3) {
			nArgIndex = 0;
		} else {
			printUsageAndExit();
		}
		float fAmplitude = Float.parseFloat(args[nArgIndex + 0]);
		String strSourceFilename = args[nArgIndex + 1];
		String strTargetFilename = args[nArgIndex + 2];
		File sourceFile = new File(strSourceFilename);
		File targetFile = new File(strTargetFilename);

		AudioInputStream sourceAudioInputStream = AudioSystem
				.getAudioInputStream(sourceFile);
		if (sourceAudioInputStream == null) {
			out("cannot open audio file");
			System.exit(1);
		}

		AudioFileFormat aff = AudioSystem.getAudioFileFormat(sourceFile);
		AudioFileFormat.Type targetType = aff.getType();

		AmplitudeAudioInputStream amplifiedAudioInputStream = new AmplitudeAudioInputStream(
				sourceAudioInputStream);

		/*
		 * Here, we set the desired amplification.
		 */
		if (bAmplitudeIsLog) {
			amplifiedAudioInputStream.setAmplitudeLog(fAmplitude);
		} else {
			amplifiedAudioInputStream.setAmplitudeLinear(fAmplitude);
		}

		/*
		 * And finally, we are writing the amplified stream to a new file.
		 */
		AudioSystem.write(amplifiedAudioInputStream, targetType, targetFile);
	}

	private static void printUsageAndExit() {
		out("AmplitudeConverter: usage:");
		out("\tjava AmplitudeConverter -h");
		out("\tjava AmplitudeConverter [--lin|--log] <amplitude> <sourcefile> <targetfile>");
		System.exit(0);
	}

	private static void out(String strMessage) {
		System.out.println(strMessage);
	}
}
