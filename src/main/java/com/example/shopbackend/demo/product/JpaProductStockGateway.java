package com.example.shopbackend.demo.product;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@Repository
public class JpaProductStockGateway implements ProductStockGateway {
    private final EntityManager em;

    public JpaProductStockGateway(EntityManager em) {
        this.em = em;
    }

    @Override
    @Transactional
    public boolean tryDecreaseStock(Long productId, int qty) {
        int updated = em.createQuery("""
                update Product p
                set p.stock = p.stock - :qty
                where p.id = :id and p.stock >= :qty
                    """)
                .setParameter("id", productId)
                .setParameter("qty", qty)
                .executeUpdate();

        return updated == 1;
    }

}
