package ev3steuerung;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Paths;
import java.util.LinkedList;


import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * Support class to handle MQTT. Designed for use in package ev3steuerung
 * @author Martin Schöne */
 
public class MqttHelper implements MqttCallback, Runnable {
    private MqttAsyncClient mqtt;
    private boolean error = false;
    private boolean debugMode = true;
    private String serverURI;
    private String deviceID;
    private MqttBrick mqttBrick;
    private LinkedList<String> failedDebugMessages;
    private String ip;
    private boolean thread = false; // is a thread running executing the run method
    
    /**
     * Constructor of the Mqtt-Helper Class
     * 
     * @param mqttBrick - The MqttBrick instance
     * @param deviceID - The DEVICE_ID from the properties file
     * @param serverURI - the MQTTSERV_IP from the properties file
     * @param ip - the IP from the properties file
     * @see MqttBrick.startMqtt()
     * @return MqttHelper - Connected Mqtt-Interface */
    protected MqttHelper(MqttBrick mqttBrick, String deviceID, String serverURI, String ip) {
        this.deviceID            =   deviceID;
        this.mqttBrick           =   mqttBrick;
        this.serverURI           =   serverURI;
        this.failedDebugMessages =   new LinkedList<String>();
        this.ip                  =   ip;
        connect();
    }
    private synchronized boolean connect() {
        try {
            mqtt=new MqttAsyncClient(serverURI, MqttAsyncClient.generateClientId());
            mqtt.setCallback(this);
            IMqttToken conToken = mqtt.connect();
            conToken.waitForCompletion();
            IMqttToken subToken = mqtt.subscribe(deviceID,0);  //Qos 0?
            subToken.waitForCompletion();
            subToken=mqtt.subscribe("vwp/"+deviceID,0);
            subToken.waitForCompletion();
            error=false;
            if(resend())
                return true;//publishToDeviceID(mqttBrick.getState().getName());
            return false;
        } catch (MqttException mqtte) {
            error=true;
            return false;
        }
    }
    private synchronized boolean resend() {
        while(failedDebugMessages.size() > 0) {
            String current=failedDebugMessages.peekLast();
            if(publishToMES(current))
                failedDebugMessages.remove();
            else 
                return false;
        }
        return true;
    }
    private synchronized void startFixer() {
        if(thread)
            return;
        else
            new Thread(this).start();
    }
    private synchronized boolean publishToDeviceID(String message) {
        
    	if(error&&!connect()) {
            return false;
        }
        try {
            mqtt.publish(deviceID, new MqttMessage(message.getBytes()));
            return true;
        } catch (MqttException e) {
            error=true;
            failedDebugMessages.add(message);
            startFixer();
            return false;
        }
    }
    private synchronized boolean publishToMES(String message) {
        try {
            mqtt.publish("vwp/stiserver", new MqttMessage(message.getBytes()));
            return true;
        } catch (MqttException e) {
            error=true;
            return false;
        }
    }
    /**
     * Registers the device at the MES, as shown in Mr. Ringel's state diagram in transition 1.
     * 
     * @see Idle
     * @return True - If the register message could be sent successfully */
    protected synchronized boolean register() {
        return publishToMES(deviceID+":register:{\"ip\":\""+ip+"\",\"name\":\""+deviceID+"\",\"status\":\""+mqttBrick.getStateName()+"\"}");
    }
    /**
     * Requests a task from the MES, as shown in Mr. Ringel's state diagram in transition 3.
     * 
     * @see Idle
     * @return True - If the TaskRequest message could be sent successfully */
    protected synchronized boolean requestTask() {
        return publishToMES(deviceID+":TaskREQ");
    }
    /**
     * Indicates a task to the MES, as shown in Mr. Ringel's state diagram in transition 5 and PROC -> MAINT.
     * 
     * @see Proc
     * @param task  - String representation of the Recipe that was worked on
     * @param result - String representation of the result the work came out [done, terminated, abort]
     * @return True - If the TaskIndication message could be sent successfully */
    protected synchronized boolean indicateTask(String task, String result) {
        return publishToMES(deviceID+":TaskIND:"+task+":"+result);
    }
    /**
     * Indicates a state change to the MES.
     * 
     * @see Idle
     * @see Proc
     * @see Maint
     * @see ShuttingDown
     * @param message - String representation of the current State
     * @return True - If the StateIndication message could be sent successfully */
    protected synchronized boolean indicateState(String message) {
        return publishToMES(deviceID+":StateIND:State:->"+message);
    }
    /**
     * Reports the current state to the monitoring tool via topic DEVICE_ID
     * 
     * @see MqttHelper.publishToDeviceId(..) */
    protected synchronized void publishState() {
        publishToDeviceID(mqttBrick.getStateName());
    }
    /**
     * Sends the message as debug to the monitoring tool.
     * This message must be at least 2 spaced words.
     * 
     * @param message - Payload of Message to send for debugging purpose */
    public synchronized void debug(String message) {
        if(debugMode) 
            publishToDeviceID("debug "+message);
    }
    /**
     * Nothing really to see here. Just Mqtt-Edge-case-Handling.
     * Setting error flag.
     * 
     * @param cause - Throwable cause */
    @Override
    public synchronized void connectionLost(Throwable cause) {
        error=true;
        debug("mqtt connection lost");
        /* joke of the year: when mqtt stops working, we debug it via mqtt
         * still makes sense, as it will be sent later (hopefully)
         * topkek
         */
    }
    /**
     * Nothing really to see here.
     * 
     * @param token - no Description */
    @Override
    public synchronized void deliveryComplete(IMqttDeliveryToken token) {
        //nothing yeah
    }
    /**
     * Handles incoming messages for all topics,
     * specially forwards from topic vwp/DEVICE_ID to MqttBrick
     * 
     * @see MqttBrick.messageArrived(..)
     * @param topic - Topic on which it has been recieved
     * @param message - MqttMessage of incoming transaction
     * @throws Exception - If there was an error while forwarding messages */
    @Override
    public synchronized void messageArrived(String topic, MqttMessage message) throws Exception {
        if(("vwp/"+deviceID).equals(topic)) {
            // EV3STEUERUNG - MES COMMUNICATION
            mqttBrick.messageArrived(new String(message.getPayload()));
        } else {
            // GUI - EV3STEUERUNG COMMUNICATION
            String[] information = new String(message.getPayload()).split(" ");
            switch(information.length) {
            case 1:
                switch(information[0]) {
                    case "hello":
                        if (!(mqttBrick.getStateName().equals("TURNING_ON")))
                            publishState();
                        break;
                    case "SHUTTING_DOWN":
                    case "TURNING_ON":
                    case "IDLE":
                    case "PROC":
                    case "MAINT":
                    case "DOWN":
                        break;
                    default:
                        System.out.println("unrecognized message "+new String(message.getPayload()));
                    }
                break;
            case 2:
                switch(information[0]) {
                case "debug":
                    if("true".equals(information[1]) || "false".equals(information[1]))
                        debugMode = Boolean.parseBoolean(information[1]);
                    break;
                case "manual":
                    mqttBrick.manualFix();
                    break;
                case "emergency":
                    mqttBrick.emergencyShutdown();
                    break;
                default:
                    System.out.println("unrecognized message "+new String(message.getPayload()));
                }
                break;
            default:
                switch(information[0]) {
                    case "debug":
                        break;
                    default:
                        System.out.println("unrecognized message "+new String(message.getPayload()));
                }
            }
        }
    } 
    private synchronized void fix() {
        if(error) connect();
    }
    /**
     * Reports the shutdown to the MES and closes the MQTT connection.
     * 
     * @see MqttBrick.stopMqtt() */
    protected synchronized void close() {
        try {
            publishToMES(deviceID+":shutting down");
            mqtt.disconnect();
            mqtt.close();
        } catch (MqttException e) {
            e.printStackTrace();
            debug("MqttException while closing Mqtt");
            System.out.println("MqttException while closing Mqtt");
        }
        String[] directories=Paths.get("").toAbsolutePath().toFile().list(new FilenameFilter(){
            @Override
            public boolean accept(File dir, String name) {
                if(name.startsWith("paho")) return true;
                return false;
            } 
        });
        for(String directory:directories) {
            debug("removing directory "+directory);
            new File(directory+"/.lck").delete();
            new File(directory).delete();
        }
    }
    /**
     * Tries to fix the MQTT connection every 10s
     * 
     * @see Mqtt-Threads */
    public void run() {
        synchronized(this) {
            thread = true;
        }
        try {
            while(error) {
                fix();
                Thread.sleep(10000);
            } 
        } catch (InterruptedException e) {
        }
        synchronized(this) {
            thread = false;
        }
    }
}