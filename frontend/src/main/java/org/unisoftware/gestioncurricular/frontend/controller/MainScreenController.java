package org.unisoftware.gestioncurricular.frontend.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.unisoftware.gestioncurricular.frontend.dto.ProgramDTO;
import org.unisoftware.gestioncurricular.frontend.service.ProgramServiceFront;
import org.unisoftware.gestioncurricular.frontend.service.ExcelUploadService;
import org.unisoftware.gestioncurricular.frontend.util.SessionManager;
import org.unisoftware.gestioncurricular.frontend.util.JwtDecodeUtil;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

@Component
public class MainScreenController implements Initializable {

    @FXML private VBox cardContainer;
    @FXML private VBox userBox; // ¡Agrega este VBox en tu FXML al inicio de la pantalla!

    @Autowired private ProgramServiceFront programServiceFront;
    @Autowired private ExcelUploadService excelUploadService;
    @Autowired private ApplicationContext applicationContext;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cargarUsuarioYMostrar();
        mostrarProgramaCard();
    }

    // Ahora usando JwtDecodeUtil para extraer nombre y roles del token
    private void cargarUsuarioYMostrar() {
        userBox.getChildren().clear();
        try {
            String token = SessionManager.getInstance().getToken();
            String username = JwtDecodeUtil.getUsername(token);

            // Obtener directamente los roles cargados en SessionManager
            List<String> roles = SessionManager.getInstance().getUserRoles();

            Label userLbl = new Label("Usuario: " + (username != null ? username : ""));
            userLbl.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

            // Esta línea convierte la lista de roles a texto separado por comas:
            String rolesStr = (roles != null && !roles.isEmpty())
                    ? String.join(", ", roles)
                    : "Sin roles";
            Label rolesLbl = new Label("Roles: " + rolesStr);
            rolesLbl.setStyle("-fx-font-size: 13px;");

            userBox.getChildren().addAll(userLbl, rolesLbl);

        } catch (Exception e) {
            userBox.getChildren().add(new Label("Error al obtener información de usuario: " + e.getMessage()));
        }
    }

    private void mostrarProgramaCard() {
        cardContainer.getChildren().clear();
        try {
            List<ProgramDTO> lista = programServiceFront.listPrograms();
            if (!lista.isEmpty()) {
                ProgramDTO prog = lista.get(0); // Solo el primero

                VBox card = new VBox(8);
                card.setStyle("-fx-background-color: #efefef; -fx-background-radius: 12; -fx-padding: 22;");
                card.setPadding(new Insets(16));
                card.setMaxWidth(580);

                Label nameLbl = new Label(prog.getName());
                nameLbl.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

                Button expandBtn = new Button("Ver más detalles");
                VBox datosBox = new VBox(4);
                datosBox.setVisible(false);

                expandBtn.setOnAction(e -> datosBox.setVisible(!datosBox.isVisible()));

                datosBox.getChildren().addAll(
                        infoLabel("Título otorgado: ", prog.getAwardingDegree()),
                        infoLabel("Perfil profesional: ", prog.getProfessionalProfile()),
                        infoLabel("Perfil ocupacional: ", prog.getOccupationalProfile()),
                        infoLabel("Perfil de ingreso: ", prog.getAdmissionProfile()),
                        infoLabel("Competencias: ", prog.getCompetencies()),
                        infoLabel("Duración semestres: ", prog.getDuration()!=null ? prog.getDuration().toString() : ""),
                        infoLabel("Resultados Aprendizaje FileID: ", prog.getLearningOutcomesFileId() != null ? prog.getLearningOutcomesFileId().toString() : "")
                );

                Button goToCursosBtn = new Button("Ver Cursos del Programa");
                goToCursosBtn.setOnAction(e -> abrirCursosPrograma(prog.getId(), prog.getName()));

                HBox botones = new HBox(16, expandBtn, goToCursosBtn);

                // Usar SessionManager para revisar roles
                if (SessionManager.getInstance().hasRole("DIRECTOR_DE_PROGRAMA")) {
                    Button uploadBtn = new Button("Subir plan Excel");
                    uploadBtn.setOnAction(e -> handleSubirExcel(prog.getId()));
                    botones.getChildren().add(uploadBtn);
                }

                card.getChildren().addAll(nameLbl, botones, datosBox);
                cardContainer.getChildren().add(card);
            } else {
                cardContainer.getChildren().add(new Label("No hay programas disponibles."));
            }
        } catch (Exception e) {
            cardContainer.getChildren().add(new Label("Error al cargar el programa: " + e.getMessage()));
        }
    }

    private HBox infoLabel(String label, String value) {
        Label l = new Label(label);
        l.setStyle("-fx-font-weight: bold;");
        Text v = new Text(value != null ? value : "");
        HBox h = new HBox(5, l, v);
        h.setMaxWidth(500);
        return h;
    }

    private void abrirCursosPrograma(Long programaId, String nombrePrograma) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ProgramCoursesScreen.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();
            ProgramCoursesScreenController controller = loader.getController();
            controller.initData(programaId, nombrePrograma);

            Stage stage = (Stage) cardContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Cursos de " + nombrePrograma);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleSubirExcel(Long programId) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecciona el archivo Excel del plan de estudios");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Archivos Excel", "*.xlsx", "*.xls"));
        Stage stage = (Stage) cardContainer.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            try {
                excelUploadService.uploadPlan(programId, selectedFile);
                mostrarAlerta("Éxito", "Archivo subido correctamente.", Alert.AlertType.INFORMATION);
            } catch (Exception ex) {
                mostrarAlerta("Error", "No se pudo subir el archivo: " + ex.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent loginView = loader.load();
            Scene loginScene = new Scene(loginView);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            double ancho = stage.getWidth();
            double alto = stage.getHeight();

            stage.setScene(loginScene);
            stage.setWidth(ancho);
            stage.setHeight(alto);
            stage.setTitle("Iniciar Sesión");
            stage.show();
            SessionManager.getInstance().clearSession();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }}