# Masterarbeit
Master thesis repository

Anweisungen zur Ausführung des Programms:

!!! Voraussetzungen:

- CouchDB muss installiert werden und auf dem Port 5984 laufen (wenn der Port anders ist - müssen der java-Mapper und das Modeling Tool angepasst werden).
- In CouchDB müssen die Datenbanken: situaions, situationstemplates, things, thingtypes existieren.
- NodeRed muss installiert werden und auf dem Port 1880 laufen (wenn der Port anders ist - muss der java-Mapper angepasst werden)
- mosquitto muss installiert werden und auf dem Port 1883 laufen

Modeling Tool Installation:

I.   Kopiere den Ordner Modelling Tool...  und ...war in webapps verzeichnis von tomcat
II.  Starte Tomcat
III. Öffne Modeling Tool in Browser: http://localhost:8080/SitTempModelingTool
IV.  Modelliere Templates und speichere sie in der Datenbank (CouchDB auf localhost:5984 muss laufen und muss oben genannte collections haben).

Mapping-Ablauf:

1. CouchDB starten.

2. NodeRed starten. Alle existierenden Flows müssen gelöscht werden damit neue Flows erstellt werden können ('Deploy' nicht vergessen).

3. mosquitto starten.

4. Ausführbare jar-Datei (28) starten
	 -> Auswahl des Situationstemplates
	 -> Auswahl der Things
	 -> Im NodeRed-Browsertab erscheint das Fenster: "The flows on the server have been updated."
	 -> 'Review Changes' drücken
	 -> 'Merge' drücken.
	 -> Flow wird erstellt und kann via 'Deploy' gestartet werden. 

5. Anpassung von node.js Server durchführen:
	- Code für Dummy-Sensordaten anpassen.
	- Die Topics für Sensordaten-publish entsprechend gewählter Things anpassen.
	- Die Topics für Situationobjekt-subscribe entsprechend der Situationen anpassen.

6. Die Situationsobjekte werden erstellt und in die Datenbank geladen.


