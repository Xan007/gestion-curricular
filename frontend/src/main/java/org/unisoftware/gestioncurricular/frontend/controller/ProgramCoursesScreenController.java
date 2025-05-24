package org.unisoftware.gestioncurricular.frontend.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.ArrayList;

import org.unisoftware.gestioncurricular.frontend.service.CourseServiceFront;
import org.unisoftware.gestioncurricular.frontend.util.SessionManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.unisoftware.gestioncurricular.frontend.dto.StudyPlanEntryDTO;
import org.unisoftware.gestioncurricular.frontend.service.ProgramServiceFront;

import java.io.IOException;
import java.util.List;
import javafx.scene.control.Alert;
import org.unisoftware.gestioncurricular.frontend.dto.CourseDTO;
import java.util.UUID;
import org.unisoftware.gestioncurricular.frontend.service.UserServiceFront;
import org.unisoftware.gestioncurricular.frontend.dto.UserInfoDTO;
import org.unisoftware.gestioncurricular.frontend.dto.FileUploadInfoDTO;
import org.unisoftware.gestioncurricular.frontend.service.CourseFileServiceFront;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ChoiceDialog;
import java.util.Arrays;
import java.io.File; // Asegúrate que File está importado
import javafx.stage.FileChooser; // Asegúrate que FileChooser está importado
import java.nio.file.Files; // Para Files.probeContentType
import javafx.scene.web.WebView; // Necesario para abrirPdfEnNavegador
import java.net.URLEncoder; // Necesario para abrirPdfEnNavegador
import java.nio.charset.StandardCharsets; // Necesario para abrirPdfEnNavegador
import java.util.function.Supplier; // Necesario para abrirPdfEnNavegador

@Component
public class ProgramCoursesScreenController {

    @FXML private VBox coursesContainer;
    @FXML private Label programNameLabel;
    @FXML private Button btnAsignarDocente;

    @Autowired private ProgramServiceFront programServiceFront;
    @Autowired private ApplicationContext applicationContext;
    @Autowired private CourseServiceFront courseServiceFront;
    @Autowired private UserServiceFront userServiceFront;
    private final CourseFileServiceFront courseFileServiceFront = new CourseFileServiceFront(); // Nuevo servicio

    private Long programaId;

    public void initData(Long programaId, String nombrePrograma) {
        this.programaId = programaId;
        programNameLabel.setText("Plan de Estudios de: " + nombrePrograma);
        // Mostrar botón solo si el usuario es DIRECTOR_DE_PROGRAMA
        if (SessionManager.getInstance().hasRole("DIRECTOR_DE_PROGRAMA")) {
            btnAsignarDocente.setVisible(true);
            btnAsignarDocente.setManaged(true);
        } else {
            btnAsignarDocente.setVisible(false);
            btnAsignarDocente.setManaged(false);
        }
        btnAsignarDocente.setOnAction(e -> handleAsignarDocente());
        cargarPlanEstudios();
    }

