package monitoringtool.fx;

import monitoringtool.base.Model;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

public class Controller {
    private Model model=Model.getInstance();
    @FXML
    Text recipes, currentRecipe, currentItem, onlineTime, processedItems, failedItems;
    @FXML
    Pane debugPane, queryPane;
    public void initialize() {
        recipes.setText("Rezepte: "+model.getRecipesString());
        currentRecipe.setText("Zurzeit benutztes Rezept: "+model.getCurrentRecipe());
        currentItem.setText("Zurzeit bearbeitetes Rezept: "+model.getCurrentItem());
        onlineTime.setText("Online seit: "+model.getOnlineTime());
        processedItems.setText("Abgearbeitete Teile: "+model.getProcessedItems());
        failedItems.setText("Fehlgeschlagene Teile: "+model.getFailedItems());
        showDebug();
    }
    public void showDebug() {
        debugPane.setVisible(true);
        queryPane.setVisible(false);
    }
    public void showQuery() {
        debugPane.setVisible(false);
        queryPane.setVisible(true);
    }
    public void emergencyShutdown() {
        System.out.println("emergencyShutdown");
    }
    public void fixMachine() {
        System.out.println("fixMachine");
    }
}
