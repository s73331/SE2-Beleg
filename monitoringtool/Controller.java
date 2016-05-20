package monitoringtool;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.util.Callback;

public class Controller implements InvalidationListener, MqttCallback, Runnable {
    private Model model=Model.getInstance();
    private  static final Logger logger=LogManager.getRootLogger();
    @FXML
    Text recipes, currentRecipe, currentItem, onlineTime, processedItems, failedItems, debugState, debugInformation, mqttError;
    @FXML
    Pane debugPane, queryPane, informationPane;
    @FXML
    ListView<String> queryList;
    @FXML
    TableView<ObservableList<String>> queryContent;
    ObservableList<String> queries;
    private MqttClient mqtt;
    private PSQLHelper psql;
    public void initialize() {
        psql=model.getPSQLHelper();
        currentRecipe.setText("Zurzeit benutztes Rezept: "+model.getCurrentRecipe());
        logger.debug("currentRecipe initialized");
        currentItem.setText("Zurzeit bearbeitetes Teil: "+model.getCurrentItem());
        logger.debug("currentItem initialized");
        onlineTime.setText("Online seit: "+model.getOnlineTime());
        logger.debug("onlineTime initialized");
        processedItems.setText("Abgearbeitete Teile: "+model.getProcessedItems());
        logger.debug("processedItems initialized");
        failedItems.setText("Fehlgeschlagene Teile: "+model.getFailedItems());
        logger.debug("failedItems initialized");
        showDebug();
        logger.debug("showing debug");
        queries=FXCollections.observableArrayList();
        for(String query:model.getQueries()) {
            queries.add(query);
        }
        queryList.setItems(queries);
        queryList.getSelectionModel().select(0);
        queryList.getSelectionModel().getSelectedItems().addListener(this);
        logger.debug("queryList initialized");
        try {
            mqtt=new MqttClient(model.getMqttServerURI(), MqttClient.generateClientId());
            logger.debug("MqttClient constructed");
            mqtt.connect();
            logger.debug("MqttClient connected");
            mqtt.setCallback(this);
            mqtt.subscribe(model.getDeviceID());
            logger.info("subscribed to mqtt topic "+model.getDeviceID());
        } catch (MqttException e) {
            logger.error("MqttException: "+e);
            model.setMqttError(true);
            mqttError.setText("\nMQTT Error");
            informationPane.setStyle("-fx-background-color:blue");
        }
        new Thread(this).start();
    }
    private boolean publish(String message) {
        if(model.hasMqttError()) {
            logger.warn("MqttError, not publishing: "+message);
            return false;
        }
        try {
            mqtt.publish(model.getDeviceID(), new MqttMessage(message.getBytes()));
        } catch (MqttException e) {
            logger.error("MqttException: "+e);
            model.setMqttError(true);
            mqttError.setText("\nMQTT Error");
            informationPane.setStyle("-fx-background-color:blue");
            e.printStackTrace();
            return false;
        }
        logger.info("published message to "+model.getDeviceID()+": "+message);
        return true;
    }
    public void showDebug() {
        logger.info("showing debug");
        debugPane.setVisible(true);
        queryPane.setVisible(false);
    }
    public void showQuery() {
        logger.info("showing query");
        updateQuery();
        debugPane.setVisible(false);
        queryPane.setVisible(true);
    }
    public void emergencyShutdown() {
        if(model.hasMqttError()) {
            logger.info("MQTT Error, not sending emergency shutdown");
        } else {
            publish("emergency shutdown");
        }
    }
    public void fixMachine() {
        if("MAINT".equals(model.getMachineState())) {
            publish("manual fix");
        } else {
            logger.info("not in maint, not sending manual fix");
        }
    }
    public void toggleDebug() {
        if(model.hasMqttError()) return;
        model.toggleDebug();
        String debugStateText="Debug Mode: ";
        if(model.isDebugging()) {
            debugStateText+="on";
        } else {
            debugStateText+="off";
        }
        if(publish("debug "+model.isDebugging())) {
            debugState.setText(debugStateText);
        }
    }
    public void updateQuery() {
        model.setCurrentQuery(queryList.getSelectionModel().getSelectedItem());
        model.updateQuery();
        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        ResultSet resultSet=model.getQueryResult();
        queryContent.getColumns().remove(0, queryContent.getColumns().size());
        try {
            for(int i=0 ; i<resultSet.getMetaData().getColumnCount(); i++) {
                final int j=i;
                //We are using non property style for making dynamic table
                TableColumn<ObservableList<String>, String> col = new TableColumn<ObservableList<String>,String>(resultSet.getMetaData().getColumnName(i+1));
                col.setCellValueFactory(new Callback<CellDataFeatures<ObservableList<String>, String>, ObservableValue<String>>() {
                    public ObservableValue<String> call(CellDataFeatures<ObservableList<String>, String> p) {
                        // p.getValue() returns the Person instance for a particular TableView row
                        try {
                        return new SimpleStringProperty(p.getValue().get(j).toString());
                        } catch(NullPointerException npe) {
                            logger.info("NullpointerException column: "+j);
                            return new SimpleStringProperty("");
                        }
                    }
                 });
                queryContent.getColumns().add(col); 
            }
            while(resultSet.next()){
                //Iterate Row
                ObservableList<String> row = FXCollections.observableArrayList();
                for(int i=1 ; i<=resultSet.getMetaData().getColumnCount(); i++){
                    //Iterate Column
                    row.add(resultSet.getString(i));
                }
                data.add(row);
            }
        } catch(SQLException sqle) {
            sqle.printStackTrace();
            model.setSQLError(true);
        }
        queryContent.setItems(data);
    }
    public void invalidated(Observable arg0) {
        logger.info("selection of queryList changed to "+queryList.getSelectionModel().getSelectedItem());
        model.setCurrentQuery(queryList.getSelectionModel().getSelectedItem());
        updateQuery();
    }
    public void connectionLost(Throwable cause) {
        logger.error("mqtt connection lost");
        model.setMqttError(true);
        mqttError.setText("\nMQTT connection lost");
    }
    public void deliveryComplete(IMqttDeliveryToken token) {
        // not used
        try {
            logger.debug("delivery complete: "+token.getMessage());
        } catch (MqttException e) {
            logger.warn("delivery complete, MqttException: "+e);
        }
    }
    public void messageArrived(String topic, MqttMessage message) throws MqttPersistenceException, MqttException {
        logger.info("message arrived on "+topic+": "+message);
        String[] information=new String(message.getPayload()).split(" ");
        if(information.length==2) {
            if("debug".equals(information[0])) {
            if("true".equals(information[1])||"false".equals(information[1])) return;
            model.addDebug(information[1]);
            debugInformation.setText(model.getDebugLog());
            return;
            }
            if("emergency".equals(information[0])&&"shutdown".equals(information[1])) return;
            if("manual".equals(information[0])&&"fix".equals(information[1])) return;
        }
        if(information.length==1&&"hello".equals(information[0])) {
            // no idea why and how
            //as this is in the callback, one can't simply call publish
            //but with this code it somehow works
            mqtt.getTopic(model.getDeviceID()).publish(new MqttMessage(("debug "+model.isDebugging()).getBytes()));
            return;
        }
        logger.warn("undefined message"+information);
    }
    @Override
    public void run() {
        while(true) {
            logger.info("updating SQL information");
            model.setMachineState(psql.getMachineState(model.getDeviceID()));
            informationPane.setStyle("-fx-background-color: "+model.getBackgroundColor()+";");
            model.setRecipes(psql.getRecipes(model.getDeviceID()));
            recipes.setText("Rezepte: "+model.getRecipes());
            if(model.hasMqttError()) {
                try {
                    mqtt.connect();
                    logger.info("MqttClient reconnected");
                    model.setMqttError(false);
                    mqttError.setText("");
                } catch (MqttException e) {
                    logger.error("MqttException: "+e);
                    model.setMqttError(true);
                    mqttError.setText("\nMQTT Error");
                }
            }
            if(model.hasSQLError()) model.setSQLError(!model.getPSQLHelper().renewConnection());
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                logger.info("terminating sql update thread");
                break;
            }
        }
    }
}
