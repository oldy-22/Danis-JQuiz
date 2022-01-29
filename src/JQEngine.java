import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.util.*;
import java.awt.*;
import javax.swing.*;

import IniFileTools.IniSearcher;

public class JQEngine {

	private IniSearcher iniSearch;
 
	private String bookFileName = "DaniFile";
		// zur Vorbelegung wenn keine Datei gewählt wurde, dadurch wird dann beim
		// ini-Schreiben nur Pseudo-Datei in Properties abgelegt und keine uaq?s initialisiert 

	/** Voreinstellungen, z.B. Fenstergröße, Position, ungestellte Fragennummern je Vokabelbuch */
	private Properties prop = new Properties();
	/** Zeitstempel der veränderbaren Vocabularies zum Checken von Veränderungen */ 
	Date vocabularyTimeStamp = new Date (0L);
	Date vocabularyTimeModified;

	/** alle Vokabeln stehen hier drin (qiv) */
    Vector quizItemVector = new Vector();
    	
	/**  beinhaltet als ungestellte Fragen die qiv-Positions (beginnend mit 0) */
	Vector unAskedQuestions = new Vector (500, 500); 
		
	/** vergleichbar einem Kartenstapel auf der Hand zum intensiveren Einprägen
		Lernrolle wird befüllt mit uaq-Einträgen als qiv-Positions! (nicht uaq-Positions) */
	Vector learnRoll = new Vector (JQSettings.LEARN_ROLL_SIZE);
		
	/** beinhaltet z.B. die letzten beiden Fragen-Nummern zum sperren */
	Vector noAsk = new Vector (JQSettings.TASKS_BLOCKED);
	
	/** Anzahl Datensätze des gesamten Vokabelbuches */
	int taskCounts = 0;
	
	/** aktueller Vok.-Nr. in quizItemVector, Lernrolle oder unAskedQuestions */
	int currentItem, currentItemInLR, currentItemInUAQ;

    JQuiz quiz; // parent
	
	/** Klasse zum Generieren der Zufallszahlen (muss global sichtbar sein)*/
	Random rand;  

	String galgenRatenString;
	
	String alphabet = new String ("ABCDEFGHIJKLMNOPQRSTUVWXYZ"+
		"abcdefghijklmnopqrstuvwxyz"+"äöüÄÖÜß");

	/** die wählbaren Lösungen für das Quizzen der Lösung */
	String qStrings[] = new String [JQSettings.QUIZCHOICES];
	
	/** für Quizzen: Fragen-Nummer einer ausgesuchten Quiz-Lösung (Hilfsvariable)*/
	int qNumber;
	
	/** qNumberRight: Position der richtigen Antwort im Quiz (beginnt mit 0)*/
	int qNumberRight;
	
	/** aktuelle Vokabeldatensätze der momentanen Aufgabe und einer 
	 * Quiz-Möglichkeit (nur Hilfvariable für Quizz-Logik)*/ 
	QuizItem qTask, qQuiz;


    /** 
     * generiert nach Funktionsaufruf von loadEngine() eine Quizengine, 
     * wobei die Quiz-Datei (Fragen und Antworten) von der angegebenen
     * URL geladen wird.
     */
	public JQEngine (JQuiz parent) {
		quiz = parent;

		iniSearch = new IniSearcher (quiz);
		loadIni();

		// initialisiere den Random-Generator
		rand = new Random(System.currentTimeMillis());
		
	}
	
	/**
	 * Copy vocabulary file from jar to the filesystem
	 *
	 */
	public static void extractFile (URL srcURL, String destFilename) {
		InputStream in; FileOutputStream out;
		byte[] bytes = new byte[512];
		int len = 0;
		try {
			in = srcURL.openStream(); 
			out = new FileOutputStream(new File (destFilename));
			while ((len = in.read(bytes)) != -1) {
				out.write(bytes, 0, len);
			}
			out.close(); in.close();
			
			// Änderungs-Datum auf Quellwert setzen, um nach Löschen des Vocs, trotzdem Fragen-Nrn. zu behalten 
			File oFile = new File (destFilename);
			File iFile= new File (srcURL.getFile());
			oFile.setLastModified(iFile.lastModified());
			
		} catch (FileNotFoundException exc) {
			System.err.println("File not found: " + destFilename);
		} catch (IOException exc) {
			System.err.println("I/O Error on file: " + destFilename);
		} catch (SecurityException exc) {
			System.err.println("You don\'t have enough rights on file: " + destFilename);
		}
	}

