package com.moonlight.project.airBnbApp.security;

import com.moonlight.project.airBnbApp.dto.LoginDto;
import com.moonlight.project.airBnbApp.dto.SignUpRequestDto;
import com.moonlight.project.airBnbApp.dto.UserDto;
import com.moonlight.project.airBnbApp.entity.User;
import com.moonlight.project.airBnbApp.entity.enums.Role;
import com.moonlight.project.airBnbApp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;

    public UserDto signUp(SignUpRequestDto signUpRequestDto) {

        // Check if the user already exists
        User user = userRepository.findByEmail(signUpRequestDto.getEmail()).orElse(null);

        if (user != null) {
            throw new RuntimeException("User already exists with email: " + signUpRequestDto.getEmail());
        }

        // Create the new user and set properties
        User newUser = new User();
        newUser.setEmail(signUpRequestDto.getEmail());
        newUser.setName(signUpRequestDto.getName());
        newUser.setPassword(passwordEncoder.encode(signUpRequestDto.getPassword()));

        // Assign default role
        newUser.setRoles(Set.of(Role.GUEST));

        // Save to DB
        User savedUser = userRepository.save(newUser);

        // Map and return DTO
        return modelMapper.map(savedUser, UserDto.class);
    }

    // --- NEW: Method to securely register Admin users ---
    public UserDto signUpAdmin(SignUpRequestDto signUpRequestDto) {
        User user = userRepository.findByEmail(signUpRequestDto.getEmail()).orElse(null);

        if (user != null) {
            throw new RuntimeException("User already exists with email: " + signUpRequestDto.getEmail());
        }

        User newUser = new User();
        newUser.setEmail(signUpRequestDto.getEmail());
        newUser.setName(signUpRequestDto.getName());
        newUser.setPassword(passwordEncoder.encode(signUpRequestDto.getPassword()));

        // Assign the Manager role
        newUser.setRoles(Set.of(Role.HOTEL_MANAGER));

        User savedUser = userRepository.save(newUser);

        return modelMapper.map(savedUser, UserDto.class);
    }

    public String[] login(LoginDto loginDto) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginDto.getEmail(), loginDto.getPassword()
        ));

        User user = (User) authentication.getPrincipal();

        String[] arr = new String[2];
        arr[0] = jwtService.generateAccessToken(user);
        arr[1] = jwtService.generateRefreshToken(user);

        return arr;
    }

    // --- NEW: Method to validate a refresh token and issue a new access token ---
    public String refreshToken(String refreshToken) {
        // 1. Extract the user ID from the refresh token.
        // If the token is invalid/expired, jwtService returns null (caught in the controller)
        Long userId = jwtService.getUserIdFromToken(refreshToken);

        if (userId == null) {
            throw new RuntimeException("Invalid refresh token");
        }

        // 2. Fetch the user from the database
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));

        // 3. Generate and return a fresh access token
        return jwtService.generateAccessToken(user);
    }
}