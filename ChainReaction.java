import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Tom Mroz
 * Simulates the game of Chain Reaction in a 11x11 grid
 * Supports up to 8 players
 * terrible intro gui
 * Now actually winnable
 * @version 1.7
 */
public class ChainReaction extends JPanel {
    private ArrayList<Player> playerList = new ArrayList<>();
    private static int[] bounds = new int[4];
    private static ArrayList<Color> colors = new ArrayList<>(9);
    private static final int height = 700;
    private static final int width = 625;
    private static int currentPlayer;
    private static int playerAmount;
    static Frame frame;
    private Ball[][] board;
    private LinkedBlockingQueue<ExplodeEvent> explodeQueue;


    private ChainReaction() {
        colors.add(Color.RED );
        colors.add(new Color(0, 120, 0));
        colors.add(Color.BLUE);
        colors.add(Color.YELLOW);
        colors.add(Color.WHITE );
        colors.add(Color.ORANGE);
        colors.add(Color.MAGENTA);
        colors.add(Color.CYAN );
        colors.add(Color.BLACK);

        setLayout(new BorderLayout(3, 3));
        setPreferredSize(new Dimension(625, 700));
        DrawingLoop game = new DrawingLoop(playerAmount);

        add(game, BorderLayout.CENTER);
        JLabel message = new JLabel("CHAIN REACTION", JLabel.CENTER);
        message.setForeground(colors.get(4));
        message.setFont(new Font("Monospaced", Font.BOLD, 36));
        message.setOpaque(false);
        add(message, BorderLayout.NORTH);

        new GameLoop();
    }
    //Only one instance can exist, need to change for client server model

    /**
     * Runs the program
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        frame = new Frame("Chain Reaction");
        JPanel content = new JPanel();
        frame.setContentPane(content);
        MainMenu menu = new MainMenu();
        content.add(menu);
        frame.pack();

    }

    /**
     * Creates the second window that contains the game board
     *
     * @param players number of players in the game
     */
    static void createWindow(int players) {
        frame = new Frame("Chain Reaction");
        playerAmount = players;
        JPanel content = new JPanel();
        frame.setContentPane(content);
        ChainReaction displayPanel = new ChainReaction();
        content.add(displayPanel);
        displayPanel.setBackground(Color.BLACK);
        displayPanel.setForeground(Color.WHITE);
        content.setPreferredSize(new Dimension(width, height));
        frame.pack();
    }

    /**
     * Nested class that handles graphics
     */
    private class DrawingLoop extends JPanel {

        DrawingLoop(int playerCount) {
            setBackground(Color.BLACK);
            for (int x = 0; x < playerCount; x++) {
                playerList.add(new Player("TOM", ChainReaction.colors.get(x)));
            }
            currentPlayer = 0;
            playerAmount--;
            startNewGame();
        }

        /**
         * Initializes the game board and calls repaint
         */
        private void startNewGame() {
            board = new Ball[11][11];
            for (int x = 0; x < 11; x++) {
                for (int y = 0; y < 11; y++) {
                    if ((x == 0 && y == 0) || (x == 0 && y == (board[0].length - 1)) || (x == (board.length - 1) && y == 0) || (x == (board.length - 1) && y == (board[0].length - 1))) {
                        board[x][y] = new Ball("Corner");

                    } else if ((x != 0 && x != (board.length - 1) && y == 0) || (x != 0 && x != (board.length - 1) && y == (board[0].length - 1)) || (x == 0 && y != 0 && y != (board[0].length - 1)) || (x == (board.length - 1) && y != 0 && y != (board[0].length - 1))) {
                        board[x][y] = new Ball("Edge");
                    } else {
                        board[x][y] = new Ball("Middle");
                    }
                }
            }
            repaint();

        }

        /**
         * Draws everything mostly lines
         *
         * @param g Graphics object used in drawing
         */
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor((playerList.get(currentPlayer)).getPlayerColor());
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int length = (int) (width * .07);
            int a = length;
            int down = 65;
            bounds[0] = length;
            g.drawLine(length - 1, down - 1, length - 1, 614);
            //vertical lines
            for (int x = 0; x < 11; x++) {

                board[0][x].setHorizontalLine(length);
                length = length + (int) (width * .08);
                g.drawLine(length - 1, down - 1, length - 1, 614);
            }