	/** lädt Settings */
	private void loadIni() {

		try {
			if ( iniSearch.isWorkingWithIni() ) {
				File f = new File (iniSearch.getIniFileName());
				if (f.exists()) prop.load( new FileInputStream( iniSearch.getIniFileName() ) );
				else	f.createNewFile();
			}
		} catch (FileNotFoundException ex) {
			JOptionPane.showMessageDialog(quiz, "Settings file not found.", "Read error", JOptionPane.WARNING_MESSAGE);
		} catch (IOException ex) {
			JOptionPane.showMessageDialog(quiz, "Could not read settings.", "Read error", JOptionPane.WARNING_MESSAGE);
		} catch (NumberFormatException ex) {
			JOptionPane.showMessageDialog(quiz, "Could not import the numbers of unasked Questions.", "Translate error", JOptionPane.WARNING_MESSAGE);
		}

		// Load window position
		try {
			int x = Integer.parseInt(prop.getProperty("WindowX"));
			int y = Integer.parseInt(prop.getProperty("WindowY"));
			int w = Integer.parseInt(prop.getProperty("WindowW"));
			int h = Integer.parseInt(prop.getProperty("WindowH"));
			quiz.setBounds(x, y, w, h);
		} catch (NumberFormatException nfe) {
			quiz.setBounds(0, 0, 650, 700);
		} 
	}
	

	/** 
	 * lädt die Vokabel-Datei in die QuizEngine
	 * @param Quiz-Datei-Namen als String, ACHTUNG Einschränkung bei Dateinamen
	 * der Wörterbücher: Wegen Datumsabgleich dürfen momentan interne Wörterbücher
	 * nicht mit "vocabulary" beginnen!
	 */
	public void loadBook (String fileName) {
		URL quizFileURL;
		InputStream inStream;

		if (fileName == "") fileName = "wed_heike.txt";
		bookFileName = new String (fileName);

		DateFormat df = DateFormat.getDateTimeInstance();

		try {
			vocabularyTimeStamp = df.parse(prop.getProperty("TimeStamp_"+fileName));
		} catch (Exception pe) {}

		try {
			quizFileURL = getClass().getResource("/data/" + fileName); // Startwert

			// Modfizierung Startwert, wenn mit ini gearbeitet wird, d.h. Files dürfen erstellt werden
			if (fileName.startsWith("vocabulary")) {
				if (iniSearch.isWorkingWithIni() ) {
					File f = new File (iniSearch.getIniDirName() + File.separator + fileName);
					quizFileURL = f.toURL();
					if ( ! f.exists()) // Buch wird zum 1. Mal geladen und/oder ini gerade in Erstellung
						extractFile( getClass().getResource("/data/" + fileName), iniSearch.getIniDirName() + File.separator + fileName );
					f = new File (iniSearch.getIniDirName() + File.separator + fileName);
					vocabularyTimeModified = new Date ( f.lastModified());
					prop.setProperty( "TimeStamp_"+fileName, df.format(vocabularyTimeModified) );
				}
			}

			// oeffne einen Stream, um die Quiz-Datei einzulesen
			inStream = quizFileURL.openStream();
			BufferedReader dataStream =
				new BufferedReader(new InputStreamReader(inStream, "UTF-8"));

			// lade die Quiz-Datei in unseren quizItemVector
			String inLine = null;
			while ((inLine = dataStream.readLine()) != null) {
				quizItemVector.addElement(new QuizItem(inLine));
				taskCounts ++;
			}
		} catch (MalformedURLException e) {
			JOptionPane.showMessageDialog(
				quiz,
				"Fehler bei der URL-Erstellung\nder Quiz-Datei",
				"File not found",
				JOptionPane.WARNING_MESSAGE);
		} catch (IOException e) {
		} catch (NoSuchElementException nsee) {
			JOptionPane.showMessageDialog(
				quiz,
				"Fehler im Vokabelbuch: " + fileName + " in Zeile " + (taskCounts+1) + " !",
				"Zeile unkorrekt!",
				JOptionPane.WARNING_MESSAGE);
		}

		// lese aus den Properties des gewählten Vokabelbuches die ungestellten Fragenummern oder generiere sie 
		if (prop.getProperty(bookFileName) == null) 
			for (int i=0; i< taskCounts; i++) { // neu anlegen
				unAskedQuestions.addElement (new Integer (i));
	//			if (JQSettings.ASK_BACKWARDS) unAskedQuestions.addElement (new Integer (-i));
			}
		else	if ( (fileName.startsWith("vocabulary")) && ( ! vocabularyTimeStamp.equals(vocabularyTimeModified)) )
			for (int i=0; i< taskCounts; i++) { // neu anlegen
				unAskedQuestions.addElement (new Integer (i));
	//			if (JQSettings.ASK_BACKWARDS) unAskedQuestions.addElement (new Integer (-i));
			}
		else {
			String copyToRemoveClamps = new String (prop.getProperty(bookFileName));
			copyToRemoveClamps = copyToRemoveClamps.replace('[', ' ');
			copyToRemoveClamps = copyToRemoveClamps.replace(']', ' ');
			StringTokenizer t = new StringTokenizer(copyToRemoveClamps, ", ");
			while (t.hasMoreTokens()) {
				unAskedQuestions.addElement (new Integer (Integer.parseInt (t.nextToken()) ));
			}
		}

		quiz.setStatusRow( "geladene Vokabeln: "+ taskCounts + ", davon unerledigt: " + unAskedQuestions.size() );

		if (JQuiz.DEBUG) {
			JOptionPane.showMessageDialog( quiz,
				"Anzahl Datensätze: " + (new Integer (taskCounts)).toString(),
				"Quiz-Daten gelesen", JOptionPane.WARNING_MESSAGE);
		}
		
		createLearnRoll ();
	}


