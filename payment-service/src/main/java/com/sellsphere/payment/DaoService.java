package com.sellsphere.payment;

import jakarta.persistence.EntityManager;

public class DaoService<T> {

    public T getProductById(Integer id, Class<T> clazz) {
        EntityManager em = JPAUtil.getEntityManager();
        T entity = null;

        try {
            em.getTransaction().begin();
            entity = em.find(clazz, id);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            throw e;
        } finally {
            em.close();
        }

        return entity;
    }

}
