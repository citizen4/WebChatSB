package kc87.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.validator.constraints.Email;
import org.springframework.data.annotation.AccessType;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Entity
@Document
@Table(name = "account_tbl")
@AccessType(AccessType.Type.PROPERTY)
@SuppressWarnings("unused")
public class Account implements Serializable {
   private String id;

   private Long created;

   @NotNull
   private String firstName;

   @NotNull
   private String lastName;

   @NotNull
   @Email(message = "{error.email_invalid}")
   private String email;

   @NotNull
   @Size(min = 2, max = 32, message = "{error.wrong_length}")
   private String username;

   @NotNull
   private String password;

   @NotNull
   private String roles;

   @Id
   @GeneratedValue(generator = "uuid")
   @GenericGenerator(name = "uuid", strategy = "uuid2")
   public String getId() {
      return id;
   }

   public void setId(String id) {
      this.id = id;
   }

   public Long getCreated() {
      return this.created;
   }

   public void setCreated(Long created) {
      this.created = created;
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

   @Column(name = "hashed_password")
   @JsonIgnore // Exclude hash on JSON serialization
   public String getPassword() {
      return password;
   }

   @JsonProperty // Include hash on JSON deserialization
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
              ", created='" + created + '\'' +
              ", firstName='" + firstName + '\'' +
              ", lastName='" + lastName + '\'' +
              ", email='" + email + '\'' +
              ", username='" + username + '\'' +
              ", roles='" + roles + '\'' +
              ", password='" + password + '\'' +
              '}';
   }
}
