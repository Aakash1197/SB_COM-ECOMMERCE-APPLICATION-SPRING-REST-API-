package com.ecommerce.project.controller;


import com.ecommerce.project.security.response.MessageResponse;
import com.ecommerce.project.security.service.UserDetailsServiceImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import com.ecommerce.project.entity.AppRole;
import com.ecommerce.project.entity.Role;
import com.ecommerce.project.entity.User;
import com.ecommerce.project.repository.RoleRepository;
import com.ecommerce.project.repository.UserRepository;
import com.ecommerce.project.security.jwt.JwtUtils;
import com.ecommerce.project.security.request.LoginRequest;
import com.ecommerce.project.security.request.SignupRequest;
import com.ecommerce.project.security.response.UserInfoResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import com.ecommerce.project.security.service.UserDetailsImpl;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.stream.Collectors;

import java.util.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;



/*
    public void registerUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword())); // Password encoding
        userRepository.save(user); // Save the user with the encoded password
    }
*/




    @PostMapping("/signing")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest request1) {
        Authentication authentication;
        UserInfoResponse response;
        ResponseCookie jwtCookie;

        try {
            // Log the incoming credentials
            logger.info("Login attempt with username: {}", request1.getUsername());
            logger.info("Password entered: {}", request1.getPassword());

            // Perform authentication
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request1.getUsername(), request1.getPassword()));

            // Check if authentication is successful
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Get user details

           /*UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();*/
            UserDetailsImpl userDetails = (com.ecommerce.project.security.service.UserDetailsImpl) authentication.getPrincipal();
            logger.info("Encoded password from database: {}", userDetails.getPassword());

            /*// Generate JWT token from username
            String jwtToken = jwtUtils.generateTokenFromUsername(userDetails);*/


            // Generate JWT token from Cookie
              jwtCookie = jwtUtils.generateJwtCookie(userDetails);



            // Get roles
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            /*// Prepare response
            //setting UserInfoResponse() constructor value with Jwt token
            response = new UserInfoResponse(userDetails.getId(), userDetails.getUsername(), roles, jwtToken);*/

            //setting UserInfoResponse() constructor value without Jwt token
            response=new UserInfoResponse(userDetails.getId(), userDetails.getUsername(), roles);

        } catch (AuthenticationException exception) {
            logger.error("Authentication failed: {}", exception.getMessage());

            // Handle bad credentials
            Map<String, Object> map = new HashMap<>();
            map.put("message", "Bad credentials: incorrect username or password");
            map.put("status", false);
            return new ResponseEntity<>(map, HttpStatus.UNAUTHORIZED);
        }

       /* return new ResponseEntity<>(response, HttpStatus.OK);*/

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, String.valueOf(jwtCookie))
                .body(response);

    }



    @PostMapping("/singup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signupRequest) {
        logger.info("USER_REQUEST_BODY   :" + signupRequest.getUsername(), " " + signupRequest.getPassword() + "   " + signupRequest.getEmail() + "  " + signupRequest.getRole());
        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            return new ResponseEntity<>("Username already in taken!!.", HttpStatus.BAD_REQUEST);
        }
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            return new ResponseEntity<>("Email already in use!!.", HttpStatus.BAD_REQUEST);
        }

        //Create the new user's account
        User user = new User(signupRequest.getUsername(), encoder.encode(signupRequest.getPassword()), signupRequest.getEmail());
        /*     User user = new User(signupRequest.getUsername(),signupRequest.getPassword(), signupRequest.getEmail()*/

        logger.info("USERNAME -----:" + user.getUsername());
        logger.info("ENCODED PASSWORD -----:" + user.getPassword());
        logger.info("EMAIL-----:" + user.getEmail());
        logger.info("ROle-----:" + user.getRoles());
        Set<String> strRole = signupRequest.getRole();
        Set<Role> roles = new HashSet<>();
        //assigning a default role
        if (strRole == null) {
            Role role = roleRepository.findByRoleName(AppRole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(role);
        } else {
            strRole.forEach((role) -> {
                switch (role) {
                    case "admin":
                        Role adminRole = roleRepository.findByRoleName(AppRole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error:Role is not found ."));
                        roles.add(adminRole);

                        break;
                    case "seller":
                        Role sellerRole = roleRepository.findByRoleName(AppRole.ROLE_SELLER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found ."));
                        roles.add(sellerRole);

                        break;
                    default:
                        Role defaultRole = roleRepository.findByRoleName(AppRole.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found ."));
                        roles.add(defaultRole);

                }
            });

        }
        user.setRoles(roles);
       /* registerUser(user);*/
        userRepository.save(user);
        return new ResponseEntity<>("User registered successfully!!", HttpStatus.CREATED);
    }
    @GetMapping("/username")
    public String currentUserLoggedInServer(Authentication authentication) {
       return  (authentication!=null)? "Login Username :"+ authentication.getName() : "No user has been logged in!.";
    }
    @GetMapping("/userDetails")
    public ResponseEntity<?> currentUserDetailsLoggedInServer(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        if (userDetails==null){
            return new ResponseEntity<>("No User details has been found!!.", HttpStatus.OK);
        }
        // Get roles
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        UserInfoResponse response=new UserInfoResponse(userDetails.getId(), userDetails.getUsername(), roles);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @PostMapping("/signout")
    public ResponseEntity<?> userSignOut(){
    ResponseCookie cookie=jwtUtils.generateJwtCleanCookie();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, String.valueOf(cookie))
                .body(new MessageResponse("You have been signed out!!."));
    }






/*
    // In AuthController
    @PostMapping("/signing")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found");
                });


        // Compare raw password with encoded password
        if (!encoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
        logger.info("BEFORE  ----------------" + user.getUsername() + "  " + user.getPassword() + "    " + user.getRoles() + " " +
                "   " + user.getEmail() + "   " + user.getRoles() + "  " + user.getRoles().stream().toList());

     *//*   Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
*//*
        try {
            logger.info("AAAAAAAAAAAAA");
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            logger.info("BBBBBBBBBBBBBB");
            SecurityContextHolder.getContext().setAuthentication(authentication);
            logger.info("CCCCCCCCCCCCC");
        } catch (BadCredentialsException e) {
            // Handle bad credentials
            throw new RuntimeException("Invalid username or password.");
        } catch (Exception e) {
            // Handle other exceptions
            throw new RuntimeException("Authentication failed: " + e.getMessage());
        }
        logger.info("DDDDDDDDD");
        UserDetailsImpl userDetails = new UserDetailsImpl(user.getUserId(), user.getUsername(), user.getPassword(), user.getEmail(), user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getRoleName().toString())) // Convert each role to SimpleGrantedAuthority
                .collect(Collectors.toList()));
        logger.info("AFTER  ----------------" + user.getUsername() + "  " + user.getPassword() + "    " + user.getRoles() + " " +
                "   " + user.getEmail() + "   " + user.getRoles() + "  " + user.getRoles().stream().toList());

        String jwtToken = jwtUtils.generateTokenFromUsername(userDetails);
        Optional<User> present = userRepository.findByUsername(user.getUsername());

        List<String> assignedRoles = new ArrayList<>();
        if (present.isEmpty()) {
            throw new RuntimeException("userId not found");

        }
        present.get().getRoles().forEach(role -> {
            assignedRoles.add(role.getRoleName().toString());

        });



*//*
       List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());*//*

        UserInfoResponse response = new UserInfoResponse(userDetails.getId(), user.getUsername()
                , assignedRoles, jwtToken);

        logger.info("RESPONSE OBJECT ::" + response.toString());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }*/
}






