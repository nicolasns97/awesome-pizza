package com.adesso.awesomepizza.menu;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PizzaRepository extends JpaRepository<PizzaEntity, Long> {

    List<PizzaEntity> findByAvailableTrueOrderByNameAsc();
}
