package it.unimol.taxManager.repository;

import it.unimol.taxManager.model.Brackets;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SogliaRepository extends JpaRepository<Brackets, Long> {
    // Metodo per trovare le soglie di un anno specifico
    Optional<Brackets> findByAnno(int anno);

    boolean existsByAnno(int anno);
}
