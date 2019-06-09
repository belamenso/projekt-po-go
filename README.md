# PoGo

Autorzy: Bartosz Białas, Rafał Kilar

## Uruchomienie
Klient - plik pogo-client/shade/pogo-client.jar
Serwer - plik pogo-client/shade/pogo-server.jar - uruchomiany z linii poleceń

Serwer uruchamia się na porcie 33107 i wypisuje ip potrzebne do połączenia z nim.

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

----

## Zasady gry
* [turorial 1](https://www.youtube.com/watch?v=5PTXdR8hLlQ)
* [tutorial 2](https://www.youtube.com/watch?v=YPMog4LAmvg)
* [interaktywny tutorial, dobre źródło testów systemu](https://online-go.com/learn-to-play-go/)

zasady:
* czarne zaczynają
* plansza 19x19, 13x13, 9x9 **przecięć**, a nie pól
* jeden samotny kamyk (stone) ma liberties - 4 przecięcia (intersection) wokół niego, chyba że jest tam róg/brzeg planszy
* grupa kamyków w jednym kolorze ma liberties - wszystkie puste przecięcia w 4 kierunkach (sąsiedzi bez kamyków innego koloru)
* gracze umieszczają tylko 1 kamyk na raz, ale mogą odmówić ruchu
* gdy dwaj gracze bezpośrednio po sobie odmówią ruchu, gra się kończy (tylko wtedy)
* nie wolno jest postawić kamienia, który natychmiast zostanie przejęty przez oponenta
* wolno postawić kamień tylko na pustym przecięciu
* **(!)** żadnemu graczowi nie wolno powtórzyć stanu *całej planszy* z jego bezpośrednio poprzedniego ruchu, gdyby nie było
tego w zasadach, można by było robić tą samą sekwencję w nieskończoność
* gdy zamknąłeś jakieś kamienie przeciwnika w kordon (w środku nie ma żadnych liberties i są same kamienie przeciwnika) to
zabierasz je z planszy do swojego naczynka - captured stones
* na końcu gry planszę dzieli się na spójne kawałki, takie będąc na pustym polu w takim kawałku, dowolna ścieżka po pustych
polach prowadzi cię do kamyka tego samego koloru (np. czarnego) albo brzegu planszy. Liczba pustych pól w takim terytorium to
punkty jakie sumują się do wyniku gracza (czarnego)
* ilość twoich kamyków, które oponent przejął jest odejmowana od twojego wyniku
* (???) gdy gra się kończy, dead stones are immediately removed
* zasady zabraniające pewnych specyficznych ułożeń, bo one mogą prowadzić do ruchów które przeplatają
się w nieskończoność
* pod koniec białe dostają jakiś bonus, ponieważ nie zaczynały pierwsze

## Inne
* [online-go.com](/online-go.com) to lepsza wersja tego, co budujemy, warto brać od nich pomysły
