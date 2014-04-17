package blackjack.javafx.main;

import blackjack.engine.BlackJackTable;
import blackjack.engine.PlayerType;
import java.net.URL;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author shai
 */
public class MainApp extends Application {
    private BlackJackTable table;
    
    @Override
    public void start(Stage stage) throws Exception {
        table = BlackJackTable.createDefaultTable();
        
        FXMLLoader fxmlLoader = new FXMLLoader();
        URL url = getClass().getResource("TableFXML.fxml");
        fxmlLoader.setLocation(url);
        Parent root = (Parent)fxmlLoader.load(url.openStream());
        TableController tableController = (TableController) fxmlLoader.getController();
        tableController.setTable(table);
        stage.setTitle("BlackJack Game");
        stage.setResizable(false);
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
