package com.autotasker.dao;

import com.autotasker.model.Department;
import com.autotasker.model.Task;
import com.autotasker.model.User;
import com.autotasker.util.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;

public class TaskDAO {

    private EntityManager em;

    public boolean addTask(Task task) {
        em = JpaUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(task);  // save task in db
            tx.commit();
            return true;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();  // rollback if error
            }
            return false;
        } finally {
            em.close();
        }
    }


    public List<Task> getAllTasks() {
        em = JpaUtil.getEntityManagerFactory().createEntityManager();
        List<Task> tasks = em.createQuery("FROM Task", Task.class).getResultList();
        em.close();
        return tasks;
    }

    public void updateTask(Task task) {
        em = JpaUtil.getEntityManagerFactory().createEntityManager();
        em.getTransaction().begin();
        em.merge(task);
        em.getTransaction().commit();
        em.close();
    }

    public void deleteTask(Task task) {
        em = JpaUtil.getEntityManagerFactory().createEntityManager();
        em.getTransaction().begin();
        em.remove(em.contains(task) ? task : em.merge(task));
        em.getTransaction().commit();
        em.clear();
    }

    public List<Task> findByUser(User user) {
        try (EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager()) {
            return em.createQuery("SELECT t FROM Task t WHERE t.assignedUser = :user", Task.class)
                    .setParameter("user", user)
                    .getResultList();
        }
    }

    public List<Task> findByDepartment(Department department) {
        try (EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager()) {
            return em.createQuery("SELECT t FROM Task t WHERE t.assignedDepartment = :dep", Task.class)
                    .setParameter("dep", department)
                    .getResultList();
        }
    }
}
