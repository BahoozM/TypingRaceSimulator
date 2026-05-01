/**
 * TypistTest.java
 *
 * Manual test suite for the Typist class.
 * Tests every required case from the assignment spec:
 *   1. Normal forward movement (typeCharacter)
 *   2. slideBack cannot go below zero
 *   3. Burnout counts down turn by turn and clears at zero
 *   4. resetToStart clears both progress and burnout
 *   5. Accuracy cannot be set outside 0.0 - 1.0
 *
 * Run with:
 *   javac Typist.java TypistTest.java
 *   java TypistTest
 */
public class TypistTest
{
    // ── tiny helper fields ──────────────────────────────────────────
    private static int passed = 0;
    private static int failed = 0;

    // ── assertion helpers ───────────────────────────────────────────

    private static void check(String testName, boolean condition)
    {
        if (condition)
        {
            System.out.println("  PASS  " + testName);
            passed++;
        }
        else
        {
            System.out.println("  FAIL  " + testName);
            failed++;
        }
    }

    private static void header(String section)
    {
        System.out.println();
        System.out.println("===========================================");
        System.out.println("  " + section);
        System.out.println("===========================================");
    }

    // ── main ────────────────────────────────────────────────────────

    public static void main(String[] args)
    {
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║       TYPIST CLASS — TEST SUITE          ║");
        System.out.println("╚══════════════════════════════════════════╝");

        testConstructor();
        testTypeCharacter();
        testSlideBack();
        testBurnout();
        testResetToStart();
        testSetAccuracy();
        testSetSymbol();
        testCombinedScenario();

        // ── summary ─────────────────────────────────────────────────
        System.out.println();
        System.out.println("===========================================");
        System.out.println("  RESULTS:  " + passed + " passed,  " + failed + " failed");
        System.out.println("===========================================");
        if (failed == 0)
            System.out.println("  All tests passed!");
        else
            System.out.println("  Some tests FAILED — check output above.");
    }

    // ================================================================
    //  TEST 1 — Constructor sets fields correctly
    // ================================================================
    private static void testConstructor()
    {
        header("TEST 1 — Constructor");

        Typist t = new Typist('①', "TURBOFINGERS", 0.85);

        check("getName()    returns 'TURBOFINGERS'",    t.getName().equals("TURBOFINGERS"));
        check("getSymbol()  returns '①'",               t.getSymbol() == '①');
        check("getAccuracy() returns 0.85",             t.getAccuracy() == 0.85);
        check("getProgress() starts at 0",              t.getProgress() == 0);
        check("isBurntOut()  starts false",             !t.isBurntOut());
        check("getBurnoutTurnsRemaining() starts at 0", t.getBurnoutTurnsRemaining() == 0);
    }

    // ================================================================
    //  TEST 2 — typeCharacter advances progress by 1 each call
    // ================================================================
    private static void testTypeCharacter()
    {
        header("TEST 2 — typeCharacter (normal forward movement)");

        Typist t = new Typist('①', "TURBOFINGERS", 0.85);

        System.out.println("  Starting progress: " + t.getProgress());

        t.typeCharacter();
        check("After 1 typeCharacter → progress == 1", t.getProgress() == 1);

        t.typeCharacter();
        check("After 2 typeCharacters → progress == 2", t.getProgress() == 2);

        t.typeCharacter();
        t.typeCharacter();
        t.typeCharacter();
        check("After 5 typeCharacters → progress == 5", t.getProgress() == 5);

        System.out.println("  Final progress: " + t.getProgress());
    }

