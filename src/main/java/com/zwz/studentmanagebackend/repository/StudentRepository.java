package com.zwz.studentmanagebackend.repository;

import com.zwz.studentmanagebackend.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByStudentNo(String studentNo);
    List<Student> findByClassId(Long classId);
    Page<Student> findByNameContaining(String name, Pageable pageable);

    @Query("SELECT s FROM Student s WHERE s.name LIKE %:keyword% OR s.studentNo LIKE %:keyword%")
    Page<Student> search(@Param("keyword") String keyword, Pageable pageable);
}