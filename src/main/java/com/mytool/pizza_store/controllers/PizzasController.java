package com.mytool.pizza_store.controllers;

import java.io.InputStream;
import java.nio.*;
import com.mytool.pizza_store.model.Pizza;
import com.mytool.pizza_store.model.PizzaDto;
import com.mytool.pizza_store.services.PizzasRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/pizzas")
public class PizzasController {
    @Autowired
    private PizzasRepository repo;

    @GetMapping({"", "/"})
    public String showPizzasList(Model model) {
        List<Pizza> pizzas = repo.findAll(Sort.by(Sort.Direction.DESC, "id"));
        model.addAttribute("pizzas", pizzas);
        return "pizzas/index";
    }

    @GetMapping("/create")
    public String showCreatePage(Model model) {
        PizzaDto pizzaDto = new PizzaDto();
        model.addAttribute("pizzaDto", pizzaDto);
        return "pizzas/CreatePizza";
    }

    @PostMapping("/create")
    public String createPizza(
            @Valid @ModelAttribute PizzaDto pizzaDto,
            BindingResult result
    ) {
        if (pizzaDto.getImageFile().isEmpty()){
            result. addError (new FieldError("pizzaDto", "imageFile", "The image file is required"));
        }

        if (result.hasErrors()) {
            return "pizzas/CreatePizza";
        }

        MultipartFile image = pizzaDto.getImageFile();
        Date createdAt = new Date();
        String storageFileName = createdAt.getTime() + "_" + image.getOriginalFilename();

        try {
            String uploadDir = "public/images/";
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            try (InputStream inputStream = image. getInputStream ()) {
                Files. copy (inputStream, Paths.get(uploadDir + storageFileName),
                        StandardCopyOption.REPLACE_EXISTING);
            }

        } catch (Exception ex) {
            System. out. println ("Exception: " + ex.getMessage ());
        }

        Pizza pizza = new Pizza();
        pizza.setName(pizzaDto.getName());
        pizza.setCategory(pizzaDto.getCategory());
        pizza.setPrice(pizzaDto.getPrice());
        pizza.setDescription(pizzaDto.getDescription());
        pizza.setCreated_at(createdAt);
        pizza.setImageFileName(storageFileName);

        repo.save(pizza);

        return "redirect:/pizzas";
    }

    @GetMapping("/edit")
    public String showEditPage(
            Model model,
            @RequestParam int id) {

        try{
        Pizza pizza = repo.findById(id).get();
        model.addAttribute("pizza", pizza);

        PizzaDto pizzaDto = new PizzaDto();
        pizzaDto.setName(pizza.getName());
        pizzaDto.setCategory(pizza.getCategory());
        pizzaDto.setPrice(pizza.getPrice());
        pizzaDto.setDescription(pizza.getDescription());

        model.addAttribute("pizzaDto", pizzaDto);
        }
        catch (Exception ex) {
            System.out.println("Exception" + ex.getMessage());
            return "redirect:/pizzas";
        }
        return "pizzas/EditPizza";
    }

    @PostMapping("/edit")
    public String updatePizza(
            Model model,
            @RequestParam int id,
            @Valid @ModelAttribute PizzaDto pizzaDto,
            BindingResult result
    ) {
        try {
            Pizza pizza = repo.findById(id).get();
            model.addAttribute("pizza", pizza);

            if (result.hasErrors()) {
                return "pizzas/EditPizza";
            }

            if (!pizzaDto.getImageFile().isEmpty()) {
                String uploadDir = "public/images/";
                Path imagePathOld = Paths.get(uploadDir + pizza.getImageFileName());

                try {
                    Files.delete(imagePathOld);
                }
                catch (Exception exception){
                    System.out.println("Exception: " + exception.getMessage());
                }

                MultipartFile image = pizzaDto.getImageFile();
                Date createdAt = new Date();
                String storageFileName = createdAt.getTime() + "_" + image.getOriginalFilename();

                try (InputStream inputStream = image.getInputStream()) {
                    Files.copy(inputStream, Paths.get(uploadDir + storageFileName),
                            StandardCopyOption.REPLACE_EXISTING);
                }
                pizza.setImageFileName(storageFileName);
            }

            pizza.setName(pizzaDto.getName());
            pizza.setCategory(pizzaDto.getCategory());
            pizza.setPrice(pizzaDto.getPrice());
            pizza.setDescription(pizzaDto.getDescription());

            repo.save(pizza);

        }
        catch (Exception ex) {
            System.out.println("Exception: " + ex.getMessage());
        }

        return "redirect:" +
                "/pizzas";
    }

    @GetMapping("/delete")
    public String deleteProduct (
            @RequestParam int id) {
        try {
            Pizza pizza = repo.findById(id).get();

            Path imagePath = Paths. get("public/images/" + pizza.getImageFileName ()) ;
            try{
                Files. delete (imagePath) ;
            } catch (Exception exception){
                System.out.println("Exception: " + exception.getMessage());
            }

            repo.delete(pizza);

        }
        catch (Exception ex) {
            System.out.println("Exception: " + ex.getMessage());
        }

        return "redirect:/pizzas";
    }

}