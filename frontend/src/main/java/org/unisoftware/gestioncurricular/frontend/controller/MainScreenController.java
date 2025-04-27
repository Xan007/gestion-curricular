package org.unisoftware.gestioncurricular.frontend.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.unisoftware.gestioncurricular.frontend.dto.CourseDTO;
import org.unisoftware.gestioncurricular.frontend.dto.ProgramDTO;
import org.unisoftware.gestioncurricular.frontend.service.CourseServiceFront;
import org.unisoftware.gestioncurricular.frontend.service.ProgramServiceFront;
import org.unisoftware.gestioncurricular.frontend.util.SessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

@Controller
public class MainScreenController implements Initializable {

    // PROGRAMAS
    @FXML private TableView<ProgramDTO> programTableView;
    @FXML private TableColumn<ProgramDTO, Long> colProgramId;
    @FXML private TableColumn<ProgramDTO, String> colProgramName;
    @FXML private TableColumn<ProgramDTO, String> colPerfilProfesional;
    @FXML private TableColumn<ProgramDTO, String> colPerfilOcupacional;
    @FXML private TableColumn<ProgramDTO, String> colPerfilIngreso;
    @FXML private TableColumn<ProgramDTO, String> colCompetencias;
    @FXML private TableColumn<ProgramDTO, String> colDuration;
    @FXML private TableColumn<ProgramDTO, String> colAwardingDegree;

    // CURSOS
    @FXML private TableView<CourseDTO> courseTableView;
    @FXML private TableColumn<CourseDTO, Long> colCourseId;
    @FXML private TableColumn<CourseDTO, String> colCourseName;
    @FXML private TableColumn<CourseDTO, String> colCourseType;
    @FXML private TableColumn<CourseDTO, Integer> colCourseCredits;
    @FXML private TableColumn<CourseDTO, String> colCourseRelation;
    @FXML private TableColumn<CourseDTO, String> colCourseArea;
    @FXML private TableColumn<CourseDTO, String> colCourseCycle;

    @Autowired private ProgramServiceFront programServiceFront;
    @Autowired private CourseServiceFront courseServiceFront;
    @Autowired private ApplicationContext applicationContext;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarColumnasProgramas();
        configurarColumnasCursos();
        cargarProgramas();
        cargarCursos();
    }

    private void configurarColumnasProgramas() {
        colProgramId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colProgramName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPerfilProfesional.setCellValueFactory(new PropertyValueFactory<>("perfilProfesional"));
        colPerfilOcupacional.setCellValueFactory(new PropertyValueFactory<>("perfilOcupacional"));
        colPerfilIngreso.setCellValueFactory(new PropertyValueFactory<>("perfilIngreso"));
        colCompetencias.setCellValueFactory(new PropertyValueFactory<>("competencias"));
        colDuration.setCellValueFactory(new PropertyValueFactory<>("duration"));
        colAwardingDegree.setCellValueFactory(new PropertyValueFactory<>("awardingDegree"));
    }

    private void configurarColumnasCursos() {
        colCourseId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCourseName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCourseType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colCourseCredits.setCellValueFactory(new PropertyValueFactory<>("credits"));
        colCourseRelation.setCellValueFactory(new PropertyValueFactory<>("relation"));
        colCourseArea.setCellValueFactory(new PropertyValueFactory<>("area"));
        colCourseCycle.setCellValueFactory(new PropertyValueFactory<>("cycle"));
    }

    private void cargarProgramas() {
        try {
            List<ProgramDTO> lista = programServiceFront.listPrograms();
            programTableView.getItems().setAll(lista);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cargarCursos() {
        try {
            List<CourseDTO> lista = courseServiceFront.listCourses();
            courseTableView.getItems().setAll(lista);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        // Limpiar la sesión
        SessionManager.getInstance().clearSession();
        // Navegar al login
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
            // Aquí podrías mostrar una alerta de error si lo deseas
        }
    }
}