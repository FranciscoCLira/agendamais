package com.agendademais.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/menus")
public class MenuController {


    @GetMapping("/menu-participante")
    public String menuParticipante() {
        // 1- View em templates/menus/menu-participante.html
        return "menus/menu-participante"; 
    }

    @GetMapping("/menu-autor")
    public String menuAutor() {
        // 2- View em templates/menus/menu-autor.html
        return "menus/menu-autor"; 
    }

    @GetMapping("/menu-administrador")
    public String menuAdministrador() {
        // 5- View em templates/menus/menu-administrador.html
        return "menus/menu-administrador"; 
    }
	
    @GetMapping("/menu-superusuario")
    public String menuSuperUsuario() {
        // 9 - View em templates/menus/menu-superusuario.html
        return "menus/menu-superusuario"; 
    }

    @GetMapping("/menu-controle-total")
    public String menuControleTotal() {
        // 0 - View em templates/menus/menu-controle-total.html
        return "menus/menu-controle-total"; 
    }
}

