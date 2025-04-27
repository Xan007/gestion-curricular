package org.unisoftware.gestioncurricular.frontend.dto;

public class ProgramDTO {
    private Long id;
    private String name;
    private String perfilProfesional;
    private String perfilOcupacional;
    private String perfilIngreso;
    private String competencias;
    private Integer duration;
    private String awardingDegree;

    // Getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPerfilProfesional() { return perfilProfesional; }
    public void setPerfilProfesional(String perfilProfesional) { this.perfilProfesional = perfilProfesional; }
    public String getPerfilOcupacional() { return perfilOcupacional; }
    public void setPerfilOcupacional(String perfilOcupacional) { this.perfilOcupacional = perfilOcupacional; }
    public String getPerfilIngreso() { return perfilIngreso; }
    public void setPerfilIngreso(String perfilIngreso) { this.perfilIngreso = perfilIngreso; }
    public String getCompetencias() { return competencias; }
    public void setCompetencias(String competencias) { this.competencias = competencias; }
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
    public String getAwardingDegree() { return awardingDegree; }
    public void setAwardingDegree(String awardingDegree) { this.awardingDegree = awardingDegree; }
}