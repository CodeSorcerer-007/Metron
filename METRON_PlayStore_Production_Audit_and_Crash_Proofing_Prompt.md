# METRON — ULTIMATE PRODUCTION RELEASE, CRASH-PROOFING & PLAY STORE AUDIT PROMPT

You are now acting as the **entire senior production engineering, Android QA, cybersecurity, performance, UX, release engineering, and Google Play compliance team** for this application.

The application is **METRON**, a native Android, offline-first expense and personal finance application.

Your job is NOT simply to review the code.

Your job is to take the **entire existing project**, aggressively audit it, identify every possible defect, reproduce failures, fix root causes, improve architecture where necessary, test the fixes, and prepare the application for a real Google Play production release.

The final result must be a **production-grade application**, not a prototype.

---

# 1. MOST IMPORTANT OBJECTIVE

Your objective is:

> **Find every realistic way this application can fail, crash, freeze, corrupt data, lose user data, behave incorrectly, become unusable, violate Android/Google Play requirements, leak private information, or degrade badly — then fix those problems at the source.**

Do not merely document problems.

Do not merely recommend fixes.

Do not provide a “things to fix later” list.

**Fix the code.**

After identifying an issue:

1. Reproduce it if possible.
2. Determine the root cause.
3. Implement the correct fix.
4. Re-run the relevant tests.
5. Verify that the fix did not introduce regressions.
6. Continue auditing.

Do this repeatedly until no known critical or high-severity issue remains.

---

# 2. IMPORTANT REALITY CHECK

Do NOT claim that software can mathematically be guaranteed to “never crash on any possible device.”

Instead, establish an extremely high production-quality bar:

- Zero known reproducible crash paths
- Zero known ANR paths
- Zero known data-loss paths
- Zero known database-corruption paths
- Zero known release-blocking defects
- Zero known security vulnerabilities in the application code
- Zero unresolved build errors
- Zero unresolved critical warnings
- Zero broken core workflows
- Zero placeholder/TODO functionality in production features

The phrase **“production ready” must mean something concrete.**

Do not declare success simply because:

> “The app builds.”

A successful build is only the beginning.

---

# 3. FIRST STEP — FULL PROJECT FORENSIC AUDIT

Before making changes, inspect the ENTIRE project.

Audit:

- Every Kotlin file
- Every Compose file
- Every XML file if present
- Every Gradle file
- Version catalogs
- Manifest
- ProGuard/R8 configuration
- Room database
- Migrations
- Data models
- Repositories
- ViewModels
- Use cases
- Navigation
- State handling
- Resources
- Strings
- Themes
- Drawables
- Icons
- Assets
- Tests
- Build variants
- Signing configuration
- Dependencies
- Plugins
- Android configuration
- Backup/export code
- Notification code
- Widget code
- File handling
- Permission handling
- Biometric/security code
- Error handling
- Logging
- Coroutines
- Threads
- Lifecycle handling

Do not assume code is correct because it currently appears to work.

Inspect the implementation.

---

# 4. ARCHITECTURAL REVIEW

Evaluate whether the application follows a maintainable modern Android architecture.

Check:

- Separation of UI and business logic
- State ownership
- Repository boundaries
- Database access
- Coroutine usage
- lifecycle awareness
- dependency injection if used
- navigation state
- configuration changes
- process recreation
- application state restoration

Identify:

- God classes
- God composables
- circular dependencies
- duplicated logic
- mutable global state
- unsafe singletons
- hidden side effects
- lifecycle leaks
- tightly coupled components
- accidental recompositions
- race conditions

Refactor architecture where necessary.

Do not preserve bad architecture merely because it already exists.

---

# 5. COMPLETE CRASH AUDIT

Search aggressively for every possible exception and unsafe operation.

Look for:

