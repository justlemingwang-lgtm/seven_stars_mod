"""Generate and validate the nine editable Seven Stars sky-arena templates.

The default mode creates only missing files. Use --force to intentionally replace
authored templates, or --validate-only to perform a read-only validation pass.
"""
from __future__ import annotations

import argparse
import gzip
import math
import struct
from dataclasses import dataclass
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
OUTPUT = ROOT / "src/main/resources/data/sevenstars/structures/sky_arena"
DATA_VERSION = 3465
RADIUS = 56
FLOOR_Y = 8
HEIGHT = 21
WALL_HEIGHT = 8


@dataclass(frozen=True)
class Tile:
    name: str
    min_x: int
    max_x: int
    min_z: int
    max_z: int

    @property
    def size(self) -> tuple[int, int, int]:
        return self.max_x - self.min_x + 1, HEIGHT, self.max_z - self.min_z + 1


TILES = (
    Tile("arena_north_west", -56, -19, -56, -19),
    Tile("arena_north", -18, 18, -56, -19),
    Tile("arena_north_east", 19, 56, -56, -19),
    Tile("arena_west", -56, -19, -18, 18),
    Tile("arena_center", -18, 18, -18, 18),
    Tile("arena_east", 19, 56, -18, 18),
    Tile("arena_south_west", -56, -19, 19, 56),
    Tile("arena_south", -18, 18, 19, 56),
    Tile("arena_south_east", 19, 56, 19, 56),
)

SEAL_POSITIONS = tuple(
    (round(math.sin(index * math.tau / 7.0) * 12), round(-math.cos(index * math.tau / 7.0) * 12))
    for index in range(7)
)
RUNE_POSITIONS = tuple(
    (round(math.sin(index * math.tau / 7.0) * 28), round(-math.cos(index * math.tau / 7.0) * 28))
    for index in range(7)
)


def tag_int(value):
    return 3, value


def tag_string(value):
    return 8, value


def tag_list(child, values):
    return 9, (child, values)


def tag_compound(value):
    return 10, value


def write_utf(value: str) -> bytes:
    raw = value.encode("utf-8")
    return struct.pack(">H", len(raw)) + raw


def write_payload(tag: int, value) -> bytes:
    if tag == 1:
        return struct.pack(">b", value)
    if tag == 2:
        return struct.pack(">h", value)
    if tag == 3:
        return struct.pack(">i", value)
    if tag == 4:
        return struct.pack(">q", value)
    if tag == 5:
        return struct.pack(">f", value)
    if tag == 6:
        return struct.pack(">d", value)
    if tag == 7:
        return struct.pack(">i", len(value)) + bytes(value)
    if tag == 8:
        return write_utf(value)
    if tag == 9:
        child, values = value
        return bytes((child,)) + struct.pack(">i", len(values)) + b"".join(
            write_payload(child, item) for item in values
        )
    if tag == 10:
        output = bytearray()
        for name, (child, child_value) in value.items():
            output += bytes((child,)) + write_utf(name) + write_payload(child, child_value)
        return bytes(output) + b"\0"
    if tag == 11:
        return struct.pack(">i", len(value)) + b"".join(struct.pack(">i", item) for item in value)
    if tag == 12:
        return struct.pack(">i", len(value)) + b"".join(struct.pack(">q", item) for item in value)
    raise ValueError(f"unsupported NBT tag {tag}")


class Reader:
    def __init__(self, data: bytes):
        self.data = data
        self.offset = 0

    def take(self, count: int) -> bytes:
        end = self.offset + count
        if end > len(self.data):
            raise ValueError("truncated NBT payload")
        value = self.data[self.offset:end]
        self.offset = end
        return value

    def unpack(self, fmt: str):
        size = struct.calcsize(fmt)
        return struct.unpack(fmt, self.take(size))[0]

    def utf(self) -> str:
        return self.take(self.unpack(">H")).decode("utf-8")

    def payload(self, tag: int):
        if tag == 1:
            return self.unpack(">b")
        if tag == 2:
            return self.unpack(">h")
        if tag == 3:
            return self.unpack(">i")
        if tag == 4:
            return self.unpack(">q")
        if tag == 5:
            return self.unpack(">f")
        if tag == 6:
            return self.unpack(">d")
        if tag == 7:
            return self.take(self.unpack(">i"))
        if tag == 8:
            return self.utf()
        if tag == 9:
            child = self.unpack(">B")
            return [self.payload(child) for _ in range(self.unpack(">i"))]
        if tag == 10:
            result = {}
            while True:
                child = self.unpack(">B")
                if child == 0:
                    return result
                name = self.utf()
                result[name] = self.payload(child)
        if tag == 11:
            return [self.unpack(">i") for _ in range(self.unpack(">i"))]
        if tag == 12:
            return [self.unpack(">q") for _ in range(self.unpack(">i"))]
        raise ValueError(f"unsupported NBT tag {tag}")


