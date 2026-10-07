# Learn Color App - Project Plan

## Project name
learnColor

## Goal
Build a simple Android app for a baby or young child to learn colors.

The app should let an adult choose a color, show that color in a large area, and then let the child press a button to hear the color name spoken aloud.

## Core user story
As an adult caregiver, I want to choose a color so that my child can see it clearly and hear its name spoken.

## Functional requirements

### 1. Main screen only
The app will be a single-screen experience. There is no need for multiple screens or navigation.

### 2. Color selection
An adult can choose a color using a color wheel.

The selected color should update the display immediately.

### 3. Large color preview
The selected color should appear in a large region on the screen so the child can focus on it.

### 4. Color name button
A large button should display the current color name, such as "Red" or "Blue".

When the button is pressed, the app should speak the color name out loud using Android text-to-speech.

### 5. Basic vs Advanced mode
The app should include a mode toggle:
- Basic: shows primary/basic colors first
- Advanced: shows a wider set of colors

This should be configurable and easy for the parent to switch.

### 6. Phone-first UI
The app should be designed primarily for a phone-sized screen.

### 7. Kid-friendly design
- large touch targets
- bright, simple visual layout
- minimal distractions
- high contrast text and buttons
- no ads, no login, no unnecessary features

## Suggested color sets

### Basic colors
- Red
- Blue
- Yellow
- Green
- Orange
- Purple
- Pink
- Brown
- Black
- White
- Gray

### Advanced colors
Add more colors such as:
- Teal
- Cyan
- Magenta
- Navy
- Maroon
- Olive
- Gold
- Silver
- Lavender
- Beige

## User experience flow
1. App launches on the main learning screen.
2. Adult chooses a color from the wheel.
3. The large preview area changes to the selected color.
4. The color name is shown on the button.
5. The child presses the button.
6. The app reads the color name aloud.
7. Repeat with another color.

## Technical approach

### Android app type
Native Android app using Java or Kotlin.

Given the simplicity of the app, a single Activity is sufficient.

### Core components
- MainActivity
- Color model or data class
- Color list configuration for Basic and Advanced modes
- Color wheel UI
- Text-to-speech integration
- Single-screen layout with large preview and button

### Suggested Android features
- Material Components for simple modern UI
- TextToSpeech API for audio output
- Color wheel or custom color selection control
- RecyclerView or simple button grid if needed

## Implementation notes

### Color wheel
The app should use a standard color wheel or a simplified picker that works cleanly on phone screens.

A simple custom selection view is acceptable if a native wheel is too complex.

### Text-to-speech
When the color name button is pressed:
- read the current color name aloud
- prefer a short phrase like "Red" or "The color is red"

The speech should be clear and easy for a young child to hear.

### Basic and advanced color configuration
Keep the list of colors in a centralized place so it can be changed easily later.

For example:
- basicColors
- advancedColors

This allows easy expansion without touching the UI logic much.

## Suggested app layout
- Top section: mode selector (Basic / Advanced)
- Middle: large selected color preview
- Bottom: very large button with the color name
- Optional side panel for color wheel if needed for phone layout

## MVP milestone
The first working version should include:
- color wheel selection
- selected color preview
- large color name button
- text-to-speech output
- basic color mode
- advanced color mode toggle
- simple, clean, child-friendly UI

## Non-goals
- account login
- cloud sync
- multiplayer
- advertisements
- complex animations
- multiple screens or app navigation

## Success criteria
The app is successful if:
- an adult can quickly choose a color
- the selected color is clearly displayed
- the button says the color out loud
- the UI is easy to understand for a child
- the app feels simple and fun

## Suggested next steps for the implementing AI
1. Create a new Android project in the learnColor folder.
2. Add a single main screen layout.
3. Implement a color selection control.
4. Add a color data model with basic and advanced color sets.
5. Connect the selected color to a large preview and label.
6. Implement Android text-to-speech on button press.
7. Add a mode switch for Basic / Advanced.
8. Test on a phone-sized layout.
9. Polish the UI for children.

## Notes
This project is intentionally simple. The focus is learning, clarity, and a child-safe experience. Do not overengineer it.

The final app should feel like a dedicated educational toy, not a complicated utility app.
