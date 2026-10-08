package application;
import javafx.scene.Scene; import javafx.stage.Stage;
public class AppRouter { private final Stage stage; public AppRouter(Stage stage){this.stage=stage;} public void show(Scene scene){stage.setScene(scene);stage.show();} }