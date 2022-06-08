import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.ArrayList;

public class Checkers extends JPanel
{

    /**
     * Main odpowiada za otwarcie okna z gra.<br>
     * Zamkniecie okna przerywa dzialanie programu.
     */
    public static void main(String[] args)
    {
        JFrame window = new JFrame("Warcaby");
        Checkers content = new Checkers();
        window.setContentPane(content);
        window.pack();
        Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
        window.setLocation( (screensize.width - window.getWidth())/2, (screensize.height - window.getHeight())/2 );
        window.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        window.setResizable(false);
        window.setVisible(true);
    }

    //---------------------------------------------------------------------
    /**
     * Przycisk nowej gry.
     */
    private JButton newGameButton;
    /**
     * Przycisk rezygnacji.
     */
    private JButton resignButton;
    /**
     * Komunikaty.
     */
    private JLabel message;

    /**
     * Konstruktor tworzy plansze oraz przyciski wraz z ich rozmiarem i umiejscowieniem.
     */
    public Checkers()
    {
        setLayout(null);
        setPreferredSize( new Dimension(700,500) );
        setBackground(new Color(0,150,0));

        Board board = new Board();
        add(board);
        add(newGameButton);
        add(resignButton);
        add(message);

        board.setBounds(40,40,328,328); // Note:  size MUST be 164-by-164 !
        newGameButton.setBounds(420, 120, 240, 30);
        resignButton.setBounds(420, 240, 240, 30);
        message.setBounds(0, 400, 700, 60);
    }


    /**
     * Obiekt tej klasy przechowuje wiersz i kolumne pionka,
     * ktorym mozna sie ruszyc oraz miejsca, do ktorego sie ruszy
     */
    private static class CheckersMove
    {
        /**
         * Wiersz startowy.
         */
        int fromRow;
        /**
         * Kolumna startowa.
         */
        int fromCol;
        /**
         * Wiersz docelowy.
         */
        int toRow;
        /**
         * Kolumna docelowa.
         */
        int toCol;
        /**
         * Konstruktor.
         */
        CheckersMove(int r1, int c1, int r2, int c2)
        {
            fromRow = r1;
            fromCol = c1;
            toRow = r2;
            toCol = c2;
        }
        /**
         *  Sprawdzenie czy ruch jest biciem.
         */
        boolean isJump()
        {

            return (fromRow - toRow == 2 || fromRow - toRow == -2);
        }
    }



    /**
     * Klasa odpowiada za gre oraz rysowanie planszy.
     */
    private class Board extends JPanel implements ActionListener, MouseListener
    {
        CheckersData board;
        /**
         * Czy gra jest w trakcie.
         */
        boolean gameInProgress;
        /**
         * Aktualny gracz.
         */
        int currentPlayer;
        /**
         *  Wybrane pole(wiersz/kolumna).
         */
        int selectedRow, selectedCol;
        /**
         *  Tablica zawierajaca mozliwe ruchy.
         */
        CheckersMove[] legalMoves;

        /**
         * Konstruktor. Tworzenie przyciskow, odpowiedzi na klikniecia w przyciski/pola.<br>
         * Stworzenie planszy i rozpoczecie gry.
         */
        Board()
        {
            setBackground(Color.BLACK);
            addMouseListener(this);
            resignButton = new JButton("Zrezygnuj");
            resignButton.addActionListener(this);
            newGameButton = new JButton("Nowa Gra");
            newGameButton.addActionListener(this);
            message = new JLabel("",JLabel.CENTER);
            message.setFont(new  Font("Serif", Font.BOLD, 14));
            message.setForeground(Color.green);
            board = new CheckersData();
            doNewGame();
        }

        /**
         * Odpowiedz na klikniecie przycisku przez gracza
         */
        public void actionPerformed(ActionEvent evt)
        {
            Object src = evt.getSource();
            if (src == newGameButton)
                doNewGame();
            else if (src == resignButton)
                doResign();
        }

        /**
         * Rozpoczecie nowej gry, zaczyna czerwony
         */
        void doNewGame()
        {
            if ( gameInProgress == true)
            {
                message.setText("ERROR");
                return;
            }
            board.setUpGame();
            currentPlayer = CheckersData.RED;
            legalMoves = board.getLegalMoves(CheckersData.RED);
            selectedRow = -1;
            message.setText("CZERWONY: Wykonaj ruch.");
            gameInProgress = true;
            newGameButton.setEnabled(false);
            resignButton.setEnabled(true);
            repaint();
        }

