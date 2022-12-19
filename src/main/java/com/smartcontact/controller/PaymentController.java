package com.smartcontact.controller;

import java.util.Map;

import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

@Controller
@RequestMapping("/payment")
public class PaymentController {

	// creating order for payment...
	
	//Not implemented...

	@PostMapping("/create_order")
	@ResponseBody
	public String createOrder(@RequestBody Map<String, Object> data) throws RazorpayException {
		System.out.println(data);

		int amt = Integer.parseInt(data.get("amount").toString());

		RazorpayClient client = new RazorpayClient("rzp_test_YWqtTOa5s43PDQ", "q1mljN2nwDTxxOmrwYUbzTBO");

		JSONObject object = new JSONObject();
		object.put("amount", amt * 100);
		object.put("currency", "INR");
		object.put("receipt", "txn_235425");

		// creating new order

		Order order = client.orders.create(object);
		System.out.println(order);

		// if you want you can save this to your database...

		return order.toString();
	}

}
