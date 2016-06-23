package ev3steuerung;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Die Test-Klasse EV3_BrickTest.
 *
 * @author  Christoph Schmidt
 * @version 23.06.2016
 */
public class SingletonTest
{
    /**
     * Konstruktor fuer die Test-Klasse EV3_BrickTest
     */
    public SingletonTest() {
        
    }

    /**
     *  Setzt das Testgerüst fuer den Test.
     *
     * Wird vor jeder Testfall-Methode aufgerufen.
     */
    @Before
    public void setUp() {
        
    }

    /**
     * Gibt das Testgerüst wieder frei.
     *
     * Wird nach jeder Testfall-Methode aufgerufen.
     */
    @After
    public void tearDown() {
        
    }

    @Test
    public void testSingleton() {
        EV3_Brick brick1 = EV3_Brick.getInstance();
        EV3_Brick brick2 = EV3_Brick.getInstance();
        
        assertNotNull(brick1);
        assertEquals(brick1,brick2);
    }
}