package com.test.tps.common;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MultiTree<E> {

    public Node<E> root;

    public List<Node<E>> nodeList;

    public MultiTree(E e) {
        this(new Node<>(e));
    }

    public MultiTree(Node<E> root) {
        this.root = root;
        nodeList = new ArrayList<>();
        consumeForNode(nodeList::add, root);
    }

    public Optional<Node<E>> getNode(E e) {
        return matchNode(e, root);
    }

    /**
     * _______[是否匹配]
     * |         /   \
     * |        y     n
     * |       /       \
     * |   [return]  [是否有子节点]
     * |                /   \
     * |               y     n
     * |_____________ /       \
     * |               [父节点是否有兄弟节点]___
     * |                    /    \           |
     * |                   y      n          |
     * |__________________/        \_________|
     */
    private Optional<Node<E>> matchNode(E e, Node<E> currentNode) {
        if (e.equals(currentNode.element)) {
            return Optional.of(currentNode);
        } else {
            if (currentNode.children != null) {
                for (Node<E> child : currentNode.children) {
                    Optional<Node<E>> result = matchNode(e, child);
                    if (result.isPresent()) {
                        return result;
                    }
                }
            }
        }

        return Optional.empty();
    }

    /**
     * 拆分多叉树
     */
    public List<MultiTree<E>> departIf(Predicate<E> predicate) {
        List<MultiTree<E>> list = new ArrayList<>();
        list.add(this);
        for (Node<E> eNode : nodeList) {
            if (predicate.test(eNode.element)) {
                Node<E> parent = eNode.parent;
                // 断开父子节点之间的指向
                if (parent != null) {
                    parent.children.remove(eNode);
                }
                eNode.parent = null;
                list.add(new MultiTree<>(eNode));
            }
        }

        return list;
    }

    /**
     * 将点添加至指定点的子节点中
     * @param node 指定点
     * @param e    需要添加的点
     */
    public boolean add(Node<E> node, E e) {
        if (node.children == null) {
            node.children = new ArrayList<>();
        }
        Node<E> newNode = new Node<>(e);
        newNode.parent = node;
        node.children.add(newNode);
        return nodeList.add(newNode);
    }

    /**
     * 前序遍历
     */
    public void preTraversal(Consumer<E> action) {
        Objects.requireNonNull(action);
        consume(action, root);
    }

    private void consume(Consumer<E> action, Node<E> currentNode) {
        action.accept(currentNode.element);
        if (currentNode.children != null) {
            currentNode.children.forEach(o -> consume(action, o));
        }
    }

    private void consumeForNode(Consumer<Node<E>> action, Node<E> currentNode) {
        action.accept(currentNode);
        if (currentNode.children != null) {
            currentNode.children.forEach(o -> consumeForNode(action, o));
        }
    }

    public static class Node<E> {
        /**
         * 父节点
         */
        Node<E> parent;
        /**
         * 当前点的值
         */
        final E element;
        /**
         * 子节点
         */
        List<Node<E>> children;

        public Node(E element) {
            this.element = element;
        }
    }

    @Override
    public String toString() {
        List<E> list = new ArrayList<>();
        this.preTraversal(list::add);
        return list.stream().map(Object::toString).collect(Collectors.joining(",", "[", "]"));
    }
}