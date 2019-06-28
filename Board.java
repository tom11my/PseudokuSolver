import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.List;
import java.awt.RenderingHints;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Timer;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Board extends JPanel {
	
	private int [][] nums;
	
	public final int WIDTH = 800;
	
	public final int HEIGHT = 800;
		
	//determines whether a single move is legal 
    //ASSUMES it is determined that spot is a 0 (or empty)
	public boolean isLegalMove (int row, int col, int num) {
	        //check row
	        for(int c = 0; c < nums[0].length; c++) {
	            if(num == nums[row][c] && c != col)
	                return false;
	        }
	        //check col
	        for(int r = 0; r < nums.length; r++) {
	            if(num == nums[r][col] && r != row)
	                return false;
	        }
	        //check local box
	        int boxStartY = (row/3)*3;
	        int boxStartX = (col/3)*3;
	        for(int y = 0; y < 3; y++) {
	            int sumY = boxStartY + y;
	            for(int x = 0; x < 3; x++) {
	                int sumX = boxStartX + x;
	                if(nums[sumY][sumX] == num && (sumY != row || sumX != col))
	                    return false;
	            }
	        }
	        return true;
	    }
	
	//Initial Solution - brute force - "backtracking"
	public void solve1 () throws InterruptedException {
		//list of locations where numbers were "guessed"
		ArrayList<Loc> previousLocs = new ArrayList<Loc>();
		int counter = 1;
		for(int row = 0; row < nums.length; row++) {
			for(int col = 0; col < nums[0].length; col++) {
				if(nums[row][col] == 0) {
					if(counter == 10) {
						Loc previous = previousLocs.get(previousLocs.size()-1);
						previousLocs.remove(previousLocs.size()-1);
						row = previous.getY();
						col = previous.getX();
						counter = nums[row][col] +1;
						nums[row][col] = 0;
						col--;
					}
					else if(isLegalMove(row, col, counter)) {
						nums[row][col] = counter;
						previousLocs.add(new Loc(col, row));
						counter = 1;
					}	
					else {
						counter++;
						col--;
					}
					//Thread.sleep(1);
				}
			}
		}
	}
	
	
	//Solves by checking whether a number exists as a single possibility in an entire row or column
	//FUTURE: can add in local box check so that more complex puzzles can be solved
	public void solve2 () throws InterruptedException {
		//for each empty box, put the legal possibilities into an array list
		//FUTURE: improve efficiency by focusing on which arrayLists of possibility you needed to update 
		//based on location of the previous addedNumber
		ArrayList<Integer>[][] pos = new ArrayList[9][9];
		for(int row = 0; row < nums.length; row++) {
			for(int col = 0; col < nums[0].length; col++) {
				if(nums[row][col] == 0) {
					pos[row][col] = new ArrayList<Integer>();
					for(int counter = 1; counter < 10; counter++) {
						if(isLegalMove(row, col, counter))
							pos[row][col].add(counter);
					}
				}		
			}
		}
		/*for each number in each array list of possibilities in each box, check whether it 
		 * appears in an array list of possibilities in a different box of the same row, col, OR local box.
		 * If not, add the number on the board through int[][]nums
		 */
		solver:
		for(int row = 0; row < nums.length; row++) {
			for(int col = 0; col <nums[0].length; col++) {
				//Here - accessing a box of 9 x 9 boxes
				if(nums[row][col] == 0) {
					if(pos[row][col].size() == 1) {
						
						nums[row][col] = pos[row][col].get(0);
						solve2();
						break solver;
					}
					for(Integer current: pos[row][col]) {
						//Here - accessing an Integer, 1-9, that one possible legal move in an array of legal moves from an empty box
						boolean appearsTwiceInCol = false;
						boolean appearsTwiceInRow = false;
						//check if current appears in pos for a column (col). If not, add to nums. If so, break
						colChecker:
						for(int r = 0; r < nums.length; r++) {
							if(pos[r][col] != null && r != row) {
								for(Integer checker: pos[r][col]) {
									if(current == checker) {
										appearsTwiceInCol = true;
										break colChecker;
									}
								}
							}
						}
						if(!appearsTwiceInCol) {
							nums[row][col] = current;
							solve2();
							break solver;
						}
						//In case colChecker didn't find if it appears twice in col, we try rowChecker
						if (appearsTwiceInCol) {
							//check if current appears in pos for a row. If not, add to nums. If so, break
							rowChecker:
							for(int c = 0; c < nums[0].length; c++) {
								if(pos[row][c] != null && c != col) {
									for(Integer checker: pos[row][c]) {
										if(current == checker) {
											appearsTwiceInRow = true;
											break rowChecker;
										}
									}
								}
							}
						}
						if(!appearsTwiceInRow) {
							nums[row][col] = current;
							solve2();
							break solver;
						}
					}
				}
			}
		}
	}
	
	public Board() {
		nums = new int [9][9];
		nums = this.generateLegalBoard1();
	}
	
	public static void main (String[] args) throws InterruptedException, FileNotFoundException {
		System.out.println("Start!");
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Board b = new Board();
		frame.add(b);
		
		frame.pack();
		frame.setVisible(true);
		b.repaint();
		Thread.sleep(5000);
		b.solve1();
		b.repaint();
		//reads from file containing a million puzzles
		Scanner s = new Scanner(new File("sudoku.csv"));
		s.nextLine();
		for(int x = 1; x < 1000000; x++) {
			String str = s.nextLine().split(",")[0];
			b.setNums(b.strToArr(str));
			b.solve1();
			//b.solve2();
			b.repaint();
			//Thread.sleep(100);
		}
		//For playing an actual game, rather than solving it
		/*
		Scanner s = new Scanner(System.in);
		while(true) {
			b.repaint();
			String input = s.nextLine();
			String modInput = removeSpaces(input);
			try {
				b.update(modInput);
			}
			catch (NumberFormatException d) {
                System.out.println("Incorrect Format. Example: \"Index y, Index x, Value\"");
            }
		}
		*/
	}
	
	private int[][] strToArr(String str) {
		int[][] hold = new int[9][9];
		for(int row = 0; row < 9; row++) {
			for(int col = 0; col < 9; col++) {
				hold[row][col] = Integer.parseInt(str.substring(row*9+col, row*9 + col+1));
			}
		}
		return hold;
	}

	private void setNums(int[][] i) {
		this.nums = i;
	}

	public void update (String input) {
		int comIndex = input.indexOf(",");
        int comIndex2 = input.lastIndexOf(",");
        int a = -1;
        int b = -1;
        int c = -1;
        
        a = Integer.parseInt(input.substring(0, comIndex));
        b = Integer.parseInt(input.substring(comIndex+1, comIndex2));
        c = Integer.parseInt(input.substring(comIndex2 + 1));
        try {
        	nums[a][b] = c;
        }
        catch (ArrayIndexOutOfBoundsException e) {
        	System.out.println("Invalid Bounds");
        }
	}
	
	public Dimension getPreferredSize() {
		return new Dimension (WIDTH, HEIGHT);
	}
	
	public void paintComponent (Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		int width = WIDTH/9;
		int height = HEIGHT/9;
		//start fresh - blank screen
		g2d.setColor(Color.white);
		g2d.fillRect(0, 0,  WIDTH,  HEIGHT);
		g2d.setColor(Color.black);
		//draw column lines (vertical)
		for(int row = 1; row < nums.length; row++) {
			g2d.setStroke(new BasicStroke(row%3==0?3:1));
			g2d.drawLine(row*width, 0, row*width, HEIGHT);
		}
		//draw row lines; could be integrated into double for loop below for efficiency
		for(int col = 1; col <nums[0].length; col++) {
			g2d.setStroke(new BasicStroke(col%3==0?3:1));
			g2d.drawLine(0, col*height, WIDTH, col*height);	
		}
		g2d.setFont(new Font("Times New Roman", Font.PLAIN, 80));
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, 
			    RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
		for(int row = 0; row < nums.length; row++) {
			for(int col = 0; col < nums[0].length; col++) {
				int num = nums[row][col];
				if(num != 0)
					g2d.drawString(Integer.toString(nums[row][col]), col*width + 25, (row+1)*height -10);
			}
		}
	}
	
    public int[][] generateLegalBoard1 () {
    	int [][] hold = new int [9][9];
    	//world's hardest sudoku puzzle
    	hold[0][0] = 8;
    	hold[1][2] = 3;
    	hold[2][1] = 7;
    	
    	hold[1][3] = 6;
    	hold[2][4] = 9;
    	
    	hold[2][6] = 2;
    	
    	hold[3][1] = 5;
    	
    	hold[3][5] = 7;
    	hold[4][5] = 5;
    	hold[4][4] = 4;
    	
    	hold[4][6] = 7;
    	hold[5][7] = 3;
    	
    	hold[6][2] = 1;
    	hold[7][2] = 8;
    	hold[8][1] = 9;
    	
    	hold[7][3] = 5;
    	
    	hold[6][7] = 6;
    	hold[6][8] = 8;
    	hold[7][7] = 1;
    	hold[8][6] = 4;
    	return hold;
    	
    	
    }
    
    public static String removeSpaces (String input) {
        for(int x = input.length()-1; x >= 0; x--) {
            if(input.substring(x, x+1).equals(" "))
                input = input.substring(0, x) + input.substring(x+1);
        }
        return input;
    }
	
}
