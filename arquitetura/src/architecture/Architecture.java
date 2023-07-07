package architecture;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import components.Bus;
import components.Memory;
import components.Register;
import components.Ula;

public class Architecture {

  private boolean simulation; // this boolean indicates if the execution is done in simulation mode.
                              // simulation mode shows the components' status after each instruction

  private boolean halt;
  private Bus extbus1;
  private Bus intbus1;
  private Bus intbus2;
  private Memory memory;
  private int memorySize;
  private Register PC;
  private Register IR;
  private Register RPG;
  private Register RPG1;
	private Register RPG2;
  private Register RPG3;

  private Register Flags;
  private Ula ula;
  private Bus demux; // only for multiple register purposes

  private ArrayList<String> commandsList;
  private ArrayList<Register> registersList;

  /**
   * Instanciates all components in this architecture
   */
  private void componentsInstances() {
    // don't forget the instantiation order
    // buses -> registers -> ula -> memory
    extbus1 = new Bus();
    intbus1 = new Bus();
    intbus2 = new Bus();
    PC = new Register("PC", extbus1, intbus2);
    IR = new Register("IR", extbus1, intbus2);
    RPG = new Register("RPG0", extbus1, intbus1);
    RPG1 = new Register("RPG1", extbus1, intbus1);
		RPG2 = new Register("RPG2", extbus1, intbus1);
    RPG3 = new Register("RPG3", extbus1, intbus1);
    Flags = new Register(2, intbus2);
    fillRegistersList();
    ula = new Ula(intbus1, intbus2);
    memorySize = 128;
    memory = new Memory(memorySize, extbus1);
    demux = new Bus(); // this bus is used only for multiple register operations

    fillCommandsList();
  }

  /**
   * This method fills the registers list inserting into them all the registers we
   * have.
   * IMPORTANT!
   * The first register to be inserted must be the default RPG
   */
  private void fillRegistersList() {
    registersList = new ArrayList<Register>();
    registersList.add(RPG);
    registersList.add(RPG1);
		registersList.add(RPG2);
    registersList.add(RPG3);
    registersList.add(PC);
    registersList.add(IR);
    registersList.add(Flags);
  }

  /**
   * Constructor that instanciates all components according the architecture
   * diagram
   */
  public Architecture() {
    componentsInstances();

    // by default, the execution method is never simulation mode
    simulation = false;
  }

  public Architecture(boolean sim) {
    componentsInstances();

    // in this constructor we can set the simoualtion mode on or off
    simulation = sim;
  }

  // getters

  protected Bus getExtbus1() {
    return extbus1;
  }

  protected Bus getIntbus1() {
    return intbus1;
  }

  protected Bus getIntbus2() {
    return intbus2;
  }

  protected Memory getMemory() {
    return memory;
  }

  protected Register getPC() {
    return PC;
  }

  protected Register getIR() {
    return IR;
  }

  protected Register getRPG() {
    return RPG;
  }

  protected Register getFlags() {
    return Flags;
  }

  protected Ula getUla() {
    return ula;
  }

  public ArrayList<String> getCommandsList() {
    return commandsList;
  }

  // all the microprograms must be impemented here
  // the instructions table is
  /*
   *
   * add addr (rpg <- rpg + addr)
   * sub addr (rpg <- rpg - addr)
   * jmp addr (pc <- addr)
   * jz addr (se bitZero pc <- addr)
   * jn addr (se bitneg pc <- addr)
   * read addr (rpg <- addr)
   * store addr (addr <- rpg)
   * ldi x (rpg <- x. x must be an integer)
   * inc (rpg++)
   * move regA regB (regA <- regB)
   */

