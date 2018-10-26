//This program facilitates playing the game 2048. It uses JavaFX and event based programming to create a version of the 
//game 2048 with a Graphical User Interface. It uses methods for moving the tiles, placing Cells into the gridpane,
//check to see if the game is lost, checking to see if the game is won, setting up a new game board, placing numbers into 
//the boards, saving the game, loading the game, saving the undo states, retrieving undo states and more.
//
//Author: Martin Amos
//Course: EECS 1510
//Instructor: Dr. L. Thomas
//Date: 27 April 2018

// Various imports
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.*;
import javafx.scene.paint.Color;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;

public class GUI2048 extends Application
{
	//Here we'll create the various panes/boxes needed to display the game
	private static GridPane gPane = new GridPane();		//This grid pane will display the 4X4 board
	private static VBox vBox = new VBox();				//This VBox will contain the required buttons
	private static HBox Scores = new HBox();			//this HBox will contain the score/highscore/move counters
	private static BorderPane bPane = new BorderPane();	//This border pane will hold the other pieces in relative position
	private static StackPane sPane = new StackPane();	
	private static File gameSave = new File("2048.dat");
	private static File hsSave = new File("HighScore.dat");
	private static Label lblHighScore = new Label("High Score: 0");
	private static Label lblScore = new Label("Score: 0");
	private static Label lblMoves = new Label("Moves: 0");
	private static Media TaDa = new Media("http://www.winhistory.de/more/winstart/mp3/win31.mp3");
	
	static int[][] board = new int[4][4];				//This array holds the current board's numbers
	static int[][][] undoBoards = new int[10][4][4];	//This array holds the 10 undo states of the board
	static int[] undoScores = new int[10];				//This array holds the 10 last scores
	static int undoPointer;								//Position of next undo in undoBoards and undoScores
	static int undoBuffer; 								//number of available undo actions
	static Font font = new Font("Arial", 54);			//Font used for numbers on the cells
	static int score = 0, movesMade = 0, highScore; 	//counters for score, highScore and moves
	static boolean gameWon = false;						//has game been won?
	static boolean setNewHighScore = false;				//has a new high score been set?
	
	@Override //overriding the start method in the Application class
	public void start(Stage primaryStage)
	{
		//creating help, exit, save, and load buttons
		Button btHelp = new Button("HELP");		
		Button btExit = new Button("EXIT");
		Button btSave = new Button("SAVE");
		Button btLoad = new Button("LOAD");
		
		setupNewGame();	//set the board for a new game
		getHighScore(); //retrieves a high score if there is one
		gPane.setPadding(new Insets(18, 18, 18, 18)); //Set the edge borders to 18 pixels
		gPane.setHgap(18);gPane.setVgap(18);
		gPane.setAlignment(Pos.CENTER);
		sPane.getChildren().add(gPane);
		
		//setting all buttons to have the same preferred size
		btHelp.setPrefWidth(80);btExit.setPrefWidth(80);btSave.setPrefWidth(80);btLoad.setPrefWidth(80);
		btHelp.setFocusTraversable(false);btExit.setFocusTraversable(false); //stops buttons from stealing the focus
		btSave.setFocusTraversable(false);btLoad.setFocusTraversable(false);
		btHelp.setOnAction(e ->{showHelp();});btSave.setOnAction(e ->{saveGame();}); //tell each button to run their appropriate
		btLoad.setOnAction(e ->{loadGame();});btExit.setOnAction(e ->{quitGame();}); //method when clicked
		vBox.getChildren().addAll(btHelp, btSave, btLoad, btExit); 		//adding buttons to vBox
		
		Scores.getChildren().addAll(lblHighScore, lblScore, lblMoves);	//adds high score, score, & moves to the Scores HBox
		Scores.setAlignment(Pos.CENTER);	//sets formatting for Scores HBox
		Scores.setSpacing(30);
		
		bPane.setTop(Scores);		//adds scores HBox to top borderpane portion
		bPane.setLeft(vBox);		//adds the button VBox to the left borderpane portion
		
		refreshBoard();		//refresh the displayed board
		Scene Game = new Scene(bPane);
		
		/* This lambda expression and accompanying switch statement are responsible for the main loop of the game.
		 * From here all moves a executed as well as all other commands.*/
		Game.setOnKeyPressed( e -> 
		{
			switch(e.getCode())
			{
				//for each direction, if it can be moved, it does, then it refreshes the displayed board and check for win or loss
				case DOWN:	if(canMoveDown()){ 	moveDown(); refreshBoard(); isWinner(); isGameOver();}break;
				case UP:	if(canMoveUp()){	moveUp();	refreshBoard(); isWinner(); isGameOver();}break;
				case LEFT:	if(canMoveLeft()){ 	moveLeft(); refreshBoard(); isWinner(); isGameOver();}break;
				case RIGHT: if(canMoveRight()){	moveRight();refreshBoard(); isWinner(); isGameOver();}break;
				
				case Z: if(e.isControlDown()) 	undoMove();					break;
				case S: if(e.isAltDown())		saveGame();					break;
				case L: if(e.isAltDown())		loadGame();					break;
				case H: if(e.isAltDown())		showHelp();					break;
				case X: if(e.isAltDown())		quitGame();					break;
				default:
			}
		});	
		
		primaryStage.setResizable(false);
		primaryStage.setTitle("2048");
		primaryStage.setScene(Game);
		primaryStage.show();		
	}

