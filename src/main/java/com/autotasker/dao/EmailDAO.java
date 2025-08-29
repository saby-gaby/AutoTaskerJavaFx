package com.autotasker.dao;

import com.autotasker.model.Email;
import com.autotasker.model.User;
import com.autotasker.util.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class EmailDAO {
    private EntityManager em;

    public EmailDAO() {
    }

    public Email insertEmail(Email email) {
        em = JpaUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(email);
            tx.commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
        } finally {
            em.close();
        }
        return findByEmailAddress(email.getEmail());
    }

    public List<Email> findAll() {
        em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery("FROM Email", Email.class).getResultList();
        } finally {
            em.close();
        }
    }

    public List<Email> findEmailsByUser(User user) {
        em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<Email> query = em.createQuery("SELECT e FROM Email e WHERE e.owner = :owner", Email.class);
            query.setParameter("owner", user);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    private Email findByEmailAddress(String email) {
        em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<Email> query = em.createQuery("SELECT e FROM Email e WHERE e.email = :email", Email.class);
            query.setParameter("email", email);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    public boolean isEmailPresent(String email) {
        em = JpaUtil.getEntityManagerFactory().createEntityManager();
        em.getTransaction();
        try {
            TypedQuery<Email> query = em.createQuery("SELECT e FROM Email e WHERE e.email = :email", Email.class);
            query.setParameter("email", email);
            return !query.getResultList().isEmpty();
        } finally {
            em.close();
        }
    }

    public boolean updateEmail(Email email) {
        em = JpaUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.merge(email);
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

    public boolean deleteEmail(Email email) {
        em = JpaUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.remove(em.contains(email) ? email : em.merge(email));
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
}
