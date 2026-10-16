package com.mna.launcher;
public final class PagePolicy {
 private PagePolicy(){}
 public static int count(int lastOccupied,int explicitlyRequested){return Math.max(1,Math.max(lastOccupied+1,explicitlyRequested));}
 public static int clamp(int target,int count){return Math.max(0,Math.min(target,Math.max(1,count)-1));}
}
