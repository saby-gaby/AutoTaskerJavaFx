package com.autotasker.dao;

import com.autotasker.model.Department;
import com.autotasker.model.Task;
import com.autotasker.model.User;
import com.autotasker.util.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    private EntityManager em;

    public UserDAO(EntityManager em) {
        this.em = em;
    }

    public UserDAO() {}

    public User saveInitUser(User user) {
        em.persist(user);
        return findByUsername(user.getUsername());
    }

    public User findByUsername(String username) {
        em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<User> query = em.createQuery("SELECT u FROM User u WHERE u.username = :username", User.class);
            query.setParameter("username", username);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    public User saveUser(User user) {
        em = JpaUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(user);  // save user in db
            tx.commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();  // rollback if error
            }
        }finally {
            em.close();
        }
        return findByUsername(user.getUsername());
    }

    public boolean updateUser(User user) {
        em = JpaUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.merge(user);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
        } finally {
            em.close();
        }
        return false;
    }

    public List<User> findAll() {
        em = JpaUtil.getEntityManagerFactory().createEntityManager();
        List<User> usersList = em.createQuery("SELECT u FROM User u", User.class).getResultList();
        em.close();
        return usersList;
    }


    public List<String> findAllUsernames() {
        em = JpaUtil.getEntityManagerFactory().createEntityManager();
        List<String> usernames = em.createQuery(
                "SELECT u.username FROM User u",
                String.class
        ).getResultList();
        em.close();
        return usernames;
    }

    public boolean deleteUser(User user) {
        em = JpaUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.remove(em.contains(user) ? user : em.merge(user));
            tx.commit();
            return true;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
        } finally {
            em.close();
        }
        return false;
    }

    public List<User> getUsersByDepartment(Department department) {
        em = JpaUtil.getEntityManagerFactory().createEntityManager();
        List<User> usersList;
        TypedQuery<User> query = em.createQuery(
                "SELECT u FROM User u WHERE u.department = :department", User.class);
        query.setParameter("department", department);
        usersList = query.getResultList();
        em.close();
        return usersList;
    }

    // TODO: add other CRUD methods as needed
}
