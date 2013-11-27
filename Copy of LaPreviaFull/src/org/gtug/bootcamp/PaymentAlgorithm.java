package org.gtug.bootcamp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

class PaymentAlgorithm {
	static final float MIN_PAYMENT = 0.01f;
		
	public class Payment {
		public int mPayer;
		public int mPayee;
		public float mAmount;
		
		Payment(int payerId, int payeeId, float amount) {
			mPayer = payerId;
			mPayee = payeeId;
			mAmount = amount;
		}
	}

	public List<Payment> getResult(List<Float> amounts) {
		initPayersAndPayees(amounts);
		
		List<Payment> payments = new ArrayList<Payment>();
		
		for (HashMap.Entry<Integer, Float> payer : mPayers.entrySet()) {
			Integer payerId = payer.getKey();
			Float payerAmount = payer.getValue();
			
			for (HashMap.Entry<Integer, Float> payee : mPayees.entrySet()) {
				Integer payeeId = payee.getKey();
				Float payeeAmount = payee.getValue();
				
				if (payeeAmount >= MIN_PAYMENT) {
					Float minAmount = Math.min(payerAmount, payeeAmount);
					payerAmount -= minAmount;							
					payeeAmount -= minAmount;
					
					payments.add(new Payment(payerId, payeeId, minAmount));

					mPayers.put(payerId, payerAmount);
					mPayees.put(payeeId, payeeAmount);
					
					if (payerAmount < MIN_PAYMENT) {
						break;
					}
				} 
			}
		}

		return payments;
	}

	private HashMap<Integer, Float> mPayers = new HashMap<Integer, Float>();	 // id es el dinero a pagar
	private HashMap<Integer, Float> mPayees = new HashMap<Integer, Float>();	 // id es el dinero que recibira

	private void initPayersAndPayees(List<Float> amounts) {
		Float average = getAverage(amounts);
		
		Integer id = 0;
		for (Float amount : amounts) {
			if (amount > average) {
				mPayees.put(new Integer(id), new Float(amount - average));
			} else if (amount < average){
				mPayers.put(new Integer(id), new Float(average - amount));
			}
			id++;
		}			
	}
	
	private Float getAverage(List<Float> amounts) {
		Float total = 0.0f;	

		for (Float amount : amounts) {
			total += amount;
		}

		return total/amounts.size();	
	}
}