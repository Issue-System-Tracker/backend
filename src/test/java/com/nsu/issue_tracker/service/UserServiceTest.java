package com.nsu.issue_tracker.service;

import com.nsu.issue_tracker.model.User;
import com.nsu.issue_tracker.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void findByEmailShouldReturnUserWhenExists() {
        User user = new User();
        user.setEmail("user@issue.local");
        when(userRepository.findByEmail("user@issue.local")).thenReturn(Optional.of(user));

        User result = userService.findByEmail("user@issue.local");
        assertEquals("user@issue.local", result.getEmail());
    }

    @Test
    void findByEmailShouldThrowWhenNotFound() {
        when(userRepository.findByEmail("missing@issue.local")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.findByEmail("missing@issue.local"));
    }

    @Test
    void existsByEmailShouldReflectRepositoryState() {
        when(userRepository.findByEmail("exists@issue.local")).thenReturn(Optional.of(new User()));
        when(userRepository.findByEmail("none@issue.local")).thenReturn(Optional.empty());

        assertEquals(true, userService.existsByEmail("exists@issue.local"));
        assertEquals(false, userService.existsByEmail("none@issue.local"));
    }

    @Test
    void getReferenceByIdShouldReturnNullForNullId() {
        assertEquals(null, userService.getReferenceById(null));
    }

    @Test
    void saveShouldDelegateToRepository() {
        User user = new User();
        userService.save(user);
        verify(userRepository).save(user);
    }

    @Test
    void findByIdShouldThrowWhenUserMissing() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.findById(userId));
    }

    @Test
    void getReferenceByIdShouldDelegateWhenIdProvided() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        when(userRepository.getReferenceById(userId)).thenReturn(user);

        User result = userService.getReferenceById(userId);
        assertEquals(userId, result.getId());
    }
}
