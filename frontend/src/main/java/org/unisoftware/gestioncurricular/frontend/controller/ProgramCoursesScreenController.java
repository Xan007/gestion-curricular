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

@Component
public class ProgramCoursesScreenController {

    @FXML private VBox coursesContainer;
    @FXML private Label programNameLabel;
    @FXML private Button btnAsignarDocente;

    @Autowired private ProgramServiceFront programServiceFront;
    @Autowired private ApplicationContext applicationContext;
    @Autowired private CourseServiceFront courseServiceFront;
    @Autowired private UserServiceFront userServiceFront;

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
        modalContent.getChildren().addAll(title, codigo, nombre, area, ciclo, tipo, creditos, relacion, requisitos, cerrar);

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
        alerta.showAndWait();
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