  protected void fillCommandsList() {
    commandsList = new ArrayList<String>();

    commandsList.add("addRegReg"); // 0
    commandsList.add("addMemReg"); // 1
    commandsList.add("addRegMem"); // 2
    commandsList.add("addImmReg"); // 3
    commandsList.add("subRegReg"); // 4
    commandsList.add("subMemReg"); // 5
    commandsList.add("subRegMem"); // 6
    commandsList.add("subImmReg"); // 7
    commandsList.add("imulMemReg"); // 8
    commandsList.add("imulRegMem"); // 9
    commandsList.add("imulRegReg"); // 10
    commandsList.add("moveMemReg"); // 11
    commandsList.add("moveRegMem"); // 12
    commandsList.add("moveRegReg"); // 13
    commandsList.add("moveImmReg"); // 14
    commandsList.add("incReg"); // 15
    commandsList.add("jmp"); // 16
    commandsList.add("jn"); // 17
    commandsList.add("jz"); // 18
    commandsList.add("jeq"); // 19
    commandsList.add("jneq"); // 20
    commandsList.add("jgt"); // 21
    commandsList.add("jlw"); // 22
  }

  /**
   * This method is used after some ULA operations, setting the flags bits
   * according the result.
   * 
   * @param result is the result of the operation
   *               NOT TESTED!!!!!!!
   */
  private void setStatusFlags(int result) {
    Flags.setBit(0, 0);
    Flags.setBit(1, 0);
    if (result == 0) { // bit 0 in flags must be 1 in this case
      Flags.setBit(0, 1);
    }
    if (result < 0) { // bit 1 in flags must be 1 in this case
      Flags.setBit(1, 1);
    }
  }

  public void addRegReg() {
    PC.internalRead();// pc passa o endere�o da instru��o no intbus2
    ula.internalStore(1);// ula consome endere�o do intbus2 e guarda em seu reg1
    ula.inc();// ula incrementa pra pegar o endere�o do par�metro
    ula.internalRead(1);// ula escreve endere�o do par�metro(Reg) no intbus2
    PC.internalStore();// pc pega o endere�o do par�metro regA e guarda
    PC.read();// pc cospe no barramento o endere�o do regA
    memory.read();// copspe no extbus1 o que
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    PC.internalStore();
    demux.put(extbus1.get());
    registersInternalRead();
    ula.store(0);
    PC.read();
    memory.read();
    demux.put(extbus1.get());
    registersInternalRead();
    ula.store(1);
    ula.add();
    ula.internalRead(1);
    setStatusFlags(intbus2.get());
    registersInternalStore();
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    PC.internalStore();
  }

  public void addMemReg() {
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    PC.internalStore(); // now PC points to the first parameter (position in memory)
    PC.read();
    memory.read();
    memory.read();
    IR.store();
    ula.inc();
    ula.internalRead(1);
    PC.internalStore(); // now PC points to the second parameter (the reg id)
    PC.read();
    memory.read();
    demux.put(extbus1.get()); // points to the correct register
    registersInternalRead(); // starts the read from the register identified into demux bus
    ula.store(1);
    IR.internalRead();
    ula.internalStore(0);
    ula.add();
    ula.read(1);
    IR.internalRead();
    setStatusFlags(intbus2.get());
    registersInternalStore(); // performs an internal store for the register identified into demux bus
    PC.internalRead(); // we need to make PC points to the next instruction address
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    PC.internalStore(); // now PC points to the next instruction. We go back to the FETCH status.
  }

  public void addRegMem() {
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    PC.internalStore(); // now PC points to the first parameter (the reg id)
    PC.read();
    memory.read(); // the register id is now in the external bus.
    ula.inc();
    ula.internalRead(1);
    PC.internalStore(); // now PC points to the second parameter
    demux.put(extbus1.get()); // points to the correct register
    registersInternalRead(); // starts the read from the register identified into demux bus
    ula.store(0);
    PC.read();
    memory.read();
    memory.store();
    memory.read();
    IR.store();
    IR.internalRead();
    ula.internalStore(1);
    ula.add();
    ula.internalRead(1);
    setStatusFlags(intbus2.get());
    IR.internalStore();
    IR.read();
    memory.store();
    PC.internalRead(); // we need to make PC points to the next instruction address
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    PC.internalStore(); // now PC points to the next instruction. We go back to the FETCH status.
  }

