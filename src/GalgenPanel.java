import java.awt.*;
import java.util.Random;

import javax.swing.JPanel;
import javax.swing.*;

/*
 * Created on 14.12.2006
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */

/**
 * @author heida
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class GalgenPanel extends JPanel implements Runnable {
	private int galgenCounter = 0; // kann je nach Figurenstand Werte von 0-11 annehmen
	private int ampelStatus = 0; // soviel Ampeln sind auszusetzen
	private int points[] = {0,0,0,0}; //veränderliche Koord. am Maennchen
	
	private Thread wackelArm = null;

	/**
	 * 
	 */
	public GalgenPanel() {
		super();
		setBorder( BorderFactory.createLoweredBevelBorder () );
	}

	public Insets getInsets() {
		return new Insets(0, 0, 0, 0);
	}

	/**
	 * @param increment - Wert der zum Galgen hinzugezeichnet werden soll <=1
	 * @ return status 1-3 (soviel Ampeln aussetzen)
	 */
	public int showMoreOfGalgen (int increment) {
		
		if (galgenCounter/4 != (galgenCounter+increment)/4)
			ampelStatus = 1;
		else ampelStatus = 0;
		
		galgenCounter += increment;
		repaint();

		if (galgenCounter > 10) {
			galgenCounter = 11;
			ampelStatus = JQSettings.TRYMAX;
			startWackelArm();
		} 

		return ampelStatus;
	}
	
	/**
	* @param increment - Wert der zum Galgen hinzugezeichnet werden soll <=1
	*/
	public void showEmptyGalgen () {
		
		galgenCounter = 0;
		ampelStatus = 0;

		if (wackelArm != null) wackelArm.interrupt();
		
		repaint();	
	}


	public void paintComponent (Graphics g) {

		super.paintComponent(g);
		
		g.setColor(JQSettings.BOARDGREEN);
		g.fillRect(0, 0, 190, 250);

		Graphics2D g2 = (Graphics2D) g;
		// g2.setColor(Color.WHITE);
		float f = (11f - (float) galgenCounter) / 11;
		g2.setColor(new Color (1f, f, f));
		g2.setStroke(new BasicStroke(2.0f)); // line thickness

		// für Thread wackelarm, dadurch wird bei Fensteraktivierung bei laufendem Thread
		// die Bewegung wieder bei Mittelstellung begonnen, sonst ist ein Schatten sichtbar (Fehler) 
		for (int i = 0; i<4; i++) points[i] =0;

		switch (galgenCounter) {
			case 11 :
				g2.drawLine(130, 140, 145, 155); // Männle Bein re.
			case 10 :
				g2.drawLine(130, 140, 115, 155);
			case 9 :
				g2.drawLine(130, 120, 145, 115); // Arm re.
			case 8 :
				g2.drawLine(130, 120, 115, 115);
			case 7 :
				g2.drawLine(130, 110, 130, 140); // body
			case 6 :
				g2.drawOval(120, 80, 20, 30);

			case 5 :
				g2.setStroke(new BasicStroke(1.0f));
				g2.drawLine(130, 45, 130, 80); // Seil

			case 4 :
				g2.setStroke(new BasicStroke(3.0f));
				g2.drawLine(51, 79, 99, 51); // Galgen
			case 3 :
				g2.drawLine(50, 50, 150, 50);
			case 2 :
				g2.drawLine(50, 50, 50, 208);
			case 1 :
				g2.drawArc(-100, 200, 400, 600, 70, 45);
			default :
		}
	}
	
	void startWackelArm () {
		if ((wackelArm == null) && (galgenCounter == 11)) {
			wackelArm = new Thread (this);
			wackelArm.start();
		} else {
			try {
				wackelArm.interrupt();
				wackelArm = null;
			} catch (Exception ex) { }
		}
	}

	public void stopWackelArm () {
		if (wackelArm != null) {
			try {
				wackelArm.interrupt();
				wackelArm = null;
			} catch (Exception ex) { }
		}
	}

	public void run() {
		
		Graphics glocal = getGraphics();
		Graphics2D g2local = (Graphics2D) glocal;
		byte oppositeSign;
		Random randomNumber = new Random(System.currentTimeMillis());

		g2local.setStroke(new BasicStroke(2.0f)); // line thickness
		for (int i = 0; i<4; i++) points[i] =0;
		
		while ( wackelArm != null ) {

			// altes Männchen löschen
			g2local.setColor(JQSettings.BOARDGREEN);

			g2local.drawLine(130, 120, 145, 115-points[0]); // Arm re
			g2local.drawLine(130, 120, 115, 115-points[1]); // Arm li
			g2local.drawLine(130, 140, 145 + points[2]/2, 155-points[2]); // Männle Bein re ...
			g2local.drawLine(130, 140, 115 -  points[3]/2, 155-points[3]); // Bein li
			
			for (int i=0; i<4; i++) {
				if (randomNumber.nextInt(5) > 2) {
					if (points[i] >0) oppositeSign = -1;
						else oppositeSign = 1;
					points[i] = randomNumber.nextInt(12) * oppositeSign;
				}
			}
			
			// neu zeichnen
			g2local.setColor(Color.RED);
			
			g2local.drawLine(130, 120, 145, 115-points[0]); // Arm re
			g2local.drawLine(130, 120, 115, 115-points[1]); // Arm li
			g2local.drawLine(130, 140, 145 + points[2]/2, 155-points[2]); // Männle Bein re ...
			g2local.drawLine(130, 140, 115 -  points[3]/2, 155-points[3]); // Bein li

			g2local.drawLine(130, 110, 130, 140); // body

			try {Thread.sleep (50 + randomNumber.nextInt(250));} catch (Exception ex) { }
			
		}
	}
}
