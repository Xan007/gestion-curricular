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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.unisoftware.gestioncurricular.frontend.util.SessionManager;
import org.unisoftware.gestioncurricular.frontend.dto.ProgramDTO;
import org.unisoftware.gestioncurricular.frontend.dto.CourseDTO;
import org.unisoftware.gestioncurricular.frontend.service.ProgramServiceFront;
import org.unisoftware.gestioncurricular.frontend.service.CourseServiceFront;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

@Controller
public class MainScreenController implements Initializable {

    // PROGRAMAS: usando las nuevas fx:id del FXML
    @FXML
    private TableView<ProgramDTO> programTable;

    @FXML
    private TableColumn<ProgramDTO, Long> idColumn;

    @FXML
    private TableColumn<ProgramDTO, String> nameColumn;

    @FXML
    private TableColumn<ProgramDTO, String> professionalProfileColumn;

    @FXML
    private TableColumn<ProgramDTO, String> occupationalProfileColumn;

    @FXML
    private TableColumn<ProgramDTO, String> admissionProfileColumn;

    @FXML
    private TableColumn<ProgramDTO, String> competenciesColumn;

    @FXML
    private TableColumn<ProgramDTO, String> learningOutcomesFileIdColumn;

    @FXML
    private TableColumn<ProgramDTO, String> durationColumn;

    @FXML
    private TableColumn<ProgramDTO, String> awardingDegreeColumn;

    // CURSOS: por si necesitas ambas tablas
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
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        professionalProfileColumn.setCellValueFactory(new PropertyValueFactory<>("professionalProfile"));
        occupationalProfileColumn.setCellValueFactory(new PropertyValueFactory<>("occupationalProfile"));
        admissionProfileColumn.setCellValueFactory(new PropertyValueFactory<>("admissionProfile"));
        competenciesColumn.setCellValueFactory(new PropertyValueFactory<>("competencies"));
        learningOutcomesFileIdColumn.setCellValueFactory(new PropertyValueFactory<>("learningOutcomesFileId"));
        durationColumn.setCellValueFactory(new PropertyValueFactory<>("duration"));
        awardingDegreeColumn.setCellValueFactory(new PropertyValueFactory<>("awardingDegree"));
    }

    private void configurarColumnasCursos() {
        if (courseTableView == null) return; // Por si no existe la tabla en el FXML
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
            programTable.getItems().setAll(lista);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cargarCursos() {
        if (courseTableView == null) return; // Por si no existe la tabla en el FXML
        try {
            List<CourseDTO> lista = courseServiceFront.listCourses();
            courseTableView.getItems().setAll(lista);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        SessionManager.getInstance().clearSession();
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