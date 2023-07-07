package architecture;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.Test;

import components.Memory;

public class TestArchitecture {
	
	//uncomment the anotation below to run the architecture showing components status
	//@Test
	public void testShowComponentes() {

		//a complete test (for visual purposes only).
		//a single code as follows
//		ldi 2
//		store 40
//		ldi -4
//		point:
//		store 41  //mem[41]=-4 (then -3, -2, -1, 0)
//		read 40
//		add 40    //mem[40] + mem[40]
//		store 40  //result must be in 40
//		read 41
//		inc
//		jn point
//		end
		
		Architecture arch = new Architecture(true);
		arch.getMemory().getDataList()[0]=7;
		arch.getMemory().getDataList()[1]=2;
		arch.getMemory().getDataList()[2]=6;
		arch.getMemory().getDataList()[3]=40;
		arch.getMemory().getDataList()[4]=7;
		arch.getMemory().getDataList()[5]=-4;
		arch.getMemory().getDataList()[6]=6;
		arch.getMemory().getDataList()[7]=41;
		arch.getMemory().getDataList()[8]=5;
		arch.getMemory().getDataList()[9]=40;
		arch.getMemory().getDataList()[10]=0;
		arch.getMemory().getDataList()[11]=40;
		arch.getMemory().getDataList()[12]=6;
		arch.getMemory().getDataList()[13]=40;
		arch.getMemory().getDataList()[14]=5;
		arch.getMemory().getDataList()[15]=41;
		arch.getMemory().getDataList()[16]=8;
		arch.getMemory().getDataList()[17]=4;
		arch.getMemory().getDataList()[18]=6;
		arch.getMemory().getDataList()[19]=-1;
		arch.getMemory().getDataList()[40]=0;
		arch.getMemory().getDataList()[41]=0;
		//now the program and the variables are stored. we can run
		arch.controlUnitEexec();
		
	}

	@Test
	public void testAddRegReg() {
		Architecture arch = new Architecture();

		arch.getExtbus1().put(10);
		arch.getPC().store();      //PC points to position 10

		arch.getMemory().getDataList()[11]=0;
		arch.getMemory().getDataList()[12]=1;

		arch.getExtbus1().put(30);
		arch.getRegistersList().get(0).store();

		arch.getExtbus1().put(10);
		arch.getRegistersList().get(1).store();

		arch.addRegReg();
		arch.getRegistersList().get(1).read();
		assertEquals(40,arch.getExtbus1().get());
		arch.getPC().read();
		assertEquals(13,arch.getExtbus1().get());
	}
	
	@Test
    public void testAddMemReg() {
        Architecture arch = new Architecture();

        arch.getExtbus1().put(10);
        arch.getPC().store();      //PC points to position 10

        arch.getMemory().getDataList()[11]=26;//dado ta no endereco 26
        arch.getMemory().getDataList()[26]=20;//dado do endereco eh 20
        arch.getMemory().getDataList()[12]=0;//regA ta na posicao 12

        arch.getExtbus1().put(30);
        arch.getRegistersList().get(0).store();
        arch.addMemReg();
		arch.getRegistersList().get(0).read();

        assertEquals(50,arch.getExtbus1().get());
        arch.getPC().read();
        assertEquals(13,arch.getExtbus1().get());
    }

	@Test
	public void testAddRegMem() {
		Architecture arch = new Architecture();

		arch.getExtbus1().put(10);
		arch.getPC().store();      //pc aponta para 10

		arch.getMemory().getDataList()[11]=0;// reg0 no endereço 11
		arch.getMemory().getDataList()[12]=26;//2 parametro (memoria) aponta para endereço 26
		arch.getMemory().getDataList()[26]= 10;// dado 10 está armazenado no endereço 26
		arch.getExtbus1().put(30);
		arch.getRegistersList().get(0).store();// reg0 tem o dado 30 armazenado 
		arch.addRegMem();
		assertEquals(40,arch.getMemory().getDataList()[26]);
		arch.getPC().read();
		assertEquals(13,arch.getExtbus1().get());
	}

