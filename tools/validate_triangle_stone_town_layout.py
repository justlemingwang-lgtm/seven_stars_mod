"""Monte Carlo validation for the connector-driven triangle stone town layout."""
from __future__ import annotations

import random

from generate_triangle_stone_town import generate_templates, pool_mapping


ROTATIONS = range(4)
STEPS = {"north": (0, -1), "south": (0, 1), "west": (-1, 0), "east": (1, 0)}
EXTENSION_WEIGHTS = {
    "extension_long_corridor": 40, "extension_corridor_straight": 30,
    "extension_corridor_turn": 20, "extension_crossroads": 12,
    "extension_multilevel_hall": 10, "extension_library": 8,
    "extension_residential": 8, "extension_workshop": 7, "extension_guard": 7,
    "extension_brewing": 6, "extension_prison": 5, "extension_research": 5,
    "extension_observation": 4, "extension_core_room": 3,
    "extension_vertical_descent": 8, "extension_vertical_ascent": 6,
    "extension_lamp_shrine": 2,
}


def rotate_position(position, rotation):
    x, y, z = position
    return ((x, y, z), (-z, y, x), (-x, y, -z), (z, y, -x))[rotation]


def rotate_direction(direction, rotation):
    directions = ("north", "east", "south", "west")
    return directions[(directions.index(direction) + rotation) % 4]


def connectors(templates, name, origin, rotation):
    result = []
    for position, (block, properties, nbt) in templates[name].blocks.items():
        if block != "minecraft:jigsaw":
            continue
        transformed = rotate_position(position, rotation)
        world_position = (transformed[0] + origin[0], transformed[1] + origin[1],
                          transformed[2] + origin[2])
        facing = rotate_direction(properties["orientation"].removesuffix("_up"), rotation)
        result.append((world_position, facing, nbt))
    return result


def bounds(templates, name, origin, rotation):
    size_x, size_y, size_z = templates[name].size
    first = rotate_position((0, 0, 0), rotation)
    last = rotate_position((size_x - 1, size_y - 1, size_z - 1), rotation)
    return (min(first[0], last[0]) + origin[0], origin[1],
            min(first[2], last[2]) + origin[2], max(first[0], last[0]) + origin[0],
            origin[1] + size_y - 1, max(first[2], last[2]) + origin[2])


def origin_for_minimum(templates, name, minimum, rotation):
    zero_bounds = bounds(templates, name, (0, 0, 0), rotation)
    return (minimum[0] - zero_bounds[0], minimum[1], minimum[2] - zero_bounds[2])


def intersects(first, second):
    return (first[0] <= second[3] and first[3] >= second[0]
            and first[1] <= second[4] and first[4] >= second[1]
            and first[2] <= second[5] and first[5] >= second[2])


def relative(position, direction):
    step_x, step_z = STEPS[direction]
    return position[0] + step_x, position[1], position[2] + step_z


def choose_candidate(candidates, rng):
    weights = [EXTENSION_WEIGHTS.get(name, 1) for name in candidates]
    return rng.choices(candidates, weights=weights, k=1)[0]


def generate_attempt(templates, pools, rng):
    start_rotation = 0
    start_origin = origin_for_minimum(templates, "entrance", (-15, 99, -15), start_rotation)
    town_origin = origin_for_minimum(templates, "town_square", (-15, 9, -61), 0)
    rooms = [
        ("entrance", start_origin, start_rotation,
         bounds(templates, "entrance", start_origin, start_rotation)),
        ("entrance_spiral", (0, 0, 0), 0, (-7, 9, -30, 7, 103, -16)),
        ("town_square", town_origin, 0,
         bounds(templates, "town_square", town_origin, 0)),
    ]
    pending = []
    for connector in connectors(templates, "town_square", town_origin, 0):
        if connector[2]["pool"][1] != "minecraft:empty":
            pending.append((connector, 2))

    while pending and len(rooms) < 72:
        connector, depth = pending.pop(rng.randrange(len(pending)))
        if depth >= 50:
            continue
        candidates = pools[connector[2]["pool"][1].rsplit("/", 1)[-1]]
        placed = None
        for _attempt in range(5):
            name = choose_candidate(candidates, rng)
            rotations = list(ROTATIONS)
            rng.shuffle(rotations)
            for rotation in rotations:
                entries = connectors(templates, name, (0, 0, 0), rotation)
                rng.shuffle(entries)
                for entry_position, entry_facing, entry_nbt in entries:
                    if (entry_nbt["name"][1] != connector[2]["target"][1]
                            or entry_facing != rotate_direction(connector[1], 2)):
                        continue
                    attachment = relative(connector[0], connector[1])
                    origin = (attachment[0] - entry_position[0], attachment[1] - entry_position[1],
                              attachment[2] - entry_position[2])
                    candidate_bounds = bounds(templates, name, origin, rotation)
                    if (candidate_bounds[1] < -59 or candidate_bounds[4] >= 20
                            or abs(attachment[0]) > 112 or abs(attachment[2] + 46) > 112
                            or candidate_bounds[0] < -128 or candidate_bounds[3] > 128
                            or candidate_bounds[2] < -174 or candidate_bounds[5] > 82
                            or any(intersects(candidate_bounds, room[3]) for room in rooms)):
                        continue
                    placed = name, origin, rotation, candidate_bounds
                    break
                if placed:
                    break
            if placed:
                break
        if not placed:
            continue
        rooms.append(placed)
        for child_connector in connectors(templates, placed[0], placed[1], placed[2]):
            child_nbt = child_connector[2]
            if (child_nbt["pool"][1] != "minecraft:empty"
                    and child_nbt["target"][1] != "minecraft:empty"):
                pending.append((child_connector, depth + 1))

    return rooms


def main():
    templates = generate_templates()
    pools = pool_mapping()
    successful_towns = 0
    minimum_rooms = 72
    maximum_rooms = 0
    for seed in range(100):
        rng = random.Random(seed)
        best = []
        for _attempt in range(16):
            layout = generate_attempt(templates, pools, rng)
            if len(layout) > len(best):
                best = layout
            names = {room[0] for room in layout}
            if len(layout) >= 24 and "core_vault" in names:
                successful_towns += 1
                best = layout
                break
        minimum_rooms = min(minimum_rooms, len(best))
        maximum_rooms = max(maximum_rooms, len(best))
        assert "core_vault" in {room[0] for room in best}, f"seed {seed} never generated the core vault"
        assert len(best) >= 24, f"seed {seed} generated only {len(best)} connected rooms"
    print(f"Validated {successful_towns}/100 towns; connected room range {minimum_rooms}..{maximum_rooms}")


if __name__ == "__main__":
    main()
