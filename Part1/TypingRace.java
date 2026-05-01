import java.util.concurrent.TimeUnit;
import java.lang.Math;

/**
 * A typing race simulation. Three typists race to complete a passage of text,
 * advancing character by character — or sliding backwards when they mistype.
 *
 * Originally written by Ty Posaurus, who left this project to "focus on his
 * two-finger technique". He assured us the code was "basically done".
 * We have found evidence to the contrary.
 *
 * Bugs fixed (see detailed notes in each method):
 *   BUG 1 — startRace(): seat3Typist was never reset before the race.
 *   BUG 2 — raceFinishedBy(): used == instead of >= so progress overshoot
 *            was never detected and the race could loop forever.
 *   BUG 3 — startRace(): winner was never announced (TODO left incomplete).
 *   BUG 4 — advanceTypist(): mistype probability scaled WITH accuracy, so
 *            more accurate typists mistyped MORE often. Should scale with
 *            (1 - accuracy) instead.
 *   BUG 5 — printSeat(): spacesAfter could go negative if the typist's
 *            progress equals passageLength (i.e. they just finished), causing
 *            multiplePrint to loop on a negative count.
 *   BUG 6 — printRace(): the legend said "[zz] = burnt out" but the code
 *            actually prints "~" after the symbol, not "[zz]". Legend updated
 *            to match the actual output.
 *   BUG 7 — startRace()/advanceTypist(): after a typist finishes the race
 *            their accuracy is never updated (spec says winner gains accuracy,
 *            burnout losers lose accuracy). Added performanceAdjustment().
 *
 * @author TyPosaurus (original) — bugs fixed by (your name)
 * @version 1.0
 */
public class TypingRace
{
    private int passageLength;   // Total characters in the passage to type
    private Typist seat1Typist;
    private Typist seat2Typist;
    private Typist seat3Typist;

    // Accuracy thresholds for mistype and burnout events
    private static final double MISTYPE_BASE_CHANCE    = 0.3;
    private static final int    SLIDE_BACK_AMOUNT      = 2;
    private static final int    BURNOUT_DURATION       = 3;

    // How much accuracy changes after a race
    private static final double ACCURACY_WIN_BOOST     = 0.01;  // winner improves
    private static final double ACCURACY_BURNOUT_DROP  = 0.005; // burnout penalty

    /**
     * Constructor for objects of class TypingRace.
     * Sets up the race with a passage of the given length.
     * Initially there are no typists seated.
     *
     * @param passageLength the number of characters in the passage to type
     */
    public TypingRace(int passageLength)
    {
        this.passageLength = passageLength;
        seat1Typist = null;
        seat2Typist = null;
        seat3Typist = null;
    }

    /**
     * Seats a typist at the given seat number (1, 2, or 3).
     *
     * @param theTypist  the typist to seat
     * @param seatNumber the seat to place them in (1–3)
     */
    public void addTypist(Typist theTypist, int seatNumber)
    {
        if (seatNumber == 1)
        {
            seat1Typist = theTypist;
        }
        else if (seatNumber == 2)
        {
            seat2Typist = theTypist;
        }
        else if (seatNumber == 3)
        {
            seat3Typist = theTypist;
        }
        else
        {
            System.out.println("Cannot seat typist at seat " + seatNumber + " — there is no such seat.");
        }
    }

