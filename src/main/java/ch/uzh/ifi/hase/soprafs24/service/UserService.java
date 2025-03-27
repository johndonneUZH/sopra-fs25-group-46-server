package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.auth.JwtUtil;
import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.models.user.User;
import ch.uzh.ifi.hase.soprafs24.models.user.UserLogin;
import ch.uzh.ifi.hase.soprafs24.models.user.UserRegister;
import ch.uzh.ifi.hase.soprafs24.models.user.UserUpdate;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs24.constant.LoginStatus;

import java.util.HashMap;
import java.util.List;

@Service
@Transactional
public class UserService {

  private final Logger log = LoggerFactory.getLogger(UserService.class);
  private final JwtUtil jwtUtil;
  private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  UserService(JwtUtil jwtUtil) {
    this.jwtUtil = jwtUtil;
  }

  @Autowired
  private UserRepository userRepository;

  public List<User> getUsers() {
    return userRepository.findAll();
  } 

  public boolean checkIfUserExists(UserRegister newUser) {
    User userByUsername = userRepository.findByUsername(newUser.getUsername());
    User userByEmail = userRepository.findByEmail(newUser.getEmail());

    if (userByUsername != null || userByEmail!= null) {
      return true;
    }
    return false;
  }

  public User createUser(UserRegister newUser) {
    User toBeSavedUser = new User();
    toBeSavedUser.setUsername(newUser.getUsername());
    toBeSavedUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
    toBeSavedUser.setEmail(newUser.getEmail());
    toBeSavedUser.setName(newUser.getName());
    toBeSavedUser.setStatus(UserStatus.ONLINE);

    toBeSavedUser = userRepository.save(toBeSavedUser);

    log.debug("Created Information for User: " + newUser);
    return toBeSavedUser;
  }

    public LoginStatus checkLoginRequest(UserLogin loginRequest) {
        User user = userRepository.findByUsername(loginRequest.getUsername());
        if (user == null) {
            return LoginStatus.USER_NOT_FOUND;
        }
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            return LoginStatus.INVALID_PASSWORD;
        }
        return LoginStatus.SUCCESS;
    }


    public HashMap<String, String> getTokenById(UserLogin loginRequest) {
        User user = userRepository.findByUsername(loginRequest.getUsername());
        
        if (user == null) { 
          return null; 
        }

        String userId = user.getId().toString();

        HashMap<String, String> tokenMap = new HashMap<>();
        tokenMap.put("token", jwtUtil.generateToken(userId));
        tokenMap.put("userId", userId);

        return tokenMap;
    }

    public User getUserById(String userId) {
        return userRepository.findById(userId).orElse(null);
    }

    public void setStatusOnline(String userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        user.setStatus(UserStatus.ONLINE);
        userRepository.save(user);
    }

    public void setStatusOffline(String userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        user.setStatus(UserStatus.OFFLINE);
        userRepository.save(user);
    }

    public String getUserIdByToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid Authorization header");
        }

        String token = authHeader.replace("Bearer ", "");

        if (!jwtUtil.validateToken(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token");
        }

        return jwtUtil.extractUserId(token);
    }

    public void authenticateUser(String userId, String authHeader) {
      if (authHeader == null || !authHeader.startsWith("Bearer ")) {
          throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid Authorization header");
      }

      String token = authHeader.replace("Bearer ", "");

      if (!jwtUtil.validateToken(token)) {
          throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token");
      }

      String tokenUserId = jwtUtil.extractUserId(token);

      if (!tokenUserId.equals(userId)) {
          throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to update this user");
      }
    }
    
    public User updateUser(String userId, UserUpdate userUpdate) {
        User userById = userRepository.findById(userId).orElse(null);
        User userByUsername = userRepository.findByUsername(userUpdate.getUsername());
        if (userById == null) {
            return null;
        }

        userById.setName(userUpdate.getName());

        if (userByUsername != null && !userByUsername.getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }
        userById.setUsername(userUpdate.getUsername());
        userById.setPassword(passwordEncoder.encode(userUpdate.getPassword()));
        return userRepository.save(userById);
    }
}