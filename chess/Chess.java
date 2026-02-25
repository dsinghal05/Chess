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
		
		if (trimmedMove.equals("resign")) {
			if (currentPlayer == Player.black) {
				result.message = ReturnPlay.Message.RESIGN_WHITE_WINS;
			}
			else {result.message = ReturnPlay.Message.RESIGN_BLACK_WINS;}
			return result;
			//ReturnPlay instance with pieces on board same as previous state of the board
		}

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
		ReturnPiece pieceToTake = checkForPiece(toFile, toRank);

		if (pieceToTake != null) { //Ensure you don't take your own piece
			if (pieceToTake.pieceType.toString().charAt(0) == pieceToMove.pieceType.toString().charAt(0)) {
				result.message = ReturnPlay.Message.ILLEGAL_MOVE;
				return result;
			}
		}

		//Check to make sure this piece can move in this way
		if (CheckMove(pieceToMove, fromFile, fromRank, toFile, toRank, pieceToTake) == false) {
			result.message = ReturnPlay.Message.ILLEGAL_MOVE;
			return result;
		}
		pieces.remove(pieceToTake);

		//Move piece
		pieceToMove.pieceFile = toFile;
		pieceToMove.pieceRank = toRank;
		
		if (trimmedMove.length() > 5) {
			if (trimmedMove.substring(6).equals("draw?")) {
				result.message = ReturnPlay.Message.DRAW;
				//ReturnPlay instance with pieces on board after move is executed
			}
		}
		
		
		//Switch player
		if (currentPlayer == Player.white) {
			currentPlayer = Player.black;
		}
		else {currentPlayer = Player.white;}
		
		
		return result;

		/*
		TO ADD:
		Logic for special moves: castling, promotion
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

	public static ReturnPiece checkForPiece(ReturnPiece.PieceFile toFile, int toRank) {
		ReturnPiece z = null;
		for (ReturnPiece p : pieces) {
			if (p.pieceFile == toFile && p.pieceRank == toRank) {
				z = p;
			}
		}
		return z;
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

	public static boolean CheckMove(ReturnPiece piece, ReturnPiece.PieceFile fromFile, int fromRank, ReturnPiece.PieceFile toFile, int toRank, ReturnPiece pieceToTake) {
		int fileDiff = Math.abs(fromFile.ordinal() - toFile.ordinal());
		int rankDiff = toRank - fromRank;

		//ensure pieces can't jump over pieces (except for knight)
		ReturnPiece pieceInBetween = null;
		
		//White pawn
		if (piece.pieceType.equals(ReturnPiece.PieceType.WP)) {
			if (pieceToTake == null) {
				if ((fileDiff == 0) && (rankDiff == 1 || (rankDiff == 2 && fromRank == 2))) {
					//if you're not taking a piece, you are not changing files.
					//you can move 1 space forward, or two if you are at rank 2.
					
					return checkForPiece(toFile, fromRank+1) == null; //make sure you don't jump over a piece
				}
			}
			else {
				if (fileDiff == 1 & rankDiff == 1) {
					return true;
				}
			}
			return false;
		}

		//Black Pawn: Same logic as white but rankDiff is negative (moves down)
		if (piece.pieceType.equals(ReturnPiece.PieceType.BP)) {
			if (pieceToTake == null) {
				if ((fileDiff == 0) && (rankDiff == -1 || (rankDiff == -2 && fromRank == 7))) {
					return checkForPiece(toFile, fromRank-1) == null; //make sure you don't jump over a piece
				}
			}
			else {
				if (fileDiff == 1 & rankDiff == -1) {
					return true;
				}
			}
			return false;
		}

		rankDiff = Math.abs(rankDiff);

		//Knight: Can jump over pieces. Moves 2 spaces 1 direction and 1 space a different direction.
		if (piece.pieceType.equals(ReturnPiece.PieceType.BN) || piece.pieceType.equals(ReturnPiece.PieceType.WN)) {
			return (fileDiff == 2 && rankDiff == 1) || (fileDiff == 1 && rankDiff == 2);
		}
		
		//Bishop
		if (piece.pieceType.equals(ReturnPiece.PieceType.BB) || piece.pieceType.equals(ReturnPiece.PieceType.WB)) {
			if (!(fileDiff == rankDiff && fileDiff > 0)) {
				return false;
			}
			return parseMovement(fromFile, fromRank, toFile, toRank);
		}

		//Rook
		if (piece.pieceType.equals(ReturnPiece.PieceType.BR) || piece.pieceType.equals(ReturnPiece.PieceType.WR)) {
			if (!((rankDiff > 0 && fileDiff == 0) || (rankDiff == 0 && fileDiff > 0))) {
				return false;
			}
			return parseMovement(fromFile, fromRank, toFile, toRank);
		}

		//Queen 
		if (piece.pieceType.equals(ReturnPiece.PieceType.BR) || piece.pieceType.equals(ReturnPiece.PieceType.WR)) {
			if (!((rankDiff > 0 && fileDiff == 0) || (rankDiff == 0 && fileDiff > 0) || (fileDiff == rankDiff && fileDiff > 0))) {
				return false;
			}
			return parseMovement(fromFile, fromRank, toFile, toRank);
		}

		//King
		if (piece.pieceType.equals(ReturnPiece.PieceType.BR) || piece.pieceType.equals(ReturnPiece.PieceType.WR)) {
			if (!((rankDiff == 1 && fileDiff == 0) || (rankDiff == 0 && fileDiff == 1) || (fileDiff == rankDiff && fileDiff == 1))) {
				return false;
			}
		}

		return true;
	}

	//Checks all spaces between Original to Final position and makes sure there are no pieces there
	static boolean parseMovement(ReturnPiece.PieceFile fromFile, int fromRank, ReturnPiece.PieceFile toFile, int toRank) {
		//indicates what direction it's going (left/right, up/down)
		int fileStep = 0;
		if (!(fromFile.equals(toFile))) {
			fileStep = (toFile.ordinal() > fromFile.ordinal()) ? 1 : -1;
		}
		int rankStep = 0;
		if (fromRank != toRank) {
			rankStep = (toRank > fromRank) ? 1 : -1;
		}

		int currentFileIndex = fromFile.ordinal() + fileStep;
		int currentRank = fromRank + rankStep;

		while (currentFileIndex != toFile.ordinal()
				&& currentRank != toRank) {

			ReturnPiece.PieceFile currentFile =
				ReturnPiece.PieceFile.values()[currentFileIndex];

			// Check if any piece is on this square
			if (checkForPiece(currentFile, currentRank) != null) {
				return false;
			}

			currentFileIndex += fileStep;
			currentRank += rankStep;
		}

		return true;

	}

}
