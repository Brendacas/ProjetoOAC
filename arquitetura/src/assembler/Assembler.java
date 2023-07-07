package assembler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import components.Register;
import architecture.Architecture;

class CommandMethods{
	private final List<String> commands;
	
	CommandMethods(List<String> commands){
		this.commands = commands;
	}
	
	public int processImul(String[] tokens){
		return this.processCommand("imul", tokens);
	}

	public int processInc(String[] tokens){
		return commands.indexOf(tokens[0] + "Reg");
	}

	public int processMove(String[] tokens){
		return this.processCommand("move", tokens);
	}

	public int processAdd(String[] tokens){
		return this.processCommand("add", tokens);
	}

	public int processSub(String[] tokens){
		return this.processCommand("sub", tokens);
	}

	public int processCommand(String command, String[] tokens){
		String param1 = tokens[1];
		String param2 = tokens[2];
		Map<String, String> paramsMap = new HashMap<>();

    paramsMap.put("% %", command + "RegReg");
		paramsMap.put("& %", command + "MemReg");
    paramsMap.put("% &", command + "RegMem");
		if(!command.equals("imul"))
			paramsMap.put("  %", command + "ImmReg");

		String paramKey = "";
		if(Character.isDigit(param1.charAt(0)) || param1.charAt(0) == '-') paramKey += " ";
		else paramKey += String.valueOf(param1.charAt(0));
		paramKey +=  " " + String.valueOf(param2.charAt(0));
		
		int process = commands.indexOf(paramsMap.get(paramKey));
		return process;
	}
}


public class Assembler {
	private List<String> lines;
	private ArrayList<String> objProgram;
	private ArrayList<String> execProgram;
	private Architecture arch;
	private ArrayList<String>commands;	
	private ArrayList<String>labels;
	private ArrayList<Integer> labelsAdresses;
	private ArrayList<String>variables;

	private static final String INPUT_EXTENSION = ".dsf";
	private static CommandMethods commandMethods;
	private static Map<String, Function<Object, Object>> methodMap;
	
	public Assembler() {
		lines = new ArrayList<>();
		labels = new ArrayList<>();
		labelsAdresses = new ArrayList<>();
		variables = new ArrayList<>();
		objProgram = new ArrayList<>();
		execProgram = new ArrayList<>();
		arch = new Architecture();
		commands = arch.getCommandsList();	
		commandMethods = new CommandMethods(commands);
		methodMap = new HashMap<>();
		setMethodMap();
	}
	
	//getters
	
	public ArrayList<String> getObjProgram() {
		return objProgram;
	}
	
	/**
	 * These methods getters and set below are used only for TDD purposes
	 * @param lines
	 */
	
	protected ArrayList<String> getLabels() {
		return labels;
	}
	
	protected ArrayList<Integer> getLabelsAddresses() {
		return labelsAdresses;
	}
	
	protected ArrayList<String> getVariables() {
		return variables;
	}
	
	protected ArrayList<String> getExecProgram() {
		return execProgram;
	}
	
	protected void setLines(ArrayList<String> lines) {
		this.lines = lines;
	}	

	protected void setExecProgram(ArrayList<String> lines) {
		this.execProgram = lines;
	}	
	
	public Architecture getArch() {
		return arch;
	}
	
	/*
	 * An assembly program is always in the following template
	 * <variables>
	 * <commands>
	 * Obs.
	 * 		variables names are always started with alphabetical char
	 * 	 	variables names must contains only alphabetical and numerical chars
	 *      variables names never uses any command name
	 * 		names ended with ":" identifies labels i.e. address in the memory
	 * 		Commands are only that ones known in the architecture. No comments allowed
	 * 	
	 * 		The assembly file must have the extention .dsf
	 * 		The executable file must have the extention .dxf 	
	 */
	


