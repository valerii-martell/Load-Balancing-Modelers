package com.commbus.planner.view;

import com.commbus.planner.Main;
import com.commbus.planner.model.SimpleNodeProperty;
import com.commbus.planner.model.Supervisor;
import com.sun.org.apache.regexp.internal.RE;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.control.TableColumn;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Transform;
import javafx.stage.FileChooser;
import javafx.util.Callback;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by KirinTor on 20.12.2017.
 */
public class MainViewController {

    //TODO: find better combinations of height and width
    int labelsHeight = 40;
    int labelsWidth;

    //компоненти основної панелі
    @FXML
    private AnchorPane mainView;

    @FXML
    private TabPane tabPane;

    //компоненти панелі для вводу системних параметрів
    @FXML
    private Tab systemParametersTab;

    @FXML
    private TextField processorsCountField;

    @FXML
    private Button processorsCountIncButton;

    @FXML
    private Button processorsCountDecButton;

    @FXML
    private Button applyButton;

    @FXML
    private Button backButton;

    //компоненти панелі для вводу параметрів графу
    @FXML
    private Tab graphParametersTab;

    @FXML
    private TableView<SimpleNodeProperty> graphParametersTable;

    @FXML
    private TableColumn<SimpleNodeProperty, String> nodeIdColumn;

    @FXML
    private TableColumn<SimpleNodeProperty, String> nodeRankColumn;

    @FXML
    private TableColumn<SimpleNodeProperty, String> nodeDependenciesColumn;

    @FXML
    private TableColumn<SimpleNodeProperty, String> nodeCommunicationsLengthsColumn;

    @FXML
    private Button nodeAddButton;

    @FXML
    private Button nodeEditButton;

    @FXML
    private Button nodeDeleteButton;

    //компоненти панелі для відображення результатів
    @FXML
    private Tab immersionResultsTab;

    @FXML
    private ScrollPane immersionResultsContainer;

    @FXML
    private GridPane immersionResultsPane;

    @FXML
    private Button repeatButton;

    @FXML
    private Button saveImageButton;

    @FXML
    private VBox busAdvisorContainer;

    @FXML
    private Label busAdvisorLabel;

    //контейнер користувацьких загаловків GridPane
    //для зміни розмірів відображення під час збереження зображення
    ArrayList<ResultLabel> resultLabels = new ArrayList<ResultLabel>();

    //посилання на головний клас
    private Main main;

    /**
     * Конструктор за замовчуванням.
     * Конструктор викличеться перед методом initialize().
     */
    public MainViewController() {
    }