	/**
	 * beendet die JQEingine
	 */
	public void endEngine () {
		try {
			saveIni();
		} catch (NullPointerException e) { /* nothing to do */}
		// e implementiert wegen ProgrammEnde vor Buchauswahl, dann darf keine ini gesichert werden
	}


	/**
	 * speichert Settings
	 */
	private void saveIni() {
		
		try {
			// Window size and position
			Rectangle r = quiz.getBounds();
			prop.setProperty("WindowX", (new Integer(r.x)).toString());
			prop.setProperty("WindowY", (new Integer(r.y)).toString());
			prop.setProperty("WindowW", (new Integer(r.width)).toString());
			prop.setProperty("WindowH", (new Integer(r.height)).toString());
			
			prop.setProperty (bookFileName, unAskedQuestions.toString());
			
			// Save settings
			if (iniSearch.isWorkingWithIni()) // wenn ohne ini gearbeitet werden soll
				prop.store (new FileOutputStream (iniSearch.getIniFileName()), "JQuiz Settings");
		} catch (FileNotFoundException ex) {  // NullPointerException dürfte hier nicht kommen
			JOptionPane.showMessageDialog(quiz, "Settings file not found.", "Save error", JOptionPane.WARNING_MESSAGE);
		} catch (IOException ex) {
			JOptionPane.showMessageDialog(quiz, "Could not save settings.", "Save error", JOptionPane.WARNING_MESSAGE);
		}
	}


	/**
	 * setzt aktuellen Datensatz als erledigt aus unAskedQuestions und Lernrolle
	 * heraus 
	 * @return QuizItem
	 */
	public void setQuizItemReady() {
		
		int uaqCount = setCurrentQuizItemAsked();
		setCurrentLearnRollQuizItemAsked(uaqCount);
	}


