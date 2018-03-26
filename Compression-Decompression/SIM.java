/*
I have neither given nor received any unauthorized aid on this assignment
Author  :  Akash R Vasishta
UFID    :  53955080
College :  University of Florida
Course  :  CDA 5636 - Embedded Systems 
*/

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.nio.file.*;

public class SIM{
	static LinkedHashMap<String, Integer> instMap = new LinkedHashMap<String, Integer>();
	static LinkedHashMap<String, Integer> dictionary = new LinkedHashMap<String, Integer>();
	static LinkedHashMap<Integer, String> dictionaryReverse = new LinkedHashMap<Integer, String>();
	static HashMap<String, Integer> formatToLength = new HashMap<String, Integer>();
	static List<String> instList = new ArrayList<String>();
	static List<String> compList = new ArrayList<String>();
	static int rle = 0;
	static String output = "";
	static int i;
	static String compInstAll = "";
	static String decompInst = "";

	public static void main(String[] args){
		String compOrDecomp = args[0];
		if(compOrDecomp.equals("1")){
			compress();
		} else if(compOrDecomp.equals("2")){
			decompress();
		} else
			System.out.println("ERROR: Please enter correct option");
	}

	public static void compress(){
		String fileName = null;
		String instLine;
		String prevInst = "not valid";

		fileName = "original.txt";
		File file = new File(fileName);
		try{
			BufferedReader br = new BufferedReader(new FileReader(file));
			while ((instLine = br.readLine()) != null){
				instList.add(instLine);
				if(instMap.containsKey(instLine)){
					instMap.put(instLine, instMap.get(instLine) + 1);
				} else{
					instMap.put(instLine, 1);					
				}
			}
		} catch(IOException e ){
			e.printStackTrace();
		} catch(Exception e){
			e.printStackTrace();
		}
//////////////////////////////////////////////////////////
//////////		Implement Dictionary		//////////////
//////////////////////////////////////////////////////////
        dictionary = getTop8ByValue(instMap);

		for(i=0; i<instList.size(); i++){
			String compInstLine = "";
			instLine = instList.get(i);

			if(instLine.equals(prevInst)){
				rle++;
			} else{//New Instruction
				if(rle>1){//Check if it is RLE
					printRle();//Print previous (rle-1) instructions
					rle = 0;
					compInstAll = compInstAll;// + "\n";
				}
				if(dictionary.containsKey(instLine)){//Direct Matching
					int dict_index = dictionary.get(instLine);
					compInstLine = String.format("%3s", Integer.toBinaryString(dict_index)).replace(' ', '0');

					// compInstLine = Integer.toBinaryString(dict_index);
					// System.out.println(compInstLine.length() + " " + compInstLine);
					// compInstLine = compInstLine.substring(compInstLine.length()-3);
					compInstAll = compInstAll + "101" + compInstLine;
				} 
				else if(!((compInstLine = checkOneBitMismatch(instLine)).equals(""))){// 1 Bit mismatch
					compInstAll = compInstAll + "010" + compInstLine;
				} 
				else if(!((compInstLine = checkTwoBitMismatch(instLine)).equals(""))){// 2 Bit mismatch
					compInstAll = compInstAll + "011" + compInstLine;
				} 
				else if(!((compInstLine = checkBitMask(instLine)).equals(""))){// Bit mask compression
					compInstAll = compInstAll + "001" + compInstLine;
				} 
				else if(!((compInstLine = checkTwoBitAnywhere(instLine)).equals(""))){// 2 bit mismatch anywhere
					compInstAll = compInstAll + "100" + compInstLine;
				} 
				else {// Original binary
					compInstAll = compInstAll + "110" + instLine;
				}
				rle = 1;
				prevInst = instLine;
			}
			// compInstAll = compInstAll + "\n" + (i + 2) + ". ";
		}
		// System.out.println("(compInstAll.length()/32)*32 = " + (compInstAll.length()/32)*32);
		String comp = String.format("%-" + ((compInstAll.length()/32)+1)*32 + "s", compInstAll).replace(' ', '1');
		String parsedStr = comp.replaceAll("(.{32})", "$1\n").trim();
		try{
			PrintStream fileStream = new PrintStream("cout.txt");
			System.setOut(fileStream);
		} catch(IOException e ){
			e.printStackTrace();
		} catch(Exception e){
			e.printStackTrace();
		}
		System.out.println(parsedStr);
		System.out.println("xxxx");
        Iterator it = dictionary.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry pair = (Map.Entry)it.next();
            System.out.println(pair.getKey());// + " " + Integer.toBinaryString(pair.getValue()));
        }


	}

	public static String intToString(int dict_index, int strLength){
		// String dict_index_S = Integer.toBinaryString(dict_index);
		// dict_index_S = dict_index_S.substring(dict_index_S.length()-strLength);
		String dict_index_S = String.format("%3s", Integer.toBinaryString(dict_index)).replace(' ', '0');
		return dict_index_S;
	}

    public static LinkedHashMap<String, Integer> getTop8ByValue(LinkedHashMap<String, Integer> map) {
    	AtomicInteger index = new AtomicInteger();
    	return map.entrySet().stream()
        	.sorted(Entry.<String, Integer> comparingByValue().reversed())
        	.limit(8)
        	.collect(
            	Collectors.toMap(
                	e -> e.getKey(),
                	e -> index.getAndIncrement(),
                	(k, v) -> {
                    	throw new IllegalStateException("Duplicate key " + k);
                	},
                	LinkedHashMap::new)
            );
    }

	public static void printRle(){
		String compInst = "";
		switch (rle){
			case 2: compInst = "00";
					break;
			case 3: compInst = "01";
					break;
			case 4: compInst = "10";
					break;
			case 5: compInst = "11";
					break;			
		}
		compInstAll = compInstAll + "000" + compInst;
	}

	public static String checkOneBitMismatch(String instLine){
		for(int h=0; h<instLine.length(); h++){
			StringBuilder instLineSb = new StringBuilder(instLine);
			// System.out.println(instLine.length() + " " + instLineSb);
			instLineSb.setCharAt(h, instLineSb.charAt(h)=='0' ? '1':'0');
			// if(instLineSb.charAt(h)== '0')
			// 	instLineSb.setCharAt(h, '1');
			// else
			// 	instLineSb.setCharAt(h, '0');
			String instruction = instLineSb.toString();
			if(dictionary.containsKey(instruction)){
				String mismatchLocation = String.format("%5s", Integer.toBinaryString(h)).replace(' ', '0');
				int dict_index = dictionary.get(instruction);
				String dict_index_S = intToString(dict_index, 3);
				return mismatchLocation + dict_index_S;
			}

		}
		return "";
	}

	public static String checkTwoBitMismatch(String instLine){
		for(int h=0; h<instLine.length()-1; h++){
			StringBuilder instLineSb = new StringBuilder(instLine);
			if(instLineSb.charAt(h) == '0' && instLineSb.charAt(h+1) == '0'){
				instLineSb.setCharAt(h, '1');
				instLineSb.setCharAt(h+1, '1');
			} else if(instLineSb.charAt(h) == '0' && instLineSb.charAt(h+1) == '1'){
				instLineSb.setCharAt(h, '1');
				instLineSb.setCharAt(h+1, '0');
			} else if(instLineSb.charAt(h) == '1' && instLineSb.charAt(h+1) == '0'){
				instLineSb.setCharAt(h, '0');
				instLineSb.setCharAt(h+1, '1');
			} else {
				instLineSb.setCharAt(h, '0');
				instLineSb.setCharAt(h+1, '0');
			}
			String instruction = instLineSb.toString();
			if(dictionary.containsKey(instruction)){
				int dict_index = dictionary.get(instruction);
				String dict_index_S = intToString(dict_index, 3);
				String mismatchLocation = String.format("%5s", Integer.toBinaryString(h)).replace(' ', '0');
				return mismatchLocation + dict_index_S;
			}
		}
		return "";
	}

	public static String checkBitMask(String instLine){
		for(int i=8; i<16; i++){
			String maskSmall = String.format("%4s", Integer.toBinaryString(i)).replace(' ', '0');
			for(int q=0; q<29; q++){
				StringBuilder instLineSb = new StringBuilder();
				// String padLeft = padLeftZeros(Integer.toBinaryString(i), q);
				// String mask = rightPadZeros(padLeft, 32);				
				// StringBuilder instLineSb = new StringBuilder();
				// for(int j=0; j<mask.length(); j++){
				// 	instLineSb.append(charOf(bitOf(mask.charAt(j)) ^ bitOf(instLine.charAt(j))));
				// }
				String padLeftZeros = String.format("%" + (q+4) + "s", maskSmall).replace(' ', '0');
				String mask = String.format("%-" + 32 + "s", padLeftZeros ).replace(' ', '0');
				// System.out.println("//////////" + mask + "/////////" + instLine);
				// String instruction="";
				for(int j=0; j<32; j++){
					instLineSb.append(charOf(bitOf(mask.charAt(j)) ^ bitOf(instLine.charAt(j))));
				}
				String instruction = instLineSb.toString();
				// System.out.println("//////////" + instruction);
				if(dictionary.containsKey(instruction)){
					int dict_index = dictionary.get(instruction);
					String dict_index_S = intToString(dict_index, 3);
					String mismatchLocation = String.format("%5s", Integer.toBinaryString(q)).replace(' ', '0');

					// System.out.println("//////////" + mismatchLocation + maskSmall + dict_index_S);
					return  mismatchLocation + maskSmall + dict_index_S;
				}
			}
		}
		return "";
	}
	
	public static String cyclicLeftShift(String s, int k){
		k = k%s.length();
		return s.substring(k) + s.substring(0, k);
	}
	
	public static String checkTwoBitAnywhere(String instLine){
		String maskLeftBit = "10000000000000000000000000000000";
		String maskRightBit = "01000000000000000000000000000000";
		for(int i=0; i<31; i++){
			// String maskLeftBit = String.format("%32s", Integer.toBinaryString(i)).replace(' ', '0');
			String leftMask = cyclicLeftShift(maskLeftBit, (32-i));
			for(int j=i+1; j<32; j++){
				StringBuilder maskSb = new StringBuilder();
				StringBuilder instLineSb = new StringBuilder();
				// String maskRightBit = String.format("%32s", Integer.toBinaryString(i)).replace(' ', '0');
				String rightMask = cyclicLeftShift(maskLeftBit, (32-j));
				for(int h=0; h<32; h++){
					maskSb.append(charOf(bitOf(leftMask.charAt(h)) ^ bitOf(rightMask.charAt(h))));
					instLineSb.append(charOf(bitOf(maskSb.charAt(h)) ^ bitOf(instLine.charAt(h))));
				}
				String instruction = instLineSb.toString();
				// System.out.println("//////////" + instruction + "/////////");
				if(dictionary.containsKey(instruction)){
					int dict_index = dictionary.get(instruction);
					String dict_index_S = intToString(dict_index, 3);
					String leftMismatchLocation = String.format("%5s", Integer.toBinaryString(i)).replace(' ', '0');
					String rightMismatchLocation = String.format("%5s", Integer.toBinaryString(j)).replace(' ', '0');
					return  leftMismatchLocation +  rightMismatchLocation +  dict_index_S;
				}
			}
		}

		return "";
	}

	private static boolean bitOf(char in) {
	    return (in == '1');
	}
	private static char charOf(boolean in) {
	    return (in) ? '1' : '0';
	}


	public static void decompress(){
		String fileName = "compressed.txt";
		String dict = "";
		String format = "";
		String compInst = "";
		// String previous = "";
		int instLength = 0;
		File file = new File(fileName);
		i = 0;
/////////Read from the fileName//////////////////////////
		try{
			compInstAll = new String(Files.readAllBytes(Paths.get(fileName)));
		} catch(IOException e ){
			e.printStackTrace();
		} catch(Exception e){
			e.printStackTrace();
		}
//////////		Implement Dictionary		//////////////
		dict = compInstAll.substring(compInstAll.lastIndexOf("x") + 1).trim();
		String lines[] = dict.split("\\r?\\n");
		for(i=0; i<lines.length; i++){
			dictionaryReverse.put(i, lines[i]);
		}
		i=0;
////Implement Format to Instruction length dictionary/////
		formatToLength.put("000", 2);
		formatToLength.put("001", 12);
		formatToLength.put("010", 8);
		formatToLength.put("011", 8);
		formatToLength.put("100", 13);
		formatToLength.put("101", 3);
		formatToLength.put("110", 32);
		compInstAll = compInstAll.replaceAll("\\r\\n|\\r|\\n", "");
		// System.out.println(compInstAll);
		// System.out.println("aaaaaaaaaaaaaaaaaa");
////Loop through compressed string to find the format/////
		while(compInstAll.charAt(i) != 'x'){
//Get the first 3 bits specifying whether RLE, DM, 1bit, 2bit or more
			format = getFormat();
			// System.out.println(format);
			if(formatToLength.containsKey(format)){
				instLength = formatToLength.get(format);
			} else {
				break;
			}
//get the next bits corresponding to that instruction
			StringBuilder sb = new StringBuilder();
			for(int j=0; j<instLength; j++){
				sb.append(compInstAll.charAt(i++));
			}
			compInst = sb.toString();
			if(format.equals("000")){
				decompInst = runLE(compInst);
			} else if(format.equals("001")){
				decompInst = bitMask(compInst);
			} else if(format.equals("010")){
				decompInst = oneBitMismatch(compInst);
			} else if(format.equals("011")){
				decompInst = twoBitMismatch(compInst);
			} else if(format.equals("100")){
				decompInst = twoBitMismatchAnywhere(compInst);
			} else if(format.equals("101")){
				decompInst = directMatch(compInst);
			} else if(format.equals("110")){
				decompInst = original(compInst);
			}
			// previous = compInst;
			// i++;
		}
		String parsedStr = output.replaceAll("(.{32})", "$1\n").trim();
		try{
			PrintStream fileStream = new PrintStream("dout.txt");
			System.setOut(fileStream);
		} catch(IOException e ){
			e.printStackTrace();
		} catch(Exception e){
			e.printStackTrace();
		}
		System.out.println(parsedStr);
	}

	public static String runLE(String compInst){
		if(compInst.equals("00")){
			output = output + decompInst;
		} else if(compInst.equals("01")){
			output = output + decompInst + decompInst;
		} else if(compInst.equals("10")){
			output = output + decompInst + decompInst + decompInst;
		} else if(compInst.equals("11")){
			output = output + decompInst + decompInst + decompInst + decompInst;
		}
		return decompInst;
	}

	public static String bitMask(String compInst){
		String mismatchLocation = compInst.substring(0,5);
		String bitmask = compInst.substring(5,9);
		String dict_index = compInst.substring(9, compInst.length());
		int dict_index_I = Integer.parseInt(dict_index, 2);
		String instruction = "";
		if(dictionaryReverse.containsKey(dict_index_I)){
			instruction = dictionaryReverse.get(dict_index_I);
		}
		int mismatchLocation_I = Integer.parseInt(mismatchLocation, 2);
		StringBuilder instructionSb = new StringBuilder();
		// System.out.println("mismatchLocation_I = " + mismatchLocation_I);
		// System.out.println("instructionSb = " + instructionSb);
		// System.out.println("instructionSb = " + mismatchLocation_I);		
		// instructionSb.setCharAt(mismatchLocation_I, instructionSb.charAt(mismatchLocation_I)=='0' ? '1':'0');
		String padLeftZeros = String.format("%" + (mismatchLocation_I+4) + "s", bitmask).replace(' ', '0');
		String mask = String.format("%-" + 32 + "s", padLeftZeros ).replace(' ', '0');
		for(int j=0; j<32; j++){
			instructionSb.append(charOf(bitOf(mask.charAt(j)) ^ bitOf(instruction.charAt(j))));
		}
		decompInst = instructionSb.toString();
		output = output + decompInst;
		// System.out.println("bitmask = " + decompInst);
		return decompInst;
	}

	public static String oneBitMismatch(String compInst){
		String mismatchLocation = compInst.substring(0,5);
		String dict_index = compInst.substring(5, compInst.length());
		int dict_index_I = Integer.parseInt(dict_index, 2);
		String instruction = "";
		if(dictionaryReverse.containsKey(dict_index_I)){
			instruction = dictionaryReverse.get(dict_index_I);
		}
		int mismatchLocation_I = Integer.parseInt(mismatchLocation, 2);
		StringBuilder instructionSb = new StringBuilder(instruction);
		// System.out.println("mismatchLocation_I = " + mismatchLocation_I);
		// System.out.println("instructionSb = " + instructionSb);
		// System.out.println("instructionSb = " + mismatchLocation_I);		
		instructionSb.setCharAt(mismatchLocation_I, instructionSb.charAt(mismatchLocation_I)=='0' ? '1':'0');
		decompInst = instructionSb.toString();
		output = output + decompInst;
		// System.out.println("oneBitMismatch = " + decompInst);
		return decompInst;
	}

	public static String twoBitMismatch(String compInst){
		String mismatchLocation = compInst.substring(0,5);
		String dict_index = compInst.substring(5, compInst.length());
		int dict_index_I = Integer.parseInt(dict_index, 2);
		String instruction = "";
		if(dictionaryReverse.containsKey(dict_index_I)){
			instruction = dictionaryReverse.get(dict_index_I);
		}
		int mismatchLocation_I = Integer.parseInt(mismatchLocation, 2);
		StringBuilder instructionSb = new StringBuilder(instruction);
		// System.out.println("mismatchLocation_I = " + mismatchLocation_I);
		// System.out.println("instructionSb = " + instructionSb);
		// System.out.println("instructionSb = " + mismatchLocation_I);		
		instructionSb.setCharAt(mismatchLocation_I, instructionSb.charAt(mismatchLocation_I)=='0' ? '1':'0');
		instructionSb.setCharAt(mismatchLocation_I+1, instructionSb.charAt(mismatchLocation_I+1)=='0' ? '1':'0');
		decompInst = instructionSb.toString();
		output = output + decompInst;
		// System.out.println("twoBitMismatch = " + decompInst);
		return decompInst;
	}

	public static String twoBitMismatchAnywhere(String compInst){
		String mismatchLocationL = compInst.substring(0,5);
		String mismatchLocationR = compInst.substring(5,10);
		String dict_index = compInst.substring(10, compInst.length());
		int dict_index_I = Integer.parseInt(dict_index, 2);
		String instruction = "";
		if(dictionaryReverse.containsKey(dict_index_I)){
			instruction = dictionaryReverse.get(dict_index_I);
		}
		int mismatchLocation_IL = Integer.parseInt(mismatchLocationL, 2);
		int mismatchLocation_IR = Integer.parseInt(mismatchLocationR, 2);
		StringBuilder instructionSb = new StringBuilder(instruction);
		// System.out.println("mismatchLocation_IL = " + mismatchLocation_IL);
		// System.out.println("mismatchLocation_IR = " + mismatchLocation_IR);
		// System.out.println("instructionSb = " + instructionSb);
		// System.out.println("instructionSb = " + mismatchLocation_I);		
		instructionSb.setCharAt(mismatchLocation_IL, instructionSb.charAt(mismatchLocation_IL)=='0' ? '1':'0');
		instructionSb.setCharAt(mismatchLocation_IR, instructionSb.charAt(mismatchLocation_IR)=='0' ? '1':'0');
		decompInst = instructionSb.toString();
		output = output + decompInst;
		// System.out.println("twoBitMismatchAnywhere = " + decompInst);
		return decompInst;
	}

	public static String directMatch(String compInst){
		String dict_index = compInst.substring(0, compInst.length());
		int dict_index_I = Integer.parseInt(dict_index, 2);
		// String instruction = "";
		if(dictionaryReverse.containsKey(dict_index_I)){
			decompInst = dictionaryReverse.get(dict_index_I);
		}
		output = output + decompInst;
		return decompInst;
	}

	public static String original(String compInst){
		decompInst = compInst;
		output = output + decompInst;
		return decompInst;
	}

	public static String getFormat(){
		StringBuilder f = new StringBuilder();
		for(int j=0; j<3; j++){
			f.append(compInstAll.charAt(i++));
		}
		return f.toString();
	}

}