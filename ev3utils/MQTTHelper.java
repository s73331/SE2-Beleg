package ev3utils;

import org.eclipse.paho.client.mqttv3.*;
import java.util.Observable;
import java.lang.String;

public class MQTTHelper extends Observable implements MqttCallback {
    private String topic;
    private MqttClient client;
    public MQTTHelper(String serverURI, String clientId, String topic) {
        try {
            client = new MqttClient(serverURI, clientId);
            client.connect();
        } catch (MqttException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        this.topic = topic;
    }
    public void connectionLost(Throwable cause) {
        System.out.println("Connection lost!");
    }
    public void deliveryComplete(IMqttDeliveryToken token) {
        // not used
    }
    public void messageArrived(String topic, MqttMessage mqttmessage) throws Exception {
        //TODO
        System.out.println("-------------------------------------------------");
        System.out.println("| Topic: " + topic);
        System.out.println("| Message: " + new String(mqttmessage.getPayload()));
        System.out.println("-------------------------------------------------");
    }
    public boolean publish(String message) {
        try {
            client.publish(topic, new MqttMessage(message.getBytes()));
            return true;
        } catch (MqttException e) {
            e.printStackTrace();
            return false;
        }
    }
    public void close() {
        try {
            client.close();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
