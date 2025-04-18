package ch.uzh.ifi.hase.soprafs24.controller;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import ch.uzh.ifi.hase.soprafs24.models.project.Project;
import ch.uzh.ifi.hase.soprafs24.models.user.User;
import ch.uzh.ifi.hase.soprafs24.models.user.UserUpdate;


import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

  private final UserService userService;
  
  UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("")
  @PreAuthorize("hasAuthority('USER')")
  public List<User> getUsers() {
    return userService.getUsers();
  }
  
  @GetMapping("/{userId}")
  @PreAuthorize("hasAuthority('USER')")
  public ResponseEntity<User> getUser(@PathVariable String userId) {
    User user = userService.getUserById(userId);
    if (user == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }
    return ResponseEntity.status(HttpStatus.OK).body(user);
  }

  @PutMapping("/{userId}")
  @PreAuthorize("hasAuthority('USER')")
  public ResponseEntity<User> updateUser(
          @PathVariable String userId,
          @RequestBody UserUpdate updatedUser,
          @RequestHeader("Authorization") String authHeader) {
  
      userService.authenticateUser(userId, authHeader);
  
      User user = userService.updateUser(userId, updatedUser);
      if (user == null) {
          return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
      }
  
      return ResponseEntity.status(HttpStatus.ACCEPTED).body(user);
  }
  
  @GetMapping("/{userId}/projects")
  @PreAuthorize("hasAuthority('USER')")
  public ResponseEntity<List<Project>> getUserProjects(@PathVariable String userId) {
    List<Project> projects = userService.getUserProjects(userId);
    if (projects == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
    }
    return ResponseEntity.status(HttpStatus.OK).body(projects);
  }

  @GetMapping("/{userId}/friends")
  @PreAuthorize("hasAuthority('USER')")
  public ResponseEntity<List<User>> getUserFriends(@PathVariable String userId) {
    List<User> friends = userService.getUserFriends(userId);
    return ResponseEntity.status(HttpStatus.OK).body(friends);
  }

  @PostMapping("/{userId}/friends/invite/{friendId}")
  @PreAuthorize("hasAuthority('USER')")
  public ResponseEntity<Void> inviteFriend(@PathVariable String userId, @PathVariable String friendId) {
    userService.inviteFriend(userId, friendId);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @PostMapping("/{userId}/friends/accept/{friendId}")
  @PreAuthorize("hasAuthority('USER')")
  public ResponseEntity<Void> acceptFriend(@PathVariable String userId, @PathVariable String friendId) {
    userService.acceptFriend(userId, friendId);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @PostMapping("/{userId}/friends/reject/{friendId}")
  @PreAuthorize("hasAuthority('USER')")
  public ResponseEntity<Void> rejectFriend(@PathVariable String userId, @PathVariable String friendId) {
    userService.rejectFriend(userId, friendId);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @PostMapping("/{userId}/friends/remove/{friendId}")
  @PreAuthorize("hasAuthority('USER')")
  public ResponseEntity<Void> removeFriend(@PathVariable String userId, @PathVariable String friendId) {
    userService.removeFriend(userId, friendId);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @PostMapping("/{userId}/friends/cancel/{friendId}")
  @PreAuthorize("hasAuthority('USER')")
  public ResponseEntity<Void> cancelFriendRequest(@PathVariable String userId, @PathVariable String friendId) {
    userService.cancelFriendRequest(userId, friendId);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }
}