  public void addImmReg() {

    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    PC.internalStore(); // pc aponta para endere�o de Imm

    PC.read(); // pc escreve endere�o de Imm em extbus1
    memory.read(); // memoria le o endere�o e escreve o dado (Imm)
    IR.store(); // ir armazena o dado de Imm
    IR.internalRead(); // ir escreve em intbus2
    ula.internalStore(0); // ula armazena em reg0 o valor de intbus2
    ula.inc(); // ula ++
    ula.internalRead(1);
    PC.internalStore(); // pc aponta para o endere�o do registrador
    PC.read(); // pc escreve em extbus1
    memory.read(); // memoria le e escreve o id do registrador
    demux.put(extbus1.get()); // aponta para o registrador correto
    registersInternalRead(); // registrador selecionado escreve em intbus1
    ula.store(1); // ula armazena o dado de intbus1 no reg1
    ula.add(); // ula faz a soma de reg0+reg1 e armazena em reg1
    ula.internalRead(1); // ula escreve o resultado em intbus2
    setStatusFlags(intbus2.get()); // atualiza flags
    ula.read(1); // ula escreve o resultado em intbus1
    registersInternalStore(); // registrador selecionado armazena o dado de intbus1
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    PC.internalStore(); // pc aponta para proxima instru��o
  }

  public void subRegReg() {
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    PC.internalStore(); // now PC points to the first parameter (the first reg id)
    PC.read();
    memory.read(); // the first register id is now in the external bus.
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    PC.internalStore(); // now PC points to the second parameter (the second reg id)
    demux.put(extbus1.get()); // points to the correct register
    registersInternalRead(); // starts the read from the register identified into demux bus
    ula.store(0);
    PC.read();
    memory.read(); // the second register id is now in the external bus.
    demux.put(extbus1.get());// points to the correct register
    registersInternalRead();
    ula.store(1);
    ula.sub();
    ula.internalRead(1);
    setStatusFlags(intbus2.get());
    ula.read(1);
    registersInternalStore(); // performs an internal store for the register identified into demux bus
    PC.internalRead(); // we need to make PC points to the next instruction address
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    PC.internalStore(); // now PC points to the next instruction. We go back to the FETCH status.
  }

  public void subMemReg() {
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    PC.internalStore(); // now PC points to the first parameter (the first reg id)
    PC.read();
    memory.read();
    memory.read();
    IR.store();
    ula.inc();
    ula.internalRead(1);
    PC.internalStore(); // now PC points to the second parameter (the second reg id)
    PC.read();
    memory.read();
    demux.put(extbus1.get()); // points to the correct register
    IR.internalRead();
    ula.internalStore(0);
    registersInternalRead(); // starts the read from the register identified into demux bus
    ula.store(1);
    ula.sub();
    ula.internalRead(1);
    setStatusFlags(intbus2.get());
    ula.read(1);
    registersInternalStore(); // performs an internal store for the register identified into demux bus
    PC.internalRead(); // we need to make PC points to the next instruction address
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    PC.internalStore(); // now//now PC points to the next instruction. We go back to the FETCH status.
  }

  public void subRegMem() {
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    PC.internalStore(); // now PC points to the first parameter (the reg id)
    PC.read();
    memory.read(); // the register id is now in the external bus.
    ula.inc();
    ula.internalRead(1);
    PC.internalStore(); // now PC points to the second parameter (the second reg id)
    demux.put(extbus1.get()); // points to the correct register
    registersInternalRead(); // starts the read from the register identified into demux bus
    ula.store(0);
    PC.read();
    memory.read();
    memory.store();
    memory.read();
    IR.store();
    IR.internalRead();
    ula.internalStore(1);
    ula.sub();
    ula.internalRead(1);
    setStatusFlags(intbus2.get());
    IR.internalStore();
    IR.read();
    memory.store();
    PC.internalRead(); // we need to make PC points to the next instruction address
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    PC.internalStore(); // now PC points to the next instruction. We go back to the FETCH status.
  }