- NullPointerException
- IllegalStateException
- IllegalArgumentException
- IndexOutOfBoundsException
- NumberFormatException
- Date/time parsing errors
- Serialization errors
- FileNotFoundException
- SecurityException
- SQLite/Room exceptions
- Migration failures
- Transaction failures
- Concurrent modification
- Coroutine cancellation bugs
- Activity lifecycle crashes
- Fragment/lifecycle crashes if applicable
- Compose state errors
- Navigation crashes
- Saved-state restoration crashes
- Parcelable problems
- Binder/IPC failures
- Bitmap/OOM errors
- Permission-related crashes
- Provider/file URI failures
- Intent resolution failures
- Deep-link failures

Do not simply wrap everything in:

```kotlin
try {
    ...
} catch (e: Exception) {
    ...
}
```

That is NOT acceptable.

Do not hide bugs.

Fix root causes.

Use exception handling only where the failure is genuinely recoverable.

---

# 6. CRASH-PATH ANALYSIS

For every user-facing feature, ask:

> “What happens if every assumption this code makes is false?”

Examples:

- Database is empty
- Database contains 50,000+ transactions
- Category no longer exists
- Account was deleted
- Transaction references invalid data
- User changes system language
- User rotates the device
- User backgrounds the app during a save
- User kills the app during a database write
- User presses buttons extremely quickly
- User repeatedly opens and closes screens
- User restores an older backup
- User imports malformed JSON
- User imports malformed CSV
- User selects a corrupted file
- User denies permissions
- User revokes permissions later
- Storage is almost full
- Storage is completely full
- Device is in battery saver mode
- Device is in airplane mode
- Device is offline
- Device is low on memory
- Android kills the process
- App is restored after process death
- App starts after a system reboot

Every important scenario must have safe behavior.

---

# 7. ANR / FREEZE AUDIT

Treat application freezes as seriously as crashes.

Inspect:

- Main-thread database work
- Main-thread file operations
- Main-thread image processing
- Main-thread JSON/CSV parsing
- Main-thread PDF generation
- Main-thread encryption
- Main-thread bitmap manipulation
- Blocking network calls
- Blocking I/O
- Long synchronous loops
- Large dataset processing
- Expensive Compose computations
- Incorrect coroutine dispatchers
- Deadlocks
- Mutex misuse
- Infinite loops
- Recursive operations
- excessive recomposition

The UI must remain responsive.

Google Play treats user-perceived ANR rate and crash rate as core quality metrics, and excessive values can hurt discoverability.

---

# 8. DATABASE & DATA-INTEGRITY AUDIT

METRON is a financial application.

**Data integrity is more important than visual polish.**

Audit:

- Room entities
- Foreign keys
- Indexes
- Transactions
- Constraints
- Nullability
- Numeric precision
- Decimal handling
- Currency handling
- Date/time storage
- Ordering
- Deletion behavior
- Referential integrity
- Concurrent writes
- Database migrations
- Rollback behavior
- Corrupted database behavior
- Backup restoration
- Import behavior

Test:

- Empty database
- One transaction
- Thousands of transactions
- Tens of thousands of transactions
- Very large transaction amounts
- Decimal amounts
- Different currencies
- Large dates
- Future dates
- Old historical dates
- Duplicate records
- Deleted categories
- Deleted accounts
- Deleted merchants

Never allow silent data corruption.

---

# 9. FINANCIAL CALCULATION AUDIT

Because this is a money application, audit every calculation.

Verify:

- Addition
- Subtraction
- Totals
- Category totals
- Budget remaining
- Budget percentage
- Monthly totals
- Weekly totals
- Daily totals
- Income
- Expense
- Net balance
- Recurring calculations
- Split transactions
- Transfers
- Currency handling
- Rounding

Do NOT use floating-point arithmetic where it can introduce unacceptable monetary precision problems.

Use an appropriate exact/decimal representation.

Create automated tests for financial calculations.

Test extreme values.

Test rounding.

Test negative values.

Test zero.

Test decimal values.

Test very large values.

---

# 10. OFFLINE-FIRST VERIFICATION

METRON is intentionally offline.

Verify that the application functions correctly with:

- Airplane Mode enabled
- Wi-Fi disabled
- Mobile data disabled
- No network connectivity

The core application must remain fully functional.

Audit permissions and dependencies.

