package com.gcore.utils;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
/* loaded from: classes.jar:com/gcore/utils/TrieTree.class */
public class TrieTree {
    private final TrieNode root = new TrieNode();

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.jar:com/gcore/utils/TrieTree$TrieNode.class */
    public static class TrieNode {
        char content;
        String word;
        boolean isEnd = false;
        List<TrieNode> children = new LinkedList();

        public TrieNode() {
        }

        public TrieNode(char c, String str) {
            this.content = c;
            this.word = str;
        }

        public boolean equals(Object obj) {
            boolean z = false;
            if (obj instanceof TrieNode) {
                z = false;
                if (((TrieNode) obj).content == this.content) {
                    z = true;
                }
            }
            return z;
        }

        public TrieNode nextNode(char c) {
            for (TrieNode trieNode : this.children) {
                if (trieNode.content == c) {
                    return trieNode;
                }
            }
            return null;
        }
    }

    public void add(String str) {
        TrieNode trieNode = this.root;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char charAt = str.charAt(i);
            sb.append(charAt);
            TrieNode trieNode2 = new TrieNode(charAt, sb.toString());
            if (((TrieNode) Objects.requireNonNull(trieNode)).children.contains(trieNode2)) {
                trieNode = trieNode.nextNode(charAt);
            } else {
                trieNode.children.add(trieNode2);
                trieNode = trieNode2;
            }
            if (i == str.length() - 1) {
                ((TrieNode) Objects.requireNonNull(trieNode)).isEnd = true;
            }
        }
    }

    public void addAll(List<String> list) {
        for (String str : list) {
            add(str);
        }
    }

    public String search(String str) {
        TrieNode trieNode = this.root;
        for (int i = 0; i < str.length(); i++) {
            char charAt = str.charAt(i);
            if (!trieNode.children.contains(new TrieNode(charAt, null))) {
                return null;
            }
            trieNode = trieNode.nextNode(charAt);
            if (((TrieNode) Objects.requireNonNull(trieNode)).isEnd) {
                return trieNode.word;
            }
        }
        return null;
    }
}