  public void subImmReg() {

    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    PC.internalStore(); // pc aponta para endere�o de Imm

    PC.read(); // pc escreve endere�o de Imm em extbus1
    memory.read(); // memoria le o endere�o e escreve o dado (Imm)
    IR.store(); // ir armazena o dado de Imm
    IR.internalRead(); // ir escreve em intbus2
    ula.internalStore(0); // ula armazena em reg0 o valor de intbus2
    ula.inc(); // ula ++
    ula.internalRead(1);
    PC.internalStore(); // pc aponta para o endere�o do registrador
    PC.read(); // pc escreve em extbus1
    memory.read(); // memoria le e escreve o id do registrador
    demux.put(extbus1.get()); // aponta para o registrador correto
    registersInternalRead(); // registrador selecionado escreve em intbus1
    ula.store(1); // ula armazena o dado de intbus1 no reg1
    ula.sub(); // ula faz a soma de reg0+reg1 e armazena em reg1
    ula.internalRead(1); // ula escreve o resultado em intbus2
    setStatusFlags(intbus2.get()); // atualiza flags
    ula.read(1); // ula escreve o resultado em intbus1
    registersInternalStore(); // registrador selecionado armazena o dado de intbus1
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    PC.internalStore(); // pc aponta para proxima instru��o
  }

  public void imulMemReg() {
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    PC.internalStore();
    PC.read();
    memory.read();
    memory.read();
    IR.store();
    ula.inc();
    ula.internalRead(1);
    PC.internalStore();
    PC.read();
    memory.read();
    demux.put(extbus1.get());
    IR.internalRead();
    ula.internalStore(1);
    registersInternalRead();
    if (intbus1.get() > 0) {
      for (int i = intbus1.get(); i > 1; i--) {
        ula.internalStore(0);
        ula.add();
      }
      ula.read(1);
      registersInternalStore();
    } else {
      if (intbus1.get() < 0) {
        for (int i = intbus1.get(); i < 1; i++) {
          ula.read(1);
          ula.store(0);
          ula.internalStore(1);
          ula.sub();
        }
        ula.read(1);
        registersInternalStore();
      } else {
        if (intbus1.get() == 0) {
          registersInternalStore();
        }
      }
    }

    setStatusFlags(intbus1.get());
    PC.internalRead(); // we need to make PC points to the next instruction address
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    PC.internalStore(); // now PC points to the next instruction. We go back to the FETCH status.
  }

  public void imulRegMem() {
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    PC.internalStore();
    PC.read();
    memory.read();
    demux.put(extbus1.get());
    ula.inc();
    ula.internalRead(1);
    PC.internalStore();
    PC.read();
    memory.read();
    IR.store();
    int aux = getMemorySize() - 1;
    extbus1.put(aux);
    memory.read();
    while (extbus1.get() != 0) {
      aux -= 1;
      extbus1.put(aux);
      memory.read();
    }
    extbus1.put(aux);
    memory.store();
    IR.read();
    memory.store();
    memory.read();
    IR.store();
    IR.internalRead();
    ula.internalStore(1);
    registersInternalRead();
    if (intbus1.get() > 0) {
      for (int i = intbus1.get(); i > 1; i--) {
        ula.internalStore(0);
        ula.add();
      }
      ula.internalRead(1);
      IR.internalStore();
      extbus1.put(aux);
      memory.read();
      memory.store();
      IR.read();
      memory.store();
    } else {
      if (intbus1.get() < 0) {
        for (int i = intbus1.get(); i < 1; i++) {
          ula.read(1);
          ula.store(0);
          ula.internalStore(1);
          ula.sub();
        }
        ula.internalRead(1);
        IR.internalStore();
        extbus1.put(aux);
        memory.read();
        memory.store();
        IR.read();
        memory.store();
      } else {
        if (intbus1.get() == 0) {
          extbus1.put(aux);
          memory.read();
          memory.store();
          extbus1.put(intbus1.get());
          memory.store();
        }
      }
    }

    setStatusFlags(extbus1.get());
    PC.internalRead(); // we need to make PC points to the next instruction address
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    PC.internalStore(); // now PC points to the next instruction. We go back to the FETCH status.
  }

