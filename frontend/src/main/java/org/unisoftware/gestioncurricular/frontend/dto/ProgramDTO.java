package org.unisoftware.gestioncurricular.frontend.dto;

public class ProgramDTO {
    private Long id;
    private String name;
    private Integer duration;
    private String awardingDegree;
    private String academicLevel;
    private String modality;

    // Getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }

    public String getAwardingDegree() { return awardingDegree; }
    public void setAwardingDegree(String awardingDegree) { this.awardingDegree = awardingDegree; }

    public String getAcademicLevel() { return academicLevel; }
    public void setAcademicLevel(String academicLevel) { this.academicLevel = academicLevel; }

    public String getModality() { return modality; }
    public void setModality(String modality) { this.modality = modality; }
}

