"""Generate and validate the checked-in triangle stone town structure templates.

The output is vanilla gzip-compressed structure NBT. Keeping the generator in
the repository makes the large binary templates reviewable and repeatable.
"""
from __future__ import annotations

import gzip
import json
import struct
import copy
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
STRUCTURES = ROOT / "src/main/resources/data/sevenstars/structures/triangle_stone_town"
POOLS = ROOT / "src/main/resources/data/sevenstars/worldgen/template_pool/triangle_stone_town"


def s(value): return (8, value)
def b(value): return (1, value)
def i(value): return (3, value)
def d(value): return (6, value)
def compound(value): return (10, value)
def list_of(tag, values): return (9, (tag, values))


def utf(value):
    raw = value.encode("utf-8")
    return struct.pack(">H", len(raw)) + raw


def payload(tag, value):
    if tag == 1:
        return struct.pack(">b", value)
    if tag == 3:
        return struct.pack(">i", value)
    if tag == 6:
        return struct.pack(">d", value)
    if tag == 8:
        return utf(value)
    if tag == 9:
        child, values = value
        return bytes([child]) + struct.pack(">i", len(values)) + b"".join(payload(child, v) for v in values)
    if tag == 10:
        out = bytearray()
        for name, (child, child_value) in value.items():
            out += bytes([child]) + utf(name) + payload(child, child_value)
        return bytes(out) + b"\0"
    raise ValueError(tag)


class Template:
    def __init__(self, name, x, z, y):
        self.name, self.size = name, (x, y, z)
        self.blocks = {}
        self.entities = []

    def put(self, x, y, z, name, properties=None, nbt=None):
        if 0 <= x < self.size[0] and 0 <= y < self.size[1] and 0 <= z < self.size[2]:
            self.blocks[(x, y, z)] = (name, properties or {}, nbt)

    def remove(self, x, y, z):
        self.blocks.pop((x, y, z), None)

    def box(self, ceiling=True, clear_interior=True):
        xz, yy, zz = self.size
        # Structure templates do not clear unspecified positions. Underground
        # rooms therefore need explicit air or the terrain remains solid stone.
        if clear_interior:
            for x in range(xz):
                for y in range(1, yy - 1):
                    for z in range(zz):
                        self.put(x, y, z, "minecraft:air")
        for x in range(xz):
            for z in range(zz):
                self.put(x, 0, z, "sevenstars:triangle_stone_bricks")
                if ceiling:
                    self.put(x, yy - 1, z, "minecraft:deepslate_tiles")
        for y in range(1, yy - 1):
            for x in range(xz):
                self.put(x, y, 0, "sevenstars:triangle_stone_bricks")
                self.put(x, y, zz - 1, "sevenstars:triangle_stone_bricks")
            for z in range(1, zz - 1):
                self.put(0, y, z, "sevenstars:triangle_stone_bricks")
                self.put(xz - 1, y, z, "sevenstars:triangle_stone_bricks")

    def jigsaw(self, x, y, z, facing, name, target="minecraft:empty", pool="minecraft:empty"):
        # These connectors are retained as template metadata, but this structure is
        # assembled by TriangleStoneTownStructure rather than vanilla jigsaw
        # placement.  Keep the authored wall closed: the generated passage that is
        # actually selected for the room cuts its own doorway during post-process.
        # Pre-carving here creates convincing-looking doors with no corridor behind
        # them whenever a room is randomly positioned or rotated.
        self.put(x, y, z, "minecraft:jigsaw", {"orientation": facing + "_up"}, {
            "id": s("minecraft:jigsaw"), "name": s(name), "target": s(target),
            "pool": s(pool), "final_state": s("sevenstars:triangle_stone_bricks"),
            "joint": s("aligned")})

    def chest(self, x, y, z, table):
        self.put(x, y, z, "minecraft:chest", {"facing": "south", "type": "single", "waterlogged": "false"},
                 {"id": s("minecraft:chest"), "LootTable": s("sevenstars:chests/triangle_stone_town/" + table)})

    def spawner(self, x, y, z, entity="sevenstars:tormented_wraith"):
        ent = {"id": s(entity)}
        self.put(x, y, z, "minecraft:spawner", nbt={"id": s("minecraft:mob_spawner"),
            "Delay": i(40), "SpawnData": compound({"entity": compound(ent)})})

    def item_frame(self, x, y, z, item, facing="south"):
        facing_ids = {"down": 0, "up": 1, "north": 2, "south": 3, "west": 4, "east": 5}
        horizontal_steps = {"north": (0, -1), "south": (0, 1), "west": (-1, 0), "east": (1, 0)}
        dx, dz = horizontal_steps[facing]
        self.entities.append({
            "pos": list_of(6, [x + 0.5 - dx * 0.46875, y + 0.5, z + 0.5 - dz * 0.46875]),
            "blockPos": list_of(3, [x, y, z]),
            "nbt": compound({
                "id": s("minecraft:item_frame"),
                "Facing": b(facing_ids[facing]),
                "Fixed": b(1),
                "Invulnerable": b(1),
                "TileX": i(x),
                "TileY": i(y),
                "TileZ": i(z),
                "ItemRotation": b(0),
                "Item": compound({"id": s(item), "Count": b(1)})
            })
        })

    def save(self):
        palette, indices = [], {}
        def state_index(name, props):
            key = (name, tuple(sorted(props.items())))
            if key not in indices:
                indices[key] = len(palette)
                entry = {"Name": s(name)}
                if props:
                    entry["Properties"] = compound({k: s(v) for k, v in props.items()})
                palette.append(entry)
            return indices[key]
        blocks = []
        for pos, (name, props, nbt) in sorted(self.blocks.items(), key=lambda e: (e[0][1], e[0][2], e[0][0])):
            row = {"pos": list_of(3, list(pos)), "state": i(state_index(name, props))}
            if nbt:
                row["nbt"] = compound(nbt)
            blocks.append(row)
        root = {"DataVersion": i(3465), "size": list_of(3, list(self.size)),
                "palette": list_of(10, palette), "blocks": list_of(10, blocks),
                "entities": list_of(10, self.entities)}
        raw = bytes([10]) + utf("") + payload(10, root)
        STRUCTURES.mkdir(parents=True, exist_ok=True)
        with (STRUCTURES / f"{self.name}.nbt").open("wb") as file_out:
            with gzip.GzipFile(fileobj=file_out, mode="wb", mtime=0) as out:
                out.write(raw)


DIMS = {
    "entrance": (31,31,12), "entrance_descent": (9,19,15), "tutorial_lamp_room": (15,15,8),
    "town_square": (31,31,10), "corridor_01_straight": (7,17,7), "living_hub": (47,31,9),
    "library": (19,17,8), "brewing_room": (17,15,8), "residential_ruin": (17,15,8),
    "maintenance_workshop": (17,15,8), "corridor_02_turn": (13,13,7), "control_hub": (47,31,9),
    "prison": (21,17,9), "guard_station": (15,13,8), "research_room": (21,19,9),
    "stairs_down": (9,19,19), "stairs_spiral_down": (13,13,19), "lower_hub": (47,47,11),
    "treasure_room_01": (21,21,9), "treasure_room_02": (21,21,9), "treasure_room_03": (21,21,9),
    "treasure_room_04": (21,21,9), "observation_gallery": (15,11,8), "sealed_array": (23,23,11),
    "core_vault": (17,17,9), "corridor_chest": (9,17,7), "lamp_shrine_dead_end": (11,11,8),
}