  public void imulRegReg() {
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    PC.internalStore();
    PC.read();
    memory.read();
    demux.put(extbus1.get());
    registersRead();
    IR.store();
    ula.inc();
    ula.internalRead(1);
    PC.internalStore();
    PC.read();
    memory.read();
    demux.put(extbus1.get());
    IR.internalRead();
    ula.internalStore(1);
    registersInternalRead();
    if (intbus1.get() > 0) {
      for (int i = intbus1.get(); i > 1; i--) {
        ula.internalStore(0);
        ula.add();
      }
      ula.read(1);
      registersInternalStore();
    } else {
      if (intbus1.get() < 0) {
        for (int i = intbus1.get(); i < 1; i++) {
          ula.read(1);
          ula.store(0);
          ula.internalStore(1);
          ula.sub();
        }
        ula.read(1);
        registersInternalStore();

      } else {
        if (intbus1.get() == 0) {
          registersInternalStore();
        }
      }
    }

    setStatusFlags(intbus1.get());
    PC.internalRead(); // we need to make PC points to the next instruction address
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    PC.internalStore(); // now PC points to the next instruction. We go back to the FETCH status.
  }

  public void moveRegReg() {
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    PC.internalStore(); // now PC points to the first parameter (the first reg id)
    PC.read();
    memory.read(); // the first register id is now in the external bus.
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    PC.internalStore(); // now PC points to the second parameter (the second reg id)
    demux.put(extbus1.get()); // points to the correct register
    registersInternalRead(); // starts the read from the register identified into demux bus
    PC.read();
    memory.read(); // the second register id is now in the external bus.
    demux.put(extbus1.get());// points to the correct register
    registersInternalStore(); // performs an internal store for the register identified into demux bus
    PC.internalRead(); // we need to make PC points to the next instruction address
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    PC.internalStore(); // now PC points to the next instruction. We go back to the FETCH status.
  }

  public void moveMemReg() {
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    PC.internalStore();
    PC.read();
    memory.read();
    memory.read();
    IR.store();
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    PC.internalStore(); // now PC points to the second parameter (the reg id)
    PC.read();
    memory.read();
    demux.put(extbus1.get()); // points to the correct register
    IR.read();
    registersStore(); // performs an internal store for the register identified into demux bus
    PC.internalRead(); // we need to make PC points to the next instruction address
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    PC.internalStore(); // now PC points to the next instruction. We go back to the FETCH status.
  }

  public void moveRegMem() {
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    PC.internalStore();
    PC.read();
    memory.read();
    demux.put(extbus1.get()); // points to the correct register
    ula.inc();
    ula.internalRead(1);
    PC.internalStore(); // now PC points to the second parameter (the reg id)
    PC.read();
    memory.read();
    memory.store();
    registersRead();
    memory.store();
    PC.internalRead(); // we need to make PC points to the next instruction address
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    PC.internalStore(); // now PC points to the next instruction. We go back to the FETCH status.
  }

  public void moveImmReg() {
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    PC.internalStore();
    PC.read();
    memory.read();
    IR.store();
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    PC.internalStore(); // now PC points to the second parameter (the reg id)
    PC.read();
    memory.read();
    demux.put(extbus1.get()); // points to the correct register
    IR.read();
    registersStore(); // performs an internal store for the register identified into demux bus
    PC.internalRead(); // we need to make PC points to the next instruction address
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    PC.internalStore(); // now PC points to the next instruction. We go back to the FETCH status.
  }

  public void incReg() {
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    PC.internalStore();
    PC.read();
    memory.read();
    demux.put(extbus1.get());
    registersInternalRead();
    ula.store(1);
    ula.inc();
    ula.read(1);
    ula.internalRead(1);
    setStatusFlags(intbus2.get());
    registersInternalStore();
    PC.internalRead(); // we need to make PC points to the next instruction address
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    PC.internalStore(); // now PC points to the next instruction. We go back to the FETCH status.
  }

  public void jmp() {
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    PC.internalStore(); // now PC points to the parameter address
    PC.read();
    memory.read();
    PC.store();
  }

  public void jn() {
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    PC.internalStore(); // now PC points to the parameter address
    if (Flags.getBit(1) == 1) {
      PC.read();
      memory.read();
      PC.store();
    } else {
      ula.inc();
      ula.internalRead(1);
      PC.internalStore();
    }
  }

