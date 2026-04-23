package com.gcore.core;

import com.gcore.app.GActivityThread;
import com.gcore.utils.TrieTree;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class IOCore {
    private static final IOCore sInstance = new IOCore();
    private final Map<String, String> mRedirectMap = new HashMap<>();
    private static final TrieTree mTrieTree = new TrieTree();

    public static IOCore get() {
        return sInstance;
    }

    public void addRedirect(String target, String relocate) {
        if (target == null || relocate == null) return;
        mTrieTree.add(target);
        mRedirectMap.put(target, relocate);
        NativeCore.addIORule(target, relocate);
    }

    public File redirectPath(File file) {
        if (file == null) return null;
        return new File(redirectPath(file.getAbsolutePath()));
    }

    public String redirectPath(String path) {
        if (path == null || path.isEmpty()) return path;
        String search = mTrieTree.search(path);
        if (search != null && !search.isEmpty()) {
            String redirect = mRedirectMap.get(search);
            if (redirect != null) {
                return path.replace(search, redirect);
            }
        }
        return path;
    }
}
