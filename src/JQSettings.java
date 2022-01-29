import java.awt.*;


/*
 * Created on 26.12.2006
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */

/**
 * @author heida
 *
 * Hier stehen einstellbare Konstanten
 */
public class JQSettings {

	/** Anzahl der maximalen Fehlversuche für solve */
	static final int TRYMAX = 3;
	
	/** Anzahl von Quiz-Lösungen im Quiz-Tab	 */
	final static int QUIZCHOICES = 6;

	/** Farbe für Tafel-Hintergrund */
	static final Color BOARDGREEN = new Color (0.15f, 0.25f, 0.15f);

	// Konstanten aus der Engine
	
	/**	solveString min. length (z.B. 3 Zeichen) */
	static final int SSML = 3;
	
	/** Größe der LernRolle */
	static final int LEARN_ROLL_SIZE = 6; // war 10 141001
	
	/** Sollen die Vokabeln hin und zurück gefragt werden? */
	static final boolean ASK_BACKWARDS = true; 
	// 141004 lass es, die neg. Werte (und 2x "0") bringen bei Zugriff auf Vector uaq Schwierigkeiten

	/** bestimmt die Anzahl von Suchversuchen für besten Quiz-String */
	static final int SEARCH_RUNS = 10;
	
	/** wieviel mal soll Frage nach dem Stellen gesperrt sein */
	static final int TASKS_BLOCKED = LEARN_ROLL_SIZE / 5;
	
	static final int NO_ONE = 0;
	static final int TOMMI = 1;
	static final int BOBO = 2;
	static final int JO = 3;
	static final int RDKB = 4;
	static final int GITHUB = 5;

	/** der Benutzer, für den ich das Programm kompiliere*/
	static int user=GITHUB; // war NO_ONE; 141001

	String[] vocFileNames = new String [6];

	/** ACHTUNG Einschränkung bei Dateinamen der änderbaren Wörterbücher: Wegen 
	 * Datumsabgleich müssen momentan diese Wörterbücher mit "vocabulary" beginnen! 
	 * Nicht änderbare Wörterbücher dürfen nicht mit vocabulary beginnen! */
	String freeBook01="vocabulary_free_01.txt", freeBook02="vocabulary_free_02.txt";

	public JQSettings () {
		
		if (user  == TOMMI) {
			freeBook01="vocabulary_thomas_01.txt"; freeBook02="vocabulary_thomas_02.txt";
		} else if (user  == BOBO) {
			freeBook01="vocabulary_bobo_01.txt"; freeBook02="vocabulary_bobo_02.txt";
		} else if (user  == JO) {
			freeBook01="Jo001.txt"; freeBook02="vocabulary_01.txt";
		} else if (user  == RDKB) {
			freeBook01="vocabulary_free_01.txt"; freeBook02="vocabulary_free_02.txt";
		} else if (user  == GITHUB) {
			freeBook01="vocabulary_free_01.txt"; freeBook02="vocabulary_free_02.txt";
		} else if (user  == NO_ONE) {
			freeBook01="vocabulary_01.txt"; freeBook02="vocabulary_02.txt";
		}

		vocFileNames = new String [] {
			"wbeng-ger.txt",
//			"wed_rdkb.txt",
			"wed_couplehood.txt",
			"wed_heike.txt",
			"irregularVerbs.txt",
			freeBook01, freeBook02
		};
	}
	
	public String getVocFilename (int i) {
		return vocFileNames[i];
	}
}
