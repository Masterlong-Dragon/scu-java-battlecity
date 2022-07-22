package com.masterlong.framework.components;

import com.masterlong.framework.essentials.*;
import com.masterlong.framework.math.Rigid;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基于四叉树(主要靠搜索引擎)的碰撞检测
 */
public class CollisionManagerNO extends GameComponentSystem implements IResDraw {

    private int w, h;//平面大小
    //先后存储静态和动态的物体
    protected ArrayList<ConcurrentHashMap<Long, ICollider>> collisionLayers;//可碰撞物体层
    //private LogicManager logicManager;//逻辑循环管理器
    private DrawManager drawManager;//绘制管理器
    private int layer;//绘制层
    private boolean drawn, isInitDrawManager;
    private final Wall[] walls;
    private final AABB wallContainer;
    private QuadTree quadTree;
    private ArrayList<ICollider> updateList;
    public static final String filterMode = "filterMode";

    private class Wall implements ICollider {
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
        public void bind(CollisionManagerNO c) {
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
    public CollisionManagerNO(int w, int h) {
        super();
        this.w = w;
        this.h = h;
        //初始化必要的数据结构
        collisionLayers = new ArrayList<ConcurrentHashMap<Long, ICollider>>();
        for (int i = 0; i < 2; i++)
            collisionLayers.add(new ConcurrentHashMap<Long, ICollider>());
        layer = 4;
        isInitDrawManager = drawn = false;
        //四面墙壁由四个有实体的墙组成
        //方便统一纳入刚体碰撞模拟
        wallContainer = new AABB(7, 30, w - 15, h - 38);
        walls = new Wall[4];
        walls[0] = new Wall(new AABB(wallContainer.x, wallContainer.y - 20, wallContainer.width, 20));
        walls[1] = new Wall(new AABB(wallContainer.x, wallContainer.y + wallContainer.height, wallContainer.width, 10));
        walls[2] = new Wall(new AABB(wallContainer.x - 10, wallContainer.y, 10, wallContainer.height));
        walls[3] = new Wall(new AABB(wallContainer.x + wallContainer.width, wallContainer.y, 10, wallContainer.height));
        updateList = new ArrayList<ICollider>();
    }

    @Override
    public String getComponentID() {
        return "collision";
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
        quadTree = new QuadTree(new AABB(0, 0, w, h), 0);
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
                if (collider.isRigid()) {
                    updateList.clear();
                    quadTree.checkElements(collider, collider.getFilter(), true, updateList);
                    for (ICollider updateCollider : updateList)
                        quadTree.insert(updateCollider.getAABB(), updateCollider);
                } else
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
        private AABB zone;

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
                    if(node.element.isRigid()){
                        updateList.add(collider);
                        nodes.remove(node.element);
                    }
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
