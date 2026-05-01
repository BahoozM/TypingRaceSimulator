/**
 * Represents a single competitor in a typing race.
 *
 * Each typist has a name, a symbol for display, an accuracy rating,
 * a progress counter (which can go up and down), and a burnout state.
 *
 * Starter code generously abandoned by Ty Posaurus, your predecessor,
 * who typed with two fingers and considered that "good enough".
 * He left a sticky note: "the slide-back thing is optional probably".
 * It is not optional. Good luck.
 *
 * @author (your name)
 * @version 1.0
 */
public class Typist
{
    // ---------------------------------------------------------------
    // Fields
    // ---------------------------------------------------------------

    /** The typist's display name (e.g. "TURBOFINGERS"). */
    private String name;

    /** Single Unicode character shown on the race track (e.g. '①'). */
    private char symbol;

    /**
     * How many characters into the passage this typist has reached.
     * Can increase (typeCharacter) or decrease (slideBack), but never
     * falls below zero.
     */
    private int progress;

    /** True while the typist is in a burnout state and cannot type. */
    private boolean burntOut;

    /**
     * Counts down the remaining turns of burnout.
     * Zero when the typist is not burnt out.
     */
    private int burnoutTurnsRemaining;

    /**
     * Typing accuracy, clamped to the range [0.0, 1.0].
     * Higher values mean the typist types more often but also risks burnout
     * more easily.
     */
    private double accuracy;


    // ---------------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------------

    /**
     * Creates a new typist ready for a race.
     *
     * @param typistSymbol   a single Unicode character representing this typist
     *                       on screen (e.g. '①', '②', '③')
     * @param typistName     the name of the typist (e.g. "TURBOFINGERS")
     * @param typistAccuracy the typist's accuracy rating, clamped to [0.0, 1.0]
     */
    public Typist(char typistSymbol, String typistName, double typistAccuracy)
    {
        symbol   = typistSymbol;
        name     = typistName;

        // Use setAccuracy so the clamping logic is applied from the start.
        setAccuracy(typistAccuracy);

        progress              = 0;
        burntOut              = false;
        burnoutTurnsRemaining = 0;
    }


    // ---------------------------------------------------------------
    // Burnout methods
    // ---------------------------------------------------------------

    /**
     * Puts this typist into a burnout state for the given number of turns.
     * While burnt out the typist cannot type.
     *
     * @param turns the number of turns the burnout will last (should be > 0)
     */
    public void burnOut(int turns)
    {
        burntOut              = true;
        burnoutTurnsRemaining = turns;
    }

    /**
     * Reduces the burnout counter by one turn.
     * When the counter reaches zero the typist automatically recovers.
     * Has no effect if the typist is not currently burnt out.
     */
    public void recoverFromBurnout()
    {
        if (!burntOut)
        {
            return; // nothing to recover from
        }

        burnoutTurnsRemaining--;

        if (burnoutTurnsRemaining <= 0)
        {
            burnoutTurnsRemaining = 0;
            burntOut              = false;
        }
    }


    // ---------------------------------------------------------------
    // Movement methods
    // ---------------------------------------------------------------

    /**
     * Advances the typist forward by one character along the passage.
     * Should only be called when the typist is not burnt out.
     */
    public void typeCharacter()
    {
        progress++;
    }

    /**
     * Moves the typist backwards by the given number of characters (a mistype).
     * Progress cannot fall below zero — the typist cannot slide off the start.
     *
     * @param amount the number of characters to slide back (must be positive)
     */
    public void slideBack(int amount)
    {
        progress -= amount;
        if (progress < 0)
        {
            progress = 0; // clamp — cannot go behind the starting line
        }
    }

    /**
     * Resets the typist to their initial state, ready for a new race.
     * Progress returns to zero and any burnout is fully cleared.
     */
    public void resetToStart()
    {
        progress              = 0;
        burntOut              = false;
        burnoutTurnsRemaining = 0;
    }


    // ---------------------------------------------------------------
    // Getters (accessors)
    // ---------------------------------------------------------------

    /**
     * Returns the typist's accuracy rating.
     *
     * @return accuracy as a double in the range [0.0, 1.0]
     */
    public double getAccuracy()
    {
        return accuracy;
    }

    /**
     * Returns the typist's current progress through the passage.
     * Progress is the number of characters typed correctly so far.
     * This value can decrease when the typist mistypes.
     *
     * @return progress as a non-negative integer
     */
    public int getProgress()
    {
        return progress;
    }

    /**
     * Returns the name of the typist.
     *
     * @return the typist's name as a String
     */
    public String getName()
    {
        return name;
    }

    /**
     * Returns the character symbol used to represent this typist on screen.
     *
     * @return the typist's symbol as a char
     */
    public char getSymbol()
    {
        return symbol;
    }

    /**
     * Returns the number of turns of burnout remaining.
     * Returns 0 if the typist is not currently burnt out.
     *
     * @return burnout turns remaining as a non-negative integer
     */
    public int getBurnoutTurnsRemaining()
    {
        return burnoutTurnsRemaining;
    }

    /**
     * Returns true if this typist is currently burnt out, false otherwise.
     *
     * @return true if burnt out
     */
    public boolean isBurntOut()
    {
        return burntOut;
    }


    // ---------------------------------------------------------------
    // Setters (mutators)
    // ---------------------------------------------------------------

    /**
     * Sets the accuracy rating of the typist.
     * Values below 0.0 are clamped to 0.0; values above 1.0 are clamped to 1.0.
     * This validation prevents an invalid accuracy from breaking race logic.
     *
     * @param newAccuracy the new accuracy rating
     */
    public void setAccuracy(double newAccuracy)
    {
        if (newAccuracy < 0.0)
        {
            accuracy = 0.0;
        }
        else if (newAccuracy > 1.0)
        {
            accuracy = 1.0;
        }
        else
        {
            accuracy = newAccuracy;
        }
    }

    /**
     * Sets the character symbol used to represent this typist on screen.
     *
     * @param newSymbol the new symbol character
     */
    public void setSymbol(char newSymbol)
    {
        symbol = newSymbol;
    }
}