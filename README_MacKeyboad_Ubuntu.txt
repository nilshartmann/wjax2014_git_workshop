So funktionieren die Sonderzeichen wie @ etc:

sudo dpkg-reconfigure keyboard-configuration

In der Konfiguration dann auswählen:
Tastaturmodell:
	MacBook/MacBook Pro
Herkunftsland für die Tastatur:
	Deutsch
Tastaturbelegung:
	Deutsch - Deutsch (Macintosh, ohne Akzenttasten)
Taste, die als AltGr fungieren soll:
	Alt rechts (AltGr)
Compose Taste:
	Keine Compose-Taste
Strg+Alt+Zurück verwenden, um den X-Server zu beenden?
	Nein

Danach ausführen:
	sudo udevadm trigger --subsystem-match=input --action=change

Danach kann man dann die rechte Alt-Taste wie die Mac Command-Taste
verwenden, d.h. das @-Zeichen bekommt man z.B. mit Alt (Rechts)-L,
das Pipe-Symbol mit Alt (Rechts)-7

