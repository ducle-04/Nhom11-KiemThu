package vn.nhanam.bookstore.bookstore.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import vn.nhanam.bookstore.bookstore.dto.UpdateProfileDTO;
import vn.nhanam.bookstore.bookstore.dto.UserDTO;
import vn.nhanam.bookstore.bookstore.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 1. Lấy thông tin tài khoản của chính mình
    @GetMapping("/me")
    public ResponseEntity<UserDTO> getMyProfile(Authentication authentication) {
        String email = authentication.getName();
        UserDTO dto = userService.getMyProfile(email);
        return ResponseEntity.ok(dto);
    }

    // 2. Cập nhật hồ sơ của chính mình
    @PutMapping("/me")
    public ResponseEntity<UserDTO> updateMyProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileDTO dto
    ) {
        String email = authentication.getName();
        UserDTO updated = userService.updateOwnProfile(email, dto);
        return ResponseEntity.ok(updated);
    }

    // 3. Admin: tạo mới tài khoản người dùng
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserDTO dto) {
        UserDTO created = userService.createUser(dto);
        return ResponseEntity.ok(created);
    }

    // 4. Admin: lấy danh sách người dùng
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // 5. Admin: lấy user theo id
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    // 6. Admin: xóa user theo id
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("Xóa người dùng thành công");
    }
}
