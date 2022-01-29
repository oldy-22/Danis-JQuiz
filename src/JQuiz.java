import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

/** 
 * Hauptklasse der Java - Quiz - Anwendung
 * zum Trainieren mit Vokabelkatalogen
 * @author Daniel Enke
 * 
 * @version x.0 vom 
 *		Design aller Panel etwas angleichen
 * 	gleiches Design Schultafel für alle Fenster (mit loweredBorder? 1x titledborder)
 * 	TODO: oder mit Worterkennung if solveString.length() >=0.7* task.smallestWord.length() ??
 * 	mit TextParser irr.Verbs dafür aufbereiten (2.+3. Zeile löschen, dann neu machen)
 * 
 * privatisieren von Daten in Klassen z.B. Voreinstellungen als presets als Vorbereitung für Menus
 **/

public class JQuiz extends JFrame implements ActionListener {
	
	// ------------ Änderbare KOSTANTENDEFINITIONEN ------------
	
	public static final String APP_TITLE = "VQuiz - Danis Vokabeltrainer ";
	static String dedicatedTo ="";
	public static String APP_VERSION = " - Version 1.3.1";
	public static boolean DEBUG = false;
	
	// Begrüßungstext im Fragenfenster
	final String ANFANGSTEXT = "Quiz-Buch wählen auf 3. Reiter!";
	final String LOADBOOKTEXT = "Die Aufgaben sind meist aus Büchern abgeschrieben " +
				"und von uns eingetippt. Wer selbst irgendwelche Sachen zum Quizzen tippen wil, " +
				"kann dies im Verzeichnis (HOME)/JQuiz in einem der beiden freien Bücher tun!" + 
				"\nNamen lauten: vocabulary_free_01.txt oder vocabulary_free_02.txt" + // TODO 210423 sehr dirt zu korrigieren mit Variablen!!!
				"\nFormat: Frage [TAB] Antwort [ENTER] (alle Unicode-Zeichen möglich)";

	// ------------ Nicht änderbare KOSTANTENDEFINITIONEN ------------

	// Texte auf den Buttons
	final String LOSGEHTS			= "        Hier gehts los !!!         ";
	final String SENDEN				= "              SENDEN !            ";
	final String GESCHEITERT	= "         Neues Rätsel!         ";
	final String ZEIGEN				= " Zeigen ";
	final String ENDE					= " Ende ";
	final String WEITER				= "Wusste ich genau!";
	final String ÜBEN					= "Nochmal fragen?";

	 // Beantwortung Stati
	int isRight; 
	static final int WRONG = 0;
	static final int INVALID = 1;
	static final int RIGHT = 2;
	
	// Panel-Nummern der JTabbedPane
	final int SOLVEPANEL = 0;
	final int GALGENRATENPANEL = 1;
	final int QUIZPANEL = 2;
	
	// ------------ globale Variablen ------------

	// Bezüge zu den benötigten Klassen-Instanzen
	JQSettings settings;
	JQTabbedAnsweringPanel quizPane;
	static JQEngine engine;
	QuizItem quizItem;
	
	// ------------ sonstige Variablen ------------
	
	boolean firstTimeSelectedGRPanel, firstTimeSelectedQPanel;
		// Hilfsvariable: nur bei erstem Anklicken soll Aktion in Gang gesetzt werden
	/**wird nur am Beginn der Anwendung gebraucht, um TaskText mit Infos zu füllen*/
	boolean loadbooksClicked = false;

	/** Zähler für aktuellen Versuch */
	int tryCount = JQSettings.TRYMAX;

	/** Variablen für die Statistik */
	int trials=0, right=0, wrong=0, invalid=0;
		
	// GUI-Elemente für die Zugriff bestehen muss
	JButton jb1, jb2, jb3;
	JLabel light1, light2, light3;
	String qLabels[] = new String[JQSettings.QUIZCHOICES];
	
	// änderbare Inhalte aus JQuiz-Engine
	JTextArea taskTextArea;
	static JLabel statusRow;

