# JQuiz
Java Vokabeltrainer

Mit dem Programm könnt ihr eingebaute und auch eigene Vokabeldateien trainieren.
Ihr könnt dabei entweder
- Text eingeben als Lösung oder
- Galgenraten oder
- aus Quiz-Lösungen die Lösung heraussuchen.

Dabei versucht das Programm, wirklich die Quizlösungen so ähnlich zu gestalten, wie die Lösung, dass z.B. bei kurzer Aufgabe, nicht eine lange Lösung sofort als falsch auffällt. Durch eine Lernrolle (kleinerer Kartenstapel auf der Hand) werden sozusagen immer eine kleine Menge von Vokabeln intensiv geübt, ehe man fortschreitet. Das ist alles natürlich im Code einstellbar, wenn ihr da andere Vorlieben habt.

Wenn ihr dem Programm erlaubt, eine ini Datei zu schreiben, werden verschiedene Werte zwischen den Sessions gemerkt (erledigte Vokabeln, …) und ihr könnt täglich üben . Die Datei wird im Profilverzeichnis angelegt mit eigenem Verzeichnis (C:\users\JQuiz\…) ist lesbar und kann dort auch wieder gelöscht werden bei Bedarf. Die eigenen Wörterbücher sollte man auch da speichern, dass sie das Programm findet. Mit der ini im Programmpfad selbst (also dort, wo ihr das Programm hin kopiert habt) geht das Programm aber auch (wenn ihr nicht euer Profil voll müllen wollt).

Schaut euch mal an (auch gern in den source Dateien), wie das Galgen-Männchen am Ende des Galgenratens zappelt. Es sind zufällige Bewegungen, die aber miteinander korrespondieren (Arme & Beine) um ein echt leidendes Galgenmännchen darzustellen. Ich glaube, man merkt es an, dass mir das beim coden "Mords-Gaudi" gemacht hat. 😈

Es ist aber von der Programmier-Perspektive für mich fast noch interessanter zu sehen, wie das Programm sowohl mit 100 Vokabeln umgehen kann als auch mit 100.000! Und es ist fast kein zeitlicher Unterschied merklich obwohl JAVA bei weitem keine schnelle Hoch-Sprache ist. Probiert es gern aus! Im wb-eng-ger sind ca. 30.ooo Vokabeln drin. Eigentlich wollte ich immer mal Parser schreiben, dass man z.B. Verben hieraus o.ä. lernen kann, weil ja 30k Zeilen zu kategoriesieren voll öde ist. Bin hier aber nie wirklich über die Idee hinausgekommen... 

 

![grafik](https://user-images.githubusercontent.com/56628625/151678564-e96144b3-57c5-47b1-b3ec-176a5b57236f.png)

![grafik](https://user-images.githubusercontent.com/56628625/151678835-d30e7b9b-8646-4a2b-8b12-998340e334a4.png)