	@Test
	public void testAddImmReg() {
		Architecture arch = new Architecture();

		arch.getExtbus1().put(10);
		arch.getPC().store();      //pc aponta para 10

		arch.getMemory().getDataList()[11]=10;// reg0 no endereço 11
		arch.getMemory().getDataList()[12]=0;//2 parametro (memoria) aponta para endereço 26
		arch.getExtbus1().put(30);
		arch.getRegistersList().get(0).store();// reg0 tem o dado 30 armazenado 
		arch.addImmReg();

		arch.getRegistersList().get(0).read();
		assertEquals(40,arch.getExtbus1().get());
		arch.getPC().read();
		assertEquals(13,arch.getExtbus1().get());
	}

	@Test
    public void testSubRegReg() {
        Architecture arch = new Architecture();

        arch.getExtbus1().put(10);
        arch.getPC().store();      //PC points to position 10

        arch.getMemory().getDataList()[11]=0;
        arch.getMemory().getDataList()[12]=1;

        arch.getExtbus1().put(30);
        arch.getRegistersList().get(0).store();

        arch.getExtbus1().put(10);
        arch.getRegistersList().get(1).store();

        arch.subRegReg();
        arch.getRegistersList().get(1).read();
        assertEquals(20,arch.getExtbus1().get());
        arch.getPC().read();
        assertEquals(13,arch.getExtbus1().get());
    }

	@Test
    public void testSubMemReg() {
        Architecture arch = new Architecture();

        arch.getExtbus1().put(10);
        arch.getPC().store();      //PC points to position 10

        arch.getMemory().getDataList()[11]=26;//dado ta no endereco 26
        arch.getMemory().getDataList()[26]=10;//dado do endereco eh 10
        arch.getMemory().getDataList()[12]=0;//regA ta na posicao 12

        arch.getExtbus1().put(30);
        arch.getRegistersList().get(0).store();
        arch.subMemReg();
		arch.getRegistersList().get(0).read();

        assertEquals(-20,arch.getExtbus1().get());
        arch.getPC().read();
        assertEquals(13,arch.getExtbus1().get());
    }

	@Test
    public void testSubRegMem() {
        Architecture arch = new Architecture();

        arch.getExtbus1().put(10);
        arch.getPC().store();      //PC points to position 10

        arch.getMemory().getDataList()[11]=0;
        arch.getMemory().getDataList()[12]=26;//endereco 26 é onde ta o dado
        arch.getMemory().getDataList()[26]=10;//dado eh igual a 10

        arch.getExtbus1().put(30);
        arch.getRegistersList().get(0).store();
        arch.subRegMem();
        arch.getRegistersList().get(1).read();
        assertEquals(20,arch.getMemory().getDataList()[26]);
        arch.getPC().read();
        assertEquals(13,arch.getExtbus1().get());
    }


	@Test
	public void testSubImmReg() {
		Architecture arch = new Architecture();

		arch.getExtbus1().put(10);
		arch.getPC().store();      //pc aponta para 10

		arch.getMemory().getDataList()[11]=10;// reg0 no endereço 11
		arch.getMemory().getDataList()[12]=0;//2 parametro (memoria) aponta para endereço 26
		arch.getExtbus1().put(30);
		arch.getRegistersList().get(0).store();// reg0 tem o dado 30 armazenado 
		arch.subImmReg();

		arch.getRegistersList().get(0).read();
		assertEquals(-20,arch.getExtbus1().get());
		arch.getPC().read();
		assertEquals(13,arch.getExtbus1().get());
	}


	@Test
	public void testMoveMenReg() {
		Architecture arch = new Architecture();

		arch.getExtbus1().put(10);
		arch.getPC().store();      //pc aponta para 10
		arch.getMemory().getDataList()[11]=26;// memoria aponta para endereço 26
		arch.getMemory().getDataList()[26]=10;
		arch.getMemory().getDataList()[12]=0;// 2 parametro aponta para id de reg0
		arch.moveMemReg();
		arch.getRegistersList().get(0).read();
		assertEquals(10,arch.getExtbus1().get());
		arch.getPC().read();
		assertEquals(13,arch.getExtbus1().get());
	}

	@Test
    public void testMoveRegMem() {
        Architecture arch = new Architecture();

        arch.getExtbus1().put(10);
        arch.getPC().store();      //PC points to position 10

        arch.getMemory().getDataList()[11]=0;
        arch.getMemory().getDataList()[12]=26;//endereco 26 é onde ta o dado
        arch.getMemory().getDataList()[26]=10;//dado eh igual a 10

        arch.getExtbus1().put(30);
        arch.getRegistersList().get(0).store();
        arch.moveRegMem();
        assertEquals(30,arch.getMemory().getDataList()[26]);
        arch.getPC().read();
        assertEquals(13,arch.getExtbus1().get());
    }