        /**
         * Rezygnacja, koniec gry.
         */
        void doResign()
        {
            if (gameInProgress == false)
            {
                message.setText("Nie toczy sie zadna gra.(ERROR)");
                return;
            }
            if (currentPlayer == CheckersData.RED)
                gameOver("CZERWONY rezygnuje, CZARNY wyrgywa.");
            else
                gameOver("CZARNY rezygnuje, CZERWONY wygrywa.");
        }

        /**
         * Koniec gry, wyswietlenie wiadomosci, mozliwosc nowej gry
         */
        void gameOver(String str)
        {
            message.setText(str);
            newGameButton.setEnabled(true);
            resignButton.setEnabled(false);
            gameInProgress = false;
        }

        /**
         * Ta metoda jest wywolywana przez mousePressed() gdy gracz wybierze kwadracik o konkretnym wierszu i kolumnie
         */
        void doClickSquare(int row, int col)
        {
            for (int i = 0; i < legalMoves.length; i++)
                if (legalMoves[i].fromRow == row && legalMoves[i].fromCol == col)
                {
                    selectedRow = row;
                    selectedCol = col;
                    if (currentPlayer == CheckersData.RED)
                        message.setText("CZERWONY: Wykonaj ruch");
                    else
                        message.setText("CZARNY: Wykonaj ruch");
                    repaint();
                    return;
                }

            if (selectedRow < 0)
            {
                message.setText("Wybierz pionek");
                return;
            }

            for (int i = 0; i < legalMoves.length; i++)
                if (legalMoves[i].fromRow == selectedRow && legalMoves[i].fromCol == selectedCol && legalMoves[i].toRow == row && legalMoves[i].toCol == col)
                {
                    doMakeMove(legalMoves[i]);
                    return;
                }

            message.setText("Wybierz pole docelowe.");

        }

        /**
         * Ta metoda jest wywolywana po wybraniu pionka przez gracza.<br>
         * Jesli nastapilo bicie, sprawdza mozliwosc ponownego bicia i jesli takowe wystepuje,
         * 	wymusza je.<br><br>
         *
         * 	Odpowiada tez za koniec tury jednego gracza, sprawdza mozliwosci ruchu drugiego gracza.<br>
         * 	Jesli drugi gracz nie ma mozliwosci ruchu, gra sie konczy.
         */
        void doMakeMove(CheckersMove move)
        {

            board.makeMove(move);
            if (move.isJump())
            {
                legalMoves = board.getLegalJumpsFrom(currentPlayer,move.toRow,move.toCol);
                if (legalMoves != null)
                {
                    if (currentPlayer == CheckersData.RED)
                        message.setText("CZERWONY: Musisz zbic kolejny pionek.");
                    else
                        message.setText("CZARNY: Musisz zbic kolejny pionek.");
                    selectedRow = move.toRow;
                    selectedCol = move.toCol;
                    repaint();
                    return;
                }
            }

            if (currentPlayer == CheckersData.RED)
            {
                currentPlayer = CheckersData.BLACK;
                legalMoves = board.getLegalMoves(currentPlayer);
                if (legalMoves == null)
                    gameOver("CZARNY nie ma mozliwosci ruchu. Wygrywa CZERWONY.");
                else if (legalMoves[0].isJump())
                    message.setText("CZARNY: Musisz zbic pionek przeciwnika.");
                else
                    message.setText("CZARNY: Wykonaj ruch.");
            }
            else
            {
                currentPlayer = CheckersData.RED;
                legalMoves = board.getLegalMoves(currentPlayer);
                if (legalMoves == null)
                    gameOver("CZERWONY nie ma mozliwosci ruchu. Wygrywa CZARNY.");
                else if (legalMoves[0].isJump())
                    message.setText("CZERWONY: Musisz zbic pionek przeciwnika.");
                else
                    message.setText("CZERWONY: Wykonaj ruch.");
            }

            /* Ustawienie selectedRow na -1, aby zaden pionek nie był zaznaczony na starcie */

            selectedRow = -1;

            /* Jesli tylko jednym pionkiem mozna ruszyc, wtedy automatycznie pokaz mozliwe ruchy dla tego pionka */

            if (legalMoves != null)
            {
                boolean sameStartSquare = true;
                for (int i = 1; i < legalMoves.length; i++)
                    if (legalMoves[i].fromRow != legalMoves[0].fromRow || legalMoves[i].fromCol != legalMoves[0].fromCol)
                    {
                        sameStartSquare = false;
                        break;
                    }
                if (sameStartSquare)
                {
                    selectedRow = legalMoves[0].fromRow;
                    selectedCol = legalMoves[0].fromCol;
                }
            }
            repaint();

        }

