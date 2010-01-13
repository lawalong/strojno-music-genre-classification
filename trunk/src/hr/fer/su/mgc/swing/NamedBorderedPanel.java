package hr.fer.su.mgc.swing;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

public abstract class NamedBorderedPanel extends JPanel {

	private static final long serialVersionUID = -259154702802673292L;
	
	protected JPanel panel;
	
	public NamedBorderedPanel(String title, int top, int left, int bottom, int right) {
		super(new BorderLayout());
		
		setBorder(BorderFactory.createEmptyBorder(top, left, bottom, right));
		panel = new JPanel();
		panel.setBorder(BorderFactory.createTitledBorder(title));
		add(panel, BorderLayout.CENTER);
		
		init();
	}
	
	public abstract void init();

}
