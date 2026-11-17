# KillEffects

Client-side cosmetic effect collection for Fabric 26.3. Open the selection screen with **K** or `/killeffects`. Choose a card, browse the five categories, preview animations, toggle effects, and adjust the particle cap. Preferences persist in `config/killeffects.properties`.

## Included effects

Anvil Drop, Card Explosion, Thunder Strike, Firework Celebration, Arcane Burst, Particle Tornado, Treasure Reveal, Soul Explosion, Meteor Strike, and Void Portal. These are visual client particles only. Meteor Strike and Thunder Strike do not create fire or affect blocks or entities.

## Important multiplayer limitation

This release is a client-only cosmetic foundation. Minecraft clients do not receive trustworthy killer attribution for arbitrary multiplayer deaths, so automatic PvP kill triggering and other-player effect synchronization are not claimed here. Reliable automatic triggers require a server-side companion and an explicit server-to-client effect packet. Preview works locally without a kill.

## Build

Requires Java 25 and Gradle 9.8.2 or compatible. Run `gradle build` from this directory. Versions are based on Fabric's official 26.3 example project: Minecraft 26.3, Loader 0.20.1, Loom 1.19-SNAPSHOT, Fabric API 0.166.2+26.3.

Install the built jar in the `mods` folder of a Fabric 26.3 client with Fabric API installed.

## Modrinth notes

Mod ID: `killeffects`. License: MIT. This project is an unlisted development build; add screenshots and a release changelog before publishing.
