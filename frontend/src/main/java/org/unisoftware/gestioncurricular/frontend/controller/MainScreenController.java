package org.unisoftware.gestioncurricular.frontend.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Region;
import javafx.util.converter.IntegerStringConverter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.web.WebView;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.unisoftware.gestioncurricular.frontend.dto.ProgramDTO;
import org.unisoftware.gestioncurricular.frontend.dto.CourseDTO;
import org.unisoftware.gestioncurricular.frontend.dto.StudyPlanEntryDTO;
import org.unisoftware.gestioncurricular.frontend.service.ProgramServiceFront;
import org.unisoftware.gestioncurricular.frontend.service.CourseServiceFront;
import org.unisoftware.gestioncurricular.frontend.service.ExcelUploadService;
import org.unisoftware.gestioncurricular.frontend.util.SessionManager;
import org.unisoftware.gestioncurricular.frontend.util.JwtDecodeUtil;
import org.unisoftware.gestioncurricular.frontend.service.ProgramFileServiceFront;
import org.unisoftware.gestioncurricular.frontend.service.ProgramFileViewServiceFront;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper; // Importar ObjectMapper
import org.unisoftware.gestioncurricular.frontend.dto.ProposalFileDTO; // Importar ProposalFileDTO
import org.unisoftware.gestioncurricular.frontend.service.ProposalFileServiceFront; // Importar ProposalFileServiceFront
import java.util.UUID; // Importar UUID

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;

@Component
public class MainScreenController implements Initializable {

    @FXML private Button adminPlantelBtn;
    @FXML private VBox cardContainer;
    @FXML private VBox userBox; // ¡Agrega este VBox en tu FXML al inicio de la pantalla!
    @FXML private Button btnPropuestasComite;
    @FXML private Button btnPropuestasEscuela;
    @FXML private Button btnPropuestasPrograma;
    @FXML private Button btnMisCursos;

    @Autowired private ProgramServiceFront programServiceFront;
    @Autowired private ExcelUploadService excelUploadService;
    @Autowired private ApplicationContext applicationContext;
    @Autowired private ProgramFileServiceFront programFileServiceFront;
    @Autowired private ProgramFileViewServiceFront programFileViewServiceFront;
    private final ProposalFileServiceFront proposalFileServiceFront = new ProposalFileServiceFront(); // Instanciar el nuevo servicio

