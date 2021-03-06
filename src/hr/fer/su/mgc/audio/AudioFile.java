package hr.fer.su.mgc.audio;

import hr.fer.su.mgc.swing.MGCSwingMain;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JLabel;
import javax.swing.JSlider;

import javazoom.jl.decoder.Bitstream;

import org.tritonus.share.sampled.TAudioFormat;
import org.tritonus.share.sampled.file.TAudioFileFormat;

public class AudioFile {
	
	private File audioFile;
	
	public File getAudioFile() {
		return audioFile;
	}

	private AudioInputStream inputStream;
	private AudioInputStream decodedInputStream;
	
	private AudioFileFormat baseFileFormat;
	private AudioFormat baseFormat;
	
	private AudioFormat decodedFormat;
	
	private SourceDataLine line;
	
	/**
	 * State variable.<br>
	 * 0 := stopped
	 * 1 := paused
	 * 2 := running
	 */
	public Integer state;
	
	private PlaybackThread playback;
	
	private SliderThread sliderThread;

	@SuppressWarnings("unchecked")
	public AudioFile(String filePath, MGCSwingMain mainRef)
			throws UnsupportedAudioFileException, IOException {
		audioFile = new File(filePath);

		state = new Integer(0);
		
		audioInit();
		
		inputStream.close();
		
		
		// Load metadata...
		
		Integer duration = null; String title = null, author = null, genre = null;
		
		if (baseFileFormat instanceof TAudioFileFormat) {
			Map properties = ((TAudioFileFormat) baseFileFormat).properties();
			
			// Grab duration in ms
			
			try {
				duration = Integer.valueOf(properties.get("duration").toString())/1000;
			} catch (Exception Ignorable) { }
			
			try {
				title = properties.get("title").toString();
			} catch (Exception Ignorable) { }
			
			try {
				author = properties.get("author").toString();
			} catch (Exception Ignorable) { }
			try {
				genre = properties.get("mp3.id3tag.genre").toString();
			} catch (Exception Ignorable) { }

		} else if (baseFormat instanceof TAudioFormat) {
			Map properties = ((TAudioFormat) baseFormat).properties();
			duration = Integer.valueOf(properties.get("duration").toString());
		}
		
		if(duration == null) duration = (int) 
			((baseFileFormat.getFrameLength() / baseFormat.getFrameRate())*1000);
		if(title == null || title.equals("")) title = "Unknown";
		if(author == null || author.equals("")) author = "Unknown";
		if(genre == null || genre.equals("")) genre = "Unknown";
		
		JLabel tagLabel = mainRef.getPlayerPanel().getTagLabel();
		JSlider slider = mainRef.getPlayerPanel().getPlayerSlider();
		
		// Set duration in ms
		slider.setMaximum(duration);
		
		// Set tag label
		tagLabel.setText("<html><b>" + title
				+ "</b> by " + author + " [" + genre + "]</html>");

		
		slider.setMajorTickSpacing(slider.getMaximum()/8);
		slider.setMinorTickSpacing(slider.getMajorTickSpacing()/5);

		Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
		for (int i = slider.getMinimum(); 
			i <= slider.getMaximum(); i += slider.getMajorTickSpacing()) {

			int secs = i / 1000, mins = 0;
			while (secs >= 60) {
				mins++;
				secs -= 60;
			}

			labelTable.put(i, new JLabel(String.valueOf(mins) + ":"
					+ ((secs < 10) ? "0" + secs : secs)));
		}
		slider.setLabelTable(labelTable);
	}
	
	private void audioInit() throws IOException, UnsupportedAudioFileException {
		// Checking for errors...
		try {
			inputStream = AudioSystem.getAudioInputStream(audioFile);
		} catch (Exception ex) {
			FileInputStream fileIn = new FileInputStream(audioFile);
			Bitstream m = new Bitstream(fileIn);
			long start = m.header_pos();

			// Need to open the stream again
			try { m.close(); } catch(Exception ex2) { ex2.printStackTrace(); }
			fileIn = new FileInputStream(audioFile);

			// Skip the header
			fileIn.skip(start);
			
			inputStream = AudioSystem.getAudioInputStream(fileIn);
		}

		baseFileFormat = AudioSystem.getAudioFileFormat(audioFile);
		baseFormat = baseFileFormat.getFormat();
	}
	