    /**
     * Starts the typing race.
     * All typists are reset to the beginning, then the simulation runs
     * turn by turn until one typist completes the full passage.
     *
     * BUG 1 FIXED: The original code only called resetToStart() on seat1Typist
     * and seat2Typist. seat3Typist was never reset, so if the race was run more
     * than once (or seat3 carried leftover state), that typist would start mid-track.
     * Fix: added seat3Typist.resetToStart().
     *
     * BUG 3 FIXED: The original code had a TODO comment and never printed the
     * winner. Fix: after the loop, find which typist finished and print their name
     * and final accuracy.
     *
     * BUG 7 FIXED: The spec requires the winner to gain a small accuracy boost and
     * typists who burnt out to lose a small amount. Added performanceAdjustment().
     */
    public void startRace()
    {
        boolean finished = false;

        // Reset ALL three typists to the start of the passage.
        // BUG 1 — original code omitted seat3Typist.resetToStart()
        seat1Typist.resetToStart();
        seat2Typist.resetToStart();
        seat3Typist.resetToStart(); // FIX: was missing entirely

        // Track how many times each typist burnt out (for accuracy adjustment)
        int burnouts1 = 0;
        int burnouts2 = 0;
        int burnouts3 = 0;

        while (!finished)
        {
            // Track burnout events this turn
            boolean was1BurntOut = seat1Typist.isBurntOut();
            boolean was2BurntOut = seat2Typist.isBurntOut();
            boolean was3BurntOut = seat3Typist.isBurntOut();

            // Advance each typist by one turn
            advanceTypist(seat1Typist);
            advanceTypist(seat2Typist);
            advanceTypist(seat3Typist);

            // Count new burnout events (typist became burnt out this turn)
            if (!was1BurntOut && seat1Typist.isBurntOut()) burnouts1++;
            if (!was2BurntOut && seat2Typist.isBurntOut()) burnouts2++;
            if (!was3BurntOut && seat3Typist.isBurntOut()) burnouts3++;

            // Print the current state of the race
            printRace();

            // BUG 2 CHECK: raceFinishedBy() uses >= now (see that method)
            if ( raceFinishedBy(seat1Typist) || raceFinishedBy(seat2Typist) || raceFinishedBy(seat3Typist) )
            {
                finished = true;
            }

            // Wait 200ms between turns so the animation is visible
            try {
                TimeUnit.MILLISECONDS.sleep(200);
            } catch (Exception e) {}
        }

        // BUG 3 FIX: Announce the winner and apply performance adjustments
        // Determine the winner (the first typist whose progress >= passageLength)
        Typist winner = null;
        if (raceFinishedBy(seat1Typist)) { winner = seat1Typist; }
        else if (raceFinishedBy(seat2Typist)) { winner = seat2Typist; }
        else { winner = seat3Typist; }

        // BUG 7 FIX: Performance adjustments per spec
        double oldAccuracy = winner.getAccuracy();
        winner.setAccuracy(winner.getAccuracy() + ACCURACY_WIN_BOOST);

        // Penalise typists who burned out during the race
        applyBurnoutPenalty(seat1Typist, burnouts1);
        applyBurnoutPenalty(seat2Typist, burnouts2);
        applyBurnoutPenalty(seat3Typist, burnouts3);

        System.out.println();
        System.out.println("And the winner is... " + winner.getName() + "!");
        System.out.printf("Final accuracy: %.2f (improved from %.2f)%n",
                          winner.getAccuracy(), oldAccuracy);
    }

    /**
     * Applies a small accuracy penalty for every burnout a typist had.
     *
     * @param theTypist the typist to penalise
     * @param burnouts  how many times they burnt out during the race
     */
    private void applyBurnoutPenalty(Typist theTypist, int burnouts)
    {
        if (burnouts > 0)
        {
            theTypist.setAccuracy(theTypist.getAccuracy() - burnouts * ACCURACY_BURNOUT_DROP);
        }
    }

    /**
     * Simulates one turn for a typist.
     *
     * If the typist is burnt out, they recover one turn and skip typing.
     * Otherwise they may type, mistype, or burn out based on their accuracy.
     *
     * BUG 4 FIXED: The original mistype check was:
     *   Math.random() < theTypist.getAccuracy() * MISTYPE_BASE_CHANCE
     * This means a typist with accuracy 0.85 had a 0.85 * 0.30 = 25.5% mistype
     * chance, while a typist with accuracy 0.30 had only a 9% chance.
     * That is the opposite of what should happen — lower accuracy should mean
     * MORE mistypes, not fewer.
     * Fix: use (1.0 - accuracy) so higher accuracy means fewer mistypes.
     *
     * @param theTypist the typist to advance
     */
    private void advanceTypist(Typist theTypist)
    {
        if (theTypist.isBurntOut())
        {
            // Recovering from burnout — skip this turn
            theTypist.recoverFromBurnout();
            return;
        }

        // Attempt to type a character
        if (Math.random() < theTypist.getAccuracy())
        {
            theTypist.typeCharacter();
        }

        // BUG 4 FIX: Mistype probability must DECREASE as accuracy increases.
        // Original: Math.random() < theTypist.getAccuracy() * MISTYPE_BASE_CHANCE
        // Fixed:    Math.random() < (1.0 - theTypist.getAccuracy()) * MISTYPE_BASE_CHANCE
        if (Math.random() < (1.0 - theTypist.getAccuracy()) * MISTYPE_BASE_CHANCE)
        {
            theTypist.slideBack(SLIDE_BACK_AMOUNT);
        }

        // Burnout check — pushing too hard increases burnout risk
        // (probability scales with accuracy squared, capped at ~0.05)
        if (Math.random() < 0.05 * theTypist.getAccuracy() * theTypist.getAccuracy())
        {
            theTypist.burnOut(BURNOUT_DURATION);
        }
    }