    // ================================================================
    //  TEST 3 — slideBack never lets progress go below zero
    // ================================================================
    private static void testSlideBack()
    {
        header("TEST 3 — slideBack (cannot go below zero)");

        Typist t = new Typist('②', "QWERTY_QUEEN", 0.60);

        // Advance a few steps first
        t.typeCharacter(); // 1
        t.typeCharacter(); // 2
        t.typeCharacter(); // 3
        System.out.println("  Progress before slideBack: " + t.getProgress()); // should be 3

        t.slideBack(2);
        check("slideBack(2) from 3 → progress == 1", t.getProgress() == 1);
        System.out.println("  Progress after slideBack(2): " + t.getProgress());

        t.slideBack(1);
        check("slideBack(1) from 1 → progress == 0", t.getProgress() == 0);
        System.out.println("  Progress after slideBack(1): " + t.getProgress());

        // Critical test: slide from 0 — must stay at 0, not go negative
        t.slideBack(5);
        check("slideBack(5) from 0 → progress stays 0 (not -5)", t.getProgress() == 0);
        System.out.println("  Progress after slideBack(5) from zero: " + t.getProgress());

        // Another edge: slide by more than current progress
        t.typeCharacter(); // 1
        t.slideBack(10);
        check("slideBack(10) from 1 → progress clamped to 0", t.getProgress() == 0);
        System.out.println("  Progress after slideBack(10) from 1: " + t.getProgress());
    }

    // ================================================================
    //  TEST 4 — Burnout counts down each turn and clears at zero
    // ================================================================
    private static void testBurnout()
    {
        header("TEST 4 — burnOut and recoverFromBurnout");

        Typist t = new Typist('③', "HUNT_N_PECK", 0.30);

        // Before burnout
        check("Before burnOut: isBurntOut() == false", !t.isBurntOut());
        check("Before burnOut: turnsRemaining == 0",    t.getBurnoutTurnsRemaining() == 0);

        // Apply 3-turn burnout
        t.burnOut(3);
        System.out.println("  Burnt out for 3 turns");
        check("After burnOut(3): isBurntOut() == true",       t.isBurntOut());
        check("After burnOut(3): turnsRemaining == 3",        t.getBurnoutTurnsRemaining() == 3);

        // Turn 1 of recovery
        t.recoverFromBurnout();
        System.out.println("  After recoverFromBurnout() #1: turns = " + t.getBurnoutTurnsRemaining());
        check("After recover #1: turnsRemaining == 2",        t.getBurnoutTurnsRemaining() == 2);
        check("After recover #1: still burnt out",            t.isBurntOut());

        // Turn 2 of recovery
        t.recoverFromBurnout();
        System.out.println("  After recoverFromBurnout() #2: turns = " + t.getBurnoutTurnsRemaining());
        check("After recover #2: turnsRemaining == 1",        t.getBurnoutTurnsRemaining() == 1);
        check("After recover #2: still burnt out",            t.isBurntOut());

        // Turn 3 — should fully recover
        t.recoverFromBurnout();
        System.out.println("  After recoverFromBurnout() #3: turns = " + t.getBurnoutTurnsRemaining());
        check("After recover #3: turnsRemaining == 0",        t.getBurnoutTurnsRemaining() == 0);
        check("After recover #3: isBurntOut() == false",      !t.isBurntOut());

        // recoverFromBurnout on a healthy typist should do nothing
        t.recoverFromBurnout();
        check("recover when not burnt out: no effect",        !t.isBurntOut() && t.getBurnoutTurnsRemaining() == 0);
    }

    // ================================================================
    //  TEST 5 — resetToStart clears progress AND burnout
    // ================================================================
    private static void testResetToStart()
    {
        header("TEST 5 — resetToStart");

        Typist t = new Typist('①', "TURBOFINGERS", 0.85);

        // Give them some progress
        for (int i = 0; i < 10; i++) t.typeCharacter();
        System.out.println("  Progress before reset: " + t.getProgress()); // 10

        // Also put them in burnout
        t.burnOut(5);
        System.out.println("  Burnout turns before reset: " + t.getBurnoutTurnsRemaining()); // 5

        // Now reset
        t.resetToStart();
        System.out.println("  Progress after resetToStart(): " + t.getProgress());
        System.out.println("  BurntOut after resetToStart(): " + t.isBurntOut());
        System.out.println("  BurnoutTurns after resetToStart(): " + t.getBurnoutTurnsRemaining());

        check("After resetToStart: progress == 0",            t.getProgress() == 0);
        check("After resetToStart: isBurntOut() == false",    !t.isBurntOut());
        check("After resetToStart: turnsRemaining == 0",      t.getBurnoutTurnsRemaining() == 0);
    }

