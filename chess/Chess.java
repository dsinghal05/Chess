package chess;

import java.util.ArrayList;

public class Chess {
	static ArrayList<ReturnPiece> pieces = new ArrayList<ReturnPiece>();
	static Player currentPlayer = Player.white;

        enum Player { white, black }
    
	/**
	 * Plays the next move for whichever player has the turn.
	 * 
	 * @param move String for next move, e.g. "a2 a3"
	 * 
	 * @return A ReturnPlay instance that contains the result of the move.
	 *         See the section "The Chess class" in the assignment description for details of
	 *         the contents of the returned ReturnPlay instance.
	 */
	public static ReturnPlay play(String move) {
		ReturnPlay result = new ReturnPlay();
		result.piecesOnBoard = pieces;

		String trimmedMove = move.trim();
		char fromFileChar = trimmedMove.charAt(0);
		int fromRank = trimmedMove.charAt(1) - '0';
		char toFileChar = trimmedMove.charAt(3);
		int toRank = trimmedMove.charAt(4) - '0';

		ReturnPiece.PieceFile fromFile = ReturnPiece.PieceFile.valueOf((String.valueOf(fromFileChar)));
		ReturnPiece.PieceFile toFile = ReturnPiece.PieceFile.valueOf((String.valueOf(toFileChar)));

		//Find piece we are trying to move
		ReturnPiece pieceToMove = null;
		for (ReturnPiece p : pieces) {
			if (p.pieceRank == fromRank && p.pieceFile == fromFile) {
				pieceToMove = p;
				break;
			}
		}
		if (pieceToMove == null) {
			result.message = ReturnPlay.Message.ILLEGAL_MOVE;
			return result;
		}

		//Ensure correct player is moving piece
		if (currentPlayer == Player.white && pieceToMove.pieceType.toString().charAt(0) != 'W') {
        	result.message = ReturnPlay.Message.ILLEGAL_MOVE;
        	return result;
    	}
		if (currentPlayer == Player.black && pieceToMove.pieceType.toString().charAt(0) != 'B') {
			result.message = ReturnPlay.Message.ILLEGAL_MOVE;
			return result;
		}

		//Check if there is piece where we are trying to move
		ReturnPiece pieceToTake = null;
		for (ReturnPiece p : pieces) {
			if (p.pieceFile == toFile && p.pieceRank == toRank) {
				pieceToTake = p;
			}
		}
		if (pieceToTake != null) { //Ensure you don't take your own piece
			if (pieceToTake.pieceType.toString().charAt(0) == pieceToMove.pieceType.toString().charAt(0)) {
				result.message = ReturnPlay.Message.ILLEGAL_MOVE;
				return result;
			}
			else {
				pieces.remove(pieceToTake);
			}
		}

		//Move piece
		pieceToMove.pieceFile = toFile;
		pieceToMove.pieceRank = toRank;
		
		//Switch player
		if (currentPlayer == Player.white) {
			currentPlayer = Player.black;
		}
		else {currentPlayer = Player.white;}
		
		
		return result;

		/*
		TO ADD:
		Rules for each piece's movement
		Logic for special moves: castling, promotion, resign, draw
		Checks, checkmate, stalemate
		Can't make moves that place you in check
		*/
	}
	
	
	/**
	 * This method should reset the game, and start from scratch.
	 */
	public static void start() {
		pieces.clear();
		initializeAllPieces();
		PlayChess.printBoard(pieces);
	}


	public static void initializeAllPieces() {
		// Back rank order (same for white and black)
		ReturnPiece.PieceType[] whiteBackRank = {
				ReturnPiece.PieceType.WR,
				ReturnPiece.PieceType.WN,
				ReturnPiece.PieceType.WB,
				ReturnPiece.PieceType.WQ,
				ReturnPiece.PieceType.WK,
				ReturnPiece.PieceType.WB,
				ReturnPiece.PieceType.WN,
				ReturnPiece.PieceType.WR
		};

		ReturnPiece.PieceType[] blackBackRank = {
				ReturnPiece.PieceType.BR,
				ReturnPiece.PieceType.BN,
				ReturnPiece.PieceType.BB,
				ReturnPiece.PieceType.BQ,
				ReturnPiece.PieceType.BK,
				ReturnPiece.PieceType.BB,
				ReturnPiece.PieceType.BN,
				ReturnPiece.PieceType.BR
		};

		ReturnPiece.PieceFile[] files = ReturnPiece.PieceFile.values();


		// ---------- WHITE ----------
		for (int i = 0; i < 8; i++) {

			// Pawn
			ReturnPiece pawn = new ReturnPiece();
			pawn.pieceType = ReturnPiece.PieceType.WP;
			pawn.pieceFile = files[i];
			pawn.pieceRank = 2;
			pieces.add(pawn);

			// Back rank piece
			ReturnPiece back = new ReturnPiece();
			back.pieceType = whiteBackRank[i];
			back.pieceFile = files[i];
			back.pieceRank = 1;
			pieces.add(back);
		}


		// ---------- BLACK ----------
		for (int i = 0; i < 8; i++) {

			// Pawn
			ReturnPiece pawn = new ReturnPiece();
			pawn.pieceType = ReturnPiece.PieceType.BP;
			pawn.pieceFile = files[i];
			pawn.pieceRank = 7;
			pieces.add(pawn);

			// Back rank piece
			ReturnPiece back = new ReturnPiece();
			back.pieceType = blackBackRank[i];
			back.pieceFile = files[i];
			back.pieceRank = 8;
			pieces.add(back);
		}
	}	
}
