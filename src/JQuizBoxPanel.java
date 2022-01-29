import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

/*
 * Created on 20.09.2006
 *
 */

/**
 * Nochmal eine Extra-Klasse zum Erzeugen des 3. Reiters vom Eingabe-Panel,
 * der Quiz-Tabelle
 * @author Dani
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */

public class JQuizBoxPanel extends Box {
	final String[] books = new String [] {
		"Wörterbuch kpl.", //"Wortliste vom xyz 2020",
		"Wortliste aus Couplehood",
		"Wortliste Heikes geistl. Buch",
		"unregelmäßige Verben für Bobo (engl. - deutsch)",
		"1. änderbares Wörterbuch " + JQuiz.dedicatedTo, 
		"2. änderbares Wörterbuch " + JQuiz.dedicatedTo, 
	};
	JTextArea[] labels;
	ButtonGroup buttonGroup;
	JRadioButton[] choices;

	public JQuizBoxPanel() {
		super(BoxLayout.Y_AXIS);

		ImageIcon checkOff = new ImageIcon(getClass().getResource("/images/radio_off.gif"));
		ImageIcon checkOn = new ImageIcon(getClass().getResource("/images/radio_on.gif"));
		choices = new JRadioButton[JQSettings.QUIZCHOICES+1];
		labels = new JTextArea[JQSettings.QUIZCHOICES+1];
		buttonGroup = new ButtonGroup();
		
		choices[0] = new JRadioButton (); // Dummy-Button um Gruppe unselected zu setzen
		buttonGroup.add(choices[0]);
		choices[0].setMnemonic(0);
		choices[0].setSelected(true);

		// Erstellung der Quiz-Einträge, ACHTUNG 1. sichtbarer Eintrag ist choices [1],
		// da ButtonGroup nicht mehr unselected gewählt werden kann 
		int i = 1;
		
		for (char a=65; a<JQSettings.QUIZCHOICES+65; a++, i++) {
			JPanel b = new JPanel();
			b.setLayout(new FlowLayout(FlowLayout.LEFT));

			// RadioButtons mit Icons und Text
			Character a_char = new Character (a);
			choices[i] = new JRadioButton (a_char.toString(), checkOff);
			choices[i].setSelectedIcon(checkOn);
			choices[i].setMnemonic(a);
			// choices[i].addActionListener(this);
			buttonGroup.add (choices[i]);
			add (choices[i]);
						
			b.setBorder (new SoftBevelBorder(BevelBorder.LOWERED));
			b.add ( choices[i] );

			b.add (createVerticalStrut(15));

			labels[i] = new JTextArea (books[i-1], 2, 35);
			labels[i].setForeground (Color.BLACK);
			labels[i].setBackground (new Color ( 0.9f, 0.9f, 0.95f));
			labels[i].setEditable (false);
			labels[i].setLineWrap (true);
			labels[i].setWrapStyleWord(true);
			b.add (labels[i]);
			b.add (createVerticalStrut(15));

			add (b);
		}
		choices[3].setSelected (true); // Vorwahl Heikes Text
	}

	/**
	 * gibt die QuizWahl zurück 0-5
	 * @param void
	 * @return 0-5 (0 = 1. Eintrag)
	 */
	public int getSelectedChoice (boolean resetChoices) {
		int i = buttonGroup.getSelection().getMnemonic();
		if (resetChoices) unselectChoices();
		if (( i >= 65 ) && ( i <= 65+JQSettings.QUIZCHOICES ))
			return i-64;
		else
			return 0;
	}

	/** 
	 * setzt Wahl auf nicht markiert (kein Kreuz zu sehen)
	 * in Wirklichkeit wird (da nicht anders implementierbar) 
	 * ein nicht sichtbarer JRadioButton choices[0] markiert, 
	 * siehe JavaDoc zu JRadioButton
	 */
	public void unselectChoices() {
		choices[0].setSelected (true);
	}

	/**
	 * setzt Text in die Auswahlfelder ein
	 * 
	 * @param String-Array
	 * @return void
	 */
	public void setQuizLabels (String[] s){
		final int MAX_CHARS = 180;
		
		for (int i = 1; i <= JQSettings.QUIZCHOICES; i++)
			if ( s[i-1].length() < MAX_CHARS)
				labels[i].setText (s[i-1]); // sichtbare Labels beginnen bei Labels[1]
			else 
				labels[i].setText (s[i-1].substring(0, MAX_CHARS));
	}

	/**
	 * @param choice
	 * @param rightQuizItem
	 */
	public void setBackgrounds(int selected, int rightSolution) {
		
		if (selected > 0)
			labels[selected].setBackground (new Color ( 0.9f, 0.2f, 0.1f));
		labels[rightSolution+1].setBackground (new Color ( 0.15f, 0.8f, 0.35f));
			// Korrektur notwendig, weil Buttons erst bei 1 beginnen 
			// (0 nicht dargestellt = Rücksetz-Buttton)
		
	}
	
	/**
	 * @param choice
	 * @param rightQuizItem
	 */
	public void resetBackgrounds() {
		
		for (int i=1; i<= JQSettings.QUIZCHOICES; i++)
			labels[i].setBackground (new Color ( 0.9f, 0.9f, 0.95f));

	}
	
	
}
