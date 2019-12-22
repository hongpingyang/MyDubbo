package com.hong.py.registry;

import java.util.List;

public interface ChildrenListener {

    void childChanged(String path, List<String> children);
}
