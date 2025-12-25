package vn.nhanam.bookstore.bookstore.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import vn.nhanam.bookstore.bookstore.config.JwtUtil;
import vn.nhanam.bookstore.bookstore.dto.LoginDTO;
import vn.nhanam.bookstore.bookstore.dto.RegisterDTO;
import vn.nhanam.bookstore.bookstore.entity.User;
import vn.nhanam.bookstore.bookstore.service.UserService;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@Validated
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserService userService,
                          JwtUtil jwtUtil,
                          UserDetailsService userDetailsService,
                          PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    // 1. Đăng ký
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterDTO registerDTO) {
        userService.register(registerDTO);
        return ResponseEntity.ok("Đăng ký người dùng thành công");
    }

    // 2. Đăng nhập (email hoặc username)
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDTO loginDTO) {
        try {
            String login = loginDTO.getLogin();

            UserDetails userDetails = userDetailsService.loadUserByUsername(login);

            User user = userService.findByLogin(login);

            if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
                return ResponseEntity.status(401).body("Thông tin đăng nhập không hợp lệ");
            }

            Set<String> roles = userDetails.getAuthorities().stream()
                    .map(a -> a.getAuthority().replace("ROLE_", ""))
                    .collect(Collectors.toSet());

            String token = jwtUtil.generateToken(userDetails.getUsername(), roles);

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("roles", roles);
            return ResponseEntity.ok(response);

        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(401).body("Thông tin đăng nhập không hợp lệ");
        }
    }

}
