package monitoringtool;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.util.Callback;

public class Controller implements InvalidationListener, Runnable, View {
    private Model model=Model.getInstance();
    private  static final Logger logger=LogManager.getLogger();
    @FXML
    Text recipes, currentRecipe, currentItem, onlineTime, processedItems, failedItems, debugState, debugInformation, mqttError, sqlError, mqttStatus, extraStatus;
    @FXML
    Pane debugPane, queryPane, informationPane, buttonPane;
    @FXML
    ListView<String> queryList;
    @FXML
    TableView<ObservableList<String>> queryContent;
    @FXML
    Button machineFixButton, shutdownButton, debugButton, queryButton, mqttFixButton;
    private ObservableList<String> queries;
    private ObservableList<ObservableList<String>> data;
    public void initialize() {
        logger.debug("initialize(): entered");
        showDebug();
        queries=FXCollections.observableArrayList(model.getQueries());
        queryList.setItems(queries);
        queryList.getSelectionModel().select(0);
        model.setCurrentQuery(queryList.getSelectionModel().getSelectedItem());
        model.updateQuery();
        updateQuery(true);
        queryList.getSelectionModel().getSelectedItems().addListener(this);
        logger.debug("initialize(): queryList initialized");
        model.setView(this);
        new Thread(model).start();
        logger.debug("initialize(): at end");
    }
    public void showDebug() {
        logger.info("showing debug");
        debugPane.setVisible(true);
        queryPane.setVisible(false);
    }
    public void showQuery() {
        logger.info("showing query");
        debugPane.setVisible(false);
        queryPane.setVisible(true);
        logger.debug("showed query");
        model.updateQuery();
        updateQuery(true);
    }
    public void emergencyShutdown() {
        model.emergencyShutdown();
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
    /*
     * necessary for FXML
     */
    public void updateQuery() {
        updateQuery(false);
    }
    public void updateQuery(boolean changeColumns) {
        logger.info("refreshing query with changeColumns: "+changeColumns);
        ResultSet resultSet=model.updateQuery();
        if(resultSet==null) {
            logger.warn("resultSet is null");
            return;
        }
        if(model.isResultSetClosed()) {
            logger.info("resultSet is closed, not updating");
            return;
        }
        try {
            if(changeColumns) {
                data = FXCollections.observableArrayList();
                queryContent.getColumns().remove(0, queryContent.getColumns().size());
                for(int i=0 ; i<resultSet.getMetaData().getColumnCount(); i++) {
                    final int j=i;
                    TableColumn<ObservableList<String>, String> col = new TableColumn<ObservableList<String>,String>(resultSet.getMetaData().getColumnName(i+1));
                    col.setCellValueFactory(new Callback<CellDataFeatures<ObservableList<String>, String>, ObservableValue<String>>() {
                        public ObservableValue<String> call(CellDataFeatures<ObservableList<String>, String> p) {
                            try {
                            return new SimpleStringProperty(p.getValue().get(j));
                            } catch(NullPointerException npe) {
                                logger.info("NullPointerException column: "+j);
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
            } else {
                for(int j=0; resultSet.next(); j++){
                    //Iterate Row
                    ObservableList<String> row = data.get(j);
                    for(int i=1 ; i<=resultSet.getMetaData().getColumnCount(); i++){
                        //Iterate Column
                        row.add(resultSet.getString(i));
                    }
                }
            }
        } catch(SQLException sqle) {
            logger.error("SQLException on controller"+sqle);
        }
        queryContent.setItems(data);
        logger.debug("refreshed query");
    }
    public void invalidated(Observable arg0) {
        logger.info("selection of queryList changed to "+queryList.getSelectionModel().getSelectedItem());
        model.setCurrentQuery(queryList.getSelectionModel().getSelectedItem());
        updateQuery(true);
    }
    public void fixMachine() {
        model.fixMachine();
    }
    @Override
    public void run() {
        logger.info("refreshing view");
        String state=model.getState();
        logger.debug("got state: "+state);
        String backgroundColor;
        switch(state) {
        case "PROC":   backgroundColor="greenyellow";
                       break;
        case "IDLE":   backgroundColor="yellow";
                       break;
        case "MAINT":  backgroundColor="darksalmon";
                       break;
        case "DOWN":   backgroundColor="aqua";
                       break;
        default:       backgroundColor="white";
                       extraStatus.setText("illegal state: "+state);
        }
        logger.debug("background color:"+backgroundColor);
        informationPane.setStyle("-fx-background-color: "+backgroundColor+";");
        buttonPane.setStyle("-fx-background-color: "+backgroundColor+";");
        String recs=model.getRecipes();
        debugInformation.setText(model.getDebugLog());
        if(recs.length()>0) {
            recipes.setText("Rezepte: "+model.getRecipes());
        } else {
            recipes.setText("");
        }
        currentItem.setText("");
        if("".equals(model.getOnlineTime())) onlineTime.setText("");
        else onlineTime.setText("Online seit:"+model.getOnlineTime());
        debugButton.setDisable(false);
        shutdownButton.setDisable(false);
        machineFixButton.setDisable(true);
        switch(state) {
        case "":
        case "DOWN":
            shutdownButton.setDisable(true);
            debugButton.setDisable(true);
            onlineTime.setText("");
            break;
        case "PROC":
            currentItem.setText("Zurzeit bearbeitetes Teil: "+model.getCurrentItem());
            break;
        case "MAINT":
            machineFixButton.setDisable(false);
            break;
        case "IDLE":
        default:
        }
        if(model.hasSQLError()) {               //sql error as 3nd last, medium priority
            sqlError.setText("SQL Error");
        } else {
            sqlError.setText("");
        }
        if(model.isMqttOnline()) {              // mqtt status as 2nd last, high priority
            mqttStatus.setText("device is online via MQTT");
            debugButton.setDisable(false);
            shutdownButton.setDisable(false);
        } else {
            mqttStatus.setText("device is offline via MQTT");
            debugButton.setDisable(true);
            machineFixButton.setDisable(true);
            shutdownButton.setDisable(true);
        }
        if(model.hasMqttError()) {              //Mqtt Error last, highest priority
            debugButton.setDisable(true);
            machineFixButton.setDisable(true);
            shutdownButton.setDisable(true);
            mqttError.setText("MQTT Error");
            mqttStatus.setText("");
            mqttFixButton.setVisible(true);
        } else {
            mqttError.setText("");
            mqttFixButton.setVisible(false);
        }
        String processed=model.getProcessedItems();
        if(!"".equals(processed)) {
            processedItems.setText("Abgearbeitete Teile: "+processed);
        }
        String failed=model.getFailedItems();
        if(!"".equals(failed)) {
            failedItems.setText("Fehlgeschlagene Teile: "+failed);
        }
        updateQuery(false);
        logger.info("refreshed view");
    } 
    public void update() {
        Platform.runLater(this);
    }
    public void fixMqtt() {
        model.fixMqtt();
    }
}