EXTENSION_VARIANTS = {
    "extension_corridor_straight": ("corridor_01_straight", True),
    "extension_corridor_turn": ("corridor_02_turn", True),
    "extension_library": ("library", True),
    "extension_brewing": ("brewing_room", True),
    "extension_residential": ("residential_ruin", True),
    "extension_workshop": ("maintenance_workshop", True),
    "extension_prison": ("prison", True),
    "extension_guard": ("guard_station", True),
    "extension_research": ("research_room", True),
    "extension_observation": ("observation_gallery", True),
    "extension_core_room": ("core_vault", True),
    "extension_lamp_shrine": ("lamp_shrine_dead_end", False),
}

MACRO_EXTENSIONS = (
    "extension_long_corridor",
    "extension_crossroads",
    "extension_vertical_descent",
    "extension_vertical_ascent",
    "extension_multilevel_hall",
)

VERTICAL_ROOMS = {
    "ossuary": (21,19,10),
    "ritual_classroom": (21,21,11),
    "collapsed_cistern": (31,31,15),
    "wraith_barracks": (23,21,11),
    "archive_annex": (23,19,12),
    "forge_chamber": (25,21,13),
    "meditation_cells": (25,23,12),
    "map_room": (27,23,13),
}


def pool_mapping():
    mapping={"start_pool":["entrance"],"entrance_descent_pool":["entrance_descent"],"tutorial_pool":["tutorial_lamp_room"],"town_square_pool":["town_square"],"corridor_01_pool":["corridor_01_straight"],"living_hub_pool":["living_hub"],"library_pool":["library"],"brewing_pool":["brewing_room"],"residential_pool":["residential_ruin"],"workshop_pool":["maintenance_workshop"],"corridor_02_pool":["corridor_02_turn"],"control_hub_pool":["control_hub"],"prison_pool":["prison"],"guard_pool":["guard_station"],"research_pool":["research_room"],"descent_pool":["stairs_down","stairs_spiral_down"],"lower_hub_pool":["lower_hub"],"treasure_pool":[f"treasure_room_0{n}" for n in range(1,5)],"observation_pool":["observation_gallery"],"sealed_pool":["sealed_array"],"core_vault_pool":["core_vault"],"corridor_chest_pool":["corridor_chest"],"lamp_shrine_pool":["lamp_shrine_dead_end"],"extension_pool":list(MACRO_EXTENSIONS)+list(EXTENSION_VARIANTS),"vertical_access_pool":["vertical_access_corridor"],"vertical_district_pool":["vertical_district"]}
    for room in VERTICAL_ROOMS:
        mapping[f"{room}_pool"]=[room]
    return mapping