	/**
	 * erzeugt die Lernrolle, die zum besseren Einprägen von Vokabeln
	 * der Menge der zufällig gewürfelten Vokabeln auf erstmal z.B. 10 begrenzt
	 * Einträge auf der Lernrolle sind nicht Verweise in den unAskedQuestions-Vektor
	 * sondern direkt in den QuizItemVektor (QIV-Positions)
	 * Verwendung nur in loadEngine, sonst resetLearnRoll()
	 */
	private void createLearnRoll () {
		
		int uaqSize = unAskedQuestions.size();
		if (uaqSize <= JQSettings.LEARN_ROLL_SIZE) {
			for (int i=0; i < uaqSize; i++)
				learnRoll.addElement ( (Integer) unAskedQuestions.get (i) );
		} else {
			int i = 0, bufferUAQ_Index;
			Integer bufferQIV_Index;
			
			while (i < JQSettings.LEARN_ROLL_SIZE) {
				do {
					bufferUAQ_Index = rand.nextInt (uaqSize);
					bufferQIV_Index = ((Integer) unAskedQuestions.get(bufferUAQ_Index));
					
					// DEBUG für lange Datensätze
					// int answerLength=((QuizItem) quizItemVector.elementAt(bufferQIV_Index.intValue())).getAnswer().length();
				} while ( learnRoll.contains (bufferQIV_Index) );
				learnRoll.addElement ( bufferQIV_Index );
				i++;
			}
		}
		noAsk.addElement (new Integer (-1)); 
		noAsk.addElement (new Integer (-1)); 
	}


	/**
	 * wenn LernRolle leer, wird sie hiermit neu generiert (im Programmablauf)
	 */
	private void resetLearnRoll () {
		
		if (taskCounts <= JQSettings.LEARN_ROLL_SIZE) {
			for (int i=0; i < taskCounts; i++)
				learnRoll.addElement ( (Integer) unAskedQuestions.get (i) );
		} else {
			int i = 0, bufferUAQ_Index;
			Integer bufferQIV_Index;
			
			while (i < JQSettings.LEARN_ROLL_SIZE) {
				do {
					bufferUAQ_Index = rand.nextInt (unAskedQuestions.size());
					bufferQIV_Index = ((Integer) unAskedQuestions.get(bufferUAQ_Index));
				} while ( learnRoll.contains (bufferQIV_Index) );
				learnRoll.addElement ( bufferQIV_Index );
				i++;
			}
		}
		noAsk.remove(0); noAsk.addElement (new Integer (-1)); 
		noAsk.remove(0); noAsk.addElement (new Integer (-1)); 
	}


	/**
	 * liefert einen QuizItem aus der Lernrolle, die zum besseren Einprägen von Vokabeln
	 * der Menge der zufällig gewürfelten Vokabeln auf erstmal z.B. 10 begrenzt
	 */
	public QuizItem getRandomLearnRollQuizItem () {
		
		for (int harakiri=0; harakiri < 10; harakiri++) {
			currentItemInLR = rand.nextInt (learnRoll.size()); // 0-9 oder kleiner
			currentItem = ((Integer) learnRoll.get(currentItemInLR)).intValue();
			currentItemInUAQ = unAskedQuestions.indexOf(new Integer (currentItem));
			if ( ! noAsk.contains ( new Integer (currentItemInUAQ)) ) break;
		} 

		// Sperre letzter Fragen aktualisieren
		noAsk.remove(0); noAsk.addElement (new Integer (currentItemInUAQ)); 

		qTask = (QuizItem)  quizItemVector.get (currentItem);

		/* if (JQuiz.DEBUG) { so nicht mehr implementierbar, da question nun private
			qTask.question() = "LR: " + (currentItemInLR+1) + "/" + learnRoll.size() +
			" UAQ: " + (currentItemInUAQ+1) + "/" + unAskedQuestions.size() + " = " + 
			qTask.question;
		}*/
		return qTask;
	}


	/**
	 * ersetzt den aktuellen Eintrag auf der Lernrolle durch einen neuen, wenn der
	 * alte bekannt (eingeprägt) war
	 */
	private void setCurrentLearnRollQuizItemAsked (int uaqSize) {
		int bufferUAQ_Index;
		Integer bufferQIV_Index;

		if (uaqSize == 0) { // Neubeginn wenn alle abgefragt sind
			learnRoll.remove(currentItemInLR);
			resetLearnRoll();
		} else {
			if (uaqSize < JQSettings.LEARN_ROLL_SIZE) // Lernrolle verkleinern
				learnRoll.remove(currentItemInLR);
			else { // Lernrollen-Eintrag neu belegen
				do {
					bufferUAQ_Index = rand.nextInt(uaqSize);
					bufferQIV_Index = ((Integer) unAskedQuestions.get(bufferUAQ_Index));
				} while (learnRoll.contains (bufferQIV_Index));
				learnRoll.set (currentItemInLR, bufferQIV_Index);
			}
		}
	}

