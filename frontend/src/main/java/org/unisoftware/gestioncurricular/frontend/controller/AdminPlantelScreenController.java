package org.unisoftware.gestioncurricular.frontend.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.application.Platform;
import org.unisoftware.gestioncurricular.frontend.util.SessionManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class AdminPlantelScreenController {
    @FXML private TableView<UserTableRow> usersTable;
    @FXML private TableColumn<UserTableRow, String> idCol;
    @FXML private TableColumn<UserTableRow, String> nameCol;
    @FXML private TableColumn<UserTableRow, String> emailCol;
    @FXML private TableColumn<UserTableRow, String> rolesCol;
    @FXML private ComboBox<String> rolesCombo;
    @FXML private Button assignRoleBtn;
    @FXML private Button removeRolesBtn;

    private ObservableList<UserTableRow> usersData = FXCollections.observableArrayList();
    private final String apiUrl = "http://localhost:8080"; // Cambia esto por tu endpoint real

    @FXML
    public void initialize() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        rolesCol.setCellValueFactory(new PropertyValueFactory<>("roles"));
        usersTable.setItems(usersData);

        // Aquí agregas la política para evitar la columna extra gris
        usersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        rolesCombo.setItems(FXCollections.observableArrayList(
                "DECANO", "DIRECTOR_DE_PROGRAMA", "DOCENTE", "COMITE_DE_PROGRAMA"
        ));
        cargarUsuarios();

        assignRoleBtn.setOnAction(e -> asignarRol());
        removeRolesBtn.setOnAction(e -> quitarRoles());
    }

    private void cargarUsuarios() {
        new Thread(() -> {
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl + "/users").openConnection();
                conn.setRequestProperty("Authorization", "Bearer " + SessionManager.getInstance().getToken());
                conn.setRequestMethod("GET");
                conn.setDoInput(true);

                List<Map<String, Object>> usuarios;
                try (var in = conn.getInputStream()) {
                    usuarios = new ObjectMapper().readValue(in, new TypeReference<List<Map<String, Object>>>(){});
                }
                Platform.runLater(() -> {
                    usersData.clear();
                    for (Map<String, Object> u : usuarios) {
                        String id = String.valueOf(u.get("id"));
                        String nombre = String.valueOf(u.getOrDefault("nombre", ""));
                        String correo = String.valueOf(u.getOrDefault("email", ""));
                        // Obtener roles haciendo petición a GET /users/{id}/role
                        obtenerYAgregarFilaUsuario(id, nombre, correo);
                    }
                });
            } catch (Exception e) {
                mostrarAlerta("Error", "No se pudieron cargar los usuarios.");
            }
        }).start();
    }

    // Obtiene los roles por usuario y agrega la fila a la tabla
    private void obtenerYAgregarFilaUsuario(String id, String nombre, String correo) {
        new Thread(() -> {
            String roles = "";
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl + "/users/" + id + "/role").openConnection();
                conn.setRequestProperty("Authorization", "Bearer " + SessionManager.getInstance().getToken());
                conn.setRequestMethod("GET");
                conn.setDoInput(true);

                try (var in = conn.getInputStream()) {
                    ObjectMapper mapper = new ObjectMapper();
                    Object datoRoles = mapper.readValue(in, Object.class);
                    // Si tu endpoint devuelve una lista, cámbialo a (List<String>)
                    if (datoRoles instanceof String) {
                        roles = (String) datoRoles;
                    } else if (datoRoles instanceof Map) {
                        Map<?, ?> mapRoles = (Map<?, ?>) datoRoles;
                        roles = String.valueOf(mapRoles.values());
                    } else if (datoRoles instanceof List) {
                        roles = String.join(", ", (List<String>) datoRoles);
                    }
                }
            } catch (Exception e) {
                roles = "Sin rol";
            }
            String finalRoles = roles;
            Platform.runLater(() -> {
                usersData.add(new UserTableRow(id, nombre, correo, finalRoles));
            });
        }).start();
    }

    private void asignarRol() {
        UserTableRow usuario = usersTable.getSelectionModel().getSelectedItem();
        String rol = rolesCombo.getValue();

        if (usuario == null || rol == null) {
            mostrarAlerta("Error", "Debes seleccionar un usuario y un rol.");
            return;
        }

        new Thread(() -> {
            try {
                String urlStr = apiUrl + "/users/" + usuario.getId() + "/assign-role?role=" + rol;
                HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Bearer " + SessionManager.getInstance().getToken());
                conn.setDoOutput(false);

                int res = conn.getResponseCode();

                if (res == 204) {
                    Platform.runLater(() -> {
                        mostrarAlerta("Rol asignado", "Rol asignado correctamente.");
                        cargarUsuarios();
                    });
                } else {
                    String errorMsg = "";
                    try (BufferedReader in = new BufferedReader(
                            new InputStreamReader(conn.getErrorStream()))) {
                        String line;
                        StringBuilder response = new StringBuilder();
                        while ((line = in.readLine()) != null) {
                            response.append(line);
                        }
                        errorMsg = response.toString();
                    } catch (Exception ignored) {}
                    final String finalErrorMsg = errorMsg.isEmpty() ? "No se pudo asignar el rol." : errorMsg;
                    Platform.runLater(() -> mostrarAlerta("Error", finalErrorMsg));
                }
            } catch (Exception ex) {
                Platform.runLater(() -> mostrarAlerta("Error", "No se pudo asignar el rol.\n" + ex.getMessage()));
            }
        }).start();
    }

    private void quitarRoles() {
        UserTableRow usuario = usersTable.getSelectionModel().getSelectedItem();
        if (usuario == null) {
            mostrarAlerta("Error", "Debes seleccionar un usuario.");
            return;
        }

        new Thread(() -> {
            try {
                String urlStr = apiUrl + "/users/" + usuario.getId() + "/remove-role";
                HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
                conn.setRequestMethod("DELETE");
                conn.setRequestProperty("Authorization", "Bearer " + SessionManager.getInstance().getToken());
                conn.setDoOutput(false);

                int res = conn.getResponseCode();
                if (res == 200 || res == 204) {
                    Platform.runLater(() -> {
                        mostrarAlerta("Roles quitados", "Roles eliminados correctamente.");
                        cargarUsuarios();
                    });
                } else {
                    mostrarAlerta("Error", "No se pudo quitar los roles.");
                }
            } catch (Exception ex) {
                mostrarAlerta("Error", "No se pudo quitar los roles.\n" + ex.getMessage());
            }
        }).start();
    }

    private void mostrarAlerta(String titulo, String msg) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(titulo);
            alert.setHeaderText(null);
            alert.setContentText(msg);
            alert.showAndWait();
        });
    }

    public static class UserTableRow {
        private final String id, name, email, roles;
        public UserTableRow(String id, String n, String email, String roles) {
            this.id = id; this.name = n; this.email = email; this.roles = roles;
        }
        public String getId() {return id;}
        public String getName() {return name;}
        public String getEmail() {return email;}
        public String getRoles() {return roles;}
    }
}