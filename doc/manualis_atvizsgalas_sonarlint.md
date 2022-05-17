# Manuális átvizsgálás, SonarLint

A projekt egy fejlesztés alatt álló compose android alkalmazás. A fejlesztés amikor megkezdődött a compose még nagyon kezdetleges állapotban állt (még most is), ezért
találhatók érdekes megoldások a kódban, ami elsőre fucsának tűnt például felugró ablakok egy booleant ellenörző if-ekkel vannak megoldva. Utána jártam, elvileg így kell megoldani.
A SonarLint is sok critikusnak számító kognitív komplexitás hibát dobott, amik elkerülhetetlenek a compose használata miatt, de jelzett hibás egymásba ágyazott if-eket is.
Mivel a projekt fejlesztés alatt áll manuális átvizsgálás során főleg bent maradt, nem használt függvényeket kellet kiszedni, illetve az olvashatóságot lehetett javítani.

A tanulság, hogy érdemes egy új technológia kijövetele után várni egy picit amíg a technológia kijön alfa/béta verzióból és utána elkezdeni egy komolyabb projektet.
