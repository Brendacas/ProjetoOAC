package Componentes;

public class Memory {
	
	private Bus bus;
	private int storePosition; //Esse valor indica que a memória leu um endereço e está esperando que um dado seja armazenado nessa posição
	private int size;
	private int dataList[];
	
	public Memory(int size, Bus bus) {
		storePosition = -1; //valores negativos que indicam que a memoria nao esta armazenando
		this.size = size;
		dataList = new int[size];
		this.bus = bus;
		for (int i=0;i<size;i++) {
			dataList[i] = 0;
		}
	}

	/**
	 * This method is used for TDD and Simulation purposes only
	 * NOT TESTED
	 * @return
	 */
	public int[] getDataList() {
		return dataList;
	}

	/**
	 * This method stores into position the data found in the bus
	 * @param position
	 */
	public void store() {
		if (storePosition < 0) { //the storing is just starting
			this.storePosition = bus.get();
		}
		else {//the storing was initiated, in the bus is the data
			this.dataList[storePosition] = bus.get();
			storePosition = -1; //no storing is being performed anymore
		}
	}
	
	/**
	 * This method gets the data from the position and stores it into the bus
	 * @param position
	 */
	public void read() {
		if ((bus.get() < size)&&(bus.get() >=0))
			bus.put(dataList[bus.get()]);
	}


}

