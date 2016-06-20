package monitoringtool;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * Support class to handle MQTT. Designed for use in monitoringtool.Model.
 * @author martin
 *
 */
public class MqttHelper implements MqttCallback {
    private static final Logger logger=LogManager.getLogger();
    private MqttAsyncClient mqtt;
    private boolean error=false;
    private String deviceID;
    private MqttModel model;
    private PropertyHelper propertyHelper;
    private boolean status=false;                 //true: online, false: offine
    public MqttHelper(PropertyHelper propertyHelper, String deviceID, MqttModel model) {
        if(deviceID==null) throw new IllegalArgumentException("deviceID can not be null");
        if(propertyHelper==null) throw new IllegalArgumentException("propertyHelper can not be null");
        if(model==null) throw new IllegalArgumentException("model can not be null");
        this.deviceID=deviceID;
        this.model=model;
        this.propertyHelper=propertyHelper;
        connect();
    }
    private boolean connect() {
        try {
            mqtt=new MqttAsyncClient(propertyHelper.getMqttServerURI(), MqttAsyncClient.generateClientId());
            logger.info("MqttClient constructed");
            mqtt.setCallback(this);
            IMqttToken conToken = mqtt.connect();
            conToken.waitForCompletion();
            logger.info("MqttClient connected");
            IMqttToken subToken = mqtt.subscribe(deviceID,0);  //Qos 0?
            subToken.waitForCompletion();
            logger.info("subscribed to mqtt topic "+deviceID);
            error=false;
            model.mqttConnected();
            return publish("hello");
        } catch (MqttException mqtte) {
            logger.error("MqttException when creating MqttClient, connecting and subscribing");
            logger.debug("connect():"+mqtte);
        } catch (IllegalArgumentException iae) {
            logger.error("IllegalArgumentException when creating MqttClient, connecting and subscribing");
            logger.debug("connect():"+iae);
        } catch(NullPointerException npe) {
            logger.error("NullPointerException when creating MqttClient, connecting and subscribing");
            logger.debug("connect():"+npe);
        }
        model.mqttConnectionLost();
        error=true;
        return false;
    }
    /**
     * Sends the message to the topic deviceID defined in the constructor.
     * @param message
     * @return
     */
    public synchronized boolean publish(String message) {
        if(error&&!connect()) {
            logger.warn("mqtt error not resolved, not publishing "+message);
            return false;
        }
        try {
            mqtt.publish(deviceID, new MqttMessage(message.getBytes()));
            logger.info("published message to "+deviceID+": "+message);
            return true;
        } catch (MqttException e) {
            logger.error("MqttException when publishing message");
            logger.debug("publish(): "+e);
            model.mqttConnectionLost();
            error=true;
            return false;
        }
    }
    @Override
    public synchronized void connectionLost(Throwable cause) {
        logger.error("mqtt connection lost");
        logger.debug("connectionLost(): cause: "+cause);
        error=true;
        model.mqttConnectionLost();
    }
    @Override
    public synchronized void deliveryComplete(IMqttDeliveryToken token) {
        try {
            logger.debug("delivery complete: "+token.getMessage());
        } catch (MqttException e) {
            logger.warn("delivery complete, MqttException");
            logger.debug("deliveryComplete(): "+e);
        }
    }
    @Override
    public synchronized void messageArrived(String topic, MqttMessage message) throws Exception {
        logger.info("message arrived on "+topic+": "+message);
        String[] information=new String(message.getPayload()).split(" ");
        if(information.length==2) {
            if("emergency".equals(information[0])&&"shutdown".equals(information[1])) {
                logger.debug("messageArrived(): ignoring message: "+message);
                return;
            }
            if("manual".equals(information[0])&&"fix".equals(information[1])) {
                logger.debug("messageArrived(): ignoring message: "+message);
                return;
            }
        }
        if(information.length==1) {
            if("DOWN".equals(information[0])||"IDLE".equals(information[0])||"PROC".equals(information[0])||"MAINT".equals(information[0])) {
                logger.info("device reported state: "+information[0]);
                if("DOWN".equals(information[0])) status=false;
                else status=true;
                publish("debug "+model.isDebugging());
                model.setState(information[0]);
                return;
            }
            if("hello".equals(information[0])) {
                logger.debug("messageArrived(): ignoring message: "+message);
                return;
            }
        }
        if("debug".equals(information[0])) {
            if(information.length==2&&("true".equals(information[1])||"false".equals(information[1]))) {
                logger.debug("messageArrived(): ignoring message: "+message);
                return;
            }
            if(information.length>1) {
                String debug=new String(message.getPayload()).substring("debug ".length());
                model.debugArrived(debug+"\n");
                logger.debug("debug added: "+debug);
                return;
            }
        }
        logger.warn("not recognized message "+message);
    }
    public void fix() {
        if(error) error=!connect();
    }
    public boolean hasError() {
        return error;
    }
    public boolean isOnline() {
        return status;
    }
    public void close() {
        try {
            mqtt.disconnect();
            mqtt.close();
            logger.info("mqtt connection closed");
        } catch (MqttException e) {
            logger.error("MqttException when disconnecting and closing MqttClient");
        } catch (NullPointerException e) {
            logger.error("NullPointerException when disconnecting and closing MqttClient");
        }
        String[] directories=Paths.get("").toAbsolutePath().toFile().list(new FilenameFilter(){
            @Override
            public boolean accept(File dir, String name) {
                if(name.startsWith("paho")) return true;
                return false;
            } 
        });
        for(String directory:directories) {
            logger.info("removing directory "+directory);
            new File(directory+"/.lck").delete();
            new File(directory).delete();
        }
    }
}
