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

	InstructionMemory INM;
	Buffer INB = null;
	Buffer AIB;
	Buffer SIB;
	Buffer PRB = null;
	RegisterFormat REG1;
	RegisterFormat REG2;
	RegisterFormat ADB = null;
	ResultBuffer REB;
	RegisterFile RGF;
	DataMemory DAM;
	Address addrToken;

	public static void main(String[] args){
		System.out.println("Hello World! :)");
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
		// while(decCanFire() || iss1CanFire() || iss2CanFire() || asuCanFire() || mlu1CanFire() || addrCanFire() || mlu2CanFire() || strCanFire() || wrCanFire() || rdCanFire() ){
		while(INM.instList.size() > 0 || INB != null || AIB != null || PRB != null || SIB != null || ADB != null || REB != null ){//|| iss2CanFire() || asuCanFire() || mlu1CanFire() || addrCanFire() || mlu2CanFire() || strCanFire() || wrCanFire() || rdCanFire() ){

			if(INM.instList.size() > 0)
				decRdFire();
			if(INB != null)
				if (INB.opCode.equals("ST")) iss2Fire(); else iss1Fire();
			if(AIB != null)
				if (AIB.opCode.equals("MUL")) mlu1Fire(); else asuFire();
			if(PRB != null)
				mlu2Fire();
			if(SIB != null)
				addrFire();
			if(ADB != null)
				strFire();
			if(REB != null)
				writeFire();
			// System.out.println("here");
		}
	}

	public void decRdFire(){
		Instruction instToken = INM.instList.get(0);
		INM.instList.remove(0);
		String[] srcReg = {instToken.srcOp1, instToken.srcOp2};
		int srcRegVal[] = findRegVal(srcReg);
		INB = new Buffer(instToken.opCode, instToken.desReg, srcRegVal[0], srcRegVal[1]);
		// System.out.println("INB.opCode = " + INB.opCode + " INB.value = " + INB.srcOp1 + " " + INB.srcOp2);
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
	}

	public void iss2Fire(){
		SIB = new Buffer(INB.opCode, INB.desReg, INB.srcOp1, INB.srcOp2);
		INB = null;
	}

	public void asuFire(){
		int sum = AIB.opCode.equals("ADD") ? (AIB.srcOp1 + AIB.srcOp2):(AIB.srcOp1 - AIB.srcOp2); 
		REG1 = new RegisterFormat(AIB.desReg, sum);
		REB.storeResult(REG1);
		AIB = null;
	}

	public void mlu1Fire(){
		PRB = new Buffer(AIB.opCode, AIB.desReg, AIB.srcOp1, AIB.srcOp2);
		AIB = null;
	}

	public void mlu2Fire(){
		int mul = PRB.srcOp1 * PRB.srcOp2;
		REG2 = new RegisterFormat(PRB.desReg, mul);
		REB.storeResult(REG2);
		PRB = null;
	}

	public void addrFire(){
		String[] srcOp = {SIB.srcOp1, "0"};
		String[] srcOps = findRegVal(srcOp);
		int address = Integer.parseInt(srcOps[0]) + SIB.srcOp2;
		ADB = new RegisterFormat(SIB.desReg, address);
		SIB = null;
	}

	public void strFire(){
		String[] regVal = {ADB.register, "0"};
		String[] regVals = findRegVal(srcOp);
		addrToken = new Address(ADB.value, Integer.parseInt(regVals[0]));
		DAM.storeDataMemory(addrToken);
	}

	public void writeFire(){

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
		int index = addList.indexOf(addrToken.value);
		if(index != -1){
			addList.remove(index);
			addList.add(addrToken);
		} else{
			addList.add(addrToken);
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