    // ================================================================
    //  TEST 6 — setAccuracy clamps to [0.0, 1.0]
    // ================================================================
    private static void testSetAccuracy()
    {
        header("TEST 6 — setAccuracy (clamping to 0.0 - 1.0)");

        Typist t = new Typist('②', "QWERTY_QUEEN", 0.60);

        // Normal valid value
        t.setAccuracy(0.75);
        check("setAccuracy(0.75) → getAccuracy() == 0.75", t.getAccuracy() == 0.75);
        System.out.println("  Set 0.75 → result: " + t.getAccuracy());

        // Exact boundary — 0.0
        t.setAccuracy(0.0);
        check("setAccuracy(0.0) → getAccuracy() == 0.0",   t.getAccuracy() == 0.0);
        System.out.println("  Set 0.0 → result: " + t.getAccuracy());

        // Exact boundary — 1.0
        t.setAccuracy(1.0);
        check("setAccuracy(1.0) → getAccuracy() == 1.0",   t.getAccuracy() == 1.0);
        System.out.println("  Set 1.0 → result: " + t.getAccuracy());

        // Too low — must clamp to 0.0
        t.setAccuracy(-0.5);
        check("setAccuracy(-0.5) → clamped to 0.0",        t.getAccuracy() == 0.0);
        System.out.println("  Set -0.5 → clamped to: " + t.getAccuracy());

        // Too high — must clamp to 1.0
        t.setAccuracy(1.5);
        check("setAccuracy(1.5) → clamped to 1.0",         t.getAccuracy() == 1.0);
        System.out.println("  Set 1.5 → clamped to: " + t.getAccuracy());

        // Very extreme values
        t.setAccuracy(-999);
        check("setAccuracy(-999) → clamped to 0.0",        t.getAccuracy() == 0.0);

        t.setAccuracy(999);
        check("setAccuracy(999) → clamped to 1.0",         t.getAccuracy() == 1.0);

        // Constructor also clamps (accuracy passed as 1.5 in constructor)
        Typist t2 = new Typist('③', "TEST", 1.5);
        check("Constructor with 1.5 → clamped to 1.0",     t2.getAccuracy() == 1.0);

        Typist t3 = new Typist('③', "TEST", -1.0);
        check("Constructor with -1.0 → clamped to 0.0",    t3.getAccuracy() == 0.0);
    }

    // ================================================================
    //  TEST 7 — setSymbol changes the symbol
    // ================================================================
    private static void testSetSymbol()
    {
        header("TEST 7 — setSymbol");

        Typist t = new Typist('①', "TURBOFINGERS", 0.85);
        check("Initial symbol == '①'", t.getSymbol() == '①');

        t.setSymbol('★');
        check("After setSymbol('★') → getSymbol() == '★'", t.getSymbol() == '★');
        System.out.println("  Symbol changed to: " + t.getSymbol());
    }

    // ================================================================
    //  TEST 8 — Combined real-race scenario
    // ================================================================
    private static void testCombinedScenario()
    {
        header("TEST 8 — Combined race scenario");

        Typist t = new Typist('①', "TURBOFINGERS", 0.85);

        System.out.println("  Simulating 5 typeCharacter calls...");
        for (int i = 0; i < 5; i++) t.typeCharacter();
        check("Progress at 5 after typing", t.getProgress() == 5);

        System.out.println("  slideBack(3) — progress should be 2");
        t.slideBack(3);
        check("Progress at 2 after slideBack(3)", t.getProgress() == 2);

        System.out.println("  burnOut(2) then recover twice");
        t.burnOut(2);
        check("isBurntOut after burnOut(2)", t.isBurntOut());

        t.recoverFromBurnout(); // turn 1
        check("Still burnt out after 1 recover", t.isBurntOut());

        t.recoverFromBurnout(); // turn 2
        check("Recovered after 2nd recover", !t.isBurntOut());

        System.out.println("  Progress still intact after burnout: " + t.getProgress());
        check("Progress unchanged during burnout (still 2)", t.getProgress() == 2);

        System.out.println("  resetToStart — everything should clear");
        t.resetToStart();
        check("Progress 0 after reset", t.getProgress() == 0);
        check("Not burnt out after reset", !t.isBurntOut());
    }
}