    /**
     * Ініціалізація класу-контролеру. Цей метод викликається автоматично
     * після того, як fxml-файл буде завантажений.
     * В ньому відбувається встановлення додаткових параметрів графічних елементів
     * та зв'язка їх з даними
     */
    @FXML
    private void initialize() {
        //картинки заголовків табличної панелі
        Image image = new Image("resources/images/graph.png", 50, 50, false, false);
        graphParametersTab.setGraphic(new ImageView(image));
        image = new Image("resources/images/cpu.png", 50, 50, false, false);
        systemParametersTab.setGraphic(new ImageView(image));
        image = new Image("resources/images/chart1.png", 50, 50, false, false);
        immersionResultsTab.setGraphic(new ImageView(image));
        image = new Image("resources/images/left.png", 20, 20, false, false);
        processorsCountDecButton.setGraphic(new ImageView(image));
        image = new Image("resources/images/right.png", 20, 20, false, false);
        processorsCountIncButton.setGraphic(new ImageView(image));

        // Ініціалізація таблиць
        // Положення тесту
        graphParametersTable.setEditable(true);
        nodeIdColumn.setStyle( "-fx-alignment: CENTER;");
        nodeRankColumn.setStyle( "-fx-alignment: CENTER;");
        nodeDependenciesColumn.setStyle( "-fx-alignment: CENTER;");
        nodeCommunicationsLengthsColumn.setStyle( "-fx-alignment: CENTER;");
        // Можливість редагування за подвійним кліком
        nodeIdColumn.setCellFactory(new Callback<TableColumn<SimpleNodeProperty, String>, TableCell<SimpleNodeProperty, String>>() {
            @Override
            public TableCell<SimpleNodeProperty, String> call(TableColumn<SimpleNodeProperty, String> param) {
                return new EditingCell(0);
            }
        });
        nodeRankColumn.setCellFactory(new Callback<TableColumn<SimpleNodeProperty, String>, TableCell<SimpleNodeProperty, String>>() {
            @Override
            public TableCell<SimpleNodeProperty, String> call(TableColumn<SimpleNodeProperty, String> param) {
                return new EditingCell(1);
            }
        });
        nodeDependenciesColumn.setCellFactory(new Callback<TableColumn<SimpleNodeProperty, String>, TableCell<SimpleNodeProperty, String>>() {
            @Override
            public TableCell<SimpleNodeProperty, String> call(TableColumn<SimpleNodeProperty, String> param) {
                return new EditingCell(2);
            }
        });
        nodeCommunicationsLengthsColumn.setCellFactory(new Callback<TableColumn<SimpleNodeProperty, String>, TableCell<SimpleNodeProperty, String>>() {
            @Override
            public TableCell<SimpleNodeProperty, String> call(TableColumn<SimpleNodeProperty, String> param) {
                return new EditingCell(3);
            }
        });
        // Зв'язування з даними
        nodeIdColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty());
        nodeRankColumn.setCellValueFactory(cellData -> cellData.getValue().rankProperty());
        nodeDependenciesColumn.setCellValueFactory(cellData -> cellData.getValue().dependenciesProperty());
        nodeCommunicationsLengthsColumn.setCellValueFactory(cellData -> cellData.getValue().communicationsLengthsProperty());
    }

    //зв'язування з головним класом
    public void setMain(Main main) {
        this.main = main;
        graphParametersTable.setItems(main.getNodes());
    }

    /**
     * Викликається при натисканні на кнопку Delete
     * Видаляє обраний запис з таблиці
     */
    @FXML
    private void handleDeleteNode() {
        int selectedIndex = graphParametersTable.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            graphParametersTable.getItems().remove(selectedIndex);
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.initOwner(main.getPrimaryStage());
            alert.setTitle("No Selection");
            alert.setHeaderText("No Node Selected");
            alert.setContentText("Please select a Node in the table.");

            alert.showAndWait();
        }
    }

    /**
     * Викликається при натисканні на кнопку New
     * Відкриває діалогове вікно редагування вершини
     */
    @FXML
    private void handleAddNode() {
        SimpleNodeProperty tempNode = new SimpleNodeProperty();
        boolean okClicked = main.showNodeEditDialog(tempNode);
        if (okClicked) {
            main.getNodes().add(tempNode);
        }
    }

    /**
     * Викликається при натисканні кнопки Edit
     * Відкриває діалогове вікно редагування вершини
     */
    @FXML
    private void handleEditNode() {
        SimpleNodeProperty selectedNode = graphParametersTable.getSelectionModel().getSelectedItem();
        if (selectedNode != null) {
            boolean applyClicked = main.showNodeEditDialog(selectedNode);
        } else {
            // Нічого не обрано
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.initOwner(main.getPrimaryStage());
            alert.setTitle("No Selection");
            alert.setHeaderText("No Node Selected");
            alert.setContentText("Please select a node in the table.");

            alert.showAndWait();
        }
    }

    // Натискання кнопки декрементації кількості процесорів
    @FXML
    private void handleProcessorsCountIncButton() {
        Integer pc = Integer.valueOf(processorsCountField.getText());
        pc++;
        processorsCountField.setText(pc.toString());
    }

    // Натискання кнопки інкрементації кількості процесорів
    @FXML
    private void handleProcessorsCountDecButton() {
        Integer pc = Integer.valueOf(processorsCountField.getText());
        pc--;
        processorsCountField.setText(pc.toString());
    }

    // Викликається коли обрано панель редагування системних параметрів
    @FXML
    private void systemParametersTabChanged() {
        try {
            repeatButton.setDefaultButton(false);
            nodeAddButton.setDefaultButton(false);
            applyButton.setDefaultButton(true);
            // Назначаємо швидкі кнопки
            tabPane.setFocusTraversable(true);
            tabPane.setOnKeyPressed(new EventHandler<KeyEvent>() {
                @Override
                public void handle(KeyEvent t) {
                    if (t.getCode() == KeyCode.D) {
                        handleProcessorsCountDecButton();
                    } else if (t.getCode() == KeyCode.I){
                        handleProcessorsCountIncButton();
                    }
                }
            });
        } catch (Exception e) {};
    }

    // Викликається коли обрано панель редагування параметрів графу
    @FXML
    private void graphParametersTabChanged() {
        try {
            applyButton.setDefaultButton(false);
            repeatButton.setDefaultButton(false);
            nodeAddButton.setDefaultButton(true);
            // Аби швидкі кнопки не спрацьовували
            tabPane.setOnKeyPressed(new EventHandler<KeyEvent>() {
                @Override
                public void handle(KeyEvent t) { }
            });
        } catch (Exception e) {};

    }

    // Викликається коли обрано панель відображення результатів
    @FXML
    private void immersionResultsTabChanged() {
        try {
            nodeAddButton.setDefaultButton(false);
            applyButton.setDefaultButton(false);
            repeatButton.setDefaultButton(true);
            // Аби швидкі кнопки не спрацьовували
            tabPane.setOnKeyPressed(new EventHandler<KeyEvent>() {
                @Override
                public void handle(KeyEvent t) { }
            });
        } catch (Exception e) {};

        // Перевіряємо коректність вводу
        String errorMessage = "";
        try {
            Integer pc = Integer.valueOf(processorsCountField.getText());
            if (pc <= 0) {
                errorMessage += "Processors count must be greater then 0!";
            }
        }
        catch (NumberFormatException e){
                errorMessage += "No valid processors count!\n";
        }

        if (errorMessage.length() != 0) {
            tabPane.getSelectionModel().select(systemParametersTab);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(main.getPrimaryStage());
            alert.setTitle("Invalid Fields");
            alert.setHeaderText("Please correct invalid fields");
            alert.setContentText(errorMessage);

            alert.showAndWait();
        } else {
            //зберігаємо кількість процесорів
            main.setProcessorsCount(Integer.valueOf(processorsCountField.getText()));

            //отримуємо з головного додатку план погруження
            ArrayList<String[]> plan = main.getPlan();

            //відображаємо панель для результатів
            immersionResultsContainer.setVisible(true);

            //очищаємо від попередніх результатів
            immersionResultsPane.getChildren().clear();

            labelsWidth = 1030 / (plan.size() + 1);

            //нумеруємо колонки
            for (int i = 0; i < plan.size() - 1; i++){
                ResultLabel label = new ResultLabel(
                        Integer.toString(i),
                        "#F5F5F5",
                        true,
                        labelsHeight,
                        labelsWidth);
                immersionResultsPane.add(label, i+1, 0);
            }
            //в т.ч. останню для шини
            immersionResultsPane.add(new ResultLabel("BUS","#F5F5F5", true,labelsHeight,labelsWidth), plan.size(), 0);

            //нумеруємо рядки по тактах
            for (int i = 0; i < Supervisor.getExecuteTime(); i++){
                ResultLabel label = new ResultLabel(
                        Integer.toString(i),
                        "#F5F5F5",
                        true,
                        labelsHeight,
                        labelsWidth);
                immersionResultsPane.add(label, 0, i+1);
            }

            //записуємо стани тактів
            for (int i = 0; i < plan.size(); i++){
                for (int j = 0; j < Supervisor.getExecuteTime(); j++){
                    ResultLabel label = new ResultLabel(plan.get(i)[j].toString(), labelsHeight, labelsWidth);
                    if (plan.get(i)[j] == " "){
                        //пустий такт
                        label.setColor("#edece8");
                    } else {
                        try {
                            //робочий такт
                            Integer.valueOf(plan.get(i)[j]);
                            label.setColor("#ccff00");
                        } catch (Exception e) {
                            //пересилка даних
                            label.setColor("#4FC3F7");
                        }
                    }
                    immersionResultsPane.add(label, i+1, j+1);
                }
            }
            //верхня ліва вершина - пуста
            immersionResultsPane.add(new ResultLabel(" ",  "#C8C8C8",false,labelsHeight, labelsWidth), 0,0);

            //красивості
            busAdvisorContainer.setVisible(true);
            busAdvisorLabel.setVisible(true);
            busAdvisorLabel.toFront();
            //TODO: change this text if you want
            busAdvisorLabel.setText("Пушка :) ");//\nIt was so wonderful!\nPress 'Repeat' and we will do it again!");
        }
    }

    @FXML
    private void handleSaveImageButton() {
        //конвертуємо розміри щоб отримане зображення зручно вставлялось в документацію
        for (int i = 0; i < immersionResultsPane.getChildren().size(); i++) {
            ResultLabel buffer = (ResultLabel) immersionResultsPane.getChildren().get(i);
            buffer.setSize(20, 60);
        }

        Image snapshot = immersionResultsPane.snapshot( new SnapshotParameters(), null);

        //повертаємо попередні розміри
        for (int i = 0; i < immersionResultsPane.getChildren().size(); i++){
            ResultLabel buffer = (ResultLabel) immersionResultsPane.getChildren().get(i);
            buffer.setSize(labelsHeight, labelsWidth);
        }


        FileChooser fileChooser = new FileChooser();

        // Задаємо фільтр розширень
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                "PNG files (*.png)", "*.png");
        fileChooser.getExtensionFilters().add(extFilter);

        // Показуємо діалог збереження файлу
        File file = fileChooser.showSaveDialog(main.getPrimaryStage());

        if (file != null) {
            // Перевіримо чи правильне розширення
            if (!file.getPath().endsWith(".png")) {
                file = new File(file.getPath() + ".png");
            }
            //Зберігаємо зображення
            try {
                ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", file);
                busAdvisorLabel.setText("It was so wonderful!\nPress 'Repeat' and we will do it again!");
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Could not save results");
                alert.setContentText("Could not save results to file:\n" + file.getPath());
                e.printStackTrace();

                alert.showAndWait();
            }
        }
    }

    @FXML
    private void handleApplyButton() throws InterruptedException { tabPane.getSelectionModel().select(immersionResultsTab); }

    @FXML
    private void handleBackButton() {
        tabPane.getSelectionModel().select(graphParametersTab);
    }

    @FXML
    private void handleRepeatButton() {
        tabPane.getSelectionModel().select(graphParametersTab);
    }

    //службовий клас для редагування записів в таблиці за подвійним кліком
    class EditingCell extends TableCell<SimpleNodeProperty, String> {

        private TextField textField;
        private int columnNumber;

        public EditingCell(int columnNumber) {
            this.columnNumber = columnNumber;
        }

        @Override
        public void startEdit() {
            super.startEdit();

            if (textField == null) {
                createTextField();
            }
            textField.setAlignment(Pos.CENTER);
            setGraphic(textField);
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            textField.selectAll();
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();

            setText(String.valueOf(getItem()));
            setContentDisplay(ContentDisplay.TEXT_ONLY);
        }

        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);

            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
                if (isEditing()) {
                    if (textField != null) {
                        textField.setText(getString());
                    }
                    setGraphic(textField);
                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                } else {
                    setText(getString());
                    setContentDisplay(ContentDisplay.TEXT_ONLY);
                }
            }
        }

        private void createTextField() {
            textField = new TextField(getString());
            textField.setMinWidth(this.getWidth() - this.getGraphicTextGap()*2);

            textField.setOnKeyPressed(new EventHandler<KeyEvent>() {

            @Override
            public void handle(KeyEvent t) {

                if (t.getCode() == KeyCode.ENTER) {
                    if (isInputValid(columnNumber, textField.getText()))
                        commitEdit(textField.getText());
                } else if (t.getCode() == KeyCode.ESCAPE) {
                    cancelEdit();
                }
            }
            });
        }

        private boolean isInputValid(int columnNumber, String input) {
            String errorMessage = "";
            if (columnNumber == 0){
                try {
                    int buffer = Integer.valueOf(input);
                    if (buffer < 0){
                        errorMessage += "Node ID must be natural number!\n";
                    }
                    for(int i = 0; i < main.getNodes().size(); i++) {
                        if (main.getNodes().get(i).getId() == input)
                            errorMessage += "Node with this ID already created!\n";
                        break;
                    }
                }
                catch (NumberFormatException e){
                    errorMessage += "No valid node ID!\n";
                }
            } else if (columnNumber == 1) {
                try {
                    int buffer = Integer.valueOf(input);
                    if (buffer <= 0){
                        errorMessage += "Rank must be greater than 0!\n";
                    }
                }
                catch (NumberFormatException e){
                    errorMessage += "No valid node rank!\n";
                }
            }

            if (errorMessage.length() == 0) {
                return true;
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.initOwner(main.getPrimaryStage());
                alert.setTitle("Invalid Fields");
                alert.setHeaderText("Please correct invalid fields");
                alert.setContentText(errorMessage);

                alert.showAndWait();

                return false;
            }
        }

        private String getString() {
            if (getItem() == null){
                return "";
            }else{
                return getItem() == "" ? "" : getItem().toString();
            }
        }
    }
}