        /**
         * Rysowanie planszy, pionkow i podswietlanie mozliwych ruchow.
         */
        public void paintComponent(Graphics g)
        {

            /* Antialiasing dla lepszego ksztaltu okregow. */

            Graphics2D g2 = (Graphics2D)g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            /* Rysowanie obramowki planszy. */

            g.setColor(Color.black);
            g.drawRect(-1,-1,getSize().width-3,getSize().height-3);
            g.drawRect(0,0,getSize().width-1,getSize().height-1);
            g.drawRect(1,1,getSize().width-3,getSize().height-3);
            g.drawRect(2,2,getSize().width-5,getSize().height-5);



            /* Rysowanie planszy i pionkow. */

            for (int row = 0; row < 8; row++)
            {
                for (int col = 0; col < 8; col++)
                {
                    if ( row % 2 == col % 2 )
                        g.setColor(Color.LIGHT_GRAY);
                    else
                        g.setColor(Color.GRAY);
                    g.fillRect(4 + col*40, 4 + row*40, 40, 40);
                    switch (board.pieceAt(row,col))
                    {
                        case CheckersData.RED:
                            g.setColor(Color.RED);
                            g.fillOval(8 + col*40, 8 + row*40, 30, 30);
                            break;
                        case CheckersData.BLACK:
                            g.setColor(Color.BLACK);
                            g.fillOval(8 + col*40, 8 + row*40, 30, 30);
                            break;
                        case CheckersData.RED_KING:
                            g.setColor(Color.RED);
                            g.fillOval(8 + col*40, 8+ row*40, 30, 30);
                            g.setColor(Color.WHITE);
                            g.drawString("K", 20 + col*40, 26 + row*40);
                            break;
                        case CheckersData.BLACK_KING:
                            g.setColor(Color.BLACK);
                            g.fillOval(8 + col*40, 8 + row*40, 30, 30);
                            g.setColor(Color.WHITE);
                            g.drawString("K", 20 + col*40, 26 + row*40);
                            break;
                    }
                }
            }

            /* Podswietlanie pionkow ktorymi dany gracz moze sie ruszyc */

            if (gameInProgress)
            {
                g.setColor(Color.cyan);

                for (int i = 0; i < legalMoves.length; i++)
                {
                    g.drawRect(4 + legalMoves[i].fromCol*40, 4 + legalMoves[i].fromRow*40, 38, 38);
                    g.drawRect(6 + legalMoves[i].fromCol*40, 6 + legalMoves[i].fromRow*40, 34, 34);
                }

                /* Podswietlanie mozliwych ruchow  */
                if (selectedRow >= 0)
                {
                    g.setColor(Color.white);
                    g.drawRect(4 + selectedCol*40, 4 + selectedRow*40, 38, 38);
                    g.drawRect(6 + selectedCol*40, 6 + selectedRow*40, 34, 34);
                    g.setColor(Color.green);

                    for (int i = 0; i < legalMoves.length; i++)
                    {
                        if (legalMoves[i].fromCol == selectedCol && legalMoves[i].fromRow == selectedRow)
                        {
                            g.drawRect(4 + legalMoves[i].toCol*40, 4 + legalMoves[i].toRow*40, 38, 38);
                            g.drawRect(6 + legalMoves[i].toCol*40, 6 + legalMoves[i].toRow*40, 34, 34);
                        }
                    }
                }
            }

        }

        /**
         * Obsluga klikania przez gracza.
         */
        public void mousePressed(MouseEvent evt)
        {
            if (gameInProgress == false)
                message.setText("Nacisnij \"Nowa Gra\" aby rozpoczac rozgrywke.");
            else
            {
                int col = (evt.getX() - 4) / 40;
                int row = (evt.getY() - 4) / 40;
                if (col >= 0 && col < 8 && row >= 0 && row < 8)
                    doClickSquare(row,col);
            }
        }
        /**
         * Obsluga klikania przez gracza.
         */
        public void mouseReleased(MouseEvent evt) { }
        /**
         * Obsluga klikania przez gracza.
         */
        public void mouseClicked(MouseEvent evt) { }
        /**
         * Obsluga klikania przez gracza.
         */
        public void mouseEntered(MouseEvent evt) { }
        /**
         * Obsluga klikania przez gracza.
         */
        public void mouseExited(MouseEvent evt) { }
    }

