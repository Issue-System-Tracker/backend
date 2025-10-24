package com.nsu.issue_tracker.service;

import com.nsu.issue_tracker.model.User;
import com.nsu.issue_tracker.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException
                        ("User with provided email does not exists"));
    }

    public User findById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException
                        ("User with provided id does not exists"));
    }

    public Boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public void save(User user) {
        userRepository.save(user);
    }

    public User getReferenceById(UUID id) {
        return id != null
                ? userRepository.getReferenceById(id)
                : null;
    }
}
