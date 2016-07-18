package ev3steuerung.rezeptabarbeitung;

public class Flag {

    public static final boolean PARALLEL = true;
    public static final boolean SEQENZIELL = false;
    public static final int TOUCHWAITTIMEOUT = 10000;


    public enum DevicePort
    {
        MotorA, MotorB, MotorC, MotorD, SensorS1, SensorS2, SensorS3, SensorS4
    }

    public enum SpinMode
    {
        Turn_to_Angle, Spin_Till_Pressed, Spin_Till_Released, Spin_Till_Colour
    }

    public enum WaitMode
    {
        Wait_Time, Wait_for_Press, Wait_for_Release
    }

    public enum CheckFor
    {
        Motor, Sensor
    }

}
