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

### Config file

The mod reads balloon definitions from:

```
config/balloons.json
```

**Storage options:**

* `storage-type`: Can be `LPMETA`, `MONGODB`, `MARIADB`, `POSTGRESQL`, or `SQLITE`
* `database`: Defines connection details for databases

** `gui`: **

* Customize button positions with the config
* Adjust menu heights and titles

** `permissions`: **

Allows to assign vanilla permission levels for the luckperm nodes.

** `messages`: **

Player facing messages / strings


Here’s a minimal example:

```json
{
  "permissions": {
    "balloons.reload": 2,
    "balloons.list": 2,
    "balloons.command": 2,
    "balloons.direct": 2,
    "balloons.give": 2,
    "balloons.remove": 2
  },
  "messages": {
    "player-not-found": "Player not found",
    "back": "Back",
    "browse": "Browse All",
    "prev": "Previous Page",
    "next": "Next Page",
    "confirm": "Confirm",
    "cancel": "Cancel",
    "component-tooltip": "Balloon: %s",
    "already-owning": "<green>You own this balloon</green>",
    "added": "%s was added to your balloons!",
    "equip-tooltip": "<green>🎈</green> Press <keybind:key.attack> to equip",
    "unequip-tooltip": "<color:#800080>↔</color> Press <keybind:key.use> to unequip",
    "get-item-tooltip": "<color:#802080>↔</color> Press <keybind:key.use> to get item"
  },
  "gui": {
    "add-back-button": true,
    "back-button-item": "minecraft:arrow",
    "back-button-location": [
      1,
      1
    ],
    "selection-menu-height": 6,
    "selection-menu-title": "Select Balloon",
    "prev-button-item": "minecraft:arrow",
    "next-button-item": "minecraft:arrow",
    "prev-button-location": [
      8,
      6
    ],
    "next-button-location": [
      9,
      6
    ],
    "browse-menu-height": 6,
    "browse-menu-title": "Browse Balloons",
    "confirmation-menu-height": 1,
    "confirmation-menu-title": "Confirm",
    "add-browse-button": true,
    "browse-button-item": "minecraft:chest",
    "browse-button-location": [
      1,
      6
    ],
    "confirm-button-item": "minecraft:emerald",
    "confirm-button-location": [
      7,
      1
    ],
    "cancel-button-location": [
      3,
      1
    ]
  },
  "storage-type": "SQLITE",
  "database": {
    "host": "localhost",
    "port": 3306,
    "user": "username",
    "password": "secret",
    "filepath": "cosmetic.sqlite",
    "max-pool-size": 10,
    "ssl-enabled": false,
    "database-name": "balloon_db",
    "connection-timeout": 30000,
    "idle-timeout": 600000,
    "keepalive-time": 300000,
    "validation-timeout": 5000,
    "use-srv": false
  },
  "balloons": [
    {
      "id": "test:one",
      "item": {
        "id": "minecraft:cobblestone",
        "count": 1
      },
      "data": {
        "model": "flower_balloon"
      },
      "glint": false,
      "lore": [],
      "permission-level": 0
    },
    {
      "id": "test:two",
      "item": {
        "id": "minecraft:stone",
        "count": 1
      },
      "data": {
        "model": "satyr_balloon"
      },
      "glint": false,
      "lore": [],
      "permission-level": 0
    },
    {
      "id": "test:three",
      "item": {
        "id": "minecraft:stone",
        "count": 1
      },
      "data": {
        "model": "apple_balloon",
        "glint": true
      },
      "glint": false,
      "lore": [],
      "permission-level": 0
    },
    {
      "id": "test:segments",
      "item": {
        "id": "minecraft:stone",
        "count": 1
      },
      "data": {
        "segments": [
          {
            "model": "dragon1",
            "distance": 0.5
          },
          {
            "model": "dragon2",
            "distance": 0.5
          },
          {
            "model": "dragon3",
            "distance": 0.5
          }
        ],
        "tilt": false,
        "model": "dragon_root"
      },
      "glint": false,
      "permission-level": 0
    }
  ]
}
```

Each balloon entry includes:

- `id`: Internal identifier for the balloon.
- `item` (optional): The item used to represent the balloon. Supports components
- `title` (optional): Formatted title
- `lore` (optional): List of formatted lore lines
- `glint` (optional): Flag whether the balloon has an enchantment glint
- `data.model`: The model filename (without extension or path), e.g., `"balloon_model"` for balloons from `config/ballons/` or for filament managed models with a namespace: `"mynamespace:balloon_model"`.
- `data.animation` (optional): Name of the animation to use. Defaults to `"idle"`.
- `data.show_leash` (optional): Whether the leash is shown. Defaults to `true`.
- `data.tilt` (optional): Whether the balloon tilts (pitch) while following the entity. Defaults to `true`.
- `data.rotate` (optional): Whether the balloon rotates (yaw). Defaults to `true`.
- `data.follow_speed` (optional): How quickly the balloon follows its target. Defaults to `0.25`.
- `data.drag` (optional): Drag factor slowing the balloon's movement when the attached entity stopped moving. Defaults to `0.2`.
- `data.bob_frequency` (optional): Frequency of the bobbing motion. Defaults to `0.2`.
- `data.bob_amplitude` (optional): Amplitude (height) of the bobbing motion. Defaults to `0.2`.
- `data.offset` (optional): Position offset relative to the attached entity.
- `data.segments` (optional): List of additional segments for the balloon

Segment format:
- `model`: Model filename, same as the model for the main balloon segment
- `animation`: Name of the animation to play
- `distance`: Distance to the previous segment

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
The balloon will respawn immediately after the player respawns.

To add balloons to the players available selection, use `/balloon give <player> <id or *>`
To remove balloons from the players available selection, use `/balloon remove <player> <id or *>`

`/balloon` will display the selection UI

`/balloon <id>` run by a player will set the balloon with the given id as active

`/balloon reload` to reload the config and db connection

---

# Components

- `balloons:token`: For voucher items that can be redeemed by using the item (the item won't be consumed if the player already has the balloon available to them)

- `balloons:balloon`: For items that should display the balloon if worn as equipment or trinket. Has the same fields the "data" object in the config has, for example "model", "animation", "show_leash", etc.

---

# filament support

[Filament](https://modrinth.com/mod/filament) based items with the `balloons:balloon` will be automatically added as option to the `/balloon show <id>` command, using the items' id.
The mod will also try to load blockbench models from filament datapacks if its installed and has the model loaded.
Make sure to specify a namespace in this case!

# Notes

- This is a backend mod only — it doesn't add items, recipes, or models directly.
- You'll need to provide your own models.
- Useful for customized servers, cosmetics, or just for fun.
