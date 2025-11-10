package com.vinodh.security.jwt;

import com.vinodh.security.jwt.model.Role;
import com.vinodh.security.jwt.model.User;
import com.vinodh.security.jwt.repository.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.util.Optional;

@SpringBootApplication
public class SpringSecurityApplication implements CommandLineRunner  {

	@Autowired
	private IUserRepository userRepository;

	public static void main(String[] args) {
		SpringApplication.run(SpringSecurityApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		Optional<User> adminAccount = userRepository.findByRole(Role.ADMIN);

		if (adminAccount.isEmpty()) {
			User user = new User();

			user.setFirstName("vinodh");
			user.setLastName("sangavaram");
			user.setEmail("admin@vcti.io");
			user.setPassword(new BCryptPasswordEncoder().encode("admin"));
			user.setRole(Role.ADMIN);

			userRepository.save(user);
		}
	}
}
