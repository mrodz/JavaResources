import java.awt.*;
import java.util.Arrays;
import java.util.function.Supplier;
import javax.swing.*;

/**
 * A special class of JSplitPane that has a {@link #toggle()} method, which as the name
 * suggests, will toggle the state of a specified component (Make it slide in or out on
 * a specified axis).
 *
 * Use the fields {@link #OUT_FROM_LEFT}, {@link #OUT_FROM_RIGHT}, {@link #OUT_FROM_TOP},
 * and {@link #OUT_FROM_BOTTOM} to specify how you want the component to slide.
 *
 * This class can be especially helpful in creating modern-looking sliding nav-bars.
 *
 * @see JSplitPane
 */
public class MSideBar extends JSplitPane {
    // used as a fixed lock for synchronization.
    private final Object lockObject = new Object();

    // other assorted fields.
    private final Timer timer;
    private double ratio;
    private double delta;
    private final double MAXIMUM_COVERAGE;

    /**
     * Specify the delay, in milliseconds, between each update to the sliding animation. Default value: 3ms.
     */
    private int tickSpeed = 3;

    /**
     * Specify the how many times the frame should be updated per cycle. Default value: 24.
     */
    private int frameRefresh = 24;

    //
    // PUBLIC (REQUIRED) FIELDS
    //
    /**
     * The component will start at the left-hand side of its parent, and slide its way over
     * to the right as the timer ticks. It is the default operation in {@link #MSideBar(JPanel, JPanel)}
     * @see #MSideBar(JPanel, JPanel, double, double)
     */
    public static final double OUT_FROM_LEFT = 0d;

    /**
     * The component will start at the right-hand side of its parent, and slide its way over
     * to the left as the timer ticks.
     * @see #MSideBar(JPanel, JPanel, double, double)
     */
    public static final double OUT_FROM_RIGHT = 1d;

    /**
     * The component will start at bottom side of its parent, and slide its way up to the top
     * as the timer ticks.
     * @see #MSideBar(JPanel, JPanel, double, double)
     */
    public static final double OUT_FROM_BOTTOM = 2d;

    /**
     * The component will start at the top side of its parent, and slide its way down
     * to the bottom as the timer ticks.
     * @see #MSideBar(JPanel, JPanel, double, double)
     */
    public static final double OUT_FROM_TOP = 3d;

    /**
     * Construct the default {@link MSideBar}, passing two {@link JPanel}s as arguments. It will slide
     * using the rule specified in {@link #OUT_FROM_LEFT}, going from covering 0% to 100% of the screen.
     * @param p1 The component to be placed on the left of the screen.
     * @param p2 The component to be placed on the right of the screen.
     * @see #OUT_FROM_LEFT
     */
    @SuppressWarnings("unused")
    public MSideBar(JPanel p1, JPanel p2) {
        this(p1, p2, 1.0, MSideBar.OUT_FROM_LEFT);
    }

    /**
     * These will either be [left panel, right panel] or [top panel, bottom panel] depending on the
     * value provided in the <tt>outDirection</tt> parameter. This constructor allows for more modifications
     * to be made to the inner workings of the slider.
     * @param p1 The first panel
     * @param p2 The second panel
     * @param maximumCoverage a {@code double} value, 0 <= x <= 1. Used to describe the amount of 'space'
     *                        that the fully slid panel should cover. (In essence, 0.3 would mean that once the
     *                        sliding has been completed, the new panel would cover ~30% of this component's
     *                        area.)
     * @param outDirection    This can be one of either {@link #OUT_FROM_LEFT},
     *                        {@link #OUT_FROM_RIGHT}, {@link #OUT_FROM_TOP}, and {@link #OUT_FROM_BOTTOM}.
     */
    public MSideBar(JPanel p1, JPanel p2, double maximumCoverage, double outDirection) {
        super(outDirection == 2d || outDirection == 3d ? JSplitPane.VERTICAL_SPLIT : JSplitPane.HORIZONTAL_SPLIT, true, p1, p2);

        // Make sure the range fits in line with the natural JSplitPane limit.
        assertRangeInclusiveEnds(maximumCoverage, 0, 1);
        assertEqualsAny(outDirection, MSideBar.OUT_FROM_LEFT, MSideBar.OUT_FROM_RIGHT, MSideBar.OUT_FROM_TOP, MSideBar.OUT_FROM_BOTTOM);

        // Don't allow users to manually draw in and out the menu.
        this.setEnabled(false);

        final double finalOutDirection = (outDirection == MSideBar.OUT_FROM_LEFT || outDirection == MSideBar.OUT_FROM_RIGHT)
                ? outDirection
                : outDirection == MSideBar.OUT_FROM_TOP
                ? MSideBar.OUT_FROM_LEFT : MSideBar.OUT_FROM_RIGHT;

        this.MAXIMUM_COVERAGE = maximumCoverage;

        // these are to avoid the rounded number.
        this.ratio = finalOutDirection == MSideBar.OUT_FROM_RIGHT ? 0.999999999 : 0.000000001;

        // the amount to be moved every 'tick'
        this.delta = (finalOutDirection == MSideBar.OUT_FROM_RIGHT ? -1 : +1) * (MAXIMUM_COVERAGE / this.frameRefresh);

        this.setDividerLocation(finalOutDirection == MSideBar.OUT_FROM_LEFT ? 0 : Integer.MAX_VALUE);

        timer = new Timer(this.tickSpeed, e1 -> {
            ratio += delta;
            if (finalOutDirection == MSideBar.OUT_FROM_LEFT) {
                if (ratio > MAXIMUM_COVERAGE) {
                    ratio = MAXIMUM_COVERAGE;
                    delta = -delta;
                    ((Timer) e1.getSource()).stop();
                } else if (ratio < 0d) {
                    delta = -delta;
                    ratio = 0d;
                    ((Timer) e1.getSource()).stop();
                }
            } else {
                if (ratio < 1d - MAXIMUM_COVERAGE) {
                    ratio = 1d - MAXIMUM_COVERAGE;
                    delta = -delta;
                    ((Timer) e1.getSource()).stop();
                } else if (ratio > 1d) {
                    delta = -delta;
                    ratio = 1d;
                    ((Timer) e1.getSource()).stop();
                }
            }

            setDividerLocation(ratio);
        });

        // To avoid waiting on the event dispatch thread.
        Thread t = new Thread(() -> {
            while (true) {
                synchronized (lockObject) {
                    try {
                        lockObject.wait();
                    } catch (InterruptedException ignored) {
                    }
                }

                timer.restart();
            }
        });

        t.start();
    }

