package org.unisoftware.gestioncurricular.frontend.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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

            List<String> roles = SessionManager.getInstance().getUserRoles();
            String rolesStr = (roles != null && !roles.isEmpty())
                    ? String.join(", ", roles)
                    : "Sin roles";

            // Icono, puede ser emoji o FontAwesome/SVG si tienes librería
            Label iconLbl = new Label("👤");
            iconLbl.setStyle("-fx-font-size: 22px;");

            Label userLbl = new Label(username != null ? username : "");
            userLbl.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

            Label rolLbl = new Label("(" + rolesStr + ")");
            rolLbl.setStyle("-fx-font-size: 14px; -fx-text-fill: #4A4A4A; -fx-padding: 0 0 0 7;");

            HBox userInfoHBox = new HBox(10, iconLbl, userLbl, rolLbl);
            userInfoHBox.setAlignment(Pos.CENTER_LEFT);

            userBox.getChildren().add(userInfoHBox);

        } catch (Exception e) {
            userBox.getChildren().add(new Label("Error al obtener información de usuario: " + e.getMessage()));
        }
    }


    private void mostrarProgramaCard() {
        cardContainer.getChildren().clear();
        try {
            List<ProgramDTO> lista = programServiceFront.listPrograms();

            if (!lista.isEmpty()) {
                for (ProgramDTO prog : lista) {
                    VBox card = new VBox(12);
                    card.setStyle(
                            "-fx-background-color: #ffffff; -fx-background-radius: 12; -fx-padding: 18 18 18 18; "
                                    + "-fx-effect: dropshadow(gaussian, #bbb, 3,0,0,1);"
                                    + "-fx-border-color: #d32f2f; -fx-border-width: 0 0 3 0;"
                    );
                    card.setPadding(new Insets(20, 24, 20, 24));
                    card.setMaxWidth(Double.MAX_VALUE);
                    card.setSpacing(10);

                    Label nameLbl = new Label(prog.getName());
                    nameLbl.setWrapText(true);
                    nameLbl.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #d32f2f;");
                    nameLbl.setMaxWidth(Double.MAX_VALUE);

                    Button expandBtn = new Button("Ver más detalles");
                    Button goToCursosBtn = new Button("Ver Cursos del Programa");
                    expandBtn.setWrapText(true);
                    goToCursosBtn.setWrapText(true);
                    expandBtn.setMaxWidth(Double.MAX_VALUE);
                    goToCursosBtn.setMaxWidth(Double.MAX_VALUE);

                    VBox datosBox = new VBox(10);
                    datosBox.setVisible(false);
                    datosBox.setManaged(false); // <- Importante para ocultar layout

                    // SUB-TÍTULOS EN SU PROPIO RENGLÓN, INFORMACIÓN DEBAJO:
                    datosBox.getChildren().addAll(
                            infoRow("Título otorgado:", prog.getAwardingDegree()),
                            infoRow("Perfil profesional:", prog.getProfessionalProfile()),
                            infoRow("Perfil ocupacional:", prog.getOccupationalProfile()),
                            infoRow("Perfil de ingreso:", prog.getAdmissionProfile()),
                            infoRow("Competencias:", prog.getCompetencies()),
                            infoRow("Duración semestres:", prog.getDuration()!=null ? prog.getDuration().toString() : ""),
                            infoRow("Resultados Aprendizaje FileID:", prog.getLearningOutcomesFileId() != null ? prog.getLearningOutcomesFileId().toString() : "")
                    );

                    // EXPAND/COLLAPSE FUNCIONALIDAD!
                    expandBtn.setOnAction(e -> {
                        boolean showing = datosBox.isVisible();
                        datosBox.setVisible(!showing);
                        datosBox.setManaged(!showing);
                        expandBtn.setText(!showing ? "Ocultar detalles" : "Ver más detalles");
                    });

                    goToCursosBtn.setOnAction(e -> abrirCursosPrograma(prog.getId(), prog.getName()));

                    HBox botones = new HBox(16, expandBtn, goToCursosBtn);
                    botones.setAlignment(Pos.CENTER_LEFT);
                    botones.setSpacing(10);

                    if (SessionManager.getInstance().hasRole("DIRECTOR_DE_PROGRAMA")) {
                        Button uploadBtn = new Button("Subir plan Excel");
                        uploadBtn.setWrapText(true);
                        uploadBtn.setMaxWidth(Double.MAX_VALUE);
                        uploadBtn.setOnAction(ev -> handleSubirExcel(prog.getId()));
                        botones.getChildren().add(uploadBtn);
                    }

                    for (Node n : botones.getChildren()) {
                        if (n instanceof Button b) {
                            HBox.setHgrow(b, javafx.scene.layout.Priority.ALWAYS);
                            b.setMinWidth(145);
                            b.setMaxWidth(Double.MAX_VALUE);
                        }
                    }

                    card.getChildren().addAll(nameLbl, botones, datosBox);
                    card.setFillWidth(true);
                    VBox.setVgrow(card, javafx.scene.layout.Priority.ALWAYS);

                    cardContainer.getChildren().add(card);
                }
            } else {
                cardContainer.getChildren().add(new Label("No hay programas disponibles."));
            }
        } catch (Exception e) {
            cardContainer.getChildren().add(new Label("Error al cargar los programas: " + e.getMessage()));
        }
    }

    // NUEVO: Crea subtítulo y valor EN DISTINTOS RENGLONES
    private VBox infoRow(String subtitulo, String valor) {
        Label subtitleLbl = new Label(subtitulo);
        subtitleLbl.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #d32f2f;");
        subtitleLbl.setWrapText(true);
        subtitleLbl.setMaxWidth(Double.MAX_VALUE);

        Label valorLbl = new Label(valor != null ? valor : "");
        valorLbl.setStyle("-fx-font-size: 15px; -fx-text-fill: #333; -fx-padding: 0 0 8 0;");
        valorLbl.setWrapText(true);
        valorLbl.setMaxWidth(Double.MAX_VALUE);

        VBox vbox = new VBox(0, subtitleLbl, valorLbl);
        vbox.setMaxWidth(Double.MAX_VALUE);
        return vbox;
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
                new FileChooser.ExtensionFilter("Archivos Excel", "*.xlsx", "*.xls", "*.csv"));
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