	/**
	 * gibt einen zufällig gewählten Datensatz zurück für Quizz
	 * @return QuizItem
	 */
	public QuizItem getRandomQuizItem() {
		
		qNumber = rand.nextInt(taskCounts);
		qQuiz = (QuizItem) quizItemVector.get (qNumber);

		/*if (JQuiz.DEBUG) {so nicht mehr implementierbar, da question nun private
			qQuiz.question = qNumber+1 + ": " + qQuiz.question;
		}*/

		return qQuiz;
	}

	/**
	 * streicht Eintrag aus Vektor mit ungestellten Fragen unAskedQuestions
	 * @return int Anzahl von Einträgen in uaq
	 */
	private int setCurrentQuizItemAsked () {
		
		unAskedQuestions.remove (currentItemInUAQ);
		int saveState = unAskedQuestions.size();
		if (saveState == 0) { // Neubeginn wenn alle abgefragt sind
			JOptionPane.showMessageDialog(
				quiz,
				"Wörterbuch fertig, beginne neu.",
				"Herzlichen Glückwunsch!!!",
				JOptionPane.INFORMATION_MESSAGE);
			for (int i=0; i<taskCounts; i++)
				unAskedQuestions.addElement (new Integer (i));
		}
		return saveState;
	}
		
	/**
	 * überprüft eingegebenen SolveString auf Übereinstimmung mit Lösung
	 * REM: hier wäre eine Möglichkeit, nochmal nachzuhaken, wenn Lösung 
	 * ähnlich erscheint
	 * @param string
	 * @return ob Antwort richtig war
	 */
	public int checkIt (String solveString) {
		String test, task;
		
		test = solveString.toUpperCase();
		task = qTask.getAnswer().toUpperCase();
		if ( (solveString.length() < JQSettings.SSML) && (task.length() >= JQSettings.SSML) )
			return JQuiz.INVALID;
		if (task.indexOf (test) == -1)
			return JQuiz.WRONG;
		else
			return JQuiz.RIGHT;
	}

/**
 * erstellt GalgenRatenString
 */
public String initGalgenRaten() {

	try {
		galgenRatenString = qTask.getAnswer();

		// Nicht-Buchstaben-Zeichen befüllen
		for (int i = 0; i < alphabet.length(); i++)
			galgenRatenString =
				galgenRatenString.replace(alphabet.charAt(i), '_');
	} catch (NullPointerException npe) {
		galgenRatenString = "";
	}

	return galgenRatenString;
}
/**
 * erstellt GalgenRatenString mit Buchstaben
 */
public float fillGalgenRaten(StringBuffer dest, char key) {

	float fillRate = 0f;
	int filledIn = 0;

	char[] charArray = galgenRatenString.toCharArray();
	for (int i = 0; i < charArray.length; i++) {
		char buffer = qTask.getAnswer().charAt(i);
		if (charArray[i] == '_') {
			if (buffer == key) {
				charArray[i] = key;
				filledIn++;
			} else {
				char upperKey = Character.toUpperCase(key);
				if (buffer == upperKey) {
					charArray[i] = upperKey;
					filledIn++;
				}
			}
		}
	}

	galgenRatenString = String.valueOf(charArray);
	dest.append(charArray);

	// alle Zeichen sind momentan  dabei mitgerechnet
	fillRate = (float) filledIn / charArray.length;
	return fillRate;
}

