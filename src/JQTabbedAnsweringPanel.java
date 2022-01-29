import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.event.*;

import javax.swing.event.ChangeEvent; import javax.swing.event.ChangeListener;

/*
 * Created on 10.08.2006
 *
 */

/**
 * Klasse zur Erstellung des Eingabe-Panels mit den Reitern unten
 * @author dani
 */
public class JQTabbedAnsweringPanel extends JPanel implements ActionListener, ChangeListener, KeyListener {

	private JQuiz parent;
	private JTabbedPane tabbedPane;
	JQuizBoxPanel quizBoxPanel;
	private JTextField solveField;
	static JTextField statusLabel;
	static JTextArea galgenRatenArea;
	JTextArea answerTextArea;
	char galgenRatenKey;
	JToggleButton nullButton;
	GalgenPanel galgenPanel;

	JQTabbedAnsweringPanel (JQuiz p) {
		parent = p;
		tabbedPane = new JTabbedPane(JTabbedPane.BOTTOM);

		// Antwort - Panel
		JPanel solvePanel01 = new JPanel();
		solvePanel01.add (new JLabel("Lösungsvorschlag :   ", JLabel.CENTER));
		solveField = new JTextField(16);
		solveField.setBorder (new SoftBevelBorder (1));
		solveField.setToolTipText("Minimal " + JQSettings.SSML + " Zeichen eingeben!");
		solvePanel01.add(solveField);
		solveField.addKeyListener(this);

		JPanel solvePanel = new JPanel ();
		// solvePanel.setLayout (new GridLayout(3, 1)); // wenn Abstand drin, dann 5 Zeilen
		solvePanel.setLayout(new BoxLayout (solvePanel, BoxLayout.Y_AXIS));
		solvePanel.add (solvePanel01);
		
		JPanel statusPanel = new JPanel();
		statusLabel = new JTextField (7);
		statusPanel.add(statusLabel);
		Font statusFont = new Font ("Arial",Font.BOLD, 60);
		statusLabel.setFont (statusFont);
		statusLabel.setHorizontalAlignment(JTextField.CENTER);
		statusLabel.setBackground (Color.GRAY);
		statusLabel.setBorder (BorderFactory.createLoweredBevelBorder());
		solvePanel.add (statusPanel);
		
		JPanel solvePanel02 = new JPanel();
		answerTextArea = new JTextArea (7, 45);
		answerTextArea.setLineWrap (true); answerTextArea.setWrapStyleWord (true);
		answerTextArea.setBorder (new LineBorder (Color.GRAY, 1));
		answerTextArea.setEditable (false);
		answerTextArea.setForeground (Color.black);  		// farbiges Textfeld
		answerTextArea.setBackground ( new Color ( 0.6f, 0.95f, 0.6f));
		solvePanel02.add (answerTextArea);
		solvePanel02.setBorder(BorderFactory.createCompoundBorder (
			BorderFactory.createTitledBorder("Richtige Lösung: "), 
			BorderFactory.createEmptyBorder(5,5,5,5)));

		solvePanel.add (solvePanel02);
		tabbedPane.addTab ("Lösung eingeben", null, solvePanel,"Lösen mit Text eingeben (minimale Zeichenzahl beachten)");

		// Galgen-Raten Panel
		GridBagLayout g = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.ipadx = 180;
		c.ipady = 240;
		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets (5, 5, 5, 5);

		JPanel galgenRatenPanel = new JPanel();
		galgenRatenPanel.setLayout(g);
		galgenPanel = new GalgenPanel();
		g.setConstraints (galgenPanel, c);
		galgenRatenPanel.add (galgenPanel);

		//c.gridwidth = GridBagConstraints.REMAINDER; //end row
		c.gridx = 1;
		c.ipadx = 0;
		c.ipady = 30;

		JPanel test = new JPanel();
		test.add(galgenRatenArea = new JTextArea(8, 28)); // test vorerst nur Mal
		galgenRatenArea.setLineWrap(true); galgenRatenArea.setWrapStyleWord (true);
		galgenRatenArea.setBorder ( BorderFactory.createLoweredBevelBorder () );
		galgenRatenArea.setBackground (JQSettings.BOARDGREEN);
		galgenRatenArea.setForeground(Color.WHITE);
	
		galgenRatenPanel.add(test);
		g.setConstraints (test, c);
		galgenRatenPanel.add (test);
		
		JPanel charPanel = new JPanel();
		charPanel.setLayout(new GridLayout(3, 10));

		char a = 'a';
		JToggleButton charButton;
		ButtonGroup alphabet = new ButtonGroup();
		
		nullButton = new JToggleButton("0"); // für nichtsichtbar-Setzung
		alphabet.add(nullButton);

		while (a <= 'x') {
			Character aChar = new Character(a);
			charButton = new JToggleButton(aChar.toString());
			charPanel.add(charButton);
			alphabet.add(charButton);
			charButton.addActionListener(this);
			a++;
		}

		int i = 0;
		for (String str[] = { "y", "z", "ä", "ö", "ü", "ß" }; i < 6; i++) {
			charPanel.add(charButton = new JToggleButton(str[i]));
			alphabet.add(charButton);
			charButton.addActionListener(this);
		}
		
		c.gridwidth = 2;
		c.gridx = 0;
		c.gridy = 1;
		g.setConstraints (charPanel, c);
		galgenRatenPanel.add (charPanel);
		tabbedPane.addTab(
			"Galgenraten",
			null,
			galgenRatenPanel,
			"Lösunghilfe durch Angabe von Buchstaben");
		

		// Quiz Panel
		JPanel quizPanel = new JPanel();
		quizPanel.setBorder(BorderFactory.createEtchedBorder());
		quizPanel.setLayout(new GridLayout(1, 8, 10, 5));
		tabbedPane.addTab(
			"wählbare Quiz-Bücher",
			null,
			quizPanel,
			"Vokabelbuch wählen");
		quizPanel.add(quizBoxPanel = new JQuizBoxPanel());
		// quizPanel.setLayout (new FlowLayout (FlowLayout.CENTER));

		tabbedPane.addChangeListener(this);
		add(tabbedPane);

		/* Implementiert ItemListener 
		  public void itemStateChanged(ItemEvent e) {
			if (newItem==true) {
			  possibleScore++;
			  switch (item.correctAnswer) {
				case 1:
				  if (answers.getSelectedCheckbox() == answerA) {
					  actualScore++;
				  }
				  break;
				case 2:
				  if (answers.getSelectedCheckbox() == answerB) {
					  actualScore++;
				  }
				  break;
				case 3:
				  if (answers.getSelectedCheckbox() == answerC) {
					  actualScore++;
				  }
				  break;
				case 4:
				  if (answers.getSelectedCheckbox() == answerD) {
					  actualScore++;
				  }
				  break;
			  }
			  lastAnswer =
					  answers.getSelectedCheckbox().getLabel();
			  statusLabel1.setText("Ihre Antwort lautet: " + lastAnswer);
			  newItem = false;
			}
			else{
			   statusLabel1.setText("Sie haben bereits " +
									 lastAnswer + " geantwortet");
			}
			statusLabel2.setText(quiz.getStatusString());
		  }
		*/

/*
 * TODO warum tut das nicht?? 
		tabbedPane.setMnemonicAt(0, 'L');
		tabbedPane.setMnemonicAt(1, 'G');
		tabbedPane.setMnemonicAt(2, 'Q');
*/
	}

