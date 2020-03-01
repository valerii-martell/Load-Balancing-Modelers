package com.commbus.planner.view;

import com.commbus.planner.Main;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;

import java.io.File;

/**
 * Created by KirinTor on 20.12.2017.
 *
 * Контролер кореневого макету.
 */
public class RootLayoutController {

        private Main main;

        /**
         * Викликається головним додатком, щоб надати посилання на самого себе
         *
         * @param main
         */
        public void setMain(Main main) {
            this.main = main;
        }

        /**
         * Створює пустий граф
         */
        @FXML
        private void handleNew() {
            main.getNodes().clear();
            main.setGraphFilePath(null);
        }

        /**
         * Відкриває FileChooser, аби надати можливість
         * вибрати готовий граф для завантаження.
         */
        @FXML
        private void handleOpen() {
            FileChooser fileChooser = new FileChooser();

            // Фільтр розширень
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                    "XML files (*.xml)", "*.xml");
            fileChooser.getExtensionFilters().add(extFilter);

            // Діалог загрузки файлу
            File file = fileChooser.showOpenDialog(main.getPrimaryStage());

            if (file != null) {
                main.loadGraphNodesFromFile(file);
            }
        }

        /**
         * Зберігає зміни в поточний відкритий файл.
         * Якщо файл не відкритий, то відображається діалог "Save as...".
         */
        @FXML
        private void handleSave() {
            File graphFile = main.getGraphFilePath();
            if (graphFile != null) {
                main.saveGraphNodesToFile(graphFile);
            } else {
                handleSaveAs();
            }
        }

        /**
         * Відкриває FileChooser, аби надати можливість
         * вибрати файл, в який буде записано дані
         */
        @FXML
        private void handleSaveAs() {
            FileChooser fileChooser = new FileChooser();

            // Задаємо фільтр розширень
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                    "XML files (*.xml)", "*.xml");
            fileChooser.getExtensionFilters().add(extFilter);

            // Показуємо діалог збереження файлу
            File file = fileChooser.showSaveDialog(main.getPrimaryStage());

            if (file != null) {
                // Перевіримо чи правильне розширення
                if (!file.getPath().endsWith(".xml")) {
                    file = new File(file.getPath() + ".xml");
                }
                main.saveGraphNodesToFile(file);
            }
        }

        /**
         * Відкриває діалогове вікно about.
         */
        @FXML
        private void handleAbout() {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Common Bus");
            alert.setHeaderText("About");
            alert.setContentText("Application for parallel programs immersion planning\nAuthor: Alyona Kalytenko\nContacts: jamaica.k0@gmail.com");

            alert.showAndWait();
        }

        /**
         * Завершує роботу
         */
        @FXML
        private void handleExit() {
            System.exit(0);
        }
}