    private int paginaActual = 0;
    private static final int PROGRAMAS_POR_PAGINA = 2;
    private List<ProgramDTO> listaProgramas = null;

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
        // Mostrar botón solo si el usuario es DOCENTE
        if (SessionManager.getInstance().hasRole("DOCENTE")) {
            btnMisCursos.setVisible(true);
            btnMisCursos.setManaged(true);
            btnMisCursos.setOnAction(e -> mostrarMisCursos());
        } else {
            btnMisCursos.setVisible(false);
            btnMisCursos.setManaged(false);
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
            if (listaProgramas == null) {
                listaProgramas = programServiceFront.listPrograms();
            }
            if (listaProgramas.isEmpty()) {
                cardContainer.getChildren().add(new Label("No hay programas disponibles."));
                return;
            }
            int totalPaginas = (int) Math.ceil(listaProgramas.size() / (double) PROGRAMAS_POR_PAGINA);
            int inicio = paginaActual * PROGRAMAS_POR_PAGINA;
            int fin = Math.min(inicio + PROGRAMAS_POR_PAGINA, listaProgramas.size());
            VBox filas = new VBox(32);
            filas.setAlignment(Pos.TOP_CENTER);
            HBox fila = new HBox(32);
            fila.setAlignment(Pos.TOP_CENTER);
            for (int i = inicio; i < fin; i++) {
                VBox card = crearCardPrograma(listaProgramas.get(i));
                fila.getChildren().add(card);
            }
            filas.getChildren().add(fila);
            cardContainer.getChildren().add(filas);
            HBox paginacion = new HBox(16);
            paginacion.setAlignment(Pos.CENTER);
            Button btnAnterior = new Button("Anterior");
            btnAnterior.getStyleClass().add("paginacion-btn");
            btnAnterior.setDisable(paginaActual == 0);
            btnAnterior.setOnAction(e -> {
                paginaActual--;
                mostrarProgramaCard();
            });
            Button btnSiguiente = new Button("Siguiente");
            btnSiguiente.getStyleClass().add("paginacion-btn");
            btnSiguiente.setDisable(paginaActual >= totalPaginas - 1);
            btnSiguiente.setOnAction(e -> {
                paginaActual++;
                mostrarProgramaCard();
            });
            Label lblPag = new Label("Página " + (paginaActual + 1) + " de " + totalPaginas);
            lblPag.setStyle("-fx-text-fill: black; -fx-font-weight: bold; -fx-font-size: 15px;");
            paginacion.getChildren().addAll(btnAnterior, lblPag, btnSiguiente);
            if (totalPaginas > 1) {
                cardContainer.getChildren().add(paginacion);
            }
        } catch (Exception e) {
            cardContainer.getChildren().add(new Label("Error al cargar los programas: " + e.getMessage()));
        }
    }

    private VBox crearCardPrograma(ProgramDTO prog) {
        VBox card = new VBox(18);
        card.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #fff, #f7f7fa 80%, #f1f1f1);"  +
            "-fx-background-radius: 18;" +
            "-fx-padding: 28 24 28 24;" +
            "-fx-effect: dropshadow(gaussian, #d32f2f44, 8,0,0,2);"
        );
        card.setMinWidth(320);
        card.setMaxWidth(320);
        card.setSpacing(16);
        card.setAlignment(Pos.TOP_LEFT);

        Label nameLbl = new Label(prog.getName());
        nameLbl.setWrapText(true);
        nameLbl.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #d32f2f; -fx-padding: 0 0 8 0;");
        nameLbl.setMaxWidth(Double.MAX_VALUE);

        Button expandBtn = new Button("Ver información");
        expandBtn.setWrapText(true);
        expandBtn.setMaxWidth(Double.MAX_VALUE);
        expandBtn.setOnAction(e -> mostrarDetallePrograma(prog));
        expandBtn.getStyleClass().add("card-btn-red");

        Button goToCursosBtn = new Button("Ver Cursos del Programa");
        goToCursosBtn.setWrapText(true);
        goToCursosBtn.setMaxWidth(Double.MAX_VALUE);
        goToCursosBtn.setOnAction(e -> abrirCursosPrograma(prog.getId(), prog.getName()));
        goToCursosBtn.getStyleClass().add("card-btn-white");

        Button btnGestionar = null;
        if (SessionManager.getInstance().hasRole("DIRECTOR_DE_PROGRAMA")) {
            btnGestionar = new Button("GESTIONAR");
            btnGestionar.setWrapText(true);
            btnGestionar.setMaxWidth(Double.MAX_VALUE);
            btnGestionar.getStyleClass().add("card-btn-red"); // O un estilo apropiado
            btnGestionar.setOnAction(e -> mostrarGestionPrograma(prog));
        }

        // Los botones de DECANO para subir curriculums y resultados se eliminan de aquí
        // y se mueven a la ventana de GESTIONAR del DIRECTOR_DE_PROGRAMA.

        card.getChildren().addAll(nameLbl, expandBtn, goToCursosBtn);
        if (btnGestionar != null) {
            card.getChildren().add(btnGestionar);
        }

        // Ajustar altura dinámica según cantidad de botones
        int botones = card.getChildren().size() - 1; // -1 por el label
        int minHeight = 80 + (botones * 48) + 24; // 80 para el label, 48 por botón, 24 padding extra
        card.setMinHeight(minHeight);
        card.setPrefHeight(minHeight);
        card.setMaxHeight(minHeight);

        // Si hay muchos botones, permitir scroll interno en la card
        if (minHeight > 320) {
            ScrollPane scroll = new ScrollPane(card);
            scroll.setFitToWidth(true);
            scroll.setPrefHeight(320);
            scroll.setMaxHeight(320);
            VBox wrapper = new VBox(scroll);
            wrapper.setMinWidth(320);
            wrapper.setMaxWidth(320);
            wrapper.setPrefWidth(320);
            return wrapper;
        }
        return card;
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

        Label AcademicLevelLabel = new Label("Nivel Academico:");
        AcademicLevelLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 15px;");
        Label AcademicLevel = new Label(prog.getAcademicLevel() != null ? prog.getAcademicLevel() : "");
        AcademicLevel.setStyle("-fx-font-size: 15px; -fx-padding: 0 0 10 0;");
        AcademicLevel.setMaxWidth(550);
        AcademicLevel.setMinWidth(0);
        AcademicLevel.setPrefWidth(550);

        Label ModalityLabel = new Label("Modalidad:");
        ModalityLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 15px;");
        Label Modality = new Label(prog.getModality() != null ? prog.getModality() : "");
        Modality.setStyle("-fx-font-size: 15px; -fx-padding: 0 0 10 0;");
        Modality.setMaxWidth(550);
        Modality.setMinWidth(0);
        Modality.setPrefWidth(550);

        Label duracionLabel = new Label("Duración (semestres):");
        duracionLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 15px;");
        Label duracion = new Label(prog.getDuration() != null ? prog.getDuration().toString() : "");
        duracion.setStyle("-fx-font-size: 15px; -fx-padding: 0 0 10 0;");
        duracion.setMaxWidth(550);
        duracion.setMinWidth(0);
        duracion.setPrefWidth(550);

        HBox archivosBox = new HBox(18);
        archivosBox.setAlignment(Pos.CENTER_LEFT);
        Button btnVerResultados = new Button("Resultados de Aprendizaje");
        btnVerResultados.getStyleClass().add("card-btn-white");
        btnVerResultados.setOnAction(e -> abrirPdfEnNavegador(() -> programFileViewServiceFront.getResultadosUrl(prog.getId())));
        Button btnVerCurriculums = new Button("Ver Curriculums");
        btnVerCurriculums.getStyleClass().add("card-btn-white");
        btnVerCurriculums.setOnAction(e -> abrirPdfEnNavegador(() -> programFileViewServiceFront.getCurriculumsUrl(prog.getId())));
        archivosBox.getChildren().addAll(btnVerResultados, btnVerCurriculums);

        Button btnDescargarArchivos = new Button("Descargar Archivos (Resultados y Curriculums)");
        btnDescargarArchivos.getStyleClass().add("card-btn-red"); // Estilo similar a GESTIONAR
        btnDescargarArchivos.setMaxWidth(Double.MAX_VALUE);
        btnDescargarArchivos.setOnAction(e -> {
            descargarArchivoDesdeUrl(() -> programFileViewServiceFront.getResultadosUrl(prog.getId()), "resultados_aprendizaje.pdf");
            descargarArchivoDesdeUrl(() -> programFileViewServiceFront.getCurriculumsUrl(prog.getId()), "curriculums_docentes.pdf");
        });

        if (SessionManager.getInstance().hasRole("DIRECTOR_DE_PROGRAMA")) {
            btnDescargarArchivos.setVisible(true);
            btnDescargarArchivos.setManaged(true);
        } else {
            btnDescargarArchivos.setVisible(false);
            btnDescargarArchivos.setManaged(false);
        }

        Button cerrar = new Button("Cerrar");
        cerrar.getStyleClass().add("cerrar-btn");

        modalContent.getChildren().addAll(
            title,
            nombreLabel, nombre,
            gradoLabel, grado,
            duracionLabel, duracion,
                ModalityLabel, Modality,
                AcademicLevelLabel, AcademicLevel,
            archivosBox, // <--- Asegúrate que archivosBox se agrega aquí
            btnDescargarArchivos, // Botón añadido aquí
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

    private void abrirPdfEnNavegador(java.util.function.Supplier<String> urlSupplier) {
        new Thread(() -> {
            try {
                String originalPdfUrl = urlSupplier.get();
                if (originalPdfUrl != null && !originalPdfUrl.isBlank()) {
                    System.out.println("URL original del PDF: " + originalPdfUrl);

                    // Codificar la URL del PDF para usarla como parámetro
                    String encodedPdfUrl = URLEncoder.encode(originalPdfUrl, StandardCharsets.UTF_8.toString());
                    String googleDocsViewerUrl = "https://docs.google.com/gview?url=" + encodedPdfUrl + "&embedded=true";

                    System.out.println("Intentando cargar PDF con Google Docs Viewer: " + googleDocsViewerUrl);

                    javafx.application.Platform.runLater(() -> {
                        Stage pdfStage = new Stage();
                        WebView webView = new WebView();

                        webView.getEngine().getLoadWorker().exceptionProperty().addListener((obs, oldEx, newEx) -> {
                            if (newEx != null) {
                                System.err.println("Error al cargar URL en WebView (Google Docs Viewer): " + newEx.getMessage());
                                newEx.printStackTrace();
                                // Intentar abrir en navegador externo como fallback si Google Viewer falla
                                try {
                                    System.out.println("Fallback: Intentando abrir URL original en navegador externo: " + originalPdfUrl);
                                    java.awt.Desktop.getDesktop().browse(new java.net.URI(originalPdfUrl));
                                    mostrarAlerta("Visor no disponible", "Se abrirá el PDF en tu navegador web.", Alert.AlertType.INFORMATION);
                                } catch (Exception fallbackEx) {
                                    System.err.println("Error en fallback al navegador externo: " + fallbackEx.getMessage());
                                    mostrarAlerta("Error", "No se pudo cargar el PDF en el visor ni en el navegador: " + newEx.getMessage(), Alert.AlertType.ERROR);
                                }
                                pdfStage.close(); // Cerrar la ventana del WebView si falla
                            }
                        });

                        webView.getEngine().load(googleDocsViewerUrl);
                        VBox root = new VBox(webView);
                        VBox.setVgrow(webView, javafx.scene.layout.Priority.ALWAYS); // Hacer que el WebView ocupe todo el espacio
                        Scene scene = new Scene(root, 800, 700);
                        pdfStage.setTitle("Visor de Documento");
                        pdfStage.setScene(scene);
                        pdfStage.show();
                    });
                } else {
                    javafx.application.Platform.runLater(() -> mostrarAlerta("Información", "No hay archivo para mostrar.", Alert.AlertType.INFORMATION));
                }
            } catch (Exception ex) {
                System.err.println("Error general al intentar abrir PDF: " + ex.getMessage());
                ex.printStackTrace();
                javafx.application.Platform.runLater(() -> mostrarAlerta("Error", "No se pudo abrir el PDF: " + ex.getMessage(), Alert.AlertType.ERROR));
            }
        }).start();
    }

    private void descargarArchivoDesdeUrl(java.util.function.Supplier<String> urlSupplier, String defaultFileName) {
        new Thread(() -> {
            try {
                String url = urlSupplier.get();
                if (url != null && !url.isBlank()) {
                    System.out.println("Intentando descargar archivo desde URL: " + url);
                    // Simular descarga abriendo en navegador. El navegador gestionará la descarga si los headers son correctos.
                    java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
                } else {
                    javafx.application.Platform.runLater(() -> mostrarAlerta("Información", "No hay URL para descargar el archivo: " + defaultFileName, Alert.AlertType.INFORMATION));
                }
            } catch (Exception ex) {
                System.err.println("Error al intentar descargar archivo (" + defaultFileName + "): " + ex.getMessage());
                ex.printStackTrace();
                javafx.application.Platform.runLater(() -> mostrarAlerta("Error de Descarga", "No se pudo iniciar la descarga para " + defaultFileName + ": " + ex.getMessage(), Alert.AlertType.ERROR));
            }
        }).start();
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
        // Mostrar alerta de confirmación para descargar plantilla
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION); // Usamos CONFIRMATION por el icono, pero los botones se reemplazarán.
        alert.setTitle("Descargar plantilla");
        alert.setHeaderText("¿Desea descargar una plantilla del formato de plan de estudios?");
        alert.setContentText("Puede usar la plantilla para cargar el plan de estudios correctamente.");

        // Definir ButtonTypes personalizados
        javafx.scene.control.ButtonType buttonTypeSiDescargar = new javafx.scene.control.ButtonType("Sí, descargar", javafx.scene.control.ButtonBar.ButtonData.YES);
        javafx.scene.control.ButtonType buttonTypeNoSoloSubir = new javafx.scene.control.ButtonType("No, solo subir", javafx.scene.control.ButtonBar.ButtonData.NO);

        // Establecer estos botones en la alerta, reemplazando los predeterminados
        alert.getButtonTypes().setAll(buttonTypeSiDescargar, buttonTypeNoSoloSubir);

        // Mostrar y esperar respuesta
        java.util.Optional<javafx.scene.control.ButtonType> optResponse = alert.showAndWait();

        if (optResponse.isPresent()) { // Si se presionó un botón (y no se cerró con 'X')
            javafx.scene.control.ButtonType response = optResponse.get();
            boolean proceedToUpload = false;

            if (response == buttonTypeSiDescargar) { // Usuario seleccionó "Sí, descargar"
                // Descargar plantilla
                String plantillaUrl = "https://fexiivjyzplakakkiyqm.supabase.co/storage/v1/object/public/documentos-publicos/ejemplos/Plantilla%20Plan%20De%20Estudios.xlsx";
                try {
                    java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
                    java.net.URI uri = new java.net.URI(plantillaUrl);
                    desktop.browse(uri);
                } catch (Exception e) {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Error al descargar");
                    errorAlert.setHeaderText("No se pudo abrir el enlace de descarga");
                    errorAlert.setContentText(e.getMessage());
                    errorAlert.showAndWait();
                }
                proceedToUpload = true; // Continuar a la subida después de intentar descargar
            } else if (response == buttonTypeNoSoloSubir) { // Usuario seleccionó "No, solo subir"
                proceedToUpload = true; // Continuar directamente a la subida
            }

            if (proceedToUpload) {
                // Continuar con el flujo de subida de archivo
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
                                mostrarAlerta("Error", "No se pudo subir el archivo: " + "No cumple con el formato requerido", Alert.AlertType.ERROR);
                            });
                        }
                    }).start();
                }
            }
        }
        // Si optResponse no está presente (es decir, la alerta se cerró con 'X'), no se hace nada más,
        // cancelando efectivamente el proceso de subida.
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
        cerrar.getStyleClass().add("cerrar-btn");
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

    private void mostrarMisCursos() {
        try {
            String docenteId = org.unisoftware.gestioncurricular.frontend.util.SessionManager.getInstance().getUserId();
            org.unisoftware.gestioncurricular.frontend.service.CourseServiceFront courseService = applicationContext.getBean(org.unisoftware.gestioncurricular.frontend.service.CourseServiceFront.class);
            List<org.unisoftware.gestioncurricular.frontend.dto.CourseDTO> cursos = courseService.listCoursesByDocenteId(docenteId);
            VBox modalContent = new VBox(18);
            modalContent.setStyle("-fx-background-color: #fff; -fx-padding: 32; -fx-background-radius: 14; -fx-effect: dropshadow(three-pass-box, #d32f2f, 12, 0.18, 0, 4); -fx-border-color: #d32f2f; -fx-border-width: 3;");
            modalContent.setPrefWidth(800);
            modalContent.setMinWidth(600);
            modalContent.setAlignment(Pos.CENTER);
            Label title = new Label("Mis Cursos Asignados");
            title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #d32f2f;");
            modalContent.getChildren().add(title);
            if (cursos == null || cursos.isEmpty()) {
                modalContent.getChildren().add(new Label("No tienes cursos asignados."));
            } else {
                HBox cardsHBox = new HBox(24);
                cardsHBox.setAlignment(Pos.CENTER);
                for (org.unisoftware.gestioncurricular.frontend.dto.CourseDTO curso : cursos) {
                    VBox card = new VBox(10);
                    card.setStyle("-fx-background-color: linear-gradient(to bottom right, #fff, #f7f7fa 80%, #f1f1f1);-fx-background-radius: 18;-fx-padding: 24 18 24 18;-fx-effect: dropshadow(gaussian, #d32f2f44, 8,0,0,2);");
                    card.setMinWidth(260);
                    card.setMaxWidth(260);
                    card.setMinHeight(180);
                    card.setMaxHeight(220);
                    card.setAlignment(Pos.TOP_LEFT);
                    Label nameLbl = new Label(curso.getName());
                    nameLbl.setWrapText(true);
                    nameLbl.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #d32f2f; -fx-padding: 0 0 8 0;");
                    nameLbl.setMaxWidth(Double.MAX_VALUE);
                    Label codeLbl = new Label("Código: " + curso.getId());
                    codeLbl.setStyle("-fx-font-size: 14px; -fx-text-fill: #222;");
                    Button btnProponer = new Button("Proponer Curriculum");
                    btnProponer.getStyleClass().add("primary-button");
                    btnProponer.setOnAction(ev -> subirPropuestaCurriculum(curso));
                    card.getChildren().addAll(nameLbl, codeLbl, btnProponer);
                    cardsHBox.getChildren().add(card);
                }
                modalContent.getChildren().add(cardsHBox);
            }
            Button cerrar = new Button("Cerrar");
            cerrar.getStyleClass().add("cerrar-btn");
            modalContent.getChildren().add(cerrar);
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
        } catch (Exception e) {
            mostrarAlerta("Error", "No se pudo cargar tus cursos: " + e.getMessage(), Alert.AlertType.ERROR);
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

        }}

    // Permite al docente seleccionar un archivo y crear una propuesta de microcurrículo
    private void subirPropuestaCurriculum(org.unisoftware.gestioncurricular.frontend.dto.CourseDTO curso) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecciona el archivo de microcurrículo");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Archivos permitidos", "*.pdf", "*.doc", "*.docx", "*.xlsx", "*.xls", "*.csv")
        );
        Stage stage = (Stage) cardContainer.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            VBox modalContent = new VBox(18);
            modalContent.setStyle("-fx-background-color: #fff; -fx-padding: 32; -fx-background-radius: 14; -fx-effect: dropshadow(three-pass-box, #d32f2f, 12, 0.18, 0, 4); -fx-border-color: #d32f2f; -fx-border-width: 3;");
            modalContent.setPrefWidth(400);
            modalContent.setMinWidth(320);
            modalContent.setAlignment(Pos.CENTER);
            Label esperando = new Label("Subiendo archivo y creando propuesta, por favor espere...");
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

            new Thread(() -> {
                try {
                    String token = SessionManager.getInstance().getToken(); // Obtener token
                    ObjectMapper objectMapper = new ObjectMapper();

                    // Mostrar UI de carga inicial (opcional, pero recomendado)
                    javafx.application.Platform.runLater(() -> {
                        // Aquí podrías actualizar la UI para indicar "Creando propuesta..."
                    });

                    // 1. Crear la propuesta primero para obtener su ID y asociar un fileId temporal
                    String tempFileId = UUID.randomUUID().toString(); // Generar un fileId temporal
                    String docenteId = SessionManager.getInstance().getUserId();

                    Map<String, Object> proposalRequestPayload = new HashMap<>();
                    proposalRequestPayload.put("title", "Propuesta de microcurrículo para " + curso.getName());
                    proposalRequestPayload.put("courseId", curso.getId());
                    proposalRequestPayload.put("teacherId", docenteId); // Asegúrate que esto sea un UUID String
                    proposalRequestPayload.put("fileId", tempFileId); // Enviar el fileId temporal

                    String jsonProposalPayload = objectMapper.writeValueAsString(proposalRequestPayload);

                    java.net.URL urlCreateProposal = new java.net.URL("http://localhost:8080/proposals");
                    java.net.HttpURLConnection connCreateProposal = (java.net.HttpURLConnection) urlCreateProposal.openConnection();
                    connCreateProposal.setDoOutput(true);
                    connCreateProposal.setRequestMethod("POST");
                    if (token != null && !token.isEmpty()) {
                        connCreateProposal.setRequestProperty("Authorization", "Bearer " + token);
                    }
                    connCreateProposal.setRequestProperty("Content-Type", "application/json");
                    connCreateProposal.setRequestProperty("Accept", "application/json");

                    try (java.io.OutputStream os = connCreateProposal.getOutputStream()) {
                        os.write(jsonProposalPayload.getBytes(StandardCharsets.UTF_8));
                    }

                    int responseCodeProposal = connCreateProposal.getResponseCode();
                    String responseBodyProposal;
                    if (responseCodeProposal >= 200 && responseCodeProposal < 300) {
                        responseBodyProposal = new String(connCreateProposal.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                    } else {
                        String errorBody = "";
                        if (connCreateProposal.getErrorStream() != null) {
                            errorBody = new String(connCreateProposal.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
                        }
                        throw new RuntimeException("Error al crear propuesta: " + connCreateProposal.getResponseMessage() +
                                " (Código: " + responseCodeProposal + "). Cuerpo: " + errorBody);
                    }

                    // Parsear la respuesta para obtener el ID de la propuesta creada
                    Map<String, Object> createdProposal = objectMapper.readValue(responseBodyProposal, Map.class);
                    Long proposalId = ((Number) createdProposal.get("id")).longValue();
                    // String actualFileId = (String) createdProposal.get("fileId"); // El fileId que el backend guardó

                    // Actualizar UI (opcional)
                    javafx.application.Platform.runLater(() -> {
                        // "Obteniendo URL de subida..."
                    });

                    // 2. Obtener la URL prefirmada para subir el archivo
                    ProposalFileDTO proposalFileDto = proposalFileServiceFront.getUploadUrl(curso.getId(), proposalId, token);
                    String presignedUrl = proposalFileDto.getUrl();
                    // String fileKeyFromBackend = proposalFileDto.getFileKey(); // Este debería coincidir con actualFileId o tempFileId

                    // Actualizar UI (opcional)
                    javafx.application.Platform.runLater(() -> {
                        // "Subiendo archivo..."
                    });

                    // 3. Subir el archivo a la URL prefirmada
                    String contentType = java.nio.file.Files.probeContentType(selectedFile.toPath());
                    if (contentType == null) {
                        // Fallback si no se puede determinar, o se puede basar en la extensión del archivo
                        if (selectedFile.getName().endsWith(".pdf")) contentType = "application/pdf";
                        else if (selectedFile.getName().endsWith(".doc")) contentType = "application/msword";
                        else if (selectedFile.getName().endsWith(".docx")) contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
                        else if (selectedFile.getName().endsWith(".xlsx")) contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                        else if (selectedFile.getName().endsWith(".xls")) contentType = "application/vnd.ms-excel";
                        else if (selectedFile.getName().endsWith(".csv")) contentType = "text/csv";
                        else contentType = "application/octet-stream"; // Genérico
                    }
                    proposalFileServiceFront.uploadFileToPresignedUrl(presignedUrl, selectedFile, contentType, token); // Pasar el token aquí

                    javafx.application.Platform.runLater(() -> {
                        anchorPane.getChildren().remove(overlay);
                        mostrarAlerta("Éxito", "Propuesta enviada correctamente.", Alert.AlertType.INFORMATION);
                    });

                } catch (Exception ex) {
                    ex.printStackTrace(); // Imprimir stack trace para depuración
                    javafx.application.Platform.runLater(() -> {
                        anchorPane.getChildren().remove(overlay);
                        mostrarAlerta("Error", "No se pudo enviar la propuesta: " + ex.getMessage(), Alert.AlertType.ERROR);
                    });
                }
            }).start();
        }
    }

    // Nueva ventana modal para GESTIONAR programa (Director de Programa)
    private void mostrarGestionPrograma(ProgramDTO prog) {
        VBox modalContent = new VBox(18);
        modalContent.setStyle(
                "-fx-background-color: #fff; -fx-padding: 32; -fx-background-radius: 14; " +
                "-fx-effect: dropshadow(three-pass-box, #d32f2f, 12, 0.18, 0, 4); " +
                "-fx-border-color: #d32f2f; -fx-border-width: 3;");
        modalContent.setPrefWidth(400);
        modalContent.setMinWidth(320);
        modalContent.setAlignment(Pos.CENTER);

        Label title = new Label("Gestionar Programa: " + prog.getName());
        title.setWrapText(true);
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #d32f2f; -fx-padding: 0 0 10 0;");

        Button btnActualizarPlan = new Button("Actualizar plan de estudios");
        btnActualizarPlan.setMaxWidth(Double.MAX_VALUE);
        btnActualizarPlan.getStyleClass().add("card-btn-white");
        btnActualizarPlan.setOnAction(e -> {
            // Primero cerrar este modal antes de abrir el de subir excel
            Node source = (Node) e.getSource();
            StackPane overlay = (StackPane) source.getScene().getRoot().lookup("#gestionProgramaOverlay");
            if (overlay != null) {
                ((AnchorPane) overlay.getParent()).getChildren().remove(overlay);
            }
            handleSubirExcel(prog.getId());
        });

        Button btnEditarCursosInfo = new Button("Editar Cursos del Programa");
        btnEditarCursosInfo.setMaxWidth(Double.MAX_VALUE);
        btnEditarCursosInfo.getStyleClass().add("card-btn-white");
        btnEditarCursosInfo.setOnAction(e -> {
            // Primero cerrar este modal
             Node source = (Node) e.getSource();
            StackPane overlay = (StackPane) source.getScene().getRoot().lookup("#gestionProgramaOverlay");
            if (overlay != null) {
                ((AnchorPane) overlay.getParent()).getChildren().remove(overlay);
            }
            mostrarEdicionProgramaYCursos(prog);
        });

        Button btnSubirCurriculums = new Button("Subir Curriculums");
        btnSubirCurriculums.setMaxWidth(Double.MAX_VALUE);
        btnSubirCurriculums.getStyleClass().add("card-btn-white");
        btnSubirCurriculums.setOnAction(e -> handleSubirCurriculums(prog.getId()));

        Button btnSubirResultados = new Button("Subir Resultados de Aprendizaje del Programa");
        btnSubirResultados.setMaxWidth(Double.MAX_VALUE);
        btnSubirResultados.getStyleClass().add("card-btn-white");
        btnSubirResultados.setOnAction(e -> handleSubirResultados(prog.getId()));


        Button cerrar = new Button("Cerrar");
        cerrar.getStyleClass().add("cerrar-btn");

        modalContent.getChildren().addAll(title, btnActualizarPlan, btnEditarCursosInfo, btnSubirCurriculums, btnSubirResultados, cerrar);

        VBox modalWrapper = new VBox(modalContent);
        modalWrapper.setAlignment(Pos.CENTER);
        modalWrapper.setPrefWidth(400);
        modalWrapper.setMinWidth(320);


        AnchorPane anchorPane = (AnchorPane) cardContainer.getScene().getRoot();
        StackPane overlay = new StackPane();
        overlay.setId("gestionProgramaOverlay"); // ID para poder cerrarlo desde los botones internos
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

    // Permite al director editar la información del programa y sus cursos
    private void mostrarEdicionProgramaYCursos(ProgramDTO prog) {
        try {
            // Obtener cursos del programa (CourseDTO generales)
            org.unisoftware.gestioncurricular.frontend.service.CourseServiceFront courseService = applicationContext.getBean(org.unisoftware.gestioncurricular.frontend.service.CourseServiceFront.class);
            List<org.unisoftware.gestioncurricular.frontend.dto.CourseDTO> cursos = courseService.listCoursesByProgramaId(prog.getId());

            // Obtener las entradas del plan de estudios (StudyPlanEntryDTO) para este programa
            List<org.unisoftware.gestioncurricular.frontend.dto.StudyPlanEntryDTO> planEntries = null;
            try {
                planEntries = programServiceFront.getStudyPlan(prog.getId());
            } catch (Exception e) {
                System.err.println("Error al cargar el plan de estudios para la edición de requisitos: " + e.getMessage());
                // Opcional: mostrar alerta al usuario
            }

            Map<Long, List<Long>> studyPlanRequirementsMap = new java.util.HashMap<>();
            if (planEntries != null) {
                for (org.unisoftware.gestioncurricular.frontend.dto.StudyPlanEntryDTO planEntry : planEntries) {
                    if (planEntry.getId() != null && planEntry.getId().getCourseId() != null) {
                        studyPlanRequirementsMap.put(
                                planEntry.getId().getCourseId(),
                                planEntry.getRequirements() != null ? planEntry.getRequirements() : new java.util.ArrayList<>()
                        );
                    }
                }
            }

            // Actualizar los requisitos en la lista de CourseDTO con los del StudyPlanEntryDTO
            for (org.unisoftware.gestioncurricular.frontend.dto.CourseDTO curso : cursos) {
                List<Long> planSpecificReqs = studyPlanRequirementsMap.get(curso.getId());
                if (planSpecificReqs != null) {
                    curso.setRequirements(planSpecificReqs); // Usar requisitos del plan de estudio
                } else {
                    // Si el curso no está en el plan de estudio o no tiene requisitos definidos allí,
                    // se podría dejar sus requisitos generales o limpiarlosa.
                    // Por ahora, si no está en el plan, se asume que no tiene requisitos específicos del plan.
                    curso.setRequirements(new java.util.ArrayList<>());
                }
            }

            // Copia profunda de los cursos originales (ahora con requisitos del plan) para comparar cambios
            List<org.unisoftware.gestioncurricular.frontend.dto.CourseDTO> cursosOriginales = new java.util.ArrayList<>();
            for (org.unisoftware.gestioncurricular.frontend.dto.CourseDTO c : cursos) {
                org.unisoftware.gestioncurricular.frontend.dto.CourseDTO copia = new org.unisoftware.gestioncurricular.frontend.dto.CourseDTO();
                copia.setId(c.getId());
                copia.setName(c.getName());
                copia.setType(c.getType());
                copia.setCredits(c.getCredits());
                copia.setCycle(c.getCycle());
                copia.setArea(c.getArea());
                copia.setRequirements(c.getRequirements());
                cursosOriginales.add(copia);
            }
            VBox modalContent = new VBox(18);
            modalContent.setStyle("-fx-background-color: #fff; -fx-padding: 32; -fx-background-radius: 14; -fx-effect: dropshadow(three-pass-box, #d32f2f, 12, 0.18, 0, 4); -fx-border-color: #d32f2f; -fx-border-width: 3;");
            modalContent.setPrefWidth(900);
            modalContent.setMinWidth(700);
            modalContent.setAlignment(Pos.TOP_CENTER);

            Label title = new Label("Editar Cursos del Programa");
            title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #d32f2f;");
            modalContent.getChildren().add(title);

            // Tabla de cursos editable
            Label cursosLbl = new Label("Cursos del Programa:");
            cursosLbl.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #1a2233;");
            modalContent.getChildren().add(cursosLbl);

            javafx.scene.control.TableView<org.unisoftware.gestioncurricular.frontend.dto.CourseDTO> table = new javafx.scene.control.TableView<>();
            table.setEditable(true);
            javafx.collections.ObservableList<org.unisoftware.gestioncurricular.frontend.dto.CourseDTO> data = javafx.collections.FXCollections.observableArrayList(cursos);
            table.setItems(data);

            javafx.scene.control.TableColumn<org.unisoftware.gestioncurricular.frontend.dto.CourseDTO, String> colNombre = new javafx.scene.control.TableColumn<>("Nombre");
            colNombre.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("name"));
            colNombre.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn());
            colNombre.setOnEditCommit(ev -> ev.getRowValue().setName(ev.getNewValue()));

            javafx.scene.control.TableColumn<org.unisoftware.gestioncurricular.frontend.dto.CourseDTO, String> colTipo = new javafx.scene.control.TableColumn<>("Tipo");
            colTipo.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("type"));
            colTipo.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn());
            colTipo.setOnEditCommit(ev -> ev.getRowValue().setType(ev.getNewValue()));

            javafx.scene.control.TableColumn<org.unisoftware.gestioncurricular.frontend.dto.CourseDTO, Integer> colCreditos = new javafx.scene.control.TableColumn<>("Créditos");
            colCreditos.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("credits"));
            colCreditos.setCellFactory(tc -> new javafx.scene.control.cell.TextFieldTableCell<>(new javafx.util.converter.IntegerStringConverter()));
            colCreditos.setOnEditCommit(ev -> ev.getRowValue().setCredits(ev.getNewValue()));

            javafx.scene.control.TableColumn<org.unisoftware.gestioncurricular.frontend.dto.CourseDTO, String> colCiclo = new javafx.scene.control.TableColumn<>("Ciclo");
            colCiclo.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("cycle"));
            colCiclo.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn());
            colCiclo.setOnEditCommit(ev -> ev.getRowValue().setCycle(ev.getNewValue()));

            javafx.scene.control.TableColumn<org.unisoftware.gestioncurricular.frontend.dto.CourseDTO, String> colArea = new javafx.scene.control.TableColumn<>("Área");
            colArea.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("area"));
            colArea.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn());
            colArea.setOnEditCommit(ev -> ev.getRowValue().setArea(ev.getNewValue()));

            // Columna de Requisitos (Solo Mostrar)
            javafx.scene.control.TableColumn<org.unisoftware.gestioncurricular.frontend.dto.CourseDTO, String> colRequisitos = new javafx.scene.control.TableColumn<>("Requisitos");
            colRequisitos.setCellValueFactory(cellData -> {
                List<Long> reqs = cellData.getValue().getRequirements();
                String reqStr = (reqs == null || reqs.isEmpty()) ? "" : reqs.stream().map(String::valueOf).reduce((a, b) -> a + "," + b).orElse("");
                return new javafx.beans.property.SimpleStringProperty(reqStr);
            });
            // No se establece CellFactory para edición, por lo tanto, es de solo lectura por defecto.
            // No se establece setOnEditCommit.

            table.getColumns().addAll(colNombre, colTipo, colCreditos, colCiclo, colArea, colRequisitos);
            table.setColumnResizePolicy(javafx.scene.control.TableView.CONSTRAINED_RESIZE_POLICY);
            table.setPrefHeight(400);
            modalContent.getChildren().add(table);

            HBox botones = new HBox(18);
            botones.setAlignment(Pos.CENTER);
            Button btnGuardar = new Button("Guardar Cambios");
            Button btnCerrar = new Button("Cerrar");
            btnGuardar.getStyleClass().add("card-btn-red");
            btnCerrar.getStyleClass().add("card-btn-red");
            botones.getChildren().addAll(btnGuardar, btnCerrar);
            modalContent.getChildren().add(botones);

            VBox modalWrapper = new VBox();
            modalWrapper.setAlignment(Pos.CENTER);
            modalWrapper.setFillWidth(true);
            modalWrapper.setPrefWidth(900);
            modalWrapper.setMinWidth(700);
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

            btnCerrar.setOnAction(ev -> anchorPane.getChildren().remove(overlay));
            btnGuardar.setOnAction(ev -> {
                try {
                    commitActiveCell(table); // Forzar commit de la celda en edición
                    boolean huboCambios = false;
                    for (int i = 0; i < data.size(); i++) {
                        org.unisoftware.gestioncurricular.frontend.dto.CourseDTO actual = data.get(i);
                        org.unisoftware.gestioncurricular.frontend.dto.CourseDTO original = cursosOriginales.get(i);
                        if (!equalsCurso(actual, original)) {
                            courseService.updateCourse(actual);
                            huboCambios = true;
                        }
                    }
                    if (huboCambios) {
                        mostrarAlerta("Éxito", "Cursos actualizados correctamente.", Alert.AlertType.INFORMATION);
                        mostrarProgramaCard();
                    } else {
                        mostrarAlerta("Sin cambios", "No se detectaron cambios para guardar.", Alert.AlertType.INFORMATION);
                    }
                } catch (Exception ex) {
                    mostrarAlerta("Error", "No se pudo guardar: " + ex.getMessage(), Alert.AlertType.ERROR);
                }
            });
        } catch (Exception ex) {
            mostrarAlerta("Error", "No se pudo cargar la edición: " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // Método auxiliar para comparar dos CourseDTO
    private boolean equalsCurso(org.unisoftware.gestioncurricular.frontend.dto.CourseDTO a, org.unisoftware.gestioncurricular.frontend.dto.CourseDTO b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        return java.util.Objects.equals(a.getId(), b.getId()) &&
                java.util.Objects.equals(a.getName(), b.getName()) &&
                java.util.Objects.equals(a.getType(), b.getType()) &&
                java.util.Objects.equals(a.getCredits(), b.getCredits()) &&
                java.util.Objects.equals(a.getCycle(), b.getCycle()) &&
                java.util.Objects.equals(a.getArea(), b.getArea()) &&
                java.util.Objects.equals(a.getRequirements(), b.getRequirements());
    }

    // Método para forzar commit de la celda en edición en cualquier columna
    private void commitActiveCell(javafx.scene.control.TableView<?> table) {
        javafx.scene.control.TablePosition<?,?> editingCell = table.getEditingCell();
        if (editingCell != null) {
            int row = editingCell.getRow();
            int col = editingCell.getColumn();
            // Forzar commit del editor activo
            javafx.scene.control.TableColumn<?,?> colObj = table.getColumns().get(col);
            table.getFocusModel().focus(row);
            table.edit(-1, null); // Esto dispara commitEdit si el editor está abierto
        }
    }

    // --- NUEVOS MÉTODOS DE SUBIDA DE ARCHIVOS ---
    private void handleSubirCurriculums(Long programId) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecciona el archivo de curriculums de docentes");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Archivos permitidos", "*.pdf", "*.doc", "*.docx", "*.xlsx", "*.xls", "*.csv")
        );
        Stage stage = (Stage) cardContainer.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
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
            new Thread(() -> {
                try {
                    programFileServiceFront.uploadCurriculum(programId, selectedFile, null);
                    javafx.application.Platform.runLater(() -> {
                        anchorPane.getChildren().remove(overlay);
                        mostrarAlerta("Éxito", "Archivo de curriculums subido correctamente.", Alert.AlertType.INFORMATION);
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

    private void handleSubirResultados(Long programId) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecciona el archivo de resultados de aprendizaje");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Archivos permitidos", "*.pdf", "*.doc", "*.docx", "*.xlsx", "*.xls", "*.csv")
        );
        Stage stage = (Stage) cardContainer.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
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
            new Thread(() -> {
                try {
                    programFileServiceFront.uploadResultados(programId, selectedFile, null);
                    javafx.application.Platform.runLater(() -> {
                        anchorPane.getChildren().remove(overlay);
                        mostrarAlerta("Éxito", "Archivo de resultados subido correctamente.", Alert.AlertType.INFORMATION);
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
}