def read_template(path: Path) -> dict:
    with path.open("rb") as raw:
        if raw.read(2) != b"\x1f\x8b":
            raise ValueError(f"{path.name} is not gzip-compressed")
    with gzip.open(path, "rb") as stream:
        reader = Reader(stream.read())
    if reader.unpack(">B") != 10:
        raise ValueError(f"{path.name} root is not a compound")
    reader.utf()
    root = reader.payload(10)
    if reader.offset != len(reader.data):
        raise ValueError(f"{path.name} has trailing NBT bytes")
    return root


def state(name: str, **properties: str) -> tuple[str, tuple[tuple[str, str], ...]]:
    return name, tuple(sorted(properties.items()))


POLISHED = state("minecraft:polished_deepslate")
REINFORCED = state("minecraft:reinforced_deepslate")
CURSED = state("sevenstars:star_cursed_bricks")
CRACKED = state("sevenstars:cracked_star_cursed_bricks")
RUNE = state("sevenstars:star_rune_tiles")
CHAIN_CORE = state("sevenstars:azure_seal_chain", broken="false")
CONTAINER = state("sevenstars:azure_soul_container", active="false", summoned="false")
SUMMON_RUNE = state("sevenstars:azure_butcher_spawn_rune")
CHAIN_Y = state("minecraft:chain", axis="y", waterlogged="false")


def radial_line(x: int, z: int, width: float, min_radius: float, max_radius: float) -> bool:
    for index in range(7):
        angle = index * math.tau / 7.0
        ux, uz = math.sin(angle), -math.cos(angle)
        along = x * ux + z * uz
        across = abs(x * uz - z * ux)
        if min_radius <= along <= max_radius and across <= width:
            return True
    return False


def near_ring(radius: float, target: float, width: float) -> bool:
    return abs(radius - target) <= width


def near_seal(x: int, z: int, radius: float) -> bool:
    return any((x - seal_x) ** 2 + (z - seal_z) ** 2 <= radius * radius
               for seal_x, seal_z in SEAL_POSITIONS)


def angular_node(x: int, z: int, count: int, tolerance: float) -> bool:
    angle = (math.atan2(x, -z) + math.tau) % math.tau
    segment = math.tau / count
    distance = abs((angle + segment / 2.0) % segment - segment / 2.0)
    return distance <= tolerance


