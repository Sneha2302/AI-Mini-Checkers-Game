package com.project;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.Parent;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MiniCheckers extends Application {

    private Stage stage;
    public static final int TILE_SIZE = 100;
    public static final int HEIGHT = 6;
    public static final int WIDTH = 6;
    private Group tileGroup = new Group();
    private Group pieceGroup = new Group();
    private Tile[][] board_gui = new Tile[WIDTH][HEIGHT];
    private Board board = new Board();
    private int max_depth_limit;
    private boolean turn;       //user plays first if true;


    //Starting the application
    @Override
    public void start(Stage primaryStage)  {
        Board.Type[][] b = board.getBoard();
        //setting up the Board GUI
        Scene scene = new Scene(setup(b));
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("New Game");
        alert.setHeaderText("New Game");
        alert.setContentText("Hi! Please select a difficulty level");
        //defining alert buttons
        ButtonType buttonTypeOne = new ButtonType("Easy");
        ButtonType buttonTypeTwo = new ButtonType("Normal");
        ButtonType buttonTypeThree = new ButtonType("Hard");
        ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(buttonTypeOne, buttonTypeTwo, buttonTypeThree, buttonTypeCancel);

        Optional<ButtonType> result = alert.showAndWait();
        //setting the max_depth_limit value based on the Difficulty level selected
        if (result.get() == buttonTypeOne){
            max_depth_limit =4;
        } else if (result.get() == buttonTypeTwo) {
            max_depth_limit =9;
        } else if (result.get() == buttonTypeThree) {
            max_depth_limit =15;
        } else {
            System.exit(0);
        }
        this.stage = primaryStage;
        primaryStage.setTitle("CheckersApp");
        primaryStage.setScene(scene);
        primaryStage.show();


        alert.setTitle("First Move");
        alert.setHeaderText(null);
        alert.setContentText("Do you want to make the first move?");

         buttonTypeOne = new ButtonType("Yes");
         buttonTypeTwo = new ButtonType("No");

        alert.getButtonTypes().setAll(buttonTypeOne, buttonTypeTwo);

        result = alert.showAndWait();
        if (result.get() == buttonTypeOne){
            turn = true;        // ... user goes first
        } else {
            turn = false;        // ... user goes second
            makeamove(0, 0, 0, 0);
        }



    }

    private Parent setup(Board.Type[][] b) {
        Pane root = new Pane();
        root.setPrefSize(WIDTH * TILE_SIZE, HEIGHT * TILE_SIZE);
        pieceGroup.getChildren().clear();
        root.getChildren().addAll(tileGroup, pieceGroup);

        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                Tile tile = new Tile((x + y) % 2 == 0, x, y);
                board_gui[x][y] = tile;

                tileGroup.getChildren().add(tile);

                Piece piece = null;
                if(b[y][x] == Board.Type.WHITE){
                    piece = makePiece(Player.Side.WHITE, x, y);
                }

                else if(b[y][x] == Board.Type.BLACK){
                    piece = makePiece(Player.Side.BLACK, x, y);
                }

                if (piece != null) {
                    tile.setPiece(piece);
                    pieceGroup.getChildren().add(piece);
                }
            }
        }

        return root;
    }

    private int toBoard(double pixel) {
        return (int)(pixel + TILE_SIZE / 2) / TILE_SIZE;
    }

    private Piece makePiece(Player.Side type, int x, int y) {
        Piece piece = new Piece(type, x, y);

        piece.setOnMouseReleased(e -> {

            int newX = toBoard(piece.getLayoutX());
            int newY = toBoard(piece.getLayoutY());
            int x0 = toBoard(piece.getOldX());
            int y0 = toBoard(piece.getOldY());

            makeamove(newX, newY, x0, y0);
        });


        return piece;
    }

    private void makeamove(int newX,int newY,int x0,int y0){

        Player one = new Player("User", Player.Side.BLACK);
        AlphaBetaSearch two = new AlphaBetaSearch(Player.Side.WHITE, max_depth_limit);
//        System.out.println("newX:"+newX+" newY:"+newY+" x0:"+x0+" y0:"+y0);
        int blackWin = 0;
        int whiteWin = 0;
        int draw = 0;
        int movesLeft = 1;
        int captureMoves = 0;
        boolean dec = false;

        Player current = one;
        if(!turn)
            current = two;
        boolean move = true;
        System.out.println(board.toString());
        while(move) {
            List<Point> capturePoints = new ArrayList<>();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            System.out.print(current.toString() + "'s turn: ");

            Board.Decision decision = null;
            if (current instanceof ABS_interface) {
                decision = ((ABS_interface) current).makeMove(board);

                //checking valid moves for the user
                List<Move> validMoves = board.getAllValidMoves(Player.Side.BLACK);
                movesLeft = validMoves.size();
                System.out.println("Valid Moves possible for User: " + movesLeft);
                if (movesLeft != 0)
                    move = false;
                else
                    turn = true;

                //getting capture move points
                capturePoints = board.getAllValidCaptureMovePoints(Player.Side.BLACK);

            } else {
                Move m;
                m = new Move(y0, x0, newY, newX);
                decision = current.makeMove(m, board);
            }

            if (decision == Board.Decision.INVALID_DESTINATION || decision == Board.Decision.INVALID_PIECE) {
                System.out.println("Move Failed");
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("");
                alert.setHeaderText(null);
                alert.setContentText("Move Failed!");
                alert.showAndWait();
                move = false;
                //checking for moves available for user
                List<Move> validMoves = board.getAllValidMoves(Player.Side.BLACK);
                movesLeft = validMoves.size();
                System.out.println("Valid Moves possible for User: " + movesLeft);
                if (movesLeft != 0)
                    move = false;
                else
                    turn = true;

                //getting capture move points
                capturePoints = board.getAllValidCaptureMovePoints(Player.Side.BLACK);

            } else if (decision == Board.Decision.SUCCESS) {
                System.out.println(board.toString());
                if (board.getNumBlackPieces() == 0) {
                    System.out.println("White wins with " + board.getNumWhitePieces() + " pieces left");
                    whiteWin++;
                    dec = true;
                }
                else if (board.getNumWhitePieces() == 0) {
                    System.out.println("Black wins with " + board.getNumBlackPieces() + " pieces left");
                    blackWin++;
                    dec = true;
                }
                if (turn)
                    current = two;
                else
                    current = one;
                turn = !turn;       //switching turns once the move is complete

            } else if (decision == Board.Decision.CAPTURE_MOVE) {
                System.out.println("Capture Move");
            } else if (decision == Board.Decision.GAME_OVER || movesLeft == 0) {
                //checking the winner based on number of pieces left
                    if(board.getNumBlackPieces() == board.getNumWhitePieces()) {
                        System.out.println("Draw, both have same number of pieces left. ");
                        draw++;
                    }
                    else if(Math.max(board.getNumBlackPieces(), board.getNumWhitePieces())==board.getNumBlackPieces()) {
                        System.out.println("Black wins with more pieces left than White.");
                        blackWin++;
                    }
                    else {
                        System.out.println("White wins with more pieces left than Black.");
                        whiteWin++;
                    }
                    dec = true;
                if(decision == Board.Decision.GAME_OVER)
                    move = false;
            }

            //setting up board again
            Board.Type[][] b = board.getBoard();
            Scene scene = new Scene(setup(b));
            stage.setScene(scene);
            stage.show();
            if (capturePoints!=null) {
                System.out.println(capturePoints.size());
                for(int i = 0; i < capturePoints.size(); i++) {
                    board_gui[capturePoints.get(i).x][capturePoints.get(i).y].setFill(Color.GREEN);
                     System.out.println(capturePoints.get(i));
                }
            }

        }
        System.out.println("Draw:"+draw+"  BlackWin:"+blackWin+"  WhiteWin:"+whiteWin);
        if(dec) {       //if we have a decision then end game
            System.out.print("Game Over. ");
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("");
            alert.setHeaderText("Game Over.");
            if (draw > 0) {
                System.out.println("It's a Draw!");
                alert.setContentText("It's a Draw!");
            }
            else if(Math.max(blackWin,whiteWin)==blackWin) {
                System.out.println("Black won!");
                alert.setContentText("Black won!");
            }
            else {
                System.out.println("White won!");
                alert.setContentText("White won!");
            }

            alert.showAndWait();

            stage.close();

        }
    }

    public static void main(String[] args) {
        launch(args);
    }

}
