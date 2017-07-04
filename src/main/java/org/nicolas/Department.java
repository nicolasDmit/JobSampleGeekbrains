package org.nicolas;

/**
 * Класс-сущность описатель отдела
 */
public class Department {

    private Integer id;
    private String depCode;
    private String depJob;
    private String description;

    public Department(Integer id, String depCode, String depJob, String description) {
        this.id = id;
        this.depCode = depCode;
        this.depJob = depJob;
        this.description = description;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDepCode() {
        return depCode;
    }

    public void setDepCode(String depCode) {
        this.depCode = depCode;
    }

    public String getDepJob() {
        return depJob;
    }

    public void setDepJob(String depJob) {
        this.depJob = depJob;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public int hashCode() {

        String fullTitle = this.depCode + this.depJob;

        int hash = 7;
        for (int i = 0; i < fullTitle.length(); i++) {
            hash = hash*31 + fullTitle.charAt(i);
        }
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Department && obj.hashCode() == this.hashCode();

    }

    @Override
    public String toString() {
        return this.getId() + " " + this.getDepCode() + " | " + this.depJob;
    }
}