    private void cargarPlanEstudios() {
        coursesContainer.getChildren().clear();
        try {
            List<StudyPlanEntryDTO> plan = programServiceFront.getStudyPlan(programaId);

            if (plan == null || plan.isEmpty()) {
                coursesContainer.getChildren().add(new Label(
                        "No hay cursos registrados en el plan de estudios para este programa."
                ));
                return;
            }

            // Crear un mapa para buscar nombres a partir del ID
            Map<Long, String> idNombreCurso = new HashMap<>();
            for (StudyPlanEntryDTO entry : plan) {
                if (entry.getId() != null && entry.getName() != null) {
                    idNombreCurso.put(entry.getId().getCourseId(), entry.getName());
                }
            }

            // Mapear los cursos por semestre
            Map<Integer, List<StudyPlanEntryDTO>> cursosPorSemestre = new TreeMap<>();
            for (StudyPlanEntryDTO entry : plan) {
                if (entry.getSemester() != null) {
                    cursosPorSemestre
                            .computeIfAbsent(entry.getSemester(), k -> new ArrayList<>())
                            .add(entry);
                }
            }

            HBox filaSemestres = new HBox(25);
            filaSemestres.setAlignment(Pos.TOP_CENTER);

            // Mapa para guardar la referencia visual de cada curso
            Map<Long, VBox> idCardMap = new HashMap<>();
            Map<Long, Integer> idSemestreMap = new HashMap<>();
            Map<Long, Integer> idIndexInSemestre = new HashMap<>();

            int semIdx = 0;
            for (Map.Entry<Integer, List<StudyPlanEntryDTO>> semestre : cursosPorSemestre.entrySet()) {
                Integer numeroSemestre = semestre.getKey();
                List<StudyPlanEntryDTO> cursos = semestre.getValue();

                VBox columnaSemestre = new VBox(12);
                columnaSemestre.setAlignment(Pos.TOP_CENTER);

                Label semLabel = new Label("Semestre " + numeroSemestre);
                semLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 0 0 6 0");
                columnaSemestre.getChildren().add(semLabel);

                int idx = 0;
                for (StudyPlanEntryDTO entry : cursos) {
                    VBox card = new VBox(4);
                    card.setAlignment(Pos.TOP_LEFT);
                    card.getStyleClass().add("course-card");
                    // Mostrar solo código y nombre
                    String codigo = entry.getId() != null && entry.getId().getCourseId() != null ? entry.getId().getCourseId().toString() : "";
                    String nombre = entry.getName() != null ? entry.getName() : "";
                    Label lcodigo = new Label(codigo);
                    lcodigo.setStyle("-fx-font-weight:bold; -fx-font-size:13px; -fx-text-fill: #fff; -fx-background-color: #d32f2f; -fx-background-radius: 8; -fx-padding: 2 8 2 8;");
                    Label lnombre = new Label(nombre);
                    lnombre.setStyle("-fx-font-weight:bold; -fx-font-size:14px; -fx-text-fill: #222; ");
                    lnombre.setWrapText(true);
                    lnombre.setMaxWidth(220);
                    card.getChildren().addAll(lcodigo, lnombre);
                    card.setOnMouseClicked(e -> mostrarDetalleCurso(entry, idNombreCurso));
                    columnaSemestre.getChildren().add(card);
                    // Guardar referencia visual
                    if (entry.getId() != null && entry.getId().getCourseId() != null) {
                        idCardMap.put(entry.getId().getCourseId(), card);
                        idSemestreMap.put(entry.getId().getCourseId(), semIdx);
                        idIndexInSemestre.put(entry.getId().getCourseId(), idx);
                    }
                    idx++;
                }

                filaSemestres.getChildren().add(columnaSemestre);
                semIdx++;
            }

            StackPane stack = new StackPane();
            stack.getChildren().add(filaSemestres);

            ScrollPane scroll = new ScrollPane(stack);
            scroll.setFitToHeight(true);
            scroll.setFitToWidth(false);
            scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

            coursesContainer.getChildren().add(scroll);

            Runnable drawLines = () -> {
                stack.getChildren().removeIf(n -> n instanceof javafx.scene.canvas.Canvas);
                javafx.scene.canvas.Canvas canvas = new javafx.scene.canvas.Canvas(stack.getWidth(), stack.getHeight());
                javafx.scene.canvas.GraphicsContext gc = canvas.getGraphicsContext2D();
                gc.setStroke(javafx.scene.paint.Color.web("#d32f2f", 0.7));
                gc.setLineWidth(3);

                Map<String, Integer> usedOffsets = new HashMap<>();
                int offsetStep = 18;

                for (StudyPlanEntryDTO entry : plan) {
                    // Usa directamente getRequirements() que devuelve List<Long>
                    List<Long> currentEntryReqList = entry.getRequirements();

                    if (currentEntryReqList != null && !currentEntryReqList.isEmpty() && entry.getId() != null && entry.getId().getCourseId() != null) {
                        Long depId = entry.getId().getCourseId();
                        VBox depCard = idCardMap.get(depId);
                        if (depCard == null) continue;

                        int colorHash = Math.abs(depId.hashCode());
                        String[] colores = {"#d32f2f", "#1976d2", "#388e3c", "#fbc02d", "#7b1fa2", "#0288d1", "#c2185b", "#ffa000", "#388e3c", "#512da8", "#455a64", "#e64a19", "#009688", "#e91e63", "#8bc34a", "#ff5722", "#607d8b"};
                        String colorLinea = colores[colorHash % colores.length];
                        gc.setStroke(javafx.scene.paint.Color.web(colorLinea, 0.85));

                        for (Long reqId : currentEntryReqList) {
                            VBox reqCard = idCardMap.get(reqId);
                            if (reqCard == null) continue;
                            Integer semReq = idSemestreMap.get(reqId);
                            Integer semDep = idSemestreMap.get(depId);
                            if (semReq == null || semDep == null || semReq.equals(semDep)) continue;
                            // Calcular puntos de conexión: centro del borde derecho de reqCard y centro del borde izquierdo de depCard
                            javafx.geometry.Point2D reqScene = reqCard.localToScene(reqCard.getWidth(), reqCard.getHeight() / 2.0);
                            javafx.geometry.Point2D depScene = depCard.localToScene(0, depCard.getHeight() / 2.0);
                            javafx.geometry.Point2D reqStack = stack.sceneToLocal(reqScene);
                            javafx.geometry.Point2D depStack = stack.sceneToLocal(depScene);
                            double x1 = reqStack.getX();
                            double y1 = reqStack.getY();
                            double x2 = depStack.getX();
                            double y2 = depStack.getY();
                            // Línea en L: primero horizontal desde reqCard hasta antes de la card destino, luego vertical, y finalmente horizontal hasta el borde de la card destino
                            double separation = 16; // separación mínima para no tocar las cards
                            double xL = x2 - separation; // punto antes de la card destino
                            gc.beginPath();
                            gc.moveTo(x1, y1); // centro del borde derecho de reqCard
                            gc.lineTo(xL, y1); // horizontal hasta antes de la card destino
                            gc.lineTo(xL, y2); // vertical hasta la altura de la card destino
                            gc.lineTo(x2, y2); // horizontal hasta el centro del borde izquierdo de depCard
                            gc.stroke();
                        }
                    }
                }
                stack.getChildren().add(0, canvas);
            };

            // Redibujar líneas cuando cambie el tamaño del stack o scroll
            stack.widthProperty().addListener((obs, oldVal, newVal) -> javafx.application.Platform.runLater(drawLines));
            stack.heightProperty().addListener((obs, oldVal, newVal) -> javafx.application.Platform.runLater(drawLines));
            scroll.viewportBoundsProperty().addListener((obs, oldVal, newVal) -> javafx.application.Platform.runLater(drawLines));
            javafx.application.Platform.runLater(drawLines);

        } catch (Exception e) {
            coursesContainer.getChildren().add(new Label("Error cargando plan de estudios: " + e.getMessage()));
        }
    }

