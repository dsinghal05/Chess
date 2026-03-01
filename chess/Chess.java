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

		String trimmedMove = move.trim(); //removes unnecessary whitespace around String move
		
		if (trimmedMove.equals("resign")) {
			if (currentPlayer == Player.black) {
				result.message = ReturnPlay.Message.RESIGN_WHITE_WINS;
			}
			else {result.message = ReturnPlay.Message.RESIGN_BLACK_WINS;}
			return result;
			//ReturnPlay instance with pieces on board same as previous state of the board
		}

		//Initialize File and Rank values
		ReturnPiece.PieceFile fromFile = ReturnPiece.PieceFile.valueOf((trimmedMove.substring(0, 1)));
		int fromRank = trimmedMove.charAt(1) - '0';
		ReturnPiece.PieceFile toFile = ReturnPiece.PieceFile.valueOf((trimmedMove.substring(3, 4)));
		int toRank = trimmedMove.charAt(4) - '0';

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
		if (CurrentlyInCheck(pieceToMove, toFile, toRank, pieceToTake))
		{
			result.message = ReturnPlay.Message.ILLEGAL_MOVE;
			return result;
		}
		if(pieceToTake != null)
		{
			pieces.remove(pieceToTake);
		}

		//Move piece - must be handled seperately for promotions because it removes pawn and adds new promoted piece
		if (!((pieceToMove.pieceType.equals(ReturnPiece.PieceType.WP) && toRank == 8) ||
			(pieceToMove.pieceType.equals(ReturnPiece.PieceType.BP) && toRank == 1))) {
				pieceToMove.pieceFile = toFile;
				pieceToMove.pieceRank = toRank;
		}
		else {
			promoting(pieceToMove, toFile, toRank, trimmedMove);
		}
		
		
		if (trimmedMove.length() > 5) {
			if (trimmedMove.substring(6).equals("draw?")) {
				result.message = ReturnPlay.Message.DRAW;
				//ReturnPlay instance with pieces on board after move is executed
			}
		}
		
		
		//Switch player
		currentPlayer = (currentPlayer == Player.white) ? Player.black : Player.white;

		boolean InCheck = KingChecked(currentPlayer);
		boolean CanMove = LegalMoves (currentPlayer);
		
		if(InCheck)
		{
			if(!CanMove)
			{
				result.message = (currentPlayer == Player.white) ? ReturnPlay.Message.CHECKMATE_BLACK_WINS : ReturnPlay.Message.CHECKMATE_WHITE_WINS;
			}
			else
			{
				result.message = ReturnPlay.Message.CHECK;
			}
			if (!CanMove)
			{
				result.message = ReturnPlay.Message.STALEMATE;
			}
		}
		return result;
	}
	
	private static boolean CurrentlyInCheck(ReturnPiece piece, ReturnPiece.PieceFile toF, int toR, ReturnPiece taken)
	{
		ReturnPiece.PieceFile oldF = piece.pieceFile;
		int oldR = piece.pieceRank;
		piece.pieceFile = toF;
		piece.pieceRank = toR;
		if(taken != null)
		{
			pieces.remove(taken);
		}
		boolean InCheck = KingChecked(currentPlayer);
		piece.pieceFile = oldF;
		piece.pieceRank = oldR;
		if(taken != null)
		{
			pieces.add(taken);
		}
		return InCheck;
	}
	private static boolean KingChecked(Player player)
	{
		ReturnPiece king = null;
		String kingName = (player == Player.white) ? "WK" : "BK";
		for(ReturnPiece p : pieces)
		{
			if(p.pieceType.toString().equals(kingName))
			{
				king = p;
				break;
			}
		}
		if(king == null)
		{
			return false;
		}
		for(ReturnPiece p : pieces)
		{
			if(p.pieceType.toString().charAt(0) != kingName.charAt(0))
			{
				if(CheckMove(p, p.pieceFile, p.pieceRank, king.pieceFile, king.pieceRank, king))
				{
					return true;
				}
			}
		}
		return false;
	}
	private static boolean LegalMoves(Player player)
	{
		ArrayList<ReturnPiece> playerPieces = new ArrayList<>();
		for(ReturnPiece p : pieces)
		{
			if((player == Player.white && p.pieceType.toString().startsWith("W")) || (player == Player.black && p.pieceType.toString().startsWith("B")))
			{
				playerPieces.add(p);
			}
		}
		for(ReturnPiece p : playerPieces)
		{
			for(ReturnPiece.PieceFile f : ReturnPiece.PieceFile.values())
			{
				for(int r = 1; r <= 8; r++)
				{
					ReturnPiece target = checkForPiece(f, r);
					if(target != null && target.pieceType.toString().charAt(0) == p.pieceType.toString().charAt(0))
					{
						continue;
					}
					if (CheckMove(p, p.pieceFile, p.pieceRank, f, r, target)) 
					{
						if (!CurrentlyInCheck(p, f, r, target)) 
						{
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	
	/**
	 * This method should reset the game, and start from scratch.
	 */
	public static void start() {
		pieces.clear();
		initializeAllPieces();
		PlayChess.printBoard(pieces);
	}

	/**
	 * Checks if there is a piece on specified square
	 * @param toFile
	 * @param toRank
	 * @return Piece if one exists, or null if there is no piece there
	 */
	public static ReturnPiece checkForPiece(ReturnPiece.PieceFile toFile, int toRank) {
		ReturnPiece z = null;
		for (ReturnPiece p : pieces) {
			if (p.pieceFile == toFile && p.pieceRank == toRank) {
				z = p;
			}
		}
		return z;
	}

	/**
	 * Initializes all pieces to starting position in pieces Array
	 */
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

	/**
	 * Handles logic for all pieces to make sure a move is valid
	 * @param piece
	 * @param fromFile
	 * @param fromRank
	 * @param toFile
	 * @param toRank
	 * @param pieceToTake
	 * @return True if piece can be moved
	 */
	public static boolean CheckMove(ReturnPiece piece, ReturnPiece.PieceFile fromFile, int fromRank, ReturnPiece.PieceFile toFile, int toRank, ReturnPiece pieceToTake) {
		int fileDiff = Math.abs(fromFile.ordinal() - toFile.ordinal());
		int rankDiff = toRank - fromRank;

		//White pawn
		if (piece.pieceType.equals(ReturnPiece.PieceType.WP)) {
			if (pieceToTake == null) {
				if ((fileDiff == 0) && (rankDiff == 1 || (rankDiff == 2 && fromRank == 2))) {
					//if you're not taking a piece, you are not changing files.
					//you can move 1 space forward, or two if you are at rank 2.
					return parseMovement(fromFile, fromRank, toFile, toRank); //make sure you don't jump over a piece
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
					return parseMovement(fromFile, fromRank, toFile, toRank); //make sure you don't jump over a piece
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
		if (piece.pieceType.equals(ReturnPiece.PieceType.BQ) || piece.pieceType.equals(ReturnPiece.PieceType.WQ)) {
			if (!((rankDiff > 0 && fileDiff == 0) || (rankDiff == 0 && fileDiff > 0) || (fileDiff == rankDiff && fileDiff > 0))) {
				return false;
			}
			return parseMovement(fromFile, fromRank, toFile, toRank);
		}

		//King
		if (piece.pieceType.equals(ReturnPiece.PieceType.BK) || piece.pieceType.equals(ReturnPiece.PieceType.WK)) {
			//Checks for conditions for castling
			if ((fromFile == ReturnPiece.PieceFile.e) && 
				((piece.pieceType.equals(ReturnPiece.PieceType.WK) && fromRank == 1) ||
				(piece.pieceType.equals(ReturnPiece.PieceType.BK) && fromRank == 8))
				&& (toRank == 1 && (toFile == ReturnPiece.PieceFile.g || toFile == ReturnPiece.PieceFile.c))) {
					if (parseMovement(fromFile, fromRank, toFile, toRank)) {
						return castling(fromFile, fromRank, toFile, toRank, piece.pieceType);
					}
					else {return false;}
				} 
			
			if (!((rankDiff == 1 && fileDiff == 0) || (rankDiff == 0 && fileDiff == 1) || (fileDiff == rankDiff && fileDiff == 1))) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Replaces existing pawn with desired piece. 
	 * @param currPiece
	 * @param toFile
	 * @param toRank
	 * @param move
	 */
	static void promoting(ReturnPiece currPawn, ReturnPiece.PieceFile toFile, int toRank, String move) {
		String promoteTo = currPawn.pieceType.toString().substring(0, 1);
		if (move.length() == 7) {
			promoteTo += move.substring(6, 7);
		}
		else {promoteTo += "Q";}
		ReturnPiece newPiece = new ReturnPiece();
		newPiece.pieceType = ReturnPiece.PieceType.valueOf(promoteTo);
		newPiece.pieceRank = toRank;
		newPiece.pieceFile = toFile;
		pieces.add(newPiece);
		pieces.remove(currPawn);
	}

	/**
	 * Handles castling, makes sure king and rook are in correct position for castling
	 * @param fromFile
	 * @param fromRank
	 * @param toFile
	 * @param toRank
	 * @param color
	 * @return True if executed properly
	 */
	static boolean castling(ReturnPiece.PieceFile fromFile, int fromRank, ReturnPiece.PieceFile toFile, int toRank, ReturnPiece.PieceType color) {
		ReturnPiece rookToMove;
		if (color.equals(ReturnPiece.PieceType.WK)) {
			if (toFile.equals(ReturnPiece.PieceFile.g)) {
				rookToMove = checkForPiece(ReturnPiece.PieceFile.h, 1);
				if (rookToMove.pieceType.equals(ReturnPiece.PieceType.WR)) {
					rookToMove.pieceFile = ReturnPiece.PieceFile.f;
					return true;
				}
			}
			else { //castling queen side
				rookToMove = checkForPiece(ReturnPiece.PieceFile.a, 1);
					if (rookToMove.pieceType.equals(ReturnPiece.PieceType.WR)) {
						rookToMove.pieceFile = ReturnPiece.PieceFile.d;
						return true;
					}
				}
			}
		
		else {// black king
			if (toFile.equals(ReturnPiece.PieceFile.g)) {
				rookToMove = checkForPiece(ReturnPiece.PieceFile.h, 8);
				if (rookToMove.pieceType.equals(ReturnPiece.PieceType.BR)) {
					rookToMove.pieceFile = ReturnPiece.PieceFile.f;
					return true;
				}
			}
			else { //castling queen side
				rookToMove = checkForPiece(ReturnPiece.PieceFile.a, 8); 
					if (rookToMove.pieceType.equals(ReturnPiece.PieceType.BR)) {
						rookToMove.pieceFile = ReturnPiece.PieceFile.d;
						return true;
					}
				}
			}
		return false;
		}
	

	/**
	 * Checks all spaces between Original to Final position and makes sure there are no pieces that you would be jumping over
	
	 * @param fromFile
	 * @param fromRank
	 * @param toFile
	 * @param toRank
	 * @return True if this move is valid
	 */
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
				|| currentRank != toRank) {

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
