import java.awt.*;
import java.awt.event.*;
import static java.lang.String.format;
import java.util.*;
import java.util.List;
import javax.swing.*;
 
public class SnakeApp extends JPanel implements Runnable {
   enum dir {up(0,-1), down(0,1),right(1,0),left(-1,0);
   dir(int x, int y){
      this.x=x; this.y=y; }
   final int x, y; }

   static final Random ran = new Random();
   static final int BOARDER = -1, FULLNESS = 3000;
   volatile boolean gameEnd = true;

   Thread gameStat;
   int score, hiScore;
   int nRows = 44;
   int nCols = 64;
   dir dir;
   int fullness; 

   int[][] grid; // game board space
   List<Point> snake, food;
   Font smallFont;

public SnakeApp() { // 2d game board
      setPreferredSize(new Dimension(640, 440));
      setBackground(Color.WHITE);
      setFont(new Font("TimesNewRoman", Font.BOLD, 48));
      setFocusable(true);
 
      smallFont = getFont().deriveFont(Font.BOLD, 18);
      initGrid();
 
      addMouseListener(
         new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) { // menu
               if (gameEnd) {
                  NewGame();
                  repaint(); } } });
      addKeyListener(
         new KeyAdapter() {
 
            @Override
            public void keyPressed(KeyEvent e) { // controller
 
               switch (e.getKeyCode()) {
 
                  case KeyEvent.VK_W:
                     if (dir != dir.down)
                        dir = dir.up;
                     break;
 
                  case KeyEvent.VK_A:
                     if (dir != dir.right)
                        dir = dir.left;
                     break;
 
                  case KeyEvent.VK_D:
                     if (dir != dir.left)
                        dir = dir.right;
                     break;
 
                  case KeyEvent.VK_S:
                     if (dir != dir.up)
                        dir = dir.down;
                     break; }
               repaint(); } }); } 
      void NewGame() { // play game
      gameEnd = false;
      stop();
      initGrid();
      food = new LinkedList<>();
 
      dir = dir.left;
      fullness = FULLNESS;
 
      if (score > hiScore)
         hiScore = score;
      score = 0;

 
      snake = new ArrayList<>();
      for (int x = 0; x < 7; x++)
         snake.add(new Point(nCols / 2 + x, nRows / 2));
      do
         addFood();
      while(food.isEmpty());
 
      (gameStat = new Thread(this)).start(); }
 
   void stop() {
      if (gameStat != null) {
         Thread tmp = gameStat;
         gameStat = null;
         tmp.interrupt(); } }

   void initGrid() { // game grid
      grid = new int[nRows][nCols];
      for (int r = 0; r < nRows; r++) {
         for (int c = 0; c < nCols; c++) {
            if (c == 0 || c == nCols - 1 || r == 0 || r == nRows - 1)
               grid[r][c] = BOARDER; } } }   

   public void run() {
 
      while (Thread.currentThread() == gameStat) {
 
         try {Thread.sleep(Math.max(75 - score, 25));} catch (InterruptedException e) {
            return; }
         if (fullnessUsed() || hitsWall()) {gameEnd();}

         else {
            if (eatFood()) { // condittion for eating dot
               score++;
               fullness = FULLNESS;
            growSnake(); }
            moveSnake();
            addFood(); }
         repaint();} }
    boolean fullnessUsed() { // nic
      fullness -= 2;
      return fullness <= 0; }
 
   boolean hitsWall() { // condition for hitting boarder
      Point head = snake.get(0);
      int nextCol = head.x + dir.x;
      int nextRow = head.y + dir.y;
      return grid[nextRow][nextCol] == BOARDER; }
 
 
   boolean eatFood() { // condition for hitting object
      Point head = snake.get(0);
      int nextCol = head.x + dir.x;
      int nextRow = head.y + dir.y;
      for (Point p : food)
         if (p.x == nextCol && p.y == nextRow) {
            return food.remove(p); }
      return false; }
 
   void gameEnd() { // game end condition
      gameEnd = true;
      stop(); }
 
   void moveSnake() { // snake movement condition
      for (int i = snake.size() - 1; i > 0; i--) {
         Point p1 = snake.get(i - 1);
         Point p2 = snake.get(i);
         p2.x = p1.x;
         p2.y = p1.y; }
      Point head = snake.get(0);
      head.x += dir.x;
      head.y += dir.y; }
 
   void growSnake() { // increase snake lenght
      Point tail = snake.get(snake.size() - 1);
      int x = tail.x + dir.x;
      int y = tail.y + dir.y;
      snake.add(new Point(x, y)); }
 
   void addFood() { // add object to game
      if (food.size() < 10) {
 
         if (ran.nextInt(20) == 0) { // 1 in 20
 
            if (ran.nextInt(6) != 0) {  // 3 in 4
               int x, y;
               while (true) {
 
                  x = ran.nextInt(nCols);
                  y = ran.nextInt(nRows);
                  if (grid[y][x] != 0)
                     continue;
 
                  Point p = new Point(x, y);
                  if (snake.contains(p) || food.contains(p))
                     continue;
 
                  food.add(p);
                  break; }
            } else if (food.size() > 3)
               food.remove(0); } } }
    void drawGrid(Graphics2D g) { // make the game board
      g.setColor(Color.black);
      for (int r = 0; r < nRows; r++) {
         for (int c = 0; c < nCols; c++) {
            if (grid[r][c] == BOARDER)
               g.fillRect(c * 10, r * 10, 10, 10);} } }
    void drawSnake(Graphics2D g) { // Snake body
      g.setColor(Color.BLACK);
      for (Point p : snake)
         g.fillRect(p.x * 10, p.y * 10, 10, 10);
 
      g.setColor(fullness < 500 ? Color.black : Color.white);
      Point head = snake.get(0);
      g.fillRect(head.x * 10, head.y * 10, 10, 10); }

   void drawFood(Graphics2D g) { // object body
      g.setColor(Color.black);
      for (Point p : food)
         g.fillRect(p.x * 10, p.y * 10, 10, 10); }

   void drawStartScreen(Graphics2D g) { // game ui
      g.setColor(Color.black);
      g.setFont(getFont());
      g.drawString("SNAKE", 240, 190);
      g.setColor(Color.black);
      g.setFont(smallFont);
      g.drawString("(Click To START)", 250, 240);
   }
 
   void drawScore(Graphics2D g) { // keep track of score
      int h = getHeight();
      g.setFont(smallFont);
      g.setColor(getForeground());
      String s = format("Hi-Score: %d    Score: %d", hiScore, score);
      g.drawString(s, 30, h - 30);
      g.drawString(format("Fullness: %d", fullness), getWidth() - 150, h - 30); }

   public void paintComponent(Graphics gg) { // object paint
      super.paintComponent(gg);
      Graphics2D g = (Graphics2D) gg;
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
             RenderingHints.VALUE_ANTIALIAS_ON);
      drawGrid(g);
 
      if (gameEnd) { // gameend
         drawStartScreen(g);} 
      else {
         drawSnake(g);
         drawFood(g);
         drawScore(g); } }
 
   public static void main(String[] args) { // master control
      SwingUtilities.invokeLater(
         () -> {
            JFrame mainFrame = new JFrame();
            mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            mainFrame.setTitle("SNAKE");
            mainFrame.setResizable(true);
            mainFrame.add(new SnakeApp(), BorderLayout.CENTER);
            mainFrame.pack();
            mainFrame.setLocationRelativeTo(null);
            mainFrame.setVisible(true); });
   }
}            
   