	/**
	 * This method reads an entire file in assembly 
	 * @param filename
	 * @throws IOException 
	 */
	public void read(String filename) throws IOException {
		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(filename + INPUT_EXTENSION))) {
			String line = "";
			while((line = bufferedReader.readLine()) != null)
				this.lines.add(line);
			bufferedReader.close();
		} catch (IOException e) {
			throw new IOException("Error while trying to open the file "+e.getMessage());
		}
	}
	
	/**
	 * This method scans the strings in lines
	 * generating, for each one, the corresponding machine code
	 * @param lines
	 */
	public void parse() {
		System.out.println(this.arch.getRegistersList());
		this.lines.forEach(line -> {
			String[] tokens = line.split(" ");
			if(tokens.length == 1){
        if(tokens[0].endsWith(":")) {
					labels.add(tokens[0].substring(0, tokens[0].length() - 1));
					labelsAdresses.add(objProgram.size());
				}
        else variables.add(tokens[0]);
      }
      else try {
				System.out.println(Integer.toString(findCommandNumber(tokens)));
				objProgram.add(Integer.toString(findCommandNumber(tokens)));
				if(!tokens[1].isEmpty()) 
					if(tokens[0].startsWith("j")) objProgram.add("&" + tokens[1]);
					else objProgram.add(tokens[1]);
				if(!tokens[2].isEmpty()) objProgram.add(tokens[2]);
			} catch (Exception e) {}
		});
	}
	
	private void setMethodMap(){
		methodMap.put("move", obj -> commandMethods.processMove((String[]) obj));
		methodMap.put("add", obj -> commandMethods.processAdd((String[]) obj));
		methodMap.put("sub", obj -> commandMethods.processSub((String[]) obj));
		methodMap.put("imul", obj -> commandMethods.processImul((String[]) obj));
		methodMap.put("inc", obj -> commandMethods.processInc((String[]) obj));
	}

	public Object invoke(String name, Object... args){
		Function<Object, Object> method = methodMap.get(name);
		return method.apply(args);
	}

	private int findCommandNumber(String[] tokens){
		Integer process = this.commands.indexOf(tokens[0]);
		if(process < 0)
			process = (Integer) invoke(tokens[0], (Object[]) tokens);
		return process;
	}
	
	/**
	 * This method creates the executable program from the object program
	 * Step 1: check if all variables and labels mentioned in the object 
	 * program are declared in the source program
	 * Step 2: allocate memory addresses (space), from the end to the begin (stack)
	 * to store variables
	 * Step 3: identify memory positions to the labels
	 * Step 4: make the executable by replacing the labels and the variables by the
	 * corresponding memory addresses 
	 * @param filename 
	 * @throws IOException 
	 */
	public void makeExecutable(String filename) throws IOException {
		if (!checkLabels())
			return;
		execProgram = new ArrayList<String>(objProgram);
		replaceAllVariables();
		replaceLabels(); //replacing all labels by the address they refer to
		replaceRegisters(); //replacing all registers by the register id they refer to
		saveExecFile(filename);
		System.out.println("Compilation has been successfull");
	}

	/**
	 * This method replaces all the registers names by its correspondings ids.
	 * registers names must be prefixed by %
	 */
	protected void replaceRegisters() {
		for (int i = 0; i < execProgram.size(); i++) {
			String line = execProgram.get(i);
			if (line.startsWith("%"))
				execProgram.set(i, Integer.toString(searchRegisterId( line.substring(1), arch.getRegistersList())));	
		}
	}

	/**
	 * This method replaces all variables by their addresses.
	 * The addresses o0f the variables startes in the end of the memory
	 * and decreases (creating a stack)
	 */
	protected void replaceAllVariables() {
		int position = arch.getMemorySize()-1; //starting from the end of the memory

		for (String var : this.variables) { //scanning all variables
			replaceVariables(var, position);
			position--;
		}
	}

	/**
	 * This method saves the execFile collection into the output file
	 * @param filename
	 * @throws IOException 
	 */
	private void saveExecFile(String filename) throws IOException {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File(filename+".dxf")))) {
			execProgram.forEach(line -> {
				try {
					extracted(writer, line);
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
			
			writer.write("-1"); //-1 is a flag indicating that the program is finished
			writer.close();
		} catch (IOException e) {
			throw new IOException("Error while opening " + filename);
		}
	}

	private void extracted(BufferedWriter writer, String line) throws IOException {
		writer.write(line + "\n");
	}

	/**
	 * This method replaces all labels in the execprogram by the corresponding
	 * address they refer to
	 */
	protected void replaceLabels() {
		for(int i = 0; i < this.labels.size(); i++){
			String label = "&" + this.labels.get(i);
			int labelPointTo = this.labelsAdresses.get(i);

			for(int lineNumber = 0; lineNumber < this.execProgram.size(); lineNumber++){
				String line = this.execProgram.get(lineNumber);
				if(line.equals(label))
					this.execProgram.set(lineNumber, Integer.toString(labelPointTo));
			}
		}
	}

	/**
	 * This method replaces all occurences of a variable
	 * name found in the object program by his address
	 * in the executable program
	 * @param var
	 * @param position
	 */
	protected void replaceVariables(String var, int position) {
		var = "&"+var;
	
		for(int i = 0; i < execProgram.size(); i++)
			if(execProgram.get(i).equals(var))
				execProgram.set(i,Integer.toString(position));
	}

	/**
	 * This method checks if all labels and variables in the object program were in the source
	 * program.
	 * The labels and the variables collection are used for this
	 */
	protected boolean checkLabels() {
		System.out.println("Checking labels and variables...");
	
		Iterator<String> itr = this.objProgram.iterator();
		while(itr.hasNext()){
			String line = itr.next();
			if(line.startsWith("&")){
				line = line.substring(1);
				if (!(this.labels.contains(line) || this.variables.contains(line))){
					System.out.println("FATAL ERROR! Variable or label "+line+" not declared!");
					return false;
				}
			}
		}

		return true;
	}
	
	/**
	 * This method searches for a register in the architecture register list
	 * by the register name
	 * @param line
	 * @param registersList
	 * @return
	 */
	private int searchRegisterId(String line, ArrayList<Register> registersList) {
		for (int i = 0; i < registersList.size(); i++) {
			Register r = registersList.get(i);
			if (line.equals(r.getRegisterName())) {
				return i;
			}
		}
		return -1;
	}

	public static void main(String[] args) throws IOException {
		Assembler assembler = new Assembler();
		assembler.read("program");
		System.out.println("Generating the object program...");
		assembler.parse();
		System.out.println("Generating executable: program.dxf...");
		assembler.makeExecutable("program");
	}
		
}
