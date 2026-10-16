# Stability 2.2 candidate

This commit changes mna/glass-refinement only. CI first runs a real Java regression test for PagePolicy, then runs the existing refine_ui.py and new stability22.py before Gradle. MainActivity remains the reviewed baseline in Git; transformations happen during the build and stop if expected anchors change.

Included: bounded navigation, explicit empty-page creation, saved-index clamping, consistent square gradient icon tiles with original colored logos, icon-style choice, Settings launcher alias, removal of the corner settings button, ARGB_8888 wallpaper decoding, less gray glass overlays. Existing customization is retained.

Not included: the user's binary Burgundy JPEG, a new system app-close animation, or verified iOS-grade refraction. If app/src/main/assets/burgundy.jpg is present, the build selects it as the default once; otherwise the current wallpaper remains and CI prints a warning. This avoids publishing a black screen or pretending the asset is bundled. The matching original JPEG exists in the update ZIP previously delivered in chat.

The Settings entry is another launcher entry in this APK, not a separate installed package or replacement for Android Settings. Choice of icon style is an app dialog, not an invented system permission. Android compilation, actual device behavior and FPS are not yet verified. No automatic merge to main is performed.
