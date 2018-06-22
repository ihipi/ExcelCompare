package gui;

import core.ExcelCompare;
import core.diff_match_patch;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class Controller {

    private diff_match_patch comparador = new diff_match_patch();
    private ExcelCompare eCompare = new ExcelCompare();

    @FXML
    private ToggleGroup toggleGroup;

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
        if(!txtA.getText().isEmpty() && !txtB.getText().isEmpty()) {
            Workbook wbA = eCompare.openFile(txtA.getText());
            Workbook wbB = eCompare.openFile(txtB.getText());
            boolean isopenl = false;
            if("OpenL".equalsIgnoreCase(toggleGroup.getSelectedToggle().toString())){
                isopenl = true;
            }
            eCompare.compareWorkbooks(wbA, wbB, isopenl);
        }
    }

    @FXML
    void initialize() {
        assert btnCompare != null : "fx:id=\"btnCompare\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert txtB != null : "fx:id=\"txtB\" was not injected: check your FXML file 'MainWindow.fxml'.";
    }

    @FXML
    public void leftDragOver(DragEvent dragEvent) {
        fileDragOver(dragEvent, txtA);
    }

    @FXML
    public void rigthDragOver(DragEvent dragEvent) {
        fileDragOver(dragEvent, txtB);
    }

    @FXML
    void fileDragOver(DragEvent event, TextArea textArea) {
        Dragboard board = event.getDragboard();
        if (board.hasFiles()) {

            List<File> phil = board.getFiles();
            String path = phil.get(0).toPath().toString();

            if (path.endsWith(".xls") ||path.endsWith(".xlsx")) {
                event.acceptTransferModes(TransferMode.ANY);
                textArea.setText(path);
            }
        }
    }

    @FXML
    public void fileChooserA(ActionEvent actionEvent) {
        fileChooser(actionEvent, txtA);
    }

    @FXML
    public void fileChooserB(ActionEvent actionEvent) {
        fileChooser(actionEvent, txtB);
    }

    @FXML
    public void fileChooser(ActionEvent event, TextArea txtArea){
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Excel '97 files (*.xls)", "*.xls");
        FileChooser.ExtensionFilter extFilterx = new FileChooser.ExtensionFilter("Excel files (*.xlsx)", "*.xlsx");
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Excel File");
        chooser.getExtensionFilters().add(extFilter);
        chooser.getExtensionFilters().add(extFilterx);
        File file = chooser.showOpenDialog(new Stage());
        if (file != null) {
            txtArea.setText(file.getPath());
        }
    }

}