    /**
     * Toggle the sliding of the display. It's as simple as that!
     */
    public void toggle() {
        // ignore clicks if there is already an animation present.
        if (timer.isRunning()) return;
        synchronized (lockObject) {
            lockObject.notify();
        }
    }

    //
    // UTILITIES
    //

    @SuppressWarnings("SameParameterValue")
    private void assertRangeInclusiveEnds(double value, double min, double max) {
        if (!(value >= min && value <= max)) {
            throw new AssertionError(String.format("%f does not fit in range [%f,%f]", value, min, max));
        }
    }

    @SuppressWarnings("unused")
    @SafeVarargs
    private synchronized final <T, D> T assertEqualsAll(T obj1, D... obj2) {
        for (D item : obj2) {
            if (!obj1.equals(item)) {
                throw new AssertionError(String.format("%s != %s", obj1, item));
            }
        }
        return obj1;
    }

    @SafeVarargs
    private synchronized final <T, D> void assertEqualsAny(T obj1, D... obj2) {
        boolean anyMatch = ((Supplier<Boolean>) () -> {
            for (D item : obj2) {
                if (obj1.equals(item)) {
                    return true;
                }
            }
            return false;
        }).get();
        if (!anyMatch) {
            throw new AssertionError(String.format("%s not present in %s", obj1, Arrays.toString(obj2)));
        }
    }

    //
    // GETTERS + SETTERS
    //

    /**
     * @see #tickSpeed
     * @return the set tick speed.
     */
    public int getTickSpeed() {
        return tickSpeed;
    }

    /**
     * Set the tick speed.
     * @param tickSpeed the new tick speed.
     * @see #getTickSpeed()
     */
    public void setTickSpeed(int tickSpeed) {
        this.tickSpeed = tickSpeed;
    }

    /**
     * @see #frameRefresh
     * @return the set frame refresh rate.
     */
    public int getFrameRefresh() {
        return frameRefresh;
    }

    /**
     * Set the frame refresh rate.
     * @param frameRefresh the new frame refresh rate.
     * @see #getFrameRefresh()
     */
    public void setFrameRefresh(int frameRefresh) {
        this.frameRefresh = frameRefresh;
    }
}


//
// EXAMPLE IMPLEMENTATION
//
class MyPanel extends JPanel {
    Color color;

    public MyPanel(Color color) {
        this.color = color;

        this.setPreferredSize(new Dimension(800, 800));
        this.setSize(new Dimension(200, 200));

        this.setBackground(Color.BLACK);
        this.setOpaque(true);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(color);
        g.drawLine(0, 0, getWidth(), getHeight());
        g.drawLine(getWidth(), 0, 0, getHeight());
    }
}

class ExampleFrame extends JFrame {
    ExampleFrame() {
        this.setTitle("Example of a sidebar!");
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setLayout(new BorderLayout());

        MSideBar sideBar = new MSideBar(new MyPanel(Color.RED), new MyPanel(Color.GREEN), 0.3, MSideBar.OUT_FROM_BOTTOM);

        JButton switchButton = new JButton();
        switchButton.setText("Toggle");
        switchButton.setFont(new Font("Book Antiqua", Font.BOLD, 32));
        switchButton.setFocusable(false);
        switchButton.addActionListener((e) -> sideBar.toggle());

        this.add(sideBar);
        this.add(switchButton, BorderLayout.PAGE_END);

        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(ExampleFrame::new);
    }
}
