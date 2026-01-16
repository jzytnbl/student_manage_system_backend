package com.zwz.studentmanagebackend.mapper;

import com.zwz.studentmanagebackend.entity.Student;
import org.apache.ibatis.annotations.*;
import java.util.List;
import java.util.Map;

@Mapper
public interface StudentMapper {

    @Select("SELECT * FROM students WHERE name LIKE CONCAT('%', #{name}, '%')")
    List<Student> findByName(@Param("name") String name);

    @Select("SELECT * FROM students WHERE class_id = #{classId}")
    List<Student> findByClassId(@Param("classId") Long classId);

    // XML映射
    List<Student> selectStudents(Map<String, Object> params);

    @Insert("INSERT INTO students (student_no, name, age, gender, email, phone, address, class_id) " +
            "VALUES (#{studentNo}, #{name}, #{age}, #{gender}, #{email}, #{phone}, #{address}, #{classId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Student student);

    @Update("UPDATE students SET name=#{name}, age=#{age}, gender=#{gender}, email=#{email}, " +
            "phone=#{phone}, address=#{address}, class_id=#{classId} WHERE id=#{id}")
    int update(Student student);

    @Delete("DELETE FROM students WHERE id=#{id}")
    int delete(Long id);

    // 修改：将文本块改为普通字符串（Java 8兼容）
    @Select("SELECT c.id as classId, c.name as className, COUNT(s.id) as studentCount " +
            "FROM classes c " +
            "LEFT JOIN students s ON c.id = s.class_id " +
            "GROUP BY c.id, c.name")
    List<Map<String, Object>> countByClass();
}