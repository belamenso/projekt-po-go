# Go

## Zasady gry
* [turorial 1](https://www.youtube.com/watch?v=5PTXdR8hLlQ)
* [tutorial 2](https://www.youtube.com/watch?v=YPMog4LAmvg)

## Plan ataku
| Kto | Co | Stan |
|---|---|---|
| Bartosz | definicja podstawowych struktur danych (plansza, kamyki, stan gry) | W TRAKCIE |
| Bartosz | funkcje implementujące podstawową logikę gry | W TRAKCIE |

## Kierunek

*Bartosz*:
Chciałbym, żeby rdzeń aplikacji (logika gry, stan gry...) były w całości odizolowane od UI, serwera itd, po prostu niech
w środku będą czyste dane, czyli klasy z public final polami takimi jak plansza pionków, itd, bez żadnych metod robiących
modyfikacje in place. I statyczne metody w tych klasach biorące stan planszy, opis ruchu i zwracające kolejny stan planszy.
To są małe struktury danych, więc to nie będzie przeszkadzało, a łatwiej to testować, odseparować od wszystkiego, trudniej
zrobić błędy w zarządzaniu stanem jeśli nie masz stanu itd. Myślę, że jak tak zrobimy rdzeń kodu opisujący samą grę, to później
będzie prościej.

## Myślenie na pryszłość
Jakie decyzje projektowe podjąć teraz, żeby później łatwo było dodawać funkcjonalność i żeby to się ładnie zeszło?

Rzeczy, które potencjalnie możemy dodać, ale później i warto się na nie przygotować:
* gra na planszach o różnych rozmiarach (wtedy wchodząc na serwer gry widać by było pokoje z zaznaczonym typem rozgrywki)
* szachy (coraz bardziej wątpliwe w tym projekcie :)
* gra offline (dwóch graczy przed jednym komputerem)
* gra z komputerem (niemożliwe do zrobienia w go, trzeba by było coś ukraść z internetu dla szachów)
