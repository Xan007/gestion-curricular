package org.unisoftware.gestioncurricular.frontend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ProgramDTO {
    private Long id;

    private String name;

    @JsonProperty("professionalProfile")
    private String professionalProfile;

    @JsonProperty("occupationalProfile")
    private String occupationalProfile;

    @JsonProperty("admissionProfile")
    private String admissionProfile;

    // En el JSON pueden venir objetos, pero aquí se deja como String (adapta si es necesario)
    private String competencies;

    @JsonProperty("learningOutcomesFileId")
    private Long learningOutcomesFileId;

    private Integer duration;

    @JsonProperty("awardingDegree")
    private String awardingDegree;

    // Getters y setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getProfessionalProfile() { return professionalProfile; }
    public void setProfessionalProfile(String professionalProfile) { this.professionalProfile = professionalProfile; }

    public String getOccupationalProfile() { return occupationalProfile; }
    public void setOccupationalProfile(String occupationalProfile) { this.occupationalProfile = occupationalProfile; }

    public String getAdmissionProfile() { return admissionProfile; }
    public void setAdmissionProfile(String admissionProfile) { this.admissionProfile = admissionProfile; }

    public String getCompetencies() { return competencies; }
    public void setCompetencies(String competencies) { this.competencies = competencies; }

    public Long getLearningOutcomesFileId() { return learningOutcomesFileId; }
    public void setLearningOutcomesFileId(Long learningOutcomesFileId) { this.learningOutcomesFileId = learningOutcomesFileId; }

    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }

    public String getAwardingDegree() { return awardingDegree; }
    public void setAwardingDegree(String awardingDegree) { this.awardingDegree = awardingDegree; }
}