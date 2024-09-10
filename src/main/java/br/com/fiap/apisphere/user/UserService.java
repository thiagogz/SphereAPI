package br.com.fiap.apisphere.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.fiap.apisphere.mail.MailService;
import br.com.fiap.apisphere.user.dto.UserProfileResponse;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class UserService {

    @Autowired
    UserRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MailService mailService;

    public List<User> findAll(){
        return repository.findAll();
    }

    public User create(User user){
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setAvatar("https://avatar.iran.liara.run/username?username=" + user.getName());
        mailService.sendWelcomeEmail(user);
        return repository.save(user);
    }

    public UserProfileResponse getUserProfile(String email){
        return repository.findByEmail(email)
                .map(UserProfileResponse::new)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public void updateAvatar(String email, MultipartFile file){
        if (file.isEmpty())
            throw new RuntimeException("File is empty");
        
        Path destinationPath = Path.of("src/main/resource/static/avatars");
        Path destinationFile = destinationPath.resolve(System.currentTimeMillis() + email +"_" +file.getOriginalFilename()).normalize().toAbsolutePath();

        try (InputStream is = file.getInputStream()) {
            Files.copy(is, destinationFile);

            var user = repository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            
            var baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
            var avatarUrl =  baseUrl + "/users/avatar/" + destinationFile.getFileName();
            user.setAvatar(avatarUrl);
            repository.save(user);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to store file", e.getCause());
        }

    }

    public ResponseEntity<Resource> getAvatar(String filename) {
        Path path = Paths.get("src/main/resources/static/avatars/" + filename);
        Resource file = UrlResource.from(path.toUri());

        return ResponseEntity
                .ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(file);
    }

    public List<User> findByName(String name) {
        return repository.findByNameContainingIgnoreCase(name);
    }
}