  public void jz() {
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    PC.internalStore(); // now PC points to the parameter address
    if (Flags.getBit(0) == 1) {
      PC.read();
      memory.read();
      PC.store();
    } else {
      ula.inc();
      ula.internalRead(1);
      PC.internalStore();
    }
  }

  public void jeq() {
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    PC.internalStore(); // pc guarda o endereco de regA

    PC.read();// escreve endereco de regA no extbus1
    memory.read(); // memoria le e escreve o id de regA
    demux.put(extbus1.get());// seleciona o registrador correto
    registersInternalRead(); // regA escreve em intbus1
    ula.store(0); // ula armazena dado de regA no seu reg0
    ula.inc();
    ula.internalRead(1);

    PC.internalStore();// pc armazena endere�o de regB
    PC.read();
    memory.read(); // memoria le e escreve id do regB
    demux.put(extbus1.get());// seleciona o registrador correto
    ula.inc();
    ula.internalRead(1);
    PC.internalStore();// pc passa a apontar para proxima parametro (mem)
    registersInternalRead();// regB escreve em intbus1
    ula.store(1);// ula armazena dado de regB em seu reg1
    ula.sub();
    ula.internalRead(1);// ula escreve o resultado em intbus2
    setStatusFlags(intbus2.get()); // atualiza flags

    if (Flags.getBit(0) == 1) { // se o bit de flag 0 for 1 significa q a ultima subtra��o deu 0 logo sao iguais
      PC.read();// pc escreve o 3 parametro (mem)
      memory.read(); // memoria le o endere�o e devolve o endere�o para desvio
      PC.store();// pc armazena o endere�o da memoria para ser desviado
    } else {
      PC.internalRead();
      ula.internalStore(1);
      ula.inc();
      ula.internalRead(1);
      PC.internalStore();// pc armazena o endere�o da proxima instru��o
    }
  }

  public void jneq() {
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    PC.internalStore(); // pc guarda o endereco de regA
    PC.read();// escreve endereco de regA no extbus1
    memory.read(); // memoria le e escreve o id de regA
    demux.put(extbus1.get());// seleciona o registrador correto
    registersInternalRead(); // regA escreve em intbus1
    ula.store(0); // ula armazena dado de regA no seu reg0
    ula.inc();
    ula.internalRead(1);

    PC.internalStore();// pc armazena endere�o de regB
    PC.read();
    memory.read(); // memoria le e escreve id do regB
    demux.put(extbus1.get());// seleciona o registrador correto
    ula.inc();
    ula.internalRead(1);
    PC.internalStore();// pc passa a apontar para proxima parametro (men)
    registersInternalRead();// regB escreve em intbus1
    ula.store(1);// ula armazena dado de regB em seu reg1

    ula.sub();
    ula.internalRead(1);// ula escreve o resultado em intbus2
    setStatusFlags(intbus2.get()); // atualiza flags

    if (Flags.getBit(0) != 1) { // se o bit de flag for 1 significa q a ultima subtra��o n�o deu 0 logo s�o
                                // diferentes
      PC.read();// pc escreve o 3 parametro (mem)
      memory.read(); // memoria le o endere�o e devolve o endere�o para desvio
      PC.store();// pc armazena o endere�o da memoria para ser desviado
    } else {
      PC.internalRead();
      ula.internalStore(1);
      ula.inc();
      ula.internalRead(1);
      PC.internalStore();// pc armazena o endere�o da proxima instru��o
    }
  }

