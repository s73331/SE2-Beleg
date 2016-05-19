package monitoringtool.views;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import monitoringtool.base.Model;
import monitoringtool.base.View;


/**
 * 
 * @author martin
 *
 */
public class MonitoringInformationView extends JPanel implements View {
    private static final long serialVersionUID = 8571048851452778601L;
    private JLabel recipes, lastRecipe, currentPart, onlineTime, partCount, errorCount;
    private Model model;
    public MonitoringInformationView(Model model) {
        this.model=model;
        setLayout(new GridLayout(6,1));
        recipes=new JLabel();
        lastRecipe=new JLabel();
        currentPart=new JLabel();
        onlineTime=new JLabel();
        partCount=new JLabel();
        errorCount=new JLabel();
        add(recipes);
        add(lastRecipe);
        add(currentPart);
        add(onlineTime);
        add(partCount);
        add(errorCount);
        update();
    }
    public void update() {
        recipes.setText("Rezepte: "+String.join(", ",model.getRecipes()));
        lastRecipe.setText("Zurzeit genutztes Rezept: "+model.getCurrentRecipe());
        currentPart.setText("Zurzeit bearbeitetes Teil: "+model.getCurrentItem());
        onlineTime.setText("Online seit: "+model.getOnlineTime());
        partCount.setText("Bearbeitete Teile: "+model.getItemCount());
        errorCount.setText("Fehlgeschlagene Teile: "+model.getFailCount());
    }
}
