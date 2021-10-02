import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.net.URL;
import java.util.Objects;

public class BirthdayCard extends JFrame {
    public static final double fontScaleFactor = 0.47;
    // Dimensions
    public static final Dimension SCREEN_SIZE = Toolkit.getDefaultToolkit().getScreenSize();
    public static final Dimension APP_SIZE =
            new Dimension((int) (SCREEN_SIZE.width / 3.25), (int) (SCREEN_SIZE.height / 1.25));

    // DEBUG:
    // static {
    //     System.out.println(APP_SIZE);
    // }

    public BirthdayCard() {
        super("Brendan's Birthday Card");
        addComponents();
        initializeFrame();
    }

    private void addComponents() {
        JPanel main = new JPanel(new BorderLayout());

        JPanel header = new JPanel();

        JLabel headerText = new JLabel();
        // text
        headerText.setText("Happy Birthday Brendan!");
        headerText.setFont(new Font("Papyrus", Font.BOLD, (int) (58 * fontScaleFactor)));
        headerText.setVerticalAlignment(SwingConstants.CENTER);
        headerText.setHorizontalAlignment(SwingConstants.CENTER);

        // image
        ImageIcon head = getImageFromFileName("Header.png");
        double scaleFactor = getPercentToReach(head.getIconWidth(), (long) getPercentOf(APP_SIZE.width, 97));
        int[][] hDimensions = new int[][] {
                {head.getIconWidth(), head.getIconHeight()},                             // default image size
                {(int) (head.getIconWidth() * scaleFactor), (int) (head.getIconHeight() * scaleFactor)}, // 210% increase in size
        };
        Image scaled = head.getImage().getScaledInstance(hDimensions[1][0], hDimensions[1][1], Image.SCALE_REPLICATE);
        headerText.setIcon(new ImageIcon(scaled));
        headerText.setVerticalTextPosition(SwingConstants.CENTER);
        headerText.setHorizontalTextPosition(SwingConstants.CENTER);

        // generic
        header.add(headerText);
        header.setBackground(new Color(0xffd6ba));
        header.setOpaque(true);


        /* ******************* */
        /* *   The Message   * */ 
        /* ******************* */

        JPanel message = new JPanel(new BorderLayout());

        JTextArea messageText1 = new JTextArea();
        messageText1.setFont(new Font("Book Antiqua", Font.PLAIN, (int) (36 * fontScaleFactor)));

        String labelText1 =
                "Hey Brendan, I just wanted to wish you a very happy birthday. I hope you're having a great day, " +
                "and that you enjoy this special occasion.";
        messageText1.setText(labelText1);
        messageText1.setWrapStyleWord(true);
        messageText1.setLineWrap(true);
        messageText1.setEditable(false);
        messageText1.setFocusable(false);
        messageText1.setOpaque(false);
        messageText1.setBorder(new EmptyBorder(0, 0, (int) getPercentOf(APP_SIZE.height, 4), 0));

        message.setBackground(new Color(0xbee3db));
        message.setOpaque(true);
        message.add(messageText1, BorderLayout.NORTH);
        int newWidth0 = (int) getPercentOf(APP_SIZE.width, 5);
        int newHeight0 = (int) getPercentOf(APP_SIZE.height, 5);
        message.setBorder(new EmptyBorder(newHeight0, newWidth0, newHeight0, newWidth0));

        JTextArea messageText2 = new JTextArea();
        messageText2.setFont(new Font("Book Antiqua", Font.PLAIN, (int) (36 * fontScaleFactor)));

        String labelText2 =
                "15 years sure feels like it's been a long time, but there will definitively " +
                "be many more candles to blow in the future.";

        messageText2.setText(labelText2);
        messageText2.setWrapStyleWord(true);
        messageText2.setLineWrap(true);
        messageText2.setEditable(false);
        messageText2.setFocusable(false);
        messageText2.setOpaque(false);
        messageText2.setBorder(new EmptyBorder(0, 0, (int) getPercentOf(APP_SIZE.height, 4), 0));

        message.add(messageText2, BorderLayout.CENTER);

        JTextArea messageText3 = new JTextArea();
        messageText3.setFont(new Font("Book Antiqua", Font.PLAIN, (int) (36 * fontScaleFactor)));

        String labelText3 =
                "So let us celebrate! It has been great to be at your side, and I wish you the best for the upcoming " +
                "year.\n\n- Mateo";

        messageText3.setText(labelText3);
        messageText3.setWrapStyleWord(true);
        messageText3.setLineWrap(true);
        messageText3.setEditable(false);
        messageText3.setFocusable(false);
        messageText3.setOpaque(false);
        // messageText3.setBorder(new EmptyBorder((int) getPercentOf(APP_SIZE.height, 4), 0, 0, 0)); fixme

        message.add(messageText3, BorderLayout.SOUTH);

        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(0x555b6e));

        JLabel date = new JLabel();
        date.setText("10/1/2021");
        date.setFont(new Font("Book Antiqua", Font.PLAIN, (int) (42 * fontScaleFactor)));
        p.add(date, BorderLayout.WEST);

        JButton exitButton = new JButton();
        exitButton.setText("Click to exit");
        exitButton.setFont(new Font("Helvetica Bold", Font.BOLD, (int) (30 * fontScaleFactor)));
        exitButton.setBackground(new Color(0x89b0ae));
        exitButton.setFocusPainted(false);
        exitButton.addActionListener(e -> BirthdayCard.super.dispose());

        p.add(exitButton, BorderLayout.EAST);

        p.setOpaque(true);
        p.setBorder(BorderFactory.createLineBorder(new Color(0x555b6e), (int) getPercentOf(APP_SIZE.height, 2)));

        main.add(header, BorderLayout.NORTH);
        main.add(message, BorderLayout.CENTER);
        main.add(p, BorderLayout.SOUTH);
        main.setBorder(BorderFactory.createLineBorder(Color.WHITE, (int) getPercentOf(APP_SIZE.width, 1.5f)));
        this.add(main);
    }

    private static float getPercentToReach(long base, float goal) {
        return goal / (float) base;
    }

    private static float getPercentOf(long base, float percent) {
        return base / 100f * percent;
    }

    private void initializeFrame() {
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setIconImage(getImageFromFileName("Birthday Cake.png").getImage());
        this.setSize(APP_SIZE);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        this.setVisible(true);
        this.setLayout(new BorderLayout());
    }

    /**
     * This method will not work unless you create a package/directory called {@code images} and fill it with
     * pictures that have names that match those specified in the code above.
     */
    private ImageIcon getImageFromFileName(String fileName) {
        URL image = Objects.requireNonNull(BirthdayCard.class.getResource(String.format("/images/%s", fileName)));
        return new ImageIcon(image);
    }
}
