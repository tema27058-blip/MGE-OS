# Glass refinement preview

One isolated preview commit; main is not changed.

The workflow runs tools/refine_ui.py before Gradle. This deterministic source transform preserves the existing activity and fails if its expected anchors differ. The resulting APK uses FrostPanel/SystemGlyph and the transformed native activity. The original MainActivity.java in Git remains the reviewed baseline; this is build-time source generation, not a claimed direct edit of that file.

Changes: translucent sampled-wallpaper panels with cached background blur; background blur preparation off the UI thread; consistent vector controls; original app icons; reuse of up to 64 Tile views; stable screen-coordinate gesture deltas; no default wallpaper motion or discontinuous page-index parallax; 150–280 ms page transitions; preview version 2.1/201.

Existing customization, free placement, resize, photos, folders and Android widgets are retained. Actual cold-start timing, widget reuse, and gestures still need device tests. This is not a claim of stable 120 FPS or iOS optical refraction. System closing animations for other apps are unchanged.

The user's original Burgundy JPEG is not included in this commit: the connector batch-write accepts text, and the original binary has not been uploaded. It can already be selected using Settings → Choose wallpaper; FrostPanel uses that selected wallpaper. No substitute picture is presented as the exact original.

Actions: choose Build Mna Glass Preview on mna/glass-refinement; artifact Mna-Glass-Preview-APK. This workflow does not automatically merge the branch. Android compilation and UI testing have not yet been verified by the assistant.
