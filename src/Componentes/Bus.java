package Componentes;


public class Bus {
	
	private int data;
	
	
	
	public Bus() {
		data = 0;
	}

	/**
	 * Metodo que implementa o armazenamento de dados no barramento
	 * @param data
	 */
	public void put(int data){
		this.data = data;
	}
	
	/**
	 * Metodo que implementa a recuperação de dados do bus
	 * @return
	 */
	public int get() {
		return this.data;
		
	}

}