Prefer that the application requires **no network permission** unless a genuine feature explicitly needs it.

Verify that no accidental SDK or library is trying to communicate externally.

Inspect:

- Network permissions
- Analytics SDKs
- Advertising SDKs
- Crash reporting SDKs
- Telemetry libraries
- Remote configuration
- Background network jobs
- hidden network dependencies

The user's financial information must not leave the device.

---

# 11. PRIVACY & SECURITY AUDIT

Treat all financial information as sensitive.

Audit:

- Local storage
- Preferences
- Database
- Logs
- Backup
- Export
- Temporary files
- Cache
- Receipt images
- Notifications
- Clipboard
- Screenshots
- Accessibility exposure
- Debug logging

Search for financial information accidentally appearing in:

- Logcat
- crash logs
- analytics
- notification previews
- temporary files
- backups
- debug output

Implement appropriate:

- Encryption
- Secure storage
- biometric authentication
- PIN protection
- screenshot/privacy protection where appropriate

Do not add security theater.

Use security mechanisms only where they provide real protection.

---

# 12. RECEIPT / IMAGE HANDLING

If METRON supports receipt images:

Test:

- Very large photos
- Extremely high-resolution images
- Corrupted images
- Unsupported formats
- Deleted files
- Missing files
- Low storage
- Rotation
- Image cropping
- Image compression
- Repeated image loading

Prevent:

- OutOfMemoryError
- UI freezes
- corrupted references
- orphaned files
- accidental deletion of unrelated files

Use appropriate image loading and memory management.

---

# 13. IMPORT / EXPORT AUDIT

Test every supported import/export format.

For CSV:

- Empty files
- Missing columns
- Extra columns
- Invalid values
- Broken rows
- commas in merchant names
- quoted values
- Unicode
- malformed data

For JSON:

- Invalid syntax
- Missing fields
- Unknown fields
- Old schema
- Future schema
- Null values
- Invalid IDs
- duplicate IDs
- malformed dates
- corrupted files

Import must fail safely.

Never partially destroy existing user data because an import file is malformed.

Use transactional import where appropriate.

---

# 14. BACKUP & RESTORE AUDIT

Simulate:

1. Create large dataset.
2. Export.
3. Delete application data.
4. Reinstall.
5. Import.
6. Verify data integrity.

Also test:

- Partial backups
- corrupt backups
- old backups
- duplicate restoration
- incompatible schema
- missing attachments
- large backups

Do not claim backup support unless it has actually been tested.

---

# 15. ANDROID LIFECYCLE TESTING

Test:

- Screen rotation
- Configuration changes
- Backgrounding
- Foregrounding
- Process death
- app restoration
- device reboot
- app force-stop
- app removal from recents
- low-memory process recreation
- multitasking
- split screen where applicable

Every critical user operation must behave correctly after lifecycle changes.

---

# 16. NAVIGATION AUDIT

Test:

- Rapid navigation
- Back button
- Predictive back
- Deep links if present
- Duplicate navigation
- Navigation during async operations
- Navigation after process death
- navigation to deleted entities
- invalid destinations

Look for:

- blank screens
- lost state
- navigation loops
- crashes
- duplicate destinations
- stale UI

---

# 17. JETPACK COMPOSE AUDIT

Inspect for:

- improper state hoisting
- unstable state
- unnecessary recomposition
- infinite recomposition
- side-effect misuse
- LaunchedEffect misuse
- DisposableEffect misuse
- remember misuse
- incorrect key usage
- state restoration problems
- snapshot errors
- expensive calculations inside composition

Use:

- remember
- derivedStateOf
- stable models
- lifecycle-aware collection
- appropriate state holders

where justified.

Do not optimize blindly.

Measure first.

---

# 18. COROUTINE & CONCURRENCY AUDIT

Search for:

- GlobalScope
- unmanaged jobs
- leaked coroutines
- cancellation bugs
- incorrect dispatchers
- races
- concurrent database writes
- overlapping saves
- stale state writes
- duplicate operations

Use structured concurrency.

