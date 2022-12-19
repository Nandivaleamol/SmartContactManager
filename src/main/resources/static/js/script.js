/**
 * 
 */

console.log("JS file included");

const toggleSideBar = () => {
	if ($(".sidebar").is(":visible")) {
		// true
		// closing sidebar
		$(".sidebar").css("display", "none");
		$(".content").css("margin-left", "0%");
	} else {
		// false 
		// showing sidebar
		$(".sidebar").css("display", "block");
		$(".content").css("margin-left", "20%");
	}
};

// searching...

const search = () => {
	// console.log("searching...");

	let query = $("#search-input").val();

	if (query == ``) {
		$(".search-result").hide();
	} else {
		// searching
		console.log(query);

		// sending request to server
		let url = `http://localhost:8080/search/${query}`;

		fetch(url)
			.then((response) => {
				return response.json;
			})
			.then((data) => {
				// data
				console.log(data);

			});

		$(".search-result").show();
	}
};



// payment gatway...
// Not implemented...
// first request - to server to create order
/*
const paymentStart = ()=>{
	console.log("payment started...");
	
	let amount = $("#payment_field").val();
	console.log(amount);
	
	if(amount==null || amount==""){
		alert("Amount is required?");
		
		return
	}
	
	//code...
	// we will user ajax to send request to server to create order...
	
	$.ajax(
		{
			ur: "/payment/create_order",
			data: JSON.stringify({amount:amount, info:"order_request"}),
			contentType: "application/json",
			type: "POST",
			dataType: "json",
			
			success: function (response){
				//invoked when success
				console.log(response);
				
				if(response.status =="created"){
					//open payment form
					
				}
				
				
			},
			error: function (error){
				//invoked when error
				console.log(error)
				alert("Something went wrong !!");
			}
		}); 
		
};
*/















