/*
I have neither given nor received any unauthorized aid on this assignment
Author  :  Akash R Vasishta
UFID    :  53955080
College :  University of Florida
Course  :  CDA 5636 - Embedded Systems 
*/
import java.io.*;
import java.util.*;

public class MIPSsim{
	int i = 0;
	InstructionMemory INM;
	Buffer INB = null;
	Buffer AIB;
	Buffer SIB;
	Buffer PRB = null;
	RegisterFormat REG1;
	RegisterFormat REG2;
	RegisterFormat REG3;
	RegisterFormat ADB = null;
	ResultBuffer REB;
	RegisterFile RGF;
	DataMemory DAM;
	Address addrToken;

	public static void main(String[] args){
		MIPSsim mips = new MIPSsim();
		mips.initializePlaces();
		mips.startSimulation();
	}

	public void initializePlaces(){
		INM = new InstructionMemory();
		INM.storeInstructions();
		RGF = new RegisterFile();
		RGF.storeRegisters();
		DAM = new DataMemory();
		DAM.storeDataMemory();
	}

	public void startSimulation(){
		try{
			PrintStream fileStream = new PrintStream("simulation.txt");
			System.setOut(fileStream);
		} catch(IOException e ){
			e.printStackTrace();
		} catch(Exception e){
			e.printStackTrace();
		}
		System.out.print("STEP " + i++ + ":");
		display();	
		// while(decCanFire() || iss1CanFire() || iss2CanFire() || asuCanFire() || mlu1CanFire() || addrCanFire() || mlu2CanFire() || strCanFire() || wrCanFire() || rdCanFire() ){
		while(INM.instList.size() > 0 || INB != null || AIB != null || PRB != null || SIB != null || ADB != null || REB != null ){//|| iss2CanFire() || asuCanFire() || mlu1CanFire() || addrCanFire() || mlu2CanFire() || strCanFire() || wrCanFire() || rdCanFire() ){
			System.out.print("\nSTEP " + i++ + ":");

			if(REB != null){
				// System.out.println("7");
				writeFire();
				// display();
			}
			if(ADB != null){
				// System.out.println("6");
				strFire();
				// display();
			}
			if(SIB != null){
				// System.out.println("5");
				addrFire();
				// display();
			}
			if(PRB != null){
				// System.out.println("4");
				mlu2Fire();
				// display();
			}
			if(AIB != null){
				// System.out.println("3");
				if (AIB.opCode.equals("MUL")) mlu1Fire(); else asuFire();
				// display();
			}
			if(INB != null){
				// System.out.println("2");
				if (INB.opCode.equals("ST")) iss2Fire(); else iss1Fire();
				// display();
			}
			if(INM.instList.size() > 0){
				// System.out.println("1");
				decRdFire();
				// display();
			}
			// System.out.println("now");
			display();
		}
	}

	public void display(){
		// System.out.print("STEP:" + i);
		System.out.print("\nINM:");
		if(INM.instList.size() > 0) System.out.print("<"+INM.instList.get(0).opCode+","+INM.instList.get(0).desReg+","+INM.instList.get(0).srcOp1+","+INM.instList.get(0).srcOp2+">");
		for(int j=1; j<INM.instList.size(); j++)
			System.out.print(",<"+INM.instList.get(j).opCode+","+INM.instList.get(j).desReg+","+INM.instList.get(j).srcOp1+","+INM.instList.get(j).srcOp2+">");
		// System.out.print("\nINB: \nAIB: \nLIB: \nADB: \nREB: \nRGF: ");// + "<"+INB.opCode+","+INB.desReg+","+INB.srcOp1+","+INB.srcOp2+">");
		// System.out.print("\nAIB: ");// + "<"+INB.opCode+","+INB.desReg+","+INB.srcOp1+","+INB.srcOp2+">");
		// System.out.print("\nLIB: ");
		// System.out.print("\nADB: ");
		// System.out.print("\nREB: ");
		// System.out.print("\nRGF: ");
		System.out.print("\nINB:");
		if(INB != null) System.out.print("<"+INB.opCode+","+INB.desReg+","+INB.srcOp1+","+INB.srcOp2+">");
		System.out.print("\nAIB:");
		if(AIB != null) System.out.print("<"+AIB.opCode+","+AIB.desReg+","+AIB.srcOp1+","+AIB.srcOp2+">");
		System.out.print("\nSIB:");
		if(SIB != null) System.out.print("<"+SIB.opCode+","+SIB.desReg+","+SIB.srcOp1+","+SIB.srcOp2+">");		
		System.out.print("\nPRB:");
		if(PRB != null) System.out.print("<"+PRB.opCode+","+PRB.desReg+","+PRB.srcOp1+","+PRB.srcOp2+">");		
		System.out.print("\nADB:");
		if(ADB != null) System.out.print("<"+ADB.register+","+ADB.value+">");
		System.out.print("\nREB:");
		if(REB != null){
			System.out.print("<"+REB.resList.get(0).register+","+REB.resList.get(0).value+">");
			for(int j=1; j<REB.resList.size(); j++)
				System.out.print(",<"+REB.resList.get(j).register+","+REB.resList.get(j).value+">");
		}
		System.out.print("\nRGF:" + "<"+RGF.regList.get(0).register+","+RGF.regList.get(0).value+">");
		for(int j=1; j<RGF.regList.size(); j++)
			System.out.print(",<"+RGF.regList.get(j).register+","+RGF.regList.get(j).value+">");
		System.out.print("\nDAM:" + "<"+DAM.addList.get(0).address+","+DAM.addList.get(0).value+">");
		for(int j=1; j<DAM.addList.size(); j++)
			System.out.print(",<"+DAM.addList.get(j).address+","+DAM.addList.get(j).value+">");
		System.out.print("\n");
	}

