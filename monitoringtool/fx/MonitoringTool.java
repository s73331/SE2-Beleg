package monitoringtool.fx;

import java.io.IOException;

import monitoringtool.base.Model;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
 
public class MonitoringTool extends Application {
    private Model model;
    public static void main(String[] args) {
        launch(args);
    }
    @Override
    public void start(Stage primaryStage) throws IOException {
        model=Model.getInstance();
        primaryStage.setTitle("Monitoring Tool - Gerät "+model.getDeviceID());
        Pane root=(Pane)FXMLLoader.load(getClass().getResource("monitoringview.fxml"));
        Scene scene=new Scene(root, model.getWidth(), model.getHeight());
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}