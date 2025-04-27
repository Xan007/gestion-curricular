package org.unisoftware.gestioncurricular.frontend;
import org.unisoftware.gestioncurricular.GestionCurricularApplication;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import java.util.Arrays;

public class JavaFXApplication extends Application {

    private ConfigurableApplicationContext applicationContext;

    @Override
    public void init() {
        try {
            // Inicializar el contexto de Spring
            applicationContext = new SpringApplicationBuilder(GestionCurricularApplication.class)
                    .headless(false) // Importante para aplicaciones con UI
                    .run(getParameters().getRaw().toArray(new String[0]));
        } catch (Exception e) {
            System.err.println("Error inicializando la aplicación: " + e.getMessage());
            e.printStackTrace();
            // No se propaga la excepción para permitir que JavaFX siga intentándolo
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            // Configurar el cargador FXML con el contexto de Spring
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            loader.setControllerFactory(applicationContext::getBean);

            // Cargar la vista y configurar la escena
            Parent root = loader.load();
            Scene scene = new Scene(root);

            try {
                scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            } catch (Exception e) {
                System.err.println("No se pudo cargar la hoja de estilos: " + e.getMessage());
            }

            primaryStage.getIcons().add(
                    new javafx.scene.image.Image(getClass().getResourceAsStream("/img/icono.png"))
            );


            primaryStage.setScene(scene);
            primaryStage.setTitle("Iniciar Sesión - Gestión Curricular");
            primaryStage.setMinWidth(600);
            primaryStage.setMinHeight(400);
            primaryStage.show();

            // Ya no se asigna el Stage al SessionManager, no es necesario ni correcto

        } catch (Exception e) {
            System.err.println("Error iniciando la aplicación JavaFX: " + e.getMessage());
            e.printStackTrace();
            throw e; // Relanzar para que JavaFX muestre el error
        }
    }

    @Override
    public void stop() {
        try {
            // Limpiar la sesión al salir
            if (applicationContext != null) {
                applicationContext.close();
            }
            Platform.exit();
        } catch (Exception e) {
            System.err.println("Error cerrando la aplicación: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        boolean hasModulePath = Arrays.stream(args).anyMatch(arg -> arg.contains("--module-path"));
        if (!hasModulePath) {
            System.out.println("Iniciando aplicación JavaFX...");
        }
        launch(args);
    }
}