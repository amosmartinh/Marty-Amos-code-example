// This program facilitates playing the game 2048. It uses methods to display the board, to see if a direction can be 
// moved, to move each direction (by squeezing out zeros and combining cells in that direction), to see if the game
// has been won, to place a 2 or 4 in a random open space on the board, to see if the game board is in a state in 
// which no more moves are possible, to display help, to undo the last move, to setup a new game, to save the high
// score, to load the high score.
//
// Author: Martin Amos
// Course: EECS 1510
// Instructor: Dr. L. Thomas
// Date: 12 April 2018

import java.util.Scanner;
import java.io.*;

public class Game2048
{
	// These are all of the line drawing characters I will make use of to print the board
	final static char TL = '\u2554'; // Top Left corner
	final static char DH = '\u2550'; // Double Horizontal bar
	final static char TC = '\u2564'; // Top Center edge (double horizontal bar with single vertical bar on bottom)
	final static char TR = '\u2557'; // Top Right corner
	final static char LE = '\u255F'; // Left Edge (double vertical bar with single horizontal bar on right)
	final static char DV = '\u2551'; // Double Vertical bar
	final static char VB = '\u2502'; // single Vertical Bar
	final static char HB = '\u2500'; // single Horizontal Bar
	final static char SC = '\u253C'; // Single Cross
	final static char BL = '\u255A'; // Bottom Left corner
	final static char BR = '\u255D'; // Bottom Right corner
	final static char BE = '\u2567'; // Bottom Edge (double horizontal bar with single vertical bar on top)
	final static char RE = '\u2562'; // Right Edge (double vertical bar with a single horizontal bar on left)

	static int[][] board = new int[4][4];
	static int[][] undoBoard = new int[4][4];
	static Scanner input = new Scanner(System.in);
	static File savedGameFile = new File("my2048Save.txt");
	static File highScoreSave = new File("my2048HighScore.txt");
	static int score = 0, undoScore = 0, movesMade = 0, highScore = 0;
	static char playerChoice;
	static boolean moveMadeSinceUndo = false, gameWon = false;
	
	public static void main(String[] args)
	{
		/* The main method of this program begins by loading the high score from any previous game play sessions.
		 * Then it enters a do/while loop containing a full play through of 1 game of 2048. In that loop it begins
		 * by setting up the game board and displaying it before entering another do/while loop, this one containing
		 * the turns of the game. It prompts the player for a move, if they player enters a move or another valid
		 * command, it is executed by the switch statement. If after a player moves, their current score is higher
		 * than what is currently stored in highScore, highScore is populated with the value from score and the 
		 * highScore saved. If the game has not yet been won, the game checks to see if the board after the 
		 * player command is now in a winning state. If the game is won, it set gameWon to true and asks the player
		 * if they would like to continue. After each command the board is displayed again. */
		getHighScore();
		do	//this do...while contains a loop of which one iteration equals one complete game
		{
			setupNewGame();
			displayBoard();
			do	//each iteration of this do while loop is one turn in the game.
			{
				System.out.print("Enter move (U/D/L/R) or another command:");
				playerChoice = input.next().toUpperCase().charAt(0);
				switch (playerChoice)
				{
					case 'L':	case '4':	
						if (canMoveLeft())	moveLeft();  //if a directional move is requested and possible, execute it
						else System.out.println("That is not a valid move"); //if the move isn't possible, inform player
						break;
				 	case 'U':	case '8':		
				 		if (canMoveUp())	moveUp();
				 		else System.out.println("That is not a valid move");
				 		break;
		 		 	case 'D':	case '2':
				 		if (canMoveDown())	moveDown();
						else System.out.println("That is not a valid move");
				 		break;
			 		case 'R':	case '6':	
				 		if (canMoveRight())	moveRight();
						else System.out.println("That is not a valid move");
				 		break;
			 	 	case 'S': 	saveGame();			break;
			 	 	case 'G':	loadSavedGame();	break;
			 	 	case 'N':	setupNewGame();		break;
			 	 	case 'H':	displayHelp();		break;
			 	 	case 'Z': 	
			 	 		if (moveMadeSinceUndo)	
			 	 			undoMove();	//if a move has been made since last undo, execute undo
			 	 		else	
			 	 			System.out.println("You cannot undo more than one move"); //if the last move was undo give error
			 	 		break;
			 	 	case 'Q':						break;
			 	 	//case 'W':	board[2][3] = 1024;board[3][3]=1024;displayBoard(); break;
			 	 	/* If anything other than a valid command is entered, tell the player how to access help*/
			 	 	default: System.out.println("That is not a valid command. You may enter H or ? at any time for help.");
				}
				if (score > highScore) //if current score exceeds highScore, set the new highScore and save it
				{
					highScore = score;
					saveHighScore();
				}
				if (!gameWon)	//if the game has not yet been won, check for a win
				{
					if(isWinner())
					{
						gameWon = true; //if game is won, set gameWon to true so the check stops being executed
						System.out.println("Congratulations! You won 2048 in "+ movesMade +" moves with a score of "+ score);
						System.out.println("Would you like to continue playing? (Y/N)");
						playerChoice = input.next().toUpperCase().charAt(0);
						if (playerChoice == 'N') 	//if the player doesn't wish to continue playing after winning
							playerChoice = 'Q';		//treat it as if the user quit the game
					}
				}
				displayBoard(); //last thing done each turn is to display the board.
			} //end of loop in which one iteration is one in game turn
			while(!isGameOver() || playerChoice == 'Q'); //keep looping turns until the player quits or the board is locked
			if (isGameOver())
			{
				if (!gameWon)	//if the board is locked and the game wasn't won, inform the player
					System.out.println("Sorry, you lost. You achieved a final score of "+score+" in "+movesMade+" moves");
				else		//if the board is locked and the game was won, congratulate the player
				{
					System.out.println("Congratulations on winning, but there are no more moves to be made");
					System.out.println("You achieved a final score of "+score+" in "+movesMade+" move");
				}
				System.out.println("Would you like to play again? (Y/N)"); //ask the player if they'd like to play again.
				playerChoice = input.next().toUpperCase().charAt(0);
			}	
		} //end of loop in which one iteration represents one play through of the game
		while (playerChoice == 'Y'); //when a game ends the player is asked if they'd like to play again, if Y a new game starts
	}
	
