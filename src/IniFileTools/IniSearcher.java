/*
 * Created on 16.08.2007
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package IniFileTools;
import java.io.File;
import javax.swing.JOptionPane;
import java.awt.Component;

/**
 * ermittelt die Position und den user-spezifischen ini-Dateinamen,
 * möglich sind: im Home-Verzeichnis des Nutzers, im aktuellen Arbeitsverzeichnis, 
 * keine ini verwenden
 * @author heida
 * 
 */

public class IniSearcher {

	private static final String INI_FILE = "jquiz.ini";
	
	/** Option: soll ini erstellt werden? Nur wenn keine ini gefunden, wird nach Rückfrage false gesetzt*/
	private boolean workingWithIni = true;

	private String iniFileName = "";
	private String iniDirName = "";
	
	/**
	 * Konstruktor ermittelt und belegt Vartiablen 
	 */
	public IniSearcher(Component parentWindow) {

		// Variablen im Konstruktor
		String workDirName = System.getProperty("user.dir");
		String homeDirName = System.getProperty("user.home") + File.separator + "JQuiz";
		String homeFileName = System.getProperty("user.home") + File.separator + "JQuiz" + File.separator + INI_FILE;
		
		File fileInHome = new File (homeFileName);
		
		if ( ! fileInHome.exists()) {
			File fileInDir = new File (workDirName + File.separator + INI_FILE);
			if ( ! fileInDir.exists()) {
				int ask = JOptionPane.showConfirmDialog( parentWindow,
					"Initialisierungs-Datei nicht gefunden.\n" + "Datei erzeugen?",
					"File not found", JOptionPane.YES_NO_OPTION);
				if (ask == 0) { // ini neu erstellen im Nutzer-Home
					File homeDirectory = new File (homeDirName);
					homeDirectory.mkdirs(); // macht nur das Verzeichnis für die ini (JQuiz)
					iniFileName = homeFileName;
					iniDirName = homeDirName;
					return;
				} else {
					workingWithIni = false;
					return;
				} 
			}
			iniFileName = workDirName + File.separator + INI_FILE;
			iniDirName = workDirName;
			return;
		}
		iniFileName = homeFileName;
		iniDirName = homeDirName;
	}
	

	/**
	 * @return Verzeichnis + Name der ini-Datei; Startwert = ""
	 */
	public String getIniFileName() {
		return iniFileName;
	}

	/**
	 * @return false: keine ini verwenden, sonst true
	 */
	public boolean isWorkingWithIni() {
		return workingWithIni;
	}

	/**
	 * @return Verzeichnis der ini-Datei; Startwert = ""
	 */
	public String getIniDirName() {
		return iniDirName;
	}

}
