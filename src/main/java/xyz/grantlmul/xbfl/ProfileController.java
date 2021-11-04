package xyz.grantlmul.xbfl;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;

import java.net.URL;
import java.util.ResourceBundle;

public class ProfileController implements Initializable {
    @FXML
    ComboBox launchBehavior;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        launchBehavior.getSelectionModel().selectFirst();
    }
}
