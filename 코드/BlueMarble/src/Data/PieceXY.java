package Data;

public class PieceXY {
	private double pieceX;
	private double pieceY;
	public PieceXY(double x,double y) {
		setPieceX(x);
		setPieceY(y);
	}
	public double getPieceX() {
		return pieceX;
	}
	public void setPieceX(double pieceX) {
		this.pieceX = pieceX;
	}
	public double getPieceY() {
		return pieceY;
	}
	public void setPieceY(double pieceY) {
		this.pieceY = pieceY;
	}
}
