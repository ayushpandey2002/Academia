package com.springboot.controller;

import java.util.concurrent.CompletableFuture;

import com.springboot.service.MailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.springboot.entity.Student;
import com.springboot.service.StudentService;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;

@RestController
@RequestMapping("/student")
public class StudentController {
	
	Logger logger = LoggerFactory.getLogger(StudentController.class);

	@Autowired
	private StudentService studentService;

	@Autowired
	private MailService mailService;
	private volatile boolean emailSent = false;
	@PostMapping("/save")
	public Student saveStudent(@RequestBody Student student) {
		return studentService.saveStudent(student);
	}

	@GetMapping("/{id}")
	@CircuitBreaker(name="studentservice", fallbackMethod="fallbackMethod")
	@Retry(name="StudentService")
	@TimeLimiter(name="StudentService")
	@ResponseStatus(HttpStatus.ACCEPTED)
	public CompletableFuture<String> getStudentWithDepartment(@PathVariable Long id) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				String result = studentService.getStudentWithDepartment(id);

				sendEmailIfNeeded(result);
				return result;
			} catch (Exception e) {
				// Handle the exception or log it
				return "Error occurred while processing the request.";
			}
		});
	}

	private void sendEmailIfNeeded(String result) {
		// Add logic to send email only if needed
		if (result != null && result.equals("Student with department details send to email")) {
			synchronized (this) {
				// Use synchronization to ensure that email sending logic is executed only once
				if (!emailSent) {
					mailService.sendMail("rambadgoti13@gmail.com", "StudentWithDepartmentInfo", result);
					emailSent = true;
				}
			}
		}
	}
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public CompletableFuture<String> fallbackMethod(@PathVariable Long id, RuntimeException ex) {
		return CompletableFuture.supplyAsync(()->"Service is down. Please try after some time.");
	}
	
}
