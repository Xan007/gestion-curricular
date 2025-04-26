package org.unisoftware.gestioncurricular.util.courseEnums;

public enum CourseArea {
    profesional("PS", "profesional"),
    basica("BA", "basica"),
    complementaria("CO", "complementaria"),
    profundizacion("PZ", "profundizacion"),
    investigacion("I", "investigacion");

    private final String code;
    private final String label;

    CourseArea(String code, String label) {
        this.code = code;
        this.label = label;
    }

    /** Abreviatura para guardar en la DB */
    public String getCode() {
        return code;
    }

    /** Texto legible para mostrar al usuario */
    public String getLabel() {
        return label;
    }

    /** Convierte abreviatura a enum */
    public static CourseArea fromCode(String code) {
        for (CourseArea courseArea : values()) {
            if (courseArea.getCode().equalsIgnoreCase(code)) {
                return courseArea;
            }
        }
        throw new IllegalArgumentException("Invalid Area: " + code);
    }

    @Override
    public String toString() {
        return label;
    }
}