    /**
     * Returns true if the given typist has completed the full passage.
     *
     * BUG 2 FIXED: The original used == (exact match only). However, because a
     * typist calls typeCharacter() which adds 1 each turn, their progress can
     * jump PAST passageLength in a single step (e.g. 39 -> 40 is fine, but if
     * they were at 39 and the passage is 40 they still advance to 40, which
     * works — but if we ever allow multi-character advances the overshoot case
     * is real). More critically, the spec itself says the win condition is >=,
     * so using == was directly contradicting the specification.
     * Fix: changed == to >=.
     *
     * @param theTypist the typist to check
     * @return true if their progress has reached or exceeded the passage length
     */
    private boolean raceFinishedBy(Typist theTypist)
    {
        // BUG 2 FIX: was == (could miss the finish if progress overshoots)
        return theTypist.getProgress() >= passageLength;
    }

    /**
     * Prints the current state of the race to the terminal.
     * Shows each typist's position along the passage and burnout state.
     *
     * BUG 6 FIXED: The legend originally said "[zz] = burnt out" but the actual
     * printSeat() code prints "~" after the symbol (e.g. "①~"), not "[zz]".
     * Updated the legend to match what is actually displayed.
     */
    private void printRace()
    {
        System.out.print('\u000C'); // Clear terminal

        System.out.println("  TYPING RACE — passage length: " + passageLength + " chars");
        multiplePrint('=', passageLength + 3);
        System.out.println();

        printSeat(seat1Typist);
        System.out.println();

        printSeat(seat2Typist);
        System.out.println();

        printSeat(seat3Typist);
        System.out.println();

        multiplePrint('=', passageLength + 3);
        System.out.println();
        // BUG 6 FIX: legend now matches the actual "~" marker used in printSeat()
        System.out.println("  [~] = burnt out    [<] = just mistyped");
    }

    /**
     * Prints a single typist's lane.
     *
     * BUG 5 FIXED: When a typist's progress equals passageLength, spacesAfter
     * becomes 0. If the typist is also burnt out, the code did spacesAfter--
     * making it -1, and multiplePrint('-', -1) would loop endlessly (since the
     * condition i < times with times = -1 is immediately false for i = 0 —
     * actually it would not loop, but the display would be off by one character
     * and push the closing '|' out of alignment). Added a guard to ensure
     * spacesAfter never goes below 0.
     *
     * Also added the [<] mistype marker that Ty's comment mentioned was missing.
     *
     * @param theTypist the typist whose lane to print
     */
    private void printSeat(Typist theTypist)
    {
        int progress     = theTypist.getProgress();
        int spacesBefore = progress;
        // BUG 5 FIX: cap spacesAfter at 0 so it never goes negative
        int spacesAfter  = Math.max(0, passageLength - progress);

        System.out.print('|');
        multiplePrint(' ', spacesBefore);

        System.out.print(theTypist.getSymbol());
        if (theTypist.isBurntOut())
        {
            System.out.print('~');
            // BUG 5 FIX: only subtract if spacesAfter > 0
            if (spacesAfter > 0) { spacesAfter--; }
        }

        multiplePrint(' ', spacesAfter);
        System.out.print('|');
        System.out.print(' ');

        // Print name and accuracy
        if (theTypist.isBurntOut())
        {
            System.out.print(theTypist.getName()
                + " (Accuracy: " + theTypist.getAccuracy() + ")"
                + " BURNT OUT (" + theTypist.getBurnoutTurnsRemaining() + " turns)");
        }
        else
        {
            System.out.print(theTypist.getName()
                + " (Accuracy: " + theTypist.getAccuracy() + ")");
        }
    }

    /**
     * Prints a character a given number of times.
     *
     * @param aChar the character to print
     * @param times how many times to print it (no-op if <= 0)
     */
    private void multiplePrint(char aChar, int times)
    {
        int i = 0;
        while (i < times)
        {
            System.out.print(aChar);
            i = i + 1;
        }
    }

    /**
     * Entry point for quick testing.
     * Run with: javac Typist.java TypingRace.java && java TypingRace
     */
    public static void main(String[] args)
    {
        TypingRace race = new TypingRace(40);
        race.addTypist(new Typist('①', "TURBOFINGERS", 0.85), 1);
        race.addTypist(new Typist('②', "QWERTY_QUEEN",  0.60), 2);
        race.addTypist(new Typist('③', "HUNT_N_PECK",   0.30), 3);
        race.startRace();
    }
}