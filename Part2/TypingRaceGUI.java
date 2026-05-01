import javax.swing.*;
// GUI Version 1.0 - Java Swing Implementation
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Timer;
import java.util.TimerTask;

/**
 * TypingRaceGUI.java — Part 2: Graphical Typing Race Simulator
 *
 * A Java Swing GUI for the Typing Race game.
 * Features:
 *   - 3 typists with configurable names and accuracy
 *   - Visual race track with animated progress bars
 *   - Start / Reset buttons
 *   - Live stats: progress %, burnout status
 *   - Winner announcement dialog
 *   - Passage length selector (Short / Medium / Long)
 *
 * Run with:
 *   javac Typist.java TypingRaceGUI.java
 *   java TypingRaceGUI
 *
 * @author (your name)
 * @version 1.0
 */
public class TypingRaceGUI extends JFrame
{
    // ── Race settings ────────────────────────────────────────────────
    private static final int PASSAGE_SHORT  = 20;
    private static final int PASSAGE_MEDIUM = 40;
    private static final int PASSAGE_LONG   = 60;

    private static final double MISTYPE_BASE_CHANCE = 0.3;
    private static final int    SLIDE_BACK_AMOUNT   = 2;
    private static final int    BURNOUT_DURATION    = 3;
    private static final int    TIMER_DELAY_MS      = 250;

    // ── Typist data ──────────────────────────────────────────────────
    private Typist seat1, seat2, seat3;
    private int passageLength = PASSAGE_MEDIUM;

    // ── Swing components ─────────────────────────────────────────────
    // Top config panel
    private JTextField name1Field, name2Field, name3Field;
    private JSlider    acc1Slider, acc2Slider, acc3Slider;
    private JLabel     acc1Label,  acc2Label,  acc3Label;
    private JComboBox<String> lengthBox;

    // Race track
    private JProgressBar bar1, bar2, bar3;
    private JLabel       status1, status2, status3;
    private JLabel       title1,  title2,  title3;

    // Buttons & log
    private JButton startButton, resetButton;
    private JLabel  raceStatusLabel;

    // Timer
    private Timer    raceTimer;
    private boolean  raceRunning = false;

    // ── Colours ──────────────────────────────────────────────────────
    private static final Color BG        = new Color(18, 18, 30);
    private static final Color PANEL_BG  = new Color(28, 28, 45);
    private static final Color ACCENT1   = new Color(99, 179, 237);   // blue
    private static final Color ACCENT2   = new Color(154, 230, 180);  // green
    private static final Color ACCENT3   = new Color(252, 176, 69);   // amber
    private static final Color BURNT     = new Color(220, 80,  80);   // red
    private static final Color TEXT_MAIN = new Color(237, 237, 255);
    private static final Color TEXT_DIM  = new Color(140, 140, 180);

    // ================================================================
    //  Constructor — build the UI
    // ================================================================
    public TypingRaceGUI()
    {
        super("Typing Race Simulator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 620);
        setMinimumSize(new Dimension(640, 560));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout(10, 10));

        add(buildTopPanel(),    BorderLayout.NORTH);
        add(buildTrackPanel(),  BorderLayout.CENTER);
        add(buildBottomPanel(), BorderLayout.SOUTH);

        initTypists();
        setVisible(true);
    }

    // ================================================================
    //  UI Builders
    // ================================================================

