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
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.unisoftware.gestioncurricular.frontend.dto.ProgramDTO;
import org.unisoftware.gestioncurricular.frontend.service.ProgramServiceFront;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

@Component
public class MainScreenController implements Initializable {

    @FXML private VBox cardContainer;

    @Autowired private ProgramServiceFront programServiceFront;
    @Autowired private ApplicationContext applicationContext;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        mostrarProgramaCard();
    }

    private void mostrarProgramaCard() {
        cardContainer.getChildren().clear();
        try {
            List<ProgramDTO> lista = programServiceFront.listPrograms();
            if (!lista.isEmpty()) {
                ProgramDTO prog = lista.get(0); // Solo el primero, según tu requerimiento

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

    @FXML
    public void handleLogout(ActionEvent event) {
        // Asumiendo Login.fxml para volver al login
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent loginView = loader.load();
            Scene loginScene = new Scene(loginView);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(loginScene);
            stage.setTitle("Iniciar Sesión");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}