// This program recreates the game Connect Four.
// It makes use of methods for displaying the game board, placing player chips into the board,
// checking for wins and draws, and for making sure the user input is acceptable. 
//
// Author: Martin Amos
// Course: EECS 1510
// Instructor: Dr. L. Thomas
// Date: 16 February 2018

import java.util.Scanner;

public class ConnectFour
{
	final static int BOARD_WIDTH = 7; 		// board dimensions
	final static int BOARD_HEIGHT = 6;
	public static void main(String[] args)
	{
		char[][] board = new char[BOARD_HEIGHT][BOARD_WIDTH];	// creates the board
		int player;												// Player turn indicator (0 is Red and 1 is Yellow)
		int choice;												// Will store the active player's column selection
		char playAgain;											// plays game again at end if the value is 'Y'
		Scanner input = new Scanner(System.in);					
		do 						//this do...while loop is the container for the option to play another game after one is finished
		{	
			player = 0;											// sets player turn indicator to 0 so Red begins the game
			for (int i = 0; i < board.length; i++) 				// places the whitespace char in every spot on the board
			{
				for (int j = 0; j < board[i].length; j++) 
				{
					board[i][j] = ' ';
				}
			}
			while (true) 		// One iteration of this loop is one turn in game, a win or draw breaks out out the loop
			{
				while (true)		// loop runs until player gives an acceptable input, breaks out when given a valid board column as input
				{
					displayBoard(board);
					System.out.print((player == 0 ? "Red" : "Yellow") + " player, choose a column to place your chip:");
					while (!input.hasNextInt()) 					//loops until the next input is a valid int
					{
						System.out.print("Please enter a valid integer (0-6):");
						input.next();
					}
					choice = input.nextInt();						//places the integer input into choice
					if (columnIsFull(board, choice)) 
						break;										//escapes the validation loop when the user gives an acceptable input
					System.out.println("You cannot play there.");	
				}
				dropDisk(board, choice, player);  					// put the player's chip at the selected column
				if (isWinner(board)) 								// calls the method that checks to see if anyone has won the game
				{
					displayBoard(board);							// displays board so players can see where four in a row took place
					System.out.println("Congratulations " + (player == 0 ? "Red" : "Yellow") + " player! You won!"); // congratulates the winner
					break;											// breaks out of the loop for the game
				}
				if (checkForDraw(board))							// checks to see if the board is full
				{
					displayBoard(board);							// show the full board that doesn't have four in a row anywhere on it
					System.out.println("This game is a draw."); 	// informs the players that the game is a draw
					break;											// breaks out of the loop for the game
				}
				player++;											// increments the player turn indicator
				player %= 2;										// sets player counter to 0 or 1 (red or yellow respectively)
			}
			System.out.print("Enter \"Y\"  if you would like to play again)"); //prompts players to see if they would like another game
			playAgain = input.next().toUpperCase().charAt(0);		// takes the incoming character, converts to upper case to potentially help user		
		}
		while(playAgain == 'Y');									// if players entered a string beginning with 'Y' or 'y' a new game starts
	}
	public static void displayBoard(char board[][]) 		// Prints out the current board state.
	{
		for (int i = 0; i < board.length; i++) 				// Selects row i
		{
			for (int j = 0; j < board[i].length; j++) 		// Prints every column of row i with board formatting
			{
				System.out.print("|" + board[i][j]);
			}
			System.out.println("|");
		}
		for (int i = 0; i < board[0].length; i++) 			// Board formatting
			System.out.print("--");
		System.out.println("-");
		for (int i = 0; i < board[0].length; i++)			// Column identifiers for players
			System.out.print(" " + i);
		System.out.println(" ");
	}
	public static boolean columnIsFull(char board[][], int selection) // Checks to see if the selected input is a valid play
	{
		if (selection < 0 || selection >= BOARD_WIDTH)	// column entered was outside the board bounds
		{
			System.out.print("That is outside the board bounds. ");	// tells player why they cannot play where they picked
			return false;
		}
		if (board[0][selection] != ' ')		// there is already something placed at the top of the selected column (aka the column is full)
		{
			System.out.print("That column is full. "); // tells the player why they cannot play where they picked
			return false;
		}
		return true;		// if the player picked a column inside the board bounds that is not full, it is valid
	}
	public static boolean isWinner(char board[][]) 		// This method checks the current board state to see if anyone has won
	{
		for (int i = 0; i < board.length; i++)			//This set of loops checks for horizontal wins
		{
			for (int j = 0; j < (board[i].length - 3); j++)		
			{
				if (board[i][j] != ' ' 				&&	// we don't care if there are 4 in a row that are all whitespace
					board[i][j] == board[i][j+1] 	&&	// checks to see if the token one to the right matches
					board[i][j] == board[i][j+2] 	&&	// checks to see if the token two to the right matches
					board[i][j] == board[i][j+3])		// checks to see if the token three to the right matches
					return true;						// if there were 4 in a row, then someone has won the game
			}
		}
		for (int j = 0; j < board[0].length; j++)		// This set of loops checks for vertical wins
		{
			for (int i = 0; i < (board.length - 3); i++)
			{
				if (board[i][j] != ' '	 			&&	// we don't care if there are 4 in a row that are all whitespace
					board[i][j] == board[i+1][j]	&&	// checks to see if the token one below matches
					board[i][j] == board[i+2][j]	&&	// checks to see if the token two below matches
					board[i][j] == board[i+3][j])		// checks to see if the token three below matches
					return true;						// if there were 4 in a row, then someone has won the game
			}
		}
		for (int i = 0; i < (board.length - 3); i++)	// This set of loops checks down-right diagonals for wins
		{
			for (int j = 0; j < (board[i].length - 3); j++)
			{
				if (board[i][j] != ' '				&&	// we don't care if there are 4 in a row that are all whitespace
					board[i][j] == board[i+1][j+1]	&&	// checks to see if the token one diagonally right and below matches
					board[i][j] == board[i+2][j+2]	&&	// checks to see if the token two diagonally right and below matches
					board[i][j] == board[i+3][j+3])		// checks to see if the token three diagonally right and below matches
					return true;						// if there were 4 in a row, then someone has won the game
			}
		}
		for (int i = 0; i < (board.length - 3); i++)	// This set of loops checks down-left diagonals for wins
		{
			for (int j = 3; j < (board[i].length); j++)
			{
				if (board[i][j] != ' '	 			&&	// we don't care if there are 4 in a row that are all whitespace
					board[i][j] == board[i+1][j-1]	&&	// checks to see if the token one diagonally left and below matches
					board[i][j] == board[i+2][j-2]	&&	// checks to see if the token two diagonally left and below matches
					board[i][j] == board[i+3][j-3])		// checks to see if the token three diagonally left and below matches
					return true;						// if there were 4 in a row, then someone has won the game
			}
		}
		return false;						// If there was no win condition found, then the winner check returns false.
	}
	public static boolean checkForDraw(char board[][]) 	// Looks to see if the game board is full, indicating a draw if no winner was found
	{
		boolean draw = true; 						// assume the game is a draw
		for (int j = 0; j < board[0].length; j++)	// check what is in the top row of every column
		{
			if (board[0][j] == ' ')  				// if the top row of any column is not yet filled with a player token...
			{
				draw = false;						// ...then the game is not a draw
				break;								// if we've found one column that proves the game isn't a draw, we have no need to check the others
			}
		}
		return draw;
	}
	public static void dropDisk(char board[][], int selection, int player) // Puts player's chip onto the board
	{
		int row = (BOARD_HEIGHT - 1);						// start at the bottom of the board
		while (board[row][selection] != ' ') 				// if there is already something placed at the bottom,
			row--;											// then we need to check the next row up until we find an empty one
		board[row][selection] = (player == 0 ? 'R' : 'Y');	// once we find the lowest empty position in the selected column, we place the active player's token
	}
}	
