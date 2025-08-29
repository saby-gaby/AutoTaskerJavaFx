package com.autotasker.dao;

import com.autotasker.model.Department;
import com.autotasker.model.User;
import com.autotasker.util.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class DepartmentDAO {
    private final EntityManager em;

    public DepartmentDAO(EntityManager em) {
        this.em = em;
    }

    public DepartmentDAO() {
        this.em = JpaUtil.getEntityManagerFactory().createEntityManager();
    }

    public Department saveInitDepartment(Department department) {
        em.persist(department);
        em.clear();
        return findByName(department.getDepartmentName());
    }

    public Department saveDepartment(Department department) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(department);
            tx.commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
        } finally {
            em.clear();
        }
        return findByName(department.getDepartmentName());
    }

    public List<Department> findAll() {
        return em.createQuery("SELECT d FROM Department d", Department.class).getResultList();
    }

    public Department findByName(String name) {
        try {
            TypedQuery<Department> query = em.createQuery(
                    "SELECT d FROM Department d WHERE d.departmentName = :name", Department.class);
            query.setParameter("name", name);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.clear();
        }
    }

    public Department findByDepartmentManager(User user) {
        try {
            TypedQuery<Department> query = em.createQuery(
                    "SELECT d FROM Department d WHERE d.departmentManager.id = :userId",
                    Department.class);
            query.setParameter("userId", user.getId());
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.clear();
        }
    }

    public List<User> findAllDepartmentManagers() {
        List<User> departmentManagers = em.createQuery(
                "SELECT d.departmentManager FROM Department d WHERE d.departmentManager IS NOT NULL",
                User.class
        ).getResultList();
        em.clear();
        return departmentManagers;
    }

    public List<String> findAllDepartmentNames() {
        List<String> departmentNames = em.createQuery(
                "SELECT d.departmentName FROM Department d",
                String.class
        ).getResultList();
        em.clear();
        return departmentNames;
    }

    public boolean updateDepartment(Department department) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.merge(department);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
        } finally {
            em.clear();
        }
        return false;
    }

    public void deleteDepartment(Department department) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.remove(em.contains(department) ? department : em.merge(department));
            tx.commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
        } finally {
            em.clear();
        }
    }
}
