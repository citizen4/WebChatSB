package kc87.domain;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.SafeHtml;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.beans.Transient;
import java.io.Serializable;

@Entity
@Table(name = "account_tbl")
@SuppressWarnings("unused")
public class Account implements Serializable {
   private Long id;

   @NotNull
   @SafeHtml(message = "No weired content please!")
   @Size(min = 2, max = 32, message = "Wrong size! (min: 2, max: 32)")
   private String firstName;

   @NotNull
   @SafeHtml(message = "No weired content please!")
   @Size(min = 2, max = 32, message = "Wrong size! (min: 2, max: 32)")
   private String lastName;

   @NotNull
   @Email(message = "{error.email_invalid}")
   private String email;

   @NotNull
   @SafeHtml(message = "No weired content please!")
   @Size(min = 2, max = 16, message = "Wrong size! (min: 2, max: 16)")
   private String username;

   @NotNull
   @Size(min = 6, max = 32, message = "Wrong size! (min: 6, max: 32)")
   private String password;

   private String pwHash;
   private String roles;


   @Id
   @GeneratedValue(strategy = GenerationType.AUTO)
   @Column(name = "ID")
   public Long getId() {
      return id;
   }

   public void setId(Long id) {
      this.id = id;
   }

   @Column(name = "FIRST_NAME")
   public String getFirstName() {
      return firstName;
   }

   public void setFirstName(String firstName) {
      this.firstName = firstName;
   }

   @Column(name = "LAST_NAME")
   public String getLastName() {
      return lastName;
   }

   public void setLastName(String lastName) {
      this.lastName = lastName;
   }

   @Column(name = "EMAIL")
   public String getEmail() {
      return email;
   }

   public void setEmail(String email) {
      this.email = email;
   }

   @Column(name = "USERNAME")
   public String getUsername() {
      return username;
   }

   public void setUsername(String username) {
      this.username = username;
   }

   @Transient
   public String getPassword() {
      return password;
   }

   public void setPassword(String password) {
      this.password = password;
   }

   @Column(name = "PW_HASH")
   public String getPwHash() {
      return pwHash;
   }

   public void setPwHash(String pwHash) {
      this.pwHash = pwHash;
   }

   @Column(name = "ROLES")
   public String getRoles() {
      return roles;
   }

   public void setRoles(String roles) {
      this.roles = roles;
   }

   @Override
   public String toString() {
      return "Account{" +
              "id=" + id +
              ", firstName='" + firstName + '\'' +
              ", lastName='" + lastName + '\'' +
              ", email='" + email + '\'' +
              ", username='" + username + '\'' +
              ", roles='" + roles + '\'' +
              ", password='" + password + '\'' +
              ", pwHash='" + pwHash + '\'' +
              '}';
   }
}
