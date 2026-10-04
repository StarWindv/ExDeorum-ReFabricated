# Ex Deorum: ReFabricated

###### *"From the Gods"*

> **此模组是 Fabric 移植版**
> 
> 原版模组(NeoForge)是
> [Ex Deorum](https://github.com/Thedarkcolour/ExDeorum)
> 
> 本模组正是以它为模板而移植的
> 
> 请在此处报告 Fabric 版本的问题, 不要打扰他们

## I. 修改了什么?

除了加载器替换之外, 该端口并非上游版本的普通复制.

主要差异如下: 

### 2.1 网络、配方和物品处理程序:

直接基于 Fabric API 构建, 取代了 NeoForge 的对应功能.

为延迟注册表、物品与流体堆栈类型以及传输能力处理器编写了替代实现, 因为 Fabric 没有内置的对应模块(还是太轻了)

### 2.2 没有 ASM 核心模块:

上游 NeoForge 版本包含一个带有类转换器的核心模块, 这些已被 Mixin 替代. 

### 2.3 移除了 SkyBlock 世界类型:

原版自定义了其自身的区块生成器和世界预设.

空岛和虚空世界应由其他模组(`Carpet Sky Additions Reborn`等)提供

### 2.4 新增了掉落率配置页面:

包含面向玩家的配置界面, 上游版本的概率仅支持数据包内设置. 

### 2.5 为其他模组提供 API:

提供了`top.starwindv.exdeorum.api.ExDeorumApi`,

用于其它模组作者为筛矿机制注册相关产物, 这是上游版本完全没有的机制

### 2.6 图标

重做了一个 Logo, 问就是豆包画的

## II. 开源协议

这里的一切都和它的 NeoForge 版本一样保持 GPL-3-Clause 协议开源

具体的协议拆分情况请见[此处](https://github.com/StarWindv/ExDeorum-ReFabricated/blob/26.2/License.md)