Ensure operations cancel correctly when appropriate.

Make user actions idempotent where possible.

---

# 19. UI INTERACTION STRESS TEST

Act like an aggressive real user.

Try:

- Double tap
- Triple tap
- 20 rapid taps
- Rapid screen switching
- Add and immediately delete
- Add and immediately edit
- Save repeatedly
- Rotate during save
- Background during save
- Press back repeatedly
- Search while typing extremely fast
- Clear fields repeatedly
- Enter extremely long strings
- Paste huge strings
- Swipe rapidly

The app must remain stable.

---

# 20. INPUT VALIDATION AUDIT

Test every input.

Examples:

- Empty string
- Whitespace
- Extremely long string
- Special characters
- Unicode
- Emoji
- Negative numbers
- Zero
- Huge numbers
- Decimal values
- malformed dates
- malformed currency
- invalid selection states

Never allow invalid input to crash or corrupt the application.

Provide clear user feedback.

---

# 21. LOCALIZATION & INTERNATIONALIZATION

Even if the first release targets one primary market, make the application structurally safe for localization.

Test:

- English
- Indian numbering formats where appropriate
- currency symbols
- long text
- different date formats
- 12/24-hour time
- Unicode
- RTL layouts
- large fonts
- accessibility font scaling

Ensure text does not silently overflow.

---

# 22. ACCESSIBILITY AUDIT

Test:

- TalkBack
- Large text
- Font scaling
- High contrast
- Touch targets
- Content descriptions
- Keyboard navigation where applicable
- Reduced motion
- Color-independent meaning

A visually beautiful application is not finished if it becomes unusable with accessibility settings enabled.

---

# 23. DARK MODE / LIGHT MODE AUDIT

Test every screen in:

- Light mode
- Dark mode
- System-controlled theme
- Dynamic color if implemented

Look for:

- invisible text
- poor contrast
- incorrect icons
- bad disabled states
- broken charts
- unreadable secondary text
- strange shadows
- incorrect status/navigation bar colors

---

# 24. PERFORMANCE AUDIT

Measure:

- Cold startup
- Warm startup
- Screen transitions
- Database queries
- Search latency
- Chart rendering
- Image loading
- scrolling
- memory usage
- battery usage

Test large datasets.

At minimum simulate:

- 1,000 transactions
- 10,000 transactions
- 50,000 transactions
- 100,000 transactions if practical

The application must remain usable.

Avoid premature optimization, but fix measured bottlenecks.

---

# 25. MEMORY / OOM AUDIT

Search for:

- large retained bitmaps
- memory leaks
- singleton references to Activities
- long-lived Context references
- unbounded caches
- large in-memory lists
- unnecessary copies
- huge JSON strings
- oversized images

Run memory stress tests.

Make sure the application handles low-memory conditions gracefully.

---

# 26. BATTERY AUDIT

Check:

- background tasks
- WorkManager
- alarms
- notifications
- wake locks
- polling
- repeated database work
- widgets

Avoid unnecessary background activity.

Do not wake the device unless there is a genuine user-facing reason.

---

# 27. DEPENDENCY AUDIT

Inspect every dependency.

For each dependency determine:

- Why is it required?
- Is it actively maintained?
- Does it introduce network behavior?
- Does it introduce tracking?
- Does it introduce unnecessary permissions?
- Is there a safer built-in alternative?
- Does it create release risks?
- Is it compatible with the target Android version?
- Does R8 break it?

Remove unnecessary dependencies.

Do not keep libraries simply because they were used during prototyping.

---

# 28. SDK / ANDROID VERSION AUDIT

The application must meet the **current Google Play target API requirement at the time of this release**.

At the current 2026 requirement, new apps and app updates submitted to Google Play must target **Android 16 / API level 36 or higher** starting August 31, 2026. Verify the latest official requirement before release rather than blindly relying on this prompt.

Verify:

- compileSdk
- targetSdk
- minSdk
- Gradle compatibility
- Android Gradle Plugin compatibility
- Kotlin compatibility
- Compose compatibility
- Java/Kotlin JVM target
- build tools