	public static void setupNewGame()
	{
		/* This method prepares the board for a new game by setting all cells to zero, then placing the 2 initial
		 * numbers. The method then sets movesMade, score, undoPointer, and undoBuffer to zero.*/
		
		for (int row = 0; row < 4; row++)
			for (int col = 0 ; col < 4; col++)
				board[row][col] = 0;
		placeNumbers();
		placeNumbers();
		board[2][3] = 1024;
		board[3][3] = 1024;
		
		movesMade = 0;score = 0; undoPointer = 0; undoBuffer = 0;
	}
	
	public static void refreshBoard()
	{
		/* This method goes through the board cell by cell creating a new Cell object for each point in the gPane
		 * based on the number appearing in the int board array. After it has refreshed to gridpane to show the 
		 * current board state, it places the gridpane in the center section of the boarderpane and sets its background
		 * color to the appropriate grey.*/
		for(int Row = 0; Row < 4; Row++)
		{
			for(int Col = 0; Col < 4; Col++)
			{
				Cell cell = new Cell(board[Row][Col]);
				gPane.add(cell, Col, Row);
			}
		}
		if (score > highScore) //if current score exceeds highScore set the new highScore, mark the boolean indicator and save it
		{
			highScore = score;
			setNewHighScore = true;
			saveHighScore();
		}
		lblHighScore = new Label("High Score: "+highScore);
		lblScore = new Label("Score: "+score);
		lblMoves = new Label("Moves: "+movesMade);
		
		Scores.getChildren().clear();
		Scores.getChildren().addAll(lblHighScore, lblScore, lblMoves);
		
		bPane.setCenter(gPane);
		gPane.setStyle("-fx-background-color: #BBADA0;"); //set the board background color to grey
	}
	
	public static void placeNumbers()
	{
		/* This method randomly picks an open space on the board and populates it with a number from the randomNumber
		 * method. First the method cycles through the board array counting the numberOfBlanks it finds and recording
		 * their positions in the array emptySpaces. Once all zero positions have been stored in emptySpaces, it 
		 * generates a random number RNG from 0 up to the number of zeros 
		 * found in the game board inclusive. It uses RNG as a position marker for emptySpaces. The location stored at
		 * emptySpaces[RNG] is where the method places the number returned by randomNumber. */
		
		int numberOfBlanks = 0;				// counter for blanks
		int[] emptySpaces = new int[16]; 		//create array of size equal to number of empty cells found
		int positionZeroArray = 0, positionGameBoard = 0;
		for (int row = 0; row < 4; row++)
		{
			for (int col = 0; col < 4; col++)
			{
				if (board[row][col] == 0) 	//When a zero is found, the location on the game board (0-15) is stored
				{
					emptySpaces[positionZeroArray] = positionGameBoard;	//record the board location in the emptySpaces array
					positionZeroArray++;	//move to the next spot in the emptySpaces array
					numberOfBlanks++;		//increment the count of blank spaces on the board
				}
				positionGameBoard++;	//whether a 0 is found or not, increment the board position counter
			}
		}
		int randomPosition = (int)(Math.random()*numberOfBlanks); //generate a random number up to the numberOfBlanks
		
		/* The line below does much of the work of this method. It selects a random open position on the board
		 * by going to randomPosition in the emptySpaces array which will hold the location of a position on the 
		 * board that is currently empty. It determines the row by dividing the board position found in the emptySpaces
		 * array by 4 and casting as an int, and determines the column by getting the value from emptySpaces and 
		 * taking that value modulo 4 cast as an int. Then it populates that board position with randomNumber method*/
		
		board[(int)(emptySpaces[randomPosition]/4)][(int)(emptySpaces[randomPosition]%4)] = randomNumber();
	} //end of placeNumbers
	
	public static int randomNumber()
	{
		/* This method generates and returns an int. 90% of the time that int is a 2 and the other 10% of the time
		 * it is a 4. It does this by creating a random integer from 0 to 9. If that number is less than 9, it returns
		 * a 2, otherwise it returns a 4.*/
		
		int RNG = (int)(Math.random()*10);
		if (RNG < 9)
			return 2;
		return 4;
	}
	
	public static boolean canMoveRight()
	{
		/* This method checks the current board state, if right is a possible move it returns true, otherwise it 
		 * returns false. First it takes the values of the elements of a row and places them into variables so they
		 * can be easily compared. If any non-zero cell has an empty cell to its right, the board can move right
		 * and the method returns true. If any non-zero cell contains the same value as the cell to its right then
		 * the board can move right and the method returns true. If neither of these conditions are met, the method
		 * checks the next row down in the same way. If the method has check all 4 rows and there was no row that
		 * could move right, then the method returns false because the board must be in a state where a move right
		 * is impossible. */
		
		for (int row = 0; row < 4; row++)
		{
			int A = board[row][0];	//A will hold the value of the left-most cell in the row being examined
			int B = board[row][1];
			int C = board[row][2];
			int D = board[row][3];
			
			if ((A != 0 && (B == 0 || C == 0 || D == 0)) || 	//if any non-zero cell has an empty cell to its right
				(B != 0 && (C == 0 || D == 0)) ||
				(C != 0 && (D == 0)) ||
				((A != 0 && A == B) || (B != 0 && B == C) || (C != 0 && C == D))) //or is equal to the cell to its right
				return true;	//then the board can be moved right
		}
		return false;
	} //end of canMoveRight()
	