def build_global_blocks() -> dict[tuple[int, int, int], tuple[str, tuple[tuple[str, str], ...]]]:
    blocks = {}

    def put(x: int, y: int, z: int, block_state):
        if x * x + z * z <= RADIUS * RADIUS:
            blocks[(x, y, z)] = block_state

    for x in range(-RADIUS, RADIUS + 1):
        for z in range(-RADIUS, RADIUS + 1):
            radius = math.hypot(x, z)
            if radius > RADIUS:
                continue

            # Full structural deck plus grouped ring/ray beams visible from below.
            put(x, FLOOR_Y - 1, z, REINFORCED if radius >= 51.5 else POLISHED)
            if (radius <= 10.0 or radial_line(x, z, 1.6, 7.0, 51.0)
                    or any(near_ring(radius, ring, 1.1) for ring in (18.0, 34.0, 50.0))):
                put(x, FLOOR_Y - 2, z, CURSED if radial_line(x, z, 1.6, 7.0, 51.0) else REINFORCED)
            if (radius <= 8.0 or radial_line(x, z, 0.9, 8.0, 45.0)
                    or any(near_ring(radius, ring, 0.7) for ring in (18.0, 34.0, 50.0))):
                put(x, FLOOR_Y - 3, z, REINFORCED)
            if radius <= 18.0 or radial_line(x, z, 0.65, 10.0, 39.0):
                put(x, FLOOR_Y - 4, z, CURSED if radius <= 12.0 else POLISHED)

            # Central suspended inverse star-cone; it ends in mid-air, never at terrain height.
            for y, cone_radius, material in (
                    (0, 5.0, REINFORCED), (1, 9.0, CURSED),
                    (2, 14.0, POLISHED), (3, 19.0, POLISHED)):
                if radius <= cone_radius:
                    put(x, y, z, material)

            # Flat combat floor with coherent rings, seven rays and seal-pressure cracking.
            floor = POLISHED
            if radius >= 51.5:
                floor = REINFORCED
            elif any(near_ring(radius, ring, 1.15) for ring in (16.0, 33.0, 48.0)):
                floor = CURSED
            if near_seal(x, z, 2.8):
                floor = CRACKED
            if radial_line(x, z, 0.85, 5.0, 50.0):
                floor = RUNE
            if radius <= 4.5:
                floor = REINFORCED if radius <= 2.0 else CURSED
            put(x, FLOOR_Y, z, floor)

            # Thick wall ring, crenellations, 28 buttresses and seven higher seal towers.
            if 52.0 <= radius <= RADIUS:
                wall_material = REINFORCED if radius >= 54.2 else CURSED
                for y in range(FLOOR_Y + 1, FLOOR_Y + WALL_HEIGHT):
                    put(x, y, z, wall_material)
                if angular_node(x, z, 28, 0.035):
                    put(x, FLOOR_Y + WALL_HEIGHT, z, REINFORCED)
                if angular_node(x, z, 7, 0.055):
                    for y in range(FLOOR_Y + 1, HEIGHT):
                        put(x, y, z, REINFORCED if y >= FLOOR_Y + WALL_HEIGHT else wall_material)

    blocks[(0, FLOOR_Y + 1, 0)] = CONTAINER
    for x, z in SEAL_POSITIONS:
        blocks[(x, FLOOR_Y + 1, z)] = CHAIN_CORE
        blocks[(x, FLOOR_Y + 2, z)] = CHAIN_Y
        blocks[(x, FLOOR_Y + 3, z)] = CHAIN_Y
        blocks[(x, FLOOR_Y + 4, z)] = CHAIN_Y
    for x, z in RUNE_POSITIONS:
        blocks[(x, FLOOR_Y, z)] = SUMMON_RUNE
    return blocks


def template_root(tile: Tile, global_blocks: dict) -> dict:
    local_blocks = []
    states = sorted({block_state for (x, _y, z), block_state in global_blocks.items()
                     if tile.min_x <= x <= tile.max_x and tile.min_z <= z <= tile.max_z})
    palette_index = {block_state: index for index, block_state in enumerate(states)}
    palette = []
    for name, properties in states:
        entry = {"Name": tag_string(name)}
        if properties:
            entry["Properties"] = tag_compound({key: tag_string(value) for key, value in properties})
        palette.append(entry)

    for (x, y, z), block_state in sorted(global_blocks.items(), key=lambda item: (item[0][1], item[0][2], item[0][0])):
        if tile.min_x <= x <= tile.max_x and tile.min_z <= z <= tile.max_z:
            local_blocks.append({
                "pos": tag_list(3, [x - tile.min_x, y, z - tile.min_z]),
                "state": tag_int(palette_index[block_state]),
            })

    return {
        "DataVersion": tag_int(DATA_VERSION),
        "size": tag_list(3, list(tile.size)),
        "palette": tag_list(10, palette),
        "blocks": tag_list(10, local_blocks),
        "entities": tag_list(10, []),
    }


def write_template(path: Path, root: dict):
    payload = bytes((10,)) + write_utf("") + write_payload(10, root)
    with path.open("wb") as raw:
        with gzip.GzipFile(filename="", mode="wb", compresslevel=9, fileobj=raw, mtime=0) as stream:
            stream.write(payload)


def decoded_blocks(root: dict) -> dict[tuple[int, int, int], str]:
    palette = root["palette"]
    result = {}
    for entry in root["blocks"]:
        result[tuple(entry["pos"])] = palette[entry["state"]]["Name"]
    return result