	public Insets getInsets() {
		// return new Insets(10, 20, 10, 20);
		return new Insets(0, 0, 0, 0);
	}

	/**
	 * Lösungsfeld leeren
	 */
	public void clearSolveField() {
		solveField.setText("");
	}

	/**
	 * @return Text im solveField
	 */
	public String getSolveField() {
		
		return solveField.getText();
	}

	/**
	 * setzt den Cursor in das Lösungsfeld
	 */
	public void requestCursorInSolveField() {
		solveField.requestFocus();
	}

	/** 
	 *  löscht das Status-Feld im ersten Reiter
	 */
	public void setStatusNeutral ()	{
		statusLabel.setText ("");
	}

	/** 
	 *  schreibt "Richtig" in das Status-Feld im ersten Reiter
	 */
	public void setStatusRight ()	{
		statusLabel.setText ("RICHTIG");
		statusLabel.setForeground (Color.GREEN);
	}

	/** 
	 *  schreibt "Falsch" in das Status-Feld im ersten Reiter
	 */
	public void setStatusWrong ()	{
		statusLabel.setText ("FALSCH");
		statusLabel.setForeground (Color.RED);
	}

	/**
	 * 
	 */
	public void setStatusInvalid() {
		statusLabel.setText ("UNGÜLTIG");
		statusLabel.setForeground (Color.BLACK);
	}

	/** 
	 *  zeigt ersten Reiter an (SolvePane - Tab)
	 */
	public void showSolvePanel () {
		// 
		tabbedPane.setSelectedIndex(0);
	}

	/** 
	 *  setzt neue Texte auf den mit index bezeichneten Reiter
	 */
	public void setNewTabText (int index, String title, String help) {
		// 
		tabbedPane.setTitleAt (index, title);
		tabbedPane.setToolTipTextAt (index, help);
	}

	public void actionPerformed (ActionEvent evt) {
		String key = evt.getActionCommand();
		galgenRatenKey = key.toLowerCase().charAt(0);
		// galgenRatenArea.setText("gedrückte Taste war: " + key.toUpperCase());
	}

	public void stateChanged (ChangeEvent e) {
		parent.tabbedPanelSelected (tabbedPane.getSelectedIndex());
		// parent.setTaskOnHold (); war nur bei getSelectedIndex>0
	}

	public int getSelecetedPanel () {
		return tabbedPane.getSelectedIndex();
	}
	
	/**
	 * gibt den gewählten Buchstaben im GalgenRaten zurück
	 * @return char - der gewählte Buchstabe
	 */
	public char getGalgenRatenKey() {
		return galgenRatenKey;
	}
	
	/**
	 * löscht Angaben im GalgenRaten
	 */
	public void resetGalgenRaten () {
		galgenRatenKey = ' '; // lieber Leerzeichen wegen Suchfunktionen
		setGalgenRatenTextArea("");
		nullButton.setSelected(true);
	}
	
	/**
	 * setzt Text in das Antwort-Textfeld auf dem SolvePanel (1.Reiter)
	 * @param str
	 */
	public void setAnswerTextArea (String str) {
		answerTextArea.setText (str);
	}

	/**
	 * setzt den aktuellen galgenRatenString in das Textfeld (2.Reiter)
	 * @param galgenString
	 */
	public void setGalgenRatenTextArea (String galgenString) {
		galgenRatenArea.setText (galgenString);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
	 */
	public void keyPressed(KeyEvent e) {
		// nothing to do
	}

	/* (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
	 */
	public void keyReleased(KeyEvent e) {
		// nothing to do
	}

	/* (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
	 */
	public void keyTyped(KeyEvent e) {
		if (e.getKeyChar() == KeyEvent.VK_ENTER)
			parent.actQuiz();
	}

/*	public void setSolveField (String str)	{
		solveField.setText("Anzahl Datensätze = " + str);
	}*/
	
}

