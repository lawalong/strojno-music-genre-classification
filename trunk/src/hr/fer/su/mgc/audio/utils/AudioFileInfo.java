package hr.fer.su.mgc.audio.utils;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

public class AudioFileInfo {
	private static final int LOAD_METHOD_STREAM = 1;
	private static final int LOAD_METHOD_FILE = 2;
	private static final int LOAD_METHOD_URL = 3;

	public static void main(String[] args) {
		if (args.length == 0) {
			printUsageAndExit();
		}
		int nLoadMethod = LOAD_METHOD_FILE;
		boolean bCheckAudioInputStream = false;
		boolean bOutputProperties = false;
		int nCurrentArg = 0;
		while (nCurrentArg < args.length) {
			if (args[nCurrentArg].equals("-h")) {
				printUsageAndExit();
			} else if (args[nCurrentArg].equals("-s")) {
				nLoadMethod = LOAD_METHOD_STREAM;
			} else if (args[nCurrentArg].equals("-f")) {
				nLoadMethod = LOAD_METHOD_FILE;
			} else if (args[nCurrentArg].equals("-u")) {
				nLoadMethod = LOAD_METHOD_URL;
			} else if (args[nCurrentArg].equals("-i")) {
				bCheckAudioInputStream = true;
			} else if (args[nCurrentArg].equals("-p")) {
				bOutputProperties = true;
			}

			nCurrentArg++;
		}
		String strSource = args[nCurrentArg - 1];
		String strFilename = null;
		AudioFileFormat aff = null;
		AudioInputStream ais = null;
		try {
			switch (nLoadMethod) {
			case LOAD_METHOD_STREAM:
				InputStream inputStream = System.in;
				aff = AudioSystem.getAudioFileFormat(inputStream);
				strFilename = "<standard input>";
				if (bCheckAudioInputStream) {
					ais = AudioSystem.getAudioInputStream(inputStream);
				}
				break;

			case LOAD_METHOD_FILE:
				File file = new File(strSource);
				aff = AudioSystem.getAudioFileFormat(file);
				strFilename = file.getCanonicalPath();
				if (bCheckAudioInputStream) {
					ais = AudioSystem.getAudioInputStream(file);
				}
				break;

			case LOAD_METHOD_URL:
				URL url = new URL(strSource);
				aff = AudioSystem.getAudioFileFormat(url);
				strFilename = url.toString();
				if (bCheckAudioInputStream) {
					ais = AudioSystem.getAudioInputStream(url);
				}
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		if (aff == null) {
			out("Cannot determine format");
		} else {
			outputFileFormat(strFilename, aff);
			if (bCheckAudioInputStream) {
				outputAudioInputStream(ais);
			}
			out("---------------------------------------------------------------------------");
			if (bOutputProperties) {
				out("AudioFileFormat properties:");
				Map<String, Object> properties = aff.properties();
				outputProperties(properties);
				out("---------------------------------------------------------------------------");
				out("AudioFormat properties:");
				properties = aff.getFormat().properties();
				outputProperties(properties);
			}
		}
	}

	private static void outputFileFormat(String strFilename, AudioFileFormat aff) {
		AudioFormat format = aff.getFormat();
		out("---------------------------------------------------------------------------");
		out("Source: " + strFilename);
		out("Type: " + aff.getType());
		out("AudioFormat: " + format);
		out("---------------------------------------------------------------------------");
		String strAudioLength = null;
		if (aff.getFrameLength() != AudioSystem.NOT_SPECIFIED) {
			strAudioLength = "" + aff.getFrameLength() + " frames, "
					+ aff.getFrameLength() * format.getFrameSize() + " bytes, "
					+ (aff.getFrameLength() / format.getFrameRate())
					+ " seconds";
		} else {
			strAudioLength = "unknown";
		}
		out("Length of audio data: " + strAudioLength);
		String strFileLength = null;
		if (aff.getByteLength() != AudioSystem.NOT_SPECIFIED) {
			strFileLength = "" + aff.getByteLength() + " bytes";
		} else {
			strFileLength = "unknown";
		}
		out("Total length of file (including headers): " + strFileLength);
	}

	private static void outputAudioInputStream(AudioInputStream ais) {
		String strAudioLength = null;
		if (ais.getFrameLength() != AudioSystem.NOT_SPECIFIED) {
			strAudioLength = "" + ais.getFrameLength() + " frames (= "
					+ ais.getFrameLength() * ais.getFormat().getFrameSize()
					+ " bytes)";
		} else {
			strAudioLength = "unknown";
		}
		out("[AudioInputStream says:] Length of audio data: " + strAudioLength);
	}

	private static void outputProperties(Map<String, Object> properties) {
		if (properties == null) {
			return;
		}
		Set<Map.Entry<String, Object>> entries = properties.entrySet();
		boolean bHasProperties = !entries.isEmpty();
		if (bHasProperties) {
			Iterator<Map.Entry<String, Object>> iter = entries.iterator();
			while (iter.hasNext()) {
				Map.Entry<String, Object> entry = iter.next();
				out(entry.getKey() + " = " + entry.getValue());
			}
		} else {
			out("[no properties]");
		}
		out("---------------------------------------------------------------------------");
	}

	private static void printUsageAndExit() {
		out("AudioFileInfo: usage:");
		out("\tjava AudioFileInfo [-s|-f|-u] [-i] [-p] <audiofile>");
		System.exit(1);
	}

	private static void out(String strMessage) {
		System.out.println(strMessage);
	}
}
