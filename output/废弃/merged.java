package merged;
import javax.swing.*;
import javax.imageio.ImageIO;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Random;
import java.util.Queue;
import java.util.Objects;
import java.util.List;
import java.util.Iterator;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.*;
import java.io.IOException;
import java.io.File;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentAdapter;
import java.awt.*;
 class AABB {
    public float x, y, width, height;

    public AABB(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Direction getCenter() {
        return new Direction((int) (x + width / 2), (int) (y + height / 2));
    }

    //容纳
    public boolean contains(AABB r) {
        return this.width > 0 && this.height > 0 && r.width > 0 && r.height > 0
                && r.x >= this.x && r.x + r.width <= this.x + this.width
                && r.y >= this.y && r.y + r.height <= this.y + this.height;
    }

    //相交
    //注意排除相切的情况
    public boolean overlaps(AABB r) {
        if (this.width > 0 && this.height > 0 && r.width > 0 && r.height > 0) {
            if (this.x >= r.x + r.width || r.x >= this.x + this.width) return false;
            return !(this.y >= r.y + r.height) && !(r.y >= this.y + this.height);
        } else return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AABB aabb = (AABB) o;
        return Float.compare(aabb.x, x) == 0 && Float.compare(aabb.y, y) == 0 && Float.compare(aabb.width, width) == 0 && Float.compare(aabb.height, height) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, width, height);
    }

    @Override
    public String toString() {
        return "x: " + x + " y: " + y + " w: " + width + " h: " + height;
    }
}
 class Animation {
    private final ResourceHandler resourceHandler;
    private final ArrayList<ImgArea> frames;//所有帧
    private final ArrayList<Integer> framePerTick;//每游戏帧对应动画帧
    private int totalTicks;//总帧数

    public Animation(ResourceHandler resourceHandler) {
        this.resourceHandler = resourceHandler;
        totalTicks = 0;
        frames = new ArrayList<ImgArea>();
        framePerTick = new ArrayList<Integer>();
    }

    //清空
    public Animation clear() {
        totalTicks = 0;
        frames.clear();
        framePerTick.clear();
        return this;
    }

    public Animation addFrame(int lastTicks, ImgArea area) {
        //生成对应的数组标记
        //占用空间++++ 但是我喜欢，因为时间O(1)且没有逻辑运算
        int frameCnt = frames.size();
        for (int p = framePerTick.size(), rend = totalTicks + lastTicks; p < rend; p++) {
            framePerTick.add(frameCnt);
        }
        totalTicks += lastTicks;
        frames.add(area);
        return this;
    }

    public Animation init(int[] lastTicks, ImgArea... areas) {
        //同理
        frames.clear();
        if (areas.length > 0 && lastTicks.length > 0) {
            int totalLength = Math.min(areas.length, lastTicks.length);
            for (int i = 0; i < totalLength; i++) {
                for (int j = totalTicks, rend = totalTicks + lastTicks[i]; j < rend; j++)
                    framePerTick.add(i);
                totalTicks += lastTicks[i];
                frames.add(areas[i]);
            }
        } else System.out.println("wrong args.");
        return this;
    }


    public Animation init(int[] lastTicks, ArrayList<ImgArea> areas) {
        //同理
        frames.clear();
        if (areas.size() > 0 && lastTicks.length > 0) {
            int totalLength = Math.min(areas.size(), lastTicks.length);
            for (int i = 0; i < totalLength; i++) {
                for (int j = totalTicks, rend = totalTicks + lastTicks[i]; j < rend; j++)
                    framePerTick.add(i);
                totalTicks += lastTicks[i];
                frames.add(areas.get(i));
            }
        } else System.out.println("wrong args.");
        return this;
    }

    //获取指定帧
    public ImgArea getFrame(int tick) {
        return frames.get(framePerTick.get(tick));
    }

    //获取指定帧
    public ImgArea getFrameAt(int index) {
        return frames.get(index);
    }

    //总时间帧数
    public int getTotalTicks() {
        return totalTicks;
    }

    //获取资源handler
    public ResourceHandler getResourceHandler() {
        return resourceHandler;
    }
}
 class AnimationItem extends GameItem {

    protected AnimationPlayer animationPlayer;
    protected int cycleCnt, cycleCntMax;
    protected boolean isCycleTimeLimited;

    public AnimationItem(Direction pos, AnimationPlayer animationPlayer) {
        super(pos, animationPlayer.getResourceHandler());
        layer = 3;
        this.animationPlayer = animationPlayer;
        cycleCnt = 0;
        cycleCntMax = 1;
        isCycleTimeLimited = false;
    }

    @Override
    public void init() {
        lastPos = new Direction(pos.x, pos.y);
        isDestroyed = false;
        callEvents(ON_INIT, this, animationPlayer, animationPlayer.animation);
        cycleCnt = 0;
    }

    public boolean isCycleTimeLimited() {
        return isCycleTimeLimited;
    }

    public AnimationItem setCycleTimeLimited(boolean cycleTimeLimited) {
        isCycleTimeLimited = cycleTimeLimited;
        return this;
    }

    public AnimationItem setCycleCnt(int cycleCntMax) {
        this.cycleCntMax = cycleCntMax;
        return this;
    }

    public AnimationItem(int x, int y, AnimationPlayer animationPlayer) {
        super(x, y, null);
        layer = 3;
        this.animationPlayer = animationPlayer;
    }

    @Override
    public void draw(Graphics g) {
        animationPlayer.draw(g, pos.x, pos.y);
    }

    @Override
    public void update(long clock) {
        super.update(clock);
        if (animationPlayer.isOver()) {
            if (isCycleTimeLimited && cycleCnt++ < cycleCntMax) {
                animationPlayer.resetFrame();
                animationPlayer.setPause(false);
            } else
                scene.removeItem(this);
        }
    }

    @Override
    public void hang() {
        super.hang();
        animationPlayer.setPause(true);
    }

    @Override
    public void resume() {
        super.resume();
        animationPlayer.setPause(false);
    }
}
 class AnimationPlayer {
    protected Animation animation;//动画
    private boolean animationPaused;//是否暂停
    private int currentTotalTickCnt;//当前总帧数
    private int currentTick;//当前帧数
    private ResourceHandler resourceHandler;//绘制
    private boolean isCycled;//是否循环

    public AnimationPlayer(Animation animation) {
        setAnimation(animation);
    }

    public ResourceHandler getResourceHandler() {
        return resourceHandler;
    }

    public void init(){
        setAnimation(animation);
    }

    public AnimationPlayer setAnimation(Animation animation) {
        this.animation = animation;
        animationPaused = false;
        currentTotalTickCnt = animation.getTotalTicks();
        currentTick = 0;
        resourceHandler = animation.getResourceHandler();
        isCycled = true;
        return this;
    }

    public boolean isCycled() {
        return isCycled;
    }

    public AnimationPlayer setCycled(boolean cycled) {
        isCycled = cycled;
        return this;
    }

    public AnimationPlayer resetFrame() {
        currentTick = 0;
        return this;
    }

    public AnimationPlayer setPause(boolean paused) {
        animationPaused = paused;
        return this;
    }

    //绘制
    public void draw(Graphics g, int x, int y) {
        ImgArea frame = animation.getFrame(currentTick);
        resourceHandler.draw(g, x, y, frame);
        if (!animationPaused) {
            currentTick++;
            if (isCycled)
                currentTick %= currentTotalTickCnt;
                //考虑非周期动画暂停到最后一帧
            else if (currentTick == currentTotalTickCnt) {
                currentTick--;
                setPause(true);
            }
        }

    }

    //绘制
    public void draw(Graphics g, int x, int y, float zx, float zy) {
        ImgArea frame = animation.getFrame(currentTick);
        resourceHandler.draw(g, x, y, frame, zx, zy);
        if (!animationPaused) {
            currentTick++;
            if (isCycled)
                currentTick %= currentTotalTickCnt;
                //考虑非周期动画暂停到最后一帧
            else if (currentTick == currentTotalTickCnt) {
                currentTick--;
                setPause(true);
            }
        }

    }

    //是否停留到最后一帧
    public boolean isOver() {
        return !isCycled && animationPaused && currentTick == currentTotalTickCnt - 1;
    }
}
 abstract class CollidableGameItem extends GameItem implements ICollider {

    protected AABB aabb;

    public CollidableGameItem(Direction pos, ResourceHandler resourceHandler) {
        super(pos, resourceHandler);
    }

    public CollidableGameItem(int x, int y, ResourceHandler resourceHandler) {
        super(x, y, resourceHandler);
    }

    @Override
    public void onCollision(ICollider item) {
        if (events != null && events.containsKey(CollisionManager.ON_COLLISION))
            for (IGameEvent event : events.get(CollisionManager.ON_COLLISION)) {
                event.run(item, this);
            }
    }

    @Override
    public void bind(CollisionManager c) {

    }

    @Override
    public AABB getAABB() {
        aabb.x = pos.x;
        aabb.y = pos.y;
        return aabb;
    }

    @Override
    public long getColliderID() {
        return ID;
    }

    @Override
    public String getColliderTag() {
        return tag;
    }

    @Override
    public boolean isInitiative() {
        return true;
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public boolean isStatic() {
        return false;
    }

    @Override
    public boolean isRigid() {
        return false;
    }

    @Override
    public HashMap<String, Integer> getFilter() {
        return null;
    }
}
 class CollisionManager extends GameComponentSystem implements IResDraw {

    public static long ON_COLLISION = EventRegister.getEventID();

    private final int w;
    private final int h;//平面大小
    //先后存储静态和动态的物体
    protected ArrayList<ConcurrentHashMap<Long, ICollider>> collisionLayers;//可碰撞物体层
    //private LogicManager logicManager;//逻辑循环管理器
    private DrawManager drawManager;//绘制管理器
    private int layer;//绘制层
    private boolean drawn, isInitDrawManager;
    private final Wall[] walls;
    private final AABB wallContainer;
    private QuadTree quadTree;
    private final ArrayList<ICollider> updateList;
    public static final String filterMode = "filterMode";

    public class Wall implements ICollider {
        public AABB aabb;
        private HashMap<String, Integer> collisionFilter;
        private final long ID;

        public Wall(AABB aabb) {
            this.aabb = aabb;
            ID = GameItem.generateID();
        }

        @Override
        public void onCollision(ICollider item) {
            Rigid.staticBounceBack(item, this, true);
        }

        @Override
        public void bind(CollisionManager c) {
        }

        @Override
        public AABB getAABB() {
            return aabb;
        }

        @Override
        public long getColliderID() {
            return ID;
        }

        @Override
        public String getColliderTag() {
            return "_WALL_";
        }

        @Override
        public boolean isInitiative() {
            return true;
        }

        @Override
        public boolean isActive() {
            return true;
        }

        @Override
        public boolean isStatic() {
            return true;
        }

        @Override
        public boolean isRigid() {
            return false;
        }

        @Override
        public HashMap<String, Integer> getFilter() {
            return collisionFilter;
        }
    }

    //初始化
    public CollisionManager(int w, int h) {
        super();
        this.w = w;
        this.h = h;
        componentID = "collision";
        //初始化必要的数据结构
        collisionLayers = new ArrayList<ConcurrentHashMap<Long, ICollider>>();
        for (int i = 0; i < 2; i++)
            collisionLayers.add(new ConcurrentHashMap<Long, ICollider>());
        layer = 4;
        isInitDrawManager = drawn = false;
        //四面墙壁由四个有实体的墙组成
        //方便统一纳入刚体碰撞模拟
        wallContainer = new AABB(0, 0, w, h);
        walls = new Wall[4];
        walls[0] = new Wall(new AABB(wallContainer.x, wallContainer.y - 20, wallContainer.width, 20));
        walls[1] = new Wall(new AABB(wallContainer.x, wallContainer.y + wallContainer.height, wallContainer.width, 10));
        walls[2] = new Wall(new AABB(wallContainer.x - 10, wallContainer.y, 10, wallContainer.height));
        walls[3] = new Wall(new AABB(wallContainer.x + wallContainer.width, wallContainer.y, 10, wallContainer.height));
        updateList = new ArrayList<ICollider>();
        quadTree = new QuadTree(new AABB(0, 0, w, h), 0);
    }

    //增加物体
    @Override
    public GameComponentSystem addItem(Object o, Object... args) {
        //根据是否静态放置到不同的优先层级
        ICollider collider = (ICollider) o;
        int cLayer = collider.isStatic() ? 0 : 1;
        collisionLayers.get(cLayer).put(collider.getColliderID(), collider);
        collider.bind(this);
        return this;
    }

    //找之
    @Override
    public Object findItem(long id, Object... args) {
        int cLayer = 0;
        if (args.length == 1 && args[0] instanceof Boolean)
            cLayer = (Boolean) args[0] ? 0 : 1;
        return collisionLayers.get(cLayer).get(id);
    }

    public void updateTree() {
        quadTree.clear();
        //循环添加矩形盒子
        for (ConcurrentHashMap<Long, ICollider> collisionItems : collisionLayers)
            for (ICollider collider : collisionItems.values())
                quadTree.insert(collider.getAABB(), collider);
    }

    @Override
    public void update(long clock) {
        updateTree();
        //四叉树
        //遍历所有参与碰撞的对象

        for (ICollider collider : collisionLayers.get(0).values()) {
            //对于静态物体 发生第一轮碰撞处理后只处理第一个响应的结果
            //重新插入运动对象
            //获取与之相交的全部对象
            if (collider.isActive()) {
                updateList.clear();
                quadTree.checkElements(collider, collider.getFilter(), true, updateList);
                for (ICollider updateCollider : updateList)
                    quadTree.insert(updateCollider.getAABB(), updateCollider);
            }
        }
        for (ICollider collider : collisionLayers.get(1).values())
            //如果碰撞对象被设定为主动，将向它发送消息
            if (collider.isInitiative() && collider.isActive()) {
                //获取与之相交的全部对象
                quadTree.checkElements(collider, collider.getFilter(), false, null);
            }

    }

    @Override
    public void doRemoveList() {
        if (toRemove) {
            ICollider item;
            while ((item = (ICollider) removeList.poll()) != null) {
                int cLayer = item.isStatic() ? 0 : 1;
                collisionLayers.get(cLayer).remove(item.getColliderID());
            }
        }
    }

    @Override
    public void clear() {
        super.clear();
        collisionLayers.get(0).clear();
        collisionLayers.get(1).clear();
        collisionLayers.clear();
    }

    @Override
    public void init() {
        for (Wall wall : walls)
            collisionLayers.get(0).put(wall.getColliderID(), wall);
    }

    @Override
    public void bind(LogicManager l) {
        logicManager = l;
        isInitDrawManager = drawn = true;
    }

    @Override
    public long getLogicID() {
        return ID;
    }

    @Override
    public void onDestroy() {
        isDestroyed = true;
    }

    //用于调试的绘制算法
    //显示tag和aabb盒
    @Override
    public void draw(Graphics g) {
        Color c = g.getColor();
        g.setColor(Color.CYAN);
        for (ICollider collider : collisionLayers.get(0).values())
            drawColliders(g, collider);
        g.setColor(Color.GREEN);
        for (ICollider collider : collisionLayers.get(1).values())
            drawColliders(g, collider);
        g.setColor(c);
    }

    private void drawColliders(Graphics g, ICollider collider) {
        AABB aabb = collider.getAABB();
        g.drawRect((int) aabb.x, (int) aabb.y
                , (int) aabb.width, (int) aabb.height);
        g.drawString(collider.getColliderTag() + "(" + aabb.x + ", " + aabb.y + ", " + aabb.width + ", " + aabb.height + ")"
                , (int) aabb.x, (int) aabb.y);
        g.drawString("id@" + collider.getColliderID()
                , (int) aabb.x, (int) aabb.y - 12);
    }

    @Override
    public void bind(DrawManager d) {
        drawManager = d;
        setDrawn(false);
    }

    @Override
    public long getDrawID() {
        return ID;
    }

    @Override
    public void setLayer(int layer) {
        this.layer = layer;
    }

    @Override
    public int getLayer() {
        return 0;
    }

    @Override
    public void setDrawn(boolean isDrawn) {
        if (isInitDrawManager) {
            if (drawn) {
                if (!isDrawn)
                    drawManager.removeItem(this);
            } else {
                if (isDrawn)
                    drawManager.addItem(this);
            }
            drawn = isDrawn;
        }

    }

    @Override
    public boolean isDrawn() {
        return drawn;
    }

    //一个通用的四叉树算法
    private class QuadNode {
        public AABB r;
        public ICollider element;

        QuadNode(AABB r, ICollider element) {
            this.r = r;
            this.element = element;
        }

        @Override
        public String toString() {
            return r.toString();
        }
    }

    class QuadTree {

        //当前节点
        public ArrayList<QuadNode> nodes;

        //当前碰撞盒
        private final AABB zone;

        public final int maxItemByNode = 20;
        public final int maxLevel = 10;

        int level;

        //四个子象限
        QuadTree[] regions;

        public final int REGION_SELF = -1;
        public final int REGION_NW = 0;
        public final int REGION_NE = 1;
        public final int REGION_SW = 2;
        public final int REGION_SE = 3;

        public QuadTree(AABB definition, int level) {
            zone = definition;
            nodes = new ArrayList<QuadNode>();
            this.level = level;
        }

        protected AABB getZone() {
            return this.zone;
        }

        protected void clear() {
            nodes.clear();
            if (regions != null) {
                for (QuadTree region : regions)
                    region.clear();
                regions = null;
            }
        }

        private int findRegion(AABB r, boolean split) {
            int region = REGION_SELF;
            if (nodes.size() >= maxItemByNode && this.level < maxLevel) {
                //当前需要分割
                if (regions == null && split) {
                    this.split();
                }
                // can be null if not split
                if (regions != null) {
                    if (regions[REGION_NW].getZone().contains(r)) {
                        region = REGION_NW;
                    } else if (regions[REGION_NE].getZone().contains(r)) {
                        region = REGION_NE;
                    } else if (regions[REGION_SW].getZone().contains(r)) {
                        region = REGION_SW;
                    } else if (regions[REGION_SE].getZone().contains(r)) {
                        region = REGION_SE;
                    }
                }
            }

            return region;
        }

        private void split() {

            regions = new QuadTree[4];

            float newWidth = zone.width / 2;
            float newHeight = zone.height / 2;
            int newLevel = level + 1;

            regions[REGION_NW] = new QuadTree(new AABB(
                    zone.x,
                    zone.y + zone.height / 2,
                    newWidth,
                    newHeight
            ), newLevel);

            regions[REGION_NE] = new QuadTree(new AABB(
                    zone.x + zone.width / 2,
                    zone.y + zone.height / 2,
                    newWidth,
                    newHeight
            ), newLevel);

            regions[REGION_SW] = new QuadTree(new AABB(
                    zone.x,
                    zone.y,
                    newWidth,
                    newHeight
            ), newLevel);

            regions[REGION_SE] = new QuadTree(new AABB(
                    zone.x + zone.width / 2,
                    zone.y,
                    newWidth,
                    newHeight
            ), newLevel);
        }

        public void insert(AABB r, ICollider element) {
            int region = this.findRegion(r, true);
            if (region == REGION_SELF || this.level == maxLevel) {
                nodes.add(new QuadNode(r, element));
                return;
            } else {
                regions[region].insert(r, element);
            }

            if (nodes.size() >= maxItemByNode && this.level < maxLevel) {
                // 再次分配
                ArrayList<QuadNode> tempNodes = new ArrayList<QuadNode>(nodes);
                nodes.clear();
                // 递归地插入
                for (QuadNode node : tempNodes) {
                    this.insert(node.r, node.element);
                }
            }
        }

        public void checkElements(ICollider collider, HashMap<String, Integer> filter, boolean rCheck, ArrayList<ICollider> updateList) {
            AABB r = collider.getAABB();
            int region = this.findRegion(r, false);
            if (filter == null) {
                simpleCheckCollide(collider, r, rCheck, updateList);
                if (region != REGION_SELF) {
                    regions[region].checkElements(collider, null, rCheck, updateList);
                } else {
                    checkAllElements(collider, true, null, rCheck, updateList);
                }
            } else {
                rCheckCollide(collider, filter, r, rCheck, updateList);
                if (region != REGION_SELF) {
                    regions[region].checkElements(collider, filter, rCheck, updateList);
                } else {
                    checkAllElements(collider, true, filter, rCheck, updateList);
                }
            }
        }

        public void checkAllElements(ICollider collider, boolean firstCall, HashMap<String, Integer> filter, boolean rCheck, ArrayList<ICollider> updateList) {
            AABB r = collider.getAABB();
            if (regions != null) {
                regions[REGION_NW].checkAllElements(collider, false, filter, rCheck, updateList);
                regions[REGION_NE].checkAllElements(collider, false, filter, rCheck, updateList);
                regions[REGION_SW].checkAllElements(collider, false, filter, rCheck, updateList);
                regions[REGION_SE].checkAllElements(collider, false, filter, rCheck, updateList);
            }

            if (!firstCall) {
                if (filter == null) {
                    simpleCheckCollide(collider, r, rCheck, updateList);
                } else {
                    rCheckCollide(collider, filter, r, rCheck, updateList);
                }
            }
        }

        private void rCheckCollide(ICollider collider, HashMap<String, Integer> filter, AABB r, boolean rCheck, ArrayList<ICollider> updateList) {
            Integer fMode = filter.get(filterMode);
            fMode = fMode == null ? 0 : fMode;
            for (int i = nodes.size() - 1; i >= 0; i--) {
                //同理
                QuadNode node = nodes.get(i);
                ICollider tag = node.element;
                Integer filterVal = filter.get(tag.getColliderTag());
                //考虑两种过滤模式，肯定和否定形
                if (node.element.isActive() && testCollision(collider, r, fMode, node.r, tag, filterVal) && rCheck) {
                    updateList.add(node.element);
                    nodes.remove(i);
                }
            }
        }

        private void simpleCheckCollide(ICollider collider, AABB r, boolean rCheck, ArrayList<ICollider> updateList) {
            for (int i = nodes.size() - 1; i >= 0; i--) {
                //同理
                QuadNode node = nodes.get(i);
                if (node.element.isActive() && !node.r.equals(r) && node.r.overlaps(r)) {
                    collider.onCollision(node.element);
                    if (rCheck) {
                        updateList.add(node.element);
                        nodes.remove(i);
                    }
                }
            }
        }

        private boolean testCollision(ICollider collider, AABB r, Integer fMode, AABB node, ICollider tag, Integer filterVal) {
            switch (fMode) {
                case 0:
                    if (filterVal != null && filterVal == 1 && !node.equals(r) && node.overlaps(r))
                        collider.onCollision(tag);
                    return true;
                case 1:
                    if ((filterVal == null || filterVal != 1) && (!node.equals(r) && node.overlaps(r)))
                        collider.onCollision(tag);
                    return true;
            }
            return false;
        }
    }

        /*public QuadTree getAllElement(boolean firstCall, ICollider collider) {
            if (regions != null) {
                regions[REGION_NW].getAllElement(false, collider);
                regions[REGION_NE].getAllElement(false, collider);
                regions[REGION_SW].getAllElement(false, collider);
                regions[REGION_SE].getAllElement(false, collider);
            }
            if (!firstCall) {
                int length = nodes.size();
                for (QuadNode node : nodes) {
                    //同理
                    if (node.element == collider)
                        return this;
                }
            }
            return null;
        }

        public ArrayList<ICollider> getElements(ArrayList<ICollider> list, AABB r, HashMap<String, Integer> filter) {
            int region = this.findRegion(r, false);
            int length = nodes.size();
            if (filter == null) {
                for (QuadNode node : nodes) {
                    //只有相交的时候添加
                    //排除掉自身的情况
                    ICollider tag = (ICollider) (node.element);
                    if (!node.r.equals(r) && node.r.overlaps(r))
                        list.add(node.element);
                }
                if (region != REGION_SELF) {
                    regions[region].getElements(list, r, null);
                } else {
                    getAllElements(list, true, r, null);
                }
            } else {
                for (QuadNode node : nodes) {
                    //只有相交的时候添加
                    //排除掉自身的情况
                    ICollider tag = (ICollider) (node.element);
                    if (!node.r.equals(r) && node.r.overlaps(r) && filter.get(tag.getColliderTag()) != null)
                        list.add(node.element);
                }
                if (region != REGION_SELF) {
                    regions[region].getElements(list, r, filter);
                } else {
                    getAllElements(list, true, r, filter);
                }
            }
            return list;
        }

        public ArrayList<ICollider> getAllElements(ArrayList<ICollider> list, boolean firstCall, AABB r, HashMap<String, Integer> filter) {
            if (regions != null) {
                regions[REGION_NW].getAllElements(list, false, r, filter);
                regions[REGION_NE].getAllElements(list, false, r, filter);
                regions[REGION_SW].getAllElements(list, false, r, filter);
                regions[REGION_SE].getAllElements(list, false, r, filter);
            }

            if (!firstCall) {
                int length = nodes.size();
                if (filter == null) {
                    for (QuadNode node : nodes) {
                        //同理
                        if (!node.r.equals(r) && node.r.overlaps(r))
                            list.add(node.element);
                    }
                } else {
                    for (QuadNode node : nodes) {
                        //同理
                        ICollider tag = (ICollider) (node.element);
                        if (!node.r.equals(r) && node.r.overlaps(r) && filter.get(tag.getColliderTag()) != null)
                            list.add(node.element);
                    }
                }
            }
            return list;
        }*/


}
 interface ICollider {
    void onCollision(ICollider item);
    void bind(CollisionManager c);
    AABB getAABB();
    long getColliderID();
    String getColliderTag();
    boolean isInitiative();
    boolean isActive();
    boolean isStatic();
    boolean isRigid();
    HashMap<String, Integer> getFilter();
}
 interface IInput {
    void keyPressed(KeyEvent key);
    long getInputID();
    void bind(InputManager i);
}
 class InputManager extends GameComponentSystem {

    protected ConcurrentHashMap<Long, IInput> inputListeners;
    private final Queue<KeyEvent> keyEvents;
    private final JFrame gameFrame;
    private final KeyMonitor keyMonitor;

    public InputManager(JFrame gameFrame) {
        inputListeners = new ConcurrentHashMap<Long, IInput>();
        keyEvents = new ConcurrentLinkedQueue<KeyEvent>();
        this.gameFrame = gameFrame;
        keyMonitor = new KeyMonitor();
        this.gameFrame.addKeyListener(keyMonitor);
    }

    @Override
    public void update(long clock) {
        super.update(clock);
        KeyEvent eKey;
        while ((eKey = keyEvents.poll()) != null)
            for (IInput listener : inputListeners.values()) {
                listener.keyPressed(eKey);
            }
    }

    @Override
    public GameComponentSystem addItem(Object o, Object... args) {
        IInput listener = (IInput) o;
        inputListeners.put(listener.getInputID(), listener);
        listener.bind(this);
        return this;
    }

    @Override
    public Object findItem(long id, Object... args) {
        return inputListeners.get(id);
    }

    @Override
    public void doRemoveList() {
        if (toRemove) {
            IInput item;
            while ((item = (IInput) removeList.poll()) != null)
                inputListeners.remove(item.getInputID());
        }
    }

    class KeyMonitor extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            keyEvents.offer(e);
        }
    }

    @Override
    public String getComponentID() {
        return "input";
    }

    //最先响应
    @Override
    public int priority() {
        return 0;
    }
}
 class PeriodicTimer {
    private final HashMap<String, IGameEvent> events;//事件
    private final HashMap<String, Long> currentClocks;//每个事件的时钟
    private long minInterval, timerClock;//最小更新间隔与计时器时钟
    private final Object parent;//父对象

    //更新最小更新间隔
    private void updateMinInterval(long period) {
        if (period < minInterval || minInterval == 0L)
            minInterval = period;
    }

    //初始化
    public PeriodicTimer(Object... args) {
        currentClocks = new HashMap<String, Long>();
        events = new HashMap<String, IGameEvent>();
        if (args.length > 0)
            parent = args[0];
        else
            parent = null;
        if (args.length > 1)
            minInterval = (long) args[1];
        else
            minInterval = 0L;
    }

    //重置事件时钟
    public void resetEventClock(String id, long clock) {
        if (currentClocks.containsKey(id))
            currentClocks.put(id, clock);
    }

    //更新
    public void update(long clock) {
        if (clock - timerClock >= minInterval) {
            timerClock = clock;
            for (String key : events.keySet()) {
                IGameEvent event;
                long period;
                if (clock - currentClocks.get(key) >= (period = (long) (event = events.get(key)).getInfo())) {
                    currentClocks.put(key, clock);
                    updateMinInterval(period);
                    event.run(parent);
                }
            }
        }
    }

    //添加事件
    public PeriodicTimer addEvent(String id, IGameEvent event) {
        events.put(id, event);
        currentClocks.put(id, 0L);
        updateMinInterval((long) event.getInfo());
        return this;
    }

    //移除事件
    public PeriodicTimer removeEvent(String id) {
        events.remove(id);
        currentClocks.remove(id);
        minInterval = 0L;
        return this;
    }

}
 class AliveMonitor extends GameComponentSystem {

    private final ConcurrentHashMap<Long, IUpdate> hangOnUpdates;
    private final LogicManager logicManager;

    public AliveMonitor() {
        hangOnUpdates = new ConcurrentHashMap<Long, IUpdate>();
        logicManager = SingletonManager.getLogicManager();
        componentID = "aliveMonitor";
    }

    @Override
    public void init() {
        super.init();
        // 仅通过findComponent被其它组件调用
        addItem(this);
    }

    public AliveMonitor hangOnAllItems() {
        for (IUpdate update : logicManager.updateItems.values())
            if (update instanceof GameItem)
                addItem(update);
        return this;
    }

    public AliveMonitor resumeAllItems() {
        for (IUpdate update : hangOnUpdates.values())
            if (update instanceof GameItem)
                removeItem(update);
        return this;
    }

    public AliveMonitor resumeAll() {
        for (IUpdate update : hangOnUpdates.values())
            removeItem(update);
        return this;
    }

    public void clear() {
        hangOnUpdates.clear();
    }

    @Override
    public GameComponentSystem addItem(Object o, Object... args) {
        if (o instanceof IUpdate) {
            hangOnUpdates.put(((IUpdate) o).getLogicID(), (IUpdate) o);
            logicManager.removeItem((IUpdate) o, false);
            return this;
        }
        return null;
    }

    @Override
    public GameComponentSystem removeItem(Object o) {
        if (o instanceof IUpdate) {
            hangOnUpdates.remove(((IUpdate) o).getLogicID());
            logicManager.addItem((IUpdate) o, false);
            return this;
        }
        return null;
    }

    @Override
    public int priority() {
        return 0;
    }
}
 class DrawManager {

    protected ArrayList<HashMap<Long, IResDraw>> drawLayers;//管理所有的可绘制物体

    private Color bgColor;
    private Image offScreenImage;//缓冲图像
    private Graphics gOffScreen;//缓冲graphics……句柄(不知道Java怎么称呼)?
    private GameView gameView;//游戏窗体
    private int viewPortW;
    private int viewPortH;//视觉实际宽和高
    private int width;
    private int height;
    private float zoomX;
    private float zoomY;
    public final static int maxLayerCount = 5;
    private long clock, delta;
    private final long delay = 8 * 1000000;
    private Lock readLock;//读锁

    public int getW() {
        return viewPortW;
    }

    public int getH() {
        return viewPortH;
    }

    public int zoomX(int x) {
        return (int) (x * zoomX);
    }

    public int zoomY(int y) {
        return (int) (y * zoomY);
    }

    public float getZoomX() {
        return zoomX;
    }

    public float getZoomY() {
        return zoomY;
    }

    //初始化
    protected DrawManager(GameView gameView, Lock readLock) {
        //初始化渲染层
        /*drawLayers = new ArrayList<ConcurrentHashMap<Long, IResDraw>>();
        for (int i = 0; i < maxLayerCount; i++)
            drawLayers.add(new ConcurrentHashMap<Long, IResDraw>());*/
        //双缓冲及窗口设置
        init(gameView, readLock);
    }

    protected DrawManager(){

    }

    public void init(GameView gameView, Lock readLock){
        offScreenImage = null;
        gOffScreen = null;
        this.gameView = gameView;
        this.readLock = readLock;
        viewPortW = gameView.getWidth();
        viewPortH = gameView.getHeight();
    }

    public void setBackgroundColor(Color c) {
        bgColor = c;
    }

    public void init(int width, int height) {
        //排除一哈空场景
        if(width == 0 || height == 0)
            return;
        this.width = width;
        this.height = height;
        viewPortW = gameView.getWidth();
        viewPortH = gameView.getHeight();
        zoomX = (float) viewPortW / width;
        zoomY = (float) viewPortH / height;
        //稍微减少一哈耗用
        if(offScreenImage == null || offScreenImage.getWidth(null) != width || offScreenImage.getHeight(null) != height) {
            offScreenImage = gameView.createImage(width, height);
            gOffScreen = offScreenImage.getGraphics();
        }
        gameView.repaint();
    }

    //添加绘制物体
    public DrawManager addItem(IResDraw drawItem, int layer) {
        drawItem.setLayer(layer);//设置层数
        HashMap<Long, IResDraw> drawLayer = drawLayers.get(layer);//获得当前层
        drawLayer.put(drawItem.getDrawID(), drawItem);//添加
        drawItem.bind(this);//绑定
        return this;
    }

    public DrawManager addItem(IResDraw drawItem) {
        HashMap<Long, IResDraw> drawLayer = drawLayers.get(drawItem.getLayer());//获得当前层
        drawLayer.put(drawItem.getDrawID(), drawItem);//添加
        drawItem.bind(this);//绑定
        return this;
    }

    //移除渲染队列
    public DrawManager removeItem(IResDraw drawItem) {
        drawLayers.get(drawItem.getLayer()).remove(drawItem.getDrawID());
        return this;
    }

    //全部清除
    public void clear() {
        /*for (ConcurrentHashMap<Long, IResDraw> layer : drawLayers)
            layer.clear();
        drawLayers.clear();*/
    }

    //获取
    public IResDraw findItem(int layer, long id) {
        return drawLayers.get(layer).get(id);
    }

    //绘制图像
    public void draw(Graphics g) {
        //独立获取时钟
        //尝试同loop的update同步 减去makeUp
        clock = System.nanoTime();
        //读锁
        readLock.lock();
        //初始化
        g.setColor(bgColor);
        //清屏重绘
        gOffScreen.fillRect(0, 0, width, height);
        for (HashMap<Long, IResDraw> layer : drawLayers)
            for (IResDraw items : layer.values())
                items.draw(gOffScreen);
        g.drawImage(offScreenImage, 0, 0, viewPortW, viewPortH, 0, 0, width, height, null);
        readLock.unlock();
        delta = delay - (System.nanoTime() - clock);
        try {
            if (delta < 0)
                delta = 0;
            Thread.sleep(delta / 1000000);//同上
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        while ((System.nanoTime() - clock) < delay) {
            // 使用循环，精确控制每帧绘制时长
        }

        gameView.repaint();
    }

    public void setDrawLayers(ArrayList<HashMap<Long, IResDraw>> drawLayers) {
        this.drawLayers = drawLayers;
    }
}
 class EventRegister {
    private final HashMap<String, IGameEvent> events;
    private static long eventsCounter = 0;

    public static long getEventID() {
        return eventsCounter++;
    }

    protected EventRegister() {
        events = new HashMap<>();
    }

    public void register(String eventType, IGameEvent event) {
        events.put(eventType, event);
    }

    public void unregister(String eventType) {
        events.remove(eventType);
    }

    public void unregisterAll() {
        events.clear();
    }

    public IGameEvent getEvent(String eventType) {
        return events.get(eventType);
    }
}
 abstract class GameComponentSystem implements IUpdate {

    protected boolean isDestroyed;
    protected LogicManager logicManager;
    protected long ID;
    protected String componentID;
    protected boolean toRemove;
    protected Queue<Object> removeList;

    public GameComponentSystem() {
        ID = GameItem.generateID();
        isDestroyed = false;
        toRemove = false;
        removeList = new ConcurrentLinkedQueue<Object>();
    }

    public String getComponentID() {
        return componentID;
    }

    public GameComponentSystem addItem(Object o, Object... args) {
        return this;
    }

    public Object findItem(long id, Object... args) {
        return null;
    }

    //移除物体
    public GameComponentSystem removeItem(Object o) {
        toRemove = true;
        removeList.offer(o);
        return this;
    }

    public void clear(){
        toRemove = false;
        removeList.clear();
    }

    public void doRemoveList() {
    }

    public int priority(){
        return 1;
    }

    @Override
    public void update(long clock) {

    }

    @Override
    public void init() {

    }

    @Override
    public void bind(LogicManager l) {

    }

    @Override
    public long getLogicID() {
        return ID;
    }

    @Override
    public void onDestroy() {
        isDestroyed = true;
    }

    @Override
    public boolean isDestroyed() {
        return isDestroyed;
    }

    @Override
    public void hang() {

    }

    @Override
    public void resume() {

    }
}
 class GameDebug implements IInput, IResDraw {

    public Scene scene;
    private final CollisionManager collisionManager;
    private DrawManager drawManager;
    private InputManager inputManager;
    private boolean isDrawn;

    public GameDebug(Scene scene) {
        this.scene = scene;
        collisionManager = (CollisionManager) scene.findComponent("collision");
    }

    @Override
    public void keyPressed(KeyEvent key) {
        switch (key.getKeyCode()) {
            case KeyEvent.VK_D:
                isDrawn = !isDrawn;
                break;
            case KeyEvent.VK_B:
                //if (isDrawn)
                collisionManager.setDrawn(!collisionManager.isDrawn());
                break;
            case KeyEvent.VK_P:
                scene.logicManager.setActiveScene("1", true, true);
                break;
        }
    }

    @Override
    public long getInputID() {
        return -1;
    }

    @Override
    public void bind(InputManager i) {
        inputManager = i;
    }

    @Override
    public void draw(Graphics g) {
        if (isDrawn) {
            Color c = g.getColor();
            g.setColor(Color.pink);
            g.drawString("Debug mode", 15, 70);
            g.setColor(Color.LIGHT_GRAY);
            int y = 0, i = 0;
            for (i = 0; i < 2; i++)
                for (IUpdate uc : scene.logicManager.updateComponents[i].values())
                    g.drawString("running " + ((GameComponentSystem) (uc)).getComponentID(), 15, y = y + (i + 1) * 10);
            i = 1;
            int dy = (y += 20);
            g.setColor(Color.YELLOW);
            for (IUpdate u : scene.logicManager.updateItems.values())
                g.drawString("looping for " + ((u instanceof GameItem) ? ((GameItem) u).getTag() : "_LOGIC") + "@" + u.getLogicID(), 15, y = y + i * 15);
            g.setColor(Color.RED);
            i = 1;
            for (HashMap<Long, IResDraw> layer : scene.drawManager.drawLayers)
                for (IResDraw d : layer.values()) {
                    g.drawString("drawing for " + ((d instanceof GameItem) ? ((GameItem) d).getTag() : "_DRAWN") + "@" + d.getDrawID(), 15 + scene.getWidth() - 160, dy = dy + i * 15);
                }
            g.setColor(c);
        }
    }

    @Override
    public void bind(DrawManager d) {
        System.out.println("debug.");
        drawManager = d;
    }

    @Override
    public long getDrawID() {
        return -1;
    }

    @Override
    public void setLayer(int layer) {

    }

    @Override
    public int getLayer() {
        return 4;
    }

    @Override
    public void setDrawn(boolean isDrawn) {
        this.isDrawn = isDrawn;
    }

    @Override
    public boolean isDrawn() {
        return isDrawn;
    }
}
 class GameFrame extends JFrame {

    protected GameView gameView, emptyView;

    public GameFrame(String str, int viewPortW, int viewPortH) {
        super(str);
        setSize(viewPortW, viewPortH);//设置窗口大小
        setDefaultCloseOperation(EXIT_ON_CLOSE);//默认关闭
        setBackground(Color.BLACK);//设置背景色
        //setUndecorated(true);//去掉窗口边框
        //setResizable(false);//禁止缩放窗口
        setVisible(true);//窗口可见
    }


    public void setGameView(GameView gameView) {
        this.gameView = gameView;
        gameView.gameFrame = this;
        setContentPane(gameView);
        //强制更新组件
        revalidate();
    }

}
 abstract class GameItem implements IResDraw, IUpdate {

    public static long ON_INIT = EventRegister.getEventID(), ON_DESTROY = EventRegister.getEventID();

    protected HashMap<Long, ArrayList<IGameEvent>> events;
    protected Direction pos;//位置
    protected Direction lastPos;//下一帧的位置
    protected ResourceHandler resourceHandler;//资源持有管理器
    protected ImgArea imgArea;
    protected DrawManager drawManager;//绘制管理器
    protected LogicManager logicManager;
    protected Scene scene;
    protected boolean isInitDrawManager;
    protected final long ID;
    protected String poolName;
    protected int layer;
    protected boolean drawn;//是否加入绘制序列

    protected boolean isDestroyed;
    private static long idCounter = 0;

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    protected String tag;

    public static long generateID() {
        return idCounter++;
    }

    public GameItem(Direction pos, ResourceHandler resourceHandler) {
        this.pos = pos;
        this.resourceHandler = resourceHandler;
        ID = generateID();
        isDestroyed = false;
        tag = "";
    }

    public GameItem(int x, int y, ResourceHandler resourceHandler) {
        pos = new Direction(x, y);
        this.resourceHandler = resourceHandler;
        ID = generateID();
        isDestroyed = false;
        tag = "";
    }

    public long getID() {
        return ID;
    }

    /*public void destroy() {
        isDestroyed = true;
    }

    public boolean isDestroyed() {
        return isDestroyed;
    }*/

    public Direction getLastPos() {
        return lastPos;
    }

    public Direction getPos() {
        return pos;
    }

    public GameItem setPos(Direction pos) {
        this.pos.set(pos);
        return this;
    }


    public Direction getDirection() {
        return Direction.sub(pos, lastPos);
    }

    public void bind(Scene scene) {
        this.scene = scene;
    }

    public GameItem addEvent(long eventType, IGameEvent event) {
        if (events == null)
            events = new HashMap<Long, ArrayList<IGameEvent>>();
        if (!events.containsKey(eventType))
            events.put(eventType, new ArrayList<IGameEvent>());
        events.get(eventType).add(event);
        return this;
    }

    public GameItem removeEvent(long eventType, IGameEvent event) throws NullPointerException {
        if (events == null)
            throw new NullPointerException("events is null");
        if (!events.containsKey(eventType))
            throw new NullPointerException("eventType does not exist");
        events.get(eventType).remove(event);
        return this;
    }

    public GameItem clearEvent(long eventType) {
        if (events == null)
            return this;
        if (!events.containsKey(eventType))
            return this;
        events.get(eventType).clear();
        return this;
    }

    public GameItem destroy() {
        isDestroyed = true;
        scene.removeItem(this);
        return this;
    }

    @Override
    public void draw(Graphics g) {
        resourceHandler.draw(g, pos.x, pos.y, imgArea);
    }

    @Override
    public void bind(DrawManager d) {
        drawManager = d;
        isInitDrawManager = drawn = true;
    }

    @Override
    public long getDrawID() {
        return ID;
    }

    @Override
    public void setLayer(int layer) {
        this.layer = layer;
    }

    @Override
    public int getLayer() {
        return layer;
    }

    @Override
    public void setDrawn(boolean isDrawn) {
        if (isInitDrawManager)
            if (drawn != isDrawn) {
                if (!drawn)
                    drawManager.addItem(this);
                else
                    drawManager.removeItem(this);
                drawn = isDrawn;
            }
    }

    @Override
    public boolean isDrawn() {
        return drawn;
    }

    @Override
    public void update(long clock) {
        //lastPos.set(pos);
        lastPos.x = pos.x;
        lastPos.y = pos.y;
    }

    @Override
    public void init() {
        lastPos = new Direction(pos.x, pos.y);
        isDestroyed = false;
        callEvents(ON_INIT, this);
    }

    protected void callEvents(long eventType, Object... args) {
        if (events != null && events.containsKey(eventType)) {
            Iterator<IGameEvent> iterator = events.get(eventType).iterator();
            while (iterator.hasNext()) {
                IGameEvent event = iterator.next();
                event.run(args);
                if ((boolean) event.getInfo())
                    iterator.remove();
            }
        }
    }

    @Override
    public void bind(LogicManager l) {
        logicManager = l;
    }

    @Override
    public long getLogicID() {
        return ID;
    }

    @Override
    public void onDestroy() {
        isDestroyed = true;
        callEvents(ON_DESTROY, this);
    }

    @Override
    public boolean isDestroyed() {
        return isDestroyed;
    }

    @Override
    public void hang() {

    }

    @Override
    public void resume() {

    }

    @Override
    public String toString() {
        return "GameItem tag:" + tag;
    }

    public String getPoolName() {
        return poolName;
    }

    public GameItem setPoolName(String poolName) {
        this.poolName = poolName;
        return this;
    }

}
 class GameView extends JPanel {

    protected ResourceHandler resourceHandler;//资源管理器
    protected SceneManager sceneManager;//场景管理器
    protected GameFrame gameFrame;

    public GameView(int viewPortW, int viewPortH, ResourceHandler resourceHandler) {
        super();
        setPreferredSize(new Dimension(viewPortW, viewPortH));//设置窗口大小

        this.resourceHandler = resourceHandler;
        sceneManager = SingletonManager.getSceneManager();
        sceneManager.init(this);
        //缩放窗口事件
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                sceneManager.adjustViewPort();
            }
        });
        //setResizable(false);//禁止缩放窗口
    }

    public GameFrame getGameFrame() {
        return gameFrame;
    }

    @Override
    public void paintComponent(Graphics g) {
        //super.paint(g);
        //开始绘制
        sceneManager.drawScene(g);
    }
}
 interface IGameEvent {
    //返回值标记是否阅后即焚
    Object getInfo(Object... args);
    void run(Object... args);
}
 interface IResDraw {
    void draw(Graphics g);
    void bind(DrawManager d);
    long getDrawID();
    void setLayer(int layer);
    int getLayer();
    void setDrawn(boolean isDrawn);
    boolean isDrawn();
}
 interface IUpdate {
    void update(long clock);
    void init();
    void hang();
    void resume();
    void bind(LogicManager l);
    long getLogicID();
    void onDestroy();
    boolean isDestroyed();
}
 class LogicManager {
    private GameView gameView;
    private SceneManager sceneManager;
    protected ConcurrentHashMap<Long, IUpdate> updateItems;//更新物体
    //前后优先级
    protected ConcurrentHashMap<Long, IUpdate>[] updateComponents;//更新组件
    private Lock writeLock;//写锁

    protected LogicManager(GameView gameView, Lock writeLock, SceneManager sceneManager) {
        init(gameView, writeLock, sceneManager);
    }

    protected LogicManager() {

    }

    public void init(GameView gameView, Lock writeLock, SceneManager sceneManager) {
        this.gameView = gameView;
        this.sceneManager = sceneManager;
        this.writeLock = writeLock;
    }

    public void setActiveScene(String id, boolean clear, boolean init) {
        sceneManager.setActiveScene(id, clear, init);
    }

    public LogicManager addItem(IUpdate updateItem, Object... args) {
        if (updateItem instanceof GameComponentSystem)
            updateComponents[((GameComponentSystem) updateItem).priority()].put(updateItem.getLogicID(), updateItem);//添加
        else
            updateItems.put(updateItem.getLogicID(), updateItem);//添加
        updateItem.bind(this);//绑定
        if (args.length == 0)
            updateItem.init();
        else if (args[0] instanceof Boolean && !(Boolean) args[0])
            updateItem.resume();
        return this;
    }

    public LogicManager removeItem(IUpdate updateItem, Object... args) {
        ConcurrentHashMap<Long, IUpdate> table = (updateItem instanceof GameComponentSystem) ? updateComponents[((GameComponentSystem) updateItem).priority()] : updateItems;
        if (table.get(updateItem.getLogicID()) != null) {
            table.remove(updateItem.getLogicID());
            if (args.length == 0)
                updateItem.onDestroy();
            else if (args[0] instanceof Boolean && !(Boolean) args[0])
                updateItem.hang();
        }
        return this;
    }

    //全部清除
    //已废弃 此部分交由场景交换时处理
    public void clear() {
        /*updateItems.clear();
        updateComponents[0].clear();
        updateComponents[1].clear();*/
    }

    //获取
    public IUpdate findUpdateItem(long id) {
        IUpdate res = updateComponents[0].get(id);
        if (res == null)
            res = updateComponents[1].get(id);
        return res;
    }

    void update(long clock) {
        //分别执行
        for (IUpdate updateComponent : updateComponents[0].values()) {
            //if (!updateComponent.isDestroyed())
            writeLock.lock();
            updateComponent.update(clock);
            writeLock.unlock();
        }
        writeLock.lock();
        for (IUpdate updateItem : updateItems.values())
            //if (!updateItem.isDestroyed())
            updateItem.update(clock);
        for (IUpdate updateComponent : updateComponents[1].values()) {
            //if (!updateComponent.isDestroyed())
            updateComponent.update(clock);
        }
        writeLock.unlock();
        for (ConcurrentHashMap updates : updateComponents)
            for (Object updateComponent : updates.values())
                ((GameComponentSystem) updateComponent).doRemoveList();
        /*if (sceneSwitch) {
            sceneSwitch = false;
            sceneManager.setActiveScene((String) switchArgs[0], (Boolean) switchArgs[1], (Boolean) switchArgs[2]);
        }*/
    }

    public void setUpdateItems(ConcurrentHashMap<Long, IUpdate> updateItems) {
        this.updateItems = updateItems;
    }

    public void setUpdateComponents(ConcurrentHashMap<Long, IUpdate>[] updateComponents) {
        this.updateComponents = updateComponents;
    }
}
 class ImgArea {
    public int x, y, dx, dy, sx, sy;

    public ImgArea(int x, int y, int dx, int dy, int sx, int sy) {
        this.x = x;
        this.y = y;
        this.dx = dx;
        this.dy = dy;
        this.sx = sx;
        this.sy = sy;
    }

    public ImgArea() {
    }


    public void set(int x, int y, int sx, int sy) {
        this.x = x;
        this.y = y;
        this.sx = sx;
        this.sy = sy;
    }

    public void set(int x, int y, int dx, int dy, int sx, int sy) {
        this.x = x;
        this.y = y;
        this.dx = dx;
        this.dy = dy;
        this.sx = sx;
        this.sy = sy;
    }
}
 interface IPoolCreate {
    GameItem createItem();
}
 class ResourceHandler {
    protected Image res;//游戏主贴图
    protected HashMap<String, Object> staticResources;
    //对象池部分
    protected HashMap<String, ArrayList<GameItem>> itemPool;
    protected HashMap<String, IPoolCreate> poolCreator;

    //初始化对象池
    public void initPoolByName(String name, IPoolCreate create, int capacity) {
        if (itemPool.containsKey(name)) {
            return;
        }
        ArrayList<GameItem> list = new ArrayList<GameItem>(capacity);
        //初始化池子
        for (int i = 0; i < capacity; i++) {
            list.add(create.createItem());
        }
        itemPool.put(name, list);
        poolCreator.put(name, create);
    }

    //清空池子
    public void clearPoolByName(String name) {
        if (itemPool.containsKey(name)) {
            itemPool.remove(name);
            poolCreator.remove(name);
        }
    }

    //清空
    public void clearAllPool() {
        itemPool.clear();
        poolCreator.clear();
    }

    //根据tag获取对象
    public GameItem getItemByName(String name) {
        if (itemPool.containsKey(name)) {
            ArrayList<GameItem> list = itemPool.get(name);
            if (list.size() > 0)
                return list.remove(0);
            return poolCreator.get(name).createItem();
        }
        return null;
    }

    //归还
    public void releaseItem(String name, GameItem item) {
        if (itemPool.containsKey(name)) {
            ArrayList<GameItem> list = itemPool.get(name);
            list.add(item);
        }
    }

    //初始化
    public ResourceHandler(String path) {
        staticResources = new HashMap<String, Object>();
        itemPool = new HashMap<String, ArrayList<GameItem>>();
        poolCreator = new HashMap<String, IPoolCreate>();
    }

    //指定并绘制指定区域
    public void draw(Graphics g, int x, int y, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2) {
        g.drawImage(res, x, y, dx2, dy2, sx1, sy1, sx2, sy2, null);
    }

    public void draw(Graphics g, int x, int y, ImgArea imgArea) {
        g.drawImage(res, x, y, x + imgArea.dx, y + imgArea.dy, imgArea.x, imgArea.y, imgArea.sx, imgArea.sy, null);
    }

    public void draw(Graphics g, int x, int y, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, float zx, float zy) {
        g.drawImage(res, (int) (x * zx), (int) (y * zy), (int) (dx2 * zx), (int) (dy2 * zy), sx1, sy1, sx2, sy2, null);
    }

    public void draw(Graphics g, int x, int y, ImgArea imgArea, float zx, float zy) {
        g.drawImage(res, (int) (x * zx), (int) (y * zy), (int) ((x + imgArea.dx) * zx), (int) ((y + imgArea.dy) * zy), imgArea.x, imgArea.y, imgArea.sx, imgArea.sy, null);
    }

    public Object getResource(String id) {
        return staticResources.get(id);
    }
}
 class Scene {

    //逻辑宽高
    protected int width;
    protected int height;
    public DrawManager drawManager;
    public LogicManager logicManager;
    protected AliveMonitor aliveMonitor;
    //老生常谈
    //管理场景物体
    protected ConcurrentHashMap<Long, GameComponentSystem> components;
    protected ArrayList<HashMap<Long, IResDraw>> drawLayers;
    protected ConcurrentHashMap<Long, IUpdate> updateItems;//更新物体
    //前后优先级
    protected ConcurrentHashMap<Long, IUpdate>[] updateComponents;//更新组件

    protected GameView gameView;

    protected boolean paused;

    public Scene(GameView gameFrame) {
        this.gameView = gameFrame;
        aliveMonitor = new AliveMonitor();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void init(Object... args) {
        width = 800;
        height = 600;
        //获取两个重要管理器的单例
        logicManager = SingletonManager.getLogicManager();
        drawManager = SingletonManager.getDrawManager();
        components = new ConcurrentHashMap<Long, GameComponentSystem>();
        //初始化渲染层
        drawLayers = new ArrayList<HashMap<Long, IResDraw>>();
        for (int i = 0; i < DrawManager.maxLayerCount; i++)
            drawLayers.add(new HashMap<Long, IResDraw>());
        //逻辑
        updateItems = new ConcurrentHashMap<Long, IUpdate>();
        updateComponents = new ConcurrentHashMap[]{new ConcurrentHashMap<Long, IUpdate>(), new ConcurrentHashMap<Long, IUpdate>()};
        takeControl();
        registerEvents();
    }

    public void takeControl() {
        drawManager.setDrawLayers(drawLayers);
        logicManager.setUpdateItems(updateItems);
        logicManager.setUpdateComponents(updateComponents);
        if (aliveMonitor != null && findComponent("aliveMonitor") == null)
            addComponent(aliveMonitor);
    }

    public void clear() {
        components.clear();
        for (HashMap<Long, IResDraw> layer : drawLayers)
            layer.clear();
        drawLayers.clear();
        updateComponents[0].clear();
        updateComponents[1].clear();
        updateItems.clear();
        aliveMonitor.clear();
    }

    //增加物体
    public Scene addItem(GameItem item) {
        item.bind(this);
        logicManager.addItem(item);
        drawManager.addItem(item);
        return this;
    }

    //删除物体
    public Scene removeItem(GameItem item) {
        logicManager.removeItem(item);
        drawManager.removeItem(item);
        return this;
    }

    //寻找item，目前看来是用不到的(况且ID索引并不是很实用)
    public GameItem findItem(long id) {
        return (GameItem) (logicManager.findUpdateItem(id));
    }

    //添加物体到组件管理器
    public Scene attachComponent(Object item, String id) {
        GameComponentSystem component = findComponent(id);
        if (component != null) {
            //System.out.println("add " + component.getComponentID() + " to " + item.getTag());
            component.addItem(item);
        }
        return this;
    }

    //退之
    public Scene detachComponent(Object item, String id) {
        GameComponentSystem component = findComponent(id);
        if (component != null)
            component.removeItem(item);
        return this;
    }

    //寻之
    public GameComponentSystem findComponent(String id) {
        for (GameComponentSystem c : components.values()) {
            if (c.getComponentID().equals(id)) {
                return c;
            }
        }
        return null;
    }

    //添加组件管理器
    public Scene addComponent(GameComponentSystem c) {
        System.out.println("init " + c.getComponentID());
        components.put(c.getLogicID(), c);
        logicManager.addItem(c);
        if (c instanceof IResDraw)
            drawManager.addItem((IResDraw) c);
        return this;
    }

    //移除组件管理器
    public Scene removeComponent(GameComponentSystem c) {
        components.remove(c.getLogicID());
        logicManager.removeItem(c);
        return this;
    }

    public JPanel getGameView() {
        return gameView;
    }

    public boolean isPaused() {
        return paused;
    }

    public Scene registerEvents(){return this;}
}
 class SceneManager {

    public DrawManager drawManager;
    public LogicManager logicManager;
    private GameView gameView;
    private Thread loopThread;
    private HashMap<String, Scene> scenes;
    private Scene activeScene, emptyScene;
    private String inActive;
    private volatile boolean ready;
    private final long delay = 16 * 1000000;
    //用于绘制和逻辑线程的读写互斥
    //绘制线程是awt的paint线程 保证在解算完成之后再绘制图片
    private final ReadWriteLock readWriteLock;

    //一个小trick了属于是
    //一开始的时候用一个空场景顶替
    //重写draw和takeControl方法 避免获取不到缓冲绘图句柄尴尬
    //这样的话只需要获取一次缓冲graphics，不用每帧都判断一哈offImage要不要创建
    public static class EmptyScene extends Scene {
        public EmptyScene(GameView gameView) {
            super(gameView);
        }

        @Override
        public void init(Object... args) {
            drawManager = new DrawManager(gameView, null) {

                @Override
                public void draw(Graphics g) {
                    g.setColor(Color.BLACK);
                    g.fillRect(0, 0, getW(), getH());
                    System.out.println("empty");
                }
            };
        }

        @Override
        public void takeControl() {
        }
    }

    protected SceneManager() {
        //初始化锁状态
        readWriteLock = new ReentrantReadWriteLock();
    }

    public void init(GameView gameView) {
        //drawManager.init();
        this.gameView = gameView;
        scenes = new HashMap<String, Scene>();
        //获取两个重要管理器的单例
        //并初始化它们
        drawManager = SingletonManager.getDrawManager();
        drawManager.init(gameView, readWriteLock.readLock());
        logicManager = SingletonManager.getLogicManager();
        logicManager.init(gameView, readWriteLock.writeLock(), this);
        ready = true;
        emptyScene = new EmptyScene(gameView);
        emptyScene.init();
        activeScene = emptyScene;
        loopThread = new Thread(new Runnable() {
            private long clock, delta = 0;

            @Override
            public void run() {
                while (ready) {
                    try {
                        //主逻辑
                        //这个其实算fixed update, 强行稳定帧速度
                        clock = System.nanoTime();
                        logicManager.update(clock / 1000000);
                        delta = delay - (System.nanoTime() - clock);
                        if (delta > 0) {
                            Thread.sleep(delta / 1000000);//同上
                            while ((System.nanoTime() - clock) < delay) {
                                // 使用循环，精确控制每帧绘制时长
                            }
                        } else
                            System.out.println("warning: delta < 0");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });//逻辑主线程//主循环逻辑线程
    }

    public void addScene(Scene scene, String id) {
        if (!scenes.containsKey(id)) {
            scenes.put(id, scene);
        }
    }

    //奇妙的切换场景
    public void setActiveScene(String id, boolean clear, boolean init, Object... args) {
        Scene temp = activeScene;
        boolean switched;
        //如果已经在运行场景
        //先一定要上锁
        if (switched = (activeScene != emptyScene)) {
            readWriteLock.writeLock().lock();
        }
        if (inActive == null || !inActive.equals(id)) {
            inActive = id;
            if (clear)
                temp.clear();
            temp = scenes.get(id);
        } else {
            activeScene = emptyScene;
            if (clear)
                temp.clear();
        }
        if (init)
            temp.init(args);
        //恢复内容控制
        //上锁防止冲突
        logicManager.clear();
        temp.takeControl();
        drawManager.init(temp.getWidth(), temp.getHeight());
        activeScene = temp;
        if (switched) {
            readWriteLock.writeLock().unlock();
        }
        if (activeScene != emptyScene && !loopThread.isAlive())
            loopThread.start();
    }

    public void drawScene(Graphics g) {
        activeScene.drawManager.draw(g);
    }

    public void adjustViewPort() {
        System.out.println("adjusting view port……");
        readWriteLock.readLock().lock();
        drawManager.init(activeScene.getWidth(), activeScene.getHeight());
        readWriteLock.readLock().unlock();
    }
}
 class SingletonManager {
    // 基于枚举的单例设计模式
    private enum SingletonEnum {
        INSTANCE;
        private final SceneManager sceneManager;
        private final DrawManager drawManager;
        private final LogicManager logicManager;
        private final EventRegister eventRegister;
        private final Random random;

        SingletonEnum() {
            sceneManager = new SceneManager();
            drawManager = new DrawManager();
            logicManager = new LogicManager();
            eventRegister = new EventRegister();
            random = new Random();
            random.setSeed(System.currentTimeMillis());
        }

        private SceneManager getSceneManager() {
            return sceneManager;
        }

        private DrawManager getDrawManager() {
            return drawManager;
        }

        private LogicManager getLogicManager() {
            return logicManager;
        }

        private EventRegister getEventRegister() {
            return eventRegister;
        }

        private Random getRandom() {
            return random;
        }

    }

    public static SceneManager getSceneManager() {
        return SingletonEnum.INSTANCE.getSceneManager();
    }

    public static DrawManager getDrawManager() {
        return SingletonEnum.INSTANCE.getDrawManager();
    }

    public static LogicManager getLogicManager() {
        return SingletonEnum.INSTANCE.getLogicManager();
    }

    public static EventRegister getEventRegister() {
        return SingletonEnum.INSTANCE.getEventRegister();
    }

    public static Random getRandom() {
        return SingletonEnum.INSTANCE.getRandom();
    }

}
 class Direction {
    //正交的俩方向基底
    public int x, y;

    //基础方向向量
    public static final Direction UP = new Direction(0, -1);
    public static final Direction DOWN = new Direction(0, 1);
    public static final Direction LEFT = new Direction(-1, 0);
    public static final Direction RIGHT = new Direction(1, 0);
    public static final Direction ZERO = new Direction(0, 0);
    public static final Direction[] AXES = {UP, DOWN, LEFT, RIGHT, ZERO};

    //重写哈希判断
    @Override
    public boolean equals(Object o) {
        return o == this || ((o instanceof Direction) && (((Direction) o).x == x && ((Direction) o).y == y));
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    Direction() {
        x = 0;
        y = 0;
    }

    public Direction(int x, int y) {
        this.x = x;
        this.y = y;
    }

    //向量加
    public static Direction add(Direction d1, Direction d2) {
        return new Direction(d1.x + d2.x, d1.y + d2.y);
    }

    //向量减
    public static Direction sub(Direction d1, Direction d2) {
        return new Direction(d1.x - d2.x, d1.y - d2.y);
    }

    //向量数乘
    public static Direction multiply(Direction d, int t) {
        return new Direction(d.x * t, d.y * t);
    }

    //向量点乘
    public static int dot(Direction d1, Direction d2){
        return d1.x * d2.x + d1.y * d2.y;
    }

    //加上
    public void addOn(Direction d) {
        x += d.x;
        y += d.y;
    }

    //减去
    public void minusOn(Direction d) {
        x -= d.x;
        y -= d.y;
    }

    //乘上
    public void multiplyOn(int t) {
        x *= t;
        y *= t;
    }

    //取反
    public Direction minus() {
        return new Direction(-x, -y);
    }

    //设值
    public void set(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void set(Direction d) {
        set(d.x, d.y);
    }

    @Override
    public String toString() {
        return "Direction{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}

 class Rigid {

    private static void reArrangeItems(GameItem collideObj, ICollider collider) {
        Direction moveDirection = collideObj.getDirection();//获得当前碰撞物的移动方向
        AABB collideR = ((ICollider) (collideObj)).getAABB();
        AABB collideOnR = collider.getAABB();
        int dx = 0, dy = 0;
        //对方退后
        if (moveDirection.x > 0)
            //x + w = sx
            dx = (int) (collideOnR.x - collideR.width - collideR.x);
        else if (moveDirection.x < 0)
            //x = sx + sw
            dx = (int) (collideOnR.x + collideOnR.width - collideR.x);
        if (moveDirection.y > 0)
            //y + h = sy
            dy = (int) (collideOnR.y - collideR.height - collideR.y);
        else if (moveDirection.y < 0)
            //y = sy + sh
            dy = (int) (collideOnR.y + collideOnR.height - collideR.y);
        collideObj.setPos(Direction.add(collideObj.getLastPos(), new Direction(dx + moveDirection.x, dy + moveDirection.y)));
    }

    public static void staticBounceBack(ICollider item, ICollider collider, boolean callBack) {
        //一个比较粗糙的只适合于动碰静被阻拦的算法
        GameItem obj = (GameItem) item;
        /*Direction moveDirection = obj.getDirection();//获得当前碰撞物的移动方向
        int dx = 0, dy = 0;
        Direction lastPos = obj.getLastPos();
        AABB r = item.getAABB();
        AABB onCollider = collider.getAABB();*/
        reArrangeItems(obj, collider);
        //如果要通知碰撞物
        if (callBack) {
            //根据filter判断到底要不要通知
            HashMap<String, Integer> filter = item.getFilter();
            if (filter == null)
                item.onCollision(collider);
            else {
                Integer fMode = filter.get(CollisionManager.filterMode);
                Integer filterVal = filter.get(collider.getColliderTag());
                fMode = fMode == null ? 0 : fMode;
                switch (fMode) {
                    case 0:
                        if (filterVal != null && filterVal == 1)
                            item.onCollision(collider);
                        break;
                    case 1:
                        if (filterVal == null || filterVal != 1)
                            item.onCollision(collider);
                        break;
                }
            }
        }
    }


    //默认二者必须都为GameItem
    public static void dynamicBounceBack(ICollider collide, ICollider collideOn) {
        GameItem collideObj = (GameItem) collide;//碰撞物体
        GameItem collideOnObj = (GameItem) collideOn;//自身
        AABB collideR = collide.getAABB();
        AABB collideOnR = collideOn.getAABB();
        Direction self = collideOnObj.getDirection();
        Direction opposite = collideObj.getDirection();
        boolean reSelf = false, reOpposite = false;
        //各退一步
        //考虑谁的原本运行方向下不会发生碰撞，谁就正常运行
        //否则大家就停住
        //先测试对方
        collideOnR.x -= self.x;
        collideOnR.y -= self.y;
        //仍然碰撞
        reSelf = collideOnR.overlaps(collideR);
        //恢复
        collideOnR.x += self.x;
        collideOnR.y += self.y;
        //再测试自己
        collideR.x -= opposite.x;
        collideR.y -= opposite.y;
        //仍然碰撞
        reOpposite = (collideR.overlaps(collideOnR));
        if (reSelf && reOpposite) {
            reArrangeItems(collideOnObj, collide);
        } else {
            if (reSelf) {
                //自己停住
                reArrangeItems(collideOnObj, collide);
                if (reOpposite) {
                    //对方停住
                    reArrangeItems(collideObj, collideOn);
                }
            }

        }
    }
}
 class myGameFrame extends GameFrame {

    public myGameFrame(String str, int w, int h) {
        super(str, w, h);
        setBackground(Color.BLACK);
        gameView = new GameView(w, h, Resource.getResourceHandlerInstance());
        setGameView(gameView);
        Scene scene1 = new MainScene(gameView);
        Scene scene2 = new MainScene(gameView);
        SceneManager sceneManager = SingletonManager.getSceneManager();
        sceneManager.addScene(scene1, "1");
        sceneManager.addScene(scene2, "2");
        sceneManager.setActiveScene("1", false, true);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        //只是用来抹平底下莫名其妙的白线
        g.drawLine(0, getHeight() - 7, getWidth(), getHeight() - 7);
    }
}
 class Bullet extends GameItem implements ICollider {

    private Direction direction;
    private HashMap<String, Integer> collisionFilter;//碰撞过滤
    private AnimationPlayer explosion;
    private AnimationItem explosionItem;
    private int subMove;
    private final AABB aabb;
    private ICollider onCollision;
    //private CollisionManager collisionManager;

    public Bullet(Direction pos, Direction direction, String tag) {
        super(pos, Resource.getResourceHandlerInstance());
        this.direction = direction;
        layer = 3;
        this.tag = tag;
        initImgArea();
        aabb = new AABB(pos.x + 2, pos.y + 2, (float) imgArea.dx / 2, (float) imgArea.dy / 2);

    }

    public Bullet(int x, int y, Direction direction, String tag) {
        super(x, y, Resource.getResourceHandlerInstance());
        this.direction = direction;
        layer = 3;
        this.tag = tag;
        initImgArea();
        aabb = new AABB(pos.x + 2, pos.y + 2, (float) imgArea.dx / 2, (float) imgArea.dy / 2);

    }

    void initImgArea() {
        imgArea = (ImgArea) resourceHandler.getResource("bulletTexture");
        explosion = new AnimationPlayer((Animation) resourceHandler.getResource("bulletExplosion"));
        explosion.setCycled(false);
    }

    public Bullet setDirection(Direction direction) {
        this.direction = direction;
        return this;
    }

    @Override
    public void init() {
        super.init();
        collisionFilter = new HashMap<String, Integer>();
        collisionFilter.put("_WALL_", 1);//只响应墙壁
        scene.attachComponent(this, "collision");//添加碰撞组件
        subMove = 0;
    }

    //移动和爆炸销毁反应
    @Override
    public void update(long clock) {
        super.update(clock);
        pos.addOn(Direction.multiply(direction, 1 + (subMove ^= 1)));
    }

    @Override
    public void draw(Graphics g) {
        super.draw(g);
    }

    @Override
    public void bind(DrawManager d) {
        super.bind(d);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("bullet " + getID() + " destroyed.");
        if (!(onCollision instanceof Tank)) {
            String bPoolExp = "bulletExplosions";
            if (onCollision instanceof CollisionManager.Wall)
                bPoolExp = "bulletExplosions2";
            explosionItem = (AnimationItem) resourceHandler.getItemByName(bPoolExp);
            scene.addItem(explosionItem.setPos(new Direction(pos.x - 14, pos.y - 14)));
        }
    }

    @Override
    public void onCollision(ICollider item) {
        //只考虑出界
        onCollision = item;
        isDestroyed = true;
        scene.detachComponent(this, "collision");
        scene.removeItem(this);
    }

    @Override
    public void bind(CollisionManager c) {
        //不用处理
        //collisionManager = c;
    }

    @Override
    public AABB getAABB() {
        //修正了一哈包围盒
        aabb.x = pos.x + 2;
        aabb.y = pos.y + 2;
        return aabb;
    }

    @Override
    public long getColliderID() {
        return ID;
    }

    @Override
    public String getColliderTag() {
        return tag;
    }

    @Override
    public boolean isInitiative() {
        return false;
    }

    @Override
    public boolean isActive() {
        return !isDestroyed;
    }

    @Override
    public boolean isStatic() {
        return false;
    }

    @Override
    public boolean isRigid() {
        return false;
    }

    @Override
    public HashMap<String, Integer> getFilter() {
        return collisionFilter;
    }
}
 class HomeSpot extends GameItem {

    private Direction homePos;

    public HomeSpot(Direction pos) {
        super(pos, Resource.getResourceHandlerInstance());
    }

    @Override
    public void init() {
        super.init();
        imgArea = (ImgArea) resourceHandler.getResource("homeSpot");
        int brickWidth = ((ImgArea) resourceHandler.getResource("brickImg")).dx;
        //最上面一排
        for (int i = 0; i < 6; i++) {
            scene.addItem(new Tile(Tile.TileType.BRICK,pos.x + i * brickWidth, pos.y));
        }
        //左边一列
        for (int i = 6; i < 6 + 4; i++)
            scene.addItem(new Tile(Tile.TileType.BRICK, pos.x, pos.y + (i - 5) * brickWidth));
        //右边一列
        for (int i = 10; i < 10 + 4; i++)
            scene.addItem(new Tile(Tile.TileType.BRICK, pos.x + 5 * brickWidth, pos.y + (i - 9) * brickWidth));
        homePos = new Direction(pos.x + brickWidth, pos.y + brickWidth);
    }

    @Override
    public void draw(Graphics g) {
        resourceHandler.draw(g, homePos.x, homePos.y, imgArea);
    }
}
 class GameMap {
    private Scene activeScene;
    private final int width = 800;
    private final int height = 600;

    private void addTilesByPos(Tile.TileType type, List<Integer> pos) {
        for (int i = 0; i < pos.size(); i += 2) {
            addTilesByPos(type, pos.get(i), pos.get(i + 1));
        }
    }

    private void addTilesByPos(Tile.TileType type, int x, int y) {
        Tile tile = new Tile(type, 0, 0);
        int width = 17;
        activeScene.addItem(tile.setPos(new Direction(x * width, y * width)));
    }

    public GameMap setActiveScene(Scene scene) {
        activeScene = scene;
        return this;
    }

    public GameMap init() {
        ResourceHandler resourceHandler = Resource.getResourceHandlerInstance();
        //初始化地图
        int brickWidth = ((ImgArea) resourceHandler.getResource("brickImg")).dx;
        PlayerTank playerTank = new PlayerTank(width / 2 - 34 / 2, height - brickWidth * 6 + 2 - 40);//在200, 200处生成一辆坦克
        activeScene.addComponent(new StatusMonitor(activeScene, new Direction(width / 2 - 34 / 2, height - brickWidth * 6 + 2 - 40), playerTank));
        activeScene.logicManager.addItem(new SpriteTankFactory(new Direction(9 + 34 * 11, 100), activeScene));
        activeScene.addItem(new HomeSpot(new Direction(width / 2 - brickWidth * 6 / 2, height - brickWidth * 5)));
        activeScene.addItem(playerTank);
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //地图图块
        for (int i = 5; i < 45; i++) {
            addTilesByPos(Tile.TileType.BRICK, i, 10);
        }
        for (int i = 6; i < 42; i++) {
            addTilesByPos(Tile.TileType.GRASS, i, 11);
        }
        for (int i = 10; i < 35; i++) {
            addTilesByPos(Tile.TileType.WATER, i, 13);
        }
        for (int i = 25; i < 35; i++) {
            addTilesByPos(Tile.TileType.IRON, i, 15);
        }
        return this;
    }

}
 class Tile extends CollidableGameItem {

    // 图块种类
    public enum TileType {
        BRICK,
        IRON,
        GRASS,
        WATER,
        HOME
    }

    protected TileType type;
    protected int width;
    protected String tileName;
    protected boolean collidable;
    protected AnimationPlayer animationPlayer;

    // 事件
    public static long ON_TILE_DRAW = EventRegister.getEventID();

    //根据传入类型初始化
    private void initByType() {
        events = new HashMap<>();
        EventRegister eventRegister = SingletonManager.getEventRegister();
        switch (type) {
            case BRICK:
                tileName = "brick";
                tag = "_WALL_";
                imgArea = (ImgArea) resourceHandler.getResource("brickImg");
                collidable = true;
                addEvent(CollisionManager.ON_COLLISION, eventRegister.getEvent("staticBounceBack"))
                        .addEvent(CollisionManager.ON_COLLISION, eventRegister.getEvent("destroyByBullet"));
                break;
            case IRON:
                tileName = "iron";
                tag = "_WALL_";
                imgArea = (ImgArea) resourceHandler.getResource("ironImg");
                collidable = true;
                addEvent(CollisionManager.ON_COLLISION, eventRegister.getEvent("staticBounceBack"));
                break;
            case GRASS:
                tileName = "grass";
                imgArea = (ImgArea) resourceHandler.getResource("grassImg");
                collidable = false;
                layer = 4;
                break;
            case WATER:
                tileName = "water";
                Animation animation = (Animation) resourceHandler.getResource("waterAnimation");
                animationPlayer = new AnimationPlayer(animation);
                animationPlayer.setCycled(true);
                addEvent(ON_TILE_DRAW, eventRegister.getEvent("animationPlay"));
                /*addEvent(ON_COLLISION, new IItemEvent() {
                    @Override
                    public void run(Object... args) {
                        if(args[0] instanceof Tank) {
                            Tank tank = (Tank) args[0];
                            tank.destroy();
                        }
                    }
                });*/
                imgArea = new ImgArea(0, 0, 0, 0, 0, 0);
                width = animation.getFrame(0).dx;
                collidable = true;
                break;
        }
        if (width == 0)
            width = imgArea.dx;
        aabb = new AABB(pos.x, pos.y, width, width);
    }

    public int getWidth() {
        return width;
    }

    public Tile(TileType tileType, Direction pos) {
        super(pos, Resource.getResourceHandlerInstance());
        type = tileType;
        initByType();
    }

    public Tile(TileType tileType, int x, int y) {
        super(x, y, Resource.getResourceHandlerInstance());
        type = tileType;
        initByType();
    }

    @Override
    public void hang() {
        super.hang();
        if (animationPlayer != null)
            animationPlayer.setPause(true);
    }

    @Override
    public void resume() {
        super.resume();
        if (animationPlayer != null)
            animationPlayer.setPause(false);
    }

    @Override
    public void init() {
        super.init();
        if (collidable)
            scene.attachComponent(this, "collision");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (collidable)
            scene.detachComponent(this, "collision");
    }

    @Override
    public void draw(Graphics g) {
        super.draw(g);
        if (events.containsKey(ON_TILE_DRAW))
            for (IGameEvent event : events.get(ON_TILE_DRAW)) {
                event.run(animationPlayer, g, pos.x, pos.y);
            }
    }


    @Override
    public boolean isStatic() {
        return true;
    }
}
 class PlayerTank extends Tank implements IInput {

    private long coolDownInterval;
    private CollisionManager collisionManager;
    //按键输入相关
    private int lastKey = KeyEvent.VK_UP;
    public int currentKey = lastKey;
    public boolean activated;
    private List<Direction> directions;
    private List<Integer> defaultKeys;
    private HashMap<Integer, Direction> tankDirectionTable;//键盘方位对照表
    private Direction direction = Direction.UP;
    private PeriodicTimer periodicTimer;
    private IGameEvent tankReborn;

    public PlayerTank(Direction pos) {
        super(pos);
        tag = "player";
    }

    //设置自定义键位
    void setKeyCollections(List<Integer> keys) {
        //清除原先储存的键位
        tankDirectionTable.clear();
        Iterator<Integer> keyIter = keys.iterator();
        for (Direction dir : directions) {
            if (keyIter.hasNext())
                tankDirectionTable.put(keyIter.next(), dir);
            else return;
        }

    }

    public PlayerTank(int x, int y) {
        super(x, y);
        tag = "player";
    }

    @Override
    public void init() {
        super.init();
        coolDownInterval = 500L;
        //手动优化switch了属于是
        tankDirectionTable = new HashMap<Integer, Direction>();
        directions = Arrays.asList(Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT);
        defaultKeys = Arrays.asList(KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT);
        //初始化设置自定义键位
        setKeyCollections(defaultKeys);
        activated = false;
        collisionFilter.put("enemyBullet", 1);
        collisionFilter.put("enemy", 1);
        scene.attachComponent(this, "input");
        periodicTimer = new PeriodicTimer();
        //射击冷却时间
        periodicTimer.addEvent("fire", new IGameEvent() {
            @Override
            public void run(Object... args) {
                fire();
            }

            @Override
            public Object getInfo(Object... args) {
                return coolDownInterval;
            }
        });
        tankReborn = new IGameEvent() {
            @Override
            public void run(Object... args) {
                ((StatusMonitor) (scene.findComponent("statusMonitor"))).playerTankRebirth();
                PlayerTank.this.isDestroyed = false;
                currentKey = lastKey = KeyEvent.VK_UP;
            }

            @Override
            public Object getInfo(Object... args) {
                return true;
            }
        };
    }

    @Override
    public void update(long clock) {
        super.update(clock);
    }

    @Override
    public void bind(CollisionManager c) {
        collisionManager = c;
    }

    //优化 只是将坦克移除出渲染和更新队列
    @Override
    public GameItem destroy() {
        scene.detachComponent(this, "input");
        scene.detachComponent(this, "collision");
        //移除渲染和更新队列
        setDrawn(false);
        scene.findComponent("aliveMonitor").addItem(this);
        explosionItem = (AnimationItem) resourceHandler.getItemByName("tankExplosions")
                .addEvent(GameItem.ON_DESTROY, tankReborn);
        destroyEffect();
        return this;
    }

    @Override
    public void keyPressed(KeyEvent key) {
        //减少一直按下的逻辑判断
        currentKey = key.getKeyCode();
        if (currentKey != lastKey)
            if ((direction = tankDirectionTable.get(currentKey)) != null) {
                setDirection(direction);//设置方向
                lastKey = currentKey;
                return;
            }
        //很不舒服，Java没有c/cpp的函数指针，也没有c#的委托，实现不了我喜欢的奇技淫巧，除非用反射
        //本来可以一步到位所有按键映射到对应方法上的，确信
        switch (currentKey) {
            case KeyEvent.VK_Q:
                setCategory((getCategory() + 1) & (Tank.maxCategory - 1));//取余2次幂的优化形式
                break;
            case KeyEvent.VK_I:
                setDrawn(!isDrawn());
                break;
            case KeyEvent.VK_SPACE: {
                periodicTimer.update(System.currentTimeMillis());
            }
            break;
        }
    }

    @Override
    public long getInputID() {
        return ID;
    }

    @Override
    public void bind(InputManager i) {

    }

    @Override
    public void hang() {
        super.hang();
        scene.detachComponent(this, "input");
    }

    @Override
    public void resume() {
        super.resume();
        scene.attachComponent(this, "input");
    }
}
 class SpriteTank extends Tank {

    private PeriodicTimer periodicTimer;
    private boolean bounced;
    private Random rand;

    public SpriteTank(Direction pos) {
        //初始化操纵
        super(pos);
        setDirection(Direction.DOWN);
        setCategory(4);
        tag = "enemy";
    }

    public SpriteTank(int x, int y) {
        //同上
        super(x, y);
        setDirection(Direction.DOWN);
        setCategory(4);
        tag = "enemy";
    }

    public void init() {
        super.init();
        bounced = false;
        rand = SingletonManager.getRandom();
        setDirection(Direction.AXES[rand.nextInt(4)]);
        collisionFilter.put("playerBullet", 1);
        collisionFilter.put("_WALL_", 1);
        collisionFilter.put("enemy", 1);
        periodicTimer = new PeriodicTimer(this);
        //开火和转弯的定时事件
        EventRegister eventRegister = SingletonManager.getEventRegister();
        periodicTimer.addEvent("fire", eventRegister.getEvent("periodicFire"))
                .addEvent("turn", eventRegister.getEvent("periodicTurn"));
    }

    @Override
    public void update(long clock) {
        super.update(clock);
        periodicTimer.update(clock);
    }

    @Override
    public void onCollision(ICollider item) {
        super.onCollision(item);
        if (item.getColliderTag().equals("_WALL_")) {
            setDirection(direction.minus());
            bounced = true;
        }
    }

    public boolean isBounced() {
        return bounced;
    }

    public SpriteTank setBounced(boolean bounced) {
        this.bounced = bounced;
        return this;
    }
}
 class SpriteTankFactory implements IUpdate {

    private long lastClock;
    private boolean isDestroyed;
    private final Random rand;
    private Direction[] generatedPos;//定点生成
    private int gapLength;
    private PeriodicTimer periodicTimer;
    private long ID;
    private Direction pos;
    private Scene scene;

    public SpriteTankFactory(Direction pos, Scene scene) {
        rand = new Random(System.nanoTime());
        ID = GameItem.generateID();
        this.pos = pos;
        this.scene = scene;
    }

    @Override
    public void update(long clock) {
        //每1秒有50%概率随机在三个点位的其中一个生成坦克
        periodicTimer.update(clock);
    }

    @Override
    public void init() {
        // 生成三个图块
        // 不参与绘制
        lastClock = 0;
        gapLength = 34 * 7;
        for (int i = -gapLength; i <= gapLength; i += gapLength)
            scene.addItem(new Tile(Tile.TileType.GRASS, pos.x + i, pos.y));
        generatedPos = new Direction[]{new Direction(pos.x - gapLength, pos.y), pos, new Direction(pos.x + gapLength, pos.y)};
        periodicTimer = new PeriodicTimer();
        //每1秒有50%概率随机在三个点位的其中一个生成坦克
        ResourceHandler resourceHandler = Resource.getResourceHandlerInstance();
        periodicTimer.addEvent("generate", new IGameEvent() {
            @Override
            public void run(Object... args) {
                if (rand.nextInt(2) == 0) {
                    Direction direction = new Direction(0, 0);
                    int index = rand.nextInt(3);
                    direction.set(generatedPos[index]);
                    SpriteTank spriteTank = (SpriteTank) resourceHandler.getItemByName("enemies");
                    spriteTank.setPos(direction);
                    scene.addItem(spriteTank);
                }
            }

            @Override
            public Object getInfo(Object... args) {
                return 1000L;
            }
        });
    }

    @Override
    public void hang() {
    }

    @Override
    public void resume() {

    }

    @Override
    public void bind(LogicManager l) {
    }

    @Override
    public long getLogicID() {
        return ID;
    }


    @Override
    public void onDestroy() {
        isDestroyed = true;
    }

    @Override
    public boolean isDestroyed() {
        return isDestroyed;
    }
}
 class StatusMonitor extends GameComponentSystem implements IInput {

    private final AnimationItem rebirthAnimationItem;
    private final AnimationPlayer animationPlayer;
    private final PlayerTank playerTank;
    private AliveMonitor aliveMonitor;
    private CollisionManager collisionManager;
    private final Scene scene;
    private final Direction rebirthPos;

    public StatusMonitor(Scene scene, Direction rebirthPos, PlayerTank playerTank) {
        ID = GameItem.generateID();
        componentID = "statusMonitor";
        isDestroyed = false;
        this.playerTank = playerTank;
        ResourceHandler resourceHandler = Resource.getResourceHandlerInstance();
        Animation rebirthAnimation = (Animation) resourceHandler.getResource("rebirthAnimation");
        this.scene = scene;
        this.rebirthPos = rebirthPos;
        animationPlayer = new AnimationPlayer(rebirthAnimation);
        animationPlayer.setCycled(false);
        rebirthAnimationItem = new AnimationItem(new Direction(rebirthPos.x, rebirthPos.y), animationPlayer) {
            @Override
            public void onDestroy() {
                super.onDestroy();
                playerTank.setPos(rebirthPos);
                scene.attachComponent(playerTank, "input");
                scene.attachComponent(playerTank, "collision");
                aliveMonitor.removeItem(playerTank);
                playerTank.setDirection(Direction.UP);
                playerTank.setDrawn(true);
            }
        };
        rebirthAnimationItem.setCycleTimeLimited(true).setCycleCnt(3).setLayer(1);
    }

    @Override
    public void init() {
        super.init();
        scene.attachComponent(this, "input");
        aliveMonitor = (AliveMonitor) scene.findComponent("aliveMonitor");
        collisionManager = (CollisionManager) scene.findComponent("collision");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        scene.detachComponent(this, "input");
    }

    public void playerTankRebirth() {
        System.out.println("Player Tank Reborn");
        animationPlayer.setPause(false).resetFrame();
        scene.addItem(rebirthAnimationItem);
    }

    @Override
    public void keyPressed(KeyEvent key) {
        switch (key.getKeyCode()) {
            case KeyEvent.VK_R: {
                aliveMonitor.hangOnAllItems().addItem(collisionManager);
                break;
            }
            case KeyEvent.VK_C: {
                aliveMonitor.resumeAllItems().removeItem(collisionManager);
                break;
            }
        }
    }

    @Override
    public long getInputID() {
        return ID;
    }

    @Override
    public void bind(InputManager i) {

    }
}
 abstract class Tank extends CollidableGameItem {

    public static final int maxCategory = 8;
    private int category;//坦克种类
    protected Direction direction;//坦克的行进方向
    private HashMap<Direction, Integer> drawDirectionTable;//用于创建逻辑方向和贴图方位的直接映射
    private int drawDirection;//贴图方向
    private AnimationPlayer[][] animationPlayers;
    protected int w, h;
    //protected CollisionManager collisionManager;
    protected int velocity;
    protected HashMap<String, Integer> collisionFilter;//碰撞过滤
    protected AnimationItem explosionItem;

    //初始化逻辑方向和贴图方位的映射关系
    //手动优化switch了属于是
    private void initDrawTable() {
        drawDirectionTable = new HashMap<Direction, Integer>();
        drawDirectionTable.put(Direction.UP, 0);
        drawDirectionTable.put(Direction.RIGHT, 1);
        drawDirectionTable.put(Direction.DOWN, 2);
        drawDirectionTable.put(Direction.LEFT, 3);
    }

    //构造方法1
    public Tank(Direction pos) {
        super(pos, Resource.getResourceHandlerInstance());
        category = 0;
        drawDirection = 0;
        direction = Direction.UP;//初始向上运动
        initDrawTable();
        layer = 2;
        aabb = new AABB(pos.x + 4, pos.y + 2, 26, 28);
        initAnimation();
    }

    //构造方法2
    public Tank(int x, int y) {
        super(x, y, Resource.getResourceHandlerInstance());
        category = 0;
        drawDirection = 0;
        direction = Direction.UP;//初始向上运动
        initDrawTable();
        layer = 2;
        aabb = new AABB(pos.x + 4, pos.y + 2, 26, 28);
        initAnimation();
    }

    //初始化贴图状态
    private void initAnimation() {
        animationPlayers = new AnimationPlayer[8][4];
        Animation[][] animations = (Animation[][]) resourceHandler.getResource("tankAnimations");
        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 4; j++)
                animationPlayers[i][j] = new AnimationPlayer(animations[i][j]);
    }

    @Override
    public void bind(CollisionManager c) {
        //collisionManager = c;
    }

    //发射子弹
    public void fire() {
        //修正子弹发射中心
        Direction center = new Direction(pos.x + 12, pos.y + 17 - Math.abs(direction.x) * 5 - ((direction.y == -1) ? 8 : 0));
        Direction bulletPos = Direction.add(center, Direction.multiply(direction, 20));
        if (Math.abs(direction.x) == 1)
            bulletPos.addOn(Direction.multiply(direction, 2));
        else
            bulletPos.addOn(Direction.multiply(direction, -2));
        Bullet bullet = (Bullet) resourceHandler.getItemByName("bullets");
        bullet.setDirection(direction).setTag(tag + "Bullet");
        bullet.setPos(bulletPos);
        bullet.setLayer(3);
        scene.addItem(bullet);
    }

    //设置坦克种类
    public Tank setCategory(int category) {
        this.category = category;
        return this;
    }

    //获取坦克种类
    public int getCategory() {
        return category;
    }

    //设置坦克方向
    public Tank setDirection(Direction direction) {
        this.direction = direction;
        drawDirection = drawDirectionTable.get(direction);//同时获取贴图位置
        return this;
    }

    public Tank setVelocity(int velocity) {
        return this;
    }

    public int getVelocity() {
        return velocity;
    }

    //坦克移动
    public Tank move() {
        //向量运算
        pos.addOn(Direction.multiply(this.direction, velocity));
        return this;
    }

    public Tank destroyEffect() {
        if (explosionItem == null)
            explosionItem = (AnimationItem) resourceHandler.getItemByName("tankExplosions");
        explosionItem.setPos(new Direction(pos.x - 17, pos.y - 17));
        explosionItem.setLayer(2);
        scene.addItem(explosionItem);
        explosionItem = null;
        return this;
    }

    @Override
    public void init() {
        super.init();
        scene.attachComponent(this, "collision");
        velocity = 1;
        collisionFilter = new HashMap<String, Integer>();
        collisionFilter.put("_WALL_", 1);
        clearEvent(CollisionManager.ON_COLLISION);
        addEvent(CollisionManager.ON_COLLISION, SingletonManager.getEventRegister().getEvent("destroyByBullet"));
    }

    @Override
    public void update(long clock) {
        super.update(clock);
        //animationPlayers[category][drawDirection].setPause(false);
        move();
    }

    @Override
    public GameItem destroy() {
        scene.detachComponent(this, "collision");
        return super.destroy();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        destroyEffect();
    }

    @Override
    public void draw(Graphics g) {
        animationPlayers[category][drawDirection].draw(g, pos.x, pos.y);
    }

    //绑定绘制管理器，方便按需移除/加入自身
    @Override
    public void bind(DrawManager d) {
        super.bind(d);
        w = d.getW();
        h = d.getH();
    }

    @Override
    public void onCollision(ICollider item) {
        super.onCollision(item);
        if (isDestroyed && item instanceof Bullet) {
            System.out.println("hit! " + item.getAABB().x + " " + item.getAABB().y);
        }
        /*else if (item.getColliderTag().equals("_WALL_")) {
            //animationPlayers[category][drawDirection].setPause(true);
        }*/
    }

    @Override
    public AABB getAABB() {
        aabb.x = pos.x + 4;
        aabb.y = pos.y + 2;
        return aabb;
    }

    @Override
    public boolean isInitiative() {
        return true;
    }

    @Override
    public boolean isActive() {
        return !isDestroyed;
    }

    @Override
    public boolean isRigid() {
        return true;
    }

    @Override
    public HashMap<String, Integer> getFilter() {
        return collisionFilter;
    }

    @Override
    public void hang() {
        super.hang();
        animationPlayers[category][drawDirection].setPause(true);
        //scene.detachComponent(this, "collision");
    }

    @Override
    public void resume() {
        super.resume();
        animationPlayers[category][drawDirection].setPause(false);
        //scene.attachComponent(this, "collision");
    }
}
 class Resource extends ResourceHandler {

    //基于枚举的安全单例 (枚举类型可以避免多线程问题)
    private enum Singleton {
        INSTANCE;
        private final Resource instance;
        private final GameMap mapInstance;

        Singleton() {
            instance = new Resource("robots_sprite.png");
            mapInstance = new GameMap();
        }

        private ResourceHandler getResourceHandlerInstance() {
            return instance;
        }

        private GameMap getMapInstance() {
            return mapInstance;
        }
    }

    public static ResourceHandler getResourceHandlerInstance() {
        return Singleton.INSTANCE.getResourceHandlerInstance();
    }

    public static GameMap getMapInstance() {
        return Singleton.INSTANCE.getMapInstance();
    }

    public Resource(String path) {
        super(path);
        File f = new File(path);
        try {
            res = ImageIO.read(f);
        } catch (IOException e) {
            e.printStackTrace();
        }
        staticResources.put("texture", res);
        //坦克动画
        ImgArea[][][] tankImgAreas = new ImgArea[8][4][2];
        Animation[][] tankAnimations = new Animation[8][4];
        for (int i = 0; i < 8; i++) {
            //4个方向两种状态
            //计算贴图位置
            for (int j = 0; j < 4; j++) {
                int x = i * 34 * 8 + j * 68;
                int y;
                if ((y = i / 4 * 34) == 34)
                    x -= 68 * 16;
                tankImgAreas[i][j][0] = new ImgArea(x, y, 34, 34, x + 34, y + 34);
                tankImgAreas[i][j][1] = new ImgArea(x + 34, y, 34, 34, x + 68, y + 34);
                tankAnimations[i][j] = new Animation(this);
                tankAnimations[i][j].addFrame(5, tankImgAreas[i][j][0]).addFrame(3, tankImgAreas[i][j][1]);
            }
        }
        //修正一个偏差
        //c2 left flag 0
        tankImgAreas[2][3][0].x += 1;
        tankImgAreas[2][3][0].sx += 1;
        //resources.put("tankTextures", tankImgAreas);
        staticResources.put("tankAnimations", tankAnimations);
        //子弹贴图
        staticResources.put("bulletTexture", new ImgArea(170, 204, 10, 10, 170 + 34, 204 + 34));
        //子弹爆炸
        Animation bulletExplosion = new Animation(this);
        for (int i = 0; i < 3; i++) {
            bulletExplosion.addFrame(6, new ImgArea((20 + i) * 34, 4 * 34, 38, 38, (20 + i + 1) * 34, 5 * 34));
        }
        staticResources.put("bulletExplosion", bulletExplosion);
        // 子弹爆炸二
        Animation bulletExplosion2 = new Animation(this);
        for (int i = 0; i < 4; i++) {
            bulletExplosion2.addFrame(6, new ImgArea((16 + i) * 34, 4 * 34, 38, 38, (16 + i + 1) * 34, 5 * 34));
        }
        staticResources.put("bulletExplosion2", bulletExplosion2);
        //坦克爆炸
        Animation tankExplosion = new Animation(this);
        for (int i = 0; i < 2; i++) {
            tankExplosion.addFrame(10, new ImgArea(23 * 34 + i * 68, 4 * 34, 68, 68, 23 * 34 + (i + 1) * 68, 4 * 34 + 68));
        }
        staticResources.put("tankExplosion", tankExplosion);
        //精灵坦克生成点
        staticResources.put("spriteTankSpot", new ImgArea(34 * 4, 34 * 7, 34, 34, 34 * 5, 34 * 8));
        //玩家坦克生成点
        ImgArea homeSpot = new ImgArea(34 * 19, 34 * 5, 17 * 4, 17 * 3, 34 * 20, 34 * 6);
        staticResources.put("homeSpot", homeSpot);
        //重生动画
        Animation rebirthAnimation = new Animation(this);
        for (int i = 0; i < 3; i++) {
            rebirthAnimation.addFrame(5, new ImgArea(13 * 34 + i * 34, 34 * 7, 34, 34, 13 * 34 + i * 34 + 34, 34 * 7 + 34));
        }
        staticResources.put("rebirthAnimation", rebirthAnimation);
        ////////////////////////////////////////////////////////////////////////////////////////////////
        //地图图块
        staticResources.put("brickImg", new ImgArea(34 * 18 + 1, 34 * 5 + 1, 17, 17, 34 * 18 + 34 / 2, 34 * 5 + 34 / 2));
        staticResources.put("grassImg", new ImgArea(34 * 4, 34 * 7, 34, 34, 34 * 5, 34 * 8));
        staticResources.put("ironImg", new ImgArea(1, 34 * 6 + 1, 17, 17, 17, 34 * 6 + 17));
        //水
        Animation waterAnimation = new Animation(this);
        for (int i = 0; i < 2; i++) {
            waterAnimation.addFrame(25, new ImgArea(i * 34, 34 * 7, 34, 34, (i + 1) * 34, 34 * 8));
        }
        staticResources.put("waterAnimation", waterAnimation);
    }
}
 class MainScene extends Scene {
    public MainScene(GameView gameView) {
        super(gameView);
    }

    @Override
    public Scene registerEvents() {
        EventRegister eventRegister = SingletonManager.getEventRegister();
        eventRegister.unregisterAll();
        // 基础事件注册
        IGameEvent staticBounceBack = new IGameEvent() {
            @Override
            public void run(Object... args) {
                Rigid.staticBounceBack((ICollider) args[0], (ICollider) args[1], !(args[0] instanceof Bullet));
            }

            @Override
            public Object getInfo(Object... args) {
                return false;
            }
        };
        IGameEvent destroyByBullet = new IGameEvent() {
            @Override
            public void run(Object... args) {
                if (!((GameItem) args[1]).isDestroyed() && args[0] instanceof Bullet) {
                    ((ICollider) args[0]).onCollision((ICollider) args[1]);
                    ((GameItem) args[1]).destroy();
                }
            }

            @Override
            public Object getInfo(Object... args) {
                return false;
            }
        };
        IGameEvent animationPlay = new IGameEvent() {
            @Override
            public void run(Object... args) {
                ((AnimationPlayer) args[0]).draw((Graphics) args[1], (int) args[2], (int) args[3]);
            }

            @Override
            public Object getInfo(Object... args) {
                return false;
            }
        };
        IGameEvent animationInit = new IGameEvent() {
            @Override
            public void run(Object... args) {
                ((AnimationPlayer) args[1]).init();
                ((AnimationPlayer) args[1]).setCycled(false);
            }

            @Override
            public Object getInfo(Object... args) {
                return false;
            }
        };
        // 初始化归还事件
        ResourceHandler resourceHandler = Resource.getResourceHandlerInstance();
        IGameEvent releaseToPool = new IGameEvent() {
            @Override
            public void run(Object... args) {
                resourceHandler.releaseItem(((GameItem) args[0]).getPoolName(), (GameItem) args[0]);
            }

            @Override
            public Object getInfo(Object... args) {
                return false;
            }
        };
        // 坦克行为
        Random sysRandom = SingletonManager.getRandom();
        IGameEvent periodicFire = new IGameEvent() {
            @Override
            public void run(Object... args) {
                Tank tank = (Tank) args[0];
                if (sysRandom.nextInt(4) == 0)
                    tank.fire();
            }

            @Override
            public Object getInfo(Object... args) {
                return 500L;
            }
        };
        IGameEvent periodicTurn = new IGameEvent() {
            @Override
            public void run(Object... args) {
                SpriteTank tank = (SpriteTank) args[0];
                if (!tank.isDestroyed()) {
                    if (!tank.isBounced())
                        tank.setDirection(Direction.AXES[sysRandom.nextInt(4)]);
                    else
                        tank.setBounced(false);
                }
            }

            @Override
            public Object getInfo(Object... args) {
                return 3000L;
            }
        };
        // 添加到事件注册器
        eventRegister.register("staticBounceBack", staticBounceBack);
        eventRegister.register("destroyByBullet", destroyByBullet);
        eventRegister.register("animationPlay", animationPlay);
        eventRegister.register("animationInit", animationInit);
        eventRegister.register("releaseToPool", releaseToPool);
        eventRegister.register("periodicFire", periodicFire);
        eventRegister.register("periodicTurn", periodicTurn);
        return super.registerEvents();
    }

    @Override
    public void init(Object... args) {
        super.init();
        //playerTank.keyMonitor.setKeyCollections(Arrays.asList(KeyEvent.VK_W, KeyEvent.VK_S, KeyEvent.VK_A, KeyEvent.VK_D));
        ResourceHandler resourceHandler = Resource.getResourceHandlerInstance();
        //初始化场景组件
        addComponent(new CollisionManager(width, height))
                .addComponent(new InputManager(gameView.getGameFrame()));
        //地图管理器
        Resource.getMapInstance().setActiveScene(this).init();
        GameDebug gameDebug = new GameDebug(this);
        findComponent("input").addItem(gameDebug);
        drawManager.addItem(gameDebug);
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        EventRegister eventRegister = SingletonManager.getEventRegister();
        IGameEvent releaseToPool = eventRegister.getEvent("releaseToPool");
        IGameEvent animationInit = eventRegister.getEvent("animationInit");
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //对象池分配资源
        //要是能用宏就好了，省一堆操作
        //在onDestroy下归还资源
        resourceHandler.initPoolByName("bullets", new IPoolCreate() {
                    @Override
                    public GameItem createItem() {
                        Bullet bullet = new Bullet(0, 0, Direction.UP, "");
                        bullet.setPoolName("bullets");
                        return bullet.addEvent(GameItem.ON_DESTROY, releaseToPool);
                    }
                }
                , 100);
        resourceHandler.initPoolByName("enemies", new IPoolCreate() {
            @Override
            public GameItem createItem() {
                SpriteTank spriteTank = new SpriteTank(0, 0);
                spriteTank.setPoolName("enemies");
                return spriteTank.addEvent(GameItem.ON_DESTROY, releaseToPool);
            }
        }, 50);
        resourceHandler.initPoolByName("bulletExplosions", new IPoolCreate() {
            @Override
            public GameItem createItem() {
                AnimationPlayer animationPlayer = new AnimationPlayer((Animation) resourceHandler.getResource("bulletExplosion"));
                AnimationItem animationItem = new AnimationItem(new Direction(0, 0),
                        animationPlayer);
                return animationItem
                        .setPoolName("bulletExplosions")
                        .addEvent(GameItem.ON_INIT, animationInit)
                        .addEvent(GameItem.ON_DESTROY, releaseToPool);
            }
        }, 50);
        resourceHandler.initPoolByName("bulletExplosions2", new IPoolCreate() {
            @Override
            public GameItem createItem() {
                AnimationPlayer animationPlayer = new AnimationPlayer((Animation) resourceHandler.getResource("bulletExplosion2"));
                AnimationItem animationItem = new AnimationItem(new Direction(0, 0),
                        animationPlayer);
                return animationItem
                        .setPoolName("bulletExplosions2")
                        .addEvent(GameItem.ON_INIT, animationInit)
                        .addEvent(GameItem.ON_DESTROY, releaseToPool);
            }
        }, 100);
        resourceHandler.initPoolByName("tankExplosions", new IPoolCreate() {
            @Override
            public GameItem createItem() {
                AnimationPlayer animationPlayer = new AnimationPlayer((Animation) resourceHandler.getResource("tankExplosion"));
                AnimationItem animationItem = new AnimationItem(new Direction(0, 0),
                        animationPlayer);
                return animationItem
                        .setPoolName("tankExplosions")
                        .addEvent(GameItem.ON_INIT, animationInit)
                        .addEvent(GameItem.ON_DESTROY, releaseToPool);

            }
        }, 100);
    }
}
 class Main {

    public static void main(String[] args) {
        // write your code here
        // 主游戏视图
        GameFrame frame = new myGameFrame("坦克大战", 800, 600 + 20);
    }
}