Do not use obsolete APIs unnecessarily.

---

# 29. ANDROID MANIFEST AUDIT

Inspect the manifest line-by-line.

Remove every unnecessary:

- Permission
- Service
- Receiver
- Provider
- exported component
- intent filter

Verify all exported components are intentional.

Check:

- android:exported
- backup behavior
- data extraction rules
- file providers
- permissions
- activities
- services
- receivers

The application must request the minimum privileges necessary.

---

# 30. RELEASE BUILD AUDIT

Do NOT test only the debug build.

Build the actual **release configuration**.

Verify:

- Release build succeeds
- Signing works
- R8 works
- ProGuard rules work
- Resource shrinking works
- minification works
- obfuscation does not break functionality
- reflection-based libraries still work
- Room works
- serialization works
- file operations work
- widgets work
- notifications work
- biometric flow works

Test the exact artifact intended for Play Store upload.

---

# 31. R8 / MINIFICATION TESTING

Create a release build with minification enabled where appropriate.

Then perform a full regression test.

Pay particular attention to:

- serialization
- Room
- reflection
- navigation
- dependency injection
- widgets
- workers
- exported components

Do not simply disable R8 to make the build work.

Fix the actual shrinking/obfuscation issue.

---

# 32. RESOURCE AUDIT

Check:

- Missing resources
- duplicate resources
- unused resources
- density problems
- invalid XML
- broken drawables
- missing icons
- launcher icon configuration
- adaptive icon configuration

Verify the application looks correct across common Android form factors.

---

# 33. SCREEN / DEVICE COMPATIBILITY

Test across a realistic matrix covering:

- Small phone
- Standard phone
- Large phone
- High-density device
- Low-density device
- Different aspect ratios
- Different Android versions within supported range
- Dark mode
- Light mode
- Large fonts

Check for:

- clipping
- overflow
- broken bottom sheets
- keyboard overlap
- dialog problems
- status bar overlap
- navigation bar overlap
- edge-to-edge issues

---

# 34. KEYBOARD / IME AUDIT

Test every text and numeric input with:

- Gboard
- Hardware keyboard where relevant
- Numeric keyboard
- Decimal keyboard
- Autofill
- Paste
- Select all
- Undo/redo where applicable

Ensure the keyboard never hides the primary action.

---

# 35. DATE & TIME AUDIT

Date/time bugs are common.

Test:

- Midnight
- Month boundaries
- Year boundaries
- Leap years
- Different time zones
- Daylight saving environments
- Future transactions
- Historical transactions
- Local timezone changes

Do not store dates in a fragile format.

Use appropriate Android/Java time APIs.

---

# 36. SEARCH / FILTER AUDIT

Test combinations such as:

- Empty search
- Very long search
- Unicode search
- Merchant search
- Amount search
- Category search
- Date search
- Multiple filters
- No results
- Thousands of results

Search must remain responsive.

---

# 37. CHART / ANALYTICS AUDIT

Verify:

- Empty datasets
- One transaction
- Many transactions
- Zero values
- Negative/edge values
- Huge values
- Long category names
- Many categories
- Different date ranges

Never allow a chart to crash because there is no data.

Empty analytics should have a deliberate empty state.

---

# 38. WIDGET AUDIT

If widgets are implemented, test:

- Widget creation
- Widget removal
- Multiple widget instances
- Configuration
- Device reboot
- App process death
- Data updates
- Dark mode
- Different sizes

A broken widget can damage the perception of the entire app.

---

# 39. NOTIFICATION AUDIT

Test:

- Permission granted
- Permission denied
- Permission revoked
- Notification tap
- Notification action
- Duplicate notifications
- Reboot
- scheduled events
- timezone changes

Ensure notifications never expose sensitive financial information unexpectedly.

---

# 40. BIOMETRIC / APP LOCK AUDIT

If app lock exists, test:

- No biometric hardware
- Biometric unavailable
- Biometric enrollment changes
- User cancels authentication
- Authentication failure
- Device credential fallback
- app backgrounding
- app process death

