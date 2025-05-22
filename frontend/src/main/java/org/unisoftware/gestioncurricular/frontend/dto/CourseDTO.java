package org.unisoftware.gestioncurricular.frontend.dto;

import java.util.List;

public class CourseDTO {
    private Long id;
    private String name;
    private String type;
    private String cycle;
    private String area;
    private Integer credits;
    private String relation;
    private Long microcurriculumFileId;
    private String teacherId;
    private Integer semester;
    private List<Long> requirements;

    // Getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getCycle() { return cycle; }
    public void setCycle(String cycle) { this.cycle = cycle; }

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }

    public Integer getCredits() { return credits; }
    public void setCredits(Integer credits) { this.credits = credits; }

    public String getRelation() { return relation; }
    public void setRelation(String relation) { this.relation = relation; }

    public Long getMicrocurriculumFileId() { return microcurriculumFileId; }
    public void setMicrocurriculumFileId(Long microcurriculumFileId) { this.microcurriculumFileId = microcurriculumFileId; }

    public String getTeacherId() { return teacherId; }
    public void setTeacherId(String teacherId) { this.teacherId = teacherId; }

    public Integer getSemester() { return semester; }
    public void setSemester(Integer semester) { this.semester = semester; }

    public List<Long> getRequirements() { return requirements; }
    public void setRequirements(List<Long> requirements) { this.requirements = requirements; }

    @Override
    public String toString() {
        return (getId() != null ? getId().toString() : "") + " - " + (getName() != null ? getName() : "");
    }
}