    /**
     * Obiekt tej klasy przechowuje informacje o grze np
     * gdzie jest jaki pionek, w ktora strone moze sie ruszyc<br>
     * Metody tej klasy zwracaja listy mozliwych ruchow tych pionkow
     */
    private static class CheckersData
    {

        static final int
                EMPTY = 0,
                RED = 1,
                RED_KING = 2,
                BLACK = 3,
                BLACK_KING = 4;

        int[][] board;

        /**
         * Konstruktor, tworzenie planszy i przygotowanie do gry.
         */
        CheckersData()
        {
            board = new int[8][8];
            setUpGame();
        }

        /**
         * Przygotowanie planszy, ustawienie pionkow
         */
        void setUpGame()
        {
            for (int row = 0; row < 8; row++)
            {
                for (int col = 0; col < 8; col++)
                {
                    if ( row % 2 == col % 2 )
                    {
                        if (row < 3)
                            board[row][col] = BLACK;
                        else if (row > 4)
                            board[row][col] = RED;
                        else
                            board[row][col] = EMPTY;
                    }
                    else
                    {
                        board[row][col] = EMPTY;
                    }
                }
            }
        }

        /**
         * Zwracanie co znajduje sie na danym polu
         */
        int pieceAt(int row, int col)
        {
            return board[row][col];
        }

        /**
         * Wykonanie ruchu
         */
        void makeMove(CheckersMove move)
        {
            makeMove(move.fromRow, move.fromCol, move.toRow, move.toCol);
        }

        /**
         * Ruch pionka z (fromRow,fromCol) do (toRow,toCol).<br>
         * Jesli dany ruch jest biciem, wtedy zbity pionek jest usuwany z planszy.<br>
         * Jesli pionek rusza sie do ostatniego wiersza na stronie przeciwnika
         * zostaje zamieniony w dame.
         */
        void makeMove(int fromRow, int fromCol, int toRow, int toCol)
        {
            board[toRow][toCol] = board[fromRow][fromCol];
            board[fromRow][fromCol] = EMPTY;
            if (fromRow - toRow == 2 || fromRow - toRow == -2)
            {
                int jumpRow = (fromRow + toRow) / 2;
                int jumpCol = (fromCol + toCol) / 2;
                board[jumpRow][jumpCol] = EMPTY;
            }
            if (toRow == 0 && board[toRow][toCol] == RED)
                board[toRow][toCol] = RED_KING;
            if (toRow == 7 && board[toRow][toCol] == BLACK)
                board[toRow][toCol] = BLACK_KING;
        }

        /**
         * Zwraca tablice mozliwych ruchow dla danego gracza.<br>
         * Jesli gracz moze zbic pionka przeciwnika, wtedy bicie jest jedynym mozliwym ruchem.<br>
         * Jednak jesli gracz nie ma zadnego mozliwego ruchu, zwraca null.
         */
        CheckersMove[] getLegalMoves(int player)
        {

            if (player != RED && player != BLACK)
                return null;

            int playerKing;
            if (player == RED)
                playerKing = RED_KING;
            else
                playerKing = BLACK_KING;

            ArrayList<CheckersMove> moves = new ArrayList<CheckersMove>();

            for (int row = 0; row < 8; row++)
            {
                for (int col = 0; col < 8; col++)
                {
                    if (board[row][col] == player || board[row][col] == playerKing)
                    {
                        if (canJump(player, row, col, row+1, col+1, row+2, col+2))
                            moves.add(new CheckersMove(row, col, row+2, col+2));
                        if (canJump(player, row, col, row-1, col+1, row-2, col+2))
                            moves.add(new CheckersMove(row, col, row-2, col+2));
                        if (canJump(player, row, col, row+1, col-1, row+2, col-2))
                            moves.add(new CheckersMove(row, col, row+2, col-2));
                        if (canJump(player, row, col, row-1, col-1, row-2, col-2))
                            moves.add(new CheckersMove(row, col, row-2, col-2));
                    }
                }
            }

            if (moves.size() == 0)
            {
                for (int row = 0; row < 8; row++)
                {
                    for (int col = 0; col < 8; col++)
                    {
                        if (board[row][col] == player || board[row][col] == playerKing)
                        {
                            if (canMove(player,row,col,row+1,col+1))
                                moves.add(new CheckersMove(row,col,row+1,col+1));
                            if (canMove(player,row,col,row-1,col+1))
                                moves.add(new CheckersMove(row,col,row-1,col+1));
                            if (canMove(player,row,col,row+1,col-1))
                                moves.add(new CheckersMove(row,col,row+1,col-1));
                            if (canMove(player,row,col,row-1,col-1))
                                moves.add(new CheckersMove(row,col,row-1,col-1));
                        }
                    }
                }
            }

            if (moves.size() == 0)
                return null;
            else
            {
                CheckersMove[] moveArray = new CheckersMove[moves.size()];
                for (int i = 0; i < moves.size(); i++)
                    moveArray[i] = moves.get(i);
                return moveArray;
            }

        }