Never lock the user out permanently because of a recoverable state.

---

# 41. ERROR MESSAGE AUDIT

Do not expose developer language to normal users.

Never show:

- stack traces
- SQL errors
- Kotlin exception messages
- internal identifiers

Instead provide:

- clear explanation
- safe recovery action
- retry where appropriate

But do not silently suppress serious errors.

---

# 42. LOGGING AUDIT

Production logs must be safe.

Search for:

- transaction amounts
- merchants
- notes
- account identifiers
- personal information
- financial data
- exported content

Remove sensitive information from logs.

Do not leave debugging statements in production.

---

# 43. DATA-LOSS AUDIT

Ask:

> “What happens if the user spends months entering data and then something goes wrong?”

Test:

- interrupted writes
- app termination during write
- storage exhaustion
- database migration
- import
- export
- restore
- uninstall/reinstall
- backup failure

Financial data must not disappear silently.

---

# 44. UI/UX PRODUCTION AUDIT

Review every screen as a world-class product designer.

Check:

- hierarchy
- typography
- spacing
- consistency
- interaction affordances
- animation
- empty states
- error states
- loading states
- disabled states
- accessibility

Remove anything unnecessary.

The app should feel premium without visual excess.

---

# 45. CORE USER JOURNEY AUDIT

Test this repeatedly:

## New user

Install → Open → Onboarding → Add first expense → See updated home screen

## Returning user

Open → Add expense → Save → View updated total

## Edit

Transaction → Edit → Save → Verify all totals

## Delete

Transaction → Delete → Undo → Verify restoration

## Search

Search → Find → Open → Edit

## Budget

Create budget → Add expenses → Verify remaining amount

## Backup

Export → Delete data → Restore → Verify

## Large dataset

Populate → Search → Filter → Analytics → Scroll → Edit → Delete

Every journey must work reliably.

---

# 46. PROPERTY-BASED TESTING

Where practical, create tests for invariants.

Examples:

- Total expenses must equal the sum of individual expenses.
- Deleting an expense must reduce the total exactly once.
- Undoing deletion must restore the exact original transaction.
- Importing and exporting should preserve required data.
- Recalculating analytics must produce deterministic results.
- Editing an expense must not create duplicates.
- Repeated taps must not create unintended duplicate transactions.

Think in terms of:

**“What must always be true?”**

Test those properties.

---

# 47. FUZZ TESTING

Where practical, fuzz:

- User input
- JSON
- CSV
- Search
- Numeric values
- dates
- transaction datasets

Try malformed data deliberately.

The goal is to discover unexpected failure paths.

---

# 48. STATIC ANALYSIS

Run all appropriate static checks.

Examples:

- Android Lint
- Kotlin compiler warnings
- Detekt if used
- dependency analysis
- API compatibility analysis
- security scanning where appropriate

Do not ignore warnings blindly.

Classify every significant warning:

- Fix
- Intentionally suppress with justification
- Remove obsolete code

Do not leave unexplained release warnings.

---

# 49. TEST COVERAGE

Prioritize meaningful coverage rather than chasing a vanity percentage.

Critical business logic must have strong automated coverage.

Especially:

- money calculations
- budget calculations
- database operations
- migrations
- import
- export
- recurrence
- analytics
- search
- filtering
- state restoration

---

# 50. PLAY STORE PRODUCTION READINESS

Audit the project against the current official Google Play requirements and policies before release.

Verify:

- target API
- App Bundle requirements
- signing
- store listing compatibility
- content rating
- target audience
- privacy requirements
- data disclosure
- permissions
- technical quality
- app functionality
- policy-sensitive behavior
- production testing requirements where applicable

Do not rely on outdated knowledge.

Where a requirement can change over time, verify it against current official Google documentation.

Google Play explicitly evaluates functional reliability and expects production apps to be stable and free from broken functionality and crashes.

---

# 51. PRE-LAUNCH REPORT

Treat the Google Play pre-launch report as a mandatory release gate.

Review:

