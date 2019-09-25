package com.billing.paypal.regularexpression;

/*
 * List of files : 
   ---------------
	
	abc123-2019fabc.csv
	abcf_2019$29#2123.csv
	abcfabc.csv
	fab.txt
	fabc2412-2019-10-20-f.csv
	fabcd(23).txt
	fabcd.txt
	fabcf.txt
	trr-2019-10-02-001.csv
	trr-2019-11-22.csv
	xfabcfx.txt
	zfab.txt
	zfabcd.txt
	abcfui_2019pathhelloworld-$29#2123.csv

   Input Pattern : 
   --------------

	"f[a-zA-Z0-9()]{8}.*" will return file name → fabcd(23).txt
	"f[a-zA-Z0-9].*"  will return file name → fab.txt, fabc2412-2019-10-20-f.csv , fabcd(23).txt , fabcd.txt , fabcf.txt. 
	"f*" will return file names -→ fab.txt, fabc2412-2019-10-20-f.csv , fabcd(23).txt , fabcd.txt , fabcf.txt. 
	"trr*" will return file names  ===> trr-2019-10-02-001.csv , trr-2019-11-22.csv
	"trr*2019-10*" will return file names  ===> trr-2019-10-02-001.csv
	"trr*2019-*-02*.csv" will return file names ===> trr-2019-10-02-001.csv
	"abc*2019*fabc.csv" will return file names ===> abc123-2019fabc.csv
	"a??*2019*fabc.csv" will return file names ===> abc123-2019fabc.csv
	"a???*2019*.csv" will return file names ===>  abcf_2019$29#2123.csv , abc123-2019fabc.csv
	"a?????*2019*#2123.csv" will return file names ====> abcfui_2019pathhelloworld-$29#2123.csv

 */

public class RegExConverter {
	
	public static int countQuestionmarks(String s) {
		int cnt = 0;

		char[] charr = s.toCharArray();
		for (char c : charr) {
			if (c == '?') {
				cnt++;
			}
		}
     return cnt; 
	}
	
	public static String buildReplacement(int cnt){
		StringBuilder blder = new StringBuilder(cnt);
		if (cnt > 0) {
			int pos = 0;
			while (pos < cnt) {
				blder.append("?");
				pos++;
			}
		} else {
			return null;
		}
		return blder.toString();
	}

}
