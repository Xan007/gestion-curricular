package org.unisoftware.gestioncurricular.util.courseEnums;

public enum CourseCycle {
    acceso("A", "acceso"),
    especifico("E", "especifico"),
    fundamentacion("F", "fundamentacion"),
    no_aplica("NA", "no_aplica");

    private final String code;
    private final String label;

    CourseCycle(String code, String label) {
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
    public static CourseCycle fromCode(String code) {
        for (CourseCycle courseCycle : values()) {
            if (courseCycle.getCode().equalsIgnoreCase(code)) {
                return courseCycle;
            }
        }
        throw new IllegalArgumentException("Invalid Cycle: " + code);
    }

    @Override
    public String toString() {
        return label;
    }
}
