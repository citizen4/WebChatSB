package kc87.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.crypto.password.PasswordEncoder;

public class ScryptPasswordEncoder implements PasswordEncoder {

   private static final Logger LOG = LogManager.getLogger(ScryptPasswordEncoder.class);

   @Override
   public String encode(CharSequence rawPassword) {
      return null;
   }

   @Override
   public boolean matches(CharSequence rawPassword, String encodedPassword) {
      return false;
   }
}
