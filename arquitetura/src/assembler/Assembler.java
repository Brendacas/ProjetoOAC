package assembler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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
	
	// TODO INC
	// TODO CONDITIONALS

	public int processImul(String[] tokens){
		return this.processCommand("imul", tokens);
	}

	public int processInc(String[] tokens){
		throw new RuntimeException("Not implemted");
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
		if(Character.isDigit(param1.charAt(0))) paramKey += " ";
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
			System.out.println(this.lines);
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
		this.lines.forEach(line -> {
			String[] tokens = line.split(" ");
			if(tokens.length == 1){
        if(tokens[0].endsWith(":")) {
					labels.add(tokens[0].substring(0, tokens[0].length() - 1));
					labelsAdresses.add(objProgram.size());
				}
        else variables.add(tokens[0]);
      }
      else System.out.println(findCommandNumber1(tokens));
			// ! MIssing conditionals
		});
		System.exit(1);
	}



	/**
	 * This method processes a command, putting it and its parameters (if they have)
	 * into the final array
	 * @param tokens
	 */
	protected void proccessCommand(String[] tokens) {
		String command = tokens[0];
		String parameter ="";
		String parameter2 = "";
		String parameter3 = "";
		int commandNumber = findCommandNumber(tokens);
		if (commandNumber == 0) { //must to proccess an addRegReg command
			parameter = tokens[1];
			parameter2 = tokens[2]; 
		}
		if (commandNumber == 1) { //must to proccess an addMemReg command
			parameter = "&" + tokens[1];
			parameter2 = tokens[2];
		}
		if (commandNumber == 2) { //must to proccess an addRegMem command
			parameter = tokens[1];
			parameter2 = "&" + tokens[2];
		}
		if (commandNumber == 3) { //must to proccess an addImmMem command
			parameter = tokens[1];
			parameter2 = "&" + tokens[2];
		}
		if (commandNumber == 4) { //must to proccess an subRegReg command
			parameter = tokens[1];
			parameter2 = tokens[2];
		}
		if (commandNumber == 5) { //must to proccess an subMemReg command
			parameter = "&" + tokens[1];
			parameter2 = tokens[2];
		}
		if (commandNumber == 6) { //must to proccess an subRegMem command
			parameter = tokens[1];
			parameter2 = "&" + tokens[2];
		}
		if (commandNumber == 7) { //must to proccess an subImmMem command
			parameter = tokens[1];
			parameter2 = "&" + tokens[2];
		}
		if (commandNumber == 8) { //must to proccess an imulMemReg command
			parameter = "&" + tokens[1];
			parameter2 = tokens[2];
		}
		if (commandNumber == 9) { //must to proccess an imulRegMem command
			parameter = tokens[1];
			parameter2 = "&" + tokens[2];
		}
		if (commandNumber == 10) { //must to proccess an imulRegReg command
			parameter = tokens[1];
			parameter2 = tokens[2];
		}
		if (commandNumber == 11) { //must to proccess an moveMemReg command
			parameter = tokens[1];
			parameter2 = tokens[2];
		}
		if (commandNumber == 12) { //must to proccess an moveRegMem command
			parameter = tokens[1];
			parameter2 = "&" + tokens[2];
		}
		if (commandNumber == 13) { //must to proccess an moveRegReg command
			parameter = tokens[1];
			parameter2 = tokens[2];
		}
		if (commandNumber == 14) { //must to proccess an moveImmReg command
			parameter = tokens[1];
			parameter2 = tokens[2];
		}
		if (commandNumber == 15) { //must to proccess an incReg command
			parameter = tokens[1];
		}
		if (commandNumber == 16) { //must to proccess an incMem command
			parameter = tokens[1];
			parameter = "&"+parameter;//this is a flag to indicate that is a position in memory
		}
		if (commandNumber == 17) { //must to proccess an jmp command
			parameter = tokens[1];
			parameter = "&"+parameter;//this is a flag to indicate that is a position in memory
		}
		if (commandNumber == 18) { //must to proccess an jn command
			parameter = tokens[1];
			parameter = "&"+parameter;//this is a flag to indicate that is a position in memory
		}
		if (commandNumber == 19) { //must to proccess an jz command
			parameter = tokens[1];
			parameter = "&"+parameter;//this is a flag to indicate that is a position in memory
		}
		if (commandNumber == 20) { //must to proccess an jnz command
			parameter = tokens[1];
			parameter = "&"+parameter;//this is a flag to indicate that is a position in memory
		}
		if (commandNumber == 21) { //must to proccess an jeq command
			parameter = tokens[1];
			parameter2 = tokens[2];
			parameter3 = tokens[3];
			parameter3 = "&" + parameter3;
		}
		if (commandNumber == 22) { //must to proccess an jgt command
			parameter = tokens[1];
			parameter2 = tokens[2];
			parameter3 = tokens[3];
			parameter3 = "&" + parameter3;
		}
		if (commandNumber == 23) { //must to proccess an jlw command
			parameter = tokens[1];
			parameter2 = tokens[2];
			parameter3 = tokens[3];
			parameter3 = "&" + parameter3;
		}
		if (commandNumber == 24) { //must to proccess an ldi command
			parameter = tokens[1];
			parameter2 = tokens[2];
		}
		if (commandNumber == 25) { //must to proccess an read command
			parameter = tokens[1];
			parameter = "&" + parameter;
			parameter2 = tokens[2];
		}
		if (commandNumber == 26) { //must to proccess an store command
			parameter = tokens[1];
			parameter2 = tokens[2];
			parameter2 = "&" + parameter2;
		}
		objProgram.add(Integer.toString(commandNumber));
		if (!parameter.isEmpty()) {
			objProgram.add(parameter);
		}
		if (!parameter2.isEmpty()) {
			objProgram.add(parameter2);
		}
		if (!parameter3.isEmpty()) {
			objProgram.add(parameter3);
		}
	}
	

	/**
	 * This method uses the tokens to search a command
	 * in the commands list and returns its id.
	 * Some commands (as move) can have multiple formats (reg reg, mem reg, reg mem) and
	 * multiple ids, one for each format.
	 * @param tokens
	 * @return
	 */
	private int findCommandNumber(String[] tokens) {
		int p = commands.indexOf(tokens[0]);
		if (p<0){ //the command isn't in the list. So it must have multiple formats
			if ("move".equals(tokens[0])) //the command is a move
				p = proccessMove(tokens);
			if("add".equals(tokens[0]))
				p = proccessAdd(tokens);
			if("imul".equals(tokens[0]))
				p = proccessImul(tokens);
			if("sub".equals(tokens[0]))
				p = proccessSub(tokens);
			if("inc".equals(tokens[0]))
				p = proccessInc(tokens);
		}
		return p;
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

	private int findCommandNumber1(String[] tokens){
		Integer process = this.commands.indexOf(tokens[0]);
		// ? Maybe the second arg for invoke should be the array of tokens but the fisrt tokens[0]
		if(process < 0)
			process = (Integer) invoke(tokens[0], (Object[]) tokens);
		return process;
	}
	/**
	 * This method proccess a move command.
	 * It must have differents formats, meaning differents internal commands
	 * @param tokens
	 * @return
	 */
	private int proccessMove(String[] tokens) {
		String p1 = tokens[1];
		String p2 = tokens[2];
		int p=-1;
		if ((p1.startsWith("%"))&&(p2.startsWith("%"))) { //this is a moveRegReg comand
			p = commands.indexOf("moveRegReg");
		}else { 
			if((p1.startsWith("&"))&& (p2.startsWith("%"))) { //this is a moveMemReg comand
				p = commands.indexOf("moveMemReg");
			}else {
				if (p1.startsWith("%")) { //this is a moveRegMem comand
					p2 = "&" + p2;
					p = commands.indexOf("moveRegMem");
				}else {
					p2 = "&" + p2;
					p = commands.indexOf("moveImmReg");
				}
			}
		}
		return p;
	}
	
	private int proccessAdd(String[] tokens) {
		String p1 = tokens[1];
		String p2 = tokens[2];
		int p=-1;
		if ((p1.startsWith("%"))&&(p2.startsWith("%"))) { //this is a addRegReg comand
			p = commands.indexOf("addRegReg");
		}else {
			if (p2.startsWith("%")) { //this is a addMemReg comand
				p1 = "&" + p1;
				p = commands.indexOf("addMemReg");
			}else {
				if (p1.startsWith("%")) { //this is a addRegMem comand
					p2 = "&" + p2;
					p = commands.indexOf("addRegMem");
				}else {
					p = commands.indexOf("addImmMem");
				}
			}
		}
		return p;
	}
	
	private int proccessSub(String[] tokens) {
		String p1 = tokens[1];
		String p2 = tokens[2];
		int p=-1;
		if ((p1.startsWith("%"))&&(p2.startsWith("%"))) { //this is a addRegReg comand
			p = commands.indexOf("subRegReg");
		}else {
			if (p2.startsWith("%")) { //this is a subMemReg comand
				p1 = "&" + p1;
				p = commands.indexOf("subMemReg");
			}else {
				if (p1.startsWith("%")) { //this is a subRegMem comand
					p2 = "&" + p2;
					p = commands.indexOf("subRegMem");
				}else { //this is a subImmMem comand
					p = commands.indexOf("subImmMem");	
				}
			}
		}
		return p;
	}
	private int proccessImul(String[] tokens) {
		String p1 = tokens[1];
		String p2 = tokens[2];
		int p=-1;
		if ((p1.startsWith("%"))&&(p2.startsWith("%"))) { //this is a imulRegReg comand
			p = commands.indexOf("imulRegReg");
		}else {
			if (p2.startsWith("%")) { //this is a imulMemReg comand
				p1 = "&" + p1;
				p = commands.indexOf("imulMemReg");
			}else {
				if (p1.startsWith("%")) { //this is a imulRegMem comand
					p2 = "&" + p2;
					p = commands.indexOf("imulRegMem");
				}
			}
		}
		return p;
	}
	
	private int proccessInc(String[] tokens) {
		String p1 = tokens[1];
		int p=-1;
		if (p1.startsWith("%")) { //this is a addRegReg comand
			p = commands.indexOf("incReg");
		}else {
			p1 = "&" + p1;
			p = commands.indexOf("incMem");
		}
		return p;
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
		execProgram = (ArrayList<String>) objProgram.clone();
		replaceAllVariables();
		replaceLabels(); //replacing all labels by the address they refer to
		replaceRegisters(); //replacing all registers by the register id they refer to
		saveExecFile(filename);
		System.out.println("Finished");
	}

	/**
	 * This method replaces all the registers names by its correspondings ids.
	 * registers names must be prefixed by %
	 */
	protected void replaceRegisters() {
		int p=0;
		for (String line:execProgram) {
			if (line.startsWith("%")){ //this line is a register
				line = line.substring(1, line.length());
				int regId = searchRegisterId(line, arch.getRegistersList());
				String newLine = Integer.toString(regId);
				execProgram.set(p, newLine);
			}
			p++;
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
			replaceVariable(var, position);
			position --;
		}
	}

	/**
	 * This method saves the execFile collection into the output file
	 * @param filename
	 * @throws IOException 
	 */
	private void saveExecFile(String filename) throws IOException {
		File file = new File(filename+".dxf");
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		for (String l : execProgram)
			writer.write(l+"\n");
		writer.write("-1"); //-1 is a flag indicating that the program is finished
		writer.close();
		
	}

	/**
	 * This method replaces all labels in the execprogram by the corresponding
	 * address they refer to
	 */
	protected void replaceLabels() {
		int i=0;
		for (String label : labels) { //searching all labels
			label = "&"+label;
			int labelPointTo = labelsAdresses.get(i);
			int lineNumber = 0;
			for (String l : execProgram) {
				if (l.equals(label)) {//this label must be replaced by the address
					String newLine = Integer.toString(labelPointTo); // the address
					execProgram.set(lineNumber, newLine);
				}
				lineNumber++;
			}
			i++;
		}
		
	}

	/**
	 * This method replaces all occurences of a variable
	 * name found in the object program by his address
	 * in the executable program
	 * @param var
	 * @param position
	 */
	protected void replaceVariable(String var, int position) {
		var = "&"+var;
		int i=0;
		for (String s:execProgram) {
			if (s.equals(var)) {
				s = Integer.toString(position);
				execProgram.set(i, s);
			}
			i++;
		}
	}

	/**
	 * This method checks if all labels and variables in the object program were in the source
	 * program.
	 * The labels and the variables collection are used for this
	 */
	protected boolean checkLabels() {
		System.out.println("Checking labels and variables");
		for (String line:objProgram) {
			boolean found = false;
			if (line.startsWith("&")) { //if starts with "&", it is a label or a variable
				line = line.substring(1, line.length());
				if (labels.contains(line))
					found = true;
				if (variables.contains(line))
					found = true;
				if (!found) {
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
		int i=0;
		for (Register r:registersList) {
			if (line.equals(r.getRegisterName())) {
				return i;
			}
			i++;
		}
		return -1;
	}

	public static void main(String[] args) throws IOException {
		//String filename = args[0];
		Assembler assembler = new Assembler();
		//System.out.println("Reading source assembler file: "+filename+".dsf");
		assembler.read("program");
		//System.out.println("Generating the object program");
		assembler.parse();
		System.out.println("Generating executable: program.dxf");
		assembler.makeExecutable("program");
	}
		
}
