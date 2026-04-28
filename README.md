![bstats](https://bstats.org/signatures/bukkit/waxshelf.svg)
# WaxShelf 🕯️

A lightweight and powerful Minecraft plugin that allows players to "wax" item frames and shelves to prevent unwanted interactions. Perfect for decorators, shop owners, and builders!

[![Modrinth](https://img.shields.io/modrinth/dt/waxshelf?logo=modrinth&label=Modrinth)](https://modrinth.com/plugin/waxshelf)
[![License](https://img.shields.io/github/license/dscss1/WaxShelf)](LICENSE)

## ✨ Features

*   **Waxing Mechanics:** Use **Honeycombs** to wax item frames and bookshelves. Once waxed, they cannot be rotated, changed, or broken by regular means.
*   **Unwaxing:** Use an **Axe** to remove the wax.
*   **Invisible Item Frames:** Sneak-right-click an item frame with **Shears** to make it invisible. Use **Leather** to make it visible again.
*   **Chiseled Bookshelf Support:** Fully compatible with Minecraft 1.20+ Chiseled Bookshelves (can be toggled in config).
*   **Cross-Platform:** One JAR for **Spigot**, **Paper**, and **Folia**.
*   **Hex Colors:** Use modern HEX colors (#RRGGBB) in all messages.
*   **Action Bar Feedback:** Clean notifications that don't spam the chat.
*   **Config Migration:** Automatic configuration updates.

## 🛠 Commands & Permissions

| Command | Description | Permission |
|---------|-------------|------------|
| `/waxshelf reload` | Reloads the configuration | `waxshelf.admin` |

| Permission | Description | Default |
|------------|-------------|---------|
| `waxshelf.wax` | Apply wax to items | `true` |
| `waxshelf.unwax` | Remove wax from items | `true` |
| `waxshelf.invisible` | Toggle frame invisibility | `true` |
| `waxshelf.bypass` | Interact with waxed items | `op` |
| `waxshelf.admin` | Use reload command | `op` |

## ⚙️ Configuration

The plugin generates a `config.yml` with the following options:

```yaml
# Enable or disable waxing mechanics
waxing:
  shelves: true
  chiseled-bookshelves: true
  frames: true

# Enable or disable specific mechanics
mechanics:
  shears-invisible-frames: true
  leather-visible-frames: true

# Check for updates on startup (Modrinth API)
update-checker: true
```

## 🚀 Installation

1.  Download the latest JAR from [Modrinth](https://modrinth.com/plugin/waxshelf).
2.  Drop it into your `plugins` folder.
3.  Restart your server.
4.  Enjoy your protected decorations!

---
Developed by **dscss2** with ❤️
