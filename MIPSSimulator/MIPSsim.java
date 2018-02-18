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

	public static void main(String[] args){
		System.out.println("Hello World! :)");
		MIPSsim mips = new MIPSsim();
		mips.initializePlaces();		
		mips.startSimulation();
	}

	public void initializePlaces(){
		InstructionMemory INM = new InstructionMemory();
		INM.storeInstructions();
		RegisterFile RGF = new RegisterFile();
		RGF.storeRegisters();
		DataMemory DAM = new DataMemory();
		DAM.storeDataMemory();
	}

	public void startSimulation(){

	}

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
    List<Register> regList = new ArrayList<Register>();

	public void storeRegisters(){
		File file = new File(fileName);
		try{
			BufferedReader br = new BufferedReader(new FileReader(file));
			while ((regLine = br.readLine()) != null){
				String eachReg = regLine.substring(regLine.indexOf("<")+1, regLine.indexOf(">"));
				String[] eachRArray = eachReg.split(",");
				register = eachRArray[0];
				value = Integer.parseInt(eachRArray[1]);
				Register regToken  = new Register(register, value);
				regList.add(regToken);
			}
			// System.out.println(instList.get(0).getOpCode());
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
}

class InstructionBuffer{
	private String opCode, desReg;
	private int srcOp1, srcOp2;

	public InstructionBuffer(String  opCode, String  desReg, int srcOp1, int srcOp2){
		this.opCode = opCode;
		this.desReg = desReg;
		this.srcOp1 = srcOp1;
		this.srcOp2 = srcOp2;
	}
}

class ArithmeticInstructionBuffer{
	private String opCode, desReg;
	private int srcOp1, srcOp2;

	public ArithmeticInstructionBuffer(String  opCode, String  desReg, int srcOp1, int srcOp2){
		this.opCode = opCode;
		this.desReg = desReg;
		this.srcOp1 = srcOp1;
		this.srcOp2 = srcOp2;
	}
}

class StoreInstructionBuffer{
	private String opCode, desReg;
	private int srcOp1, srcOp2;

	public StoreInstructionBuffer(String  opCode, String  desReg, int srcOp1, int srcOp2){
		this.opCode = opCode;
		this.desReg = desReg;
		this.srcOp1 = srcOp1;
		this.srcOp2 = srcOp2;
	}
}

class ResultBuffer{
	private String register;
	private int value;

	public ResultBuffer(String register, int value){
		this.register = register;
		this.value = value;
	}

	public String getRegister(){
		return this.register;
	}
}

class PartialResultBuffer{
	private String opCode, desReg;
	private int srcOp1, srcOp2;

	public PartialResultBuffer(String  opCode, String  desReg, int srcOp1, int srcOp2){
		this.opCode = opCode;
		this.desReg = desReg;
		this.srcOp1 = srcOp1;
		this.srcOp2 = srcOp2;
	}
}

class AddressBuffer{
	private String register;
	private int address;

	public AddressBuffer(String register, int address){
		this.register = register;
		this.address = address;
	}

	public String getRegister(){
		return this.register;
	}
}

class Instruction{
	private String opCode;
	private String desReg;
	private String srcOp1;
	private String srcOp2;

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

class Register{
	private String register;
	private int value;

	public Register(String register, int value){
		this.register = register;
		this.value = value;
	}

	public String getRegister(){
		return this.register;
	}
}

class Address{
	private int address;
	private int value;

	public Address(int address, int value){
		this.address = address;
		this.value = value;
	}

	public int getAddress(){
		return this.address;
	}
}