- crashes
- ANRs
- screenshots
- device behavior
- compatibility problems
- security warnings
- test failures

Google Play can surface crash/ANR information and Logcat/video evidence from pre-launch testing, so use that evidence to fix the actual root cause.

Do not dismiss failures simply because they occurred on one device.

Investigate them.

---

# 52. ANDROID VITALS MINDSET

The application must be designed with real-world technical quality in mind.

Pay particular attention to:

- crash rate
- user-perceived crash rate
- ANR rate
- user-perceived ANR
- excessive background work
- memory usage
- battery usage

Google Play's current core quality framework uses these metrics and can reduce discoverability when apps perform poorly.

---

# 53. RELEASE CANDIDATE LOOP

Do NOT stop after the first successful build.

Use this loop:

### Build

↓

### Static analysis

↓

### Unit tests

↓

### Integration tests

↓

### UI tests

↓

### Release build

↓

### Install release artifact

↓

### Manual smoke test

↓

### Stress test

↓

### Edge-case test

↓

### Crash/ANR investigation

↓

### Fix

↓

### Rebuild

↓

### Regression test

↓

### Repeat

Continue until all release-blocking issues are eliminated.

---

# 54. ZERO-TODO RELEASE RULE

Before declaring production readiness, search the entire codebase for:

- TODO
- FIXME
- HACK
- TEMP
- mock
- placeholder
- sample-only
- fake data
- unfinished screen
- stub implementation
- unimplemented function

Every result must be reviewed.

Nothing critical may remain unfinished in the production path.

---

# 55. DEMO DATA AUDIT

Ensure test/sample/demo data cannot accidentally ship as real user data.

Do not hardcode fake financial values into production logic.

Do not accidentally ship test accounts or debug screens.

---

# 56. DEBUG BUILD AUDIT

Ensure debug-only functionality cannot accidentally be accessed by normal users.

Remove:

- debug menus
- test buttons
- fake transactions
- developer shortcuts
- test notifications
- debug logging
- development endpoints

unless explicitly intended for production.

---

# 57. SECURITY OF RELEASE BUILD

Verify that:

- release keys are handled safely
- secrets are not embedded
- API keys are not present
- private certificates are not shipped
- debug flags are disabled
- developer endpoints are absent
- logging is production-safe

Because METRON is offline, there should generally be no reason for backend credentials in the app.

---

# 58. FINAL CODE QUALITY REVIEW

After all fixes, perform another complete source-code review.

The final code should be:

- understandable
- maintainable
- modular
- testable
- consistent
- type-safe
- properly documented where necessary

Remove dead code.

Remove duplicate code.

Remove unnecessary abstractions.

Remove unused dependencies.

Remove obsolete APIs.

---

# 59. DO NOT MASK FAILURES

Never “fix” a crash by simply:

- disabling a feature
- swallowing exceptions
- disabling R8
- lowering target SDK
- removing tests
- hiding the error
- returning fake data
- skipping the failing code path

A valid fix must preserve the intended functionality whenever reasonably possible.

---

# 60. FINAL ACCEPTANCE CRITERIA

Do NOT declare METRON production-ready until ALL of the following are true:

### Build

- Release build succeeds.
- Production artifact installs correctly.
- No unresolved compilation errors.
- No release-blocking build warnings.

### Stability

- No known reproducible crash paths.
- No known reproducible ANR paths.
- No known fatal lifecycle problems.
- No known critical memory issues.

### Data

- No known data-corruption paths.
- No known silent data-loss paths.
- Database migrations work.
- Import/export works.
- Backup/restore works.

### Security

- No known critical security vulnerabilities.
- No sensitive financial data leaks in logs.
- No unnecessary permissions.
- No unnecessary network access.

### Performance

- Startup is responsive.
- UI scrolling is smooth.
- Search remains responsive with large datasets.
- Analytics remain usable with large datasets.
- Memory usage is controlled.

### UX

- Core workflows work end-to-end.
- Empty states work.
- Error states work.
- Loading states work.
- Accessibility works.
- Dark mode works.
- Light mode works.

### Android