	public static boolean canMoveLeft()
	{
		/* This method checks the current board state, if left is a possible move it returns true, otherwise it 
		 * returns false. First it takes the values of the elements of a row and places them into variables so they
		 * can be easily compared. If any non-zero cell has an empty cell to its left, the board can move left
		 * and the method returns true. If any non-zero cell contains the same value as the cell to its left then
		 * the board can move left and the method returns true. If neither of these conditions are met, the method
		 * checks the next row down in the same way. If the method has check all 4 rows and there was no row that
		 * could move left, then the method returns false because the board must be in a state where a move left
		 * is impossible. */
		
		for (int row = 0; row < 4; row++)
		{
			int A = board[row][3];	//A will hold the value of the right-most cell in the row being examined
			int B = board[row][2];
			int C = board[row][1];
			int D = board[row][0];
			
			if ((A != 0 && (B == 0 || C == 0 || D == 0)) || 	//if any non-zero cell has an empty cell to its left
				(B != 0 && (C == 0 || D == 0)) ||
				(C != 0 && (D == 0)) ||
				((A != 0 && A == B) || (B != 0 && B == C) || (C != 0 && C == D))) //or is equal to the cell to its left
				return true;	//then the board can be moved left
		}
		return false;
	} //end of canMoveLeft()
	
	public static boolean canMoveDown()
	{
		/* This method checks the current board state, if down is a possible move it returns true, otherwise it 
		 * returns false. First it takes the values of the elements of a column and places them into variables so
		 * they can be easily compared. If any non-zero cell has an empty cell below it, the column can move down
		 * and the method returns true. If any non-zero cell contains the same value as the cell below it then
		 * the column can move down and the method returns true. If neither of these conditions are met, the method
		 * checks the next column to the right in the same way. If the method has checked all 4 columns and there was 
		 * no column that could move down, then the method returns false because the board must be in a state where a 
		 * move down is impossible. */
		
		for (int col = 0; col < 4; col++)
		{
			int A = board[0][col];	//A will hold the value of the top cell in the column being examined
			int B = board[1][col];
			int C = board[2][col];
			int D = board[3][col];
			
			if ((A != 0 && (B == 0 || C == 0 || D == 0)) || 	//if any non-zero cell has an empty cell below it
				(B != 0 && (C == 0 || D == 0)) ||
				(C != 0 && (D == 0)) ||
				((A != 0 && A == B) || (B != 0 && B == C) || (C != 0 && C == D))) //or is equal to the cell below it
				return true;	//then the board can be moved down
		}
		return false;
	} //end of canMoveDown()
	
	public static boolean canMoveUp()
	{
		/* This method checks the current board state, if up is a possible move it returns true, otherwise it 
		 * returns false. First it takes the values of the elements of a column and places them into variables so
		 * they can be easily compared. If any non-zero cell has an empty cell above it, the column can move up
		 * and the method returns true. If any non-zero cell contains the same value as the cell above it then
		 * the column can move up and the method returns true. If neither of these conditions are met, the method
		 * checks the next column to the right in the same way. If the method has check all 4 columns and there was 
		 * no column that could move up, then the method returns false because the board must be in a state where a 
		 * move up is impossible. */
		
		for (int col = 0; col < 4; col++)
		{
			int A = board[3][col];	//A will hold the value of the bottom cell in the column being examined
			int B = board[2][col];
			int C = board[1][col];
			int D = board[0][col];
			
			if ((A != 0 && (B == 0 || C == 0 || D == 0)) || 	//if any non-zero cell has an empty cell above it
				(B != 0 && (C == 0 || D == 0)) ||
				(C != 0 && (D == 0)) ||
				((A != 0 && A == B) || (B != 0 && B == C) || (C != 0 && C == D))) //or is equal to the cell above it
				return true;	//then the board can be moved up
		}
		return false;
	} //end of canMoveUp()
	
	public static void squeezeNumbersRight()
	{
		/* This method moves all number towards the right edge of the board, displacing any blank cells to the left.
		 * It examines a row of the board. If any non-zero cell has a blank cell to its right, they are swapped and 
		 * a swap counter incremented. The row is examined in this way until no swaps are performed which indicates
		 * that all blank cells have been forced out to the left. Then the method move to the next row of the array
		 * and performs operations in this way until all numbers on the board have been squeezed to the right.*/
		
		for (int row = 0; row < 4; row++)
		{
			int swaps;		//swap counter
			do
			{
				swaps = 0;	//set swap counter to zero
				int A = board[row][0];	//A is the left-most cell of the row
				int B = board[row][1];
				int C = board[row][2];
				int D = board[row][3];
				if (A != 0 && B == 0)	{board[row][1] = A; board[row][0] = 0; swaps++;} //If a zero is found to the right
				if (B != 0 && C == 0)	{board[row][2] = B; board[row][1] = 0; swaps++;} //of any non-zero cell, they are 
				if (C != 0 && D == 0)	{board[row][3] = C; board[row][2] = 0; swaps++;} //swapped and swaps incremented.
			}
			while (swaps != 0); //if any swaps were performed, examine the row again to see if more squeezing is needed
		}
	} //end squeezeNumbersRight
	