	/**
	 * erstellt Quiz-Strings
	 */
	public String[] initQuiz() {

		Vector qNumbers = new Vector(JQSettings.QUIZCHOICES);
		String qStringBuffer, qStringBest="";
		int qTaskLength = qTask.getAnswer().length();
		String qTaskAnswer = qTask.getAnswer();
		final byte IS_OTHER=0, IS_SUBSTANTIVE=1,IS_VERB=2;
		byte qTaskStatus=IS_OTHER;
		
		if ( isSubstantive(qTaskAnswer) ) qTaskStatus = IS_SUBSTANTIVE;
		else if ( isVerb(qTaskAnswer) ) qTaskStatus = IS_VERB;

		// Quiz-Antworten zufällig belegen
		for (int i = 0; i < JQSettings.QUIZCHOICES; i++) {
			int j = 0, harakiri = 0;
			int qNumberBuffer = 0, qNumberBest = 0;
			float proportionBest = 10e7f;

			while (j < JQSettings.SEARCH_RUNS) {

				byte qStringBufferStatus = IS_OTHER;
				qStringBuffer = getRandomQuizItem().getAnswer();
				qNumberBuffer = qNumber;
				// vielleicht besser mit StringBuffer??

				// QuizItemNummer darf nicht der richtigen Lösung entsprechen 
				// und nicht doppelt vorkommen  
				if ((qNumberBuffer == currentItem) || (qNumbers.contains(new Integer(qNumberBuffer)))) {
					harakiri ++;
					if (harakiri < 100000) 	continue; // Mindestbedingung muss erfüllt sein
				}

				j++;

				// ab hier Tests
				// 1. auf beste Längenproportionierung
				float proportion = (qStringBuffer.length() - qTaskLength);
				proportion = proportion * proportion;
				// Quadrierung auch wegen +/- Ausgleich
				proportion = (proportion +2) * 50;
				// höhere Abhängigkeit erzeugen

				// Test auf Verb / Substativ
				if ( isSubstantive (qStringBuffer.toString()) ) qStringBufferStatus = IS_SUBSTANTIVE;
 				if ( isVerb (qStringBuffer.toString()) ) qStringBufferStatus = IS_VERB;
 				if (qStringBufferStatus == qTaskStatus) proportion = proportion / 100;

				// kleinere proportion bedeutet bessere Lösung
				if (proportion < proportionBest) {
					proportionBest = proportion;
					qNumberBest = qNumberBuffer;
					qStringBest = qStringBuffer;
				}
			}

			qNumbers.addElement(new Integer(qNumberBest));
			
			qStrings[i] = qStringBest;

			if (JQuiz.DEBUG == true)
				qStrings[i] = qStringBest +" -> Proportion: " + proportionBest;
		}

		// Grossbuchstabentest: wenn 1. oder 2. Wort in Lösung gross beginnt
		// soll im Quiz-String auch eines der beiden ersten Worte groß beginnen

		// Aufgabe an zufaelliger Position untermischen
		qNumberRight = rand.nextInt(JQSettings.QUIZCHOICES);
		qStrings[qNumberRight] = qTask.getAnswer();
		return qStrings;
	}

	private boolean isSubstantive (String s) {
		if ( Character.isUpperCase(s.charAt(0)) )
			return true;
		else return false;
	}

	private boolean isVerb (String s) {
		if ( (s.indexOf("en ") != -1) || (s.endsWith("en")) ) 
			return true;
		else return false;
	}

	/**
	 * sendet die Position der richtigen Lösung im Quiz zurück
	 * @return 0-5 (0 = 1. Eintrag) 
	 */
	public int getRightQuizNumber() {
		
		return qNumberRight;
	}


 /*   public String getStatusString() {
	return ("Frage " + currentItem + " von " +
		quizItemVector.size());
    }*/
}

class QuizItem {
    private String question = null;
    private String answer = null;
    private String comment = null;

    /** Konstruiert ein Quiz-Element aus einer durch '\t (Tab)'
     *  unterteilten Eingabezeile.
     *  Keine Fehlerkontrolle.
     * @param Zeile aus der QuizDatei als String
     */
	public QuizItem (String s) {
		StringTokenizer t = new StringTokenizer(s,"\t\n");
		question = t.nextToken();
		answer = t.nextToken();
		try { comment = t.nextToken();
		} catch (NoSuchElementException n) {}
    }
    
	/** @return answerString of QuizItem */
	public String getAnswer() {
		return answer;
	}

	/** @return questionString of QuizItem */
	public String getQuestion() {
		return question;
	}

	/** @return Comment of QuizItem or null if there is no comment*/
	public String getComment() {
		if (comment == null) return ""; 
		else return comment;
	}
}

