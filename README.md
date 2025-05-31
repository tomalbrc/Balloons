# Balloons

A small **serverside mod for Fabric** that lets you attach custom balloon models to players using commands.

> ⚠️ This mod does **not** include any balloon models — you have to add your own!

---

# What it does

This mod lets you define **balloons in a config file**, and then allows players to **show or hide one of them** using server commands.

Each balloon is linked to a specific item, a model file, and an optional animation.

There is **no client mod required** — models are handled via `.bbmodel` or `.ajblueprint` files, and shown using item displays and a generated resourcepack.

---

# Setup

### 🪄 Add balloon models

Put your model files in:

```
config/balloons/<model>.bbmodel
```

- The model filename (without extension) becomes the internal model ID used in the config.
- Supports both `.bbmodel` (Blockbench) and `.ajblueprint` (Animated Java) formats.

---

### ⚙️ Config file

The mod reads balloon definitions from:

```
config/balloons.json
```

Here’s a minimal example:

```json
{
  "mongo_db": {
    "enabled": true,
    "host": "127.0.0.1",
    "port": 27017,
    "collection": "balloons",
    "username": "",
    "password": "",
    "database": "game",
    "authSource": "admin",
    "useSSL": false
  },
  "balloons": [
    {
      "id": "test:one",
      "item": {
        "count": 1,
        "id": "minecraft:cobblestone"
      },
      "data": {
        "model": "flower_balloon",
        "animation": "idle"
      }
    },
    {
      "id": "test:two",
      "item": {
        "count": 1,
        "id": "minecraft:stone"
      },
      "data": {
        "model": "frog_balloon",
        "animation": "idle"
      }
    }
  ]
}
```

Each balloon entry includes:
- `id`: internal identifier for the balloon
- `item`: optional item used to represent it
- `data.model`: the model filename (without extension)
- `data.animation`: name of the animation to use, if any

---

### 🔧 MongoDB (optional)

If you want the players active balloon to persist across multiple servers (e.g. in a network), you can enable MongoDB in the config.

If `"enabled"` is true, the mod will sync player balloon state using the configured database.  
If `username` and `password` are empty, it will try to connect without authentication.

---

# Commands

This mod adds simple commands to let players **show or hide a balloon**.

Available commands:

```
/balloon show <id>
/balloon hide
```

Example:

```
/balloon show test:one
```

This will attach the balloon with ID `test:one` (from your config) to the player.

---

# Data storage

- By default, balloon state is stored in each player’s **Overworld player data**.
- If MongoDB is enabled, the mod uses that instead — useful for syncing across multiple servers.

---

# filament support

The mod comes with a filament behaviour:
```
{
  "behaviour": {
    "balloon": {
      "model": "flower_balloon",
      "animation": "idle"
    }
  }
}
```

This will use the item's id for the balloon id in `/balloons activate <id>`

The mod will also try to load blockbench models from filament datapacks if its installed and has the model loaded. Make sure to specify a namespace in this case!

# Notes

- This is a backend mod only — it doesn't add items, recipes, or models directly.
- You'll need to provide your own models.
- Useful for customized servers, cosmetics, or just for fun.