	public static void squeezeNumbersLeft()
	{
		/* This method moves all number towards the left edge of the board, displacing any blank cells to the right.
		 * It examines a row of the board. If any non-zero cell has a blank cell to its left, they are swapped and 
		 * a swap counter incremented. The row is examined in this way until no swaps are performed which indicates
		 * that all blank cells have been forced out to the left. Then the method moves to the next row of the array
		 * and performs operations in this way until all numbers on the board have been squeezed to the left.*/
		
		for (int row = 0; row < 4; row++)
		{
			int swaps;		//swap counter
			do
			{
				swaps = 0;	//set swap counter to zero
				int A = board[row][3];	//A is the right-most cell of the row
				int B = board[row][2];
				int C = board[row][1];
				int D = board[row][0];
				if (A != 0 && B == 0)	{board[row][2] = A; board[row][3] = 0; swaps++;} //If a zero is found to the left
				if (B != 0 && C == 0)	{board[row][1] = B; board[row][2] = 0; swaps++;} //of any non-zero cell, they are 
				if (C != 0 && D == 0)	{board[row][0] = C; board[row][1] = 0; swaps++;} //swapped and swaps incremented.
			}
			while (swaps != 0); //if any swaps were performed, examine the row again to see if more squeezing is needed
		}
	} //end squeezeNumbersLeft
	
	public static void squeezeNumbersUp()
	{
		/* This method moves all number towards the top edge of the board, displacing any blank cells to the bottom.
		 * It examines a column of the board. If any non-zero cell has a blank cell above it, they are swapped and 
		 * a swap counter incremented. The column is examined in this way until no swaps are performed which indicates
		 * that all blank cells have been forced out to the bottom. Then the method moves to the next column of the array
		 * and performs operations in this way until all numbers on the board have been squeezed to the top.*/

		for (int col = 0; col < 4; col++)
		{
			int swaps;		//swap counter
			do
			{
				swaps = 0;	//set swap counter to zero
				int A = board[3][col];	//A is the bottom cell of the column
				int B = board[2][col];
				int C = board[1][col];
				int D = board[0][col];
				if (A != 0 && B == 0)	{board[2][col] = A; board[3][col] = 0; swaps++;} //If a zero is found above
				if (B != 0 && C == 0)	{board[1][col] = B; board[2][col] = 0; swaps++;} //any non-zero cell, they are 
				if (C != 0 && D == 0)	{board[0][col] = C; board[1][col] = 0; swaps++;} //swapped and swaps incremented.
			}
			while (swaps != 0); //if any swaps were performed, examine the column again to see if more squeezing is needed
		}
	} //end squeezeNumbersUp

	public static void squeezeNumbersDown()
	{
		/* This method moves all number towards the bottom edge of the board, displacing any blank cells to the top.
		 * It examines a column of the board. If any non-zero cell has a blank cell below it, they are swapped and 
		 * a swap counter incremented. The column is examined in this way until no swaps are performed which indicates
		 * that all blank cells have been forced out to the top. Then the method moves to the next column of the array
		 * and performs operations in this way until all numbers on the board have been squeezed to the bottom.*/
		
		for (int col = 0; col < 4; col++)
		{
			int swaps;		//swap counter
			do
			{
				swaps = 0;	//set swap counter to zero
				int A = board[0][col];	//A is the top cell of the column
				int B = board[1][col];
				int C = board[2][col];
				int D = board[3][col];
				if (A != 0 && B == 0)	{board[1][col] = A; board[0][col] = 0; swaps++;} //If a zero is found below
				if (B != 0 && C == 0)	{board[2][col] = B; board[1][col] = 0; swaps++;} //any non-zero cell, they are 
				if (C != 0 && D == 0)	{board[3][col] = C; board[2][col] = 0; swaps++;} //swapped and swaps incremented.
			}
			while (swaps != 0); //if any swaps were performed, examine the column again to see if more squeezing is needed
		}
	} //end squeezeNumbersDown

	public static void combineRight()
	{
		/* This method merges any adjacent numbers which are equal to each other and adds the value of the 
		 * merged cell to the score tracker.  The method goes through the array row by row. For each row, it 
		 * assigns the values of the cells to variables A, B, C, & D for easy comparison. If the method finds 
		 * that a non-zero cell is equal to the cell to its left, they are merged and the value added to score.
		 * The operations are performed on the variables representing the cells of the array, so before moving on
		 * to the next row, the method places the new values of A, B, C, & D back into where they were gathered
		 * from in the main board.*/
		
		for (int row = 0; row < 4; row++)
		{
			int A = board[row][0];	//A is the left-most cell of the current row
			int B = board[row][1];
			int C = board[row][2];
			int D = board[row][3];
			
			if (D != 0 && C == D) {D = 2*C; C = 0; score += D;} //If a non-zero cell is equal to the cell to its left,
			if (C != 0 && B == C) {C = 2*B; B = 0; score += C;} //the right cell takes on 2 times the current value of
			if (B != 0 && A == B) {B = 2*A; A = 0; score += B;} //both cells, the left cell set to 0, and score increased.
			
			board[row][0] = A;		//The changed values of A, B, C, & D are placed back into the array.
			board[row][1] = B;
			board[row][2] = C;
			board[row][3] = D;
		}
	} //end combineRight

