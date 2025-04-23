package com.autotasker.model;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;

import java.io.InputStream;
import java.util.List;
import java.util.Properties;

public class TaskDAO {

    private final EntityManager entityManager;

    public TaskDAO() {
        try {
            // load properties from db.properties
            Properties props = new Properties();
            InputStream input = getClass().getClassLoader().getResourceAsStream("db.properties");

            if (input == null) {
                throw new RuntimeException("File db.properties not found in resources folder.");
            }

            props.load(input);

            // setup EntityManagerFactory and override in persistence.xml
            EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("autotasker", props);
            entityManager = entityManagerFactory.createEntityManager();

        } catch (Exception e) {
            throw new RuntimeException("DB configuration not successful", e);
        }
    }

    public void addTask(Task task) {
        EntityTransaction tx = entityManager.getTransaction();
        try {
            tx.begin();
            entityManager.persist(task);  // save task in db
            tx.commit();
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();  // rollback if error
            }
        }
    }

    public List<Task> getAllTasks() {
        return entityManager.createQuery("FROM Task", Task.class).getResultList();
    }

    public void updateTask(Task task) {
        entityManager.getTransaction().begin();
        entityManager.merge(task);
        entityManager.getTransaction().commit();
    }

    public void deleteTask(Task task) {
        entityManager.getTransaction().begin();
        entityManager.remove(entityManager.contains(task) ? task : entityManager.merge(task));
        entityManager.getTransaction().commit();
    }
}
