package com.griddynamics.logtool;

import java.util.HashMap;
import java.util.Map;

/**
 * Basic node of storage structure. One node equals one folder in storage structure.
 */
public class Tree {
    /**
     * Children represented as map where keys are subfolder's names and values are Tree objects
     */
    private Map<String, Tree> children;

    public Tree() {
        this.children = new HashMap<String, Tree>();
    }

    public Map<String, Tree> getChildren() {
        return children;
    }

    public Tree(Tree node) {
        this.children = new HashMap<String, Tree>();
        for (String key : node.getChildren().keySet()) {
            this.children.put(key, null);
        }
    }
}
