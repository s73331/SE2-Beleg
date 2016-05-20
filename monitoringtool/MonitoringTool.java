package monitoringtool;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
 
public class MonitoringTool extends Application {
    private Model model;
    private static final Logger logger=LogManager.getRootLogger();
    public static void main(String[] args) throws ClassNotFoundException {
        logger.info("entering main");
        Class.forName("org.postgresql.Driver");
        logger.debug("loaded org.postgresql.Driver");
        launch(args);
    }
    @Override
    public void start(Stage primaryStage) throws IOException {
        model=Model.getInstance();
        primaryStage.setTitle("Monitoring Tool - Gerät "+model.getDeviceID());
        Pane root=(Pane)FXMLLoader.load(getClass().getResource("monitoringview.fxml"));
        logger.debug("FXML loaded");
        Scene scene=new Scene(root, model.getWidth(), model.getHeight());
        primaryStage.setScene(scene);
        primaryStage.show();
        logger.debug("start finished");
    }
    public void stop() {
        logger.info("exiting normally");
        System.exit(0);
    }
}