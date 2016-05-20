package monitoringtool;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

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

public class Controller implements InvalidationListener, MqttCallback {
    private Model model=Model.getInstance();
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
    public void initialize() {
        recipes.setText("Rezepte: "+model.getRecipesString());
        currentRecipe.setText("Zurzeit benutztes Rezept: "+model.getCurrentRecipe());
        currentItem.setText("Zurzeit bearbeitetes Rezept: "+model.getCurrentItem());
        onlineTime.setText("Online seit: "+model.getOnlineTime());
        processedItems.setText("Abgearbeitete Teile: "+model.getProcessedItems());
        failedItems.setText("Fehlgeschlagene Teile: "+model.getFailedItems());
        showDebug();
        queries=FXCollections.observableArrayList();
        for(String query:model.getQueries()) {
            queries.add(query);
        }
        queryList.setItems(queries);
        queryList.getSelectionModel().select(0);
        queryList.getSelectionModel().getSelectedItems().addListener(this);
        try {
            mqtt=new MqttClient(model.getMqttServerURI(), MqttClient.generateClientId());
            mqtt.connect();
            mqtt.setCallback(this);
            mqtt.subscribe(model.getDeviceID());
        } catch (MqttException e) {
            model.setMqttError(true);
            mqttError.setText("\nMQTT Error");
            informationPane.setStyle("-fx-background-color:blue");
            e.printStackTrace();
        }
    }
    private boolean publish(String message) {
        if(model.hasMqttError()) return false;
        try {
            mqtt.publish(model.getDeviceID(), new MqttMessage(message.getBytes()));
        } catch (MqttException e) {
            model.setMqttError(true);
            mqttError.setText("\nMQTT Error");
            informationPane.setStyle("-fx-background-color:blue");
            e.printStackTrace();
            return false;
        }
        return true;
    }
    public void showDebug() {
        debugPane.setVisible(true);
        queryPane.setVisible(false);
    }
    public void showQuery() {
        updateQuery();
        debugPane.setVisible(false);
        queryPane.setVisible(true);
    }
    public void emergencyShutdown() {
        publish("emergency shutdown");
    }
    public void fixMachine() {
        publish("manual fix");
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
                            System.out.println("NullPointerException @column:"+j);
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
        model.setCurrentQuery(queryList.getSelectionModel().getSelectedItem());
        updateQuery();
    }
    public void connectionLost(Throwable cause) {
        model.setMqttError(true);        
    }
    public void deliveryComplete(IMqttDeliveryToken token) {
        // not used
    }
    public void messageArrived(String topic, MqttMessage message) {
        String information=new String(message.getPayload());
        if(information.startsWith("debug")) {
            information=information.substring(6);
            if("true".equals(information)||"false".equals(information)) return;
            model.addDebug(information);
            debugInformation.setText(model.getDebugLog());
            return;
        }
        
    }
}