        /**
         * Zwracanie listy mozliwych bic z danego pola, przez danego gracza.<br>
         * Zwraca null gdy nie ma mozliwego bicia.
         */
        CheckersMove[] getLegalJumpsFrom(int player, int row, int col)
        {
            if (player != RED && player != BLACK)
                return null;
            int playerKing;
            if (player == RED)
                playerKing = RED_KING;
            else
                playerKing = BLACK_KING;
            ArrayList<CheckersMove> moves = new ArrayList<CheckersMove>();
            if (board[row][col] == player || board[row][col] == playerKing)
            {
                if (canJump(player, row, col, row+1, col+1, row+2, col+2))
                    moves.add(new CheckersMove(row, col, row+2, col+2));
                if (canJump(player, row, col, row-1, col+1, row-2, col+2))
                    moves.add(new CheckersMove(row, col, row-2, col+2));
                if (canJump(player, row, col, row+1, col-1, row+2, col-2))
                    moves.add(new CheckersMove(row, col, row+2, col-2));
                if (canJump(player, row, col, row-1, col-1, row-2, col-2))
                    moves.add(new CheckersMove(row, col, row-2, col-2));
            }
            if (moves.size() == 0)
                return null;
            else
            {
                CheckersMove[] moveArray = new CheckersMove[moves.size()];
                for (int i = 0; i < moves.size(); i++)
                    moveArray[i] = moves.get(i);
                return moveArray;
            }
        }

        /**
         * Sprawdzanie czy jest mozliwe bicie z pozycji (r1,c1) do (r3,c3) tj:<br>
         * - czy pole (r3,c3) znajduje sie na planszy,<br>
         * - czy jakis pionek jest na polu (r3,c3),<br>
         * - czy na polu (r2,c2) jest pionek do bicia<br>
         */
        private boolean canJump(int player, int r1, int c1, int r2, int c2, int r3, int c3)
        {

            if (r3 < 0 || r3 >= 8 || c3 < 0 || c3 >= 8)
                return false;

            if (board[r3][c3] != EMPTY)
                return false;

            if (player == RED)
            {
                if (board[r1][c1] == RED && r3 > r1)
                    return false;
                if (board[r2][c2] != BLACK && board[r2][c2] != BLACK_KING)
                    return false;
                return true;
            }
            else
            {
                if (board[r1][c1] == BLACK && r3 < r1)
                    return false;
                if (board[r2][c2] != RED && board[r2][c2] != RED_KING)
                    return false;
                return true;
            }

        }

        /**
         * Metoda wywolywana przez CzyPrawidlowyRuch() do sprawdzenia czy gracz
         * moze sie ruszyc z pola (r1,c1) do pola (r2, c2).<br>
         * (r1,c1) jest polem z pionkiem gracza, natomiast (r2,c2) to sasiadujace pole.
         */
        private boolean canMove(int player, int r1, int c1, int r2, int c2)
        {

            if (r2 < 0 || r2 >= 8 || c2 < 0 || c2 >= 8)
                return false;

            if (board[r2][c2] != EMPTY)
                return false;

            if (player == RED)
            {
                if (board[r1][c1] == RED && r2 > r1)
                    return false;
                return true;
            }
            else
            {
                if (board[r1][c1] == BLACK && r2 < r1)
                    return false;
                return true;
            }

        }

    }


}
