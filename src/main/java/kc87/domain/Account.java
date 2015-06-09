package kc87.domain;

import kc87.web.RegisterFormValidationGroup;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.SafeHtml;
import org.springframework.data.annotation.AccessType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Entity
@Table(name = "account_tbl")
@AccessType(AccessType.Type.PROPERTY)
@SuppressWarnings("unused")
public class Account implements Serializable {
   private long id;

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

   @NotNull(groups = RegisterFormValidationGroup.class)
   @Size(min = 6, max = 32, message = "Wrong size! (min: 6, max: 32)", groups = RegisterFormValidationGroup.class)
   private String password;

   private String roles;


   @Id @GeneratedValue(strategy = GenerationType.AUTO)
   public long getId() {
      return id;
   }

   public void setId(long id) {
      this.id = id;
   }

   @Column(name = "first_name")
   public String getFirstName() {
      return firstName;
   }

   public void setFirstName(String firstName) {
      this.firstName = firstName;
   }

   @Column(name = "last_name")
   public String getLastName() {
      return lastName;
   }

   public void setLastName(String lastName) {
      this.lastName = lastName;
   }

   public String getEmail() {
      return email;
   }

   public void setEmail(String email) {
      this.email = email;
   }

   public String getUsername() {
      return username;
   }

   public void setUsername(String username) {
      this.username = username;
   }

   @Column(name = "encoded_password")
   public String getPassword() {
      return password;
   }

   public void setPassword(String password) {
      this.password = password;
   }

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
              '}';
   }
}
