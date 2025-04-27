package org.unisoftware.gestioncurricular.frontend.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

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

            int semestrePrevio = -1;
            VBox semestreBox = null;

            for (StudyPlanEntryDTO entry : plan) {
                Integer semestreActual = entry.getSemester();

                // Detectar y crear caja por semestre
                if (semestreActual != null && semestreActual != semestrePrevio) {
                    semestrePrevio = semestreActual;
                    Label semLabel = new Label("Semestre " + semestrePrevio);
                    semLabel.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-padding: 10 0 2 0");
                    semestreBox = new VBox(6);
                    semestreBox.getChildren().add(semLabel);
                    coursesContainer.getChildren().add(semestreBox);
                }

                VBox card = new VBox(4);
                card.setStyle("-fx-background-color:#f8fcfa; -fx-padding:12; -fx-background-radius: 8;");
                Label lnombre = new Label("Nombre: " + (entry.getName() != null ? entry.getName() : ""));
                lnombre.setStyle("-fx-font-weight:bold; -fx-font-size:14px;");

                String area = entry.getArea() != null ? entry.getArea() : "N/A";
                Label larea = new Label("Área: " + area);

                String tipo = entry.getType() != null ? entry.getType() : "N/A";
                Label ltipo = new Label("Tipo: " + tipo);

                String relacion = entry.getRelation() != null ? entry.getRelation() : "Ninguna";
                Label lrelacion = new Label("Relación: " + relacion);

                String ciclo = entry.getCycle() != null ? entry.getCycle() : "N/A";
                Label lciclo = new Label("Ciclo: " + ciclo);

                String creditos = entry.getCredits() != null ? entry.getCredits().toString() : "N/A";
                Label lcreditos = new Label("Créditos: " + creditos);

                // Requisitos como lista
                List<Long> requisitosList = entry.getRequirements();
                String requisitosString = (requisitosList != null && !requisitosList.isEmpty())
                        ? requisitosList.toString()
                        : "Ninguno";
                Label lrequisitos = new Label("Requisitos: " + requisitosString);

                // Añadir los datos relevantes al card
                card.getChildren().addAll(lnombre, larea, lciclo, ltipo, lcreditos, lrelacion, lrequisitos);

                if (semestreBox != null) {
                    semestreBox.getChildren().add(card);
                } else {
                    coursesContainer.getChildren().add(card);
                }
            }
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