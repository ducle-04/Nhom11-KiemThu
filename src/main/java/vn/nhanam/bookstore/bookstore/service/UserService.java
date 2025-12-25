package vn.nhanam.bookstore.bookstore.service;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import vn.nhanam.bookstore.bookstore.dto.RegisterDTO;
import vn.nhanam.bookstore.bookstore.dto.UserDTO;
import vn.nhanam.bookstore.bookstore.entity.Role;
import vn.nhanam.bookstore.bookstore.entity.User;
import vn.nhanam.bookstore.bookstore.repository.RoleRepository;
import vn.nhanam.bookstore.bookstore.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    // 1. Đăng ký người dùng
    @Transactional
    public User register(RegisterDTO dto) {

        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại");
        }

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email đã được sử dụng");
        }

        Role userRole = roleRepository.findByName("USER")
                .orElseGet(() ->
                        roleRepository.save(Role.builder().name("USER").build())
                );

        User user = User.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .phoneNumber(dto.getPhoneNumber())
                .enabled(true)
                .roles(Set.of(userRole))
                .build();

        return userRepository.save(user);
    }

    // 2. Admin tạo mới tài khoản người dùng
    @Transactional
    public UserDTO createUser(UserDTO dto) {

        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại");
        }

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email đã được sử dụng");
        }

        Set<String> roleNames = (dto.getRoles() == null || dto.getRoles().isEmpty())
                ? Set.of("USER")
                : dto.getRoles();

        User user = User.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .password(passwordEncoder.encode("123456"))
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .phoneNumber(dto.getPhoneNumber())
                .enabled(dto.isEnabled())
                .roles(getOrCreateRoles(roleNames))
                .build();

        return toUserDTO(userRepository.save(user));
    }

    // 3. Lấy danh sách người dùng
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toUserDTO)
                .collect(Collectors.toList());
    }

    // 4. Lấy người dùng theo ID
    public UserDTO getUserById(Long id) {
        return userRepository.findById(id)
                .map(this::toUserDTO)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));
    }

    // 5. Xóa người dùng
    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    // 6. Người dùng tự cập nhật hồ sơ
    @Transactional
    public UserDTO updateOwnProfile(String email, UserDTO dto) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        if (!user.getEmail().equals(dto.getEmail())
                && userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email đã được sử dụng");
        }

        user.setEmail(dto.getEmail());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setPhoneNumber(dto.getPhoneNumber());

        return toUserDTO(userRepository.save(user));
    }

    // 7. Chuyển User sang UserDTO
    private UserDTO toUserDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber(),
                user.isEnabled(),
                user.getRoles()
                        .stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet())
        );
    }

    // 8. Lấy hoặc tạo role
    private Set<Role> getOrCreateRoles(Set<String> roleNames) {
        Set<Role> roles = new HashSet<>();
        for (String roleName : roleNames) {
            Role role = roleRepository.findByName(roleName)
                    .orElseGet(() ->
                            roleRepository.save(Role.builder().name(roleName).build())
                    );
            roles.add(role);
        }
        return roles;
    }


    // 9. Tìm user theo email hoặc username
    public User findByLogin(String login) {
        return userRepository.findByEmail(login)
                .or(() -> userRepository.findByUsername(login))
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + login));
    }

    // 10. Lấy thông tin tài khoản hiện tại
    public UserDTO getMyProfile(String login) {
        User user = findByLogin(login);
        return toUserDTO(user);
    }
}
