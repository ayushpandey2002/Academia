package com.springboot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.springboot.entity.Student;
import com.springboot.exception.StudentNotFoundException;
import com.springboot.repository.StudentRepository;
import com.springboot.vo.Department;

@Service
public class StudentService {

	private String baseUrl = "http://DEPARTMENTSERVICE:8090/department/";
	@Autowired
	private StudentRepository studentRepository;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private MailService mailService;

	public Student saveStudent(Student student) {
		return studentRepository.save(student);
	}

	public String getStudentWithDepartment(Long id) {
		Student student = studentRepository.findById(id).get();
		System.out.println("Student data is "+student);
		Department department = restTemplate.getForObject(baseUrl+student.getDepartmentId(), Department.class);
		StringBuffer buffer = new StringBuffer();
		buffer.append("Please find below ");
		buffer.append("\n");
		buffer.append(student.getStudentId()+"  "+student.getFirstName()+ "  "+student.getLastName()+"  "+student.getEmail()+"  "+student.getDepartmentId());
		buffer.append("\n");
		buffer.append("department = "+department.getDepartmentName()+"  "+"departmentCode = "+ department.getDepartmentCode()+"  "+"departmentAddress = "+department.getDepartmentAddress());
//		mailService.sendMail("rambadgoti13@gmail.com", "StudentWithDepartmentInfo", buffer.toString());
		return buffer.toString();
	}
}