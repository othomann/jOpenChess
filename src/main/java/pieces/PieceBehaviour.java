package main.java.pieces;

import java.util.ArrayList;
import java.util.Iterator;

import jchess.board.Square;
import main.java.game.Player;
import squareBoard.SquareBoard;

public class PieceBehaviour {

	private SquareBoard chessboard; 
	private Square square;
	private Player player;
	private String name;
	protected String symbol;
	
	
	
	public PieceBehaviour(SquareBoard chessboard, Player player) {
	this.setChessboard(chessboard);
	this.setPlayer(player);
	this.setName(this.getClass().getSimpleName());	
	}

	
	boolean canMove(Square square, ArrayList<Square> allmoves) {
		// throw new UnsupportedOperationException("Not supported yet.");
		ArrayList<Square> moves = allmoves;
		for (Iterator<Square> it = moves.iterator(); it.hasNext();) {
			Square sq = it.next();// get next from iterator
			if (sq == square) {// if address is the same
				return true; // piece canMove
			}
		}
		return false;// if not, piece cannot move
	}
	
	protected boolean isout(int x, int y) {
		if (x < 0 || x > 7 || y < 0 || y > 7) {
			return true;
		}
		return false;
	}
	
	/**
	 * @param x
	 *            y position on chessboard
	 * @param y
	 *            y position on chessboard
	 * @return true if can move, false otherwise
	 */
	protected boolean checkPiece(int x, int y) {
		if (getChessboard().getSquares()[x][y].piece != null
				&& getChessboard().getSquares()[x][y].piece.getName().equals("King")) {
			return false;
		}
		Piece piece = getChessboard().getSquares()[x][y].piece;
		if (piece == null || // if this square is empty
				piece.getPlayer() != this.getPlayer()) // or piece is another
														// player
		{
			return true;
		}
		return false;
	}
	
	/**
	 * Method check if piece has other owner than calling piece
	 * 
	 * @param x
	 *            x position on chessboard
	 * @param y
	 *            y position on chessboard
	 * @return true if owner(player) is different
	 */
	protected boolean otherOwner(int x, int y) {
		Square sq = getChessboard().getSquares()[x][y];
		if (sq.piece == null) {
			return false;
		}
		if (this.getPlayer() != sq.piece.getPlayer()) {
			return true;
		}
		return false;
	}
	
	
	public Square getSquare() {
		return square;
	}

	public void setSquare(Square square) {
		this.square = square;
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public SquareBoard getChessboard() {
		return chessboard;
	}

	public void setChessboard(SquareBoard chessboard) {
		this.chessboard = chessboard;
	}
	
}
