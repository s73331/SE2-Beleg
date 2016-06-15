package ev3steuerung; 

/**
 * Tragen Sie hier eine Beschreibung des Interface State ein.
 * 
 * @author (Ihr Name) 
 * @version (eine Versionsnummer oder ein Datum)
 */

public interface State {
   
    /**
     * Ein Beispiel eines Methodenkops - ersetzen Sie diesen Kommentar mit Ihrem eigenen
     * 
     * @param  y    ein Beispielparameter für eine Methode
     * @return        das Ergebnis von beispMethode
     */
    String getName();
    
    void doAction();
    
    int getColor();
}
