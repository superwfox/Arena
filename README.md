# Arena - Minecraft 竞技场插件

基于 Paper 1.21 的 Minecraft 竞技场生存插件，玩家通过传送门进入程序化生成的竞技场世界，挑战逐波增强的怪物。

## 核心特性

- 程序化世界生成：基于 Simplex 噪声的地形高度变化 + 预设区块模板
- 事件驱动架构：无定时轮询，通过 Bukkit 事件监听实现状态流转
- 异步任务调度：怪物生成检测、玩家状态监控均为异步执行，主线程零阻塞
- 动态难度系统：怪物血量、装备品质随挑战次数递增

## 项目结构

```
arena/
├── Arena.java              # 插件入口，世界初始化
├── WorldManager.java       # 世界创建/重置，自定义 ChunkGenerator
├── ChunkTemplate.java      # 区块模板数据，Simplex 噪声地形生成
├── BlockPropertyUtil.java  # 方块属性编解码（方向、半砖类型）
├── MobChain.java           # 波次控制、怪物生成、胜利判定
├── MobContainer.java       # 怪物类型定义、Team 管理
├── PortalListener.java     # 传送门事件、死亡/变形事件处理
└── SaveChunkTemplateCommand.java  # 开发工具：导出区块为模板代码
```

## 性能设计

| 模块 | 设计 | 性能优势 |
|------|------|----------|
| 世界生成 | `ChunkGenerator.generateNoise()` 覆写 | 仅在区块首次加载时执行，无运行时开销 |
| 波次监控 | `BukkitRunnable.runTaskTimerAsynchronously()` | 异步线程执行，不阻塞主线程 tick |
| BOSS 检测 | `CompletableFuture` + 主线程回调 | 避免异步直接访问 Bukkit API |
| 怪物管理 | Scoreboard Team | 原生 API 管理实体分组，无额外数据结构 |
| 模板存储 | 一维数组 `int[] + Material[]` | 内存紧凑，遍历高效 |

## 游戏流程

1. 玩家站在镀金黑石上进入下界门 → 传送至竞技场
2. 45 秒准备时间（蹲在黄色陶瓦上可退出）
3. 5 波怪物挑战，每波 12 tick 刷怪周期
4. 第 5 波为 BOSS 战（Warden / Iron Golem）
5. 通关后烟花庆祝，进入下一轮（难度递增）

## 命令

- `/arena` - 导出当前区块为模板代码（开发用）