	public static void combineLeft()
	{
		/* This method merges any adjacent numbers which are equal to each other and adds the value of the 
		 * merged cell to the score tracker.  The method goes through the array row by row. For each row, it 
		 * assigns the values of the cells to variables A, B, C, & D for easy comparison. If the method finds 
		 * that a non-zero cell is equal to the cell to its right, they are merged and the value added to score.
		 * The operations are performed on the variables representing the cells of the array, so before moving on
		 * to the next row, the method places the new values of A, B, C, & D back into where they were gathered
		 * from in the main board.*/
		
		for (int row = 0; row < 4; row++)
		{
			int A = board[row][3];	//A is the right-most cell of the current row
			int B = board[row][2];
			int C = board[row][1];
			int D = board[row][0];
			
			if (D != 0 && C == D) {D = 2*C; C = 0; score += D;} //If a non-zero cell is equal to the cell to its right,
			if (C != 0 && B == C) {C = 2*B; B = 0; score += C;} //the left cell takes on 2 times the current value of
			if (B != 0 && A == B) {B = 2*A; A = 0; score += B;} //both cells, the right cell set to 0, and score increased.
			
			board[row][3] = A;		//The changed values of A, B, C, & D are placed back into the array.
			board[row][2] = B;
			board[row][1] = C;
			board[row][0] = D;
		}
	} //end combineLeft
	
	public static void combineUp()
	{
		/* This method merges any vertically adjacent numbers which are equal to each other and adds the value of 
		 * the merged cell to the score tracker.  The method goes through the array column by column. For each column, 
		 * it assigns the values of the cells to variables A, B, C, & D for easy comparison. If the method finds 
		 * that a non-zero cell is equal to the cell below it, they are merged and the value added to score.
		 * The operations are performed on the variables representing the cells of the array, so before moving on
		 * to the next column, the method places the new values of A, B, C, & D back into where they were gathered
		 * from in the main board.*/
		
		for (int col = 0; col < 4; col++)
		{
			int A = board[3][col];	//A is the bottom cell of the current column
			int B = board[2][col];
			int C = board[1][col];
			int D = board[0][col];
			
			if (D != 0 && C == D) {D = 2*C; C = 0; score += D;} //If a non-zero cell is equal to the cell below it,
			if (C != 0 && B == C) {C = 2*B; B = 0; score += C;} //the upper cell takes on 2 times the current value of
			if (B != 0 && A == B) {B = 2*A; A = 0; score += B;} //both cells, the lower cell set to 0, and score increased.
			
			board[3][col] = A;		//The changed values of A, B, C, & D are placed back into the board array.
			board[2][col] = B;
			board[1][col] = C;
			board[0][col] = D;
		}
	} //end combineUp

	public static void combineDown()
	{
		/* This method merges any vertically adjacent numbers which are equal to each other and adds the value of 
		 * the merged cell to the score tracker.  The method goes through the array column by column. For each column, 
		 * it assigns the values of the cells to variables A, B, C, & D for easy comparison. If the method finds 
		 * that a non-zero cell is equal to the cell above it, they are merged and the value added to score.
		 * The operations are performed on the variables representing the cells of the array, so before moving on
		 * to the next column, the method places the new values of A, B, C, & D back into where they were gathered
		 * from in the main board.*/
		
		for (int col = 0; col < 4; col++)
		{
			int A = board[0][col];	//A is the top cell of the current column
			int B = board[1][col];
			int C = board[2][col];
			int D = board[3][col];
			
			if (D != 0 && C == D) {D = 2*C; C = 0; score += D;} //If a non-zero cell is equal to the cell above it,
			if (C != 0 && B == C) {C = 2*B; B = 0; score += C;} //the lower cell takes on 2 times the current value of
			if (B != 0 && A == B) {B = 2*A; A = 0; score += B;} //both cells, the upper cell set to 0, and score increased.
			
			board[0][col] = A;		//The changed values of A, B, C, & D are placed back into the board array.
			board[1][col] = B;
			board[2][col] = C;
			board[3][col] = D;
		}
	} //end combineDown

	public static void moveRight()
	{
		/* This method executes a move right when requested by the player. It starts by saving the current board state
		 * so that the user can undo to a position before the move executes. It squeezes out any zero cells to the 
		 * left using squeezeNumbersRight, then its executes combineLeft to merge any horizontally adjacent equal cells,
		 * then it executes squeeze again because there is a chance that combineRight introduced zero cells where they
		 * don't belong.  Finally it calls placeNumbers to place a new 2 or 4 into the board and increments the move
		 * counter.  Lastly, it sets moveMadeSinceUndo to true so it is known that the move can be undone.*/ 
		
		saveForUndo();
		squeezeNumbersRight();
		combineRight();
		squeezeNumbersRight();
		placeNumbers();
		movesMade++;
	}

