// This program facilitates playing the sliding tile puzzle game known as fifteen-square.
// It makes use of methods for displaying the board, setting up an initial solvable board,
// shuffling, determining if the board is solved, finding where the blank tile is located 
// on the board, moving tiles, undoing the last move, displaying help, saving a current 
// game state, and loading a previously saved game.
//
// Author: Martin Amos
// Course: EECS 1510
// Instructor: Dr. L. Thomas
// Date: 14 March 2018

import java.util.Scanner;
import java.io.*;

public class FifteenSquare
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
	static int movesMade = 0, gamesWon = 0, gamesPlayed;
	static Scanner input = new Scanner(System.in);
	static char lastMove = ' ', playerSelection = ' ', playAgain = 'Y';
	static File savedGameFile = new File("mySavedGameFile.txt");
	public static void main(String[] args)
	{
		/* The do...while loop below contains one play through of the game. It increments the games played counter
		 * sets the board to a solvable state, then performs 10000 random moves to shuffle. The board is then
		 * displayed for the player and they are prompted to enter a move or command which is contained in the 
		 * loop within this one. If the game terminated because the player solved the board, they will be prompted
		 * if they would like to play again. If they player answers Y, this loop repeats. */
		do
		{
			gamesPlayed++;
			initializeBoard();
			shuffle();
			displayBoard();
			/* The do...while loop below is one turn in the game. It terminates when the board has been solved
			 * or the player enters Q indicating they want to quit.  */
			do
			{
				System.out.print("Please enter a direction to move the blank tile (U,D,L,R) or another command:");
				playerSelection = input.next().toUpperCase().charAt(0);
				/* This switch statement contains almost all of the selections the player will ever have to enter. 
				 * L for Left, U for Up, D for Down, R for Right, H for Help, Z for Undo, S for Save, G for Get(Load)
				 * and Q for Quit. The default case informs the player that their selection is not valid and lets them
				 * know how to access help.*/
				switch (playerSelection)
				{
					case 'L':	moveLeft();			displayBoard();		break;	// The board is not displayed after help so 
					case 'U':	moveUp();			displayBoard();		break;	// that is doesn't shove help off the user's
					case 'D':	moveDown();			displayBoard();		break;	// screen. The board is also not displayed 
					case 'R':	moveRight();		displayBoard();		break;	// if the user chooses to quit, because it 
					case 'H':	displayHelp();							break;  // is assumed they are no longer interested
					case 'Z': 	undoLast();			displayBoard();		break;  // in the game if they have chosen to quit.
					case 'S': 	saveGame();			displayBoard();		break;
					case 'G':	loadSavedGame();	displayBoard();		break;
					case 'Q': 	playAgain = 'N';						break;
					//case 'A':	almostSolved();		displayBoard();		break;	//this line puts the board into a nearly solved state
					default :   //anything that isn't a valid command falls here
					{
						System.out.println("That is not a valid command. Enter H at any time for help.");
						displayBoard();
					}
				}
				System.out.println(" " + lastMove);
			}		//closes loop containing 1 turn
			while(!isSolved() && playerSelection != 'Q'); //if the board is solved or the player chose to quit, the game is over
			if (isSolved()) //if the game ended by being solved, the player may want to play again.
			{
				gamesWon++;
				System.out.print("Congratulations, you solved the puzzle in " + movesMade + " moves. Would you like to play again? (Y/N)");
				playAgain = input.next().toUpperCase().charAt(0);
			}
		}	//closes loop containing 1 game
		while(playAgain == 'Y');
	} 		//closes main

	public static void displayBoard()
	/* This method prints out the board state. It begins by printing the top edge of the board. Then it prints out
	 * a line of number from the board array with the appropriate formatting using the line drawing characters. It 
	 * check to see if a number is the last in the row because the line drawing is different at that position. After
	 * printing a number line, it prints the horizontal line characters to separate the row of tiles from the next.
	 * This also check if it has printed the last line of the array because the bottom edge of the board is drawn
	 * differently from the lines separating rows of the array.*/
	{
		System.out.println(" "+TL+DH+DH+DH+DH+TC+DH+DH+DH+DH+TC+DH+DH+DH+DH+TC+DH+DH+DH+DH+TR); // board top edge
		for (int row = 0; row < 4; row++) // row selector
		{
			System.out.print(" "+DV);	
			for (int col = 0; col < 4; col++) //column selector
			{
				System.out.print(" ");
				if (board[row][col]==0) //check for blank tile
					System.out.print("  ");
				else
					System.out.printf("%2d", board[row][col]);
				if (col==3) // check for right edge
					System.out.println(" "+DV);
				else
					System.out.print(" "+VB);
			}
			if (row < 3) // check if not yet last row 
				System.out.println(" "+LE+HB+HB+HB+HB+SC+HB+HB+HB+HB+SC+HB+HB+HB+HB+SC+HB+HB+HB+HB+RE);
			else
				System.out.println(" "+BL+DH+DH+DH+DH+BE+DH+DH+DH+DH+BE+DH+DH+DH+DH+BE+DH+DH+DH+DH+BR);
		}
	} // end of display board method
	
	public static void initializeBoard()
	/* This method sets the board to an initial state that is solvable before we begin our shuffle. 
	 * It starts on the first row, and puts the numbers 0-4 into the 4 positions of the row then advances
	 * the next now. At the end it puts 15 into the top left corner and the blank square into the lower
	 * right corner because this is a solvable initial state.*/
	{
		for (int row = 0, num = 0; row < 4; row++) //row selector
		{
			for (int col = 0; col < 4; col++, num++) //column selector
			{
				board[row][col] = num; //places numbers
			}
		}
		board[0][0] = 15; //putting 15 and 0 into the correct positions
		board[3][3] = 0;
	}

	public static boolean moveLeft()
	/* This method moves the blank position on the board to the left. First it retrieves the row/column of the 
	 * blank from the row()/col() methods. Then it checks to see the blank is on the left edge of the board, if 
	 * it is the move cannot be completed so the method returns false. If the move can be made, the number 1
	 * column to the left of blank is copied into blank's current position and where that number was is replaced
	 * with a zero. The method then sets lastMove to L, increments the move counter, and returns true if the move
	 * was properly completed.*/
	{
		int col = col();
		int row = row();
		if (col == 0)
			return false;
		if (col > 0)
		{
			board[row][col] = board[row][col-1];
			board[row][col-1] = 0;
			lastMove = 'L';
			movesMade++;
			return true;
		}
		return true; //never encountered, but needed for compiler
	}
	
	public static boolean moveRight()
	/* This method moves the blank position on the board to the right. First it retrieves the row/column of the 
	 * blank from the row()/col() methods. Then it checks to see the blank is on the right edge of the board, if 
	 * it is the move cannot be completed so the method returns false. If the move can be made, the number 1
	 * column to the right of blank is copied into blank's current position and where that number was is replaced 
	 * with a zero. The method then sets lastMove to R, increments movesMade,  and returns true if the move was 
	 * properly completed.*/
	{
		int col = col();
		int row = row();
		if (col == 3)
			return false;
		if (col < 3)
		{	
			board[row][col] = board[row][col+1];
			board[row][col+1] = 0;
			lastMove = 'R';
			movesMade++;
			return true;
		}
		return true; //never encountered, but needed for compiler
	}
	
	public static boolean moveUp()
	/* This method moves the blank position on the board up. First it retrieves the row/column of the 
	 * blank from the row()/col() methods. Then it checks to see the blank is on the top edge of the board, if 
	 * it is the move cannot be completed so the method returns false. If the move can be made, the number 1
	 * row above the blank is copied into blank's current position and where that number was is replaced 
	 * with a zero. The method then sets lastMove to U, increments movesMade, and returns true if the move was 
	 * properly completed.*/
	{
		int col = col();
		int row = row();
		if (row == 0)
			return false;
		if (row > 0)
		{
			board[row][col] = board[row-1][col];
			board[row-1][col] = 0;
			lastMove = 'U';
			movesMade++;
			return true;
		}
		return true; //never encountered, but needed for compiler
	}
	
	public static boolean moveDown()
	/* This method moves the blank position on the board down. First it retrieves the row/column of the 
	 * blank from the row()/col() methods. Then it checks to see the blank is on the bottom edge of the board, if 
	 * it is the move cannot be completed so the method returns false. If the move can be made, the number 1
	 * row below the blank is copied into blank's current position and where that number was is replaced 
	 * with a zero. The method then sets lastMove to D, increments movesMade, and returns true if the move was 
	 * properly completed.*/
	{
		int col = col();
		int row = row();
		if (row == 3)
			return false;
		if (row < 3)
		{
			board[row][col] = board[row+1][col];
			board[row+1][col] = 0;
			lastMove = 'D';
			movesMade++;
			return true;
		}
		return true; //never encountered, but needed for compiler
	}
	
	public static void shuffle()
	/* This method attempts to perform 10000 random moves as a means to shuffle the board position. For each move a random int
	 * between 0 and 3 is generated. Each number is mapped to a shuffle direction. When the loop attempts to perform a move, it checks that the 
	 * move is not the opposite of the last move and that the move method returns true (indicating that it is not against an
	 * edge). If those 2 conditions are met, the move is performed and stored in the variable lastShuffle and the count is
	 * incremented. After all shuffles are performed, movesMade is set to zero because shuffle only runs at the start of a 
	 * game and lastMove is set to ' ' so players cannot try to undo as the first move of a game.*/
	{
		int lastShuffle = -1, count = 0;  //lastShuffle initialized at -1 to be sure that any direction is available for first move
		while (count < 10000)
		{
			int rand = (int)(Math.random()*4);
			if (rand == 0 && lastShuffle != 2 && moveUp())	//if rand=0, last move wasn't down, and it can move up, it will
			{	
				lastShuffle = 0;
				count++;
			}
			if (rand == 1 && lastShuffle != 3 && moveRight())//if rand=1, last move wasn't left, and it can move right, it will
			{
				lastShuffle = 1;
				count++;
			}
			if (rand == 2 && lastShuffle != 0 && moveDown())//if rand=2, last move wasn't up, and it can move down it will
			{
				lastShuffle = 2;
				count++;
			}
			if (rand == 3 && lastShuffle != 1 && moveLeft())//if rand=3, last move wasn't right, and it can move left it will
			{
				lastShuffle = 3;
				count++;
			}
		}
		lastMove = ' ';
		movesMade = 0;
	}
	
	public static int findBlank()
	/* This method finds the empty tile by looking at each position on the board and marking where it finds zero.*/
	{
		int blank = 0;
		if (board[0][0] == 0)	blank = 0;
		if (board[0][1] == 0)	blank = 1;
		if (board[0][2] == 0)	blank = 2;
		if (board[0][3] == 0)	blank = 3;
		if (board[1][0] == 0)	blank = 4;
		if (board[1][1] == 0)	blank = 5;
		if (board[1][2] == 0)	blank = 6;
		if (board[1][3] == 0)	blank = 7;
		if (board[2][0] == 0)	blank = 8;
		if (board[2][1] == 0)	blank = 9;
		if (board[2][2] == 0)	blank = 10;
		if (board[2][3] == 0)	blank = 11;
		if (board[3][0] == 0)	blank = 12;
		if (board[3][1] == 0)	blank = 13;
		if (board[3][2] == 0)	blank = 14;
		if (board[3][3] == 0)	blank = 15;
		return blank;
	}
		
	public static int row()
	/* This method takes the integer position of the blank on the board from findBlank, determines what row
	 * it is in, and returns that row. */
	{
		if (findBlank() == 0 || findBlank() == 1 || findBlank() == 2 || findBlank() == 3)
			return 0;
		if (findBlank() == 4 || findBlank() == 5 || findBlank() == 6 || findBlank() == 7)
			return 1;
		if (findBlank() == 8 || findBlank() == 9 || findBlank() ==10 || findBlank() == 11)
			return 2;
		if (findBlank() ==12 || findBlank() ==13 || findBlank() ==14 || findBlank() == 15)
			return 3;
		return -1; //this line never reached, but the compiler complained that all my returns were inside "if"s
	}
		
	public static int col()
	/* This method takes the integer position of the blank on the board from findBlank, determines what column 
	 * it is in, and returns that column.*/
	{
		if (findBlank() == 0 || findBlank() == 4 || findBlank() == 8 || findBlank() == 12)
			return 0;
		if (findBlank() == 1 || findBlank() == 5 || findBlank() == 9 || findBlank() == 13)
			return 1;
		if (findBlank() == 2 || findBlank() == 6 || findBlank() ==10 || findBlank() == 14)
			return 2;
		if (findBlank() == 3 || findBlank() == 7 || findBlank() ==11 || findBlank() == 15)
			return 3;
		return -1; //this line never reached, but the compiler complained that all my returns were inside "if" statements
	}

	public static void almostSolved()
	/* This method puts the board into a state that is one move away from solved and increments the move counter
	 * I used it solely for testing purposes and the line of the switch statement that calls to it has been commented
	 * out, but could be reactivated for further testing.*/
	{
		board[0][0] = 1;	board[0][1] = 2;	board[0][2] = 3;	board[0][3] = 4;
		board[1][0] = 5;	board[1][1] = 6;	board[1][2] = 7;	board[1][3] = 8;
		board[2][0] = 9;	board[2][1] = 10;	board[2][2] = 11;	board[2][3] = 12;
		board[3][0] = 13;	board[3][1] = 14;	board[3][2] = 0;	board[3][3] = 15;
		movesMade++;
	}
	
	public static boolean isSolved()
	/* This method checks the board state to see if the solution has been reached.			1	2	3	4
	 * The method begins by assuming that the board is in a solved state. 					5	6	7	8
	 * It looks at the board array and compares is to the values as seen on the right. 		9	10	11	12
	 * If any position in the array contains an incorrect value, solved is set to false.	13	14	15	0
	 * After the "if" statement completes, the boolean variable solved is returned to the method caller. */
	{
		boolean solved = true;
		if (board[0][0] !=  1 || board[0][1] !=  2 || board[0][2] !=  3 || board[0][3] !=  4 ||
			board[1][0] !=  5 || board[1][1] !=  6 || board[1][2] !=  7 || board[1][3] !=  8 ||
			board[2][0] !=  9 || board[2][1] != 10 || board[2][2] != 11 || board[2][3] != 12 ||
			board[3][0] != 13 || board[3][1] != 14 || board[3][2] != 15 || board[3][3] !=  0 )
			solved = false; //if anything is in the wrong position, solved is set to false
		return solved;
	}

	public static void undoLast()
	/* This method undoes the last move made by the user, if they have made a move since the game began or since loading
	 * a save.  If the last move the player made was to undo a previous move, the player is informed that they cannot 
	 * undo two times in a row. The undo move is made by moving in the opposite direction of the lastMove variable. 
	 * Once a move to undo is made, the lastMove variable is set to Z to represent that undo was the last move. 
	 * As a philosophical choice of game design, I believe undo should be counted as a move, but if the requirements
	 * were to indicate specifically otherwise, I would just add a decrement to the move counter after 
	 * setting lastMove to Z.*/
	{
		if (lastMove != ' ') //checks if any moves have been made this game / since loading a save
		{
			if (lastMove == 'Z')
				System.out.println("You cannot undo twice in a row");
			if (lastMove == 'D')	
			{
				moveUp();
				lastMove = 'Z';
			}
			if (lastMove == 'U')	
			{
				moveDown();
				lastMove = 'Z';
			}
			if (lastMove == 'L') 	
			{
				moveRight();
				lastMove = 'Z';
			}
			if (lastMove == 'R')	
			{	
				moveLeft();
				lastMove = 'Z';
			}	
		}
		else
			System.out.println("You cannot undo right now.");
	}
	
	public static void displayHelp()
	/* This method displays for the player everything they might need to know to interact with the game. It also tells the
	 * player how many games have been played, how many games have been won, and home many moves have been made in the 
	 * current game. As a choice, I decided that once a game board has been generated, it should count as played. As a 
	 * result, games won will always be at least 1 less than games played when displayed to the user in help.*/
	{
		System.out.println(" 1	 2	 3	 4  Welcome to the classic game Fifteen Square. The goal of this game is to move");
		System.out.println(" 5	 6	 7	 8  the numbered tiles so that they are in ascending order left to right then top");
		System.out.println(" 9	10	11	12  bottom, as seen on the left. When the blank tile is moved, it swaps positions");
		System.out.println("13 	14	15	    with the tile in the direction which it moved.");
		System.out.println("You can move the blank tile Up, Down, Left, or Right by entering U, D, L, or R respectively.");
		System.out.println("You can enter Z to undo the last move, but it cannot be done twice in a row and you cannot use");
		System.out.println("undo as the first move in a new game or immediately after loading a saved game.");
		System.out.println("You can enter S to Save your current game and enter G to Get a previously saved game.");
		System.out.println("If you need to see this screen again you may enter H for Help. If you want to Quit out");
		System.out.println("of the game completely, enter Q.");
		System.out.println("Games Played: " + gamesPlayed); 	//Informs player of games played, games won, and moves made.
		System.out.println("Games Won: " + gamesWon);
		System.out.println("Moves Made This Game: " + movesMade);
	}
	
	public static void saveGame()
	/* This method creates a PrintWriter and attaches it to our savedGameFile object and creates the file if it does not
	 * exist. It then writes modesMade, gamesPlayed, gamesWon, and the board array to the file. */
	{
		PrintWriter output;
		/* This try/catch block is needed to handle a possible FileNotFoundException, but if the file doesn't yet exist, 
		 * this creates it anyway, so the exception should never be encountered.*/
		try 
		{
			output = new PrintWriter(savedGameFile); //attaches PrintWriter to savedGameFile
			output.println(movesMade);
			output.println(gamesPlayed);
			output.println(gamesWon);
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
	}	

	public static void loadSavedGame()
	/* This method checks to see if a save game file exists and has contents. If it does, it creates a scanner linked to
	 * the save file object. It reads the first 3 ints from the file as movesMade, gamesPlayed, and gameWon, then the next
	 * 16 ints are read as the entries of the slider puzzle board. If there isn't yet a save file or it contains nothing,
	 * the user is informed that there isn't yet a saved game. */
	{
		Scanner loader;
		if (savedGameFile.exists() && savedGameFile.length() > 0)	//make sure file exists
		{
			/* try/catch needed to handle the possibility of a FileNotFoundException, but the if statement prevents us
			 * from ever encountering that exception, so the catch block can be empty.*/
			try			 
			{
				loader = new Scanner(savedGameFile); //attaches scanner to savedGameFile object
				movesMade = loader.nextInt();
				gamesPlayed = loader.nextInt();
				gamesWon = loader.nextInt();
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
	}
}