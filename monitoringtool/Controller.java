package monitoringtool;

import java.sql.ResultSet;
import java.sql.SQLException;

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

public class Controller implements InvalidationListener {
    private Model model=Model.getInstance();
    @FXML
    Text recipes, currentRecipe, currentItem, onlineTime, processedItems, failedItems, debugState, debugInformation;
    @FXML
    Pane debugPane, queryPane, informationPane;
    @FXML
    ListView<String> queryList;
    @FXML
    TableView<ObservableList<String>> queryContent;
    ObservableList<String> queries;
    public void initialize() {
        recipes.setText("Rezepte: "+model.getRecipesString());
        currentRecipe.setText("Zurzeit benutztes Rezept: "+model.getCurrentRecipe());
        currentItem.setText("Zurzeit bearbeitetes Rezept: "+model.getCurrentItem());
        onlineTime.setText("Online seit: "+model.getOnlineTime());
        processedItems.setText("Abgearbeitete Teile: "+model.getProcessedItems());
        failedItems.setText("Fehlgeschlagene Teile: "+model.getFailedItems());
        showDebug();
        for(int i=0;i<=1000;i++) {
            debugInformation.setText(debugInformation.getText()+"kek"+i+"\n");
        }
        queries=FXCollections.observableArrayList();
        for(String query:model.getQueries()) {
            queries.add(query);
        }
        queryList.setItems(queries);
        queryList.getSelectionModel().select(0);
        queryList.getSelectionModel().getSelectedItems().addListener(this);
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
        System.out.println("emergencyShutdown");
    }
    public void fixMachine() {
        System.out.println("fixMachine");
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
    //@SuppressWarnings({ "rawtypes", "unchecked" })
    public void updateQuery() {
        model.updateQuery();
        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        ResultSet resultSet=model.getQueryResult();
        queryContent.getColumns().remove(0, queryContent.getColumns().size());
        try {
            /**********************************
             * TABLE COLUMN ADDED DYNAMICALLY *
             **********************************/
            for(int i=0 ; i<resultSet.getMetaData().getColumnCount(); i++) {
                final int j=i;
                //We are using non property style for making dynamic table
                TableColumn<ObservableList<String>, String> col = new TableColumn<ObservableList<String>,String>(resultSet.getMetaData().getColumnName(i+1));
                col.setCellValueFactory(new Callback<CellDataFeatures<ObservableList<String>, String>, ObservableValue<String>>() {
                    public ObservableValue<String> call(CellDataFeatures<ObservableList<String>, String> p) {
                        // p.getValue() returns the Person instance for a particular TableView row
                        System.out.println("j: "+j);
                        try {
                        return new SimpleStringProperty(p.getValue().get(j).toString());
                        } catch(NullPointerException npe) {
                            System.out.println("NullPointerException @j="+j);
                            return new SimpleStringProperty("null");
                        }
                    }
                 });
                queryContent.getColumns().add(col); 
            }
            /********************************
             * Data added to ObservableList *
             ********************************/
            while(resultSet.next()){
                //Iterate Row
                ObservableList<String> row = FXCollections.observableArrayList();
                for(int i=1 ; i<=resultSet.getMetaData().getColumnCount(); i++){
                    //Iterate Column
                    row.add(resultSet.getString(i));
                }
                System.out.println("Row [1] added "+row);
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
}
