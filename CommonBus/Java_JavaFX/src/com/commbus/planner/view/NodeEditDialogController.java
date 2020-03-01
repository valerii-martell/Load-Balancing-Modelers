package com.commbus.planner.view;

import com.commbus.planner.Main;
import com.commbus.planner.model.SimpleNodeProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Created by KirinTor on 20.12.2017.
 */
public class NodeEditDialogController {

    @FXML
    private TextField nodeIdField;
    @FXML
    private TextField nodeRankField;
    @FXML
    private TextField nodeDependenciesField;
    @FXML
    private TextField nodeCommunicationsLengthsField;

    private Stage dialogStage;
    private SimpleNodeProperty node;
    private boolean applyClicked = false;

    private Main main;

    @FXML
    private void initialize() {
    }

    public void setMain(Main main) {
        this.main = main;
    }

    /**
     * Устанавлюємо сцену для цього вікна.
     *
     * @param dialogStage
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
        this.dialogStage.getIcons().add(new Image(getClass().getResourceAsStream("/resources/images/Icon.png")));
    }

    /**
     * Задаємо вершину, для якої змінюватимемо параметри
     *
     * @param node
     */
    public void setNode(SimpleNodeProperty node) {
        this.node = node;

        nodeIdField.setText(node.getId());
        nodeRankField.setText(node.getRank());
        nodeDependenciesField.setText(node.getDependencies());
        nodeCommunicationsLengthsField.setText(node.getCommunicationsLengths());
    }

    public boolean isApplyClicked() {
        return applyClicked;
    }

    @FXML
    private void handleApply() {
        if (isInputValid()) {
            node.setId(nodeIdField.getText());
            node.setRank(nodeRankField.getText());
            node.setDependencies(nodeDependenciesField.getText());
            node.setCommunicationsLengths(nodeCommunicationsLengthsField.getText());

            applyClicked = true;
            dialogStage.close();
        }
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    /**
     * Перевіряє коректність вводу
     *
     * @return true, якщо введення коректне
     */
    private boolean isInputValid() {
        String errorMessage = "";
        int buffer;
        try {
            buffer = Integer.valueOf(nodeIdField.getText());
            if (buffer < 0){
                errorMessage += "Node ID must be natural number!\n";
            }
            for(int i = 0; i < main.getNodes().size(); i++) {
                if (main.getNodes().get(i).getId() == nodeIdField.getText())
                    errorMessage += "Node with this ID already created!\n";
                break;
            }
        }
        catch (NumberFormatException e){
            errorMessage += "No valid node ID!\n";
        }

        try {
            buffer = Integer.valueOf(nodeRankField.getText());
            if (buffer <= 0){
                errorMessage += "Rank must be greater than 0!\n";
            }
        }
        catch (NumberFormatException e){
            errorMessage += "No valid node rank!\n";
        }
/*
        String dependenciesString = nodeDependenciesField.getText();
        String[] dependencies = new String[]{};
        if (dependenciesString != null){
            dependencies = dependenciesString.split(" ");

            try {
                for (int i = 0; i<dependencies.length; i++){
                    buffer = Integer.valueOf(dependencies[i]);
                }

            }
            catch (NumberFormatException e){
                errorMessage += "No valid node dependencies!\n";
            }

            Set<String> duplicates = new HashSet<>();
            for (int i = 0; i < dependencies.length; i++)
            {
                if (duplicates.contains(dependencies[i]))
                {
                    continue;
                }
                for (int j = i + 1; j < dependencies.length; j++)
                {
                    if (dependencies[i].equals(dependencies[j]))
                    {
                        duplicates.add(dependencies[i]);
                        break;
                    }
                }
            }
            if (!duplicates.isEmpty())
                errorMessage += "Duplicates in dependecies!\n";
        }

        String communicationsLengthsString = nodeCommunicationsLengthsField.getText();
        String[] communicationsLengths = new String[]{};
        if (communicationsLengthsString != null){
            communicationsLengths = communicationsLengthsString.split(" ");

            try {
                for (int i = 0; i<communicationsLengths.length; i++){
                    buffer = Integer.valueOf(communicationsLengths[i]);
                }
            }
            catch (NumberFormatException e){
                errorMessage += "No valid node communications lengths!\n";
            }
        }

        if (dependencies.length != communicationsLengths.length)
            errorMessage += "Disparity dependencies and communications lengths!\n";

*/

        if (errorMessage.length() == 0) {
            return true;
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(dialogStage);
            alert.setTitle("Invalid Fields");
            alert.setHeaderText("Please correct invalid fields");
            alert.setContentText(errorMessage);

            alert.showAndWait();

            return false;
        }
    }

}
