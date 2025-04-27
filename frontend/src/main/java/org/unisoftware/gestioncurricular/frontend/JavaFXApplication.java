package org.unisoftware.gestioncurricular.frontend;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.unisoftware.gestioncurricular.GestionCurricularApplication;
import org.unisoftware.gestioncurricular.frontend.util.SessionManager;

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
                    
            // Inicializar el SessionManager
            SessionManager.initialize();
        } catch (Exception e) {
            System.err.println("Error inicializando la aplicación: " + e.getMessage());
            e.printStackTrace();
            // No propagar la excepción, permitirá que la aplicación siga intentando iniciarse
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            // Configurar el cargador FXML con el contexto de Spring
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            loader.setControllerFactory(applicationContext::getBean);

            // Cargar la vista de login como primera pantalla
            Parent root = loader.load();

            // Configurar la escena y mostrar la ventana
            Scene scene = new Scene(root);

            // Intentar cargar estilos CSS (con manejo de error)
            try {
                scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            } catch (Exception e) {
                System.err.println("No se pudo cargar la hoja de estilos: " + e.getMessage());
            }

            primaryStage.setScene(scene);
            primaryStage.setTitle("Iniciar Sesión - Gestión Curricular");
            primaryStage.setMinWidth(600);
            primaryStage.setMinHeight(400);
            primaryStage.show();

            // Guardar referencia al escenario principal para acceso global
            if (SessionManager.getInstance() != null) {
                SessionManager.getInstance().setPrimaryStage(primaryStage);
            }
        } catch (Exception e) {
            System.err.println("Error iniciando la aplicación JavaFX: " + e.getMessage());
            e.printStackTrace();
            throw e; // Relanzar para que muestre el error
        }
    }

    @Override
    public void stop() {
        try {
            // Limpiar la sesión
            if (SessionManager.getInstance() != null) {
                SessionManager.getInstance().clearSession();
            }
            
            // Cerrar el contexto de Spring al salir
            if (applicationContext != null) {
                applicationContext.close();
            }
            
            Platform.exit();
        } catch (Exception e) {
            System.err.println("Error cerrando la aplicación: " + e.getMessage());
        }
    }

    /**
     * Punto de entrada principal
     * Añade argumentos específicos para módulos JavaFX si es necesario
     */
    public static void main(String[] args) {
        // Verificamos si hay argumentos necesarios para JavaFX y los añadimos si no están
        boolean hasModulePath = Arrays.stream(args).anyMatch(arg -> arg.contains("--module-path"));
        
        if (!hasModulePath) {
            System.out.println("Iniciando aplicación JavaFX...");
        }
        
        launch(args);
    }
}