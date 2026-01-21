package it.unimol.taxManager.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import it.unimol.taxManager.model.Tax;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

@Repository
public interface TaxRepository extends JpaRepository<Tax, Long> {

//    TODO: decommentare sono quelle utilizzate
//    List<Tax> findByStudent(Student student);
//
//    List<Tax> findByStatus(String status);
//
    Tax findByPagoPaNoticeCode(String pagoPaNoticeCode);

    @Query("SELECT t FROM Tax t WHERE t.student.id = :studentId")
    List<Tax> findTaxesByStudentId(@Param("studentId") String studentId);

}