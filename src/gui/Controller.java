package gui;

import core.diff_match_patch;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller {

    private diff_match_patch comparador = new diff_match_patch();

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button btnCompare;

    @FXML
    private TextArea txtA;
    @FXML
    private TextArea txtB;

    @FXML
    public void compareFiles(ActionEvent actionEvent) {

        Parent root;
        try {
            root = FXMLLoader.load(getClass().getClassLoader().getResource("CompareWindow.fxml"), resources);
            Stage stage = new Stage();
            stage.setTitle("My New Stage Title");
            stage.setScene(new Scene(root, 450, 450));
            stage.show();
            // Hide this current window (if this is what you want)
            //((Node)(actionEvent.getSource())).getScene().getWindow().hide();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }

    @FXML
    void initialize() {
        assert btnCompare != null : "fx:id=\"btnCompare\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert txtB != null : "fx:id=\"txtB\" was not injected: check your FXML file 'MainWindow.fxml'.";


    }


}
