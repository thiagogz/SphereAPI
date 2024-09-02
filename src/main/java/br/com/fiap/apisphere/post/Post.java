package br.com.fiap.apisphere.post;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

import br.com.fiap.apisphere.user.User;

@Entity
@Table(name = "posts")
@Data
public class Post {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String text;
    LocalDateTime createdAt;

    @ManyToOne
    User user;
}