            bounds[1] = length + 2;
            bounds[2] = down;
            //horizontal lines
            for (int x = 0; x < 11; x++) {
                g.drawLine(a - 1, down - 1, length - 1, down - 1);
                board[x][0].setVerticalLine(down);
                down = down + (int) (width * .08);
            }
            g.drawLine(a, down, length, down);

            for (int x = 0; x < board.length; x++) {
                for (int y = 0; y < board[0].length; y++) {
                    if (board[x][y].getValue() != 0) {

                        g.setColor(board[x][y].getBallColor());
                        g.fillOval(((board[0][x].getHorizontalLine())) + 5, (board[y][0].getVerticalLine()) + 5, 40, 40);
                        g.drawString(String.valueOf(board[x][y].getValue()), ((board[0][x].getHorizontalLine()) + 5), (board[y][0].getVerticalLine()) + 5);

                    }

                }
            }
        }

    }

    /**
     * Handles all of the mouse events
     */
    private class GameLoop implements MouseListener {

        GameLoop() {
            addMouseListener(this);
        }

        public void mouseReleased(MouseEvent evt) {
            int locationX = evt.getX();
            int locationY = evt.getY();
            ballPlacer(locationX, locationY, playerList.get(currentPlayer).getPlayerColor());

        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }

        public void mousePressed(MouseEvent evt) {
        }

        public void mouseClicked(MouseEvent evt) {
        }

        /**
         * Called after a mouse click, places the balls, and controls the flow of logic
         * @param x     x coordinate of the mouse click
         * @param y     y coordinate of the mouse click
         * @param color Color of the ball placed
         */
        private void ballPlacer(int x, int y, Color color) {
            boolean moveLegal = isMoveLegal(x, y, color);
            int xBallPosition = 0;
            int yBallPosition = 0;
            if (moveLegal == false) {
                return;
            }
            if (moveLegal) {
                for (int a = 0; a < board.length - 1; a++) {
                    if ((((board[0][a].getHorizontalLine()) <= x)) && ((board[0][a + 1].getHorizontalLine()) >= x)) {
                        xBallPosition = a ;
                    }
                }
                for (int a = 0; a < board[0].length - 1; a++) {
                    if ((((board[a][0].getVerticalLine()) <= y)) && (board[a + 1][0].getVerticalLine()) >= y) {
                        yBallPosition = a - 1;
                    }
                }
                if ((y >= 565) && (y < 615)){
                    yBallPosition = 9;
                }
                if ((y >= 615) && (y <= 665)) {
                    yBallPosition = 10;
                }
                if (x >= 542 && x <= bounds[1]) {
                    xBallPosition = 10;
                }
                if (!(color.equals(board[xBallPosition][yBallPosition].getBallColor()) || (board[xBallPosition][yBallPosition].getBallColor() == null))) {
                    return;
                }
                board[xBallPosition][yBallPosition].setValue((board[xBallPosition][yBallPosition].getValue()) + 1);
                board[xBallPosition][yBallPosition].setColor(color);

                if (currentPlayer == playerAmount) {
                    currentPlayer = 0;
                } else {
                    currentPlayer++;
                }
            }
            repaint();
            explodeQueue = new LinkedBlockingQueue<>();
            repaint();
            for (int a = 0; a < 11; a++) {
                for (int b = 0; b < 11; b++) {
                    if ((board[a][b].getValue() >= board[a][b].getMaxValue())) {
                        explodeQueue.add(new ExplodeEvent(a, b));
                    }
                }
            }

            while (explodeQueue.size() != 0) {
                explode(explodeQueue.peek().getX(), explodeQueue.peek().getY());
                explodeQueue.poll();
            }
        }
    }

    /**
     * Tests if the mouse clicks are not in an occupied spot
     *
     * @param x     x coordinate of the mouse click
     * @param y     y coordinate of the mouse click
     * @param color Color of the ball placed
     * @return True if the move is legal, false if it is not
     */
    private boolean isMoveLegal(int x, int y, Color color) {
        if ((x <= bounds[0]) || (x >= bounds[1]) || (y <= 115) || (y >= 665)) {
            return false;
        } else if ((color == null)) {
            return true;
        } else {
            return true;
        }
    }

    /**
     * Tests if a square is about to explsoe
     *
     * @param a a place in the array of the explosion
     * @param b b place in the array of the explosion
     */
    private void doesExplode(int a, int b) {
        if ((board[a][b].getValue() >= board[a][b].getMaxValue())) {
            explodeQueue.add(new ExplodeEvent(a, b));
        }

    }

    /**
     * Does all the math to see if a square will explode
     *
     * @param a a place in the array of the explosion
     * @param b b place in the array of the explosion
     */
    private void explode(int a, int b) {

        board[a][b].setValue(0);
        if (board[a][b].getMaxValue() == 2) {
            if ((a == 0 && b == 0)) {
                board[a + 1][b].setValue(board[a + 1][b].getValue() + 1);
                board[a][b + 1].setValue(board[a][b + 1].getValue() + 1);

                board[a + 1][b].setColor(board[a][b].getBallColor());
                board[a][b + 1].setColor(board[a][b].getBallColor());

                doesExplode(a + 1, b);
                doesExplode(a, b + 1);

            } else if (a == 0 && b == (board[0].length - 1)) {
                board[a + 1][b].setValue(board[a + 1][b].getValue() + 1);
                board[a][b - 1].setValue(board[a][b - 1].getValue() + 1);

                board[a + 1][b].setColor(board[a][b].getBallColor());
                board[a][b - 1].setColor(board[a][b].getBallColor());

                doesExplode(a + 1, b);
                doesExplode(a, b - 1);

            } else if (a == (board.length - 1) && b == 0) {
                board[a - 1][b].setValue(board[a - 1][b].getValue() + 1);
                board[a][b + 1].setValue(board[a][b + 1].getValue() + 1);

                board[a][b + 1].setColor(board[a][b].getBallColor());
                board[a - 1][b].setColor(board[a][b].getBallColor());

                doesExplode(a, b + 1);
                doesExplode(a - 1, b);
            } else {
                board[a - 1][b].setValue(board[a - 1][b].getValue() + 1);
                board[a][b - 1].setValue(board[a][b - 1].getValue() + 1);

                board[a - 1][b].setColor(board[a][b].getBallColor());
                board[a][b - 1].setColor(board[a][b].getBallColor());

                doesExplode(a - 1, b);
                doesExplode(a, b - 1);
            }
        } else if (board[a][b].getMaxValue() == 3) {
            if (a > 0 && b == 0) {
                board[a - 1][b].setValue(board[a - 1][b].getValue() + 1);
                board[a][b + 1].setValue(board[a][b + 1].getValue() + 1);
                board[a + 1][b].setValue(board[a + 1][b].getValue() + 1);

                board[a + 1][b].setColor(board[a][b].getBallColor());
                board[a][b + 1].setColor(board[a][b].getBallColor());
                board[a - 1][b].setColor(board[a][b].getBallColor());

                doesExplode(a + 1, b);
                doesExplode(a, b + 1);
                doesExplode(a - 1, b);
            } else if (a > 0 && b == board[0].length - 1) {
                board[a - 1][b].setValue(board[a - 1][b].getValue() + 1);
                board[a][b - 1].setValue(board[a][b - 1].getValue() + 1);
                board[a + 1][b].setValue(board[a + 1][b].getValue() + 1);

                board[a + 1][b].setColor(board[a][b].getBallColor());
                board[a - 1][b].setColor(board[a][b].getBallColor());
                board[a][b - 1].setColor(board[a][b].getBallColor());

                doesExplode(a + 1, b);
                doesExplode(a - 1, b);
                doesExplode(a, b - 1);

            } else if (a == 0 && b > 0) {
                board[a][b - 1].setValue(board[a][b - 1].getValue() + 1);
                board[a + 1][b].setValue(board[a + 1][b].getValue() + 1);
                board[a][b + 1].setValue(board[a][b + 1].getValue() + 1);

                board[a][b + 1].setColor(board[a][b].getBallColor());
                board[a][b - 1].setColor(board[a][b].getBallColor());
                board[a + 1][b].setColor(board[a][b].getBallColor());

                doesExplode(a + 1, b);
                doesExplode(a, b + 1);
                doesExplode(a, b - 1);
            } else if(a == board.length - 1 && b > 0){
                board[a][b - 1].setValue(board[a][b - 1].getValue() + 1);
                board[a - 1][b].setValue(board[a - 1][b].getValue() + 1);
                board[a][b + 1].setValue(board[a][b + 1].getValue() + 1);

                board[a][b + 1].setColor(board[a][b].getBallColor());
                board[a - 1][b].setColor(board[a][b].getBallColor());
                board[a][b - 1].setColor(board[a][b].getBallColor());

                doesExplode(a, b + 1);
                doesExplode(a - 1, b);
                doesExplode(a, b - 1);
            }
        } else if (board[a][b].getMaxValue() == 4) {
            board[a + 1][b].setValue(board[a + 1][b].getValue() + 1);
            board[a - 1][b].setValue(board[a - 1][b].getValue() + 1);
            board[a][b + 1].setValue(board[a][b + 1].getValue() + 1);
            board[a][b - 1].setValue(board[a][b - 1].getValue() + 1);

            board[a + 1][b].setColor(board[a][b].getBallColor());
            board[a - 1][b].setColor(board[a][b].getBallColor());
            board[a][b + 1].setColor(board[a][b].getBallColor());
            board[a][b - 1].setColor(board[a][b].getBallColor());

            doesExplode(a + 1, b);
            doesExplode(a, b + 1);
            doesExplode(a - 1, b);
            doesExplode(a, b - 1);
        }

        board[a][b].setColor(null);
        if (isGameOver()) {
            endGame();
        }
    }

    private boolean isGameOver() {
        int x;
        ArrayList<Integer> gameEnd = new ArrayList<>(8);
        for(x = 0;  x < 8; x++){
            gameEnd.add(0);
        }
        for (x = 0; x < gameEnd.size() - 1; x++) {
            for (int a = 0; a < 11; a++) {
                for (int b = 0; b < 11; b++) {
                    if (board[a][b].getBallColor() != null) {
                        if ((board[a][b].getBallColor().equals(colors.get(x)))) {
                            gameEnd.set(x, gameEnd.get(x)+ 1);
                        }
                    }
                }
            }
            if(gameEnd.get(x) == 0 && x <= playerList.size() - 1){
                playerList.remove(x);
                gameEnd.remove(x);
                playerAmount--;
                colors.remove(x);
            }
        }
        /*if (gameEnd[0] >= 1 && gameEnd[1] == 0 && gameEnd[2] == 0 && gameEnd[3] == 0 && gameEnd[4] == 0 && gameEnd[5] == 0 && gameEnd[6] == 0 && gameEnd[7] == 0) {
            return true;
        } else if (gameEnd[0] == 0 && gameEnd[1] >= 1 && gameEnd[2] == 0 && gameEnd[3] == 0 && gameEnd[4] == 0 && gameEnd[5] == 0 && gameEnd[6] == 0 && gameEnd[7] == 0) {
            return true;
        } else if (gameEnd[0] == 0 && gameEnd[1] == 0 && gameEnd[2] >= 1 && gameEnd[3] == 0 && gameEnd[4] == 0 && gameEnd[5] == 0 && gameEnd[6] == 0 && gameEnd[7] == 0) {
            return true;
        } else if (gameEnd[0] == 0 && gameEnd[1] == 0 && gameEnd[2] == 0 && gameEnd[3] >= 1 && gameEnd[4] == 0 && gameEnd[5] == 0 && gameEnd[6] == 0 && gameEnd[7] == 0) {
            return true;
        } else if (gameEnd[0] == 0 && gameEnd[1] == 0 && gameEnd[2] == 0 && gameEnd[3] == 0 && gameEnd[4] >= 1 && gameEnd[5] == 0 && gameEnd[6] == 0 && gameEnd[7] == 0) {
            return true;
        } else if (gameEnd[0] == 0 && gameEnd[1] == 0 && gameEnd[2] == 0 && gameEnd[3] == 0 && gameEnd[4] == 0 && gameEnd[5] >= 1 && gameEnd[6] == 0 && gameEnd[7] == 0) {
            return true;
        } else if (gameEnd[0] == 0 && gameEnd[1] == 0 && gameEnd[2] == 0 && gameEnd[3] == 0 && gameEnd[4] == 0 && gameEnd[5] == 0 && gameEnd[6] >= 1 && gameEnd[7] == 0) {
            return true;
        } else if (gameEnd[0] == 0 && gameEnd[1] == 0 && gameEnd[2] == 0 && gameEnd[3] == 0 && gameEnd[4] == 0 && gameEnd[5] == 0 && gameEnd[6] == 0 && gameEnd[7] >= 1) {
            return true;
        }*/
        if(playerList.size() == 1){
            return true;
        }
        return false;

    }

    private void endGame() {

        setBackground(Color.BLACK);
        frame.dispose();
        frame = new Frame("Chain Reaction");
        JPanel content = new JPanel();
        frame.setContentPane(content);
        content.add(new EndMenu());
        frame.pack();

    }

    /**
     * subclass defining some essential variables
     */
    private class ExplodeEvent {
        private int x;
        private int y;

        private ExplodeEvent(int a, int b) {
            x = a;
            y = b;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }
    }

    /**
     * Subclass representing the first menu, which initializes the number of players
     */
    private static class MainMenu extends JPanel implements ActionListener {
        JTextField playerSize;

        private MainMenu() {
            setLayout(new BorderLayout(3, 3));
            setBackground(Color.BLACK);
            setPreferredSize(new Dimension(300, 400));
            JLabel message = new JLabel("CHAIN REACTION", JLabel.CENTER);
            message.setForeground(Color.WHITE);
            message.setFont(new Font("Monospaced", Font.BOLD, 24));
            message.setOpaque(false);
            add(message, BorderLayout.NORTH);
            JButton newGame = new JButton("New Game");
            newGame.addActionListener(this);
            newGame.setBackground(Color.BLACK);
            newGame.setPreferredSize(new Dimension(70, 30));
            add(newGame, BorderLayout.CENTER);
            playerSize = new JTextField("3");
            add(playerSize, BorderLayout.SOUTH);

            repaint();
        }

        public void actionPerformed(ActionEvent evt) {
            try {
                if ((playerSize.getText() != null) && (Integer.parseInt(playerSize.getText()) >= 2) && (Integer.parseInt(playerSize.getText()) <= 8)) {
                    frame.setVisible(false);
                    frame.dispose();
                    createWindow(Integer.parseInt(playerSize.getText()));
                }
            } catch (NumberFormatException e) {
                System.out.println("cant do that homeslice");
            }

        }

        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(Color.WHITE);
        }

    }

    /**
     * Subclass of JFrame used to create frames in the beginning
     */
    private static class Frame extends JFrame {
        Frame(String name) {
            this.setName(name);
            this.setLocation(150, 0);
            this.setVisible(true);
            this.setResizable(false);
            this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        }

    }
    private class EndMenu extends JPanel {
        public EndMenu() {
            setBackground(Color.BLACK);
            setPreferredSize(new Dimension(200, 350));
            setLayout(new BorderLayout());
            JLabel label = new JLabel("GAME OVER!", JLabel.CENTER);
            label.setForeground(Color.WHITE);
            label.setFont(new Font("Monospaced", Font.BOLD, 24));
            label.setOpaque(false);
            add(label, BorderLayout.CENTER);
            repaint();
        }

        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(Color.WHITE);
        }
    }
}
