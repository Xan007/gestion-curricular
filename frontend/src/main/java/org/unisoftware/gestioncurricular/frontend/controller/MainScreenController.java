package org.unisoftware.gestioncurricular.frontend.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
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
import org.unisoftware.gestioncurricular.frontend.service.ChatServiceFront; // Importar ChatServiceFront
import java.util.UUID; // Importar UUID
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

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
    @FXML private Button btnMisPropuestas; // Nuevo botón para "Mis Propuestas"
    @FXML private Button btnChatIA; // Nuevo botón para el Chat IA

    @Autowired private ProgramServiceFront programServiceFront;
    @Autowired private ExcelUploadService excelUploadService;
    @Autowired private ApplicationContext applicationContext;
    @Autowired private ProgramFileServiceFront programFileServiceFront;
    @Autowired private ProgramFileViewServiceFront programFileViewServiceFront;
    private final ProposalFileServiceFront proposalFileServiceFront = new ProposalFileServiceFront(); // Instanciar el nuevo servicio
    @Autowired private ChatServiceFront chatServiceFront; // Inyectar ChatServiceFront

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

            btnMisPropuestas.setVisible(true); // Hacer visible el nuevo botón
            btnMisPropuestas.setManaged(true); // Manejar el espacio del nuevo botón
            btnMisPropuestas.setOnAction(e -> mostrarMisPropuestasDocente()); // Asignar acción
        } else {
            btnMisCursos.setVisible(false);
            btnMisCursos.setManaged(false);

            btnMisPropuestas.setVisible(false); // Ocultar si no es DOCENTE
            btnMisPropuestas.setManaged(false); // No manejar espacio si no es DOCENTE
        }

        adminPlantelBtn.setOnAction(e -> abrirAdministracionPlantel(e));
        btnPropuestasComite.setOnAction(e -> mostrarPropuestasMicro("Comité de Programa"));
        btnPropuestasEscuela.setOnAction(e -> mostrarPropuestasMicro("Escuela"));
        btnPropuestasPrograma.setOnAction(e -> mostrarPropuestasMicro("Programa"));

        // Configurar el botón del Chat IA
        btnChatIA.setOnAction(e -> abrirVentanaChat());
        btnChatIA.setVisible(true); // Visible para todos los usuarios
        btnChatIA.setManaged(true);

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

        // Botones históricos individuales (ya no están en HBox)
        Button btnVerResultadosHistoricos = new Button("Resultados de Aprendizaje Históricos");
        btnVerResultadosHistoricos.getStyleClass().add("card-btn-white");
        btnVerResultadosHistoricos.setMaxWidth(Double.MAX_VALUE); // Para que ocupe todo el ancho disponible
        btnVerResultadosHistoricos.setOnAction(e -> mostrarArchivosHistoricos("Resultados de Aprendizaje Históricos", () -> programFileViewServiceFront.getHistoricalResultadosUrls(prog.getId())));

        Button btnVerCurriculumsHistoricos = new Button("Currículums Históricos");
        btnVerCurriculumsHistoricos.getStyleClass().add("card-btn-white");
        btnVerCurriculumsHistoricos.setMaxWidth(Double.MAX_VALUE); // Para que ocupe todo el ancho disponible
        btnVerCurriculumsHistoricos.setOnAction(e -> mostrarArchivosHistoricos("Currículums Históricos", () -> programFileViewServiceFront.getHistoricalCurriculumsUrls(prog.getId())));

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
                btnVerResultadosHistoricos,btnVerCurriculumsHistoricos, // <--- Nuevo HBox para históricos
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
            // Obtener años disponibles del backend (puedes ajustar esto según tu lógica real)
            List<Integer> aniosDisponibles = programServiceFront.getAniosPlanEstudios(programaId);
            if (aniosDisponibles == null || aniosDisponibles.isEmpty()) {
                mostrarAlerta("Sin años", "No hay años disponibles para este programa.", Alert.AlertType.INFORMATION);
                return;
            }
            ChoiceDialog<Integer> dialog = new ChoiceDialog<>(aniosDisponibles.get(0), aniosDisponibles);
            dialog.setTitle("Seleccionar año");
            dialog.setHeaderText("¿Qué año desea ver?");
            dialog.setContentText("Año:");
            java.util.Optional<Integer> result = dialog.showAndWait();
            if (result.isPresent()) {
                Integer anioSeleccionado = result.get();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ProgramCoursesScreen.fxml"));
                loader.setControllerFactory(applicationContext::getBean);
                Parent root = loader.load();
                ProgramCoursesScreenController controller = loader.getController();
                controller.initData(programaId, nombrePrograma, anioSeleccionado); // Nuevo parámetro año
                Stage stage = (Stage) cardContainer.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Cursos de " + nombrePrograma);
                stage.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo cargar los años disponibles: " + e.getMessage(), Alert.AlertType.ERROR);
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
                String plantillaUrl = "https://fexiivjyzplakakkiyqm.supabase.co/storage/v1/object/public/documentos-publicos/ejemplos/Plantilla%20Plan%20de%20estudios.xlsx";
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
        AnchorPane anchorPane = (AnchorPane) cardContainer.getScene().getRoot();
        // Eliminar overlay anterior si existe
        StackPane oldOverlay = (StackPane) anchorPane.lookup("#propuestasOverlay");
        if (oldOverlay != null) {
            anchorPane.getChildren().remove(oldOverlay);
        }
        VBox modalContent = new VBox(18);
        modalContent.setStyle("-fx-background-color: #fff; -fx-padding: 32; -fx-background-radius: 14; -fx-effect: dropshadow(three-pass-box, #d32f2f, 12, 0.18, 0, 4); -fx-border-color: #d32f2f; -fx-border-width: 3;");
        modalContent.setPrefWidth(800);
        modalContent.setMinWidth(600);
        modalContent.setAlignment(Pos.CENTER);
        Label titleLabel = new Label("Propuestas Microcurrículo - " + tipo);
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #d32f2f;");
        modalContent.getChildren().add(titleLabel);

        List<Map<String, Object>> propuestas = new ArrayList<>();
        String token = SessionManager.getInstance().getToken();
        String role = SessionManager.getInstance().getUserRole();
        try {
            // Todos los roles de revisión usarán el endpoint general /proposals.
            // El backend se encargará de filtrar las propuestas según el rol.
            String endpointUrl = "http://localhost:8080/proposals";

            if ("DIRECTOR_DE_ESCUELA".equals(role) && "Escuela".equals(tipo)) {
                endpointUrl = "http://localhost:8080/proposals/pending-signatures";
            }

            if ("DIRECTOR_DE_PROGRAMA".equals(role) || "COMITE_DE_PROGRAMA".equals(role) || "DIRECTOR_DE_ESCUELA".equals(role)) {
                java.net.URL urlObj = new java.net.URL(endpointUrl);
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) urlObj.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "Bearer " + token);
                conn.setRequestProperty("Accept", "application/json");
                int responseCode = conn.getResponseCode();
                if (responseCode >= 200 && responseCode < 300) {
                    String responseBody = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    propuestas = mapper.readValue(responseBody, List.class);
                } else {
                    String errorBody = "";
                    if (conn.getErrorStream() != null) {
                        errorBody = new String(conn.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
                    }
                    throw new IOException("Error del servidor (" + responseCode + "): " + errorBody);
                }
            }
        } catch (Exception ex) {
            modalContent.getChildren().add(new Label("Error al cargar propuestas: " + ex.getMessage()));
        }

        if (propuestas.isEmpty()) {
            modalContent.getChildren().add(new Label("No hay propuestas disponibles para su rol y vista actual."));
        } else {
            VBox listaPropuestas = new VBox(12);
            listaPropuestas.setAlignment(Pos.TOP_CENTER);
            for (Map<String, Object> propuesta : propuestas) {
                VBox card = new VBox(8);
                card.setStyle("-fx-background-color: #f7f7fa; -fx-background-radius: 10; -fx-padding: 18; -fx-effect: dropshadow(gaussian, #d32f2f22, 4,0,0,1);");
                card.setMinWidth(500);
                card.setAlignment(Pos.TOP_LEFT);
                String titulo = (String) propuesta.get("title");
                String estado = (String) propuesta.get("status");
                // String observaciones = (String) propuesta.get("observations"); // Ya no se usa directamente
                Map<String, Object> fileInfoMap = (Map<String, Object>) propuesta.get("file"); // Contiene información preliminar del archivo
                Long proposalId = ((Number) propuesta.get("id")).longValue();
                Long courseId = ((Number) propuesta.get("courseId")).longValue();

                Label lblTitulo = new Label("Título: " + titulo);
                lblTitulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #d32f2f;");
                Label lblEstado = new Label("Estado: " + estado);

                String observacionesRaw = (String) propuesta.get("observations");
                String observacionesFormateadas = formatarObservaciones(observacionesRaw);
                Label lblObs = new Label("Observaciones:\n" + observacionesFormateadas);
                lblObs.setWrapText(false);
                // Establecer un ancho máximo para el Label de observaciones para que el texto se ajuste correctamente.
                // El ancho de la tarjeta es 500 (minWidth), con padding de 18 a cada lado (500 - 36 = 464).
                lblObs.setMaxWidth(460);


                Button btnVerArchivo = new Button("Ver Archivo");

                if (fileInfoMap != null) { // Asumimos que si fileInfoMap no es null, hay un archivo potencial
                    btnVerArchivo.setDisable(false);
                    btnVerArchivo.setOnAction(e -> {
                        new Thread(() -> { // Ejecutar en un hilo separado para no bloquear la UI
                            try {
                                // Se utiliza el 'token' del ámbito de mostrarPropuestasMicro
                                ProposalFileDTO fileDto = proposalFileServiceFront.getDisplayUrlDTO(courseId, proposalId, token);

                                if (fileDto != null && fileDto.getUrl() != null && !fileDto.getUrl().isBlank()) {
                                    final String finalUrl = fileDto.getUrl();
                                    javafx.application.Platform.runLater(() -> abrirPdfEnNavegador(() -> finalUrl));
                                } else {
                                    javafx.application.Platform.runLater(() -> mostrarAlerta("Información", "No se encontró URL para el archivo de la propuesta.", Alert.AlertType.INFORMATION));
                                }
                            } catch (Exception ex) {
                                System.err.println("Error al obtener URL del archivo de propuesta: " + ex.getMessage());
                                ex.printStackTrace(); // Para depuración
                                javafx.application.Platform.runLater(() -> mostrarAlerta("Error", "No se pudo obtener la URL del archivo: " + ex.getMessage(), Alert.AlertType.ERROR));
                            }
                        }).start();
                    });
                } else {
                    btnVerArchivo.setDisable(true);
                    btnVerArchivo.setTooltip(new Tooltip("No hay archivo adjunto para esta propuesta o falta información."));
                }
                card.getChildren().addAll(lblTitulo, lblEstado, lblObs, btnVerArchivo);

                // Lógica de acciones según estado y rol
                if ("DIRECTOR_DE_PROGRAMA".equals(role) && "EN_REVISION_DIRECTOR".equals(estado)) {
                    Button btnAprobar = new Button("Aprobar");
                    Button btnRechazar = new Button("Rechazar");
                    btnAprobar.setOnAction(e -> revisarPropuesta(proposalId, true));
                    btnRechazar.setOnAction(e -> revisarPropuesta(proposalId, false));
                    card.getChildren().addAll(btnAprobar, btnRechazar);
                } else if ("COMITE_DE_PROGRAMA".equals(role) && "EN_REVISION_COMITE".equals(estado)) {
                    Button btnAprobar = new Button("Aprobar");
                    Button btnRechazar = new Button("Rechazar");
                    btnAprobar.setOnAction(e -> revisarPropuesta(proposalId, true));
                    btnRechazar.setOnAction(e -> revisarPropuesta(proposalId, false));
                    card.getChildren().addAll(btnAprobar, btnRechazar);
                } else if (("DIRECTOR_DE_PROGRAMA".equals(role) || "DIRECTOR_DE_ESCUELA".equals(role)) && "ESPERANDO_FIRMAS".equals(estado)) {
                    boolean yaFirmoDirectorPrograma = Boolean.TRUE.equals(propuesta.get("signedByDirectorPrograma"));
                    boolean yaFirmoDirectorEscuela = Boolean.TRUE.equals(propuesta.get("signedByDirectorEscuela"));
                    boolean mostrarBotonesFirma = false;

                    if ("DIRECTOR_DE_PROGRAMA".equals(role) && !yaFirmoDirectorPrograma) {
                        mostrarBotonesFirma = true;
                    } else if ("DIRECTOR_DE_ESCUELA".equals(role) && !yaFirmoDirectorEscuela) {
                        mostrarBotonesFirma = true;
                    }

                    if (mostrarBotonesFirma) {
                        Button btnFirmar = new Button("Firmar Propuesta");
                        Button btnRechazarFirma = new Button("Rechazar Firma");
                        btnFirmar.setOnAction(e -> firmarPropuesta(proposalId, true));
                        btnRechazarFirma.setOnAction(e -> firmarPropuesta(proposalId, false));
                        card.getChildren().addAll(btnFirmar, btnRechazarFirma);
                    } else {
                        if (("DIRECTOR_DE_PROGRAMA".equals(role) && yaFirmoDirectorPrograma) ||
                            ("DIRECTOR_DE_ESCUELA".equals(role) && yaFirmoDirectorEscuela)) {
                            Label lblYaFirmado = new Label("Ya has procesado tu firma para esta propuesta.");
                            lblYaFirmado.setStyle("-fx-font-style: italic; -fx-text-fill: #555;");
                            card.getChildren().add(lblYaFirmado);
                        }
                    }
                }

                listaPropuestas.getChildren().add(card);
            }
            // --- CAMBIO: envolver en ScrollPane ---
            ScrollPane scrollPane = new ScrollPane(listaPropuestas);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefViewportHeight(400); // Altura visible
            scrollPane.setMaxHeight(400);
            scrollPane.setMinHeight(200);
            scrollPane.setStyle("-fx-background-color: transparent;");
            modalContent.getChildren().add(scrollPane);
        }
        Button cerrar = new Button("Cerrar");
        cerrar.getStyleClass().add("cerrar-btn");
        modalContent.getChildren().add(cerrar);
        VBox modalWrapper = new VBox();
        modalWrapper.setAlignment(Pos.CENTER);
        modalWrapper.setFillWidth(true);
        modalWrapper.setPrefWidth(800);
        modalWrapper.setMinWidth(600);
        modalWrapper.getChildren().add(modalContent);
        StackPane overlay = new StackPane();
        overlay.setId("propuestasOverlay"); // ID para identificar y eliminar overlays viejos
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

    // Lógica para aprobar/rechazar propuesta
    private void revisarPropuesta(Long id, boolean aprobar) {
        javafx.application.Platform.runLater(() -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle(aprobar ? "Aprobar Propuesta" : "Rechazar Propuesta");
            dialog.setHeaderText("Observaciones (opcional):");
            dialog.setContentText("Observaciones:");
            java.util.Optional<String> result = dialog.showAndWait();
            String observaciones = result.orElse("");
            new Thread(() -> {
                try {
                    String token = SessionManager.getInstance().getToken();
                    String url = "http://localhost:8080/proposals/" + id + "/revisar";
                    java.net.URL urlObj = new java.net.URL(url);
                    java.net.HttpURLConnection conn = (java.net.HttpURLConnection) urlObj.openConnection();
                    conn.setRequestMethod("PUT");
                    conn.setRequestProperty("Authorization", "Bearer " + token);
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setDoOutput(true);
                    String action = aprobar ? "ACCEPT" : "REJECT"; // Corregido aquí
                    String body = String.format("{\"action\":\"%s\",\"observations\":\"%s\"}", action, observaciones.replace("\"", "'"));
                    try (java.io.OutputStream os = conn.getOutputStream()) {
                        os.write(body.getBytes(StandardCharsets.UTF_8));
                    }
                    int responseCode = conn.getResponseCode();
                    if (responseCode >= 200 && responseCode < 300) {
                        javafx.application.Platform.runLater(() -> {
                            mostrarAlerta("Éxito", "Propuesta revisada correctamente.", Alert.AlertType.INFORMATION);
                            // Actualizar la vista de propuestas
                            String currentRole = SessionManager.getInstance().getUserRole();
                            String tipoVista = "";
                            if ("DIRECTOR_DE_PROGRAMA".equals(currentRole)) tipoVista = "Programa";
                            else if ("COMITE_DE_PROGRAMA".equals(currentRole)) tipoVista = "Comité de Programa";
                            else if ("DIRECTOR_DE_ESCUELA".equals(currentRole)) tipoVista = "Escuela";
                            if (!tipoVista.isEmpty()) {
                                mostrarPropuestasMicro(tipoVista);
                            }
                        });
                    } else {
                        String errorBody = "";
                        if (conn.getErrorStream() != null) {
                            errorBody = new String(conn.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
                        }
                        final String finalErrorBody = errorBody;
                        javafx.application.Platform.runLater(() -> mostrarAlerta("Error", "No se pudo revisar la propuesta. Servidor dice: " + finalErrorBody, Alert.AlertType.ERROR));
                    }
                } catch (Exception ex) {
                    javafx.application.Platform.runLater(() -> mostrarAlerta("Error", "No se pudo revisar la propuesta: " + ex.getMessage(), Alert.AlertType.ERROR));
                }
            }).start();
        });
    }

    // Lógica para firmar/rechazar propuesta
    private void firmarPropuesta(Long id, boolean aceptar) {
        javafx.application.Platform.runLater(() -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle(aceptar ? "Firmar Propuesta" : "Rechazar Firma");
            dialog.setHeaderText("Observaciones (opcional):");
            dialog.setContentText("Observaciones:");
            java.util.Optional<String> result = dialog.showAndWait();
            String observaciones = result.orElse("");
            new Thread(() -> {
                try {
                    String token = SessionManager.getInstance().getToken();
                    String url = "http://localhost:8080/proposals/" + id + "/sign";
                    java.net.URL urlObj = new java.net.URL(url);
                    java.net.HttpURLConnection conn = (java.net.HttpURLConnection) urlObj.openConnection();
                    conn.setRequestMethod("PUT");
                    conn.setRequestProperty("Authorization", "Bearer " + token);
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setDoOutput(true);
                    String body = String.format("{\"accept\":%s,\"observations\":\"%s\"}", aceptar, observaciones.replace("\"", "'"));
                    try (java.io.OutputStream os = conn.getOutputStream()) {
                        os.write(body.getBytes(StandardCharsets.UTF_8));
                    }
                    int responseCode = conn.getResponseCode();
                    if (responseCode >= 200 && responseCode < 300) {
                        javafx.application.Platform.runLater(() -> {
                            mostrarAlerta("Éxito", "Firma procesada correctamente.", Alert.AlertType.INFORMATION);
                            // Actualizar la vista de propuestas
                            String currentRole = SessionManager.getInstance().getUserRole();
                            String tipoVista = "";
                            if ("DIRECTOR_DE_PROGRAMA".equals(currentRole)) tipoVista = "Programa";
                            else if ("COMITE_DE_PROGRAMA".equals(currentRole)) tipoVista = "Comité de Programa";
                            else if ("DIRECTOR_DE_ESCUELA".equals(currentRole)) tipoVista = "Escuela";
                            if (!tipoVista.isEmpty()) {
                                mostrarPropuestasMicro(tipoVista);
                            }
                        });
                    } else {
                        String errorBody = "";
                        if (conn.getErrorStream() != null) {
                            errorBody = new String(conn.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
                        }
                        final String finalErrorBody = errorBody;
                        javafx.application.Platform.runLater(() -> mostrarAlerta("Error", "No se pudo procesar la firma. Servidor dice: " + finalErrorBody, Alert.AlertType.ERROR));
                    }
                } catch (Exception ex) {
                    javafx.application.Platform.runLater(() -> mostrarAlerta("Error", "No se pudo procesar la firma: " + ex.getMessage(), Alert.AlertType.ERROR));
                }
            }).start();
        });
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
                    Button btnProponer = new Button("Proponer Micro-Curriculum");
                    btnProponer.getStyleClass().add("primary-button");
                    btnProponer.setOnAction(ev -> subirPropuestaCurriculum(curso));
                    card.getChildren().addAll(nameLbl, codeLbl, btnProponer);
                    cardsHBox.getChildren().add(card);
                }
                // ScrollPane horizontal para los cursos
                ScrollPane scrollPane = new ScrollPane(cardsHBox);
                scrollPane.setFitToHeight(true);
                scrollPane.setFitToWidth(false);
                scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
                scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                scrollPane.setPrefViewportHeight(260);
                scrollPane.setPrefViewportWidth(700);
                scrollPane.setStyle("-fx-background-color: transparent;");
                modalContent.getChildren().add(scrollPane);
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
            // Obtener años disponibles del plan de estudios
            List<Integer> aniosDisponibles = programServiceFront.getAniosPlanEstudios(prog.getId());
            if (aniosDisponibles == null || aniosDisponibles.isEmpty()) {
                mostrarAlerta("Sin años", "No hay años disponibles para este programa.", Alert.AlertType.INFORMATION);
                return;
            }

            // Encontrar el año más reciente (asumiendo que la lista está ordenada, si no, debemos ordenarla)
            Integer anioMasReciente = aniosDisponibles.stream()
                    .max(Integer::compareTo)
                    .orElse(aniosDisponibles.get(0));

            // Obtener cursos del programa (CourseDTO generales)
            org.unisoftware.gestioncurricular.frontend.service.CourseServiceFront courseService = applicationContext.getBean(org.unisoftware.gestioncurricular.frontend.service.CourseServiceFront.class);

            // Obtener las entradas del plan de estudios (StudyPlanEntryDTO) para este programa y año específico
            List<org.unisoftware.gestioncurricular.frontend.dto.StudyPlanEntryDTO> planEntries = null;
            try {
                planEntries = programServiceFront.getStudyPlanByYear(prog.getId(), anioMasReciente);
                if (planEntries == null || planEntries.isEmpty()) {
                    mostrarAlerta("Sin cursos", "No hay cursos disponibles para el plan de estudios del año " + anioMasReciente, Alert.AlertType.INFORMATION);
                    return;
                }
            } catch (Exception e) {
                System.err.println("Error al cargar el plan de estudios para la edición de requisitos: " + e.getMessage());
                mostrarAlerta("Error", "No se pudo cargar el plan de estudios: " + e.getMessage(), Alert.AlertType.ERROR);
                return;
            }

            // Extraer los IDs de los cursos del plan de estudios más reciente
            List<Long> idsCursosDelPlanActual = new ArrayList<>();
            Map<Long, List<Long>> studyPlanRequirementsMap = new java.util.HashMap<>();
            if (planEntries != null) {
                for (org.unisoftware.gestioncurricular.frontend.dto.StudyPlanEntryDTO planEntry : planEntries) {
                    if (planEntry.getId() != null && planEntry.getId().getCourseId() != null) {
                        Long cursoId = planEntry.getId().getCourseId();
                        idsCursosDelPlanActual.add(cursoId);
                        studyPlanRequirementsMap.put(
                                cursoId,
                                planEntry.getRequirements() != null ? planEntry.getRequirements() : new java.util.ArrayList<>()
                        );
                    }
                }
            }

            // Obtener solo los cursos del plan actual
            List<org.unisoftware.gestioncurricular.frontend.dto.CourseDTO> cursos = courseService.listCoursesByIds(idsCursosDelPlanActual);

            if (cursos == null || cursos.isEmpty()) {
                mostrarAlerta("Sin cursos", "No se encontraron detalles de los cursos para el plan de estudios actual.", Alert.AlertType.INFORMATION);
                return;
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

            Label title = new Label("Editar Cursos del Programa - Plan " + anioMasReciente);
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

                    // Detectar todos los cursos que han cambiado antes de hacer peticiones
                    List<org.unisoftware.gestioncurricular.frontend.dto.CourseDTO> cursosModificados = new ArrayList<>();

                    for (int i = 0; i < data.size(); i++) {
                        org.unisoftware.gestioncurricular.frontend.dto.CourseDTO actual = data.get(i);
                        org.unisoftware.gestioncurricular.frontend.dto.CourseDTO original = cursosOriginales.get(i);
                        if (!equalsCurso(actual, original)) {
                            cursosModificados.add(actual);
                        }
                    }

                    if (cursosModificados.isEmpty()) {
                        mostrarAlerta("Sin cambios", "No se detectaron cambios para guardar.", Alert.AlertType.INFORMATION);
                        return;
                    }

                    // Actualizar cursos en un hilo separado para no bloquear la UI
                    new Thread(() -> {
                        boolean huboCambios = false;
                        Exception excepcion = null;

                        try {
                            // Actualizar todos los cursos modificados de una vez
                            for (org.unisoftware.gestioncurricular.frontend.dto.CourseDTO curso : cursosModificados) {
                                courseService.updateCourse(curso);
                                huboCambios = true;
                            }
                        } catch (Exception ex) {
                            excepcion = ex;
                        }

                        final boolean exito = huboCambios && excepcion == null;
                        final Exception errorFinal = excepcion;

                        // Actualizar la UI en el hilo de JavaFX
                        javafx.application.Platform.runLater(() -> {
                            if (exito) {
                                mostrarAlerta("Éxito", "Cursos actualizados correctamente.", Alert.AlertType.INFORMATION);
                                mostrarProgramaCard();
                                // Ya NO cerrar el modal de edición automáticamente
                            } else {
                                mostrarAlerta("Error", "No se pudo guardar: " + (errorFinal != null ? errorFinal.getMessage() : "Error desconocido"), Alert.AlertType.ERROR);
                            }
                        });
                    }).start();

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
        TextInputDialog dialog = new TextInputDialog("yyyy-MM-dd");
        dialog.setTitle("Fecha del Documento");
        dialog.setHeaderText("Ingrese la fecha para el archivo de currículums.");
        dialog.setContentText("Fecha (yyyy-MM-dd o yyyy):");

        java.util.Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && !result.get().isBlank()){
            String dateStr = result.get();
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Selecciona el archivo de currículums de docentes");
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
                        programFileServiceFront.uploadCurriculum(programId, selectedFile, dateStr);
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
    }

    private void handleSubirResultados(Long programId) {
        TextInputDialog dialog = new TextInputDialog("yyyy-MM-dd");
        dialog.setTitle("Fecha del Documento");
        dialog.setHeaderText("Ingrese la fecha para el archivo de resultados de aprendizaje.");
        dialog.setContentText("Fecha (yyyy-MM-dd o yyyy):");

        java.util.Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && !result.get().isBlank()){
            String dateStr = result.get();
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
                        programFileServiceFront.uploadResultados(programId, selectedFile, dateStr);
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

    private String formatarObservaciones(String rawObservations) {
        if (rawObservations == null || rawObservations.isBlank()) {
            return "Sin observaciones";
        }
        StringBuilder formattedObservations = new StringBuilder();
        // Manejar saltos de línea Windows (CRLF) y Unix/Linux (LF)
        String[] entries = rawObservations.split("\\r?\\n|\\n");

        for (String entry : entries) {
            if (entry.trim().isEmpty()) {
                continue; // Saltar líneas vacías
            }
            // Dividir la entrada por el carácter pipe '|'. Pattern.quote se usa para tratar el pipe literalmente.
            String[] parts = entry.split(java.util.regex.Pattern.quote("|"));

            // Se espera que parts[1] sea el rol y parts[3] la acción.
            // La observación puede estar en parts[4]
            if (parts.length >= 4) {
                String role = parts[1].replace("_", " "); // Reemplazar guiones bajos por espacios para mejor legibilidad
                String action = parts[3].replace("_", " "); // Reemplazar guiones bajos por espacios para mejor legibilidad
                String observation = "";
                if (parts.length > 4 && parts[4] != null && !parts[4].trim().isEmpty()) {
                    observation = " - " + parts[4].trim();
                }

                if (formattedObservations.length() > 0) {
                    formattedObservations.append("\n"); // Añadir salto de línea antes de la nueva observación
                }
                formattedObservations.append(role).append(": ").append(action).append(observation);
            }
            // Si una entrada no tiene el formato esperado, se ignora en esta versión.
            // Alternativamente, se podría añadir la entrada original o un mensaje de error.
        }

        if (formattedObservations.length() == 0) {
            // Si ninguna entrada pudo ser formateada (e.g., formato inesperado en todas las líneas)
            return "Sin observaciones (formato no reconocido o vacío)";
        }

        return formattedObservations.toString();
    }

    private void mostrarArchivosHistoricos(String tituloVentana, java.util.function.Supplier<List<String>> urlsSupplier) {
        new Thread(() -> {
            try {
                List<String> todasLasUrls = urlsSupplier.get();
                if (todasLasUrls == null || todasLasUrls.isEmpty()) {
                    javafx.application.Platform.runLater(() -> mostrarAlerta("Información", "No hay archivos históricos para mostrar.", Alert.AlertType.INFORMATION));
                    return;
                }

                Map<Integer, List<String>> urlsPorAnio = new java.util.TreeMap<>(java.util.Collections.reverseOrder()); // TreeMap para ordenar años
                for (String url : todasLasUrls) {
                    String nombreArchivo = obtenerNombreArchivoDeUrl(url);
                    Integer anio = extraerAnioDeNombreArchivo(nombreArchivo);
                    if (anio != null) {
                        urlsPorAnio.computeIfAbsent(anio, k -> new ArrayList<>()).add(url);
                    }
                }

                List<Integer> aniosDisponibles = new ArrayList<>(urlsPorAnio.keySet());
                if (aniosDisponibles.isEmpty()) {
                    javafx.application.Platform.runLater(() -> mostrarAlerta("Información", "No se encontraron archivos históricos con un año identificable en el nombre.", Alert.AlertType.INFORMATION));
                    return;
                }

                javafx.application.Platform.runLater(() -> {
                    ChoiceDialog<Integer> dialogAnio = new ChoiceDialog<>(aniosDisponibles.get(0), aniosDisponibles);
                    dialogAnio.setTitle("Seleccionar Año");
                    dialogAnio.setHeaderText("Seleccione el año para ver los " + tituloVentana.toLowerCase() + ":");
                    dialogAnio.setContentText("Año:");

                    java.util.Optional<Integer> anioSeleccionadoOpt = dialogAnio.showAndWait();
                    if (anioSeleccionadoOpt.isPresent()) {
                        Integer anioSeleccionado = anioSeleccionadoOpt.get();
                        List<String> urlsFiltradas = urlsPorAnio.get(anioSeleccionado);

                        if (urlsFiltradas != null && !urlsFiltradas.isEmpty()) {
                            Stage stage = new Stage();
                            VBox root = new VBox(10);
                            root.setPadding(new javafx.geometry.Insets(10));
                            root.setAlignment(Pos.CENTER_LEFT);
                            Label titleLabel = new Label(tituloVentana + " - Año " + anioSeleccionado);
                            titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
                            root.getChildren().add(titleLabel);

                            for (String url : urlsFiltradas) {
                                Button btnUrl = new Button(obtenerNombreArchivoDeUrl(url));
                                btnUrl.setOnAction(e -> abrirPdfEnNavegador(() -> url));
                                root.getChildren().add(btnUrl);
                            }
                            ScrollPane scrollPane = new ScrollPane(root);
                            scrollPane.setFitToWidth(true);
                            Scene scene = new Scene(scrollPane, 450, 350);
                            stage.setTitle(tituloVentana + " - " + anioSeleccionado);
                            stage.setScene(scene);
                            stage.show();
                        } else {
                            mostrarAlerta("Información", "No hay archivos para el año " + anioSeleccionado + ".", Alert.AlertType.INFORMATION);
                        }
                    }
                });

            } catch (Exception ex) {
                System.err.println("Error al mostrar archivos históricos: " + ex.getMessage());
                ex.printStackTrace();
                javafx.application.Platform.runLater(() -> mostrarAlerta("Error", "No se pudo mostrar los archivos históricos: " + ex.getMessage(), Alert.AlertType.ERROR));
            }
        }).start();
    }

    private String obtenerNombreArchivoDeUrl(String url) {
        try {
            String path = new java.net.URI(url).getPath();
            return new File(path).getName();
        } catch (Exception e) {
            return url; // Devuelve la URL completa si no se puede parsear el nombre
        }
    }

    private Integer extraerAnioDeNombreArchivo(String nombreArchivo) {
        if (nombreArchivo == null) return null;
        // Regex para encontrar un año de 4 dígitos (ej. 2023, 1998)
        // Intenta encontrar años como XXXX o _XXXX o -XXXX o (XXXX)
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(".*([12][0-9]{3}).*");
        java.util.regex.Matcher matcher = pattern.matcher(nombreArchivo);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                // No es un número válido, aunque la regex coincidió
                return null;
            }
        }
        return null;
    }

    // Nuevo método para mostrar las propuestas del docente
    private void mostrarMisPropuestasDocente() {
        AnchorPane anchorPane = (AnchorPane) cardContainer.getScene().getRoot();
        // Eliminar overlay anterior si existe
        StackPane oldOverlay = (StackPane) anchorPane.lookup("#misPropuestasDocenteOverlay");
        if (oldOverlay != null) {
            anchorPane.getChildren().remove(oldOverlay);
        }

        VBox modalContent = new VBox(18);
        modalContent.setStyle("-fx-background-color: #fff; -fx-padding: 32; -fx-background-radius: 14; -fx-effect: dropshadow(three-pass-box, #d32f2f, 12, 0.18, 0, 4); -fx-border-color: #d32f2f; -fx-border-width: 3;");
        modalContent.setPrefWidth(800);
        modalContent.setMinWidth(600);
        modalContent.setAlignment(Pos.CENTER);
        Label titleLabel = new Label("Mis Propuestas de Microcurrículo");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #d32f2f;");
        modalContent.getChildren().add(titleLabel);

        List<Map<String, Object>> propuestas = new ArrayList<>();
        String token = SessionManager.getInstance().getToken();
        // String teacherId = SessionManager.getInstance().getUserId(); // Ya no se necesita para este endpoint

        try {
            // Endpoint para obtener las propuestas del docente actual
            String endpointUrl = "http://localhost:8080/proposals"; // Modificado según la solicitud
            java.net.URL urlObj = new java.net.URL(endpointUrl);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) urlObj.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + token);
            conn.setRequestProperty("Accept", "application/json");

            int responseCode = conn.getResponseCode();
            if (responseCode >= 200 && responseCode < 300) {
                String responseBody = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                propuestas = mapper.readValue(responseBody, List.class);
            } else {
                String errorBody = "";
                if (conn.getErrorStream() != null) {
                    errorBody = new String(conn.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
                }
                throw new IOException("Error del servidor (" + responseCode + "): " + errorBody);
            }
        } catch (Exception ex) {
            modalContent.getChildren().add(new Label("Error al cargar propuestas: " + ex.getMessage()));
        }

        if (propuestas.isEmpty()) {
            modalContent.getChildren().add(new Label("No hay propuestas disponibles."));
        } else {
            VBox listaPropuestas = new VBox(12);
            listaPropuestas.setAlignment(Pos.TOP_CENTER);
            for (Map<String, Object> propuesta : propuestas) {
                VBox card = new VBox(8);
                card.setStyle("-fx-background-color: #f7f7fa; -fx-background-radius: 10; -fx-padding: 18; -fx-effect: dropshadow(gaussian, #d32f2f22, 4,0,0,1);");
                card.setMinWidth(500);
                card.setAlignment(Pos.TOP_LEFT);
                String titulo = (String) propuesta.get("title");
                String estado = (String) propuesta.get("status");
                // String observaciones = (String) propuesta.get("observations"); // Ya no se usa directamente
                Map<String, Object> fileInfoMap = (Map<String, Object>) propuesta.get("file"); // Contiene información preliminar del archivo
                Long proposalId = ((Number) propuesta.get("id")).longValue();
                Long courseId = ((Number) propuesta.get("courseId")).longValue();

                Label lblTitulo = new Label("Título: " + titulo);
                lblTitulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #d32f2f;");
                Label lblEstado = new Label("Estado: " + estado);

                String observacionesRaw = (String) propuesta.get("observations");
                String observacionesFormateadas = formatarObservaciones(observacionesRaw);
                Label lblObs = new Label("Observaciones:\n" + observacionesFormateadas);
                lblObs.setWrapText(false);
                // Establecer un ancho máximo para el Label de observaciones para que el texto se ajuste correctamente.
                // El ancho de la tarjeta es 500 (minWidth), con padding de 18 a cada lado (500 - 36 = 464).
                lblObs.setMaxWidth(460);


                Button btnVerArchivo = new Button("Ver Archivo");

                if (fileInfoMap != null) { // Asumimos que si fileInfoMap no es null, hay un archivo potencial
                    btnVerArchivo.setDisable(false);
                    btnVerArchivo.setOnAction(e -> {
                        new Thread(() -> { // Ejecutar en un hilo separado para no bloquear la UI
                            try {
                                // Se utiliza el 'token' del ámbito de mostrarPropuestasMicro
                                ProposalFileDTO fileDto = proposalFileServiceFront.getDisplayUrlDTO(courseId, proposalId, token);

                                if (fileDto != null && fileDto.getUrl() != null && !fileDto.getUrl().isBlank()) {
                                    final String finalUrl = fileDto.getUrl();
                                    javafx.application.Platform.runLater(() -> abrirPdfEnNavegador(() -> finalUrl));
                                } else {
                                    javafx.application.Platform.runLater(() -> mostrarAlerta("Información", "No se encontró URL para el archivo de la propuesta.", Alert.AlertType.INFORMATION));
                                }
                            } catch (Exception ex) {
                                System.err.println("Error al obtener URL del archivo de propuesta: " + ex.getMessage());
                                ex.printStackTrace(); // Para depuración
                                javafx.application.Platform.runLater(() -> mostrarAlerta("Error", "No se pudo obtener la URL del archivo: " + ex.getMessage(), Alert.AlertType.ERROR));
                            }
                        }).start();
                    });
                } else {
                    btnVerArchivo.setDisable(true);
                    btnVerArchivo.setTooltip(new Tooltip("No hay archivo adjunto para esta propuesta o falta información."));
                }
                card.getChildren().addAll(lblTitulo, lblEstado, lblObs, btnVerArchivo);


                listaPropuestas.getChildren().add(card);
            }
            // --- CAMBIO: envolver en ScrollPane ---
            ScrollPane scrollPane = new ScrollPane(listaPropuestas);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefViewportHeight(400); // Altura visible
            scrollPane.setMaxHeight(400);
            scrollPane.setMinHeight(200);
            scrollPane.setStyle("-fx-background-color: transparent;");
            modalContent.getChildren().add(scrollPane);
        }
        Button cerrar = new Button("Cerrar");
        cerrar.getStyleClass().add("cerrar-btn");
        modalContent.getChildren().add(cerrar);
        VBox modalWrapper = new VBox();
        modalWrapper.setAlignment(Pos.CENTER);
        modalWrapper.setFillWidth(true);
        modalWrapper.setPrefWidth(800);
        modalWrapper.setMinWidth(600);
        modalWrapper.getChildren().add(modalContent);
        StackPane overlay = new StackPane();
        overlay.setId("misPropuestasDocenteOverlay"); // ID para identificar y eliminar overlays viejos
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

    // --- INICIO: Funcionalidad del Chat IA ---
    private void abrirVentanaChat() {
        AnchorPane anchorPane = (AnchorPane) cardContainer.getScene().getRoot();
        StackPane chatOverlay = (StackPane) anchorPane.lookup("#chatOverlay");
        if (chatOverlay != null) {
            anchorPane.getChildren().remove(chatOverlay);
        }

        VBox chatModalContent = new VBox(10);
        chatModalContent.setStyle("-fx-background-color: #fff; -fx-padding: 20; -fx-background-radius: 14; -fx-effect: dropshadow(three-pass-box, #000000, 10, 0.2, 0, 3); -fx-border-color: #cccccc; -fx-border-width: 1;");
        chatModalContent.setPrefSize(500, 600);
        chatModalContent.setMaxSize(500, 600);
        chatModalContent.setMinSize(400, 500);

        Label chatTitle = new Label("Chat con Asistente IA");
        chatTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333;");

        VBox messagesContainer = new VBox(8);
        messagesContainer.setStyle("-fx-background-color: #f9f9f9; -fx-padding: 10; -fx-background-radius: 8;");
        ScrollPane scrollPane = new ScrollPane(messagesContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        VBox.setVgrow(scrollPane, javafx.scene.layout.Priority.ALWAYS); // Para que el scrollpane crezca

        // Auto-scroll hacia abajo
        messagesContainer.heightProperty().addListener(observable -> scrollPane.setVvalue(1.0));


        TextField inputField = new TextField();
        inputField.setPromptText("Escribe tu mensaje...");
        inputField.setStyle("-fx-font-size: 14px;");

        Button sendButton = new Button("Enviar");
        sendButton.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        sendButton.setDefaultButton(true); // Permite enviar con Enter

        HBox inputBox = new HBox(10, inputField, sendButton);
        HBox.setHgrow(inputField, javafx.scene.layout.Priority.ALWAYS);
        inputBox.setAlignment(Pos.CENTER);

        sendButton.setOnAction(e -> {
            String message = inputField.getText();
            if (message != null && !message.trim().isEmpty()) {
                inputField.clear();
                agregarMensajeUsuario(messagesContainer, message);

                // Enviar mensaje al backend en un nuevo hilo
                new Thread(() -> {
                    try {
                        String response = chatServiceFront.sendMessage(message);
                        javafx.application.Platform.runLater(() -> {
                            Label iaLabel = new Label(""); // Crear Label vacío
                            iaLabel.setWrapText(true);
                            iaLabel.setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: black; -fx-padding: 8px; -fx-background-radius: 10px 10px 10px 0; -fx-font-size: 14px;");
                            iaLabel.setMaxWidth(Double.MAX_VALUE);

                            HBox messageRow = new HBox(iaLabel);
                            messageRow.setAlignment(Pos.CENTER_LEFT);
                            messageRow.setPadding(new javafx.geometry.Insets(0, 50, 0, 0));
                            messagesContainer.getChildren().add(messageRow);

                            agregarMensajeIAConEfectoTipeo(iaLabel, response); // Llamar al nuevo método
                        });
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        javafx.application.Platform.runLater(() -> agregarMensajeError(messagesContainer, "Error al conectar con el asistente: " + ex.getMessage()));
                    }
                }).start();
            }
        });

        Button cerrarChatButton = new Button("Cerrar");
        cerrarChatButton.setStyle("-fx-background-color: #757575; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");


        chatModalContent.getChildren().addAll(chatTitle, scrollPane, inputBox, cerrarChatButton);

        VBox modalWrapper = new VBox(chatModalContent);
        modalWrapper.setAlignment(Pos.CENTER);
        modalWrapper.setPrefSize(500, 600);
        modalWrapper.setMaxSize(500, 600);


        StackPane overlay = new StackPane();
        overlay.setId("chatOverlay");
        overlay.setStyle("-fx-background-color: rgba(30,32,48,0.18);");
        overlay.setPickOnBounds(true);
        overlay.setPrefSize(anchorPane.getWidth(), anchorPane.getHeight());
        overlay.setAlignment(Pos.CENTER);
        overlay.getChildren().add(modalWrapper);

        anchorPane.getChildren().add(overlay);
        AnchorPane.setTopAnchor(overlay, 0.0);
        AnchorPane.setBottomAnchor(overlay, 0.0);
        AnchorPane.setLeftAnchor(overlay, 0.0);
        AnchorPane.setRightAnchor(overlay, 0.0);

        cerrarChatButton.setOnAction(ev -> anchorPane.getChildren().remove(overlay));
    }

    private void agregarMensajeUsuario(VBox container, String texto) {
        Label userLabel = new Label(texto);
        userLabel.setWrapText(true);
        userLabel.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white; -fx-padding: 8px; -fx-background-radius: 10px 10px 0 10px; -fx-font-size: 14px;");
        userLabel.setMaxWidth(Double.MAX_VALUE); // Para que ocupe el ancho disponible

        HBox messageRow = new HBox(userLabel);
        messageRow.setAlignment(Pos.CENTER_RIGHT); // Alinea el mensaje del usuario a la derecha
        messageRow.setPadding(new javafx.geometry.Insets(0, 0, 0, 50)); // Margen a la izquierda para no ocupar todo el ancho
        container.getChildren().add(messageRow);
    }

    private void agregarMensajeIA(VBox container, String texto) {
        Label iaLabel = new Label(texto);
        iaLabel.setWrapText(true);
        iaLabel.setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: black; -fx-padding: 8px; -fx-background-radius: 10px 10px 10px 0; -fx-font-size: 14px;");
        iaLabel.setMaxWidth(Double.MAX_VALUE); // Para que ocupe el ancho disponible

        HBox messageRow = new HBox(iaLabel);
        messageRow.setAlignment(Pos.CENTER_LEFT); // Alinea el mensaje de la IA a la izquierda
        messageRow.setPadding(new javafx.geometry.Insets(0, 50, 0, 0)); // Margen a la derecha
        container.getChildren().add(messageRow);
    }

    private void agregarMensajeIAConEfectoTipeo(Label iaLabel, String textoCompleto) {
        final Timeline timeline = new Timeline();
        final String[] textoParcial = {""};
        for (int i = 0; i < textoCompleto.length(); i++) {
            final int index = i;
            KeyFrame keyFrame = new KeyFrame(Duration.millis(20 * i), event -> { // Reducido de 50 a 20 para mayor velocidad
                textoParcial[0] += textoCompleto.charAt(index);
                iaLabel.setText(textoParcial[0]);
            });
            timeline.getKeyFrames().add(keyFrame);
        }
        timeline.play();
    }

    private void agregarMensajeError(VBox container, String texto) {
        Label errorLabel = new Label("Error: " + texto);
        errorLabel.setWrapText(true);
        errorLabel.setStyle("-fx-background-color: #ffcdd2; -fx-text-fill: #c62828; -fx-padding: 8px; -fx-background-radius: 10px; -fx-font-size: 14px;");
        errorLabel.setMaxWidth(Double.MAX_VALUE);

        HBox messageRow = new HBox(errorLabel);
        messageRow.setAlignment(Pos.CENTER_LEFT);
        container.getChildren().add(messageRow);
    }
    // --- FIN: Funcionalidad del Chat IA ---

} // Este es el cierre de la clase MainScreenController
