package com.lu.lubottommenu.api;

import com.lu.lubottommenu.menuitem.AbstractBottomMenuItem;

/**
 * Created by 陆正威 on 2018/1/28.
 */

public interface IMultiMenu {

    void setItemSelected(long id, boolean b);

    void show(long last);

    void hide(long last);

    IMultiMenu addRootItem(AbstractBottomMenuItem abstractBottomMenuItem);

    IMultiMenu addItem(long parentId, AbstractBottomMenuItem item);
}