    private void mostrarDetalleCurso(StudyPlanEntryDTO entry, Map<Long, String> idNombreCurso) {
        VBox modalContent = new VBox(16);
        modalContent.setStyle("-fx-background-color: #fff; -fx-padding: 32; -fx-background-radius: 14; -fx-effect: dropshadow(three-pass-box, #222, 12, 0.18, 0, 4);");
        modalContent.setMaxWidth(520);
        modalContent.setMinWidth(320);
        modalContent.setAlignment(Pos.TOP_LEFT);

        // Obtener información actualizada del curso desde el backend
        try {
            if (entry.getId() != null && entry.getId().getCourseId() != null) {
                Long cursoId = entry.getId().getCourseId();
                // Obtenemos todos los cursos del programa y buscamos el actualizado por ID
                List<CourseDTO> cursos = courseServiceFront.listCoursesByProgramaId(programaId);
                for (CourseDTO curso : cursos) {
                    if (curso.getId().equals(cursoId)) {
                        // Actualizar los campos con la información más reciente
                        entry.setName(curso.getName());
                        entry.setType(curso.getType());
                        entry.setCredits(curso.getCredits());
                        entry.setCycle(curso.getCycle());
                        entry.setArea(curso.getArea());

                        // NO sobreescribir los requisitos del StudyPlanEntryDTO con los del CourseDTO genérico.
                        // Los requisitos que se mostrarán son los que 'entry' (StudyPlanEntryDTO) ya tiene del plan de estudios.
                        // entry.setRequirements(curso.getRequirements()); // Esta línea se comenta o elimina.
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            System.err.println("Error al actualizar información del curso: " + ex.getMessage());
            // Continuamos con la información que ya teníamos
        }

        Label title = new Label("Detalles del Curso");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1a2233;");
        Label codigo = new Label("Código: " + (entry.getId() != null ? entry.getId().getCourseId() : ""));
        Label nombre = new Label("Nombre: " + (entry.getName() != null ? entry.getName() : ""));
        nombre.setWrapText(true);
        Label area = new Label("Área: " + (entry.getArea() != null ? entry.getArea() : "N/A"));
        Label ciclo = new Label("Ciclo: " + (entry.getCycle() != null ? entry.getCycle() : "N/A"));
        Label tipo = new Label("Tipo: " + (entry.getType() != null ? entry.getType() : "N/A"));
        Label creditos = new Label("Créditos: " + (entry.getCredits() != null ? entry.getCredits().toString() : "N/A"));
        Label relacion = new Label("Relación: " + (entry.getRelation() != null ? entry.getRelation() : "Ninguna"));

        // Usa directamente getRequirements() que devuelve List<Long>
        List<Long> requisitosList = entry.getRequirements();

        String requisitosDisplayString;
        if (requisitosList != null && !requisitosList.isEmpty()) { // Verificar si es null antes de isEmpty()
            List<String> nombresReq = new ArrayList<>();
            for (Long reqId : requisitosList) {
                String nombreReq = idNombreCurso.getOrDefault(reqId, reqId.toString());
                nombresReq.add(nombreReq);
            }
            requisitosDisplayString = String.join(", ", nombresReq);
        } else {
            requisitosDisplayString = "Ninguno";
        }
        Label requisitos = new Label("Requisitos: " + requisitosDisplayString);

        Button cerrar = new Button("Cerrar");
        cerrar.getStyleClass().add("cerrar-btn");

        // Botones para visualizar archivos (todos los usuarios)
        HBox botonesVisualizarArchivos = new HBox(10);
        botonesVisualizarArchivos.setAlignment(Pos.CENTER_LEFT);

        Button btnVerApoyos = new Button("Ver Archivos de Apoyo");
        btnVerApoyos.getStyleClass().add("card-btn-white");
        btnVerApoyos.setOnAction(e -> {
            if (entry.getId() != null && entry.getId().getCourseId() != null) {
                handleVerArchivosApoyo(entry.getId().getCourseId());
            } else {
                mostrarAlerta("Error", "No se pudo obtener el ID del curso.", Alert.AlertType.ERROR);
            }
        });

        Button btnVerMicro = new Button("Ver Microcurrículo");
        btnVerMicro.getStyleClass().add("card-btn-white");
        btnVerMicro.setOnAction(e -> {
            if (entry.getId() != null && entry.getId().getCourseId() != null) {
                handleVerMicrocurriculum(entry.getId().getCourseId());
            } else {
                mostrarAlerta("Error", "No se pudo obtener el ID del curso.", Alert.AlertType.ERROR);
            }
        });
        botonesVisualizarArchivos.getChildren().addAll(btnVerApoyos, btnVerMicro);


        // Botones para subir archivos (solo para Director de Programa)
        HBox botonesArchivos = new HBox(10);
        botonesArchivos.setAlignment(Pos.CENTER_LEFT);
        if (SessionManager.getInstance().hasRole("DIRECTOR_DE_PROGRAMA")) {
            Button btnSubirApoyo = new Button("Subir Archivo de Apoyo");
            btnSubirApoyo.getStyleClass().add("card-btn-white");
            btnSubirApoyo.setOnAction(e -> {
                if (entry.getId() != null && entry.getId().getCourseId() != null) {
                    handleSubirArchivoApoyo(entry.getId().getCourseId());
                } else {
                    mostrarAlerta("Error", "No se pudo obtener el ID del curso.", Alert.AlertType.ERROR);
                }
            });

            Button btnSubirMicro = new Button("Subir Microcurrículo");
            btnSubirMicro.getStyleClass().add("card-btn-white");
            btnSubirMicro.setOnAction(e -> {
                if (entry.getId() != null && entry.getId().getCourseId() != null) {
                    handleSubirMicrocurriculum(entry.getId().getCourseId());
                } else {
                    mostrarAlerta("Error", "No se pudo obtener el ID del curso.", Alert.AlertType.ERROR);
                }
            });

            botonesArchivos.getChildren().addAll(btnSubirApoyo, btnSubirMicro);
        }

        modalContent.getChildren().addAll(title, codigo, nombre, area, ciclo, tipo, creditos, relacion, requisitos);
        modalContent.getChildren().add(botonesVisualizarArchivos); // Añadir botones de visualización
        if (!botonesArchivos.getChildren().isEmpty()) {
            modalContent.getChildren().add(botonesArchivos);
        }
        modalContent.getChildren().add(cerrar);

        // Obtener el AnchorPane raíz de la escena
        AnchorPane anchorPane = (AnchorPane) coursesContainer.getScene().getRoot();

        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(30,32,48,0.18);");
        overlay.setPickOnBounds(true);
        overlay.setPrefSize(anchorPane.getWidth(), anchorPane.getHeight());
        overlay.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        overlay.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        overlay.setAlignment(Pos.CENTER);

        VBox modalWrapper = new VBox();
        modalWrapper.setAlignment(Pos.CENTER);
        modalWrapper.setFillWidth(true);
        modalWrapper.getChildren().add(modalContent);
        overlay.getChildren().add(modalWrapper);

        // Cambiar borde de la ventana de información a rojo
        modalContent.setStyle("-fx-background-color: #fff; -fx-padding: 32; -fx-background-radius: 14; -fx-effect: dropshadow(three-pass-box, #d32f2f, 12, 0.18, 0, 4); -fx-border-color: #d32f2f; -fx-border-width: 3;");

        anchorPane.getChildren().add(overlay);
        AnchorPane.setTopAnchor(overlay, 0.0);
        AnchorPane.setBottomAnchor(overlay, 0.0);
        AnchorPane.setLeftAnchor(overlay, 0.0);
        AnchorPane.setRightAnchor(overlay, 0.0);

        final StackPane overlayFinal = overlay;
        cerrar.setOnAction(ev -> anchorPane.getChildren().remove(overlayFinal));
    }

    private void handleSubirArchivoApoyo(Long courseId) {
        // 1. Pedir tipo de apoyo
        List<String> tiposApoyo = Arrays.asList("GUIA_DE_LABORATORIO", "GUIA_TEORICA", "OTRO");
        ChoiceDialog<String> dialogTipo = new ChoiceDialog<>(tiposApoyo.get(0), tiposApoyo);
        dialogTipo.setTitle("Tipo de Archivo de Apoyo");
        dialogTipo.setHeaderText("Seleccione el tipo de archivo de apoyo que desea subir.");
        dialogTipo.setContentText("Tipo:");
        applyDialogStyles(dialogTipo.getDialogPane());

        java.util.Optional<String> tipoResult = dialogTipo.showAndWait();
        if (!tipoResult.isPresent()) {
            return; // Usuario canceló
        }
        String tipoSeleccionado = tipoResult.get();

        // 2. Seleccionar archivo
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Archivo de Apoyo para el curso " + courseId);
        File selectedFile = fileChooser.showOpenDialog(coursesContainer.getScene().getWindow());

        if (selectedFile != null) {
            StackPane overlay = crearOverlayCarga("Subiendo archivo de apoyo...");
            new Thread(() -> {
                try {
                    String token = SessionManager.getInstance().getToken();
                    // 3. Obtener URL de subida del backend
                    FileUploadInfoDTO uploadInfo = courseFileServiceFront.getApoyoUploadUrl(courseId, selectedFile.getName(), token);
                    String presignedUrl = uploadInfo.getUploadUrl();
                    String fileId = uploadInfo.getFileId();

                    if (presignedUrl == null) {
                        throw new IOException("No se pudo obtener la URL prefirmada del backend.");
                    }

                    // 4. Subir archivo a la URL prefirmada
                    String contentType = Files.probeContentType(selectedFile.toPath());
                    contentType = (contentType == null) ? "application/octet-stream" : contentType;
                    courseFileServiceFront.uploadFileToPresignedUrl(presignedUrl, selectedFile, contentType, token);

                    // 5. Registrar el archivo de apoyo en el backend SOLO si hay fileId
                    if (fileId != null && !fileId.isEmpty()) {
                        courseFileServiceFront.registerApoyoAcademico(courseId, fileId, tipoSeleccionado, token);
                        javafx.application.Platform.runLater(() -> {
                            removerOverlayCarga(overlay);
                            mostrarAlerta("Éxito", "Archivo de apoyo '" + selectedFile.getName() + "' subido y registrado como " + tipoSeleccionado + " correctamente.", Alert.AlertType.INFORMATION);
                        });
                    } else {
                        javafx.application.Platform.runLater(() -> {
                            removerOverlayCarga(overlay);
                            mostrarAlerta("Advertencia", "El archivo fue subido pero no se pudo registrar en la plataforma porque el backend no retornó un identificador único (UUID). El archivo no será visible ni gestionable desde la plataforma.", Alert.AlertType.WARNING);
                        });
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    javafx.application.Platform.runLater(() -> {
                        removerOverlayCarga(overlay);
                        mostrarAlerta("Error", "No se pudo subir el archivo de apoyo: " + ex.getMessage(), Alert.AlertType.ERROR);
                    });
                }
            }).start();
        }
    }

    private void handleSubirMicrocurriculum(Long courseId) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Archivo de Microcurrículo para el curso " + courseId);
        File selectedFile = fileChooser.showOpenDialog(coursesContainer.getScene().getWindow());

        if (selectedFile != null) {
            StackPane overlay = crearOverlayCarga("Subiendo microcurrículo...");
            new Thread(() -> {
                try {
                    String token = SessionManager.getInstance().getToken();
                    // 1. Obtener URL de subida del backend
                    FileUploadInfoDTO uploadInfo = courseFileServiceFront.getMicrocurriculumUploadUrl(courseId, token);
                    String presignedUrl = uploadInfo.getUploadUrl();
                    // String fileId = uploadInfo.getFileId(); // El fileId se extrae y está en uploadInfo.getFileId()
                    // No es explícitamente necesario para un POST de registro separado aquí,
                    // ya que el backend asocia el microcurrículo al subirlo a la URL.

                    if (presignedUrl == null) {
                        throw new IOException("No se pudo obtener la URL prefirmada del backend para el microcurrículo.");
                    }

                    // 2. Subir archivo a la URL prefirmada
                    String contentType = Files.probeContentType(selectedFile.toPath());
                    contentType = (contentType == null) ? "application/octet-stream" : contentType;
                    courseFileServiceFront.uploadFileToPresignedUrl(presignedUrl, selectedFile, contentType, token);

                    javafx.application.Platform.runLater(() -> {
                        removerOverlayCarga(overlay);
                        mostrarAlerta("Éxito", "Microcurrículo \"" + selectedFile.getName() + "\" subido correctamente.", Alert.AlertType.INFORMATION);
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                    javafx.application.Platform.runLater(() -> {
                        removerOverlayCarga(overlay);
                        mostrarAlerta("Error", "No se pudo subir el microcurrículo: " + ex.getMessage(), Alert.AlertType.ERROR);
                    });
                }
            }).start();
        }
    }

    // Métodos de ayuda para overlay de carga (refactorizados)
    private StackPane crearOverlayCarga(String mensaje) {
        VBox modalContentCarga = new VBox(18);
        modalContentCarga.setStyle("-fx-background-color: #fff; -fx-padding: 32; -fx-background-radius: 14; -fx-effect: dropshadow(three-pass-box, #d32f2f, 12, 0.18, 0, 4); -fx-border-color: #d32f2f; -fx-border-width: 3;");
        modalContentCarga.setPrefWidth(400);
        modalContentCarga.setMinWidth(320);
        modalContentCarga.setAlignment(Pos.CENTER);
        Label esperando = new Label(mensaje);
        esperando.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #d32f2f;");
        modalContentCarga.getChildren().add(esperando);

        VBox modalWrapperCarga = new VBox(modalContentCarga);
        modalWrapperCarga.setAlignment(Pos.CENTER);

        AnchorPane anchorPane = (AnchorPane) coursesContainer.getScene().getRoot();
        StackPane overlayCarga = new StackPane(modalWrapperCarga);
        overlayCarga.setStyle("-fx-background-color: rgba(30,32,48,0.18);");
        overlayCarga.setPrefSize(anchorPane.getWidth(), anchorPane.getHeight());
        overlayCarga.setAlignment(Pos.CENTER);

        anchorPane.getChildren().add(overlayCarga);
        AnchorPane.setTopAnchor(overlayCarga, 0.0);
        AnchorPane.setBottomAnchor(overlayCarga, 0.0);
        AnchorPane.setLeftAnchor(overlayCarga, 0.0);
        AnchorPane.setRightAnchor(overlayCarga, 0.0);
        return overlayCarga;
    }

    private void removerOverlayCarga(StackPane overlay) {
        if (overlay != null && overlay.getParent() instanceof AnchorPane) {
            ((AnchorPane) overlay.getParent()).getChildren().remove(overlay);
        }
    }

    private void handleVerArchivosApoyo(Long courseId) {
        try {
            Map<String, String> apoyosPorTipo = courseFileServiceFront.getApoyosUrlsPorTipo(courseId);
            if (apoyosPorTipo == null || apoyosPorTipo.isEmpty()) {
                mostrarAlerta("Información", "No hay archivos de apoyo disponibles para este curso.", Alert.AlertType.INFORMATION);
                return;
            }

            List<String> tiposDisponibles = new ArrayList<>(apoyosPorTipo.keySet());
            if (tiposDisponibles.isEmpty()) {
                mostrarAlerta("Información", "No hay tipos de archivos de apoyo definidos.", Alert.AlertType.INFORMATION);
                return;
            }

            ChoiceDialog<String> dialogTipo = new ChoiceDialog<>(tiposDisponibles.get(0), tiposDisponibles);
            dialogTipo.setTitle("Ver Archivo de Apoyo");
            dialogTipo.setHeaderText("Seleccione el tipo de archivo de apoyo que desea ver.");
            dialogTipo.setContentText("Tipo:");
            applyDialogStyles(dialogTipo.getDialogPane());

            java.util.Optional<String> tipoResult = dialogTipo.showAndWait();
            if (tipoResult.isPresent()) {
                String tipoSeleccionado = tipoResult.get();
                String urlArchivo = apoyosPorTipo.get(tipoSeleccionado);
                if (urlArchivo != null && !urlArchivo.isBlank()) {
                    abrirPdfEnNavegador(() -> urlArchivo);
                } else {
                    mostrarAlerta("Error", "No se encontró la URL para el tipo de apoyo seleccionado.", Alert.AlertType.ERROR);
                }
            }
        } catch (Exception ex) {
            mostrarAlerta("Error", "No se pudo obtener la lista de archivos de apoyo: " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void handleVerMicrocurriculum(Long courseId) {
        try {
            String urlMicrocurriculum = courseFileServiceFront.getMicrocurriculoUrl(courseId);
            if (urlMicrocurriculum != null && !urlMicrocurriculum.isBlank()) {
                abrirPdfEnNavegador(() -> urlMicrocurriculum);
            } else {
                mostrarAlerta("Información", "No hay microcurrículo disponible para este curso.", Alert.AlertType.INFORMATION);
            }
        } catch (Exception ex) {
            mostrarAlerta("Error", "No se pudo obtener la URL del microcurrículo: " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // Método de ayuda para aplicar estilos a diálogos
    private void applyDialogStyles(javafx.scene.control.DialogPane dialogPane) {
        dialogPane.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        dialogPane.getStyleClass().add("dialog-pane-custom");
        dialogPane.lookupButton(javafx.scene.control.ButtonType.OK).getStyleClass().addAll("dialog-ok");
        dialogPane.lookupButton(javafx.scene.control.ButtonType.CANCEL).getStyleClass().addAll("dialog-cancel");
        javafx.scene.Node combo = dialogPane.lookup(".combo-box");
        if (combo != null) combo.getStyleClass().add("dialog-combo");
    }

    private void handleAsignarDocente() {
        try {
            // Obtener cursos del programa
            List<CourseDTO> cursos = courseServiceFront.listCoursesByProgramaId(programaId);
            if (cursos == null || cursos.isEmpty()) {
                mostrarAlerta("No hay cursos", "No hay cursos disponibles para asignar docente.", Alert.AlertType.INFORMATION);
                return;
            }
            // Diálogo para seleccionar curso
            javafx.scene.control.ChoiceDialog<CourseDTO> dialog = new javafx.scene.control.ChoiceDialog<>(cursos.get(0), cursos);
            dialog.setTitle("Asignar Docente a Curso");
            dialog.setHeaderText("Seleccione el curso al que desea asignar un docente");
            dialog.setContentText("Curso:");
            // --- ESTILO AVANZADO ---
            dialog.getDialogPane().getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            dialog.getDialogPane().getStyleClass().add("dialog-pane-custom");
            dialog.getDialogPane().lookupButton(javafx.scene.control.ButtonType.OK).getStyleClass().addAll("dialog-ok");
            dialog.getDialogPane().lookupButton(javafx.scene.control.ButtonType.CANCEL).getStyleClass().addAll("dialog-cancel");
            // Estilo al ComboBox interno
            javafx.scene.Node combo = dialog.getDialogPane().lookup(".combo-box");
            if (combo != null) combo.getStyleClass().add("dialog-combo");
            // --- FIN ESTILO AVANZADO ---
            java.util.Optional<CourseDTO> result = dialog.showAndWait();
            if (result.isPresent()) {
                CourseDTO cursoSeleccionado = result.get();
                // Obtener lista de docentes
                List<UserInfoDTO> docentes = userServiceFront.getDocentes();
                if (docentes == null || docentes.isEmpty()) {
                    mostrarAlerta("No hay docentes", "No hay docentes disponibles para asignar.", Alert.AlertType.INFORMATION);
                    return;
                }
                // Diálogo para seleccionar docente por email
                javafx.scene.control.ChoiceDialog<UserInfoDTO> docenteDialog = new javafx.scene.control.ChoiceDialog<>(docentes.get(0), docentes);
                docenteDialog.setTitle("Asignar Docente");
                docenteDialog.setHeaderText("Seleccione el docente a asignar al curso: " + cursoSeleccionado.getName());
                docenteDialog.setContentText("Docente (email):");
                // --- ESTILO AVANZADO ---
                docenteDialog.getDialogPane().getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
                docenteDialog.getDialogPane().getStyleClass().add("dialog-pane-custom");
                docenteDialog.getDialogPane().lookupButton(javafx.scene.control.ButtonType.OK).getStyleClass().addAll("dialog-ok");
                docenteDialog.getDialogPane().lookupButton(javafx.scene.control.ButtonType.CANCEL).getStyleClass().addAll("dialog-cancel");
                javafx.scene.Node combo2 = docenteDialog.getDialogPane().lookup(".combo-box");
                if (combo2 != null) combo2.getStyleClass().add("dialog-combo");
                // --- FIN ESTILO AVANZADO ---
                java.util.Optional<UserInfoDTO> docenteResult = docenteDialog.showAndWait();
                if (docenteResult.isPresent()) {
                    UserInfoDTO docenteSeleccionado = docenteResult.get();
                    String docenteIdStr = docenteSeleccionado.getId();
                    try {
                        courseServiceFront.assignTeacher(cursoSeleccionado.getId(), docenteIdStr);
                        mostrarAlerta("Éxito", "Docente asignado correctamente.", Alert.AlertType.INFORMATION);
                    } catch (Exception ex) {
                        mostrarAlerta("Error", "No se pudo asignar el docente: " + ex.getMessage(), Alert.AlertType.ERROR);
                    }
                }
            }
        } catch (Exception e) {
            mostrarAlerta("Error", "No se pudo cargar la lista de cursos o docentes: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.getDialogPane().getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        alerta.getDialogPane().getStyleClass().add("dialog-pane-custom");
        alerta.showAndWait();
    }

    private void abrirPdfEnNavegador(Supplier<String> urlSupplier) {
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

    @FXML
    public void handleVolver(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainScreen.fxml"));
        loader.setControllerFactory(applicationContext::getBean);
        Parent root = loader.load();
        Stage stage = (Stage) programNameLabel.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Programas Académicos");
        stage.show();
    }
}
