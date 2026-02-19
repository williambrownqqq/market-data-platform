package com.market.data.platform.service;

import com.market.data.platform.dto.response.UserDTO;
import com.market.data.platform.dto.response.UserResponseDTO;
import com.market.data.platform.model.User;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService{

    List<UserDTO> getAllUsers();
    UserDTO getUserById(Long id);
    void deleteById(Long id);


}
