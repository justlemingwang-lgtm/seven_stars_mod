# 青龙 Boss 最终美术资源清单

代码当前使用原版资源作为合法占位，不包含伪装成最终美术的临时贴图。下列文件是完整正式美术交付清单。

## Blockbench 模型源文件

- `src/main/resources/assets/sevenstars/models/entity/azure_dragon.bbmodel`

模型必须保留这些骨骼名称：

- `root`
- `head`
- `neck`
- `body`
- `left_wing`
- `right_wing`
- `wing_blades`
- `tail_1`
- `tail_2`
- `tail_3`
- `front_left_leg`
- `front_right_leg`
- `back_left_leg`
- `back_right_leg`

模型内必须制作这些动画：

- `idle`
- `movement`
- `physical_breath`
- `stomp`
- `turning_tail`
- `phase_two_transition`
- `azure_breath`
- `divine_tail`
- `bull_slash_windup`
- `bull_slash_chase`
- `bull_slash_left`
- `bull_slash_right`
- `phase_three_transition`
- `defeated`

本项目不使用 GeckoLib，因此不需要也不应提交 `azure_dragon.geo.json` 或 GeckoLib `animation.json`。Blockbench 模型应导出为 Mojang mappings Java entity model，现有 `AzureDragonModel` 与 `AzureDragonAnimations` 是接入目标。

## 实体贴图

- `src/main/resources/assets/sevenstars/textures/entity/azure_dragon.png`
- `src/main/resources/assets/sevenstars/textures/entity/azure_dragon_emissive.png`

两张图必须尺寸一致，建议 `128x128` 或 `256x256`。发光层仅保留眼睛、七道星痕、口部蓄力纹与双翼利刃区域，其余像素透明。

## 方块贴图

- `src/main/resources/assets/sevenstars/textures/block/azure_seal_chain.png`
- `src/main/resources/assets/sevenstars/textures/block/azure_seal_chain_broken.png`
- `src/main/resources/assets/sevenstars/textures/block/azure_soul_container_side.png`
- `src/main/resources/assets/sevenstars/textures/block/azure_soul_container_side_active.png`
- `src/main/resources/assets/sevenstars/textures/block/azure_soul_container_side_summoned.png`
- `src/main/resources/assets/sevenstars/textures/block/azure_soul_container_top.png`
- `src/main/resources/assets/sevenstars/textures/block/azure_soul_container_bottom.png`
- `src/main/resources/assets/sevenstars/textures/block/azure_butcher_spawn_rune.png`

对应方块状态和模型 JSON 已建立：完整/断裂锁链、未启动/召唤中/已召唤容器、屠夫召唤符文。收到贴图后只需把现有模型中的原版占位纹理路径换为以上路径。

## 物品贴图

- `src/main/resources/assets/sevenstars/textures/item/azure_dragon_scale.png`
- `src/main/resources/assets/sevenstars/textures/item/lost_star_magic_token.png`

锁链、灵魂容器和召唤符文的物品形态直接复用对应方块模型，不需要重复绘制物品贴图。

## GUI 与 HUD 贴图

- `src/main/resources/assets/sevenstars/textures/gui/qinglong_illusion_overlay.png`
- `src/main/resources/assets/sevenstars/textures/gui/armor_disabled.png`

`qinglong_illusion_overlay.png` 应为可平铺或全屏拉伸的半透明火焰/岩浆边缘覆盖，不能完全遮挡中心视野。`armor_disabled.png` 建议为 `16x16` 或 `32x32`。

## 自定义粒子贴图与描述文件

正式粒子包需要以下贴图：

- `src/main/resources/assets/sevenstars/textures/particle/azure_arc.png`
- `src/main/resources/assets/sevenstars/textures/particle/azure_star.png`
- `src/main/resources/assets/sevenstars/textures/particle/azure_breath.png`
- `src/main/resources/assets/sevenstars/textures/particle/azure_stomp_warning.png`
- `src/main/resources/assets/sevenstars/textures/particle/azure_tail_warning.png`
- `src/main/resources/assets/sevenstars/textures/particle/azure_wing_blade_slash.png`
- `src/main/resources/assets/sevenstars/textures/particle/azure_soul_burst.png`
- `src/main/resources/assets/sevenstars/textures/particle/azure_seven_star_array.png`
- `src/main/resources/assets/sevenstars/textures/particle/qinglong_illusion_flame.png`

以及对应描述文件：

- `src/main/resources/assets/sevenstars/particles/azure_arc.json`
- `src/main/resources/assets/sevenstars/particles/azure_star.json`
- `src/main/resources/assets/sevenstars/particles/azure_breath.json`
- `src/main/resources/assets/sevenstars/particles/azure_stomp_warning.json`
- `src/main/resources/assets/sevenstars/particles/azure_tail_warning.json`
- `src/main/resources/assets/sevenstars/particles/azure_wing_blade_slash.json`
- `src/main/resources/assets/sevenstars/particles/azure_soul_burst.json`
- `src/main/resources/assets/sevenstars/particles/azure_seven_star_array.json`
- `src/main/resources/assets/sevenstars/particles/qinglong_illusion_flame.json`

在正式粒子包交付前，代码使用 `END_ROD`、`ELECTRIC_SPARK`、`SOUL_FIRE_FLAME`、`FLAME` 和彩色尘埃保证判定提示可读。

## 最终音频文件

- `src/main/resources/assets/sevenstars/sounds/entity/azure_dragon/ambient.ogg`
- `src/main/resources/assets/sevenstars/sounds/entity/azure_dragon/hurt.ogg`
- `src/main/resources/assets/sevenstars/sounds/entity/azure_dragon/roar.ogg`
- `src/main/resources/assets/sevenstars/sounds/entity/azure_dragon/breath.ogg`
- `src/main/resources/assets/sevenstars/sounds/entity/azure_dragon/stomp.ogg`
- `src/main/resources/assets/sevenstars/sounds/entity/azure_dragon/tail.ogg`
- `src/main/resources/assets/sevenstars/sounds/entity/azure_dragon/phase_transition.ogg`
- `src/main/resources/assets/sevenstars/sounds/entity/azure_dragon/defeated.ogg`
- `src/main/resources/assets/sevenstars/sounds/entity/azure_dragon/star_reveal.ogg`
- `src/main/resources/assets/sevenstars/sounds/block/azure_seal_chain_break.ogg`

全部声音事件与中英文字幕已经注册；在正式音频交付前，`sounds.json` 合法引用原版声音事件。