	public void decRdFire(){
		Instruction instToken = INM.instList.get(0);
		INM.instList.remove(0);
		String[] srcReg = {instToken.srcOp1, instToken.srcOp2};
		int srcRegVal[] = findRegVal(srcReg);
		INB = new Buffer(instToken.opCode, instToken.desReg, srcRegVal[0], srcRegVal[1]);
		// System.out.println("INB.opCode = " + INB.opCode + " INB.value = " + INB.srcOp1 + " " + INB.srcOp2);
		// System.out.print("INM: ");
		// for(int j=0; j<INM.instList.size(); j++)
		// 	System.out.print("<"+INM.instList.get(j).opCode+","+INM.instList.get(j).desReg+","+INM.instList.get(j).srcOp1+","+INM.instList.get(j).srcOp2+">");
		// System.out.println("INB: "+ "<"+INB.opCode+","+INB.desReg+","+INB.srcOp1+","+INB.srcOp2+">");
	}

	public int[] findRegVal(String[] srcReg){
		int srcRegVal[] = new int[2];
		for(RegisterFormat index : RGF.regList){
			// System.out.println("index.register = " + index.register + " srcReg[1] = " + srcReg[1]);
			if(index.register.equals(srcReg[0]))
				srcRegVal[0] = index.value;
			if(index.register.equals(srcReg[1]))
				srcRegVal[1] = index.value;
			try{
				srcRegVal[1] = Integer.parseInt(srcReg[1]);
			} catch(NumberFormatException e){
			}
		}
		return srcRegVal;
	}

	public void iss1Fire(){
		AIB = new Buffer(INB.opCode, INB.desReg, INB.srcOp1, INB.srcOp2);
		INB = null;
		// if(INM.instList.size() > 0)
		// 	decRdFire();
		// System.out.println("AIB: "+ "<"+AIB.opCode+","+AIB.desReg+","+AIB.srcOp1+","+AIB.srcOp2+">");
	}

	public void iss2Fire(){
		SIB = new Buffer(INB.opCode, INB.desReg, INB.srcOp1, INB.srcOp2);
		INB = null;
		// if(INM.instList.size() > 0)
		// 	decRdFire();
		// System.out.println("SIB: "+ "<"+SIB.opCode+","+SIB.desReg+","+SIB.srcOp1+","+SIB.srcOp2+">");		
	}

	public void asuFire(){
		int sum = AIB.opCode.equals("ADD") ? (AIB.srcOp1 + AIB.srcOp2):(AIB.srcOp1 - AIB.srcOp2); 
		REG1 = new RegisterFormat(AIB.desReg, sum);
		// System.out.println("REG1.value = " + REG1.value);
		if(REB == null)
			REB = new ResultBuffer();
		REB.storeResult(REG1);
		AIB = null;
		// System.out.print("REB: ");
		// for(int j=0; j<REB.resList.size(); j++)
		// 	System.out.print("<"+REB.resList.get(j).opCode+","+REB.resList.get(j).desReg+","+REB.resList.get(j).srcOp1+","+REB.resList.get(j).srcOp2+">");

	}

