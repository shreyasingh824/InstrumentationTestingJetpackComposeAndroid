
## File 1 — `build.gradle` (root)

```groovy
plugins {
    id 'com.android.application'         version '8.3.2'  apply false
    id 'org.jetbrains.kotlin.android'    version '1.9.24' apply false
}
```

This is the **root** build file. It lives at the very top of the project, not inside the `app` folder.

`apply false` means — declare the plugin exists and what version to use, but do NOT apply it here. Apply it later inside `app/build.gradle`. The root file is just a version registry. This way every module in the project uses the same version automatically.

---

## File 2 — `settings.gradle`

```groovy
rootProject.name = "ComposeDemo"
include ':app'
```

This tells Gradle two things — what the project is called, and which modules exist. `:app` is the only module. In a bigger project you might see `:feature:login`, `:feature:home` etc. The `pluginManagement` and `dependencyResolutionManagement` blocks tell Gradle where to download libraries from (Google's Maven repo, Maven Central).

---

## File 3 — `gradle.properties`

```properties
android.useAndroidX=true       ← required for all androidx.* libraries
android.enableJetifier=true    ← auto-migrates old support libraries to AndroidX
org.gradle.jvmargs=-Xmx2048m  ← gives Gradle 2GB RAM (speeds up builds)
```

Without `android.useAndroidX=true` the project crashes at build time — which is exactly the error you just fixed.

---

## File 4 — `gradle/wrapper/gradle-wrapper.properties`

```properties
distributionUrl=https://services.gradle.org/distributions/gradle-8.11.1-bin.zip
```

This tells the Gradle Wrapper which exact version of Gradle to download and run the build with. When someone clones your project for the first time, this file ensures they get the same Gradle version you used — not whatever random version they happen to have installed.

---

## File 5 — `app/build.gradle`

This is the most important config file. Let me break each section:

```groovy
plugins {
    id 'com.android.application'      // this is an Android app (not a library)
    id 'org.jetbrains.kotlin.android' // Kotlin support
}
```

```groovy
android {
    namespace 'com.example.composedemo' // package name — must match your Kotlin files
    compileSdk 35                        // compile against Android 15 APIs
    
    defaultConfig {
        applicationId "com.example.composedemo" // unique ID on Play Store
        minSdk 26                                // lowest Android version supported (Android 8)
        targetSdk 35                             // optimised for Android 15
        
        // THIS LINE is critical for instrumented tests
        // It tells Android which class runs your tests on the device
        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
    }
    
    buildFeatures {
        compose true    // turn on Jetpack Compose
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion '1.5.14'  // Compose compiler version (Kotlin 1.9 style)
    }
}
```

```groovy
dependencies {
    implementation '...'          // goes into your real APK
    androidTestImplementation '...' // ONLY in the test APK — never in your real app
    debugImplementation '...'     // only in debug builds
}
```

The three types of dependency matter a lot for testing. `androidTestImplementation` is what makes a dependency available to your `src/androidTest/` code.

---

## File 6 — `AndroidManifest.xml`

```xml
<activity
    android:name=".MainActivity"
    android:exported="true">       <!-- exported=true means it can be launched -->
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>               <!-- this makes it the launch screen -->
</activity>
```

Every Activity must be declared here. The `LAUNCHER` intent-filter is what makes the app icon appear on the home screen and opens this Activity when tapped.

---

## File 7 — `MainActivity.kt`

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                var count by remember { mutableStateOf(0) }  // state lives here

                CounterScreen(
                    count = count,
                    onIncrement = { if (count < 10) count++ },
                    onDecrement = { if (count > 0) count-- },
                    onReset = { count = 0 }
                )
            }
        }
    }
}
```

`setContent {}` is the bridge between the old Android Activity world and Compose. Everything inside `setContent` is Compose. `remember { mutableStateOf(0) }` creates state that survives recomposition — when `count` changes, Compose automatically redraws `CounterScreen` with the new value. The callbacks (`onIncrement` etc.) are lambdas passed down to the screen.

---

## File 8 — `CounterScreen.kt` (production code)

```kotlin
object Tags {
    const val COUNTER_VALUE    = "counter_value"
    const val INCREMENT_BUTTON = "increment_button"
    const val DECREMENT_BUTTON = "decrement_button"
    const val RESET_BUTTON     = "reset_button"
    const val ERROR_TEXT       = "error_text"
}
```

`Tags` is defined as an `object` (singleton). Both `CounterScreen.kt` and `CounterScreenTest.kt` import and use these constants. This is the critical design decision — if you use raw strings like `"counter_value"` in both places and you make a typo, the test silently never finds the node and fails with a confusing error. Constants give you a compile-time error instead.

```kotlin
@Composable
fun CounterScreen(
    count: Int,               // the data to display — passed IN
    onIncrement: () -> Unit,  // what to do when + tapped — passed IN
    onDecrement: () -> Unit,
    onReset: () -> Unit,
) {
    // screen has NO logic of its own
    // it just renders what it is told and fires callbacks when tapped
}
```

This pattern — passing state in and callbacks out — is called "hoisting state". The screen is a pure function of its inputs. This is exactly what makes it testable: in the test you pass in exactly the state you want and capture exactly which callbacks fired.

```kotlin
Button(
    onClick = onDecrement,
    enabled = count > 0,                     // disabled at zero — test checks this
    modifier = Modifier.testTag(Tags.DECREMENT_BUTTON)  // tag for test to find it
)
```

`Modifier.testTag()` adds the node to the semantic tree with that tag. Without this line, `onNodeWithTag(Tags.DECREMENT_BUTTON)` in the test would throw `AssertionError: No node with tag 'decrement_button'`.

---

## File 9 — `CounterScreenTest.kt` (the instrumented test)

```kotlin
@RunWith(AndroidJUnit4::class)   // ← INSTRUMENTED. Device/emulator required.
class CounterScreenTest {
```

`@RunWith` tells JUnit which runner to use. `AndroidJUnit4` is Android's runner — it knows how to install your test APK, launch the test Activity, and run your tests on the device.

```kotlin
@get:Rule
val composeTestRule = createComposeRule()
```

`@get:Rule` tells JUnit "apply this rule before and after every test". `createComposeRule()` spins up a blank Activity on the device specifically to host your Composable during the test. This is why `debugImplementation 'ui-test-manifest'` is needed in `build.gradle` — that dependency adds the blank test Activity to your debug APK.

```kotlin
private fun renderScreen(count: Int = 0, ...) {
    composeTestRule.setContent {
        CounterScreen(count = count, ...)
    }
}
```

This helper reduces copy-paste. Each test only sets the parameters it cares about, everything else uses defaults. Without this you'd repeat `composeTestRule.setContent { CounterScreen(...) }` in every single test.

```kotlin
@Test
fun decrement_button_disabled_when_count_is_zero() {
    renderScreen(count = 0)                          // ARRANGE — set up the screen

    composeTestRule                                  // ASSERT — check the state
        .onNodeWithTag(Tags.DECREMENT_BUTTON)        // find the node
        .assertIsNotEnabled()                        // verify it is disabled
}
```

This is the AAA pattern — Arrange, Act, Assert. No "Act" step here because we are testing static state (is the button disabled), not a user interaction.

```kotlin
@Test
fun tapping_increment_fires_onIncrement_callback() {
    var wasCalled = false                   // start false

    renderScreen(
        count = 5,
        onIncrement = { wasCalled = true }  // flip to true when tapped
    )

    composeTestRule
        .onNodeWithTag(Tags.INCREMENT_BUTTON)
        .performClick()                     // ACT — tap the button

    assertTrue(wasCalled)                   // ASSERT — did callback fire?
}
```

This tests behaviour, not state. You cannot test "did the number increase" here because `CounterScreen` has no internal logic — it just calls `onIncrement` and waits for the parent to give it a new `count`. So the correct thing to test is "did it call the right callback".

---

## How everything connects when you run a test---

## The key design principle of the whole project

The screen and the test share the `Tags` constants. The screen puts `testTag(Tags.DECREMENT_BUTTON)` on the button. The test finds it with `onNodeWithTag(Tags.DECREMENT_BUTTON)`. They never use raw strings — so a rename is one change in one place, and the compiler tells you everywhere it needs to update.

The screen has zero logic of its own. It receives `count` as a number and fires callbacks when buttons are tapped. All the logic ("don't go below 0", "don't go above 10") lives in `MainActivity`. This means:

- The screen test only tests UI rendering — "is the button disabled?", "is the right text shown?"
- The logic would be tested separately in unit tests (which this project doesn't have yet, but would live in `src/test/`)