	public static void moveLeft()
	{
		/* This method executes a move left when requested by the player. It starts by saving the current board state
		 * so that the user can undo to a position before the move executes. It squeezes out any zero cells to the 
		 * right using squeezeNumbersLeft, then its executes combineLeft to merge any horizontally adjacent equal cells,
		 * then it executes squeeze again because there is a chance that combineLeft introduced zero cells where they
		 * don't belong.  Finally it calls placeNumbers to place a new 2 or 4 into the board and increments the move
		 * counter. Lastly, it sets moveMadeSinceUndo to true so it is known that the move can be undone.*/ 
		
		saveForUndo();
		squeezeNumbersLeft();
		combineLeft();
		squeezeNumbersLeft();
		placeNumbers();
		movesMade++;
	}
	
	public static void moveUp()
	{
		/* This method executes a move up when requested by the player. It starts by saving the current board state
		 * so that the user can undo to a position before the move executes. It squeezes out any zero cells to the 
		 * bottom using squeezeNumbersUp, then its executes combineUp to merge any vertically adjacent equal cells,
		 * then it executes squeeze again because there is a chance that combineUp introduced zero cells where they
		 * don't belong.  Finally it calls placeNumbers to place a new 2 or 4 into the board and increments the move
		 * counter. Lastly, it sets moveMadeSinceUndo to true so it is known that the move can be undone.*/
		
		saveForUndo();
		squeezeNumbersUp();
		combineUp();
		squeezeNumbersUp();
		placeNumbers();
		movesMade++;
	}
	
	public static void moveDown()
	{
		/* This method executes a move down when requested by the player. It starts by saving the current board state
		 * so that the user can undo to a position before the move executes. It squeezes out any zero cells to the 
		 * top using squeezeNumbersDown, then its executes combineDown to merge any vertically adjacent equal cells,
		 * then it executes squeeze again because there is a chance that combineDown introduced zero cells where they
		 * don't belong. Finally it calls placeNumbers to place a new 2 or 4 into the board and increments the move
		 * counter. Lastly, it sets moveMadeSinceUndo to true so it is known that the move can be undone.*/
		
		saveForUndo();
		squeezeNumbersDown();
		combineDown();
		squeezeNumbersDown();
		placeNumbers();
		movesMade++;
	}
	
	public static void saveForUndo()
	{
		/* This method saves the current board state into undoBoards array before a move is made. It goes through 
		 * the current board cell by cell and copies the current board entries into the undoBoard array which
		 * contains the last 10 boards. It also saves the current score into an array containing the 10 last scores.
		 * Then it increments the undo pointer so the next saveForUndo won't overwrite anything more recent than
		 * 10 moves ago and sets undoPointer to undoPointer % 10 because we only save 10 undo states and will get
		 * an arrayOutOfBoundsException if undo is allowed to be 10 or greater. Finally, if undoBuffer is less than
		 * 10, it is incremented.*/
		for (int Col = 0; Col < 4; Col++)
		{
			for (int Row = 0; Row < 4; Row++)
			{
				undoBoards[undoPointer][Row][Col] = board[Row][Col];
			}
		}
		undoScores[undoPointer] = score;
		undoPointer++;
		undoPointer %= 10;
		if (undoBuffer < 10)
			undoBuffer++;
	}
	
	public static void quitGame()
	{ 
		/* This method is called whenever the game ends. If a new high score was set, it displays a window with the new 
		 * HS and plays the TaDa sound effect. After a 5 second pause, it closes the HS window and calls the gameOver method.
		 * If a new high score was not set, this method just immediately calls the gameOver method.*/
		saveHighScore();
		if (setNewHighScore)
		{
			Label lblNewHS = new Label("New High Score: "+highScore);	//text of high score
			StackPane HSPane = new StackPane();			//create stack pane for highscore
			HSPane.getChildren().add(lblNewHS);			//add high score label to stack pane
			Scene HSScene = new Scene(HSPane, 500, 300);//create scene to display on stage 
			Stage HSStage = new Stage();				//create stage
			MediaPlayer TADA = new MediaPlayer(TaDa);	//create MediaPlayer for TaDa Media object
			TADA.play();								//play tada sound
			HSStage.setScene(HSScene);					//set the HSScene on the stage
			HSStage.show();		
			PauseTransition fiveSecs = new PauseTransition(Duration.millis(5000)); //creating a 5 sec pause
			fiveSecs.setOnFinished(e -> {HSStage.close(); gameOver();}); //when fiveSecs finishes, close stage and run gameOver
			fiveSecs.play();	//play the 5 sec pause
		}
		else
			gameOver();
	}
	
	public static void gameOver()
	{
		/* This method changes the pane where scores/moves is displayed to show GAME OVER then 
		 * closes the program 2 seconds later.*/
		Scores.getChildren().clear();						//clears out previous entries
		Scores.getChildren().add(new Label("GAME OVER"));	//replaces with GAME OVER
		
		PauseTransition GameOver = new PauseTransition(Duration.millis(2000)); //Create a 2 sec PauseTransition
		GameOver.setOnFinished(e -> {Platform.exit();});	//event on PauseTransition finish closes the program
		GameOver.play();	//play the PauseTransition
	}
	
