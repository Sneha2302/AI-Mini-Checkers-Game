package com.project;


import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class Board {

    private Type[][] board;
    public final int SIZE = 6;

    private int numWhitePieces;
    private int numBlackPieces;

    //types of pieces
    public enum Type {
        EMPTY, WHITE, BLACK
    }

    //possible result values to make a decision
    public enum Decision {
        SUCCESS,
        CAPTURE_MOVE,
        GAME_OVER,
        INVALID_PIECE,
        INVALID_DESTINATION,
    }

    public Board() {
        setUpBoard();
    }
    public Board(Type[][] board)
    {
        numWhitePieces = 0;
        numBlackPieces = 0;

        this.board = board;
        for(int i = 0; i < SIZE; i++)
        {
            for(int j = 0; j< SIZE; j++)
            {
                Type piece = getPiece(i, j);
                if(piece == Type.BLACK)
                    numBlackPieces++;
                else if(piece == Type.WHITE)
                    numWhitePieces++;
            }
        }
    }

    //setting up the board
    private void setUpBoard() {
        numWhitePieces = 6;
        numBlackPieces = 6;
        board = new Type[SIZE][SIZE];
        for (int i = 0; i < board.length; i++) {
            int start = 0;
            if (i % 2 == 0)
                start = 1;

            Type pieceType = Type.EMPTY;
            if (i <= 1)
                pieceType = Type.WHITE;
            else if (i >= 4)
                pieceType = Type.BLACK;

            for (int j = start; j < board[i].length; j += 2) {
                board[i][j] = pieceType;
            }
        }
        //add empty values where no pieces
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] == null)
                    board[i][j] = Type.EMPTY;
            }
        }
    }


    //returns piece using row and col
    public Type getPiece(int row, int col) {
        return board[row][col];
    }
    //returns piece using point
    public Type getPiece(Point point) {
        return board[point.x][point.y];
    }

    public Type[][] getBoard()
    {
        return board;
    }
    //getting number of pieces
    public int getNumWhitePieces() {
        return numWhitePieces;
    }

    public int getNumBlackPieces() {
        return numBlackPieces;
    }


    // returns true if the move is successful
    public Decision makeMove(Move move, Player.Side side) {
        if(move == null) {
            return Decision.GAME_OVER;
        }
        Point start = move.getStart();
        int startRow = start.x;
        int startCol = start.y;
        Point end = move.getEnd();
        int endRow = end.x;
        int endCol = end.y;

        //allows moving only own piece
        if (!isMovingOwnPiece(startRow, startCol, side) || getPiece(startRow, startCol) == Type.EMPTY)
            return Decision.INVALID_PIECE;

        List<Move> possibleMoves = getValidMoves(startRow, startCol, side);

        Type currType = getPiece(startRow, startCol);

        if (possibleMoves.contains(move)) {
            boolean captureMove = false;
            //if it contains move then it is either 1 move or 1 jump
            if (startRow + 1 == endRow || startRow - 1 == endRow) {
                board[startRow][startCol] = Type.EMPTY;
                board[endRow][endCol] = currType;
            } else {
                captureMove = true;
                board[startRow][startCol] = Type.EMPTY;
                board[endRow][endCol] = currType;
                Point mid = findMidSquare(move);

                Type middle = getPiece(mid);
                if (middle == Type.BLACK)
                    numBlackPieces--;
                else if(middle == Type.WHITE)
                    numWhitePieces--;
                board[mid.x][mid.y] = Type.EMPTY;
            }

            if (captureMove) {
                List<Move> additional = getValidCaptureMoves(endRow, endCol, side);
                if (additional.isEmpty())
                    return Decision.SUCCESS;
                return Decision.CAPTURE_MOVE;
            }
            return Decision.SUCCESS;
        } else
            return Decision.INVALID_DESTINATION;
    }

    //gets all the valid moves for the player
    public List<Move> getAllValidMoves(Player.Side side) {

        Type normal = side == Player.Side.BLACK ? Type.BLACK : Type.WHITE;

        List<Move> possibleMoves = new ArrayList<>();
        for(int i = 0; i < SIZE; i++)
        {
            for(int j = 0; j < SIZE; j++)
            {
                Type t = getPiece(i, j);
                if(t == normal)
                    possibleMoves.addAll(getValidMoves(i, j, side));
            }
        }

        return possibleMoves;
    }

    // requires there to actually be a mid square
    private Point findMidSquare(Move move) {

        Point ret = new Point((move.getStart().x + move.getEnd().x) / 2,
                (move.getStart().y + move.getEnd().y) / 2);

        return ret;
    }
    //checks if player is moving own piece
    private boolean isMovingOwnPiece(int row, int col, Player.Side side) {
        Type pieceType = getPiece(row, col);
        if (side == Player.Side.BLACK && pieceType != Type.BLACK)
            return false;
        else if (side == Player.Side.WHITE && pieceType != Type.WHITE)
            return false;
        return true;
    }

    //gets all valid moves possible
    public List<Move> getValidMoves(int row, int col, Player.Side side) {
        Type type = board[row][col];
        Point startPoint = new Point(row, col);
        if (type == Type.EMPTY)
            throw new IllegalArgumentException();

        List<Move> moves = new ArrayList<>();

        //2 possible moves
        if (type == Type.WHITE || type == Type.BLACK) {
            int rowChange = type == Type.WHITE ? 1 : -1;

            int newRow = row + rowChange;
            if (newRow >= 0 && newRow < SIZE) {
                int newCol = col + 1;
                if (newCol < SIZE && getPiece(newRow, newCol) == Type.EMPTY)
                    moves.add(new Move(startPoint, new Point(newRow, newCol)));
                newCol = col - 1;
                if (newCol >= 0 && getPiece(newRow, newCol) == Type.EMPTY)
                    moves.add(new Move(startPoint, new Point(newRow, newCol)));
            }

        }

        moves.addAll(getValidCaptureMoves(row, col, side));
        return moves;
    }

    public List<Move> getAllValidCaptureMoves(Player.Side side) {

        Type normal = side == Player.Side.BLACK ? Type.BLACK : Type.WHITE;

        List<Move> possibleMoves = new ArrayList<>();
        for(int i = 0; i < SIZE; i++)
        {
            for(int j = 0; j < SIZE; j++)
            {
                Type t = getPiece(i, j);
                if(t == normal)
                    possibleMoves.addAll(getValidCaptureMoves(i, j, side));
            }
        }

        return possibleMoves;
    }

    //gets all valid capture moves
    public List<Move> getValidCaptureMoves(int row, int col, Player.Side side) {
        List<Move> move = new ArrayList<>();
        Point start = new Point(row, col);

        List<Point> options = new ArrayList<>();

        if(side == Player.Side.WHITE && getPiece(row, col) == Type.WHITE)
        {
            options.add(new Point(row + 2, col + 2));
            options.add(new Point(row + 2, col - 2));
        }
        else if(side == Player.Side.BLACK && getPiece(row, col) == Type.BLACK)
        {
            options.add(new Point(row - 2, col + 2));
            options.add(new Point(row - 2, col - 2));
        }

        for (int i = 0; i < options.size(); i++) {
            Point temp = options.get(i);
            Move m = new Move(start, temp);
            if (temp.x < SIZE && temp.x >= 0 && temp.y < SIZE && temp.y >= 0 && getPiece(temp.x, temp.y) == Type.EMPTY
                    && isOpponentPiece(side, getPiece(findMidSquare(m)))) {
                move.add(m);
            }
        }

        //System.out.println("Skip moves: " + move);
        return move;
    }

    //gets all valid capture moves
    public List<Point> getAllValidCaptureMovePoints(Player.Side side) {
        List<Move> validCaptureMoves = getAllValidCaptureMoves(Player.Side.BLACK);
        List<Point> capturePoints = new ArrayList<>();
        int captureMoves = validCaptureMoves.size();
        if(captureMoves>0) {
            System.out.println("Capture moves possible for user: "+captureMoves);
            for(int i = 0; i < captureMoves; i++)
            {
                System.out.println(validCaptureMoves.get(i));
                Point p = validCaptureMoves.get(i).getStart();
                capturePoints.add(new Point(p.y, p.x));
            }
        }
        return capturePoints;
    }

    // return true if the piece is opponents
    private boolean isOpponentPiece(Player.Side current, Type opponentPiece) {
        if (current == Player.Side.BLACK && (opponentPiece == Type.WHITE))
            return true;
        if (current == Player.Side.WHITE && (opponentPiece == Type.BLACK))
            return true;
        return false;
    }
    //prints the Board in string on the console
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("  ");
        for (int i = 0; i < board.length; i++) {
            b.append(i + " ");
        }
        b.append("\n");
        for (int i = 0; i < board.length; i++) {
            for (int j = -1; j < board[i].length; j++) {
                String a = "";
                if (j == -1)
                    a = i + "";
                else if (board[i][j] == Type.WHITE)
                    a = "w";
                else if (board[i][j] == Type.BLACK)
                    a = "b";
                else
                    a = "_";

                b.append(a);
                b.append(" ");
            }
            b.append("\n");
        }
        return b.toString();
    }

    //for cloning the board
    public Board clone()
    {
        Type[][] newBoard = new Type[SIZE][SIZE];
        for(int i = 0; i < SIZE; i++)
        {
            for(int j = 0; j< SIZE; j++)
            {
                newBoard[i][j] = board[i][j];
            }
        }
        Board b = new Board(newBoard);
        return b;
    }
}
