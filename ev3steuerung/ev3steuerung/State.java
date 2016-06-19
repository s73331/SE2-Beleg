package ev3steuerung; 

/**
 * Tragen Sie hier eine Beschreibung des Interface State ein.
 * 
 * @author (Ihr Name) 
 * @version (eine Versionsnummer oder ein Datum)
 */

public interface State {
   
    String getName();
    void doAction();
    int getColor();
}