	@Test
    public void testMoveRegReg() {
        Architecture arch = new Architecture();

        arch.getExtbus1().put(10);
        arch.getPC().store();      //PC points to position 10

        arch.getMemory().getDataList()[11]=0;
        arch.getMemory().getDataList()[12]=1;

        arch.getExtbus1().put(30);
        arch.getRegistersList().get(0).store();
        arch.getExtbus1().put(20);
        arch.getRegistersList().get(1).store();
        arch.moveRegReg();
        arch.getRegistersList().get(1).read();
        assertEquals(30,arch.getExtbus1().get());
        arch.getPC().read();
        assertEquals(13,arch.getExtbus1().get());
    }

	@Test
    public void testMoveImmReg() {
        Architecture arch = new Architecture();

        arch.getExtbus1().put(10);
        arch.getPC().store();      //PC points to position 10

        arch.getMemory().getDataList()[11]=10;
        arch.getMemory().getDataList()[12]=0;//endereco 26 é onde ta o dado
        arch.getExtbus1().put(30);
        arch.getRegistersList().get(0).store();
        arch.moveImmReg();
		arch.getRegistersList().get(0).read();
        assertEquals(10,arch.getExtbus1().get());
        arch.getPC().read();
        assertEquals(13,arch.getExtbus1().get());
    }
	
	
	
	@Test
	public void testJmp() {
		Architecture arch = new Architecture();

        arch.getExtbus1().put(10);
        arch.getPC().store();      //PC points to position 10
		arch.getMemory().getDataList()[11]=20;
		arch.jmp();
        arch.getPC().read();
        assertEquals(20,arch.getExtbus1().get());
	}

	@Test
    public void jeq(){

    Architecture arch = new Architecture();

        arch.getExtbus1().put(10);
        arch.getPC().store();      //PC points to position 10

        arch.getMemory().getDataList()[11]=0;
     	arch.getMemory().getDataList()[12]=1;
		arch.getMemory().getDataList()[13]=20;
        arch.getExtbus1().put(30);
        arch.getRegistersList().get(0).store();

     	arch.getExtbus1().put(30);
        arch.getRegistersList().get(1).store();

    	arch.jeq();
		arch.getPC().read();
        assertEquals(20,arch.getExtbus1().get());

	}
	@Test
    public void jneq(){

    	Architecture arch = new Architecture();

        arch.getExtbus1().put(10);
        arch.getPC().store();      //PC points to position 10
        arch.getMemory().getDataList()[11]=0;
     	arch.getMemory().getDataList()[12]=1;
		arch.getMemory().getDataList()[13]=20;
        arch.getExtbus1().put(30);
        arch.getRegistersList().get(0).store();
     	arch.getExtbus1().put(31);
        arch.getRegistersList().get(1).store();
    	arch.jneq();
		arch.getPC().read();
        assertEquals(20,arch.getExtbus1().get());

	}
	
	@Test
	public void testJz() {
		Architecture arch = new Architecture();
		arch.getExtbus1().put(10);
        arch.getPC().store();      
        arch.getMemory().getDataList()[11]=0;
     	arch.getMemory().getDataList()[12]=1;
        arch.getExtbus1().put(30);
        arch.getRegistersList().get(0).store();
     	arch.getExtbus1().put(30);
        arch.getRegistersList().get(1).store();
		arch.subRegReg();
		arch.getMemory().getDataList()[14]=20;
		arch.jz();
		arch.getPC().read();
        assertEquals(20,arch.getExtbus1().get());
		
	}
	
	@Test
	public void testJn() {
		Architecture arch = new Architecture();
		arch.getExtbus1().put(10);
        arch.getPC().store();      
        arch.getMemory().getDataList()[11]=0;
     	arch.getMemory().getDataList()[12]=1;
        arch.getExtbus1().put(30);
        arch.getRegistersList().get(0).store();
     	arch.getExtbus1().put(31);
        arch.getRegistersList().get(1).store();
		arch.subRegReg();
		arch.getMemory().getDataList()[14]=20;
		arch.jn();
		arch.getPC().read();
        assertEquals(20,arch.getExtbus1().get());
	}
	
