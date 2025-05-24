package org.unisoftware.gestioncurricular.frontend.dto;

import java.util.List;

public class StudyPlanEntryDTO {
    // Clase interna para el id embebido
    public static class Id {
        private Long programId;
        private Long courseId;
        private Long year;

        public Long getProgramId() { return programId; }
        public void setProgramId(Long programId) { this.programId = programId; }

        public Long getCourseId() { return courseId; }
        public void setCourseId(Long courseId) { this.courseId = courseId; }

        public Long getYear() { return year; }
        public void setYear(Long year) { this.year = year; }
    }

    private Id id;
    private String name;
    private String type;
    private Integer credits;
    private String relation;
    private String area;
    private String cycle;
    private Integer semester;
    private List<Long> requirements;

    // Getters y setters
    public Id getId() { return id; }
    public void setId(Id id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Integer getCredits() { return credits; }
    public void setCredits(Integer credits) { this.credits = credits; }

    public String getRelation() { return relation; }
    public void setRelation(String relation) { this.relation = relation; }

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }

    public String getCycle() { return cycle; }
    public void setCycle(String cycle) { this.cycle = cycle; }

    public Integer getSemester() { return semester; }
    public void setSemester(Integer semester) { this.semester = semester; }

    public List<Long> getRequirements() { return requirements; }
    public void setRequirements(List<Long> requirements) { this.requirements = requirements; }
}