def validate_files() -> list[tuple[str, tuple[int, int, int], int, int, int]]:
    expected_names = {tile.name for tile in TILES}
    actual_names = {path.stem for path in OUTPUT.glob("*.nbt")}
    missing = expected_names - actual_names
    if missing:
        raise ValueError(f"missing templates: {sorted(missing)}")

    stitched = {}
    report = []
    container_locations = []
    seal_locations = []
    for tile in TILES:
        root = read_template(OUTPUT / f"{tile.name}.nbt")
        if tuple(root.get("size", ())) != tile.size:
            raise ValueError(f"{tile.name} size {root.get('size')} != {tile.size}")
        local = decoded_blocks(root)
        containers = sum(name == "sevenstars:azure_soul_container" for name in local.values())
        seals = sum(name == "sevenstars:azure_seal_chain" for name in local.values())
        report.append((tile.name, tile.size, len(local), containers, seals))
        for (local_x, y, local_z), name in local.items():
            global_pos = tile.min_x + local_x, y, tile.min_z + local_z
            if global_pos in stitched:
                raise ValueError(f"overlapping template block at {global_pos}")
            stitched[global_pos] = name
            if name == "sevenstars:azure_soul_container":
                container_locations.append((tile.name, global_pos))
            if name == "sevenstars:azure_seal_chain":
                seal_locations.append((tile.name, global_pos))

    if len(container_locations) != 1:
        raise ValueError(f"expected exactly one soul container, found {len(container_locations)}")
    if len(seal_locations) != 7:
        raise ValueError(f"expected exactly seven seal cores, found {len(seal_locations)}")
    if container_locations[0][0] != "arena_center" or any(tile != "arena_center" for tile, _ in seal_locations):
        raise ValueError("container and all seal cores must remain inside arena_center")

    floor_positions = {(x, z) for (x, y, z) in stitched if y == FLOOR_Y}
    expected_floor = {(x, z) for x in range(-RADIUS, RADIUS + 1)
                      for z in range(-RADIUS, RADIUS + 1) if x * x + z * z <= RADIUS * RADIUS}
    if floor_positions != expected_floor:
        missing_floor = expected_floor - floor_positions
        outside_floor = floor_positions - expected_floor
        raise ValueError(f"floor discontinuity: missing={len(missing_floor)}, outside={len(outside_floor)}")
    if not all((edge, 0) in floor_positions for edge in (-RADIUS, RADIUS)):
        raise ValueError("arena does not reach exact radius 56")
    if not all((0, edge) in floor_positions for edge in (-RADIUS, RADIUS)):
        raise ValueError("arena does not reach exact radius 56")
    if any(x * x + z * z > RADIUS * RADIUS for x, _y, z in stitched):
        raise ValueError("solid template block lies outside radius 56")

    # Wall and ring checks deliberately cross all four internal tile seams.
    for x, z in expected_floor:
        radius = math.hypot(x, z)
        if 52.0 <= radius <= RADIUS and (x, FLOOR_Y + 1, z) not in stitched:
            raise ValueError(f"outer wall seam/gap at {(x, z)}")
    for seam in (-19, -18, 18, 19):
        for other in range(-RADIUS, RADIUS + 1):
            for x, z in ((seam, other), (other, seam)):
                if x * x + z * z <= RADIUS * RADIUS and (x, FLOOR_Y, z) not in stitched:
                    raise ValueError(f"floor seam gap at {(x, z)}")
    return report


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--force", action="store_true", help="overwrite all nine checked-in templates")
    parser.add_argument("--validate-only", action="store_true", help="validate existing NBT without writing")
    args = parser.parse_args()
    if args.force and args.validate_only:
        parser.error("--force and --validate-only are mutually exclusive")

    OUTPUT.mkdir(parents=True, exist_ok=True)
    if not args.validate_only:
        global_blocks = build_global_blocks()
        for tile in TILES:
            path = OUTPUT / f"{tile.name}.nbt"
            if path.exists() and not args.force:
                print(f"Protected existing template: {path.name}")
                continue
            write_template(path, template_root(tile, global_blocks))
            print(f"Wrote {path.name}")

    report = validate_files()
    print("Sky arena validation passed")
    for name, size, block_count, containers, seals in report:
        print(f"  {name}: size={size[0]}x{size[1]}x{size[2]}, blocks={block_count}, "
              f"containers={containers}, seals={seals}")
    print(f"  totals: blocks={sum(row[2] for row in report)}, containers={sum(row[3] for row in report)}, "
          f"seals={sum(row[4] for row in report)}")


if __name__ == "__main__":
    main()
