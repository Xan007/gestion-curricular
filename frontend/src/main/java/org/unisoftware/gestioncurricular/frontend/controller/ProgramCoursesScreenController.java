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
                            "-fx-background-color: #42a5f5;" + // azul claro fijo
                                    "-fx-background-radius: 12;" +
                                    "-fx-padding: 10 8 10 8;" +
                                    "-fx-min-width: 190px;" +
                                    "-fx-max-width: 320px;" +
                                    "-fx-effect: dropshadow(three-pass-box, #AAAAAA, 3, 0.18, 1, 2);"
                    );

                    Label lnombre = new Label((entry.getName() != null ? entry.getName() : ""));
                    lnombre.setStyle("-fx-font-weight:bold; -fx-font-size:13px; -fx-text-fill: white;");
                    lnombre.setWrapText(true);
                    lnombre.setMaxWidth(290);

                    Label larea = new Label("Área: " + (entry.getArea() != null ? entry.getArea() : "N/A"));
                    larea.setStyle("-fx-text-fill: white;");
                    Label ltipo = new Label("Tipo: " + (entry.getType() != null ? entry.getType() : "N/A"));
                    ltipo.setStyle("-fx-text-fill: white;");
                    Label lciclo = new Label("Ciclo: " + (entry.getCycle() != null ? entry.getCycle() : "N/A"));
                    lciclo.setStyle("-fx-text-fill: white;");
                    Label lcreditos = new Label("Créditos: " + (entry.getCredits() != null ? entry.getCredits().toString() : "N/A"));
                    lcreditos.setStyle("-fx-text-fill: white;");
                    String relacion = entry.getRelation() != null ? entry.getRelation() : "Ninguna";
                    Label lrelacion = new Label("Relación: " + relacion);
                    lrelacion.setStyle("-fx-text-fill: white;");

                    // MOSTRAR NOMBRES DE CURSOS REQUISITOS
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
                    Label lrequisitos = new Label("Requisitos: " + requisitosString);
                    lrequisitos.setStyle("-fx-text-fill: white;");

                    card.getChildren().addAll(lnombre, larea, lciclo, ltipo, lcreditos, lrelacion, lrequisitos);
                    columnaSemestre.getChildren().add(card);
                }

                filaSemestres.getChildren().add(columnaSemestre);
            }

            ScrollPane scroll = new ScrollPane(filaSemestres);
            scroll.setFitToHeight(true);
            scroll.setFitToWidth(false);
            scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

            coursesContainer.getChildren().add(scroll);

        } catch (Exception e) {
            coursesContainer.getChildren().add(new Label("Error cargando plan de estudios: " + e.getMessage()));
        }
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