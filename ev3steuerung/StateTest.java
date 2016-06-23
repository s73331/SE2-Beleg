package ev3steuerung;



import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Die Test-Klasse StateTest.
 *
 * @author  Christoph Schmidt
 * @version 26.06.2016
 */
public class StateTest
{
    /**
     * Konstruktor fuer die Test-Klasse StateTest
     */
    public StateTest()
    {
    }

    /**
     *  Setzt das Testgerüst fuer den Test.
     *
     * Wird vor jeder Testfall-Methode aufgerufen.
     */
    @Before
    public void setUp()
    {
    }

    /**
     * Gibt das Testgerüst wieder frei.
     *
     * Wird nach jeder Testfall-Methode aufgerufen.
     */
    @After
    public void tearDown()
    {
    }
    
    @Test
    public void turningOnTest() {
        
    }
    
    @Test
    public void idleTest() {
        
    }
    
    @Test
    public void procTest() {
        
    }
    
    @Test
    public void maintTest() {
        
    }
    
    @Test
    public void shuttingDownTest() {
        
    }
    
    @Test
    public void stateAttributesTest() {
        State s = new TurningOn();
        
        assertEquals(s.getName(),"TURNING_ON");
        assertEquals(s.getColor(),4); // Green Shining
        
        s = new Idle();
        
        assertEquals(s.getName(),"IDLE");
        assertEquals(s.getColor(),3); // Orange Flashing
        
        s = new Proc();
        
        assertEquals(s.getName(),"PROC");
        assertEquals(s.getColor(),1); // Green Flashing
        
        s = new Maint();
        
        assertEquals(s.getName(),"MAINT");
        assertEquals(s.getColor(),5); // Red Flashing
        
        s = new ShuttingDown();
        
        assertEquals(s.getName(),"DOWN");
        assertEquals(s.getColor(),6); // Orange Pulse
    }
    
}
