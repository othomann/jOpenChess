package test.java.movesInCircleBoard;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import main.java.board.IMove;
import main.java.board.Square;
import main.java.circleBoard.CircleBoard;
import main.java.game.Player;
import main.java.movesInCircleBoard.RookMovesInCircleBoard;
import main.java.pieces.Piece;
import main.java.pieces.PieceFactory;
import main.java.pieces.PieceFactory.PieceType;

public class RookMovesInCircleBoardTest {
	ArrayList<IMove> moveBehaviour;
	private Player p1;
	private CircleBoard board;
	private int x;
	private int y;
	Piece rook;

	@Before
	public void setUp() throws Exception {
		// Set Board and Player
		board =  new CircleBoard();
		p1 = new Player("Player1", "white");
		
		// Set Piece
		x = 3;
		y = 3;
		rook = PieceFactory.createSpecificPieceForCircleBoard(board, p1, PieceType.Rook);
		board.getSquares()[x][y].setPiece(rook);
	}

	// Case 1: Forward Before Center
	@Test
	public final void testGetMovesForwardRook() {

		ArrayList<Square> expected = new ArrayList<Square>();
		// Forward
		for (int i = 1; i <= 2; i++) {
			expected.add(board.getSquares()[x][y + i]);
		}
		// Backwards
		for (int i = 1; i <= 3; i++) {
			expected.add(board.getSquares()[x][y - i]);
		}
		// Clockwise
		for (int i = 1; i <= 20; i++) {
			expected.add(board.getSquares()[x + i][y]);
		}
		// Counterclockwise
		for (int i = 3; i > 0; i--) {
			expected.add(board.getSquares()[x + i][y]);
		}

		RookMovesInCircleBoard pm = new RookMovesInCircleBoard();
		ArrayList<Square> obtained = pm.getMoves(board, rook, true);
		assertTrue(obtained.containsAll(expected));

	}

}