	public void mlu1Fire(){
		PRB = new Buffer(AIB.opCode, AIB.desReg, AIB.srcOp1, AIB.srcOp2);
		AIB = null;
	}

	public void mlu2Fire(){
		int mul = PRB.srcOp1 * PRB.srcOp2;
		REG2 = new RegisterFormat(PRB.desReg, mul);
		if(REB == null)
			REB = new ResultBuffer();
		REB.storeResult(REG2);
		PRB = null;
	}

	public void addrFire(){
		// String[] srcOp = {SIB.srcOp1, "0"};
		// String[] srcOps = findRegVal(srcOp);
		// int address = Integer.parseInt(srcOps[0]) + SIB.srcOp2;
		int address = SIB.srcOp1 + SIB.srcOp2;
		ADB = new RegisterFormat(SIB.desReg, address);
		SIB = null;
	}

	public void strFire(){
		String[] regVal = {ADB.register, "0"};
		int[] regVals = findRegVal(regVal);
		addrToken = new Address(ADB.value, regVals[0]);
		DAM.storeDataMemory(addrToken);
		ADB = null;
	}

	public void writeFire(){
		// System.out.println("Inside writeFire");
		// System.out.println("REB.resList.get(0).register = " + REB.resList.get(0).register);
		// System.out.println("here");
		String register = REB.resList.get(0).register;
		int value = REB.resList.get(0).value;
		REG3 = new RegisterFormat(register, value);
		// REG3 = REB.resList.get(0);
		// System.out.println("here1");
		RGF.storeRegisters(REG3);
		// System.out.println("here2");
		REB.resList.remove(0);
		// System.out.print("REB.resList.register = ");
		if(REB.resList.size()==0)
			REB = null;
		// for(int h=0;h<REB.resList.size();h++)
		// 	System.out.print(REB.resList.get(h) + " , ");
	}

/*	public boolean decCanFire(){
		return (INM.instList.size() > 0) ? True:False; 
	}
*/

}

class InstructionMemory{
	String fileName = "instructions.txt";
	String opCode, desReg, srcOp1, srcOp2, instLine;
    List<Instruction> instList = new ArrayList<Instruction>();

	public void storeInstructions(){
		File file = new File(fileName);
		try{
			BufferedReader br = new BufferedReader(new FileReader(file));
			while ((instLine = br.readLine()) != null){
				String eachInst = instLine.substring(instLine.indexOf("<")+1, instLine.indexOf(">"));
				String[] eachIArray = eachInst.split(",");
				opCode = eachIArray[0];
				desReg = eachIArray[1];
				srcOp1 = eachIArray[2];
				srcOp2 = eachIArray[3];
				Instruction instToken  = new Instruction(opCode, desReg, srcOp1, srcOp2);
				instList.add(instToken);
			}
		} catch(IOException e ){
			e.printStackTrace();
		} catch(Exception e){
			e.printStackTrace();
		}
	}
}

class RegisterFile{
	String fileName = "registers.txt";
	String register, regLine;
	int value;
    List<RegisterFormat> regList = new ArrayList<RegisterFormat>();

	public void storeRegisters(){
		File file = new File(fileName);
		try{
			BufferedReader br = new BufferedReader(new FileReader(file));
			while ((regLine = br.readLine()) != null){
				String eachReg = regLine.substring(regLine.indexOf("<")+1, regLine.indexOf(">"));
				String[] eachRArray = eachReg.split(",");
				register = eachRArray[0];
				value = Integer.parseInt(eachRArray[1]);
				RegisterFormat regToken  = new RegisterFormat(register, value);
				regList.add(regToken);
			}
			// System.out.println("RegList[0].opCode = " + regList.get(0).getRegister());
		} catch(IOException e ){
			e.printStackTrace();
		} catch(Exception e){
			e.printStackTrace();
		}
	}

