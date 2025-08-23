package com.YOGIITSU.repository;

import com.YOGIITSU.entity.Department;
import com.YOGIITSU.repository.projection.*;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface DepartmentRepository extends Repository<Department, Long> {

	// 단과대 ID로 학과 목록(id + name) 조회
	@Query(value = """
		    SELECT d.id        AS id,
		           d.department_name AS departmentName
		    FROM departments d
		    WHERE (:collegeId IS NULL OR d.college_id = :collegeId)
		    ORDER BY d.department_name
		""", nativeQuery = true)
	List<DeptListItemView> findDeptItemsByCollegeId(@Param("collegeId") Long collegeId);

	// 학과 ID 단건 정보(위치/전화/팩스/업무시간)
	@Query(value = """
		    SELECT d.department_name AS departmentName,
		           d.location        AS location
		    FROM departments d
		    WHERE d.id = :deptId
		""", nativeQuery = true)
	List<DeptLocationView> findLocationByDeptId(@Param("deptId") Long deptId);

	@Query(value = """
		    SELECT d.department_name AS departmentName,
		           d.phone           AS phone
		    FROM departments d
		    WHERE d.id = :deptId
		""", nativeQuery = true)
	List<DeptPhoneView> findPhoneByDeptId(@Param("deptId") Long deptId);

	@Query(value = """
		    SELECT d.department_name AS departmentName,
		           d.fax             AS fax
		    FROM departments d
		    WHERE d.id = :deptId
		""", nativeQuery = true)
	List<DeptFaxView> findFaxByDeptId(@Param("deptId") Long deptId);

	@Query(value = """
		    SELECT d.department_name AS departmentName,
		           d.office_hours    AS officeHours
		    FROM departments d
		    WHERE d.id = :deptId
		""", nativeQuery = true)
	List<DeptHoursView> findHoursByDeptId(@Param("deptId") Long deptId);
}