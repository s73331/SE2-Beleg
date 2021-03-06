package monitoringtool;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
/**
 * Main class for Monitoring Tool.
 * 
 * @author martin
 *
 */
public class MonitoringTool extends Application {
    private Model model;
    private static final Logger logger=LogManager.getLogger();
    /**
     * Launches the Monitoring Tool.
     * @param args not used
     */
    public static void main(String[] args) {
        logger.debug("main(): entered");
        
        boolean ok = true;
        String[] classes = {"org.postgresql.core.BaseStatement", "javafx.application.Platform", "org.apache.logging.log4j.Logger", "org.eclipse.paho.client.mqttv3.MqttMessage"};
        
        for (String c : classes) {
            try {
                Class.forName(c);
            } catch( ClassNotFoundException e ) {
                System.out.println("Class "+c+" not found!");
                ok = false;
            }
        }
        
        if (ok)
            launch(args);
        else
            System.out.println("Not all Classes could be loaded. Exiting..");
        logger.debug("main(): exiting");
    }
    /**
     * Method as defined by javafx.application.Application.
     */
    @Override
    public void start(Stage primaryStage) {
        try {
            logger.debug("start(): entered");
            Class.forName("org.postgresql.Driver");
            logger.debug("start(): loaded org.postgresql.Driver");
            model=Model.getInstance();
            primaryStage.setTitle("Monitoring Tool - Ger�t "+model.getDeviceID());
            logger.debug("title set");
            Pane root=(Pane)FXMLLoader.load(getClass().getResource("monitoringview.fxml"));
            logger.debug("FXML loaded");
            Scene scene=new Scene(root, model.getWidth(), model.getHeight());
            logger.debug("scene constructed");
            primaryStage.setScene(scene);
            logger.debug("scene is now scene of primary stage");
            primaryStage.show();
            logger.debug("primary stage shown");
            logger.debug("start finished");
        } catch(Exception e) {
            logger.fatal("start(): Exception: "+e+e.getCause());e.printStackTrace();
            stop();
        }
    }
    
    /**
     * Method as defined by javafx.application.Application.
     */
    public void stop() {
        model.shutdown();
        logger.info("exiting successfully");
        System.exit(0);
    }
}