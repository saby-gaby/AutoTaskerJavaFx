package com.autotasker.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "departments")
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "department_name", nullable = false, length = 100)
    private String departmentName;

    // department_manager references User.id
    @OneToOne
    @JoinColumn(name = "department_manager", nullable = true)
    private User departmentManager;

    public Department() {}

    public Department(String departmentName, User departmentManager) {
        this.departmentName = departmentName;
        this.departmentManager = departmentManager;
    }

    public Long getId() { return id; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    public User getDepartmentManager() { return departmentManager; }
    public void setDepartmentManager(User departmentManager) { this.departmentManager = departmentManager; }

    // equals, hashCode (based on id)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Department)) return false;
        Department that = (Department) o;
        return Objects.equals(getId(), that.getId());
    }
    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
