# PoGo

Autorzy: Bartosz Białas, Rafał Kilar

## Możliwe kierunki
* usuwanie martwych kamieni przed zliczeniem punktów
* lepsze GUI (eg. wyświetlanie liczby pojmanych kamieni, estetyczny resize)
* dodanie obsługi plansz różnej wielkości do GUI
* dodanie możliwości oglądania rozgrywek, bez uczestniczenia w nich
* lepszy sposób łączenia się z serwerem (eg. stały port)
* dodanie importu i eksportu plików SGF - integracja z innymi programami do gry w go
* przechowywanie zakończonych rozgrywek (baza danych)
* dodanie podglądu historii rozgrywki do GUI + animacja gry
* bardziej zorganizowany chat
* możliwość forkowania rozgrywki od dowolnego punktu
* gra offline (dwóch graczy przed jednym komputerem)
* dodanie dźwięku

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

## Uwagi odnośnie pierwszej wersji serwera
Klasa Server uruchamia na komputerze server na wolnym porcie (uruchamiana z lini poleceń). Czeka na klientów.
Klasa ServerClient przechowuje dane o kliencie - id, ip, port, strumien wyjsciowy, listener
Interfejs ServerListener przetwarza dane i evenety od clienta. Póki co implementują go dwie klasy:

LobbyListener - domyślny listener, obsluguje komendy:
* list - zwraca liste pokoi - trzeba to inaczej sformatowac, tak aby dalo sie to latwo odczytac po stronie klienta i wstawic do GUI
* create [name] - tworzy nowy pokoj o nazwie name, powinno jeszcze automatycznie przenosic klienta do pokoju
* join [name] - laczy klienta z pokojem o nazwie name - zmienia mu listenera
* remove [name] - usuwa pokoj o nazwie name, wysyla ja tylko RoomListener gdy wyjda z niego wszyscy gracze

RoomListener - obluguje pojedyncza gre, na chwile obecną obsługuje jedną komendę:
* quit - usuwa gracza z pokoju i przenosi do lobby

W kazdym innym przypadku wysyla otrzymana wiadomosc do wszystkich graczy.
W przyszłości będzie odpowiadał za przeprowadzenie gry i updatował graczy po otrzymaniu wiadomości o ruchach. Dobrze by było żeby w przyszłości była to klasa abstrakcyjna z której dziedziczyć bedą klasy pokoi dla odpowiednich trybów gry

Po stronie klienta:
Klasa Client laczy sie z serwerem i nasluchuje, wiadomosci i eventy przesyla do ClientListenera, póki co są dwa listenery:
* ClientLobbyListener - póki co nic nie robi oprócz zmiany listenera na ClientRoomListener gdy otrzyma connectedToRoom, w przyszłości ma współdziałać z GUI aby wysyłać komendy i wyswietlać listę pokoi by umożliwić tworzenie i łączenie się z pokojem
* ClientRoomListener - póki co jedyne co robi to wyświetla wiadomości i zmienia listenera na ClientLobbyListener gdy otrzyma exitedRoom, w przyszłości ma updatować GUI aby wyświetlaćzmiany na planszy

TODO ConnectListener - podstawowy listener, bedzie działał z GUI przy łaczeniu się z serwerem, gdy się połaczy zmieni listenera na ClientLobbyListener i zmieni odpowiednio GUI
Wszystkie wiadomosci wysyłane przez klienta są póki co czytane ze standardowego wejscia. W finalnym produkcie bedą wysyłane przez GUI.
W tym stanie występuje dużo śmieci w komunikacji, które były potrzebne do testowania, wszystko się posprząta, gdy zaczniemy wprowadzać gui i logikę gry.
Możesz przetestować serwer to w kilku oknach linii poleceń. W jednym uruchom server.Server, który wypisze IP i port. W pozostałych uruchom client.Client i przetestuj komendy (list, create, join, quit).
