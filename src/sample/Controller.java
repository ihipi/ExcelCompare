package sample;

import core.diff_match_patch;
import core.diff_match_patch.Diff;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.input.DragEvent;

import java.net.URL;
import java.util.LinkedList;
import java.util.ResourceBundle;

import static core.diff_match_patch.diff_main;

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



    }

    @FXML
    void initialize() {
        assert btnCompare != null : "fx:id=\"btnCompare\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert txtB != null : "fx:id=\"txtB\" was not injected: check your FXML file 'MainWindow.fxml'.";


    }


}