  public void jgt() {
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    PC.internalStore(); // now PC points to the parameter address

    PC.read();
    memory.read(); // memoria le e escreve o id de regA
    demux.put(extbus1.get());// seleciona o registrador correto
    registersInternalRead(); // regA escreve em intbus1
    ula.store(0); // ula armazena dado de regA no seu reg0
    ula.inc();
    ula.internalRead(1);

    PC.internalStore();// pc armazena endere�o de regB
    PC.read();
    memory.read(); // memoria le e escreve id do regB
    demux.put(extbus1.get());// seleciona o registrador correto
    ula.inc();
    ula.internalRead(1);
    PC.internalStore();// pc passa a apontar para proxima parametro (men)
    registersInternalRead();// regB escreve em intbus1
    ula.store(1);// ula armazena dado de regB em seu reg1

    ula.sub();
    ula.internalRead(1);// ula escreve o resultado em intbus2
    setStatusFlags(intbus2.get()); // atualiza flags

    if (Flags.getBit(1) == 0) {// se o bit de flag for 0 significa q a ultima opera��o n�o deu negativa
      if (Flags.getBit(0) == 0) {// signica que ele n�o� igual
        PC.read();// pc escreve o 3 parametro (mem)
        memory.read(); // memoria le o endere�o e devolve o endere�o para desvio
        PC.store();// pc armazena o endere�o da memoria para ser desviado
      } else {
        PC.internalRead();
        ula.internalStore(1);
        ula.inc();
        ula.internalRead(1);
        PC.internalStore();// pc armazena o endere�o da proxima instru��o
      }
    }

  }

  public void jlw() {
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    PC.internalStore(); // now PC points to the parameter address

    PC.read();
    memory.read(); // memoria le e escreve o id de regA
    demux.put(extbus1.get());// seleciona o registrador correto
    registersInternalRead(); // regA escreve em intbus1
    ula.store(0); // ula armazena dado de regA no seu reg0
    ula.inc();
    ula.internalRead(1);

    PC.internalStore();// pc armazena endere�o de regB
    PC.read();
    memory.read(); // memoria le e escreve id do regB
    demux.put(extbus1.get());// seleciona o registrador correto
    ula.inc();
    ula.internalRead(1);
    PC.internalStore();// pc passa a apontar para proxima parametro (mem)
    registersInternalRead();// regB escreve em intbus1
    ula.store(1);// ula armazena dado de regB em seu reg1

    ula.sub();
    ula.internalRead(1);// ula escreve o resultado em intbus2
    setStatusFlags(intbus2.get()); // atualiza flags

    if (Flags.getBit(1) == 1) {// se o bit de flag for 1 significa q a ultima opera��o deu negativa, logo regA
                               // � menor que B

      PC.read();// pc escreve o 3 parametro (mem)
      memory.read(); // memoria le o endere�o e devolve o endere�o para desvio
      PC.store();// pc armazena o endere�o da memoria para ser desviado
    } else {
      PC.internalRead();
      ula.internalStore(1);
      ula.inc();
      ula.internalRead(1);
      PC.internalStore();// pc armazena o endere�o da proxima instru��o
    }
  }

  public ArrayList<Register> getRegistersList() {
    return registersList;
  }

  /**
   * This method performs an (external) read from a register into the register
   * list.
   * The register id must be in the demux bus
   */
  private void registersRead() {
    registersList.get(demux.get()).read();
  }

  /**
   * This method performs an (internal) read from a register into the register
   * list.
   * The register id must be in the demux bus
   */
  private void registersInternalRead() {
    registersList.get(demux.get()).internalRead();
    ;
  }

  /**
   * This method performs an (external) store toa register into the register list.
   * The register id must be in the demux bus
   */
  private void registersStore() {
    registersList.get(demux.get()).store();
  }

  /**
   * This method performs an (internal) store toa register into the register list.
   * The register id must be in the demux bus
   */
  private void registersInternalStore() {
    registersList.get(demux.get()).internalStore();
    ;
  }

  /**
   * This method reads an entire file in machine code and
   * stores it into the memory
   * NOT TESTED
   * 
   * @param filename
   * @throws IOException
   */
  public void readExec(String filename) throws IOException {
    BufferedReader br = new BufferedReader(new FileReader(filename + ".dxf"));
    String linha;
    int i = 0;
    while ((linha = br.readLine()) != null) {
      extbus1.put(i);
      memory.store();
      extbus1.put(Integer.parseInt(linha));
      memory.store();
      i++;
    }
    br.close();
  }

  /**
   * This method executes a program that is stored in the memory
   */
  public void controlUnitEexec() {
    halt = false;
    while (!halt) {
      fetch();
      decodeExecute();
    }

  }

