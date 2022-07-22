package com.masterlong.framework.components;

import java.awt.event.KeyEvent;

/**
 * 输入监听接口
 */
public interface IInput {
    void keyPressed(KeyEvent key);
    long getInputID();
    void bind(InputManager i);
}
