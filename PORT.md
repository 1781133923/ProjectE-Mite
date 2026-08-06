# ProjectE-MITE 移植说明

把 1.7.10 Forge 版 ProjectE（`X:\MITEmoddev\ProjectE-MC17`，PE1.10.1，354 个源文件）移植到
**MITE 1.6.4 R196 + FishModLoader 3.4.1（FML3）**，前置依赖 **RustedIronCore 1.5.5**。

## 当前状态

- ✅ 全部 354 个 ProjectE 源文件在本工程内编译通过（`gradlew build` 成功，产物 `build/libs/ProjectE-0.1.0.jar`）
- ✅ 服务端实测启动成功：mod 加载、配置加载、物品/方块/方块实体/配方经 FML 事件注册、
  服务端启动、EMC 映射（`Registered 203 EMC values`）均无异常
- ✅ 客户端实测可进入游戏（`runClient`）：主菜单 → 单机世界 → 玩家进入世界均正常，
  ProjectE 随游戏加载
- ✅ 贴图缺失问题已修复：根因是资源被复制成了 `assets/assets/projecte`（多套一层），
  已修正目录层级，jar 内资源完整、客户端无 Missing resource
- ✅ 玩家扩展属性（Transmutation/AlchBag）改为按需注册，变化术等 GUI 不再因
  `getDataFor` 返回 null 崩溃
- ✅ 网络通道修复：`SimpleNetworkWrapper` 原来所有实例共用一个静态 channel，
  GUI 通道会覆盖主通道的接收器导致 `Unknown discriminator`；改为每个实例独立通道。
  同时修复了接收路径上 `ByteBuf` 委托模式未覆盖 `readBytes` 导致 EOF、
  NBT 包读写头部不对称、`FMLClientHandler.getClientPlayerEntity()` 返回 null 等问题

## 移植架构

### 1. Forge 兼容层（shim）
为了让 1.7.10 源码尽量原样编译，工程内实现了一套最小 Forge API 替代：

- `cpw.mods.fml.*`：`@Mod` 注解、`GameRegistry`、`EntityRegistry`、`NetworkRegistry`、
  `SimpleNetworkWrapper`、`FMLCommonHandler`、tick/player/input 事件、`Optional` 等
- `net.minecraftforge.*`：事件总线（`@SubscribeEvent`）、`OreDictionary`（启发式从 MITE
  注册表生成）、`Configuration`、`ISpecialArmor`、`IExtendedEntityProperties`、
  `ForgeDirection`、流体 shim、`IShearable/IPlantable` 等
- 集成类 stub：Baubles、Thaumcraft、Chisel、NEI、MineTweaker、invtweaks、netty `ByteBuf`
  （MITE 1.6.4 无 netty，自实现了一个 DataInput/DataOutput 版 ByteBuf）
- `com.mojang.authlib.GameProfile`、`net.minecraft.init.Items/Blocks` 等 1.7.10 独有类型映射

### 2. 生命周期入口（`moze_intel.projecte.ProjectE`）
`ProjectE implements ModInitializer`：

- 手动设置 `PECore.proxy`（shim 不处理 `@SidedProxy`）
- 用 shim 事件对象调用 `PECore.preInit/load/postInit`
- 注册 `MITEEventBridge` 到 `MITEEvents.MITE_EVENT_BUS`，把 `GameRegistry` 队列在
  FML 的 `ItemRegistryEvent/BlockRegistryEvent/RecipeRegistryEvent/TileEntityRegisterEvent/
  EntityRegisterEvent` 中真正落地
- 用 RIC 的 `Handlers.Tick` 驱动 `TickEvent.ServerTickEvent/PlayerTickEvent` 和
  `PECore.serverStarting`（首个服务端 tick 时注册指令并启动 EMC 映射）

### 3. 1.7.10 → MITE API 的主要机械改造
- 包名扁平化：`net.minecraft.entity.player.EntityPlayer` → `net.minecraft.EntityPlayer` 等
- `EntityPlayerMP` → `ServerPlayer`；`getHeldItem()` → `getHeldItemStack()`
- 方块：`Block(Material)` → `Block(id, Material, BlockConstants)`，`setBlockName` →
  `setUnlocalizedName`，`onBlockPlacedBy` → `onBlockPlacedMITE`，`onBlockActivated` 的
  `int side` → `EnumFace`，`hasTileEntity(int)/createTileEntity(World,int)` →
  `hasTileEntity()/createNewTileEntity(World)`（经 `ITileEntityProvider`/`BlockContainer`）
- 物品：`onItemRightClick(ItemStack,World,EntityPlayer)` → `onItemRightClick(EntityPlayer,float,boolean)`，
  `addInformation(...)` 追加 `Slot` 参数，`registerBlockIcons` → `registerIcons`，
  `getIconFromDamage` → `getIconFromSubtype`，`getDigSpeed` → `getStrVsBlock(Block,int)`
