# Smart Pantry Manager

A Java Android app that tracks the ingredients you have at home and suggests only the recipes
you can cook right now with the ingredients available - no shopping trip needed - to reduce food waste.

## Features
- Add, edit, delete pantry items (name, quantity, unit, optional expiry date)
- 16 pre-loaded recipes
- "Suggested Recipes" uses STRICT matching: every ingredient must be in the pantry in the required quantity
- Recipe detail screen, settings screen (expiry highlight toggle)

## Database: SQLite (SQLiteOpenHelper)
Chosen because it is local, needs no internet or server, persists after the app is closed,
and is the approach covered in the module.

## Setup / Run
1. Install Android Studio.
2. Clone: `git clone <YOUR-REPO-URL>`
3. Open the folder in Android Studio and let Gradle sync.
4. Run on an emulator or a phone (Run > Run 'app'). Minimum SDK 24.

## Author
<Jocelyn Pillay> - <402412286>
