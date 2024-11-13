package com.springboot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.controller.ApplyOfferRequest;
import com.springboot.controller.ApplyOfferResponse;
import com.springboot.controller.OfferRequest;
import com.springboot.controller.SegmentResponse;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CartOfferApplicationTests {


	@Test
	public void checkFlatXForOneSegment() throws Exception {

		//TC01 - Verify that a user in segment p1 with an offer of FLAT ₹10 off on a ₹200 order sees ₹190 to pay.
		List<String> segments = new ArrayList<>();
		segments.add("p1");
		OfferRequest offerRequest = new OfferRequest(1,"FLATX",10,segments);
		boolean result = addOffer(offerRequest);
		Assert.assertEquals(result,true); // able to add offer

		ApplyOfferRequest applyOfferRequest = new ApplyOfferRequest(200, 1, 1);
		ApplyOfferResponse applyOfferResponse = applyOffer(applyOfferRequest);
		int cartValue = applyOfferResponse.getCart_value();
		System.out.println("Cart_value for TC01 ====> "+cartValue);
		Assert.assertEquals("Incorrect cart value", 190, cartValue);


		//TC04 - Verify FLAT ₹10 off when total order amount is exactly ₹10.
		offerRequest = new OfferRequest(1,"FLATX",10,segments);
		result = addOffer(offerRequest);
		Assert.assertEquals(result,true); // able to add offer

		applyOfferRequest = new ApplyOfferRequest(10, 1, 1);
		applyOfferResponse = applyOffer(applyOfferRequest);
		cartValue = applyOfferResponse.getCart_value();
		System.out.println("Cart_value for TC04 ====> "+cartValue);
		Assert.assertEquals("Cart value should be zero, but incorrect value", 0, cartValue);

		//TC06 - Verify FLAT ₹10 off on a large order amount (e.g., ₹10,000).
		offerRequest = new OfferRequest(1,"FLATX",10,segments);
		result = addOffer(offerRequest);
		Assert.assertEquals(result,true); // able to add offer

		applyOfferRequest = new ApplyOfferRequest(10000, 1, 1);
		applyOfferResponse = applyOffer(applyOfferRequest);
		cartValue = applyOfferResponse.getCart_value();
		System.out.println("Cart_value for TC06 ====> "+cartValue);
		Assert.assertEquals("Incorrect value", 9990, cartValue);

		//TC10 - Verify behavior when total order amount after offer application becomes negative (e.g., FLAT ₹500 on ₹400).
		offerRequest = new OfferRequest(1,"FLATX",500,segments);
		result = addOffer(offerRequest);
		Assert.assertEquals(result,true); // able to add offer

		applyOfferRequest = new ApplyOfferRequest(400, 1, 1);
		applyOfferResponse = applyOffer(applyOfferRequest);
		cartValue = applyOfferResponse.getCart_value();
		System.out.println("Cart_value for TC10 ====> "+cartValue);
		Assert.assertEquals("Cart value should be zero, but incorrect value", 0, cartValue);
	}

	@Test
	public void checkFlatXPercentForOneSegment() throws Exception {
		//TC02 - Verify that a user in segment p2 with an offer of FLAT 10% off on a ₹200 order sees ₹180 to pay.
		List<String> segments = new ArrayList<>();
		segments.add("p2");
		OfferRequest offerRequest = new OfferRequest(1,"FLATX%",10,segments);
		boolean result = addOffer(offerRequest);
		Assert.assertEquals(result,true); // able to add offer

		ApplyOfferRequest applyOfferRequest = new ApplyOfferRequest(200, 1, 2);
		ApplyOfferResponse applyOfferResponse = applyOffer(applyOfferRequest);
		int cartValue = applyOfferResponse.getCart_value();
		System.out.println("Cart_value for TC02 ====> "+cartValue);
		Assert.assertEquals("Incorrect cart value", 180, cartValue);

		//TC05 - Verify FLAT 1% off when total order amount is ₹100 (smallest meaningful percentage).
		offerRequest = new OfferRequest(1,"FLATX%",1,segments);
		result = addOffer(offerRequest);
		Assert.assertEquals(result,true); // able to add offer

		applyOfferRequest = new ApplyOfferRequest(100, 1, 2);
		applyOfferResponse = applyOffer(applyOfferRequest);
		cartValue = applyOfferResponse.getCart_value();
		System.out.println("Cart_value for TC05 ====> "+cartValue);
		Assert.assertEquals("Incorrect cart value", 99, cartValue);

		//TC07 - Verify FLAT 10% off on a large order amount (e.g., ₹10,000).
		offerRequest = new OfferRequest(1,"FLATX%",10,segments);
		result = addOffer(offerRequest);
		Assert.assertEquals(result,true); // able to add offer

		applyOfferRequest = new ApplyOfferRequest(10000, 1, 2);
		applyOfferResponse = applyOffer(applyOfferRequest);
		cartValue = applyOfferResponse.getCart_value();
		System.out.println("Cart_value for TC07 ====> "+cartValue);
		Assert.assertEquals("Incorrect cart value", 9000, cartValue);

		//TC09 - Verify behavior when applying a 100% discount for a segment.
		offerRequest = new OfferRequest(1,"FLATX%",100,segments);
		result = addOffer(offerRequest);
		Assert.assertEquals(result,true); // able to add offer

		applyOfferRequest = new ApplyOfferRequest(10000, 1, 2);
		applyOfferResponse = applyOffer(applyOfferRequest);
		cartValue = applyOfferResponse.getCart_value();
		System.out.println("Cart_value for TC09 ====> "+cartValue);
		Assert.assertEquals("Cart value should be zero, but incorrect value", 0, cartValue);

		//TC11 - Verify behavior when applying a 200% discount for a segment.
		offerRequest = new OfferRequest(1,"FLATX%",200,segments);
		result = addOffer(offerRequest);
		Assert.assertEquals(result,true); // able to add offer

		applyOfferRequest = new ApplyOfferRequest(10000, 1, 2);
		applyOfferResponse = applyOffer(applyOfferRequest);
		cartValue = applyOfferResponse.getCart_value();
		System.out.println("Cart_value for TC11 ====> "+cartValue);
		Assert.assertEquals("Cart value should be zero, but incorrect value", 0, cartValue);
	}

	@Test
	public void checkNoOfferForOneSegment() throws Exception {
		//TC03 - Verify that a user in segment p3 with no offer applied sees the original amount of ₹200.
		ApplyOfferRequest applyOfferRequest = new ApplyOfferRequest(200, 1, 3);
		ApplyOfferResponse applyOfferResponse = applyOffer(applyOfferRequest);
		int cartValue = applyOfferResponse.getCart_value();
		System.out.println("Cart_value for TC03 ====> "+cartValue);
		Assert.assertEquals("Incorrect cart value", 200, cartValue);

		//TC08 - Verify behavior when order amount is ₹0 (no offers applicable).
		applyOfferRequest = new ApplyOfferRequest(0, 1, 3);
		applyOfferResponse = applyOffer(applyOfferRequest);
		cartValue = applyOfferResponse.getCart_value();
		System.out.println("Cart_value for TC08 ====> "+cartValue);
		Assert.assertEquals("Cart value should be zero", 0, cartValue);
	}

	public boolean addOffer(OfferRequest offerRequest) throws Exception {
		String urlString = "http://localhost:9001/api/v1/offer";
		URL url = new URL(urlString);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setDoOutput(true);
		con.setRequestProperty("Content-Type", "application/json");

		ObjectMapper mapper = new ObjectMapper();

		String POST_PARAMS = mapper.writeValueAsString(offerRequest);
		OutputStream os = con.getOutputStream();
		os.write(POST_PARAMS.getBytes());
		os.flush();
		os.close();
		int responseCode = con.getResponseCode();
		System.out.println("POST Response Code :: " + responseCode);

		if (responseCode == HttpURLConnection.HTTP_OK) { //success
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			// print result
			System.out.println(response.toString());
		} else {
			System.out.println("POST request did not work.");
		}
		return true;
	}

	public ApplyOfferResponse applyOffer(ApplyOfferRequest applyOfferRequest) throws Exception {
		String urlString = "http://localhost:9001/api/v1/cart/apply_offer";
		URL url = new URL(urlString);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setDoOutput(true);
		con.setRequestProperty("Content-Type", "application/json");

		ObjectMapper mapper = new ObjectMapper();

		String POST_PARAMS = mapper.writeValueAsString(applyOfferRequest);
		OutputStream os = con.getOutputStream();
		os.write(POST_PARAMS.getBytes());
		os.flush();
		os.close();
		int responseCode = con.getResponseCode();
		System.out.println("POST Response Code :: " + responseCode);

		if (responseCode == HttpURLConnection.HTTP_OK) { //success
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			// print result
			System.out.println(response.toString());
			ObjectMapper objectMapper = new ObjectMapper();
			return objectMapper.readValue(response.toString(), ApplyOfferResponse.class);
		} else {
			System.out.println("POST request did not work.");
		}
		return null;
	}
}
