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
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.GridPane;
import javafx.scene.control.ScrollPane;

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

    @FXML private Button adminPlantelBtn;
    @FXML private VBox cardContainer;
    @FXML private VBox userBox; // ¡Agrega este VBox en tu FXML al inicio de la pantalla!
    @FXML private Button btnPropuestasComite;
    @FXML private Button btnPropuestasEscuela;
    @FXML private Button btnPropuestasPrograma;

    @Autowired private ProgramServiceFront programServiceFront;
    @Autowired private ExcelUploadService excelUploadService;
    @Autowired private ApplicationContext applicationContext;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cargarUsuarioYMostrar();
        mostrarProgramaCard();

        // Mostrar botón solo si el usuario es decano
        if (SessionManager.getInstance().hasRole("DECANO")) {
            adminPlantelBtn.setVisible(true);
            adminPlantelBtn.setManaged(true);
        } else {
            adminPlantelBtn.setVisible(false);
            adminPlantelBtn.setManaged(false);
        }

        // Mostrar botones de propuestas según rol
        if (SessionManager.getInstance().hasRole("COMITE_DE_PROGRAMA")) {
            btnPropuestasComite.setVisible(true);
            btnPropuestasComite.setManaged(true);
        }
        if (SessionManager.getInstance().hasRole("DIRECTOR_DE_ESCUELA")) {
            btnPropuestasEscuela.setVisible(true);
            btnPropuestasEscuela.setManaged(true);
        }
        if (SessionManager.getInstance().hasRole("DIRECTOR_DE_PROGRAMA")) {
            btnPropuestasPrograma.setVisible(true);
            btnPropuestasPrograma.setManaged(true);
        }

        adminPlantelBtn.setOnAction(e -> abrirAdministracionPlantel(e));
        btnPropuestasComite.setOnAction(e -> mostrarPropuestasMicro("Comité de Programa"));
        btnPropuestasEscuela.setOnAction(e -> mostrarPropuestasMicro("Escuela"));
        btnPropuestasPrograma.setOnAction(e -> mostrarPropuestasMicro("Programa"));
    }

    @FXML
    private void abrirAdministracionPlantel(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AdminPlantelScreen.fxml"));
            loader.setControllerFactory(applicationContext::getBean); // <-- Clave para integración Spring

            Parent view = loader.load();
            Scene scene = new Scene(view);
            Stage stage = new Stage();
            stage.setTitle("Administración de Plantel");
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            mostrarAlerta("Error", "No se pudo abrir administración de plantel.\n" + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }


    // Ahora usando JwtDecodeUtil para extraer nombre y roles del token
    private void cargarUsuarioYMostrar() {
        userBox.getChildren().clear();
        try {
            String token = SessionManager.getInstance().getToken();
            String username = JwtDecodeUtil.getUsername(token);

            String role = SessionManager.getInstance().getUserRole();
            String rolesStr = (role != null && !role.isEmpty())
                    ? role
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
                GridPane grid = new GridPane();
                grid.setHgap(32);
                grid.setVgap(32);
                grid.setAlignment(Pos.TOP_CENTER);
                int col = 0, row = 0;
                int maxCols = 2; // Solo dos programas por fila
                for (ProgramDTO prog : lista) {
                    VBox card = new VBox(18);
                    card.setStyle(
                        "-fx-background-color: linear-gradient(to bottom right, #fff, #f7f7fa 80%, #f1f1f1);" +
                        "-fx-background-radius: 18;" +
                        "-fx-padding: 28 24 28 24;" +
                        "-fx-effect: dropshadow(gaussian, #d32f2f44, 8,0,0,2);" +
                        "-fx-border-color: #d32f2f; -fx-border-width: 2; -fx-border-radius: 18;"
                    );
                    card.setMinWidth(370);
                    card.setMaxWidth(370);
                    card.setMinHeight(340);
                    card.setMaxHeight(340);
                    card.setSpacing(16);
                    card.setAlignment(Pos.TOP_LEFT);

                    Label nameLbl = new Label(prog.getName());
                    nameLbl.setWrapText(true);
                    nameLbl.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #d32f2f; -fx-padding: 0 0 8 0;");
                    nameLbl.setMaxWidth(Double.MAX_VALUE);

                    Button expandBtn = new Button("Ver información");
                    expandBtn.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: #fff; -fx-background-radius: 8; -fx-font-weight: bold; -fx-padding: 8 24 8 24; -fx-border-color: #b71c1c; -fx-border-width: 2; -fx-font-size: 15px;");
                    expandBtn.setWrapText(true);
                    expandBtn.setMaxWidth(Double.MAX_VALUE);
                    expandBtn.setOnAction(e -> mostrarDetallePrograma(prog));

                    Button goToCursosBtn = new Button("Ver Cursos del Programa");
                    goToCursosBtn.setStyle("-fx-background-color: #fff; -fx-text-fill: #d32f2f; -fx-background-radius: 8; -fx-font-weight: bold; -fx-padding: 8 24 8 24; -fx-border-color: #d32f2f; -fx-border-width: 2; -fx-font-size: 15px;");
                    goToCursosBtn.setWrapText(true);
                    goToCursosBtn.setMaxWidth(Double.MAX_VALUE);
                    goToCursosBtn.setOnAction(e -> abrirCursosPrograma(prog.getId(), prog.getName()));

                    // Botón solo para DIRECTOR_DE_PROGRAMA
                    Button actualizarBtn = null;
                    if (SessionManager.getInstance().hasRole("DIRECTOR_DE_PROGRAMA")) {
                        actualizarBtn = new Button("Actualizar plan de estudios");
                        actualizarBtn.setStyle("-fx-background-color: #fff; -fx-text-fill: #d32f2f; -fx-background-radius: 8; -fx-font-weight: bold; -fx-padding: 8 24 8 24; -fx-border-color: #d32f2f; -fx-border-width: 2; -fx-font-size: 15px;");
                        actualizarBtn.setWrapText(true);
                        actualizarBtn.setMaxWidth(Double.MAX_VALUE);
                        final Long progId = prog.getId();
                        actualizarBtn.setOnAction(e -> handleSubirExcel(progId));
                    }

                    card.getChildren().addAll(nameLbl, expandBtn, goToCursosBtn);
                    if (actualizarBtn != null) card.getChildren().add(actualizarBtn);
                    grid.add(card, col, row);
                    col++;
                    if (col >= maxCols) {
                        col = 0;
                        row++;
                    }
                }
                // ScrollPane para grid de programas, con drag
                ScrollPane gridScroll = new ScrollPane(grid);
                gridScroll.setFitToWidth(true);
                gridScroll.setFitToHeight(true);
                gridScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
                gridScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
                gridScroll.setPannable(true); // Permite arrastrar con el mouse
                // Mejorar experiencia de drag
                gridScroll.setOnMousePressed(e -> {
                    gridScroll.setUserData(new double[]{e.getSceneX(), e.getSceneY(), gridScroll.getHvalue(), gridScroll.getVvalue()});
                });
                gridScroll.setOnMouseDragged(e -> {
                    double[] data = (double[]) gridScroll.getUserData();
                    double deltaX = e.getSceneX() - data[0];
                    double deltaY = e.getSceneY() - data[1];
                    // Ajustar la sensibilidad del drag
                    gridScroll.setHvalue(data[2] - deltaX / 1000);
                    gridScroll.setVvalue(data[3] - deltaY / 1000);
                });
                cardContainer.getChildren().add(gridScroll);
            } else {
                cardContainer.getChildren().add(new Label("No hay programas disponibles."));
            }
        } catch (Exception e) {
            cardContainer.getChildren().add(new Label("Error al cargar los programas: " + e.getMessage()));
        }
    }

    private void mostrarDetallePrograma(ProgramDTO prog) {
        VBox modalContent = new VBox(18);
        modalContent.setStyle("-fx-background-color: #fff; -fx-padding: 32; -fx-background-radius: 14; -fx-effect: dropshadow(three-pass-box, #d32f2f, 12, 0.18, 0, 4); -fx-border-color: #d32f2f; -fx-border-width: 3;");
        modalContent.setPrefWidth(600);
        modalContent.setMinWidth(400);
        modalContent.setAlignment(Pos.TOP_LEFT);

        Label title = new Label("Información del Programa");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1a2233; -fx-padding: 0 0 10 0;");

        // Subtítulos en negrilla y datos con espacio
        Label nombreLabel = new Label("Nombre:");
        nombreLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 15px;");
        Label nombre = new Label(prog.getName() != null ? prog.getName() : "");
        nombre.setWrapText(true);
        nombre.setStyle("-fx-font-size: 15px; -fx-padding: 0 0 10 0;");
        nombre.setMaxWidth(550);
        nombre.setMinWidth(0);
        nombre.setPrefWidth(550);

        Label gradoLabel = new Label("Título otorgado:");
        gradoLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 15px;");
        Label grado = new Label(prog.getAwardingDegree() != null ? prog.getAwardingDegree() : "");
        grado.setWrapText(true);
        grado.setStyle("-fx-font-size: 15px; -fx-padding: 0 0 10 0;");
        grado.setMaxWidth(550);
        grado.setMinWidth(0);
        grado.setPrefWidth(550);

        Label perfilProfLabel = new Label("Perfil profesional:");
        perfilProfLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 15px;");
        Label perfilProf = new Label(prog.getProfessionalProfile() != null ? prog.getProfessionalProfile() : "");
        perfilProf.setWrapText(true);
        perfilProf.setStyle("-fx-font-size: 15px; -fx-padding: 0 0 10 0;");
        perfilProf.setMaxWidth(550);
        perfilProf.setMinWidth(0);
        perfilProf.setPrefWidth(550);

        Label perfilOcupLabel = new Label("Perfil ocupacional:");
        perfilOcupLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 15px;");
        Label perfilOcup = new Label(prog.getOccupationalProfile() != null ? prog.getOccupationalProfile() : "");
        perfilOcup.setWrapText(true);
        perfilOcup.setStyle("-fx-font-size: 15px; -fx-padding: 0 0 10 0;");
        perfilOcup.setMaxWidth(550);
        perfilOcup.setMinWidth(0);
        perfilOcup.setPrefWidth(550);

        Label perfilIngresoLabel = new Label("Perfil de ingreso:");
        perfilIngresoLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 15px;");
        Label perfilIngreso = new Label(prog.getAdmissionProfile() != null ? prog.getAdmissionProfile() : "");
        perfilIngreso.setWrapText(true);
        perfilIngreso.setStyle("-fx-font-size: 15px; -fx-padding: 0 0 10 0;");
        perfilIngreso.setMaxWidth(550);
        perfilIngreso.setMinWidth(0);
        perfilIngreso.setPrefWidth(550);

        Label competenciasLabel = new Label("Competencias:");
        competenciasLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 15px;");
        Label competencias = new Label(prog.getCompetencies() != null ? prog.getCompetencies() : "");
        competencias.setWrapText(true);
        competencias.setStyle("-fx-font-size: 15px; -fx-padding: 0 0 10 0;");
        competencias.setMaxWidth(550);
        competencias.setMinWidth(0);
        competencias.setPrefWidth(550);

        Label duracionLabel = new Label("Duración (semestres):");
        duracionLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 15px;");
        Label duracion = new Label(prog.getDuration() != null ? prog.getDuration().toString() : "");
        duracion.setStyle("-fx-font-size: 15px; -fx-padding: 0 0 10 0;");
        duracion.setMaxWidth(550);
        duracion.setMinWidth(0);
        duracion.setPrefWidth(550);

        Label resultadosLabel = new Label("Resultados Aprendizaje FileID:");
        resultadosLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 15px;");
        Label resultados = new Label(prog.getLearningOutcomesFileId() != null ? prog.getLearningOutcomesFileId().toString() : "");
        resultados.setStyle("-fx-font-size: 15px; -fx-padding: 0 0 10 0;");
        resultados.setMaxWidth(550);
        resultados.setMinWidth(0);
        resultados.setPrefWidth(550);
        resultados.setWrapText(true);

        Button cerrar = new Button("Cerrar");
        cerrar.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: #fff; -fx-background-radius: 8; -fx-font-size: 14px; -fx-padding: 6 18 6 18; -fx-font-weight: bold; -fx-border-color: #b71c1c; -fx-border-width: 2;");

        modalContent.getChildren().addAll(
            title,
            nombreLabel, nombre,
            gradoLabel, grado,
            perfilProfLabel, perfilProf,
            perfilOcupLabel, perfilOcup,
            perfilIngresoLabel, perfilIngreso,
            competenciasLabel, competencias,
            duracionLabel, duracion,
            resultadosLabel, resultados,
            cerrar
        );

        ScrollPane scrollPane = new ScrollPane(modalContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-border-radius: 14; -fx-background-radius: 14;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setPrefViewportWidth(600);
        scrollPane.setPrefViewportHeight(600);
        scrollPane.setMaxWidth(600);
        scrollPane.setMaxHeight(600);
        scrollPane.setMinWidth(400);
        scrollPane.setMinHeight(400);
        scrollPane.setPannable(true); // Permite arrastrar con el mouse
        // Drag para el modal
        scrollPane.setOnMousePressed(e -> {
            scrollPane.setUserData(new double[]{e.getSceneX(), e.getSceneY(), scrollPane.getHvalue(), scrollPane.getVvalue()});
        });
        scrollPane.setOnMouseDragged(e -> {
            double[] data = (double[]) scrollPane.getUserData();
            double deltaX = e.getSceneX() - data[0];
            double deltaY = e.getSceneY() - data[1];
            scrollPane.setHvalue(data[2] - deltaX / 1000);
            scrollPane.setVvalue(data[3] - deltaY / 1000);
        });

        // El modalWrapper ahora fuerza el tamaño del modal y centra el scrollPane
        VBox modalWrapper = new VBox();
        modalWrapper.setAlignment(Pos.CENTER);
        modalWrapper.setFillWidth(true);
        modalWrapper.setPrefWidth(600);
        modalWrapper.setMinWidth(400);
        modalWrapper.setMaxWidth(600);
        modalWrapper.setPrefHeight(600);
        modalWrapper.setMaxHeight(600);
        modalWrapper.getChildren().add(scrollPane);

        AnchorPane anchorPane = (AnchorPane) cardContainer.getScene().getRoot();
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(30,32,48,0.18);");
        overlay.setPickOnBounds(true);
        overlay.setPrefSize(anchorPane.getWidth(), anchorPane.getHeight());
        overlay.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        overlay.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        overlay.setAlignment(Pos.CENTER);
        overlay.getChildren().add(modalWrapper);

        anchorPane.getChildren().add(overlay);
        AnchorPane.setTopAnchor(overlay, 0.0);
        AnchorPane.setBottomAnchor(overlay, 0.0);
        AnchorPane.setLeftAnchor(overlay, 0.0);
        AnchorPane.setRightAnchor(overlay, 0.0);

        final StackPane overlayFinal = overlay;
        cerrar.setOnAction(ev -> anchorPane.getChildren().remove(overlayFinal));
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
            // Mostrar ventana de espera tipo modal igual a la de información de programa
            VBox modalContent = new VBox(18);
            modalContent.setStyle("-fx-background-color: #fff; -fx-padding: 32; -fx-background-radius: 14; -fx-effect: dropshadow(three-pass-box, #d32f2f, 12, 0.18, 0, 4); -fx-border-color: #d32f2f; -fx-border-width: 3;");
            modalContent.setPrefWidth(400);
            modalContent.setMinWidth(320);
            modalContent.setAlignment(Pos.CENTER);
            Label esperando = new Label("Subiendo archivo, por favor espere...");
            esperando.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #d32f2f;");
            modalContent.getChildren().add(esperando);

            VBox modalWrapper = new VBox();
            modalWrapper.setAlignment(Pos.CENTER);
            modalWrapper.setFillWidth(true);
            modalWrapper.setPrefWidth(400);
            modalWrapper.setMinWidth(320);
            modalWrapper.getChildren().add(modalContent);

            AnchorPane anchorPane = (AnchorPane) cardContainer.getScene().getRoot();
            StackPane overlay = new StackPane();
            overlay.setStyle("-fx-background-color: rgba(30,32,48,0.18);");
            overlay.setPickOnBounds(true);
            overlay.setPrefSize(anchorPane.getWidth(), anchorPane.getHeight());
            overlay.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
            overlay.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
            overlay.setAlignment(Pos.CENTER);
            overlay.getChildren().add(modalWrapper);
            anchorPane.getChildren().add(overlay);
            AnchorPane.setTopAnchor(overlay, 0.0);
            AnchorPane.setBottomAnchor(overlay, 0.0);
            AnchorPane.setLeftAnchor(overlay, 0.0);
            AnchorPane.setRightAnchor(overlay, 0.0);
            // Subir archivo en un hilo aparte para no congelar la UI
            new Thread(() -> {
                try {
                    excelUploadService.uploadPlan(programId, selectedFile);
                    javafx.application.Platform.runLater(() -> {
                        anchorPane.getChildren().remove(overlay);
                        mostrarAlerta("Éxito", "Archivo subido correctamente.", Alert.AlertType.INFORMATION);
                    });
                } catch (Exception ex) {
                    javafx.application.Platform.runLater(() -> {
                        anchorPane.getChildren().remove(overlay);
                        mostrarAlerta("Error", "No se pudo subir el archivo: " + ex.getMessage(), Alert.AlertType.ERROR);
                    });
                }
            }).start();
        }
    }

    private void mostrarPropuestasMicro(String tipo) {
        VBox modalContent = new VBox(18);
        modalContent.setStyle("-fx-background-color: #fff; -fx-padding: 32; -fx-background-radius: 14; -fx-effect: dropshadow(three-pass-box, #d32f2f, 12, 0.18, 0, 4); -fx-border-color: #d32f2f; -fx-border-width: 3;");
        modalContent.setPrefWidth(400);
        modalContent.setMinWidth(320);
        modalContent.setAlignment(Pos.CENTER);
        Label title = new Label("Propuestas Microcurrículo - " + tipo);
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #d32f2f;");
        Label info = new Label("Aquí se mostrarán las propuestas de microcurrículo para: " + tipo);
        info.setWrapText(true);
        Button cerrar = new Button("Cerrar");
        cerrar.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: #fff; -fx-background-radius: 8; -fx-font-size: 14px; -fx-padding: 6 18 6 18; -fx-font-weight: bold; -fx-border-color: #b71c1c; -fx-border-width: 2;");
        modalContent.getChildren().addAll(title, info, cerrar);

        VBox modalWrapper = new VBox();
        modalWrapper.setAlignment(Pos.CENTER);
        modalWrapper.setFillWidth(true);
        modalWrapper.setPrefWidth(400);
        modalWrapper.setMinWidth(320);
        modalWrapper.getChildren().add(modalContent);

        AnchorPane anchorPane = (AnchorPane) cardContainer.getScene().getRoot();
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(30,32,48,0.18);");
        overlay.setPickOnBounds(true);
        overlay.setPrefSize(anchorPane.getWidth(), anchorPane.getHeight());
        overlay.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        overlay.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        overlay.setAlignment(Pos.CENTER);
        overlay.getChildren().add(modalWrapper);
        anchorPane.getChildren().add(overlay);
        AnchorPane.setTopAnchor(overlay, 0.0);
        AnchorPane.setBottomAnchor(overlay, 0.0);
        AnchorPane.setLeftAnchor(overlay, 0.0);
        AnchorPane.setRightAnchor(overlay, 0.0);
        final StackPane overlayFinal = overlay;
        cerrar.setOnAction(ev -> anchorPane.getChildren().remove(overlayFinal));
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

        }}}

