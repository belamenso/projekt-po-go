# Go

## Zasady gry
* [turorial 1](https://www.youtube.com/watch?v=5PTXdR8hLlQ)
* [tutorial 2](https://www.youtube.com/watch?v=YPMog4LAmvg)


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
* (???) jeszcze dziwniejsze zasady zabraniające pewnych specyficznych ułożeń, bo one mogą prowadzić do ruchów które przeplatają
się w nieskończoność
* (???) pod koniec białe dostają jakiś bonus, ponieważ nie zaczynały pierwsze

## Plan ataku
| Kto | Co | Stan |
|---|---|---|
| Bartosz | definicja podstawowych struktur danych (plansza, kamyki, stan gry) | W TRAKCIE |
| Bartosz | funkcje implementujące podstawową logikę gry | W TRAKCIE |

## Kierunek

*Bartosz 26.04 18:00*:

Chciałbym, żeby rdzeń aplikacji (logika gry, stan gry...) były w całości odizolowane od UI, serwera itd, po prostu niech
w środku będą czyste dane, czyli klasy z public final polami takimi jak plansza pionków, itd, bez żadnych metod robiących
modyfikacje in place. I statyczne metody w tych klasach biorące stan planszy, opis ruchu i zwracające kolejny stan planszy.
To są małe struktury danych, więc to nie będzie przeszkadzało, a łatwiej to testować, odseparować od wszystkiego, trudniej
zrobić błędy w zarządzaniu stanem jeśli nie masz stanu itd. Myślę, że jak tak zrobimy rdzeń kodu opisujący samą grę, to później
będzie prościej.

*Bartosz 26.04 22:50*:

Z uwagi na to, że logika gry jest napisana funkcyjnie i robienie takich rzeczy teraz jest bardzo proste,
proponuję dodać sztuczki ze stanem aplikacji, to jest
* cofanie ruchów (w multiplayerze może cofanie do 5 sekund po wykonaniu?)
* w GUI można dodać podgląd ruchu, który chce się zrobić (w sensie, np. kamienie, które znikają jeśli wykonasz ruch
w miejsce gdzie masz krusor myszy są półprzezroczyste)
* podgląd historii rozgrywki (możesz sobie sliderem przesunąć i pokaże ci jak gra się rozwijała od początku do końca) <- **to będzie super fajne**

Do pokojów na serwerze powinno się też móc wchodzić jako spektator, może nawet kiedy ciągle oczekuje się na 2giego gracza.

## Myślenie na pryszłość
Jakie decyzje projektowe podjąć teraz, żeby później łatwo było dodawać funkcjonalność i żeby to się ładnie zeszło?

Rzeczy, które potencjalnie możemy dodać, ale później i warto się na nie przygotować:
* gra na planszach o różnych rozmiarach (wtedy wchodząc na serwer gry widać by było pokoje z zaznaczonym typem rozgrywki)
* szachy (coraz bardziej wątpliwe w tym projekcie :)
* gra offline (dwóch graczy przed jednym komputerem)
* gra z komputerem (niemożliwe do zrobienia w go, trzeba by było coś ukraść z internetu dla szachów)