- Lifecycle behavior is correct.
- Back navigation works.
- Process death is handled.
- Rotation/configuration changes are safe.
- Release build behaves correctly.

### Google Play

- Current target API requirement satisfied.
- App Bundle is valid.
- Signing is correct.
- Current Play technical-quality requirements reviewed.
- Current relevant Play policies reviewed.
- Pre-launch testing completed where applicable.

---

# 61. FINAL “ATTACK THE APP” TEST

Before giving the final result, assume:

> “A malicious tester is trying to break this application.”

Try to break it.

Try:

- invalid input
- malformed files
- corrupted database
- huge datasets
- rapid taps
- repeated actions
- lifecycle interruption
- low memory
- low storage
- denied permissions
- revoked permissions
- device restart
- process death
- rotation
- unusual dates
- unusual currencies
- very large numbers
- empty states
- unexpected navigation
- concurrent operations
- restore from old data
- incomplete imports

The goal is to make the application fail **safely**, not crash.

---

# 62. IF YOU FIND A PROBLEM

Do not tell me:

> “There is a crash in X.”

Instead:

1. Explain the root cause briefly.
2. Fix it in the source code.
3. Add a regression test.
4. Verify the fix.
5. Continue auditing.

Do not stop after finding one problem.

Assume there are more.

---

# 63. DO NOT ASK ME TO FIX THE ISSUES MANUALLY

You are the engineering agent responsible for the codebase.

Unless an action genuinely requires human credentials or an external Play Console operation, perform the fix yourself.

Do not leave me with instructions such as:

> “Change this file manually.”

Make the actual code changes.

---

# 64. DO NOT DECLARE SUCCESS PREMATURELY

Statements such as:

- “Looks good.”
- “Should be fine.”
- “Probably won't crash.”
- “Seems production-ready.”
- “I don't see any issues.”

are unacceptable.

Your final assessment must be based on actual:

- source inspection
- builds
- tests
- release artifact testing
- static analysis
- stress testing
- edge-case testing
- Play readiness checks

---

# 65. FINAL REPORT FORMAT

At the end, provide a structured report containing:

## A. Overall Release Status

One of:

**RELEASE BLOCKED**

or

**RELEASE CANDIDATE READY**

or

**PRODUCTION READY**

Do not use “Production Ready” unless every critical gate has actually passed.

---

## B. Bugs Found

For every issue that was found during the audit:

- Issue
- Severity
- Root cause
- Fix applied
- Test added
- Verification result

---

## C. Tests Performed

List:

- Unit tests
- Integration tests
- UI tests
- Database tests
- Migration tests
- Stress tests
- Performance tests
- Memory tests
- Accessibility tests
- Release build tests
- Device tests
- Import/export tests
- Backup/restore tests

---

## D. Release Configuration

Report:

- compileSdk
- targetSdk
- minSdk
- Kotlin version
- Android Gradle Plugin
- Compose version
- application version
- versionCode
- build type
- minification status
- signing status

---

## E. Play Store Readiness

Report whether each relevant requirement is:

- PASS
- FAIL
- NOT APPLICABLE
- NEEDS MANUAL VERIFICATION

Do not falsely mark a requirement as PASS when it requires information you cannot access.

---

## F. Remaining Risks

Be honest.

If something cannot be verified from the local project, clearly identify it.

Do not invent evidence.

---

# 66. FINAL INSTRUCTION

Treat METRON as if it is about to be installed by **hundreds of thousands of real users who trust it with months or years of personal financial data.**

A crash is bad.

A wrong financial calculation is worse.

Silent data loss is unacceptable.

Data corruption is unacceptable.

Privacy leaks are unacceptable.

Broken release builds are unacceptable.

Do not optimize for:

> “It works on my machine.”

Optimize for:

> **“This is a dependable production product.”**

The final product should feel like a world-class Android application built by a mature engineering organization.

**Audit.  
Break it.  
Fix it.  
Test it.  
Break it again.  
Fix it again.  
Repeat until the release candidate survives the attack.**

Only then declare the application ready for Google Play production release.