    /** Top panel: typist names, accuracy sliders, passage length. */
    private JPanel buildTopPanel()
    {
        JPanel outer = darkPanel();
        outer.setLayout(new BorderLayout(6, 6));
        outer.setBorder(new EmptyBorder(10, 12, 6, 12));

        // Title
        JLabel heading = new JLabel("⌨  Typing Race Simulator", SwingConstants.CENTER);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 20));
        heading.setForeground(TEXT_MAIN);
        heading.setBorder(new EmptyBorder(0, 0, 8, 0));
        outer.add(heading, BorderLayout.NORTH);

        // Three typist config columns
        JPanel cols = new JPanel(new GridLayout(1, 3, 10, 0));
        cols.setOpaque(false);

        name1Field = new JTextField("TURBOFINGERS");
        name2Field = new JTextField("QWERTY_QUEEN");
        name3Field = new JTextField("HUNT_N_PECK");

        acc1Slider = makeSlider(85); acc1Label = dimLabel("0.85");
        acc2Slider = makeSlider(60); acc2Label = dimLabel("0.60");
        acc3Slider = makeSlider(30); acc3Label = dimLabel("0.30");

        acc1Slider.addChangeListener(e -> acc1Label.setText(sliderToStr(acc1Slider)));
        acc2Slider.addChangeListener(e -> acc2Label.setText(sliderToStr(acc2Slider)));
        acc3Slider.addChangeListener(e -> acc3Label.setText(sliderToStr(acc3Slider)));

        cols.add(typistConfigPanel("Typist 1", ACCENT1, name1Field, acc1Slider, acc1Label));
        cols.add(typistConfigPanel("Typist 2", ACCENT2, name2Field, acc2Slider, acc2Label));
        cols.add(typistConfigPanel("Typist 3", ACCENT3, name3Field, acc3Slider, acc3Label));
        outer.add(cols, BorderLayout.CENTER);

        // Passage length selector
        JPanel lengthRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        lengthRow.setOpaque(false);
        JLabel lbl = dimLabel("Passage length:");
        lengthBox = new JComboBox<>(new String[]{"Short (20)", "Medium (40)", "Long (60)"});
        lengthBox.setSelectedIndex(1);
        styleCombo(lengthBox);
        lengthRow.add(lbl);
        lengthRow.add(lengthBox);
        outer.add(lengthRow, BorderLayout.SOUTH);

        return outer;
    }

    /** Centre panel: three race lanes with progress bars. */
    private JPanel buildTrackPanel()
    {
        JPanel track = darkPanel();
        track.setLayout(new GridLayout(3, 1, 0, 8));
        track.setBorder(new EmptyBorder(6, 12, 6, 12));

        bar1 = makeBar(ACCENT1); bar2 = makeBar(ACCENT2); bar3 = makeBar(ACCENT3);

        title1  = laneTitle("① TURBOFINGERS", ACCENT1);
        title2  = laneTitle("② QWERTY_QUEEN",  ACCENT2);
        title3  = laneTitle("③ HUNT_N_PECK",   ACCENT3);

        status1 = dimLabel("Ready");
        status2 = dimLabel("Ready");
        status3 = dimLabel("Ready");

        track.add(buildLane(title1, bar1, status1));
        track.add(buildLane(title2, bar2, status2));
        track.add(buildLane(title3, bar3, status3));

        return track;
    }

    /** Single lane: name label + progress bar + status. */
    private JPanel buildLane(JLabel title, JProgressBar bar, JLabel status)
    {
        JPanel lane = new JPanel(new BorderLayout(6, 2));
        lane.setOpaque(false);
        lane.setBorder(new CompoundBorder(
            new LineBorder(new Color(60, 60, 90), 1, true),
            new EmptyBorder(6, 10, 6, 10)
        ));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(title,  BorderLayout.WEST);
        top.add(status, BorderLayout.EAST);

        lane.add(top, BorderLayout.NORTH);
        lane.add(bar, BorderLayout.CENTER);
        return lane;
    }

    /** Bottom panel: Start / Reset buttons and race status text. */
    private JPanel buildBottomPanel()
    {
        JPanel bottom = darkPanel();
        bottom.setLayout(new BorderLayout(8, 4));
        bottom.setBorder(new EmptyBorder(4, 12, 10, 12));

        raceStatusLabel = new JLabel("Press Start to begin the race!", SwingConstants.CENTER);
        raceStatusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        raceStatusLabel.setForeground(TEXT_DIM);

        startButton = makeButton("▶  Start Race", new Color(56, 161, 105));
        resetButton = makeButton("↺  Reset",      new Color(80, 80, 120));
        resetButton.setEnabled(false);

        startButton.addActionListener(e -> startRace());
        resetButton.addActionListener(e -> resetRace());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 0));
        btnRow.setOpaque(false);
        btnRow.add(startButton);
        btnRow.add(resetButton);

        bottom.add(raceStatusLabel, BorderLayout.NORTH);
        bottom.add(btnRow,          BorderLayout.CENTER);
        return bottom;
    }

    // ================================================================
    //  Race Logic
    // ================================================================

    /** Create fresh Typist objects from the config fields. */
    private void initTypists()
    {
        seat1 = new Typist('①', name1Field.getText().trim(), acc1Slider.getValue() / 100.0);
        seat2 = new Typist('②', name2Field.getText().trim(), acc2Slider.getValue() / 100.0);
        seat3 = new Typist('③', name3Field.getText().trim(), acc3Slider.getValue() / 100.0);
    }

    /** Start the race: lock config, reset typists, begin timer. */
    private void startRace()
    {
        if (raceRunning) return;

        // Read passage length
        switch (lengthBox.getSelectedIndex())
        {
            case 0: passageLength = PASSAGE_SHORT;  break;
            case 2: passageLength = PASSAGE_LONG;   break;
            default: passageLength = PASSAGE_MEDIUM;
        }

        // Build fresh typists from current config
        initTypists();
        seat1.resetToStart(); seat2.resetToStart(); seat3.resetToStart();

        // Update lane titles
        title1.setText("①  " + seat1.getName());
        title2.setText("②  " + seat2.getName());
        title3.setText("③  " + seat3.getName());

        // Reset bars
        bar1.setMaximum(passageLength); bar1.setValue(0);
        bar2.setMaximum(passageLength); bar2.setValue(0);
        bar3.setMaximum(passageLength); bar3.setValue(0);

        status1.setText("Racing...");
        status2.setText("Racing...");
        status3.setText("Racing...");
        status1.setForeground(TEXT_DIM);
        status2.setForeground(TEXT_DIM);
        status3.setForeground(TEXT_DIM);

        raceStatusLabel.setText("Race in progress...");
        raceStatusLabel.setForeground(TEXT_DIM);

        startButton.setEnabled(false);
        resetButton.setEnabled(false);
        lockConfig(true);

        raceRunning = true;

        raceTimer = new Timer();
        raceTimer.scheduleAtFixedRate(new TimerTask()
        {
            @Override
            public void run()
            {
                SwingUtilities.invokeLater(() -> raceTurn());
            }
        }, 0, TIMER_DELAY_MS);
    }

    /** One turn of the race simulation — called on the EDT. */
    private void raceTurn()
    {
        advanceTypist(seat1);
        advanceTypist(seat2);
        advanceTypist(seat3);

        updateLane(bar1, status1, seat1, ACCENT1);
        updateLane(bar2, status2, seat2, ACCENT2);
        updateLane(bar3, status3, seat3, ACCENT3);

        // Check for winner
        if (raceFinishedBy(seat1) || raceFinishedBy(seat2) || raceFinishedBy(seat3))
        {
            raceTimer.cancel();
            raceRunning = false;

            Typist winner = raceFinishedBy(seat1) ? seat1
                          : raceFinishedBy(seat2) ? seat2
                          : seat3;

            // Boost winner accuracy
            winner.setAccuracy(winner.getAccuracy() + 0.01);

            // Update display to show 100% for winner
            JProgressBar winBar = (winner == seat1) ? bar1
                                : (winner == seat2) ? bar2 : bar3;
            winBar.setValue(passageLength);

            // Announce
            String msg = "🏆  " + winner.getName() + " wins!\n"
                       + "Final accuracy: " + String.format("%.2f", winner.getAccuracy());
            raceStatusLabel.setText("Winner: " + winner.getName() + "!");
            raceStatusLabel.setForeground(new Color(246, 210, 84));

            resetButton.setEnabled(true);
            lockConfig(false);

            JOptionPane.showMessageDialog(this, msg, "Race Over!", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /** Advance one typist by one simulation turn. */
    private void advanceTypist(Typist t)
    {
        if (t.isBurntOut())
        {
            t.recoverFromBurnout();
            return;
        }

        if (Math.random() < t.getAccuracy())
            t.typeCharacter();

        if (Math.random() < (1.0 - t.getAccuracy()) * MISTYPE_BASE_CHANCE)
            t.slideBack(SLIDE_BACK_AMOUNT);

        if (Math.random() < 0.05 * t.getAccuracy() * t.getAccuracy())
            t.burnOut(BURNOUT_DURATION);
    }

    /** Returns true if the typist has reached or passed the finish line. */
    private boolean raceFinishedBy(Typist t)
    {
        return t.getProgress() >= passageLength;
    }

    /** Update one lane's progress bar and status label. */
    private void updateLane(JProgressBar bar, JLabel status, Typist t, Color baseColor)
    {
        int prog = Math.min(t.getProgress(), passageLength);
        bar.setValue(prog);
        int pct = (int)(prog * 100.0 / passageLength);
        bar.setString(pct + "%");

        if (t.isBurntOut())
        {
            status.setText("💤 Burnt out (" + t.getBurnoutTurnsRemaining() + " turns)");
            status.setForeground(BURNT);
            bar.setForeground(BURNT);
        }
        else
        {
            status.setText("Progress: " + pct + "%  |  Accuracy: "
                         + String.format("%.2f", t.getAccuracy()));
            status.setForeground(TEXT_DIM);
            bar.setForeground(baseColor);
        }
    }

    /** Reset everything back to the initial state. */
    private void resetRace()
    {
        if (raceTimer != null) raceTimer.cancel();
        raceRunning = false;

        initTypists();
        bar1.setValue(0); bar2.setValue(0); bar3.setValue(0);
        bar1.setString("0%"); bar2.setString("0%"); bar3.setString("0%");
        bar1.setForeground(ACCENT1); bar2.setForeground(ACCENT2); bar3.setForeground(ACCENT3);

        status1.setText("Ready"); status1.setForeground(TEXT_DIM);
        status2.setText("Ready"); status2.setForeground(TEXT_DIM);
        status3.setText("Ready"); status3.setForeground(TEXT_DIM);

        title1.setText("①  " + name1Field.getText().trim());
        title2.setText("②  " + name2Field.getText().trim());
        title3.setText("③  " + name3Field.getText().trim());

        raceStatusLabel.setText("Press Start to begin the race!");
        raceStatusLabel.setForeground(TEXT_DIM);

        startButton.setEnabled(true);
        resetButton.setEnabled(false);
        lockConfig(false);
    }

    // ================================================================
    //  Helper UI methods
    // ================================================================

    private void lockConfig(boolean locked)
    {
        name1Field.setEnabled(!locked); name2Field.setEnabled(!locked); name3Field.setEnabled(!locked);
        acc1Slider.setEnabled(!locked); acc2Slider.setEnabled(!locked); acc3Slider.setEnabled(!locked);
        lengthBox.setEnabled(!locked);
    }

    private JPanel typistConfigPanel(String heading, Color accent,
                                     JTextField nameField,
                                     JSlider slider, JLabel valueLabel)
    {
        JPanel p = new JPanel(new GridLayout(4, 1, 2, 2));
        p.setOpaque(false);
        p.setBorder(new CompoundBorder(
            new LineBorder(accent.darker(), 1, true),
            new EmptyBorder(6, 8, 6, 8)
        ));

        JLabel h = new JLabel(heading, SwingConstants.CENTER);
        h.setFont(new Font("Segoe UI", Font.BOLD, 12));
        h.setForeground(accent);

        styleTextField(nameField);

        JPanel sliderRow = new JPanel(new BorderLayout(4, 0));
        sliderRow.setOpaque(false);
        JLabel accLbl = dimLabel("Accuracy:");
        sliderRow.add(accLbl,    BorderLayout.WEST);
        sliderRow.add(valueLabel, BorderLayout.EAST);

        p.add(h);
        p.add(nameField);
        p.add(sliderRow);
        p.add(slider);
        return p;
    }

    private JSlider makeSlider(int value)
    {
        JSlider s = new JSlider(0, 100, value);
        s.setOpaque(false);
        s.setForeground(TEXT_DIM);
        return s;
    }

    private JProgressBar makeBar(Color color)
    {
        JProgressBar b = new JProgressBar(0, PASSAGE_MEDIUM);
        b.setStringPainted(true);
        b.setString("0%");
        b.setForeground(color);
        b.setBackground(new Color(40, 40, 60));
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setPreferredSize(new Dimension(0, 26));
        return b;
    }

    private JLabel laneTitle(String text, Color color)
    {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        l.setForeground(color);
        return l;
    }

    private JLabel dimLabel(String text)
    {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(TEXT_DIM);
        return l;
    }

    private JButton makeButton(String text, Color bg)
    {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(160, 36));
        return b;
    }

    private JPanel darkPanel()
    {
        JPanel p = new JPanel();
        p.setBackground(PANEL_BG);
        return p;
    }

    private void styleTextField(JTextField f)
    {
        f.setBackground(new Color(40, 40, 65));
        f.setForeground(TEXT_MAIN);
        f.setCaretColor(TEXT_MAIN);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        f.setBorder(BorderFactory.createLineBorder(new Color(70, 70, 100)));
    }

    private void styleCombo(JComboBox<String> c)
    {
        c.setBackground(new Color(40, 40, 65));
        c.setForeground(TEXT_MAIN);
        c.setFont(new Font("Segoe UI", Font.PLAIN, 12));
    }

    private String sliderToStr(JSlider s)
    {
        return String.format("%.2f", s.getValue() / 100.0);
    }

    // ================================================================
    //  Main
    // ================================================================
    public static void main(String[] args)
    {
        // Use system look and feel for best rendering
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}

        SwingUtilities.invokeLater(TypingRaceGUI::new);
    }
}
