package com.market.data.platform.controller;

import com.market.data.platform.dto.response.UserDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.market.data.platform.service.UserService;

import java.util.List;

@Tag(name = "User management", description = "Endpoints for managing users")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final  UserService userService;

    @GetMapping
    @Operation(summary = "Get all users", description = "Get a list of all users in system")
    public List<UserDTO> getAll() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Get details of a user by  id")
    public UserDTO getById(@PathVariable Long id) {
        return userService.getUserById(id);
    }


}
