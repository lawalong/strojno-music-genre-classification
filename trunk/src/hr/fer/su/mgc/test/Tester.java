package hr.fer.su.mgc.test;

import hr.fer.su.mgc.Config;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

public class Tester {

	public static void main(String[] args) throws Exception {

		Config.init();
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JFrame frame = new JFrame();

				DefaultPieDataset dataset = new DefaultPieDataset();
				dataset.setValue("a1", .200);
				dataset.setValue("a2", .32);
				dataset.setValue("a3", .21);
				dataset.setValue("a4", .1);
				dataset.setValue("a5", .05);
				
				JFreeChart chart = ChartFactory.createPieChart("Chart 1",
						dataset, true, true, false);
				PiePlot plot = (PiePlot) chart.getPlot();
				plot.setCircular(true);
				plot.setBackgroundAlpha(0.1f);
				JPanel chartPanel = new ChartPanel(chart);
				
				frame.add(chartPanel);
				frame.setSize(400, 400);
				frame.setVisible(true);
				
		        // create the dataset...
		        CategoryDataset dataset2 = createDataset();
				
				chart = ChartFactory.createBarChart(
						"Naziv", "Žanrovi", "Postotak", dataset2, PlotOrientation.VERTICAL, true, true, false);
				
				chart.getPlot().setBackgroundAlpha(0.1f);
				JPanel chartPanel2 = new ChartPanel(chart, true);
				frame = new JFrame();
				frame.add(chartPanel2);
				frame.setLocation(500, 0);
				frame.setSize(400, 400);
				frame.setVisible(true);
			}
		});

	}
	
	/**
	 * Returns a sample dataset.
	 * 
	 * @return The dataset.
	 */
	protected static CategoryDataset createDataset() {

		// row keys...
		double val1 = .2, val2 = .3, val3 = .05, val4 = .104;
		String genre1 = "blues", genre2 = "jazz", genre3 = "classical", genre4 = "rock";

		// create the dataset...
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();

		dataset.addValue(val1, genre1, "");
		dataset.addValue(val2, genre2, "");
		dataset.addValue(val3, genre3, "");
		dataset.addValue(val4, genre4, "");

		return dataset;
	}

}