	public static void undoMove()
	{
		/* This move first checks to see if undoBuffer is greater than 0, because if it is 0 that indicates there are no
		 * moves to undo. If undoBuffer == 0 the method ends without doing anything. The method goes cell by cell writing 
		 * the entries from the latest undoBoard into the game board. Then it sets the score equal to the last score before
		 * the move that we are trying to undo.Then it decrements the undoPointer, unless it is already at zero in which case 
		 * the methods sets undoPointer to 9. If undoBuffer is > 0, it is decremented so the program knows there is one fewer
		 * available undo move. Finally it decrements movesMade and refreshes the board.*/
		if (undoBuffer > 0 )
		{
			for (int Col = 0; Col < 4; Col++)
			{
				for (int Row = 0; Row < 4; Row++)
				{
					/* If the undoPointer is at 0, we need to look at position 9 explicitly because undoPointer-1
					 * will cause an ArrayOutOfBoundsException to occur.*/					
					if (undoPointer == 0)
					{	
						board[Row][Col] = undoBoards[9][Row][Col];
						score = undoScores[9];
					}
					else
					{	
						board[Row][Col] = undoBoards[undoPointer-1][Row][Col];
						score = undoScores[undoPointer-1];
					}	
				}
			}
			if (undoPointer == 0)
				undoPointer = 9;
			else
				undoPointer--;
			if(undoBuffer > 0)
				undoBuffer--;
			movesMade--;
			refreshBoard();
		}
	}
	
