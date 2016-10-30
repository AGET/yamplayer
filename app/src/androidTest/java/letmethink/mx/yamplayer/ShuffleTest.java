package letmethink.mx.yamplayer;

import android.support.test.runner.AndroidJUnit4;
import android.support.test.rule.ActivityTestRule;
import android.support.test.filters.LargeTest;

import android.widget.ToggleButton;

import static android.support.test.espresso.matcher.ViewMatchers.isChecked;
import static android.support.test.espresso.matcher.ViewMatchers.isNotChecked;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.Rule;

/**
 * Instrumentation test will be executed on an Android device and test the
 * shuffle button behavior.
 *
 * TODO:
 *  - Test to ensure the player instance uses the shuffle button to ramdomize
 *    the song list.
 *  - Test the application read/writes to a configuration file the user's
 *    preference.
 */

@RunWith(AndroidJUnit4.class)
public class ShuffleTest {
    @Rule
    public ActivityTestRule mainActivity = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void shuffleButtonIsDisplayed() throws Exception {
        onView(withId(R.id.shuffle)).check(matches(isDisplayed()));
    }

    @Test
    public void shuffleButtonDisabledByDefault() {
        onView(withId(R.id.shuffle)).check(matches(isNotChecked()));
    }

    @LargeTest
    public void toggleShuffleButton() throws Exception {
        onView(withId(R.id.shuffle)).perform(click());
        onView(withId(R.id.shuffle)).check(matches(isChecked()));
    }
}
