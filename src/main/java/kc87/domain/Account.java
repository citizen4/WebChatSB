package kc87.domain;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.SafeHtml;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;

public class Account implements Serializable {

   private Long id;

   @NotNull
   @SafeHtml
   @Size(min = 2, max = 32)
   private String firstName;

   @NotNull
   @SafeHtml
   @Size(min = 2, max = 32)
   private String lastName;

   @NotNull
   @Email(message = "Not a valid email address!")
   private String email;

   @NotNull
   @SafeHtml
   @Size(min = 2, max = 16)
   private String username;

   @NotNull
   @Size(min = 6, max = 32)
   private String password;


   private String pwHash;

   public Long getId() {
      return id;
   }

   public void setId(Long id) {
      this.id = id;
   }

   public String getFirstName() {
      return firstName;
   }

   public void setFirstName(String firstName) {
      this.firstName = firstName;
   }

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

   public String getPassword() {
      return password;
   }

   public void setPassword(String password) {
      this.password = password;
   }

   public String getPwHash() {
      return pwHash;
   }

   public void setPwHash(String pwHash) {
      this.pwHash = pwHash;
   }


   @Override
   public String toString() {
      return "Account{" +
            "id=" + id +
            ", firstName='" + firstName + '\'' +
            ", lastName='" + lastName + '\'' +
            ", email='" + email + '\'' +
            ", username='" + username + '\'' +
            ", password='" + password + '\'' +
            ", pwHash='" + pwHash + '\'' +
            '}';
   }
}
