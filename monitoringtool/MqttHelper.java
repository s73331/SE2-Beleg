package monitoringtool;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MqttHelper implements MqttCallback {
    private static final Logger logger=LogManager.getLogger();
    private MqttAsyncClient mqtt;
    private boolean error=false;
    private String deviceID;
    private MqttModel model;
    private PropertyHelper propertyHelper;
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
            mqtt=new MqttAsyncClient(propertyHelper.getMqttServerURI(), MqttAsyncClient.generateClientId()); //todo: ClientID
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
            return true;
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
        if(information.length==1&&"hello".equals(information[0])) {
            logger.debug("messageArrived(): answering debug state");
            publish("debug "+model.isDebugging());
            return;
        }
        if("debug".equals(information[0])) {
            if(information.length==2&&("true".equals(information[1])||"false".equals(information[1]))) {
                logger.debug("messageArrived(): ignoring message: "+message);
                return;
            }
            if(information.length>1) {
                String debug=new String(message.getPayload()).substring("debug ".length());
                model.addDebug(debug+"\n");
                logger.debug("debug added: "+debug);
                model.debugArrived();
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
}
