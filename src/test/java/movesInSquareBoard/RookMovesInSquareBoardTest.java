package test.java.movesInSquareBoard;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import main.java.board.Square;
import main.java.game.Player;
import main.java.game.Settings;
import main.java.movesInSquareBoard.RookMovesInSquareBoard;
import main.java.pieces.Piece;
import main.java.pieces.PieceFactory;
import main.java.pieces.PieceFactory.PieceType;
import main.java.squareBoard.SquareBoard;

public class RookMovesInSquareBoardTest {
	SquareBoard board;
	Settings settings;
	Piece rook;
	Player p1;
	int x;
	int y;

	@Before
	public void setUp() throws Exception {
		// Set Board and Player
		settings = mock(Settings.class); 
		board =  new SquareBoard(settings);
		p1 = new Player("Player1", "white");
		
		// Set Piece
		x = 3;
		y = 3;
		rook = PieceFactory.createSpecificPieceForSquareBoard(board, p1, PieceType.Rook);
		board.getSquares()[x][y].setPiece(rook);
	}

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
		// Right
		for (int i = 1; i <= 4; i++) {
			expected.add(board.getSquares()[x + i][y]);
		}
		// Left
		for (int i = 3; i > 0; i--) {
			expected.add(board.getSquares()[x + i][y]);
		}

		RookMovesInSquareBoard pm = new RookMovesInSquareBoard();
		ArrayList<Square> obtained = pm.getMoves(board, rook, true);
		assertTrue(obtained.containsAll(expected));
	}
}