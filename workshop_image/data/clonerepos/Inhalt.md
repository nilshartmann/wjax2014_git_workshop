Einstieg
--------

Ziele:
 * Object-Database und Inhalt zeigen
  * Blob, Tree, Commit
 * cat-file -p
 * Umbenennen verstehen
 * git log kennenlernen
 
Ablauf:
 * Hallo.groovy und Welt.groovy unter src anlegen
 * Commit -> Zeigen der Objekte -> cat-file -p
 * Unterverzeichnis "de" anlegen und Datein verschieben
 * git add --all -> Rename im Status zeigen -> git commit
 * Übung: Mit "cat-file -p" überprüfen ob der Tree von HEAD^/src und HEAD/src/de gleich ist
 * Hallo.groovy und Welt.groovy zu HalloWelt.groovy zusammen legen
 * git add --all -> kein Rename im Status??? -> git commit
 * Englische Version anlegen mit längeren Dateien "Hello.groovy" "World.groovy"
 * Dateien zusammenlegen -> Rename sichtbar
  * git log --oneline HelloWorld.groovy
  * git log --oneline --follow HelloWorld.groovy
  * git log --oneline --follow --name-status HelloWorld.groovy
