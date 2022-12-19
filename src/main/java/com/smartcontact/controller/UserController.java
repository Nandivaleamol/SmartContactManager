package com.smartcontact.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.smartcontact.dao.ContactRepository;
import com.smartcontact.dao.UserRepository;
import com.smartcontact.entities.Contact;
import com.smartcontact.entities.User;
import com.smartcontact.helper.Message;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ContactRepository contactRepository;

	// method for adding common user data
	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		String username = principal.getName();
		System.out.println(username);
		User user = this.userRepository.getUserByUserName(username);
		System.out.println(user);

		model.addAttribute("user", user);
	}

	// user Dashboard handler
	@GetMapping("/index")
	public String dashboard(Model model, Principal principal) {
		model.addAttribute("title", "User Dashboard");
		model.addAttribute("contact", new Contact());
		return "normal/user_dashboard";
	}

	// add contact handler
	@GetMapping("/add-contact")
	public String addContact(Model model) {
		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());
		return "normal/add_contact_form";
	}

	// show user contacts
	// per page contacts = 5[n]
	// current page = 0[page]
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page, Model model, Principal principal) {
		model.addAttribute("title", "Show User Contacts");
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);

		// current page
		// contact per page = 5

		Pageable pageable = PageRequest.of(page, 5);
		Page<Contact> contacts = contactRepository.findContactsByUserIdContacts(user.getId(), pageable);

		model.addAttribute("contacts", contacts);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", contacts.getTotalPages());

		return "normal/show_contacts";
	}

	// showing particular user details
	@GetMapping("/{cId}/contact")
	public String showContactDetail(@PathVariable("cId") Integer cId, Model model, Principal principal) {
		System.out.println("CID " + cId);
		/*
		 * Optional<Contact> contactOptional = contactRepository.findById(cId); Contact
		 * contact = contactOptional.get();
		 */

		Contact contact = contactRepository.findById(cId).orElseThrow();

		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);

		// condition for validate current user contacts
		if (user.getId() == contact.getUser().getId()) {
			model.addAttribute("contact", contact);
			model.addAttribute("title", contact.getName());
		}

		return "normal/contact_detail";
	}

	// delete contact

	@GetMapping("/delete/{cId}")
	public String deleteContact(@PathVariable("cId") Integer cId, Model model, Principal principal,
			HttpSession session) {

		User user = this.userRepository.getUserByUserName(principal.getName());
		Contact contact = this.contactRepository.findById(cId).get();

		// condition for validate current user contacts
		if (user.getId() == contact.getUser().getId()) {
			System.out.println("Contact: " + contact.getcId());

			user.getContacts().remove(contact);
			this.userRepository.save(user);

			System.out.println("Deleted");
			session.setAttribute("message", new Message("Contact Deleted Successfully", "success"));
		}
		return "redirect:/user/show-contacts/0";
	}

	// open update contact form handler...
	@PostMapping("/update-contact/{cId}")
	public String updateForm(@PathVariable("cId") Integer cId, Model model) {
		model.addAttribute("title", "Update Contact");

		Contact contact = this.contactRepository.findById(cId).get();

		model.addAttribute("contact", contact);

		return "normal/update_form";
	}

	// update process contact form

	@RequestMapping(value = "/update-process", method = RequestMethod.POST)
	public String updateContact(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
			Model model, HttpSession session, Principal principal) {
		try {

			// old contact details
			Contact oldContactDetail = this.contactRepository.findById(contact.getcId()).get();

			// image file
			if (!file.isEmpty()) {
				// file work..
				// rewrite
				// delete old profile image

				File deleFile = new ClassPathResource("static/img").getFile();
				File file2 = new File(deleFile, oldContactDetail.getImage());
				file2.delete();

				// update image
				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(file.getOriginalFilename());

			} else {
				contact.setImage(oldContactDetail.getImage());
			}

			// update contact details
			User user = this.userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);
			this.contactRepository.save(contact);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return "redirect:/user/" + contact.getcId() + "/contact";
	}

	// process contact form
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
			Principal principal, HttpSession session) {

		try {
			String name = principal.getName();
			User user = this.userRepository.getUserByUserName(name);

			// processing and uploading file...
			if (file.isEmpty()) {
				// if the file is empty then try our message
				System.out.println("File is empty");

				// default image
				contact.setImage("contact.png");

			} else {
				// file the file to folder and update the name to contact
				contact.setImage(file.getOriginalFilename());

				File saveFile = new ClassPathResource("static/img").getFile();

				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

				System.out.println("Image is uploaded");

				// success message alert
				session.setAttribute("message", new Message("Your contact added!! Add more contacts...", "success"));

			}

			contact.setUser(user);
			user.getContacts().add(contact);
			this.userRepository.save(user);
			System.out.println("DATA: " + contact);
			System.out.println("Added to database");

		} catch (Exception e) {
			System.out.println("ERROR: " + e.getMessage());
			e.printStackTrace();
			// error message alert
			session.setAttribute("message", new Message("Something went wrong!! Try again...", "danger"));
		}
		return "normal/add_contact_form";
	}
	
	// User profile handler
	@GetMapping("/profile")
	public String profile(Model model) {
		model.addAttribute("title","Profile Page");
		return "normal/profile";
	}
	
	// settings handler
	@GetMapping("/settings")
	public String openSettings(Model model) {
		model.addAttribute("title","Settings Page");
		return"normal/settings";
	}
	
	// change password handler
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword, @RequestParam("newPassword")
	String newPassword, Principal principal, HttpSession session) {
		System.out.println("old pass" +oldPassword);
		System.out.println("new pass"+ newPassword);
		
		User currentUser = this.userRepository.getUserByUserName(principal.getName());
		System.out.println(currentUser.getPassword());
		
		if (this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword())) {
			
			// change password
			currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			User user = this.userRepository.save(currentUser);
			
			session.setAttribute("message", new Message("Your password is successfully changed !!", "success"));
			
		}else {
			session.setAttribute("message", new Message("Please enter correct old password !!", "danger"));
			return "redirect:/user/settings";

		}
		
		return "redirect:/user/index";
	}
	
	
	
	
	

}
