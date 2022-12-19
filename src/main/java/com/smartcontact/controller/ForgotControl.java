package com.smartcontact.controller;

import java.util.Random;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smartcontact.dao.UserRepository;
import com.smartcontact.entities.User;
import com.smartcontact.service.EmailService;

@Controller
public class ForgotControl {

	@Autowired
	private EmailService emailService;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	Random random = new Random(1000);

	// forgot password handler
	@RequestMapping("/forgot")
	public String openForgotPasswordForm() {
		return "forgot_password_form";
	}

	// send otp on mail
	@PostMapping("/send-otp")
	public String sendOTP(@RequestParam("email") String email, HttpSession session) {

		// generating 4 digit OTP

		int otp = random.nextInt(999999);
		System.out.println("OTP " + otp);

		// code for send otp to email....

		String message = "OTP- " + otp;
		String subject = "OTP From SCM";
		String to = email;

		boolean flag = this.emailService.sendEmail(message, subject, to);

		if (flag) {
			System.out.println("OTP "+otp);
			
			session.setAttribute("otp", otp);
			session.setAttribute("email", email);
			
			return "verify_otp";
		} else {

			session.setAttribute("message", "Check Your Email Id !!");
			
			return "forgot_password_form";
		}
	}
	
	// verify otp handler
	
	@PostMapping("/verify-otp")
	public String verifyOtp(@RequestParam("otp") int otp, HttpSession session) {
		
		int myOtp = (int) session.getAttribute("otp");
		String email = (String) session.getAttribute("email");
		
		if (myOtp==otp) {
			
			// check user in database
			User user = this.userRepository.getUserByUserName(email);
			
			if (user==null) {
				// send error message

				session.setAttribute("message", "User does not exist with this email !!");
				
				return "forgot_password_form";
			}else {
				// send change password form
				
				return "password_change_form";
			}
			
		}else {
			
			session.setAttribute("message", "You have entered wrong otp !!");
			return "verify_otp";
		}
		
	}
	
	// change password handler
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("newpassword") String newpassword, HttpSession session) {
		
		String email = (String) session.getAttribute("email");
		User user = this.userRepository.getUserByUserName(email);
		user.setPassword(this.bCryptPasswordEncoder.encode(newpassword));
		this.userRepository.save(user);
		
		return "redirect:/signin?change=password changed successfully..";
		
	}
	
	
	
	
	
	
	
	
	
}
