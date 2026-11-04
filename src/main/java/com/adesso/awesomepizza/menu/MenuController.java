package com.adesso.awesomepizza.menu;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Menu")
@RestController
@RequestMapping("/api/v1/menu")
public class MenuController {
    private final PizzaRepository pizzaRepository;

    public MenuController(PizzaRepository pizzaRepository) {
        this.pizzaRepository = pizzaRepository;
    }

    @Operation(summary = "List the available pizzas")
    @GetMapping
    public ResponseEntity<List<PizzaResponseDto>> getMenu() {
        List<PizzaResponseDto> menu = pizzaRepository.findByAvailableTrueOrderByNameAsc().stream()
                .map(pizza -> new PizzaResponseDto(pizza.getId(), pizza.getName(), pizza.getPrice()))
                .toList();
        return ResponseEntity.ok(menu);
    }
}