- 网络：自建 `ByteBuf`；收发走 MITE `Packet250CustomPayload` + RIC `PacketReader/Network`
- GUI：`player.openGui` → `PEGuiHelper.openGui`（自定义 OpenGui 包，服务端建容器、
  客户端同步 windowId 并显示 GUI）
- 聊天：`addChatMessage(IChatComponent)` → `PEChatHelper.send(...)`
- 伤害：`attackEntityFrom(src, dmg)` → `attackEntityFrom(new Damage(src, dmg))`
- 熔炉：`FurnaceRecipes` 的键为 `Integer`（物品 ID），相关 mapper 已适配

## 构建与运行

```powershell
$env:GRADLE_USER_HOME='C:\Users\17811\.gradle'
.\gradlew.bat build --offline
Copy-Item build\libs\ProjectE-0.1.0.jar run\mods\
Copy-Item X:\MITEmoddev\RustedIronCore-1.5.5.jar run\mods\
.\gradlew.bat runClient   # 或 runServer
```

注意：`fml.mod.json` 中依赖 id 是 `rusted_iron_core`（1.5.5 起的名字，下划线）。

## 客户端调试踩坑记录

- **不要**在 `src/main/resources` 根部放 `pack.mcmeta`：MITE 的
  `DefaultResourcePack.getPackMetadata` 从类路径根读 `/pack.mcmeta`，工程里的
  `pack.mcmeta`（没有 language 段）会遮蔽游戏的语言元数据，导致
  `LanguageManager.getCurrentLanguage()` 为 null 直接崩在 `Initializing game`。
- **不要**重复实现 MITE 已有的类：`net.minecraft.EnumChatFormatting` 是 MITE 自带类，
  移植时新建同名 shim 会顶掉原类（`NoSuchMethodError: getByChar`）。已删除该 shim。
- 客户端手册初始化（`ManualPageHandler.init`）需要 `Minecraft.getMinecraft()` 存在，
  FML 入口执行时还没有实例，已改为通过 RIC `Handlers.Initialization` 延迟到游戏启动后。

## 已知缺口 / 下一步

1. **客户端渲染**：方块/物品模型与 1.6.4 纹理注册机制（IBlock/IItem 接口、IconRegister）
   尚未实机验证；`MinecraftForgeClient.registerItemRenderer` 未接入 `RenderItem`。
2. **容器点击**：MITE 的 `Container.slotClick` 是 final，无法覆写；原 6 个容器的自定义
   slotClick 已改名保留（`handleSlotClick`），尚未用 mixin 重新接线（影响变化术/袋子的点击逻辑）。
3. **玩家扩展属性**：`IExtendedEntityProperties` 用内存 Map 暂存（`ExtendedProperties`），
   尚未挂到玩家 NBT 保存/读取（需要 mixin `EntityPlayer.writeEntityToNBT/readEntityFromNBT`）。
4. **带元数据物品**：MITE 的 `has_subtypes` 是 final 且由构造器决定；多数 ProjectE 物品用无参
   构造，子类型（燃料/克莱因星/袋子等）未正确标记，`ItemStack.getItemSubtype()` 可能异常。
5. **天气控制**：MITE 移除了 `WorldInfo.setRaining/setRainTime`，evertide/volcanite 的
   下雨/停雨目前是 no-op 钩子。
6. **OreDictionary**：启发式生成，覆盖面有限；常见矿石/锭可用，但自定义词条不足。
7. **燃料**：`IFuelHandler` 未接入 MITE 熔炉燃料逻辑。
8. **按键**：`ClientRegistry.registerKeyBinding` 只是登记，未接入 RIC 的 KeybindingHandler 与
   KeyInputEvent 派发。
9. **时装/特殊效果**：`ISpecialArmor` 的减伤、Gem 盔甲被动（onArmorTick 改名保留但未挂 tick）、
   玩家飞行/防火/跳跃高度需要 RIC 的 PlayerAttribute/Combat 钩子或 mixin。
10. **崩溃外的杂音**：更新/UUID 检查线程在无外网时打印异常（无碍）；NBT 白名单里其他 mod
    的物品已降级为 WARN 跳过。

## 文件布局

- `src/main/java/moze_intel/projecte/`：移植的 ProjectE（保留原包名）
- `src/main/java/moze_intel/projecte/compat/`：移植辅助（GUI/聊天/配方适配/扩展属性等）
- `src/main/java/cpw/mods/fml/`、`net/minecraftforge/`、`io/netty/`、`baubles/`、
  `thaumcraft/`、`codechicken/`、`minetweaker/`、`stanhebben/`、`com/mojang/`、
  `invtweaks/`、`net/minecraft/init/`：兼容层与依赖 stub
- `docs/helloFML-example/`：原 HelloFML 示例代码备份
