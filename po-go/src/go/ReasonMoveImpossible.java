package go;

public enum ReasonMoveImpossible {
    /**
     * Podana pozycja nie mieści się na planszy
     */
    PositionOutOfBounds,
    /**
     * Podana pozycja jest już zajęta
     */
    PositionOccupied,
    /**
     * Ruch na podaną pozycję natychmiast zakończył by się przejęciem przez przeciwnika
     */
    SuicidalMove,
    /**
     * Gracz, który zgłosił ruch nie miał teraz prawa tego zrobić
     */
    NotYourTurn,
    /**
     * Wykonując ten ruch, stan gry sprzed dwóch ruchów powtórzyłby się w całości
     */
    ReturnToImmediatePreviousState,
    /**
     * Jeden z graczy zerwał połączenie z serwerem zanim gra się zakończyła
     */
    GameInterrupted
}
