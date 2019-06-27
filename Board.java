import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.List;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Board extends JPanel {
	
	private int [][] nums;
	
	public final int WIDTH = 800;
	
	public final int HEIGHT = 800;
	
	public int countAlternator = 0;
	
	public int fillCount = 0;
	public boolean isLegalMove (int row, int col, int num) {
	        //determines whether a single move is legal 
	        //ASSUMES it is determined that spot is a 0 (or empty)
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
					repaint();
				}
			}
		}
	}
	//A solution based on the process of elimination and restriction
	public void solve2 () throws InterruptedException {
		//for each empty box, put the legal possibilities into an array list
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
		//find needed nums in each column
		ArrayList<Integer>[] neededInCol = new ArrayList[9];
		for(int col = 0; col < nums[0].length; col++) {
			neededInCol[col] = new ArrayList<Integer>();
			for(int counter = 1; counter < 10; counter++) {
				neededInCol[col].add(counter);
			}
			//removing existing numbers from neededInCol
			//cannot remove by index due to multiple removals; must use search
			for(int row = 0; row < nums.length; row++) {
				if(nums[row][col] != 0) {
					neededInCol[col].remove((Integer) nums[row][col]);
				}
			}
		}
		//check possibilities with needed nums in each column
		//for now, only use the case where there is a num in possibilities that 
		//is needed in col and not in any other possibilities
		
		
		//find needed nums in each row
		ArrayList<Integer>[] neededInRow = new ArrayList[9];
		for(int row = 0; row < nums[0].length; row++) {
			neededInRow[row] = new ArrayList<Integer>();
			for(int counter = 1; counter < 10; counter++) {
				neededInRow[row].add(counter);
			}
			//removing existing numbers from neededInCol
			//cannot remove by index due to multiple removals; must use search
			for(int col = 0; col < nums.length; col++) {
				if(nums[row][col] != 0) {
					neededInRow[row].remove((Integer) nums[row][col]);
				}
			}
		}
		//find needed nums in each local box
		//local box is 2D array - 3 x 3
		ArrayList<Integer>[][] neededInLocalBox = new ArrayList[3][3];
		for(int r = 0; r < neededInLocalBox.length; r++) {
			int startY = 3*r;
			for(int c = 0; c < neededInLocalBox[0].length; c++) {
				neededInLocalBox[r][c] = new ArrayList<Integer>();
				for(int counter = 1; counter < 10; counter++) {
					neededInLocalBox[r][c].add(counter);
				}
				int startX = 3*c;
				for(int y = 0; y < 3; y++) {
					int sumY = startY + y;
					for(int x = 0; x < 3; x++) {
						int sumX = startX + x;
						if(nums[sumY][sumX] != 0)
							neededInLocalBox[r][c].remove((Integer)nums[sumY][sumX]);
					}
				}
			}
		}
		loop:
		for(int row = 0; row < nums.length; row++) {
			for(int col = 0; col < nums[0].length; col++) {
				if(nums[row][col] == 0) {
					ArrayList<Integer> curPosibs = pos[row][col];
					//check if array of pos in one box is of length one
					if(curPosibs.size() == 1) {
						fillCount++;
						nums[row][col] = curPosibs.get(0);
						repaint();
						solve2();
						break loop;
						
					}
					for(int curPos: curPosibs) {
					 	//onlyOnce refers to one of multiple possibilities in a single square
						boolean onlyOnce = true;	
						//first, check each column so we increment over rows
					 	
					 	if(countAlternator == 0) {
						 	column:
						 	for(int r = 0; r < nums.length; r++) {
						 		if(nums[r][col] == 0 && r != row) {
						 			for(Integer i: pos[r][col]) {
						 				if(curPos == (int)i) {
						 					countAlternator++;
						 					onlyOnce = false;
						 					break column;
						 				}
						 			}
						 		}
						  	}
					 	}
					 	else {
							//second, check each row so we increment over cols
							System.out.println("reached");
							row:
							for(int c = 0; c < nums[0].length; c++) {
						 		if(nums[row][c] == 0 && c != col) {
						 			for(Integer i: pos[row][c]) {
						 				if(curPos == (int)i) {
						 					countAlternator = 0;
						 					onlyOnce = false;
						 					break row;
						 				}
						 			}
						 		}
						  	}
					 	}
						if(onlyOnce && fillCount < 67) {
							fillCount++;
							nums[row][col] = curPos;
							//Thread.sleep(50);
							repaint();
							solve2();
							break loop;
						}
					 }
				}

			}
		}
		System.out.println(pos[8][7]);
		//find needed nums in row
		//later make this an arrayList
				
		
	
	}
	
	public Board() {
		nums = new int [9][9];
		setUp();
		
		//System.out.println("solved");
	}
	
	public void setUp () {
		//random board is not necessarily legal if >9 nums generated
		//this.generateRandomBoard2();
		nums = this.generateLegalBoard1();
	}
	
	public static void main (String[] args) throws InterruptedException {
		System.out.println("Start!");
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Board b = new Board();
		frame.add(b);
		
		frame.pack();
		frame.setVisible(true);
		b.repaint();
		b.solve2();
		b.repaint();
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
	
	
	
	//generates a random board and checks if it is legal after each addition
    public  int[][] generateRandomBoard2() {
        int [][] rand = new int[9][9];
        int count = 0;
        while(count < 60) {
            int y = (int)(Math.random()*9);
            int x = (int)(Math.random()*9);
            int z = 1 + (int)(Math.random()*9);
            //necessary for isLegalMove method in checking which spots are full
            nums = rand;
            if(nums[y][x] == 0 && isLegalMove(y, x, z)) {
                rand[y][x] = z;
                count++;
            }
        }
        return rand;
        
    }
    
    public int[][] generateLegalBoard1 () {
    	int [][] hold = new int [9][9];
    	hold[0][0] = 2;
    	hold[1][0] = 9;
    	hold[0][1] = 1;
    	
    	hold[2][3] = 7;
    	hold[2][4] = 5;
    	hold[0][4] = 4;
    	
    	hold[0][7] = 3;
    	hold[2][8] = 4;
    	
    	hold[5][0] = 7;
    	hold[5][1] = 2;
    	
    	hold[5][3] = 5;
    	hold[4][4] = 8;
    	
    	hold[4][6] = 4;
    	hold[3][8] = 6;
    	hold[4][8] = 1;
    	hold[5][8] = 8;
    	
    	hold[6][2] = 9;
    	hold[7][2] = 4;
    	hold[8][1] = 8;
    	
    	hold[6][7] = 2;
    	hold[7][6] = 5;
    	hold[8][6] = 6;
    	hold[8][8] = 3;
    	
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