	public void storeRegisters(RegisterFormat regToken){
		// System.out.println("here11");
		// int index = regList.indexOf(regToken.value);
		int index = -1;
		int lessThan= 0; int regInt = 0; int regTokenInt = 0;
		String reg = null;
		regTokenInt = Integer.parseInt(regToken.register.replaceAll("[^0-9]", ""));
		// System.out.println("regTokenInt = " + regTokenInt);
		// System.out.print("regInt = ");
		for(int indexL=0;indexL<regList.size();indexL++){
			reg = regList.get(indexL).register;
			regInt = Integer.parseInt(reg.replaceAll("[^0-9]", ""));
			// System.out.print(" " + regInt);
			if(regTokenInt >= regInt)
				lessThan = indexL;
			if(regToken.register.equals(reg)){
				index = indexL;
				break;
			} else{
				index = -1;
			}			
		}
		if(index != -1){
			// regList.remove(index);
			// regList.add(regToken);
			// System.out.println("index = " + index);
			regList.set(index, regToken);
		} else{
			// regList.add(regToken);
			// System.out.println("lessThan = " + lessThan);
			regList.add(lessThan+1, regToken);
		}
	}

}

class DataMemory{
	String fileName = "datamemory.txt";
	String addLine;
	int address;
	int value;
    List<Address> addList = new ArrayList<Address>();

	public void storeDataMemory(){
		File file = new File(fileName);
		try{
			BufferedReader br = new BufferedReader(new FileReader(file));
			while ((addLine = br.readLine()) != null){
				String eachAdd = addLine.substring(addLine.indexOf("<")+1, addLine.indexOf(">"));
				String[] eachAArray = eachAdd.split(",");
				address = Integer.parseInt(eachAArray[0]);
				value = Integer.parseInt(eachAArray[1]);
				Address addToken  = new Address(address, value);
				addList.add(addToken);
			}
			// System.out.println(instList.get(0).getOpCode());
		} catch(IOException e ){
			e.printStackTrace();
		} catch(Exception e){
			e.printStackTrace();
		}
	}

	public void storeDataMemory(Address addrToken){
		// System.out.println("addrToken.address = " + addrToken.address);
		int index = -1;
		int lessThan= 0; int addrInt = 0; int addrTokenInt = 0;
		int addr = 0;
		addrTokenInt = addrToken.address;

		// for(int h=0;h<addList.size();h++)
		// 	System.out.print(addList.get(h).address + " , ");
		for(int indexL=0;indexL<addList.size();indexL++){
			addr = addList.get(indexL).address;
			addrInt = addr;
			// System.out.print(" " + regInt);
			if(addrTokenInt >= addrInt)
				lessThan = indexL;

			if(addrToken.address == addr){
				index = indexL;
				break;
			} else{
				index = -1;
			}
			
		}
		// int index = addList.indexOf(addrToken.address);
		// System.out.println("index = " + index);
		if(index != -1){
			// addList.remove(index);
			// addList.add(addrToken);
			addList.set(index, addrToken);
		} else{
			// addList.add(addrToken);
			addList.add(lessThan+1, addrToken);
		}
	}

}

class ResultBuffer{
    List<RegisterFormat> resList = new ArrayList<RegisterFormat>();
	public void storeResult(RegisterFormat resToken){
		resList.add(resToken);
	}
}

class Buffer{
	protected String opCode, desReg;
	protected int srcOp1, srcOp2;

	public Buffer(String  opCode, String  desReg, int srcOp1, int srcOp2){
		this.opCode = opCode;
		this.desReg = desReg;
		this.srcOp1 = srcOp1;
		this.srcOp2 = srcOp2;
	}
}

class AddressBuffer{
	protected String register;
	protected int address;

	public AddressBuffer(String register, int address){
		this.register = register;
		this.address = address;
	}

	public String getRegister(){
		return this.register;
	}
}

class Instruction{
	protected String opCode;
	protected String desReg;
	protected String srcOp1;
	protected String srcOp2;

	public Instruction(String opCode, String desReg, String srcOp1, String srcOp2){
		this.opCode = opCode;
		this.desReg = desReg;
		this.srcOp1 = srcOp1;
		this.srcOp2 = srcOp2;
	}

	public String getOpCode(){
		return this.opCode;
	}
}

class RegisterFormat{
	protected String register;
	protected int value;
	public RegisterFormat(String register, int value){
		this.register = register;
		this.value = value;
	}

	public String getRegister(){
		return this.register;
	}
}

class Address{
	protected int address;
	protected int value;

	public Address(int address, int value){
		this.address = address;
		this.value = value;
	}

	public int getAddress(){
		return this.address;
	}
}