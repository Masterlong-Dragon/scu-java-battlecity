package com.masterlong.framework.components;

import com.masterlong.framework.essentials.GameComponentSystem;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 用户输入监听
 * 目前只实现了键盘响应
 */
public class InputManager extends GameComponentSystem {

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
