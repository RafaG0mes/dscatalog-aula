package br.com.devsuperior.dscatalog.repositories;

import br.com.devsuperior.dscatalog.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
