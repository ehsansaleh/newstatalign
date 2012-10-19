package statalign.postprocess.plugins;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import statalign.base.InputData;
import statalign.base.State;
import statalign.postprocess.Postprocess;
import statalign.postprocess.gui.StructureGUI;
import statalign.postprocess.utils.RNAFoldingTools;
import fr.orsay.lri.varna.exceptions.ExceptionNonEqualLength;
import fr.orsay.lri.varna.models.rna.RNA;

public class Structure extends statalign.postprocess.Postprocess {

	/**
	 * Postprocess which handles the consensus structure generated by PPFold.
	 * 
	 * @author Preeti Arunapuram
	 */

	public String title;
	private JPanel pan = new JPanel(new BorderLayout());
	private StructureGUI gui;
	private ArrayList<JComponent> toolbar;
	private ButtonGroup group;

	public static float[][] probMatrix;
	public static float[] singleMatrix;
	public static String currentSequence;
	public static String currentDotBracketStructure;
	
	//public boolean probMode = true;

	public Structure() {
		sampling = true;
		screenable = true;
		outputable = true;
		postprocessable = true;
		postprocessWrite = true;
		hasToolBar = true;
		rnaAssociated = true;
	}

	@Override
	public void init() {
		toolbar = new ArrayList<JComponent>();
		createToolBar();
	}

	@Override
	public String getTabName() {
		return "Consensus Structure";
	}

	@Override
	public Icon getIcon() {
		// TODO Auto-generated method stub
		return new ImageIcon("icons/test1.png");
	}

	@Override
	public JPanel getJPanel() {
		// TODO Auto-generated method stub
		//pan = new JPanel(new BorderLayout());
		return pan;
	}
	
	@Override
	public void reloadPanel() {
		pan = new JPanel(new BorderLayout());
	}

	@Override
	public String getTip() {
		// TODO Auto-generated method stub
		return "Consensus structure";
	}
	
	@Override
    public double getTabOrder() {
        return 9.0d;
    }

	@Override
	public void setSampling(boolean enabled) {
		// TODO Auto-generated method stub
		sampling = enabled;

	}
	
	@Override
	public String[] getDependences() {
		return new String[] { "statalign.postprocess.plugins.PPFold"};
	}
	
	public PPFold ppfold;
	@Override
	public void refToDependences(Postprocess[] plugins) {
		ppfold = (PPFold) plugins[0];
	}

	public static void updateBasePairMatrix(float[][] newMatrix) {
		probMatrix = newMatrix;
	}
	
	public static void updateSingleMatrix(float[] newMatrix) {
		singleMatrix = newMatrix;
	}

	public static void updateStructure() {
		RNAFoldingTools rnaTools = new RNAFoldingTools();
		int[] pairedSites = rnaTools.getPosteriorDecodingConsensusStructureMultiThreaded(probMatrix);
		currentDotBracketStructure = RNAFoldingTools.getDotBracketStringFromPairedSites(pairedSites);
	}

	public void updateSequence() {
		currentSequence = ppfold.getSequenceByName(ppfold.getSequences(), ppfold.getRefName()).replaceAll("-", "");;
	}
	
	@Override
    public ArrayList<JComponent> getToolBarItems() {
        return toolbar;
    }

	private void createToolBar() {
		toolbar.add(new JToolBar.Separator());
		group = new ButtonGroup();
		
		
		JToggleButton normalButton = new JToggleButton(new ImageIcon(ClassLoader.getSystemResource("icons/normalRNA.png")));
		//JToggleButton normalButton = new JToggleButton(new ImageIcon(ClassLoader.getSystemResource("icons/phylogram.png")));
		String text = "Normal mode";
		normalButton.setToolTipText(text);
    	normalButton.setActionCommand(text);
    	normalButton.setEnabled(false);
    	normalButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				RNA.probMode = false;
				//probMode = false;
				gui.repaint();
			}
    		
    	});
		
    	JToggleButton probButton = new JToggleButton(new ImageIcon(ClassLoader.getSystemResource("icons/probRNA.png")));
    	String probText = "Probability mode";
    	probButton.setToolTipText(probText);
    	probButton.setActionCommand(probText);
    	probButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				RNA.probMode = true;
				//probMode = true;
				gui.repaint();
			}
    		
    	});  
    	
    	group.add(normalButton);
    	group.add(probButton);
    	
    	toolbar.add(normalButton);
    	toolbar.add(probButton);
    	
    	probButton.setSelected(true);
	}
	
	@Override
	public void beforeFirstSample(InputData input) {
		if(show) {
			
			Enumeration<AbstractButton> buttons = group.getElements();
        	while (buttons.hasMoreElements()) {
        		buttons.nextElement().setEnabled(true);
        	}
			
			pan.removeAll();
			title = input.title;
			try {
				gui = new StructureGUI(title);
			} catch (ExceptionNonEqualLength e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			JScrollPane scroll = new JScrollPane(gui);
			pan.add(scroll, BorderLayout.CENTER);
			System.out.println("Structure parent: " + pan.getParent());
			if(pan.getParent() != null)
			{
				pan.getParent().validate();
			}
			
		}
	}

	@Override
	public void newSample(State state, int no, int total) {

		if(sampling) {
			if(ppfold.samplingAndAveragingPPfold)
			{
				updateSequence();			
				updateStructure();
			}
			//gui.updateAndDraw(currentSequence, currentDotBracketStructure);
		}
		
		if(show) {
			if(ppfold.samplingAndAveragingPPfold)
			{
				gui.updateAndDraw(currentSequence, currentDotBracketStructure);
			}			
		}
	}

	@Override
	public void afterLastSample() {

	}

}