package hr.fer.su.mgc.swing;

import hr.fer.su.mgc.classifier.ClassifierAdapter;
import hr.fer.su.mgc.conv.ConversionException;
import hr.fer.su.mgc.conv.MGCconv;
import hr.fer.su.mgc.features.FeatureExtractor;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

public class MultiClassificationThread extends Thread {
	
	private MGCSwingMain mainRef;
	
	private long totalTime;
	
	private File classificationTarget;
	
	private ClassifierAdapter classifier;
	
	protected JProgressBar testProgress;
	
	protected JPanel testingChartPanel;
	
	private JScrollPane learnerScroll;
	
	private JPanel chartPanel;
	
	protected String[] genres;
	protected int[] correctClassCounter;
	protected int[] falseClassCounter;
	private int totalCorrect;
	private int totalFalse;
	
	private volatile boolean stopFlag = false;
	
	public void stopThreadASAP() {
		this.stopFlag = true;
		while(true) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException Ignorable) { }
			if(!this.isAlive()) break;
		}
	}

	public MultiClassificationThread(MGCSwingMain mainRef, 
			ClassifierAdapter classifier, File classificationTarget, 
			JPanel testingChartPanel, JProgressBar testProgress, JScrollPane learnerScroll) {
		this.mainRef = mainRef;
		this.classificationTarget = classificationTarget;
		this.classifier = classifier;
		this.testingChartPanel = testingChartPanel;
		this.testProgress = testProgress;
		this.learnerScroll = learnerScroll;
	}

	@Override
	public void run() {
		long time; File tempFile, song;
		double max = 0; int ind = -1;
		double[] result; int counter = 0;
		
		totalTime = System.currentTimeMillis();
		
		genres = classifier.getGenres();
		
		FeatureExtractor featureExtractor = new FeatureExtractor(genres);

		try {
			
			mainRef.clearOut();
		
			mainRef.writeOut("VALIDATION: Starting dataset validation from " +
					classificationTarget.getName(), false);
			
			correctClassCounter = new int[genres.length];
			falseClassCounter = new int[genres.length];
			totalCorrect = totalFalse = 0;
			
			String trueFalseVar, genreName;
			
			Map<File, String> tmpFileList = new HashMap<File, String>();
			
			for(File genreDir : classificationTarget.listFiles()) {
				if(genreDir.isDirectory()) {
					genreName = genreDir.getName().trim();
					if(!classifier.supportedGenre(genreName)) {
						String message = "VALIDATION ERROR: Genre " + genreName + 
							"is not supported by classifier! Skipping " + genreName + "...";
						mainRef.writeOut(message, true);
						continue;
					}
					
					for(File tmpSong : genreDir.listFiles()) {
						if(tmpSong.isFile()) {
							tmpFileList.put(tmpSong, genreName);
						}
					}
				}
			}
			
			for(File tmpSong : tmpFileList.keySet()) {
				genreName = tmpFileList.get(tmpSong);
				
				time = System.currentTimeMillis();
				
				try {
					tempFile = MGCconv.convertForClassification(tmpSong);
				} catch (ConversionException cex) {
					final String message = 
						"VALIDATION ERROR: " + cex.getLocalizedMessage() + 
						"\n Skipping file " + tmpSong.getName() + ".";
					mainRef.writeOut(message, true);
					continue;
				}
				
				song = featureExtractor.extractSongFeatures(new File[] {tempFile});
				
				if(tempFile.exists()) tempFile.delete();

				result = classifier.classifyInstance(song);
				
				if(song.exists()) song.delete();
				
				max = 0; ind = -1;
				for (int i = 0; i < result.length; ++i) {
					if (max < result[i]) { max = result[i]; ind = i; }
				}
				
				if(genres[ind].equalsIgnoreCase(genreName)) {
					correctClassCounter[ind]++;
					totalCorrect++;
					trueFalseVar = "TRUE";
				} else {
					falseClassCounter[grabIndexFromGenres(genres, genreName)]++;
					totalFalse++;
					trueFalseVar = "FALSE";
				}
				
				mainRef.writeOut("VALIDATION: " + genreName + "/" + tmpSong.getName() + " => " + 
						genres[ind] + " => " + trueFalseVar + ". Completed in " + 
						((System.currentTimeMillis() - time)/1000f) + " seconds.", false);
				
				if(stopFlag) { finish(); return; }
				
				updateSlider(++counter);
				
				if(counter % 5 == 0) updateDatasetTestingCharts();
			}
			
			finish();
		
		} catch (Throwable e) {
			e.printStackTrace();
			final String message = 
				"VALIDATION ERROR: " + e.getLocalizedMessage();
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					JOptionPane.showMessageDialog(mainRef, message, 
							"Validation Error", JOptionPane.ERROR_MESSAGE);
				}
			});

			mainRef.writeOut(message, true);
		}
	}
	
	private void finish() {
		updateDatasetTestingCharts();
		
		updateSlider(testProgress.getMaximum());
		
		mainRef.writeOut("VALIDATION: Completed in " + 
				((System.currentTimeMillis() - totalTime)/1000f) + " seconds.", false);
		
		mainRef.writeOut(" => Result: " + totalCorrect + " classified correctly, " + 
				totalFalse + " false (" + (100*totalCorrect/(totalCorrect + totalFalse)) + "%).", false);
	}
	
	private int grabIndexFromGenres(String[] genres, String genre) {
		for(int i = 0; i < genres.length; i++)
			if(genres[i].equalsIgnoreCase(genre)) return i;
		return -1;
	}
	
	protected void updateSlider(final int percentage) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				testProgress.setValue(percentage);
			}
		});
	}
	
	protected void updateDatasetTestingCharts() {
		SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
		    	
				if(genres.length == 0) return;
				
				int i;
				
				// Create Pie Chart ...
				
				DefaultPieDataset pieDataset = new DefaultPieDataset();
				pieDataset.setValue("Correct", totalCorrect);
				pieDataset.setValue("Incorrect", totalFalse);
				
				JFreeChart chart = ChartFactory.createPieChart(
						"Classification Correct/Incorrect", pieDataset, true, true, false);
				PiePlot plot = (PiePlot) chart.getPlot();
				plot.setCircular(true);
				plot.setBackgroundAlpha(0.1f);
				final JPanel piePanel = new ChartPanel(chart);
				
				
				DefaultCategoryDataset barDataset = new DefaultCategoryDataset();
				double perc;
				for(i = 0; i < genres.length; i++) {
					if(!(correctClassCounter[i] + falseClassCounter[i] == 0)) {
						perc = (double)correctClassCounter[i]/
							(correctClassCounter[i] + falseClassCounter[i]);
						barDataset.addValue(perc, genres[i], "");
					}
				}
				
				chart = ChartFactory.createBarChart(
						"Classification percentage on genres.", "Genres", "Percentage", 
						barDataset, PlotOrientation.VERTICAL, true, true, false);
				
				chart.getPlot().setBackgroundAlpha(0.1f);
				final JPanel barPanel = new ChartPanel(chart, true);
				
				testingChartPanel.removeAll();
				
				chartPanel = new JPanel(new GridLayout(1, 2, 16, 0));
				chartPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
				chartPanel.add(piePanel); chartPanel.add(barPanel);
				testingChartPanel.add(chartPanel, BorderLayout.CENTER);
				
				testingChartPanel.revalidate();
				
				learnerScroll.getVerticalScrollBar().setValue(
						learnerScroll.getVerticalScrollBar().getMaximum());
		    	
				learnerScroll.invalidate();
		    }
		});
	}
	
}