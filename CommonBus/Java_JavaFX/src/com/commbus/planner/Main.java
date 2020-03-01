package com.commbus.planner;

import com.commbus.planner.model.Node;
import com.commbus.planner.model.NodeListWrapper;
import com.commbus.planner.model.SimpleNodeProperty;
import com.commbus.planner.model.Supervisor;
import com.commbus.planner.view.MainViewController;
import com.commbus.planner.view.NodeEditDialogController;
import com.commbus.planner.view.RootLayoutController;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.prefs.Preferences;


/**
 * Created by KirinTor on 20.12.2017.
 */
public class Main extends Application {

    private Stage primaryStage;
    private BorderPane rootLayout;
    private ObservableList<SimpleNodeProperty> nodes = FXCollections.observableArrayList();
    private int processorsCount = 0;

    public Main() { }

    public ObservableList<SimpleNodeProperty> getNodes() {
        return nodes;
    }
    public int getProcessorsCount() {return processorsCount; }
    public void setProcessorsCount(int value) {processorsCount = value; }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Common Bus");
        this.primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/resources/images/Icon.png")));

        initRootLayout();

        showMainView();
    }

    /**
     * Ініціалізує кореневий макет.
     */
    public void initRootLayout() {
        try {
            // Завантажуємо кореневий макет із fxml файлу.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class
                    .getResource("view/RootLayout.fxml"));
            rootLayout = (BorderPane) loader.load();

            // Відоображаємо сцену, яка містить кореневий макет
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);

            // Надаємо контролеру доступ до головного додатку
            RootLayoutController controller = loader.getController();
            controller.setMain(this);

            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Намагаємося завантажити останній файл з готовим графом
        File file = getGraphFilePath();
        if (file != null) {
            loadGraphNodesFromFile(file);
        }
    }

    /**
     * Відображаємо основний макет на відповідному місці в кореневому макеті
     */
    public void showMainView() {
        try {
            // Загружаємо макет
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("view/MainView.fxml"));
            AnchorPane mainView = (AnchorPane) loader.load();

            MainViewController controller = loader.getController();
            controller.setMain(this);

            // Встановлюємо в центр
            rootLayout.setCenter(mainView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Відповідає за логіку
     *
     * @return набір масивів станів кожного елементу системи на кожному такті
     */
    public ArrayList<String[]> getPlan() {
        //підключаємо класи логіки
        Supervisor supervisor = new Supervisor(nodes, processorsCount);

        //отримуємо план погруження
        return supervisor.getPlan();
    }


    /**
     * Відкриває діалогове вікно редагування обраної вершини
     * При натисканні "Apply" оновлені дані зберігаються за наданою в параметрах адресою
     * і повертається значення true.
     *
     * @param node - ою'єкт вершини, яку необхідно відредагувати
     * @return true, якщо натиснуто Apply, в протилежному випадку false.
     */
    public boolean showNodeEditDialog(SimpleNodeProperty node) {
        try {
            // Загружаємо fxml-файл і створюємо нову сцену
            // для діалогового вікна
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("view/NodeEditDialog.fxml"));
            AnchorPane page = (AnchorPane) loader.load();

            // Створюємо діалогове вікно Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit Node");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Передаємо в контролер
            NodeEditDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setNode(node);
            controller.setMain(this);

            // Відображаємо і чекаємо доки користувач його не закриє
            dialogStage.showAndWait();

            return controller.isApplyClicked();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Повертає preference файлу зі збереженим графом, тобто останній відкритий файл.
     * Цей preference зчитується з реєстру.
     * Якщо preference не було знайдено, то повертається null.
     *
     * @return
     */
    public File getGraphFilePath() {
        Preferences prefs = Preferences.userNodeForPackage(Main.class);
        String filePath = prefs.get("filePath", null);
        if (filePath != null) {
            return new File(filePath);
        } else {
            return null;
        }
    }

    /**
     * Задає шлях поточному завантаженому файлу.
     * Цей шлях зберігається в реєстрі
     *
     * @param file - файл або null, щоб видалити шлях
     */
    public void setGraphFilePath(File file) {
        Preferences prefs = Preferences.userNodeForPackage(Main.class);
        if (file != null) {
            prefs.put("filePath", file.getPath());

            // Оновляємо заголовок сцени
            primaryStage.setTitle("Common Bus - " + file.getName());
        } else {
            prefs.remove("filePath");

            // Оновляємо заголовок сцени
            primaryStage.setTitle("Common Bus");
        }
    }

    /**
     * Завантжуємо інформацію про вершини графу зі вказаного файлу.
     * Поточну інформацію буде замінено.
     *
     * @param file
     */
    public void loadGraphNodesFromFile(File file) {
        try {
            JAXBContext context = JAXBContext
                    .newInstance(NodeListWrapper.class);
            Unmarshaller um = context.createUnmarshaller();

            // Читання XML з файлу та демаршалізація.
            NodeListWrapper wrapper = (NodeListWrapper) um.unmarshal(file);

            nodes.clear();
            nodes.addAll(wrapper.getNodes());

            // Зберігаємо шлях до файлу в реєстрі.
            setGraphFilePath(file);

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Could not load data");
            alert.setContentText("Could not load data from file:\n" + file.getPath());
            e.printStackTrace();

            alert.showAndWait();
        }
    }

    /**
     * Зберігаємо поточну інформацію про вершини графу в вказаному файлі
     *
     * @param file
     */
    public void saveGraphNodesToFile(File file) {
        try {
            JAXBContext context = JAXBContext
                    .newInstance(NodeListWrapper.class);
            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            // Згортаємо дані про вершини
            NodeListWrapper wrapper = new NodeListWrapper();
            wrapper.setNodes(nodes);

            // Маршалуємо і зберігаємо XML в файл.
            m.marshal(wrapper, file);

            // Зберігаємо шлях до файлу в реєстрі.
            setGraphFilePath(file);
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Could not save data");
            alert.setContentText("Could not save data to file:\n" + file.getPath());
            e.printStackTrace();

            alert.showAndWait();
        }
    }

    /**
     * Повертає головну сцену.
     * @return
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