	public static void main (String[] args) {
		
		try {
			UIManager.setLookAndFeel(
				UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			System.err.println("Couldn't use the system "
						 + "look and feel: " + e);
		}

		if (JQSettings.user  == JQSettings.NO_ONE) dedicatedTo = "";
		if (JQSettings.user  == JQSettings.BOBO) dedicatedTo = "für Bobo ";
		if (JQSettings.user  == JQSettings.TOMMI) dedicatedTo = "für Thomas ";
		if (JQSettings.user  == JQSettings.JO) dedicatedTo = "für Johannes ";
		if (JQSettings.user  == JQSettings.RDKB) dedicatedTo = "für RD/KB ";

		JQuiz quiz = new JQuiz();

		WindowListener l = new WindowAdapter() {
			public void windowClosing (WindowEvent e) {
				closeApplication();
				System.exit(0);
			}
		};

		quiz.addWindowListener (l);
		quiz.pack ();

		engine = new JQEngine ( quiz );

		quiz.setVisible (true);

	}
	
	public JQuiz() {
		
		super (APP_TITLE + dedicatedTo + APP_VERSION);
		setIconImage(new ImageIcon(getClass().getResource("/images/gdict.png")).getImage());
		settings = new JQSettings();

		// Hauptfenster
		JPanel pane0 = new JPanel ();
		pane0.setLayout (new BorderLayout ());

		// Aufgabenbereich
		JPanel pane1 = new JPanel ();
		pane1.setBorder (new EmptyBorder (10, 40, 10, 40));
		pane1.setLayout (new BoxLayout (pane1, BoxLayout.Y_AXIS));
		
		// JLabel taskMark = new JLabel ("Aufgabe", JLabel.CENTER); rausgenommen zur Verkleinerung
		// taskMark.setBorder (new EmptyBorder (10, 0, 10, 0));
 		// pane1.add (taskMark); 
		taskTextArea = new JTextArea (ANFANGSTEXT, 3, 30);
		taskTextArea.setLineWrap (true); taskTextArea.setWrapStyleWord (true);
		taskTextArea.setBorder (new LineBorder (Color.GRAY, 1));
		pane1.add (taskTextArea);
		taskTextArea.setEditable (false);
		taskTextArea.setForeground (new Color ( 0.1f, 0.05f, 0.6f)); // farbiges Textfeld
		taskTextArea.setBackground (new Color ( 0.95f, 0.95f, 0.6f));

		// Lösungsbereich
		quizPane = new JQTabbedAnsweringPanel (this);

		//Action-Bereich
		JPanel actionArea = new JPanel ();
		actionArea.setBorder (new EmptyBorder (10, 0, 10, 0));
		actionArea.setLayout (new FlowLayout (FlowLayout.LEFT));
		
		jb1 = new JButton (LOSGEHTS);
		jb1.setMargin ( getButtonInsets ());
		jb1.setMnemonic ('s');
		jb1.setToolTipText ("Lösung abschicken, neue Aufgabe holen oder Lösung gewusst.");
		jb1.addActionListener (this);
		actionArea.add ( jb1 );
		
		jb2 = new JButton (ZEIGEN);
		jb2.setMargin ( getButtonInsets ());
		jb2.setMnemonic ('n');
		jb2.setToolTipText ("Richtige Lösung zeigen oder Lösung nicht gewusst.");
		jb2.addActionListener (this);
		actionArea.add ( jb2 );
		jb2.setEnabled (false);
		
		jb3 = new JButton (ENDE);
		jb3.setMargin ( getButtonInsets ());
		jb3.setMnemonic ('e');
		jb3.setToolTipText ("Programm beenden und Fenster schließen! Gelöste Aufgaben merkt es sich.");
		jb3.addActionListener (this);
		actionArea.add ( jb3 );

		// Statusbereich
		JPanel statusArea = new JPanel ();
		statusArea.setLayout (new FlowLayout (FlowLayout.LEFT));
		
		// 3 Ampeln in Statusleiste
		ImageIcon lightoff = new ImageIcon (getClass().getResource("/images/lightoff.gif"));
		ImageIcon lighton = new ImageIcon (getClass().getResource("/images/lighton.gif"));
		light1 = new JLabel (lighton); light1.setDisabledIcon (lightoff);
		light2 = new JLabel (lighton); light2.setDisabledIcon (lightoff);
		light3 = new JLabel (lighton); light3.setDisabledIcon (lightoff);
		statusArea.add (light1); statusArea.add (light2); statusArea.add (light3);
		light1.setEnabled(false); light2.setEnabled(false); light3.setEnabled(false);

		statusRow = new JLabel ("by Dani since January 2006 { :-)");
		JPanel test = new JPanel();
		test.add(statusRow);
		// statusArea.setBorder(new EmptyBorder(10, 0, 10, 0)); Platz reicht vorerst nicht dafuer
		statusArea.add (test);
		statusArea.setBorder (BorderFactory.createLoweredBevelBorder());
		
		JPanel pane2 = new JPanel();
		pane2.setLayout (new BoxLayout (pane2, BoxLayout.Y_AXIS));
		pane2.add ( actionArea );
		pane2.add ( statusArea );

		pane0.add ("North", pane1);
		pane0.add ("Center", quizPane);
		pane0.add ("South", pane2);

		setContentPane(pane0);
	}

	public Insets getButtonInsets () {
		
		return new Insets (12, 40, 12, 40);
	}
	
	public void actionPerformed (ActionEvent evt) {
	Object src = evt.getSource ();

	if (src instanceof JButton)
		
		if (evt.getActionCommand() == ENDE) {
			closeApplication ();
		}
		else if (evt.getActionCommand() == LOSGEHTS) {
			loadBooks();
			showNextTask();
		}
		else if (evt.getActionCommand() == SENDEN) {
			actQuiz();
		}
		else if (evt.getActionCommand() == WEITER) {
			engine.setQuizItemReady();
			showNextTask();
			setStatusRow("abgesandt: " + trials + ", richtig:  " + right	+ ", falsch: " + wrong // + ", ungültig: " + invalid 
				+ ", ungelöste Vokabeln: "	+ engine.unAskedQuestions.size());
		}
		else if (evt.getActionCommand() == GESCHEITERT) {
			showNextTask();
		}
		else if (evt.getActionCommand() == ÜBEN) {
			showNextTask();
		}
		else if (evt.getActionCommand() == ZEIGEN) {
			showAnswer ();
			quizPane.setStatusNeutral();
		}
		
	}

	/** lädt mit der engine die Vokabel-Datei */
	public void loadBooks() {
		
		int ch = quizPane.quizBoxPanel.getSelectedChoice (true);
		engine.loadBook (settings.getVocFilename(ch-1));
		quizPane.setNewTabText(2, "Quizzen", "Lösung aus Vorschlägen quizzen");
		for (int i=0; i<JQSettings.QUIZCHOICES; i++) qLabels[i]="";
		quizPane.quizBoxPanel.setQuizLabels(qLabels);
	}

	/** zeigt die nächste Aufgabe (zu lösenden Vokabel) */
	private void showNextTask () {
		
		quizItem = engine.getRandomLearnRollQuizItem();
		setTaskText (quizItem.getQuestion());
		setAnswerText ("");
		quizPane.setStatusNeutral();
		jb1.setText (SENDEN);
		jb2.setText (ZEIGEN);
		jb2.setEnabled (true);
		tryCount = 1;
		showLights (tryCount);

		quizPane.showSolvePanel();
		quizPane.clearSolveField();
		quizPane.requestCursorInSolveField();
		quizPane.resetGalgenRaten();

		firstTimeSelectedGRPanel=true;
		firstTimeSelectedQPanel=true;
		
		quizPane.quizBoxPanel.resetBackgrounds();
		quizPane.quizBoxPanel.unselectChoices();
	}
	
	/** ruft die Prüf-Aktionen: Lösen, Galgenraten, Quizzen auf */ 
	void actQuiz() {
		trials++;
		switch (quizPane.getSelecetedPanel()) {
			case SOLVEPANEL: solveIt(); break;
			case GALGENRATENPANEL: gruessIt(); break;
			case QUIZPANEL: quizIt(); break;
		}
		setStatusRow("abgesandt: " + trials + ", richtig:  " + right	+ ", falsch: " + wrong // + ", ungültig: " + invalid 
			+ ", ungelöste Vokabeln: "	+ engine.unAskedQuestions.size());
	}

	/** sendet eine Lösung an die Quiz-Engine */
	private void solveIt() {
		
		int incrementsPerTry = 1;
		if (isQuizzable (incrementsPerTry) ) { // zähle 1 zur Anzahl der Lösungsversuche dazu 
			isRight = engine.checkIt(quizPane.getSolveField());
			if (isRight == RIGHT) {
				right++;
				quizPane.setStatusRight();
				setAnswerText (quizItem.getAnswer() + "\n" + quizItem.getComment());
				jb1.setText (WEITER);
				jb2.setText (ÜBEN);
			} 
			else	if (isRight == WRONG) {
				quizPane.setStatusWrong();
				wrong++;
			} else {
				quizPane.setStatusInvalid();
				invalid ++;
			}
		}

		quizPane.clearSolveField();
		quizPane.requestCursorInSolveField();
	}


	/** prüft, ob gequizzt werden kann, setzt Ampeln und Texte */
	public boolean isQuizzable (int incrementTry) {
		if (tryCount <= JQSettings.TRYMAX){
			tryCount += incrementTry;
			showLights (tryCount);
			return true;
		} else {
			showAnswer ();
			jb1.setText (GESCHEITERT);
			return false;
		}
	}

	
	/** Lösen der Aufgabe wird abgebrochen, da Lösung gezeigt */
	private void setTaskOnHold () {
		tryCount = JQSettings.TRYMAX + 1;
		showLights (tryCount);
	}


	/** nur noch 1 Lösungsversuch, z.B. beim Quizzen */
	private void setChoicesToOne () {
		tryCount = JQSettings.TRYMAX;
		showLights (tryCount);
	}


	/** zeigt die Antwort, wenn Aufgabe beendet / aufgegeben wird */
	private void showAnswer() {
		quizPane.showSolvePanel();
		String answerString = new String (quizItem.getAnswer() + "\n" + quizItem.getComment());
		setAnswerText (answerString);

		jb1.setText (GESCHEITERT);
		jb2.setEnabled (false);
		setTaskOnHold();
	}

	/** setzt die Frage / Aufgabe in oberstes Textfeld ein */
	private void setTaskText (String str)	{
		taskTextArea.setText (str);
	}
	
	/** setzt die Antwort / Lösung in unterstes Textfeld ein */
	private void setAnswerText (String str)	{
		quizPane.setAnswerTextArea (str);
	}

	/**
	 * @param Versuchsnummer läuft von 1 bis 4 
	 * aktualisiert die Ampel-Lichter unten links nach der Anzahl der Versuche
	 */
	private void showLights (int retries) {
		switch (retries) {
			case 1: light1.setEnabled (true); light2.setEnabled (true); 
				light3.setEnabled (true); break;
			case 4 : light3.setEnabled (false);
			case 3 : light2.setEnabled (false);
			case 2 : light1.setEnabled (false);
		}
	}

	/** setzt die Statuszeile (ganz unten) ein */
	public void setStatusRow ( String str) {
		statusRow.setText (str);
	}

	/** reagiert auf einen ChangeEvent vom TabbedAnsweringPanel
	 * und startet die Funktionen zum Galgenraten oder quizzen
	 * @param Index des aktivierten Panels [0-2] 
	 */
	public void tabbedPanelSelected (int panel) {
		
		switch (panel) {
			case GALGENRATENPANEL :
				if (firstTimeSelectedGRPanel) startGalgenRaten (); 
				firstTimeSelectedGRPanel=false; 
				quizPane.galgenPanel.startWackelArm(); break;
			case QUIZPANEL :
				boolean solve = false;
				if (firstTimeSelectedGRPanel) solve=true;
				if (firstTimeSelectedQPanel) startQuiz();
				if (! solve) setTaskOnHold();
				firstTimeSelectedQPanel=false;
				quizPane.galgenPanel.stopWackelArm(); 
				if (loadbooksClicked)	break;
				setTaskText(LOADBOOKTEXT); loadbooksClicked=true;
				setStatusRow("Viel Erfolg! :-)");
			case SOLVEPANEL:
				quizPane.galgenPanel.stopWackelArm(); 
				quizPane.requestCursorInSolveField(); break;
		}
	}

	/** GalgenRatenPanel wurde angeklickt, galgenRatenString initialisieren */
	public void startGalgenRaten() {
		String galgenString = engine.initGalgenRaten();
		quizPane.setGalgenRatenTextArea (galgenString);
		quizPane.galgenPanel.showEmptyGalgen();
	}

	/** Funktion zum Galgenraten guess + gr(Galgenraten) */
	private void gruessIt() {
		
		int galgenIncrement = 1;
		
		StringBuffer galgenString = new StringBuffer();
		float rate = engine.fillGalgenRaten (galgenString, quizPane.getGalgenRatenKey());

		if (rate > 0.36) galgenIncrement++;
		if (rate > 0.24) galgenIncrement++;
		if (rate > 0.12) galgenIncrement++;
		int lightIncrement = quizPane.galgenPanel.showMoreOfGalgen(galgenIncrement);

		if (isQuizzable(lightIncrement))		
			quizPane.setGalgenRatenTextArea (new String (galgenString));

	}

	/** QuizPanel wurde angeklickt, Quiz initialisieren */
	public void startQuiz() {
		setChoicesToOne();
		qLabels = engine.initQuiz();
		quizPane.quizBoxPanel.setQuizLabels(qLabels);
	}

	/** ?? */
	private void quizIt() {
		
		int choice = quizPane.quizBoxPanel.getSelectedChoice (false);
		int rightQuizItem = engine.getRightQuizNumber();
		
		quizPane.quizBoxPanel.setBackgrounds (choice, rightQuizItem);

		int incrementsPerTry = 1;
		if (isQuizzable (incrementsPerTry) ) { // zähle 1 zur Anzahl der Lösungsversuche dazu 
			if (choice == 0) {
				jb1.setText (GESCHEITERT);
				invalid ++;
			} 
			else if (choice -1 == rightQuizItem) {
				right++;
				jb1.setText (WEITER);
				jb2.setText (ÜBEN);
			} 
			else	{
				wrong++;
				jb1.setText (GESCHEITERT);
			} 
		}
	}

	/** beendet JQuiz */
	public static void closeApplication() {
		engine.endEngine();
		System.exit(0);
	}

}

