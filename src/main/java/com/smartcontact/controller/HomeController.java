package com.smartcontact.controller;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.smartcontact.dao.UserRepository;
import com.smartcontact.entities.User;
import com.smartcontact.helper.Message;

@Controller
public class HomeController {
	
	@Autowired
	BCryptPasswordEncoder passwordEncoder;

	@Autowired
	UserRepository userRepository;

	// home
	@RequestMapping("/")
	public String home(Model model) {
		model.addAttribute("title", "Home- Smart Contact Manager");

		return "home";
	}

	// about
	@RequestMapping("/about")
	public String about(Model model) {
		model.addAttribute("title", "About- Smart Contact Manager");

		return "about";
	}

	// signup
	@RequestMapping("/signup")
	public String singup(Model model) {
		model.addAttribute("title", "Signup- Smart Contact Manager");
		model.addAttribute("user", new User());
		return "signup";
	}

	// handler for registration
	@RequestMapping(value = "/do_register", method = RequestMethod.POST)
	public String register(@Valid @ModelAttribute("user") User user,BindingResult bindResult,
			@RequestParam(value = "agreement", defaultValue = "false") boolean agreement, Model model, HttpSession session ) {

		try {
			if (!agreement) {
				System.out.println("You have not agreed terms and conditions.");
				throw new Exception("You have not agreed terms and conditions.");
			}
			if (bindResult.hasErrors()) {
				System.out.println("ERROR : "+bindResult.toString());
				model.addAttribute("user",user);
				return "signup";
				
			}
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("default.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			
			System.out.println("Agreement : " + agreement);
			User result = userRepository.save(user);
			System.out.println(result);
			model.addAttribute("user",new User());
			session.setAttribute("message", new Message("Successfuly Registered !!", "alert-success"));
			return "signup";
		} catch (Exception e) {
			e.printStackTrace();
			session.setAttribute("message",new Message("Something went wrong!! "+ e.getMessage() , "alert-danger"));
			return "signup";
		}
		
	}

	// custom login handler
	@GetMapping("/signin")
	public String singin(Model model) {
		model.addAttribute("title", "Login- Smart Contact Manager");
		
		return "login";
	}
	
	
	
	
}
