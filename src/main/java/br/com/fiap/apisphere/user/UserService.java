package br.com.fiap.apisphere.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import br.com.fiap.apisphere.user.dto.UserProfileResponse;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
public class UserService {

    @Autowired
    UserRepository repository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<User> findAll(){
        return repository.findAll();
    }

    public User create(User user){
        user.setPassword(passwordEncoder.encode(user.getPassword()));
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
            
            var avatarUrl = "http://localhost:8080/avatars/" + destinationFile.getFileName();
            user.setAvatar(avatarUrl);
            repository.save(user);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to store file", e.getCause());
        }

    }
}
