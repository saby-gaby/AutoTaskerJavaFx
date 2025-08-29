package com.autotasker.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.io.InputStream;
import java.util.Properties;

public class JpaUtil {
    private static EntityManagerFactory emf;

    private JpaUtil() {}

    public static EntityManagerFactory getEntityManagerFactory() {
        if (emf == null) {
            try {
                // load properties from db.properties
                Properties props = new Properties();
                InputStream input = JpaUtil.class.getClassLoader().getResourceAsStream("db.properties");

                if (input == null) {
                    throw new RuntimeException("File db.properties not found in resources folder.");
                }

                props.load(input);

                emf = Persistence.createEntityManagerFactory("autotasker", props);
            } catch (Exception e) {
                throw new RuntimeException("DB configuration not successful", e);
            }
        }

        return emf;
    }

    public static boolean hasAnyUsers() {
        try(EntityManager em = getEntityManagerFactory().createEntityManager()){
            Long count = em
                    .createQuery("SELECT COUNT(u) FROM User u", Long.class)
                    .getSingleResult();
            return count > 0;
        }
    }
}
