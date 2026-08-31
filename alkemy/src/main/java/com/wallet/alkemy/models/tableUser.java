package com.wallet.alkemy.models;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.wallet.alkemy.config.tableUserRole;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity (name = "Users")
@Table (name = "Users")
@Data
@NoArgsConstructor
@AllArgsConstructor


public class tableUser implements UserDetails {

    @Id
    @Column (name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column (name = "Name")
    private String name;

    @Column (name = "Last Name")
    private String lastName;

    @Column (name = "Email", nullable = false, unique = true)
    private String email;

    @Column (name = "Date_Created")
    private LocalDate dateCreated;

    @Column (name = "Last_Login")
    private LocalDate lastLogin;

    @Column (name = "is_Active")
    private boolean isActive;

    @Column (name = "City")
    private String city;

    @Column (name = "Province")
    private String province;

    @Column (name = "Country")
    private String country;

    @Column (name = "Birthdate")
    private LocalDate birthDate;

    @Column (name = "Address")
    private String address;

    @Column (name = "DNI")
    private BigInteger dni;

    @Column (name = "Postal_Code")
    private String postalCode;

    @Column (name = "Employment")
    private String employment;

    @Column (name = "Gender")
    private String gender;

    @Column (name = "Phone_Number")
    private BigInteger phoneNumber;

    @Column (name = "Password")
    private String password;

    @Column (name = "Role")
    @Enumerated(EnumType.STRING)
    private tableUserRole role;




    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override  // Aca se asigna el Email como nombre de usuario
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
}
