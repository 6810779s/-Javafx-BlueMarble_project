package Data;

public class PlayerData {
	private String id;
	private int turn;
	private int money;
	private int cntX;
	private int cntY;
	private int win;
	private int lose;
	private int posi;
	private int position;
	public int bre;
	private int card;
	public PlayerData() {
		money = 2000000;
		cntX = 15;
		cntY =20;
		posi = 2;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public int getTurn() {
		return turn;
	}
	public void setTurn(int turn) {
		this.turn = turn;
	}
	public int getMoney() {
		return money;
	}
	public void setMoney(int money) {
		this.money = money;
	}
	public int getCntX() {
		return cntX;
	}
	public void setCntX(int cntX) {
		this.cntX = cntX;
	}
	public int getCntY() {
		return cntY;
	}
	public void setCntY(int cntY) {
		this.cntY = cntY;
	}
	public int getWin() {
		return win;
	}
	public void setWin(int win) {
		this.win = win;
	}
	public int getLose() {
		return lose;
	}
	public void setLose(int lose) {
		this.lose = lose;
	}
	public int getPosi() {
		return posi;
	}
	public void setPosi(int posi) {
		this.posi = posi;
	}
	public int getPosition() {
		return position;
	}
	public void setPosition(int position) {
		this.position = position;
	}
	public int getBre() {
		return bre;
	}
	public void setBre(int bre) {
		this.bre = bre;
	}
	public int getCard() {
		return card;
	}
	public void setCard(int card) {
		this.card = card;
	}
	
	
}