	private void playInit(MGCSwingMain mainRef) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
		
		audioInit();

		DataLine.Info info = new DataLine.Info(SourceDataLine.class,
				baseFormat, AudioSystem.NOT_SPECIFIED);
		if(AudioSystem.isLineSupported(info)) {
			decodedFormat = baseFormat;
			decodedInputStream = inputStream;
		}
		else {
			decodedFormat = new AudioFormat(
					AudioFormat.Encoding.PCM_SIGNED,
					baseFormat.getSampleRate(), 16, baseFormat.getChannels(),
					baseFormat.getChannels() * 2, baseFormat.getSampleRate(),
					false);
			decodedInputStream = AudioSystem.getAudioInputStream(decodedFormat, inputStream);
		}
		
		line = getLine(decodedFormat);
		
		playback = new PlaybackThread();
		sliderThread = new SliderThread(mainRef);
	}


	private SourceDataLine getLine(AudioFormat audioFormat)
			throws LineUnavailableException {
		SourceDataLine res = null;
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
		res = (SourceDataLine) AudioSystem.getLine(info);
		res.open(audioFormat);
		return res;
	}
	
	public void play(MGCSwingMain mainRef) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
		
		// Wait for last stop request to finish...
		while(state == -1) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException Ignorable) { }
		}
		
		synchronized (state) {
			switch(state) {
			case 0:
				playInit(mainRef);
				line.start();
				state = 2;
				sliderThread.start();
				playback.start();
				break;
			case 1:
				line.start();
				state = 2;
				synchronized(playback) {
					playback.notify();
				}
				synchronized(sliderThread) {
					sliderThread.notify();
				}
				
				break;
			case 2: // Restart playback...
				stop();
				play(mainRef);
				break;
			}
		}
	}
	
	public void pause() {
		synchronized (state) {
			if(state == 2) {
				line.stop();
				state = 1;
			}
		}
	}
	
	public void seek(long bytesToSkip) {		// TODO
		try {
			line.stop();
			line.flush();
			decodedInputStream.reset();
			decodedInputStream.skip(bytesToSkip);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public void stop() {
		synchronized (state) {
			int tmpState = state;
			state = 0;
			if(tmpState == 1) {
				synchronized(playback) {
					playback.notify();
				}
				synchronized(sliderThread) {
					sliderThread.notify();
				}
			} else if(tmpState == 2) {
				line.stop();
				close();
			}
		}
	}
	
	
	private void close() {
		try {
			state = 0;
			line.close();
			inputStream.close();
		} catch (Exception Ignorable) { }
	}


	class PlaybackThread extends Thread {
		
		public static final int BUFFER_SIZE = 16384;
		
		private byte[] data;

		public PlaybackThread() {
			data = new byte[BUFFER_SIZE];
		}

		@Override
		public void run() {
			int nBytesRead = 0;
			
			try {
				while (nBytesRead != -1) {
					switch(state) {
					case 0: return;
					case 1:
						while(state == 1) {
							try {
								synchronized (this) {
									this.wait();
								}
							} catch (InterruptedException ex) {
								continue;
							}
						}
						if(state == 0) return;
						break;
					}

					
					nBytesRead = decodedInputStream.read(data, 0, data.length);

					if (nBytesRead != -1) {
						line.write(data, 0, nBytesRead);
					}
					
				}
				line.drain();
			
			} catch (IOException Ignorable) {}
			close();
		}
	}
	
	class SliderThread extends Thread {
		private MGCSwingMain mainRef;

		public SliderThread(MGCSwingMain mainRef) {
			this.mainRef = mainRef;
		}

		@Override
		public void run() {
			JSlider slider = mainRef.getPlayerPanel().getPlayerSlider();
			while(true) {
				switch(state) {
				case 0: 
					slider.setValue(0);
					return;
				case 1:
					while(state == 1) {
						try {
							synchronized (this) {
								this.wait();
							}
						} catch (InterruptedException ex) {
							continue;
						}
					}
					if(state == 0) {
						slider.setValue(0);
						return;
					}
					break;
				}
//				mainRef.timeLabel.setText(text)
				slider.setValue((int)(line.getMicrosecondPosition()/1000));
				try {
					Thread.sleep(80);
				} catch (InterruptedException Ignorable) { }
				
			}
		}
	}


}
