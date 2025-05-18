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


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.unisoftware.gestioncurricular.frontend.dto.StudyPlanEntryDTO;
import org.unisoftware.gestioncurricular.frontend.service.ProgramServiceFront;

import java.io.IOException;
import java.util.List;

@Component
public class ProgramCoursesScreenController {

    @FXML private VBox coursesContainer;
    @FXML private Label programNameLabel;

    @Autowired private ProgramServiceFront programServiceFront;
    @Autowired private ApplicationContext applicationContext;

    private Long programaId;

    public void initData(Long programaId, String nombrePrograma) {
        this.programaId = programaId;
        programNameLabel.setText("Plan de Estudios de: " + nombrePrograma);
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

            for (Map.Entry<Integer, List<StudyPlanEntryDTO>> semestre : cursosPorSemestre.entrySet()) {
                Integer numeroSemestre = semestre.getKey();
                List<StudyPlanEntryDTO> cursos = semestre.getValue();

                VBox columnaSemestre = new VBox(12);
                columnaSemestre.setAlignment(Pos.TOP_CENTER);

                Label semLabel = new Label("Semestre " + numeroSemestre);
                semLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 0 0 6 0");
                columnaSemestre.getChildren().add(semLabel);

                for (StudyPlanEntryDTO entry : cursos) {
                    VBox card = new VBox(4);
                    card.setAlignment(Pos.TOP_LEFT);
                    card.setStyle(
                            "-fx-background-color: #ffffff;" +
                                    "-fx-background-radius: 14;" +
                                    "-fx-padding: 12 10 12 10;" +
                                    "-fx-min-width: 170px;" +
                                    "-fx-max-width: 260px;" +
                                    "-fx-border-color: #d1d5db;" +
                                    "-fx-border-width: 1;" +
                                    "-fx-effect: dropshadow(three-pass-box, #e0e3e7, 4, 0.10, 0, 2);" +
                                    "-fx-cursor: hand;"
                    );

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
                    }
                }

                filaSemestres.getChildren().add(columnaSemestre);
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

                for (StudyPlanEntryDTO entry : plan) {
                    if (entry.getRequirements() != null) {
                        for (Long reqId : entry.getRequirements()) {
                            VBox reqCard = idCardMap.get(reqId);
                            VBox depCard = idCardMap.get(entry.getId().getCourseId());
                            if (reqCard != null && depCard != null) {
                                // Coordenadas globales de los cards
                                javafx.geometry.Point2D reqScene = reqCard.localToScene(reqCard.getWidth(), reqCard.getHeight() / 2);
                                javafx.geometry.Point2D depScene = depCard.localToScene(0, depCard.getHeight() / 2);
                                javafx.geometry.Point2D reqStack = stack.sceneToLocal(reqScene);
                                javafx.geometry.Point2D depStack = stack.sceneToLocal(depScene);
                                gc.strokeLine(reqStack.getX(), reqStack.getY(), depStack.getX(),depStack.getY());
                            }
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
        List<Long> requisitosList = entry.getRequirements();
        String requisitosString;
        if (requisitosList != null && !requisitosList.isEmpty()) {
            List<String> nombresReq = new ArrayList<>();
            for (Long reqId : requisitosList) {
                String nombreReq = idNombreCurso.getOrDefault(reqId, reqId.toString());
                nombresReq.add(nombreReq);
            }
            requisitosString = String.join(", ", nombresReq);
        } else {
            requisitosString = "Ninguno";
        }
        Label requisitos = new Label("Requisitos: " + requisitosString);
        requisitos.setWrapText(true);

        Button cerrar = new Button("Cerrar");
        cerrar.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: #fff; -fx-background-radius: 8; -fx-font-size: 14px; -fx-padding: 6 18 6 18; -fx-font-weight: bold; -fx-border-color: #b71c1c; -fx-border-width: 2;");
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

