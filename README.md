# scu-java-battlecity

## 简介

~~某不知名西南带专的java课作业）~~

这是一个非常粗糙的项目，内容是模仿经典的90坦克大战，属未完成品，具有一定的学习参考意义（大概）。

框架的具体使用参照[这里]([ml-game-framework/README.md at main · Masterlong-Dragon/ml-game-framework (github.com)](https://github.com/Masterlong-Dragon/ml-game-framework/blob/main/README.md))。

<img src=".\演示.png" alt="演示" style="zoom:50%;" />

## 功能

### 完成部分

- 上下左右方向键控制玩家坦克移动方向
- 空格键玩家坦克发射炮弹
- Q键玩家坦克换肤（并没有火力方面的同步更改）
- 三个敌方坦克出生点
- 绿色敌方坦克随机移动并发射炮弹
- 地图图块阻拦和爆炸消失效果
- 坦克爆炸和消失
- 玩家坦克被命中重生
- 从地图文件map.txt读取地图

### 未完成部分

- 分数具体判定
- 家被攻破等机制判定
- 具体图块的性质判定（已经实现了相关接口：例如落水的功能，这个本来是有的，但是被要求删掉了qwq）
- ……

## 特点

- 遵照jdk8标准编写，使用swing作为绘图框架，没有使用其它引擎
- 项目主要由提供绘图与逻辑管理的framework部分与包含游戏详细业务代码的game部分组成
- 瞎混进去了一堆有的没的设计模式，现在我自己也说不清楚
- ~~很多内容没有忠实地还原原作，因为老师不要求，该项目大都是支持相关功能实现的：我曾经做了交上去还被助教批画蛇添足~~

## 项目整体结构

![](.\src.png)

