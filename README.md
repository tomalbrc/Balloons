# Balloons

A small **serverside mod for Fabric** that lets you attach custom balloon models to players using commands.

> ⚠️ This mod does **not** include any balloon models — you have to add your own!

---

# What it does

This mod lets you define **balloons in a config file**.
Balloons can be permanent (persist after death, does not require them to be equipped)
or based on items with the `minecraft:equippable` component or using [trinkets](https://modrinth.com/mod/trinkets-polymer)

You can add the `balloons:balloon` component to any item.

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

- `id`: Internal identifier for the balloon.
- `item` (optional): The item used to represent the balloon.
- `data.model`: The model filename (without extension or path), e.g., `"mynamespace:balloon_model"`.
- `data.animation` (optional): Name of the animation to use. Defaults to `"idle"`.
- `data.show_leash` (optional): Whether the leash is shown. Defaults to `true`.
- `data.tilt` (optional): Whether the balloon tilts (pitch) while following the entity. Defaults to `true`.
- `data.rotate` (optional): Whether the balloon rotates (yaw). Defaults to `true`.
- `data.follow_speed` (optional): How quickly the balloon follows its target. Defaults to `0.25`.
- `data.drag` (optional): Drag factor slowing the balloon's movement when the attached entity stopped moving. Defaults to `0.2`.
- `data.bob_frequency` (optional): Frequency of the bobbing motion. Defaults to `0.2`.
- `data.bob_amplitude` (optional): Amplitude (height) of the bobbing motion. Defaults to `0.2`.
- `data.offset` (optional): Position offset relative to the attached entity.

---

### 🔧 MongoDB (optional)

If you want the players active balloon to persist across multiple servers (e.g. in a network), you can enable MongoDB in the config.

If `"enabled"` is true, the mod will sync player balloon state using the configured database.  
If `username` and `password` are empty, it will try to connect without authentication.

---

# Commands

This mod adds simple commands to allow permanent attachment of the balloon to the player and to remove it.

```
/balloon show <id>
/balloon hide
```

Example:

```
/balloon show test:one
```

This will attach the balloon with ID `test:one` (from your config) to the player.
The balloon will respawn immediately after the player respawns

---

# Data storage

- By default, balloon state is stored in each player’s **Overworld player data**.
- If MongoDB is enabled, the mod uses that instead — useful for syncing across multiple servers.

---

# filament support

[Filament](https://modrinth.com/mod/filament) based items with the `balloons:balloon` will be automatically added as option to the `/balloon show <id>` command, using the items' id.
The mod will also try to load blockbench models from filament datapacks if its installed and has the model loaded. 
Make sure to specify a namespace in this case!

# Notes

- This is a backend mod only — it doesn't add items, recipes, or models directly.
- You'll need to provide your own models.
- Useful for customized servers, cosmetics, or just for fun.