	@Test
	public void testeIncReg(){
		Architecture arch = new Architecture();

		arch.getExtbus1().put(10);
		arch.getPC().store();      //PC points to position 10

		arch.getMemory().getDataList()[11]=0;
		arch.getExtbus1().put(30);
		arch.getRegistersList().get(0).store();
		arch.incReg();
		arch.getRegistersList().get(0).read();
		assertEquals(31,arch.getExtbus1().get());
		arch.getPC().read();
		assertEquals(12,arch.getExtbus1().get());
	}	

	@Test
	public void testeJgt(){
		Architecture arch = new Architecture();

        arch.getExtbus1().put(10);
        arch.getPC().store();      //PC points to position 10
        arch.getMemory().getDataList()[11]=0;
     	arch.getMemory().getDataList()[12]=1;
		arch.getMemory().getDataList()[13]=20;
        arch.getExtbus1().put(31);
        arch.getRegistersList().get(0).store();
     	arch.getExtbus1().put(30);
        arch.getRegistersList().get(1).store();
    	arch.jgt();
		arch.getPC().read();
        assertEquals(20,arch.getExtbus1().get());

	}

	@Test
	public void testeJlw(){
		Architecture arch = new Architecture();

        arch.getExtbus1().put(10);
        arch.getPC().store();      //PC points to position 10
        arch.getMemory().getDataList()[11]=0;
     	arch.getMemory().getDataList()[12]=1;
		arch.getMemory().getDataList()[13]=20;
        arch.getExtbus1().put(30);
        arch.getRegistersList().get(0).store();
     	arch.getExtbus1().put(31);
        arch.getRegistersList().get(1).store();
    	arch.jlw();
		arch.getPC().read();
        assertEquals(20,arch.getExtbus1().get());

	}

	@Test
	public void testFillCommandsList() {
		
		//all the instructions must be in Commands List
		/*
		 *
				add addr (rpg <- rpg + addr)
				sub addr (rpg <- rpg - addr)
				jmp addr (pc <- addr)
				jz addr  (se bitZero pc <- addr)
				jn addr  (se bitneg pc <- addr)
				read addr (rpg <- addr)
				store addr  (addr <- rpg)
				ldi x    (rpg <- x. x must be an integer)
				inc    (rpg++)
		 */
		
		Architecture arch = new Architecture();
		ArrayList<String> commands = arch.getCommandsList();
		assertTrue("addRegReg".equals(commands.get(0)));
		assertTrue("addMemReg".equals(commands.get(1)));
		assertTrue("addRegMem".equals(commands.get(2)));
		assertTrue("addImmReg".equals(commands.get(3)));
		assertTrue("subRegReg".equals(commands.get(4)));
		assertTrue("subMemReg".equals(commands.get(5)));
		assertTrue("subRegMem".equals(commands.get(6)));
		assertTrue("subImmReg".equals(commands.get(7)));
		assertTrue("imulMemReg".equals(commands.get(8)));
		assertTrue("imulRegMem".equals(commands.get(9)));
		assertTrue("imulRegReg".equals(commands.get(10)));
		assertTrue("moveMemReg".equals(commands.get(11)));
		assertTrue("moveRegMem".equals(commands.get(12)));
		assertTrue("moveRegReg".equals(commands.get(13)));
		assertTrue("moveImmReg".equals(commands.get(14)));
		assertTrue("incReg".equals(commands.get(15)));
		assertTrue("jmp".equals(commands.get(16)));
		assertTrue("jn".equals(commands.get(17)));
		assertTrue("jz".equals(commands.get(18)));
		assertTrue("jeq".equals(commands.get(19)));
		assertTrue("jneq".equals(commands.get(20)));
		assertTrue("jgt".equals(commands.get(21)));
		assertTrue("jlw".equals(commands.get(22)));
		

	}
	
	@Test
	public void testReadExec() throws IOException {
		Architecture arch = new Architecture();
		arch.readExec("testFile");
		assertEquals(5, arch.getMemory().getDataList()[0]);
		assertEquals(4, arch.getMemory().getDataList()[1]);
		assertEquals(3, arch.getMemory().getDataList()[2]);
		assertEquals(2, arch.getMemory().getDataList()[3]);
		assertEquals(1, arch.getMemory().getDataList()[4]);
		assertEquals(0, arch.getMemory().getDataList()[5]);
	}

}