	public static void displayBoard()
	{
		/* This method prints out the board state. It begins by printing the top edge of the board. Then it prints out
		 * a line of number from the board array with the appropriate formatting using the line drawing characters. It 
		 * cycles through the board printing each row. After a row of cells has printed, it prints a line to separate
		 * the cells from the next row followed by either highScore, movesMade, or score*/
		
		System.out.println(" "+TL+DH+DH+DH+DH+TC+DH+DH+DH+DH+TC+DH+DH+DH+DH+TC+DH+DH+DH+DH+TR); // board top edge
		for (int row = 0; row < 4; row++) // row selector
		{	
			System.out.print(" "+DV);
			for (int col = 0; col < 4; col++) //column selector
			{
				if (board[row][col] == 0) 							//check for blank tile
					System.out.print("    ");
				if (board[row][col] > 0 && board[row][col] < 100) 	//print formatting for numbers under 3 digits
					System.out.printf(" %2d ", board[row][col]);
				if (board[row][col] > 100) 							//print formatting for 3 and 4 digit numbers
					System.out.printf("%-4d", board[row][col]);
				if (col==3) 										//check for right edge
					System.out.println(""+DV);
				else
					System.out.print(""+VB);
			}
			if (row == 0) // checks row printed to see if it needs to print highScore, movesMade, or score 
				System.out.println(" "+LE+HB+HB+HB+HB+SC+HB+HB+HB+HB+SC+HB+HB+HB+HB+SC+HB+HB+HB+HB+RE+"High Score: "+highScore);
			if (row == 1)
				System.out.println(" "+LE+HB+HB+HB+HB+SC+HB+HB+HB+HB+SC+HB+HB+HB+HB+SC+HB+HB+HB+HB+RE+"Moves Made: "+movesMade);
			if (row == 2)
				System.out.println(" "+LE+HB+HB+HB+HB+SC+HB+HB+HB+HB+SC+HB+HB+HB+HB+SC+HB+HB+HB+HB+RE+"Game Score: "+score);
			if (row == 3)
				System.out.println(" "+BL+DH+DH+DH+DH+BE+DH+DH+DH+DH+BE+DH+DH+DH+DH+BE+DH+DH+DH+DH+BR);
		}
	} // end of display board method
 
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
		moveMadeSinceUndo = true;
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
		moveMadeSinceUndo = true;
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
		moveMadeSinceUndo = true;
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
		moveMadeSinceUndo = true;
	}

	public static void saveGame()
	/* This method creates a PrintWriter and attaches it to our savedGameFile object and creates the file if it does not
	 * exist. It then writes score, movesMade, and the board array to the file. */
	{
		PrintWriter output;
		/* This try/catch block is needed to handle a possible FileNotFoundException, but if the file doesn't yet exist, 
		 * this creates it anyway, so the exception should never be encountered.*/
		try 
		{
			output = new PrintWriter(savedGameFile); //attaches PrintWriter to savedGameFile
			output.println(score);
			output.println(movesMade);
			for (int row = 0; row < 4; row++) //cycles through array and prints to file
			{
				for (int col = 0; col < 4; col++)
					output.println(board[row][col]);
			}
			output.close();
		} 
		catch (FileNotFoundException ex)
		{
			//never happens
		}
	} //end of saveGame
	
	public static void saveHighScore()
	{
		/* This method is used to save the highScore in a file so that it can be remembered between multiple
		 * sessions of playing the game. It creates a PrintWriter and attached it to the highScoreSave file
		 * object then writes what is currently in the highScore variable in to the file.*/
		
		PrintWriter output;
		try 	//try...catch block needed for possible exception
		{
			output = new PrintWriter(highScoreSave); //attaches PrintWriter to savedGameFile
			output.println(highScore);	//writes highScore to highScoreSave
			output.close();
		} 
		catch (FileNotFoundException ex)
		{
			//never happens
		}
	}
	
	public static void getHighScore()
	{
		/* This method retrieves high score from a file so it will be saved even between sessions of the game.
		 * First it checks to see if a highScoreSave exists and has contents. If it does, the method loads the 
		 * first int of the file into the highScore variable.*/
		
		Scanner loader;
		if (highScoreSave.exists() && highScoreSave.length() > 0)	//check file exists
		{
			try // try...catch block needed to catch possible exception
			{
				loader = new Scanner(highScoreSave);	//attach scanner to the highScoreSave file
				highScore = loader.nextInt();			//load first int from file into the highScore variable
			}
			catch (FileNotFoundException ex)
			{
				//never happens because of the if statement.
			}
		}
	} //end getHighScore

	public static void loadSavedGame()
	/* This method checks to see if a save game file exists and has contents. If it does, it creates a scanner linked to
	 * the save file object. It reads the first 3 ints from the file as score, movesMade, then the next
	 * 16 ints are read as the board. If there isn't yet a save file or it contains nothing, the user is informed that 
	 * there isn't yet a saved game. */
	{
		Scanner loader;
		if (savedGameFile.exists() && savedGameFile.length() > 0)	//make sure file exists and has contents
		{
			/* try/catch needed to handle the possibility of a FileNotFoundException, but the if statement prevents us
			 * from ever encountering that exception, so the catch block can be empty.*/
			try			 
			{
				loader = new Scanner(savedGameFile); //attaches scanner to savedGameFile object
				score = loader.nextInt();
				movesMade = loader.nextInt();
				for (int row = 0; row < 4; row++)	//cycles through board array and places numbers where they belong
				{
					for (int col = 0; col < 4; col++)
						board[row][col] = loader.nextInt();
				}
				loader.close();
			} 
			catch (FileNotFoundException ex)
			{
				// the if statement makes sure this won't happen
			}
		}
		else
			System.out.println("There is no game saved yet.");
	} //end loadSavedGame

	public static boolean isGameOver()
	{
		/* This method checks the board to see if any moves are possible. If no moves are possible it returns true,
		 * otherwise it returns false. */
		
		if (!canMoveLeft() && !canMoveRight() && !canMoveUp() && !canMoveDown())
			return true;
		return false;
	}

	public static void setupNewGame()
	{
		/* This method prepares the board for a new game by setting all cells to zero, then placing the 2 initial
		 * numbers. The method then sets movesMade and score to zero.*/
		
		for (int row = 0; row < 4; row++)
			for (int col = 0 ; col < 4; col++)
				board[row][col] = 0;
		placeNumbers();
		placeNumbers();
		movesMade = 0;
		score = 0;
	}

	public static void saveForUndo()
	{
		/* This method stores the board's current state in the undoBoard array so that if the player requests an undo
		 * it can be retrieved. It cycles through the board array storing every cell into the undoBoard array. It also
		 * stores the current score so that it can be reset to the score at the time of the undo*/
		
		for (int row = 0; row < 4; row++)
			for (int col = 0; col < 4; col++)
				undoBoard[row][col] = board[row][col];
		undoScore = score;
	}
	
	public static void undoMove()
	{
		/* This method replaces the current board positions with the board as it is stored in undoBoard array and
		 * replaces the current score with the score as it was in the desired undo state. It also decrements the 
		 * move counter. It also sets the boolean variable moveMadeSinceUndo to false so the game knows the last
		 * move was to undo.*/
		
		for (int row = 0; row < 4; row++)
			for (int col = 0; col < 4; col++)
				board[row][col] = undoBoard[row][col];
		score = undoScore;
		moveMadeSinceUndo = false;
		movesMade--;
	}
	
	public static void displayHelp()
	{
		/* This method prints out help for the user so they are aware of the functioning of the game
		 * and all the available features.*/
		
		System.out.println("Welcome to 2048. You can move the board by entering U/D/L/R to slide the numbers.");
		System.out.println("Up/Down/Left/Right may also be entered as 8/2/4/6 respectively on the number pad.");
		System.out.println("When 2 numbers that are equal slide into each other, they are merged. The goal of");
		System.out.println("the game is to create a cell containing the number 2048. At any time you may enter");
		System.out.println("Q to quit the game, N to start a new game, H or ? to display this help screen, Z to");
		System.out.println("undo the last move, S to save the current game, or G to get a saved game");
	}
	
	public static boolean isWinner()
	{
		/* This method check the board to see if any cell is 2048 which would indicate that the player has won 
		 * the game. It returns true if it finds 2048 and if it cycles through the whole board and doesn't find
		 * 2048, it returns false.*/
		for (int row = 0; row < 4; row++)
			for (int col = 0; col < 4; col++)
				if (board[row][col] == 2048) 
					return true;
		return false;
	}
}

