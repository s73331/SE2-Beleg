package monitoringtool;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

public class Controller implements InvalidationListener, MqttMiniCallback, Runnable {
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
    private ObservableList<String> queries;
    public void initialize() {
        logger.debug("currentRecipe initialized");
        showDebug();
        queries=FXCollections.observableArrayList();
        for(String query:model.getQueries()) {
            queries.add(query);
        }
        queryList.setItems(queries);
        queryList.getSelectionModel().select(0);
        queryList.getSelectionModel().getSelectedItems().addListener(this);
        logger.debug("queryList initialized");
        model.setMqttCallback(this);
        new Thread(this).start();
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
        model.emergencyShutdown();
    }
    public void fixMachine() {
        model.fixMachine();
    }
    public void toggleDebug() {
        model.toggleDebug();
        String debugStateText="Debug Mode: ";
        if(model.isDebugging()) {
            debugStateText+="on";
        } else {
            debugStateText+="off";
        }
        debugState.setText(debugStateText);
    }
    public void updateQuery() {
        model.setCurrentQuery(queryList.getSelectionModel().getSelectedItem());
        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        ResultSet resultSet=model.updateQuery();
        if(resultSet==null) {
            logger.warn("resultSet is null");
            return;
        }
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
            logger.error("SQLException on controller"+sqle);
        }
        queryContent.setItems(data);
    }
    public void invalidated(Observable arg0) {
        logger.info("selection of queryList changed to "+queryList.getSelectionModel().getSelectedItem());
        model.setCurrentQuery(queryList.getSelectionModel().getSelectedItem());
        updateQuery();
    }
    @Override
    public void run() {
        while(true) {
            logger.info("updating SQL information");
            String state=model.getMachineState();
            informationPane.setStyle("-fx-background-color: "+model.getBackgroundColor()+";");
            String recs=model.getRecipes();
            if(recs.length()>0) {
                recipes.setText("Rezepte: "+model.getRecipes());
            } else {
                recipes.setText("");
            }
            if("DOWN".equals(state)) {
                currentItem.setText("");
                onlineTime.setText("");
            } else {
                currentItem.setText("Zurzeit bearbeitetes Teil: "+model.getCurrentItem());
                onlineTime.setText("Online seit:"+model.getOnlineTime());
            }
            processedItems.setText("Abgearbeitete Teile: "+model.getProcessedItems());
            failedItems.setText("Fehlgeschlagene Teile: "+model.getFailedItems());
            if(model.fixMqtt()) {
                mqttError.setText("");
            } else {
                mqttError.setText("MQTT Error 3");
            }
            try {
                logger.debug("sleeping for 10s");
                Thread.sleep(10000);
                logger.debug("slept for 10s");
            } catch (InterruptedException e) {
                logger.info("terminating sql update thread");
                break;
            }
        }
    }
    @Override
    public void connectionLost() {
        mqttError.setText("\nMQTT connection lost");
    }
    @Override
    public void debugArrived() {
        debugInformation.setText(model.getDebugLog());
    }
}