  private void decodeExecute() {
    IR.internalRead(); // the instruction is in the internalbus2
    int command = intbus2.get();
    simulationDecodeExecuteBefore(command);
    switch (command) {
      case 0:
        addRegReg();
        break;
      case 1:
        addMemReg();
        break;
      case 2:
        addRegMem();
        break;
      case 3:
        addImmReg();
        break;
      case 4:
        subRegReg();
        break;
      case 5:
        subMemReg();
        break;
      case 6:
        subRegMem();
        break;
      case 7:
        subImmReg();
        break;
      case 8:
        imulMemReg();
        break;
      case 9:
        imulRegMem();
        break;
      case 10:
        imulRegReg();
        break;
      case 11:
        moveMemReg();
        break;
      case 12:
        moveRegMem();
        break;
      case 13:
        moveRegReg();
        break;
      case 14:
        moveImmReg();
        break;
      case 15:
        incReg();
        break;
      case 16:
        jmp();
        break;
      case 17:
        jn();
        break;
      case 18:
        jz();
        break;
      case 19:
        jeq();
        break;
      case 20:
        jneq();
        break;
      case 21:
        jgt();
        break;
      case 22:
        jlw();
        break;
      default:
        halt = true;
        break;
    }
    if (simulation)
      simulationDecodeExecuteAfter();
  }

  /**
   * This method is used to show the components status in simulation conditions
   * NOT TESTED
   * 
   * @param command
   */
  private void simulationDecodeExecuteBefore(int command) {
    System.out.println("----------BEFORE Decode and Execute phases--------------");
    String instruction;
    int parameter = 0;
    for (Register r : registersList) {
      System.out.println(r.getRegisterName() + ": " + r.getData());
    }
    if (command != -1)
      instruction = commandsList.get(command);
    else
      instruction = "END";
    if (hasOperands(instruction)) {
      parameter = memory.getDataList()[PC.getData() + 1];
      System.out.println("Instruction: " + instruction + " " + parameter);
    } else
      System.out.println("Instruction: " + instruction);
    if ("read".equals(instruction))
      System.out.println("memory[" + parameter + "]=" + memory.getDataList()[parameter]);

  }

  /**
   * This method is used to show the components status in simulation conditions
   * NOT TESTED
   */
  private void simulationDecodeExecuteAfter() {
    String instruction;
    System.out.println("-----------AFTER Decode and Execute phases--------------");
    System.out.println("Internal Bus 1: " + intbus1.get());
    System.out.println("Internal Bus 2: " + intbus2.get());
    System.out.println("External Bus 1: " + extbus1.get());
    for (Register r : registersList) {
      System.out.println(r.getRegisterName() + ": " + r.getData());
    }
    Scanner entrada = new Scanner(System.in);
    System.out.println("Press <Enter>");
    String mensagem = entrada.nextLine();
  }

  /**
   * This method uses PC to find, in the memory,
   * the command code that must be executed.
   * This command must be stored in IR
   * NOT TESTED!
   */
  private void fetch() {
    PC.read();
    memory.read();
    IR.store();
    simulationFetch();
  }

  /**
   * This method is used to show the components status in simulation conditions
   * NOT TESTED!!!!!!!!!
   */
  private void simulationFetch() {
    if (simulation) {
      System.out.println("-------Fetch Phase------");
      System.out.println("PC: " + PC.getData());
      System.out.println("IR: " + IR.getData());
    }
  }

  /**
   * This method is used to show in a correct way the operands (if there is any)
   * of instruction,
   * when in simulation mode
   * NOT TESTED!!!!!
   * 
   * @param instruction
   * @return
   */
  private boolean hasOperands(String instruction) {
    if ("inc".equals(instruction)) // inc is the only one instruction having no operands
      return false;
    else
      return true;
  }

  /**
   * This method returns the amount of positions allowed in the memory
   * of this architecture
   * NOT TESTED!!!!!!!
   * 
   * @return
   */
  public int getMemorySize() {
    return memorySize;
  }

  public static void main(String[] args) throws IOException {
    Architecture arch = new Architecture(true);
    arch.readExec("program");
    arch.controlUnitEexec();
  }

}
