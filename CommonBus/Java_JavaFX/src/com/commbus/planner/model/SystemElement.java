package com.commbus.planner.model;

/**
 * Created by KirinTor on 20.12.2017.
 *
 * Клас насправді не потрібен, планувався для up-касту плану-результату
 */
public abstract class SystemElement {

    public abstract String[] getTicks();

    public abstract int getFirstAvailableTick(int start, int length);

    public abstract boolean isUsed();

}