def entry(t, side="south", y=1, name=None):
    x, _, z = t.size
    positions = {"south": (x//2,y,z-1), "north": (x//2,y,0), "west": (0,y,z//2), "east": (x-1,y,z//2)}
    px, py, pz = positions[side]
    t.jigsaw(px, py, pz, side, name or f"sevenstars:town/{t.name}_entry")


def exit_to(t, side, target, pool, y=1, offset=0):
    x, _, z = t.size
    if side in ("north", "south"):
        pos = (x//2 + offset, y, 0 if side == "north" else z-1)
    else:
        pos = (0 if side == "west" else x-1, y, z//2 + offset)
    t.jigsaw(*pos, side, f"sevenstars:town/{t.name}_{side}_{abs(offset)}", target, f"sevenstars:triangle_stone_town/{pool}")


def column(t, x, z, y1, y2, block="sevenstars:triangle_stone_bricks"):
    for yy in range(y1, y2 + 1):
        t.put(x, yy, z, block)


def floor_rect(t, x1, z1, x2, z2, block, y=0):
    for px in range(x1, x2 + 1):
        for pz in range(z1, z2 + 1):
            t.put(px, y, pz, block)


def framed_arch(t, side, offset=0, block="minecraft:polished_deepslate"):
    x, y, z = t.size
    cx, cz = (x//2 + offset, 0 if side == "north" else z-1) if side in ("north", "south") else (0 if side == "west" else x-1, z//2 + offset)
    for d in (-3, 3):
        for yy in range(1, min(6, y-1)):
            px, pz = (cx+d, cz) if side in ("north", "south") else (cx, cz+d)
            t.put(px, yy, pz, block)
    for d in range(-3, 4):
        px, pz = (cx+d, cz) if side in ("north", "south") else (cx, cz+d)
        t.put(px, min(5, y-2), pz, block)


def slab_surface(t, x, z, surface_half, headroom=4):
    """Place a walkable surface measured in half-blocks and solidly support it."""
    block_y = max(0, (surface_half - 1) // 2)
    slab_type = "top" if surface_half % 2 == 0 else "bottom"
    for yy in range(1, block_y):
        t.put(x, yy, z, "sevenstars:triangle_stone_bricks")
    t.put(x, block_y, z, "minecraft:deepslate_tile_slab",
          {"type": slab_type, "waterlogged": "false"})
    for yy in range(block_y + 1, min(block_y + headroom + 1, t.size[1] - 1)):
        t.put(x, yy, z, "minecraft:air")


def linear_slope_surface(t, x, z, surface_half, next_half, facing):
    """Use a stair only for a full-block rise; shallow/flat runs use half slabs."""
    if next_half - surface_half >= 2:
        block_y = max(0, surface_half // 2)
        for yy in range(1, block_y):
            t.put(x, yy, z, "sevenstars:triangle_stone_bricks")
        t.put(x, block_y, z, "minecraft:deepslate_tile_stairs",
              {"facing": facing, "half": "bottom", "shape": "straight", "waterlogged": "false"})
        for yy in range(block_y + 1, min(block_y + 5, t.size[1] - 1)):
            t.put(x, yy, z, "minecraft:air")
    else:
        slab_surface(t, x, z, surface_half)


def spiral_surface(t, x, z, standing_y, previous_y, ascent_facing):
    """Build a conventional spiral tread: oriented stair at each drop, top slab between drops."""
    if previous_y > standing_y:
        for yy in range(1, standing_y):
            t.put(x, yy, z, "sevenstars:triangle_stone_bricks")
        t.put(x, standing_y, z, "minecraft:deepslate_tile_stairs",
              {"facing": ascent_facing, "half": "bottom", "shape": "straight", "waterlogged": "false"})
        for yy in range(standing_y + 1, min(standing_y + 4, t.size[1] - 1)):
            t.put(x, yy, z, "minecraft:air")
    else:
        slab_surface(t, x, z, standing_y * 2, headroom=3)


def build_straight_descent(t, high_y, low_y):
    """Five-wide stair descending from the south high door to north low door."""
    x, _, z = t.size
    cx = x // 2
    run = list(range(1, z-1))
    slope_start, slope_end = 2, z - 3
    # Lay down both landings first. The slope then owns the transition cells,
    # so a landing can never overwrite the first/last stair tread.
    floor_rect(t,cx-2,1,cx+2,2,"minecraft:deepslate_bricks",low_y-1)
    floor_rect(t,cx-2,z-3,cx+2,z-2,"minecraft:deepslate_bricks",high_y-1)
    levels = {
        pz: low_y * 2 + round(
            (min(max(pz, slope_start), slope_end) - slope_start)
            * (high_y-low_y) * 2 / max(1, slope_end-slope_start))
        for pz in run
    }
    for pz in run:
        surface_half = levels[pz]
        next_half = levels[min(pz + 1, z - 2)]
        for px in range(cx-2, cx+3):
            linear_slope_surface(t, px, pz, surface_half, next_half, "south")


def build_spiral_descent(t):
    """A wall-free two-wide 1.5-turn helix with oriented stairs at every drop."""
    high_y=t.size[1]-4
    for px in range(5,8):
        for pz in range(5,8):
            column(t,px,pz,1,t.size[1]-2)
    path=[]
    path += [(px,10) for px in range(6,1,-1)]
    path += [(2,pz) for pz in range(9,1,-1)]
    path += [(px,2) for px in range(3,11)]
    path += [(10,pz) for pz in range(3,11)]
    path += [(px,10) for px in range(9,1,-1)]
    path += [(2,pz) for pz in range(9,1,-1)]
    path += [(px,2) for px in range(3,7)]
    # The south bridge narrows into the outside of the first turn. Keeping its
    # west edge clear prevents it from flattening the first descending tread.
    floor_rect(t,4,12,8,t.size[2]-1,"minecraft:deepslate_bricks",high_y-1)
    floor_rect(t,6,10,8,12,"minecraft:deepslate_bricks",high_y-1)
    floor_rect(t,4,1,8,3,"minecraft:deepslate_bricks",0)
    for idx,(px,pz) in enumerate(path):
        standing_y = high_y - round(idx * (high_y-1) / (len(path)-1))
        previous_y = high_y - round(max(0, idx-1) * (high_y-1) / (len(path)-1))
        previous = path[max(0,idx-1)]
        ascent_dx,ascent_dz=previous[0]-px,previous[1]-pz
        ascent_facing = "east" if ascent_dx>0 else "west" if ascent_dx<0 else "south" if ascent_dz>0 else "north"
        neighbor = path[min(idx + 1, len(path)-1)] if idx < len(path)-1 else path[idx-1]
        dx,dz=neighbor[0]-px,neighbor[1]-pz
        if dx:
            ox,oz=0,(1 if pz>6 else -1)
        else:
            ox,oz=(1 if px>6 else -1),0
        for sx,sz in {(px,pz),(px+ox,pz+oz)}:
            if 0 < sx < 12 and 0 < sz < 12:
                spiral_surface(t,sx,sz,standing_y,previous_y,ascent_facing)


def build_low_east_turn(t):
    """Connect the north low landing to an east-facing exit on standing Y=1."""
    cx=t.size[0]//2; cz=t.size[2]//2; bend=t.size[0]-3
    floor_rect(t,cx-1,1,bend+1,3,"minecraft:deepslate_bricks",0)
    floor_rect(t,bend-1,2,bend+1,cz+1,"minecraft:deepslate_bricks",0)
    floor_rect(t,bend-1,cz-1,t.size[0]-1,cz+1,"minecraft:deepslate_bricks",0)
    for px in range(cx-1,bend+2):
        for pz in range(1,4):
            for yy in range(1,5): t.put(px,yy,pz,"minecraft:air")
    for pz in range(2,cz+2):
        for px in range(bend-1,min(bend+2,t.size[0])):
            for yy in range(1,5): t.put(px,yy,pz,"minecraft:air")


def extension_variant(base, new_name, continues=True):
    """Clone a decorated room and retarget its connectors to the repeatable pool."""
    result=Template(new_name,base.size[0],base.size[2],base.size[1])
    result.blocks=copy.deepcopy(base.blocks)
    entry_side=None
    has_output=False
    for pos,(block,props,nbt) in list(result.blocks.items()):
        if block != "minecraft:jigsaw":
            continue
        pool_id=nbt["pool"][1]
        if pool_id == "minecraft:empty":
            nbt["name"]=s("sevenstars:town/extension_entry")
            entry_side=props["orientation"].removesuffix("_up")
        else:
            nbt["target"]=s("sevenstars:town/extension_entry")
            nbt["pool"]=s("sevenstars:triangle_stone_town/extension_pool")
            has_output=True
    if continues and not has_output:
        assert entry_side is not None
        opposite={"north":"south","south":"north","east":"west","west":"east"}[entry_side]
        exit_to(result,opposite,"sevenstars:town/extension_entry","extension_pool")
    return result


def build_axis_stair(t, x1, x2, zc, low_y, high_y):
    """Five-wide west-to-east stair used inside the multi-level crossing."""
    for px in range(x1,x2+1):
        surface_half=low_y*2+round((px-x1)*(high_y-low_y)*2/max(1,x2-x1))
        next_half=low_y*2+round((min(px+1,x2)-x1)*(high_y-low_y)*2/max(1,x2-x1))
        for pz in range(zc-2,zc+3):
            linear_slope_surface(t,px,pz,surface_half,next_half,"east")


def macro_extension_templates():
    result={}
    t=Template("extension_long_corridor",9,47,9); t.box(); entry(t,"south",name="sevenstars:town/extension_entry")
    exit_to(t,"north","sevenstars:town/extension_entry","extension_pool")
    exit_to(t,"east","sevenstars:town/extension_entry","extension_pool")
    exit_to(t,"west","sevenstars:town/extension_entry","extension_pool")
    for pz in range(2,45): t.put(4,0,pz,"sevenstars:triangle_rune_tiles")
    for pz in range(5,44,8):
        for px in (1,7): column(t,px,pz,1,6,"minecraft:chiseled_deepslate")
        t.put(1,3,pz,"minecraft:soul_lantern"); t.put(7,3,pz,"minecraft:soul_lantern")
    for side in ("north","south","east","west"): framed_arch(t,side)
    result[t.name]=t

    t=Template("extension_crossroads",31,31,12); t.box(); entry(t,"south",name="sevenstars:town/extension_entry")
    for side in ("north","east","west"): exit_to(t,side,"sevenstars:town/extension_entry","extension_pool")
    floor_rect(t,3,12,27,18,"minecraft:deepslate_bricks"); floor_rect(t,12,3,18,27,"minecraft:deepslate_bricks")
    floor_rect(t,10,10,20,20,"minecraft:polished_deepslate")
    for px,pz in ((8,8),(22,8),(8,22),(22,22)): column(t,px,pz,1,8,"sevenstars:triangle_core_pillar")
    for px,pz in ((15,8),(8,15),(22,15),(15,22)): t.put(px,2,pz,"minecraft:soul_lantern")
    column(t,15,15,1,4,"minecraft:chiseled_deepslate"); t.put(15,5,15,"sevenstars:soul_calming_lamp")
    for side in ("north","south","east","west"): framed_arch(t,side)
    result[t.name]=t

    t=Template("extension_vertical_descent",15,31,21); t.box(); entry(t,"south",17,"sevenstars:town/extension_entry")
    exit_to(t,"north","sevenstars:town/extension_entry","extension_pool",1); build_straight_descent(t,17,1)
    for pz in range(4,28,6):
        for px in (2,12): column(t,px,pz,1,18,"minecraft:chiseled_deepslate")
    result[t.name]=t

    t=Template("extension_vertical_ascent",15,31,21); t.box(); entry(t,"north",1,"sevenstars:town/extension_entry")
    exit_to(t,"south","sevenstars:town/extension_entry","extension_pool",17); build_straight_descent(t,17,1)
    for pz in range(4,28,6):
        for px in (2,12): column(t,px,pz,1,18,"minecraft:chiseled_deepslate")
    result[t.name]=t

    t=Template("extension_multilevel_hall",31,31,21); t.box(); entry(t,"south",9,"sevenstars:town/extension_entry")
    exit_to(t,"north","sevenstars:town/extension_entry","extension_pool",9)
    exit_to(t,"west","sevenstars:town/extension_entry","extension_pool",1)
    exit_to(t,"east","sevenstars:town/extension_entry","extension_pool",17)
    floor_rect(t,0,12,8,18,"minecraft:deepslate_bricks",0)
    floor_rect(t,11,0,19,30,"minecraft:deepslate_bricks",8)
    floor_rect(t,22,12,30,18,"minecraft:deepslate_bricks",16)
    build_axis_stair(t,2,15,15,1,9); build_axis_stair(t,15,28,15,9,17)
    for px,pz in ((5,10),(5,20),(15,8),(15,22),(25,10),(25,20)): column(t,px,pz,1,18,"sevenstars:triangle_core_pillar")
    for px,pz,yy in ((5,15,3),(15,15,11),(25,15,19)): t.put(px,yy,pz,"minecraft:soul_lantern")
    result[t.name]=t
    return result


def build_grand_helix(t):
    """Four closed, two-wide flights that land exactly on every district gallery."""
    ring=[]
    ring += [(px,31) for px in range(23,14,-1)]
    ring += [(15,pz) for pz in range(30,14,-1)]
    ring += [(px,15) for px in range(16,32)]
    ring += [(31,pz) for pz in range(16,32)]
    ring += [(px,31) for px in range(30,22,-1)]
    landings=(41,37,25,13,1)
    route=[]
    for flight,(high_y,low_y) in enumerate(zip(landings,landings[1:])):
        for idx,(px,pz) in enumerate(ring):
            if flight > 0 and idx == 0:
                continue
            # Reach the target one cell before the end, leaving a flat tread at
            # the shared gallery coordinate instead of ending on a stair block.
            progress=min(idx,len(ring)-2)
            standing_y=high_y-round(progress*(high_y-low_y)/max(1,len(ring)-2))
            route.append((px,pz,standing_y))
    for idx,(px,pz,standing_y) in enumerate(route):
        previous_x,previous_z,previous_y=route[max(0,idx-1)]
        ascent_dx,ascent_dz=previous_x-px,previous_z-pz
        ascent_facing="east" if ascent_dx>0 else "west" if ascent_dx<0 else "south" if ascent_dz>0 else "north"
        neighbor_x,neighbor_z,_neighbor_y=route[min(idx+1,len(route)-1)] if idx<len(route)-1 else route[idx-1]
        dx,dz=neighbor_x-px,neighbor_z-pz
        if dx:
            ox,oz=0,(1 if pz>23 else -1)
        else:
            ox,oz=(1 if px>23 else -1),0
        for sx,sz in {(px,pz),(px+ox,pz+oz)}:
            spiral_surface(t,sx,sz,standing_y,previous_y,ascent_facing)
    for px in range(20,27):
        for pz in range(20,27): column(t,px,pz,1,44,"sevenstars:triangle_stone_bricks")


def vertical_district_templates():
    result={}
    access=Template("vertical_access_corridor",9,31,9); access.box()
    entry(access,"south",name="sevenstars:town/vertical_access_entry")
    exit_to(access,"north","sevenstars:town/vertical_district_entry","vertical_district_pool")
    for pz in range(2,29): access.put(4,0,pz,"sevenstars:triangle_rune_tiles")
    for pz in range(4,28,6):
        for px in (1,7): column(access,px,pz,1,6,"minecraft:chiseled_deepslate")
        access.put(1,3,pz,"minecraft:soul_lantern"); access.put(7,3,pz,"minecraft:soul_lantern")
    result[access.name]=access

    district=Template("vertical_district",47,47,47); district.box()
    entry(district,"south",41,"sevenstars:town/vertical_district_entry")
    exits=[
        ("north",1,-14,"ossuary"),("north",13,14,"ritual_classroom"),
        ("east",25,-14,"collapsed_cistern"),("east",37,14,"wraith_barracks"),
        ("west",13,-14,"archive_annex"),("west",25,14,"forge_chamber"),
        ("south",1,-14,"meditation_cells"),("south",37,14,"map_room"),
    ]
    for side,yy,offset,room in exits:
        exit_to(district,side,f"sevenstars:town/{room}_entry",f"{room}_pool",yy,offset)
    # Four broad galleries, each with bridges from the helix to the outer wall.
    for floor_y in (0,12,24,36):
        for px in range(5,42):
            for pz in range(5,42):
                if px in range(5,11) or px in range(36,42) or pz in range(5,11) or pz in range(36,42):
                    district.put(px,floor_y,pz,"minecraft:deepslate_bricks")
        floor_rect(district,8,21,38,25,"minecraft:polished_deepslate",floor_y)
        floor_rect(district,21,8,25,38,"minecraft:polished_deepslate",floor_y)
        for px,pz in ((8,8),(38,8),(8,38),(38,38)):
            column(district,px,pz,floor_y+1,min(floor_y+8,45),"sevenstars:triangle_core_pillar")
            district.put(px,min(floor_y+9,45),pz,"minecraft:soul_lantern")
    # Every external room door gets a five-wide bridge to its level gallery.
    for side,standing,offset,_room in exits:
        floor_y=standing-1
        if side in ("north","south"):
            cx=23+offset; z1,z2=(0,10) if side=="north" else (36,46)
            floor_rect(district,cx-2,z1,cx+2,z2,"minecraft:polished_deepslate",floor_y)
        else:
            cz=23+offset; x1,x2=(0,10) if side=="west" else (36,46)
            floor_rect(district,x1,cz-2,x2,cz+2,"minecraft:polished_deepslate",floor_y)
    floor_rect(district,21,31,25,46,"minecraft:polished_deepslate",40)
    build_grand_helix(district)
    for yy in range(4,44,6): district.put(23,yy,19,"minecraft:chain")
    result[district.name]=district

    for name,(sx,sz,sy) in VERTICAL_ROOMS.items():
        room=Template(name,sx,sz,sy); room.box(); entry(room,"south",name=f"sevenstars:town/{name}_entry")
        decorate_vertical_room(room); result[name]=room
    return result


def decorate_vertical_room(t):
    x,y,z=t.size; cx=x//2; cz=z//2
    for pz in range(2,z-2): t.put(cx,0,pz,"sevenstars:triangle_rune_tiles")
    if t.name=="ossuary":
        for px in (3,7,x-8,x-4):
            for pz in range(3,z-3,3):
                column(t,px,pz,1,3,"minecraft:bone_block"); t.put(px,4,pz,"minecraft:skeleton_skull")
        t.spawner(cx,1,4,"minecraft:skeleton")
    elif t.name=="ritual_classroom":
        floor_rect(t,5,5,x-6,z-6,"minecraft:polished_deepslate")
        for pz in range(7,z-4,4):
            for px in range(4,x-3,4): t.put(px,1,pz,"minecraft:dark_oak_stairs",{"facing":"north","half":"bottom","shape":"straight","waterlogged":"false"})
        t.put(cx,1,4,"minecraft:lectern"); t.put(cx,1,cz,"sevenstars:soul_calming_lamp")
        t.spawner(cx,1,z-4)
    elif t.name=="collapsed_cistern":
        floor_rect(t,7,7,x-8,z-8,"minecraft:water",1)
        floor_rect(t,cx-1,3,cx+1,z-4,"minecraft:stone_bricks",1)
        for px,pz in ((5,5),(x-6,5),(5,z-6),(x-6,z-6)): column(t,px,pz,1,10,"minecraft:mossy_stone_bricks")
        for px,pz in ((9,9),(x-10,z-10)): t.put(px,2,pz,"minecraft:sea_lantern")
    elif t.name=="wraith_barracks":
        for px in (5,11,17):
            for pz in range(4,z-4):
                if pz not in (9,10,11):
                    for yy in range(1,5): t.put(px,yy,pz,"minecraft:iron_bars")
        for px,pz in ((3,5),(x-4,5),(3,z-5),(x-4,z-5)): t.spawner(px,1,pz)
        t.put(cx,1,cz,"sevenstars:soul_calming_lamp")
    elif t.name=="archive_annex":
        for px in (2,3,x-4,x-3):
            for pz in range(3,z-3):
                for yy in range(1,6): t.put(px,yy,pz,"minecraft:bookshelf")
        for px in range(7,x-7): t.put(px,1,cz,"minecraft:lectern")
        t.chest(cx,1,3,"library")
    elif t.name=="forge_chamber":
        floor_rect(t,4,4,x-5,z-5,"minecraft:polished_blackstone")
        for px in (5,10,15,20): t.put(px,1,5,"minecraft:blast_furnace")
        for px,block in ((6,"minecraft:anvil"),(12,"minecraft:smithing_table"),(18,"minecraft:stonecutter")): t.put(px,1,cz,block)
        for px in (7,x-8):
            floor_rect(t,px,14,px+2,16,"minecraft:lava",1)
        for px,pz in ((4,4),(x-5,4),(4,z-5),(x-5,z-5)): column(t,px,pz,1,9,"minecraft:polished_blackstone_bricks")
    elif t.name=="meditation_cells":
        for px in (6,12,18):
            for pz in range(3,z-3):
                if pz not in (10,11,12): column(t,px,pz,1,5,"minecraft:dark_oak_planks")
        for px,pz in ((3,6),(9,6),(15,6),(21,6),(3,17),(9,17),(15,17),(21,17)):
            t.put(px,1,pz,"minecraft:gray_wool"); t.put(px,3,pz,"minecraft:soul_lantern")
    elif t.name=="map_room":
        floor_rect(t,5,5,x-6,z-6,"minecraft:smooth_stone")
        for px in range(7,x-7,4):
            for pz in range(7,z-7,4): t.put(px,0,pz,"sevenstars:triangle_rune_tiles")
        for px,pz in ((5,5),(x-6,5),(5,z-6),(x-6,z-6)): column(t,px,pz,1,8,"minecraft:chiseled_deepslate")
        for px in range(8,x-8,3): t.put(px,1,cz,"minecraft:cartography_table")
        t.chest(cx,1,4,"corridor_chest")


def decorate(t):
    x, y, z = t.size
    # A continuous rune route is the visual navigation language of the town.
    for zz in range(2, z-2):
        t.put(x//2, 0, zz, "sevenstars:triangle_rune_tiles")
    for side in ("north","south","west","east"):
        framed_arch(t,side)

    if t.name == "tutorial_lamp_room":
        floor_rect(t,4,5,10,10,"minecraft:polished_deepslate")
        for px,pz in ((4,5),(10,5),(4,10),(10,10)): column(t,px,pz,1,5,"minecraft:chiseled_deepslate")
        t.put(x//2,1,z//2,"sevenstars:soul_calming_lamp"); t.spawner(x//2,1,3)
        for px in (3,11): t.put(px,2,7,"minecraft:soul_lantern")
    elif t.name == "town_square":
        floor_rect(t,11,11,19,19,"minecraft:polished_deepslate")
        for px,pz in ((11,11),(19,11),(11,19),(19,19)): column(t,px,pz,1,6,"minecraft:chiseled_deepslate")
        for px,pz in ((15,10),(10,15),(20,15)): t.put(px,1,pz,"minecraft:stone_brick_slab")
        column(t,15,15,1,3,"sevenstars:triangle_core_pillar"); t.put(15,4,15,"sevenstars:soul_calming_lamp")
        for px,pz in ((7,15),(23,15),(15,7)): t.put(px,1,pz,"minecraft:soul_lantern")
        # The south vestibule carries the tutorial-lamp encounter. Folding it
        # into this template keeps the deepest route within the vanilla size=7
        # jigsaw limit while preserving the intended gameplay beat.
        floor_rect(t,11,22,19,27,"minecraft:polished_deepslate")
        for px,pz in ((11,22),(19,22),(11,27),(19,27)): column(t,px,pz,1,5,"minecraft:chiseled_deepslate")
        t.put(15,2,24,"minecraft:soul_lantern")
    elif t.name == "corridor_01_straight":
        for pz in (4,8,12):
            column(t,1,pz,1,4,"minecraft:stripped_dark_oak_log"); column(t,5,pz,1,4,"minecraft:stripped_dark_oak_log")
            t.put(1,2,pz,"minecraft:soul_lantern"); t.put(5,2,pz,"minecraft:soul_lantern")
    elif t.name == "corridor_02_turn":
        # Interior buttress makes the passage read as an L-turn rather than an empty box.
        floor_rect(t,2,2,10,5,"minecraft:deepslate_bricks")
        floor_rect(t,7,5,10,10,"minecraft:deepslate_bricks")
        for pz in range(6,11): column(t,5,pz,1,4)
        for px in (3,9): t.put(px,2,4,"minecraft:chain")
    elif t.name == "living_hub":
        # Closed street loop around a ruined communal courtyard.
        floor_rect(t,5,5,x-6,z-6,"minecraft:deepslate_bricks")
        floor_rect(t,11,10,x-12,z-11,"minecraft:mossy_stone_bricks")
        for px in range(12,x-12):
            for pz in (9,z-10): column(t,px,pz,1,4,"sevenstars:triangle_stone_bricks")
        for pz in range(10,z-10):
            for px in (11,x-12): column(t,px,pz,1,4,"sevenstars:triangle_stone_bricks")
        for px,pz in ((15,12),(31,12),(15,18),(31,18)): t.put(px,1,pz,"minecraft:barrel")
        floor_rect(t,20,12,26,18,"minecraft:stone_bricks"); t.put(23,1,15,"minecraft:cauldron")
        for px,pz in ((8,8),(38,8),(8,22),(38,22)): column(t,px,pz,1,6,"minecraft:chiseled_deepslate")
    elif t.name == "library":
        for pz in range(3,z-3):
            if pz not in (7,8,9):
                for px in (2,3,x-4,x-3):
                    for yy in range(1,5): t.put(px,yy,pz,"minecraft:bookshelf")
        for px in range(4,x-4): t.put(px,4,3,"minecraft:dark_oak_slab")
        t.put(x//2,1,4,"minecraft:lectern"); t.put(x//2,1,2,"minecraft:dark_oak_stairs",{"facing":"south","half":"bottom","shape":"straight","waterlogged":"false"})
        t.chest(x//2,1,z-4,"library")
        # Fixed display: the scale remains in place and unlocks its codex chapter on interaction.
        t.item_frame(x//2, 3, 1, "sevenstars:azure_dragon_scale", "south")
    elif t.name == "brewing_room":
        for px in range(3,x-3):
            if px%2: t.put(px,1,3,"minecraft:polished_blackstone_brick_slab")
        for px in (4,x//2,x-5): t.put(px,2,3,"minecraft:brewing_stand")
        for px,pz in ((4,8),(x//2,9),(x-5,8)): t.put(px,1,pz,"minecraft:cauldron")
        for px,pz in ((3,11),(x-4,11)): t.put(px,1,pz,"minecraft:barrel")
        for px in (5,11): t.put(px,3,6,"minecraft:soul_lantern")
    elif t.name == "residential_ruin":
        for px in (5,11):
            for pz in range(3,z-3):
                if pz not in (7,8): column(t,px,pz,1,4,"minecraft:cracked_stone_bricks")
        for px,pz in ((3,4),(8,4),(13,4)): t.put(px,1,pz,"minecraft:hay_block")
        for px,pz in ((3,10),(8,11),(13,10)): t.put(px,1,pz,"minecraft:barrel")
        for px,pz in ((4,6),(12,6)): t.put(px,1,pz,"minecraft:crafting_table")
        for px,pz in ((2,3),(14,11)): t.put(px,1,pz,"minecraft:cobweb")
    elif t.name == "maintenance_workshop":
        floor_rect(t,3,3,x-4,5,"minecraft:polished_andesite")
        for px,block in ((4,"minecraft:anvil"),(x//2,"minecraft:grindstone"),(x-5,"minecraft:stonecutter")): t.put(px,1,4,block)
        for px,pz in ((3,9),(x-4,9)): t.put(px,1,pz,"minecraft:scaffolding")
        for px in (5,x-6):
            for yy in range(2,6): t.put(px,yy,11,"minecraft:chain")
        t.put(x//2,1,10,"sevenstars:soul_calming_lamp"); t.put(x//2,0,10,"minecraft:cracked_deepslate_bricks")
    elif t.name == "control_hub":
        floor_rect(t,5,5,x-6,z-6,"minecraft:polished_deepslate")
        floor_rect(t,15,9,31,21,"minecraft:deepslate_bricks")
        for px in range(15,32):
            for pz in (9,21):
                for yy in range(1,5): t.put(px,yy,pz,"minecraft:iron_bars")
        for pz in range(10,21):
            for px in (15,31):
                for yy in range(1,5): t.put(px,yy,pz,"minecraft:iron_bars")
        for px,pz in ((18,12),(28,12),(18,18),(28,18)): t.put(px,1,pz,"minecraft:dark_oak_stairs",{"facing":"south","half":"bottom","shape":"straight","waterlogged":"false"})
        for px in (18,23,28): t.put(px,2,15,"minecraft:chain")
    elif t.name == "prison":
        for pz in (4,8,12):
            for side_x in (5,15):
                for yy in range(1,5):
                    for dx in range(-2,3): t.put(side_x+dx,yy,pz,"minecraft:iron_bars")
        for px,pz in ((3,3),(17,3),(3,11),(17,11)): t.spawner(px,1,pz)
        t.put(x//2,1,z-4,"sevenstars:soul_calming_lamp")
        t.put(x//2-1,1,z-4,"minecraft:dark_oak_stairs",{"facing":"north","half":"bottom","shape":"straight","waterlogged":"false"})
    elif t.name == "guard_station":
        floor_rect(t,3,3,11,5,"minecraft:dark_oak_planks")
        for px in (4,7,10): t.put(px,2,3,"minecraft:lever",{"face":"wall","facing":"south","powered":"false"})
        for px in (4,10): t.put(px,1,8,"minecraft:grindstone")
        for yy in range(1,5): t.put(12,yy,8,"minecraft:iron_bars")
        t.put(7,1,8,"minecraft:cartography_table")
    elif t.name == "research_room":
        floor_rect(t,7,7,13,12,"minecraft:polished_blackstone")
        for px,pz in ((4,5),(10,5),(16,5)):
            for dx in range(-2,3):
                for yy in range(1,5): t.put(px+dx,yy,pz+2,"minecraft:iron_bars")
        for px,entity in ((4,"minecraft:zombie"),(10,"minecraft:skeleton"),(16,"minecraft:spider")): t.spawner(px,1,5,entity)
        for px in range(7,14): t.put(px,1,10,"minecraft:smooth_stone_slab")
        for px in (3,17):
            for yy in range(1,4): t.put(px,yy,14,"minecraft:bookshelf")
    elif t.name == "lower_hub":
        # Broad triangular loop with a shrine/wayfinding pillar at each vertex.
        vertices=((23,7),(7,38),(39,38))
        for a,b in zip(vertices,vertices[1:]+vertices[:1]):
            steps=max(abs(b[0]-a[0]),abs(b[1]-a[1]))
            for n in range(steps+1):
                px=round(a[0]+(b[0]-a[0])*n/steps); pz=round(a[1]+(b[1]-a[1])*n/steps)
                for dx in (-1,0,1):
                    for dz in (-1,0,1): t.put(px+dx,0,pz+dz,"sevenstars:triangle_rune_tiles")
        for px,pz in vertices:
            column(t,px,pz,1,7,"sevenstars:triangle_core_pillar"); t.put(px,8,pz,"minecraft:soul_lantern")
        for px,pz in ((23,23),(18,29),(28,29)): t.put(px,1,pz,"minecraft:chiseled_deepslate")
    if t.name == "sealed_array":
        floor_rect(t,4,4,18,18,"minecraft:polished_blackstone")
        for n in range(12):
            for px,pz in ((11-n//2,5+n),(11+n//2,5+n),(5+n,16)): t.put(px,0,pz,"sevenstars:triangle_rune_tiles")
        for px,pz in ((11,5),(6,16),(16,16)): t.put(px,1,pz,"sevenstars:soul_calming_lamp")
        t.chest(3,2,11,"sealed_materials"); t.chest(19,2,11,"sealed_archive")
        for px,pz in ((8,9),(14,9),(8,14),(14,14)): t.spawner(px,1,pz)
        for px,pz in ((4,4),(18,4),(4,18),(18,18)): column(t,px,pz,1,7,"minecraft:chiseled_deepslate")
    if t.name.startswith("treasure_room_"):
        for px,pz in ((4,4),(16,4),(4,16),(16,16)): t.spawner(px,1,pz)
        for px,pz,table in ((10,3,"treasure_materials"),(17,10,"treasure_triangle"),(10,17,"treasure_supplies"),(3,10,"treasure_rare")): t.chest(px,2,pz,table)
        t.put(10,1,10,"sevenstars:soul_calming_lamp"); t.put(19,2,18,"minecraft:netherite_block")
        for px,pz in ((4,4),(16,4),(4,16),(16,16)):
            for dx,dz in ((-1,0),(1,0),(0,-1),(0,1)): t.put(px+dx,0,pz+dz,"minecraft:chiseled_deepslate")
        variant = int(t.name[-1])
        trap_sets={1:[(6,7),(7,7),(13,7),(14,7),(6,13),(14,13)],2:[(7,6),(7,8),(7,12),(7,14),(13,6),(13,14)],3:[(5,10),(6,10),(14,10),(15,10),(10,6),(10,14)],4:[(6,6),(14,6),(6,14),(14,14),(8,12),(12,8)]}
        for px,pz in trap_sets[variant]: t.put(px,0,pz,"sevenstars:qi_sapping_triangle_tile")
    elif t.name == "observation_gallery":
        for px in range(2,x-2):
            for yy in range(1,5): t.put(px,yy,2,"minecraft:tinted_glass")
        for px in (3,x-4): t.put(px,1,6,"minecraft:dark_oak_stairs",{"facing":"north","half":"bottom","shape":"straight","waterlogged":"false"})
        t.put(x//2,2,2,"minecraft:iron_bars")
    elif t.name == "core_vault":
        floor_rect(t,5,5,11,11,"sevenstars:triangle_rune_tiles")
        for px,pz in ((5,5),(11,5),(5,11),(11,11)): column(t,px,pz,1,5,"minecraft:chiseled_deepslate")
        column(t,8,8,1,4,"sevenstars:triangle_stone_bricks"); t.put(8,5,8,"sevenstars:soul_calming_lamp")
        t.chest(8,1,4,"core_vault")
    elif t.name == "corridor_chest":
        for pz in (4,8,12):
            column(t,1,pz,1,4,"minecraft:chiseled_deepslate"); column(t,7,pz,1,4,"minecraft:chiseled_deepslate")
        floor_rect(t,1,6,3,10,"minecraft:polished_deepslate")
        t.chest(2,1,8,"corridor_chest"); t.put(2,3,8,"minecraft:soul_lantern")
    elif t.name == "lamp_shrine_dead_end":
        floor_rect(t,3,3,7,7,"minecraft:polished_deepslate")
        for px in range(3,8): t.put(px,1,3,"minecraft:stone_brick_slab")
        t.put(5,2,4,"sevenstars:soul_calming_lamp")
        for px,pz in ((3,6),(7,6)): column(t,px,pz,1,5,"minecraft:cracked_deepslate_bricks")
        for px,pz in ((4,8),(5,7),(6,8)): t.put(px,2,pz,"sevenstars:triangle_rune_tiles")


def generate_templates():
    templates = {name: Template(name,*dims) for name,dims in DIMS.items()}
    for t in templates.values():
        t.box(ceiling=t.name != "entrance", clear_interior=t.name != "entrance")
    # Surface landmark: triangular rune outline and three unequal pillars.
    e = templates["entrance"]
    for n in range(10):
        for px,pz in ((15-n,6+n),(15+n,6+n),(6+n*2,16)):
            e.put(px,0,pz,"sevenstars:triangle_rune_tiles")
    for px,pz,height in ((15,6,8),(6,16,6),(24,16,10)):
        for yy in range(1,height): e.put(px,yy,pz,"sevenstars:triangle_stone_bricks")
    exit_to(e,"north","sevenstars:town/entrance_descent_entry","entrance_descent_pool")

    d=templates["entrance_descent"]; entry(d,"south",13,"sevenstars:town/entrance_descent_entry"); exit_to(d,"north","sevenstars:town/town_square_entry","town_square_pool",1)
    build_straight_descent(d,13,1)
    t=templates["tutorial_lamp_room"]; entry(t,name="sevenstars:town/tutorial_entry")
    t=templates["town_square"]; entry(t,name="sevenstars:town/town_square_entry"); exit_to(t,"east","sevenstars:town/corridor_01_entry","corridor_01_pool"); exit_to(t,"west","sevenstars:town/corridor_02_entry","corridor_02_pool"); exit_to(t,"north","sevenstars:town/descent_entry","descent_pool")
    # Straight corridor templates always run along their long Z axis. Jigsaw
    # rotation turns this south-to-north piece into an east/west passage when
    # it attaches to the town square's east exit.
    t=templates["corridor_01_straight"]; entry(t,"south",name="sevenstars:town/corridor_01_entry"); exit_to(t,"north","sevenstars:town/living_hub_entry","living_hub_pool")
    t=templates["living_hub"]; entry(t,"west",name="sevenstars:town/living_hub_entry"); exit_to(t,"north","sevenstars:town/library_entry","library_pool",offset=-12); exit_to(t,"north","sevenstars:town/brewing_entry","brewing_pool",offset=12); exit_to(t,"south","sevenstars:town/residential_entry","residential_pool",offset=-12); exit_to(t,"south","sevenstars:town/workshop_entry","workshop_pool",offset=12); exit_to(t,"east","sevenstars:town/tutorial_entry","tutorial_pool")
    for name,key in (("library","library"),("brewing_room","brewing")):
        entry(templates[name],"south",name=f"sevenstars:town/{key}_entry")
    for name,key in (("residential_ruin","residential"),("maintenance_workshop","workshop")):
        entry(templates[name],"north",name=f"sevenstars:town/{key}_entry")
    t=templates["corridor_02_turn"]; entry(t,"east",name="sevenstars:town/corridor_02_entry"); exit_to(t,"north","sevenstars:town/control_hub_entry","control_hub_pool")
    t=templates["control_hub"]; entry(t,"south",name="sevenstars:town/control_hub_entry"); exit_to(t,"north","sevenstars:town/prison_entry","prison_pool",offset=-12); exit_to(t,"north","sevenstars:town/guard_entry","guard_pool",offset=12); exit_to(t,"east","sevenstars:town/research_entry","research_pool"); exit_to(t,"west","sevenstars:town/extension_entry","extension_pool")
    for name,key in (("prison","prison"),("guard_station","guard")): entry(templates[name],name=f"sevenstars:town/{key}_entry")
    entry(templates["research_room"],"west",name="sevenstars:town/research_entry")
    t=templates["stairs_down"]; entry(t,"south",15,"sevenstars:town/descent_entry"); exit_to(t,"east","sevenstars:town/lower_hub_entry","lower_hub_pool",1); build_low_east_turn(t); build_straight_descent(t,15,1)
    t=templates["stairs_spiral_down"]; entry(t,"south",15,"sevenstars:town/descent_entry"); exit_to(t,"east","sevenstars:town/lower_hub_entry","lower_hub_pool",1); build_low_east_turn(t); build_spiral_descent(t)
    t=templates["lower_hub"]; entry(t,"west",name="sevenstars:town/lower_hub_entry"); exit_to(t,"west","sevenstars:town/treasure_entry","treasure_pool",offset=-20); exit_to(t,"north","sevenstars:town/corridor_chest_entry","corridor_chest_pool"); exit_to(t,"east","sevenstars:town/observation_entry","observation_pool"); exit_to(t,"south","sevenstars:town/vertical_access_entry","vertical_access_pool"); exit_to(t,"south","sevenstars:town/extension_entry","extension_pool",offset=-20); exit_to(t,"south","sevenstars:town/extension_entry","extension_pool",offset=20)
    for n in range(1,5): entry(templates[f"treasure_room_0{n}"],"east",name="sevenstars:town/treasure_entry")
    t=templates["observation_gallery"]; entry(t,name="sevenstars:town/observation_entry"); exit_to(t,"north","sevenstars:town/sealed_entry","sealed_pool")
    t=templates["sealed_array"]; entry(t,name="sevenstars:town/sealed_entry"); exit_to(t,"north","sevenstars:town/core_vault_entry","core_vault_pool")
    entry(templates["core_vault"],name="sevenstars:town/core_vault_entry")
    t=templates["corridor_chest"]; entry(t,"south",name="sevenstars:town/corridor_chest_entry"); exit_to(t,"north","sevenstars:town/lamp_shrine_entry","lamp_shrine_pool")
    entry(templates["lamp_shrine_dead_end"],"west",name="sevenstars:town/lamp_shrine_entry")
    for t in templates.values():
        decorate(t)
    extensions={name:extension_variant(templates[base],name,continues) for name,(base,continues) in EXTENSION_VARIANTS.items()}
    templates.update(extensions)
    templates.update(macro_extension_templates())
    templates.update(vertical_district_templates())
    validate_connector_graph(templates)
    for t in templates.values():
        if t.name != "entrance":
            air_count = sum(1 for name, _props, _nbt in t.blocks.values() if name == "minecraft:air")
            assert air_count > 0, f"{t.name} has no explicit excavation air"
        if t.name.startswith("treasure_room_"):
            names=[block for block,_props,_nbt in t.blocks.values()]
            assert names.count("minecraft:chest") == 4
            assert names.count("minecraft:spawner") == 4
            assert names.count("sevenstars:soul_calming_lamp") == 1
            assert names.count("minecraft:netherite_block") == 1
        if t.name in {"corridor_01_straight", "corridor_chest"}:
            orientations={props.get("orientation") for block,props,_nbt in t.blocks.values() if block == "minecraft:jigsaw"}
            assert orientations == {"north_up", "south_up"}, f"{t.name} connectors must follow its long Z axis"
        if t.name == "ritual_classroom":
            wraith_spawners=[nbt for block,_props,nbt in t.blocks.values()
                             if block == "minecraft:spawner"
                             and nbt["SpawnData"][1]["entity"][1]["id"] == s("sevenstars:tormented_wraith")]
            assert len(wraith_spawners) == 1, "ritual classroom must contain exactly one tormented wraith spawner"
        if t.name == "library":
            assert len(t.entities) == 1, "main library must contain the Azure Dragon Scale display"
            frame_nbt = t.entities[0]["nbt"][1]
            assert frame_nbt["id"] == s("minecraft:item_frame")
            assert frame_nbt["Item"][1]["id"] == s("sevenstars:azure_dragon_scale")
        if t.name == "extension_library":
            assert not t.entities, "extension libraries must not duplicate the unique chapter unlock"
        if t.name in {"stairs_spiral_down", "vertical_district"}:
            stair_facings={props.get("facing") for block,props,_nbt in t.blocks.values()
                           if block == "minecraft:deepslate_tile_stairs"}
            assert stair_facings == {"north", "south", "east", "west"}, \
                f"{t.name} must retain stairs turning through all four directions"
            assert all(block != "minecraft:deepslate_brick_wall"
                       for block,_props,_nbt in t.blocks.values()), \
                f"{t.name} must widen the spiral instead of adding guard walls"
        if t.name == "stairs_down":
            assert t.blocks[(t.size[0]//2, 1, 2)][0] == "minecraft:deepslate_tile_stairs", \
                "straight descent must retain its low-landing connection stair"
        if t.name == "stairs_spiral_down":
            assert t.blocks[(4, 14, 10)][0] == "minecraft:deepslate_tile_stairs", \
                "spiral descent must retain its high-landing connection stair"
            assert t.blocks[(5, 1, 2)][0] == "minecraft:deepslate_tile_stairs", \
                "spiral descent must retain its low-landing connection stair"
        if t.name == "vertical_district":
            for standing_y in (41,37,25,13,1):
                block,props,_nbt=t.blocks[(23,standing_y-1,31)]
                assert block == "minecraft:deepslate_tile_slab" and props.get("type") == "top", \
                    f"grand helix must meet the gallery landing at standing Y={standing_y}"
        for block, _props, nbt in t.blocks.values():
            if block == "minecraft:jigsaw":
                assert nbt["final_state"] == s("sevenstars:triangle_stone_bricks"), \
                    f"{t.name} contains a connector that would open without a generated passage"
        t.save()
    return templates


def pool(name, locations):
    return {"name":f"sevenstars:triangle_stone_town/{name}","fallback":"minecraft:empty","elements":[
        {"weight":12 if loc == "extension_long_corridor" else 7 if loc == "extension_crossroads" else 5 if loc == "extension_corridor_straight" else 4 if loc == "extension_corridor_turn" else 3 if loc == "extension_multilevel_hall" else 2 if ("vertical_" in loc or any(kind in loc for kind in ("residential","guard","workshop"))) else 1,
         "element":{"element_type":"minecraft:single_pool_element","location":f"sevenstars:triangle_stone_town/{loc}","processors":"minecraft:empty","projection":"rigid"}} for loc in locations]}


def generate_pools():
    POOLS.mkdir(parents=True,exist_ok=True)
    for name,locations in pool_mapping().items():
        (POOLS/f"{name}.json").write_text(json.dumps(pool(name,locations),indent=2)+"\n",encoding="utf-8")


def validate_connector_graph(templates):
    """Prove that every authored exit has a matching entry and every room is reachable."""
    mapping=pool_mapping()
    graph={name:set() for name in templates}
    prefix="sevenstars:triangle_stone_town/"
    for name,template in templates.items():
        for block,_props,nbt in template.blocks.values():
            if block != "minecraft:jigsaw" or nbt["pool"][1] == "minecraft:empty":
                continue
            pool_id=nbt["pool"][1]
            assert pool_id.startswith(prefix), f"{name} uses foreign connector pool {pool_id}"
            pool_name=pool_id.removeprefix(prefix)
            assert pool_name in mapping, f"{name} references missing pool {pool_name}"
            target=nbt["target"][1]
            matches=[]
            for candidate_name in mapping[pool_name]:
                candidate=templates[candidate_name]
                if any(candidate_nbt and candidate_nbt["name"][1] == target
                       for candidate_block,_candidate_props,candidate_nbt in candidate.blocks.values()
                       if candidate_block == "minecraft:jigsaw"):
                    matches.append(candidate_name)
            assert matches, f"{name} exit {target} has no attachable room in {pool_name}"
            graph[name].update(matches)

    reachable=set()
    pending=["entrance"]
    while pending:
        name=pending.pop()
        if name in reachable:
            continue
        reachable.add(name)
        pending.extend(graph[name]-reachable)
    unreachable=set(templates)-reachable
    assert not unreachable, f"rooms unreachable from entrance: {sorted(unreachable)}"


def validate_worldgen_limits():
    path=ROOT/"src/main/resources/data/sevenstars/worldgen/structure/triangle_stone_circle.json"
    config=json.loads(path.read_text(encoding="utf-8"))
    if config["type"] == "minecraft:jigsaw":
        assert 0 <= config["size"] <= 7, "Minecraft 1.20.1 jigsaw size must be <= 7"
        assert 1 <= config["max_distance_from_center"] <= 128, \
            "Minecraft 1.20.1 max distance must be <= 128"
    else:
        assert config["type"] == "sevenstars:triangle_stone_town", \
            "triangle stone town must use its connectivity-owning structure type"


if __name__ == "__main__":
    validate_worldgen_limits(); generate_templates(); generate_pools()
    print(f"Generated {len(DIMS)+len(EXTENSION_VARIANTS)+len(MACRO_EXTENSIONS)+2+len(VERTICAL_ROOMS)} templates and {len(list(POOLS.glob('*.json')))} pools")