	public static void saveGame()
	{
		/* This method creates an ObjectOutputStream and writes the undoBoards, undoScores, board, score, and 
		 * movesMade to a file 2048.dat. Then it closes the output. None of the exceptions should occur, so the
		 * catch blocks are left empty.*/
		try
		{
			ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream("2048.dat"));
			output.writeObject(undoBoards);
			output.writeObject(undoScores);
			output.writeObject(board);
			output.writeInt(score);
			output.writeInt(movesMade);
		
			output.close();
		}
		catch (FileNotFoundException ex)
		{
			//never happens
		}
		catch (IOException ex)
		{
			//never happens
		}
	}	
	
	public static void saveHighScore()
	{
		/* This method writes the highScore to a file so that it can be retained between game sessions.
		 * It creates an ObjectOutputStream and writes highScore to it as an int.*/
		try
		{
			ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream("HighScore.dat"));
			output.writeInt(highScore);
			
			output.close();
		}
		catch (FileNotFoundException ex)
		{
			//never happens
		}
		catch (IOException ex)
		{
			//never happens
		}
	}
	
	public static void loadGame()
	{
		/* This method checks if 2048.dat exists and has contents. If it does, the method creates an ObjectInputStream
		 * and reads undoBoards, undoScores, board, score, and movesMade from it. Then it refreshes the board and closes
		 * the input. The checked exceptions should never occur so the catch blocks can be left empty.*/
		if (gameSave.exists() && gameSave.length() > 0)
		{
			try
			{
				ObjectInputStream input = new ObjectInputStream(new FileInputStream("2048.dat"));
				undoBoards = (int[][][])input.readObject();
				undoScores = (int[])input.readObject();
				board = (int[][])input.readObject();
				score = input.readInt();
				movesMade = input.readInt();
				refreshBoard();
				input.close();
			}
			catch (ClassNotFoundException ex)
			{
				//never happens because we are within the class where the objects are found as static variables
			}
			catch (FileNotFoundException ex)
			{
				//never thanks to if statement
			}
			catch (IOException ex)
			{
				//never happens
			}
		}
	}

	public static void getHighScore()
	{
		/* This method reads in the previous highScore if it exists. It checks to see that the highScore file
		 * exists and has contents. If it does, this method loads an int from the file into the highScore variable. */
		if (hsSave.exists() && hsSave.length() > 0)
		{
			try
			{
				ObjectInputStream input = new ObjectInputStream(new FileInputStream("HighScore.dat"));
				highScore = input.readInt();
								
				input.close();
			}
			catch (FileNotFoundException ex)
			{
				//never happens thanks to if statement
			}
			catch (IOException ex)
			{
				//never happens
			}
		}
		else highScore = 0; //if there is no saved highscore, set highScore to zero.
	}
	
	public static void isGameOver()
	{
		/* This method checks the board to see if any moves are possible. If no moves are possible it runs the 
		 * quitGame method which ends the game. */
		
		if (!canMoveLeft() && !canMoveRight() && !canMoveUp() && !canMoveDown())
			quitGame();
	}
	
	public static void showHelp()
	{ 
		/* This method creates a giant string and throws it into a label, into a pane, into a scene, onto a stage to \
		 * be displayed when the user requests help.*/
		FlowPane help = new FlowPane();
		String helpInstructions = new String("Welcome to 2048. You can move the board by pressing the arrow keys. \n");
		helpInstructions = helpInstructions + "When 2 numbers that are equal slide into each other, they are merged. \n";
		helpInstructions = helpInstructions + "The goal of the game is to create a cell containing the number 2048. \n";
		helpInstructions = helpInstructions + "You can press ctrl+Z to undo up to 10 moves. Alt+H or the HELP button \n";
		helpInstructions = helpInstructions + "open this help screen. Alt+S or the SAVE button save the current game.\n";
		helpInstructions = helpInstructions + " Alt+L or the LOAD button load a save game if one exists. Alt+X or the \n";
		helpInstructions = helpInstructions + "EXIT button will exit the game";
		Label lblHelp = new Label(helpInstructions);
		help.getChildren().add(lblHelp);
		Scene helpScene = new Scene(help, 400, 160);
		Stage HelpWindow = new Stage();
		HelpWindow.setResizable(false);
		HelpWindow.setScene(helpScene);
		HelpWindow.show();
	}
	
	public static void isWinner()
	{
		/* This method only runs most of its functions if the gameWon flag is false and there is a 2048 on the board.
		 * If the game hasn't yet been won and there is a 2048, it sets the gameWon flag to true and then opens a window
		 * congratulations the user and asks if they would like to continue playing. If the user wants to keep playing
		 * the window closes and the game goes on. If they user doesn't wish to keep playing, the quitGame method is called.*/
		if (!gameWon) //game not yet won
		{
			for (int row = 0; row < 4; row++)		
				for (int col = 0; col < 4; col++)
					if (board[row][col] == 2048)	//search the board for 2048 and if found..
					{
						gameWon = true;				
						BorderPane winningGame = new BorderPane();	//BPane to place buttons and text in.
						Label youWon = new Label("Congratulations, you won! Would you like to continue playing?");
						winningGame.setTop(youWon); 		//set too to congratulations and prompt to continue
						Button btYes = new Button("Yes");	//create yes and no buttons
						Button btNo = new Button("No");
						winningGame.setLeft(btYes);			//place buttons in the BPane
						winningGame.setRight(btNo);
						Scene win = new Scene(winningGame);	//set the BPane on a scene
						Stage winner = new Stage();			//create a stage to be shown
						winner.setScene(win);				//set the scene on a stage
						winner.show();						//show the stage
						btYes.setOnAction(e -> {winner.close();});		//if yes is clicked, just close window so play goes on
						btNo.setOnAction(e -> {winner.close(); quitGame();});	//if no clicked, close window and end game.
					}
		}
	}
	
	static class Cell extends StackPane
	{
		/* This class Cell creates the individual cells that will go into the main GridPane to show the board. It
		 * extends StackPane because all I really need them to do is hold a number in the center of the cell, have
		 * a background color, and be able to have rounded edges. */
		
		int number;

		Cell(int number)
		{
			/* This constructor receives an integer from the board array and generates the cell that show up in
			 * the GUI. It sets the the constructed cell's number to be equal to the int passed to the constructor.
			 * It sets the cell size to 132x132 pixels then creates a Text object containing the number of the cell
			 * and sets the font.*/
			this.number = number;
			this.setPrefSize(132, 132);
			Label cNum = new Label(""+number);
			cNum.setFont(font);
			cNum.setStyle("-fx-font-weight: BOLD");
			/* This switch statement checks the number of the cell and sends the Cell pane to have the number displayed
			 * (if it isn't zero), the background color set, and the corners rounded. */
			switch(number)
			{
				case 0: 	this.setStyle("-fx-background-color: #CCC0B3;-fx-background-radius: 8;");	break;
				case 2: 	this.getChildren().add(cNum);
							this.setStyle("-fx-background-color: #EEE4DA;-fx-background-radius: 8;");	break;
				case 4: 	this.getChildren().add(cNum);
							this.setStyle("-fx-background-color: #EDE0C8;-fx-background-radius: 8;");	break;
				case 8: 	this.getChildren().add(cNum);
							this.setStyle("-fx-background-color: #F2B179;-fx-background-radius: 8;");	
							cNum.setTextFill(Color.WHITE);												break;
				case 16: 	this.getChildren().add(cNum);
							this.setStyle("-fx-background-color: #F59563;-fx-background-radius: 8;");	
							cNum.setTextFill(Color.WHITE);												break;
				case 32: 	this.getChildren().add(cNum);
							this.setStyle("-fx-background-color: #F67C5F;-fx-background-radius: 8;");	
							cNum.setTextFill(Color.WHITE);												break;
				case 64: 	this.getChildren().add(cNum);
							this.setStyle("-fx-background-color: #F65E3B;-fx-background-radius: 8;");	
							cNum.setTextFill(Color.WHITE);												break;
				case 128: 	this.getChildren().add(cNum);
							this.setStyle("-fx-background-color: #EDCF90;-fx-background-radius: 8;");	
							cNum.setTextFill(Color.WHITE);												break;
				case 256: 	this.getChildren().add(cNum);
							this.setStyle("-fx-background-color: #EDCC61;-fx-background-radius: 8;");	
							cNum.setTextFill(Color.WHITE);												break;
				case 512: 	this.getChildren().add(cNum);
							this.setStyle("-fx-background-color: #EDC850;-fx-background-radius: 8;");	
							cNum.setTextFill(Color.WHITE);												break;
				case 1024: 	this.getChildren().add(cNum);
							this.setStyle("-fx-background-color: #EDC53F;-fx-background-radius: 8;");	
							cNum.setTextFill(Color.WHITE);												break;
				case 2048: 	this.getChildren().add(cNum);
							this.setStyle("-fx-background-color: #EDC22E;-fx-background-radius: 8;");	
							cNum.setTextFill(Color.WHITE);												break;
				default: 	this.getChildren().add(cNum);
							this.setStyle("-fx-background-color: #3C3A32;-fx-background-radius: 8;");	
							cNum.setTextFill(Color.WHITE);												break;
			}
		}
	}
}
