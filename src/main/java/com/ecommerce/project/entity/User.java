package com.ecommerce.project.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@Entity
@Table(name="users",uniqueConstraints = {
        @UniqueConstraint(columnNames = "username"),
        @UniqueConstraint(columnNames = "email")
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @NotBlank
    @Size( max = 20)
    @Column(name = "username")
    private String username;

    @NotBlank
    @Size(max=120)
    @Column(name = "password")
    private String password;

    @NotBlank
    @Size(max=50)
    @Email
    @Column(name = "email")
    private String email;

    public User(String username , String password, String email) {
        this.password = password;
        this.email = email;
        this.username = username;
    }


    @Getter
    @Setter
    @ManyToMany(cascade = {CascadeType.PERSIST,CascadeType.MERGE},fetch = FetchType.EAGER)
    @JoinTable(name="user_role",
            joinColumns=@JoinColumn(name="user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

/*  1.orphanRemoval? means if user will be deleted then related
    orderd products also been deleted.
    2.for current condition user is also seller and he selling the products.
    3.Owner is product and User is child*/
    @ToString.Exclude
    @OneToMany(mappedBy = "user",cascade ={CascadeType.MERGE,CascadeType.PERSIST},
            orphanRemoval = true)
    private Set<Product> proudcts;

    @Getter
    @Setter
    @OneToMany(mappedBy = "users",cascade = {CascadeType.MERGE,CascadeType.PERSIST},orphanRemoval = true)
   /* @JoinTable(name="user_address",
    joinColumns = @JoinColumn(name ="user_id")  ,
    inverseJoinColumns =@JoinColumn(name="address_id")
    )*/
    private List<Address> addresses=new ArrayList<>();

    @ToString.Exclude
    @OneToOne(mappedBy = "user",cascade = {CascadeType.MERGE,CascadeType.PERSIST},orphanRemoval = true)
    private Cart cart;
}
