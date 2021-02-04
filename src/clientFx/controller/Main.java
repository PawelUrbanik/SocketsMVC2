package controller;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.util.Optional;

public class Main extends Application {

    public static final String appName = "Sockets-JavaFX-MVC";

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/sample.fxml"));

        AnchorPane root = loader.load();
        ChatController chatController = loader.getController();
        chatController.setUserName(getUserName());
        chatController.setHost("localhost");
        chatController.setPort(9001);
        chatController.run();
        primaryStage.setTitle(appName);
        primaryStage.setScene(new Scene(root, 450, 600));
        primaryStage.setOnHiding(e -> primaryStage_Hiding(e, chatController));
        primaryStage.show();
    }

    public String getUserName(){
        TextInputDialog textInputDialog = new TextInputDialog("Anonymous");
        textInputDialog.setTitle("Nazwa użytkownika");
        textInputDialog.setHeaderText("Podaj swóje imię lub ksywkę");
        textInputDialog.setContentText("Wpisz dane:");
        Optional<String> result = textInputDialog.showAndWait();
        return result.orElse("Anonymous");
    }

    private void primaryStage_Hiding(WindowEvent e, ChatController controller)  {
        try {
            controller.close();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
    public static void main(String[] args) {
        launch(args);
    }
}
