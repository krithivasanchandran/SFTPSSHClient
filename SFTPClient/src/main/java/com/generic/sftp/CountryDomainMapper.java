package com.billing.sftp;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class CountryDomainMapper {
	
	static final SortedMap<String,String> countryCodeMapper = (SortedMap<String, String>) Collections.unmodifiableSortedMap(new TreeMap<String,String>(){
		{
			put("US","USD");
			put("GB","GBP");
			put("CA","CAD");
			put("EU","EUR");
			put("AU","AUD");
			put("SG","SGD");
			put("JP","JPY");
		}
	});
	
	static final Map<String,String> kPay_Process_Mode = Collections.unmodifiableMap(new HashMap<String,String>(){
		{
			put("PP_PAY_BART","put");
			put("PP_CONFIRM_BART","get");
			put("PP_SETTLE_BART","get");
			put("PP_TRR_BART","get");
		}
	});
    
	public String lookUpCC(String inbound_ccode){
		if(countryCodeMapper.containsKey(inbound_ccode)){
			return countryCodeMapper.get(inbound_ccode);
		}else{
			return null;
		}
	}
	
	public String lookModeFunc(String inbound_prcmode){
		if(kPay_Process_Mode.containsKey(inbound_prcmode)){
			return kPay_Process_Mode.get(inbound_prcmode);
		}else{
			return null;
		}
	}
}