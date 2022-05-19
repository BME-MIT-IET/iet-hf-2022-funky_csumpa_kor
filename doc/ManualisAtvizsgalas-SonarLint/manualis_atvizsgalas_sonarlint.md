# Manuális átvizsgálás, SonarLint

A projektet manuálisan átvizsgáltuk SonarLint statikus analízis eszköz futtatása mellet. A jelzett hibákat melyek főleg fölöslegesen egymásba ágyazott if-ek,
nem használt importok, illetve kritikusnak számító kognitív komplexitások-ból álltak megvizsgáltuk és ahol szükséges volt javítottuk. A nem jelzett hibákat, melyek főleg
olvashatósági problémák, nem használt függvények voltak szintén javítottuk. Az Issue-n Marton Balázs és Pekár Patrik dolgozott.
 
Ezek eredményeképpen egy olvashatóbb, átláthatóbb kódot kaptunk.

A tanulság, hogy a projekt igényesen van megírva, jól struktúrált, de fejlesztés alatt áll és elég friss technológiát használ. Nem találtunk/jelzett sok hibát a statikus analízis eszköz, a jelzésekből is jópár annak köszönhető, hogy compose-ban folyik a fejlesztés, melyben a megjelenés leírásához sok elágazás szükséges, pl: megjelenő ablak is if-el jelenítődik meg.
