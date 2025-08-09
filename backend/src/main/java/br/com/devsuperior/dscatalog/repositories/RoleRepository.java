package br.com.devsuperior.dscatalog.repositories;

import br.com.devsuperior.dscatalog.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
}
