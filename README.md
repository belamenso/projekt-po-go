# PoGo

Autorzy: Bartosz Białas, Rafał Kilar

## Uruchomienie
Klient - plik `pogo-client/shade/pogo-client.jar`

Wymaga najnowszej wersjii Javy.

Serwer - plik `pogo-client/shade/pogo-server.jar` - uruchamiany z linii poleceń `java -jar pogo-server.jar <numer portu - domyślnie 33107>`

Serwer uruchamia się na podanym i wypisuje adres IP potrzebny do połączenia z nim. Zatrzymany przez wpisanie 'close'.

## Obsługa
* Łączenie z serwerem przez wypisane IP i port
* W lobby tworzenie nowych pokoi przez podanie nazwy i wybranie rozmiaru planszy
* Wchodzenie do pokoju przez podwójne klinięcie na wybrany pokój
* Możliwość wyboru preferowanego koloru - używany, gdy pokój jest pusty
* Spectatowanie przez wybranie pokoju i kliknięcie 'Spectate'
* Forkowanie w czasie spectatowania przez podanie nazwy forka i klinięcie Fork lub Enter w polu
* Obsługa historii przez slider pod planszą, przyciski lub klikanie w wiadomości
* Klawisz M - włączanie i wyłączanie

## Funkcjonalności
* Obsługa wielu pokojów w lobby
* Możliwość gry w Go w pokoju zgodnie z zasadami gry
* Podgląd kamieni usuwanych z planszy przed ruchem
* Podgląd zajętych terytoriów pod koniec gry
* Prosty chat między graczami
* Skrypt z rozgrywki z czasami zdarzeń
* Obsługa plansz różnej wielkości
* Obsługa wyboru koloru
* Podgląd historii rozgrywki
* Forkowanie rozgrywki
* Spectatowanie gier
* Usuwanie martwych kamienii
* Dźwięki