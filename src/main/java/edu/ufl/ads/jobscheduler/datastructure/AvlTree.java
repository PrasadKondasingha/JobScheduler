package edu.ufl.ads.jobscheduler.datastructure;

import edu.ufl.ads.jobscheduler.model.Job;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class AvlTree {
  private Node root;
  private int size;
  private int insertions;
  private int deletions;
  private int leftRotations;
  private int rightRotations;

  public Job find(int id) {
    Node node = root;
    while (node != null) {
      if (id == node.key) {
        return node.job;
      }
      node = id < node.key ? node.left : node.right;
    }
    return null;
  }

  public boolean insert(Job job) {
    Objects.requireNonNull(job, "AVL job must not be null");
    if (find(job.getId()) != null) {
      return false;
    }
    root = insert(root, job);
    size++;
    insertions++;
    return true;
  }

  private Node insert(Node node, Job job) {
    if (node == null) {
      return new Node(job);
    }
    if (job.getId() < node.key) {
      node.left = insert(node.left, job);
    } else {
      node.right = insert(node.right, job);
    }
    return rebalance(node);
  }

  public Job delete(int id) {
    Job existing = find(id);
    if (existing == null) {
      return null;
    }
    root = delete(root, id);
    size--;
    deletions++;
    return existing;
  }

  private Node delete(Node node, int id) {
    if (id < node.key) {
      node.left = delete(node.left, id);
    } else if (id > node.key) {
      node.right = delete(node.right, id);
    } else {
      return removeNode(node);
    }
    return rebalance(node);
  }

  private Node removeNode(Node node) {
    if (node.left == null) {
      return node.right;
    }
    if (node.right == null) {
      return node.left;
    }
    Node successor = minimum(node.right);
    node.key = successor.key;
    node.job = successor.job;
    return deleteSuccessor(node, successor.key);
  }

  private Node deleteSuccessor(Node node, int key) {
    node.right = delete(node.right, key);
    return rebalance(node);
  }

  private Node minimum(Node node) {
    Node current = node;
    while (current.left != null) {
      current = current.left;
    }
    return current;
  }

  private Node rebalance(Node node) {
    refreshHeight(node);
    int balance = balance(node);
    if (balance > 1 && balance(node.left) >= 0) {
      return rotateRight(node);
    }
    if (balance > 1) {
      node.left = rotateLeft(node.left);
      return rotateRight(node);
    }
    if (balance < -1 && balance(node.right) <= 0) {
      return rotateLeft(node);
    }
    if (balance < -1) {
      node.right = rotateRight(node.right);
      return rotateLeft(node);
    }
    return node;
  }

  private Node rotateRight(Node upper) {
    Node lower = upper.left;
    Node middle = lower.right;
    lower.right = upper;
    upper.left = middle;
    refreshHeight(upper);
    refreshHeight(lower);
    rightRotations++;
    return lower;
  }

  private Node rotateLeft(Node upper) {
    Node lower = upper.right;
    Node middle = lower.left;
    lower.left = upper;
    upper.right = middle;
    refreshHeight(upper);
    refreshHeight(lower);
    leftRotations++;
    return lower;
  }

  private int balance(Node node) {
    return node == null ? 0 : height(node.left) - height(node.right);
  }

  private int height(Node node) {
    return node == null ? 0 : node.height;
  }

  private void refreshHeight(Node node) {
    node.height = 1 + Math.max(height(node.left), height(node.right));
  }

  public List<Job> inorderJobs() {
    List<Job> jobs = new ArrayList<>();
    inorder(root, jobs);
    return jobs;
  }

  private void inorder(Node node, List<Job> jobs) {
    if (node == null) {
      return;
    }
    inorder(node.left, jobs);
    jobs.add(node.job);
    inorder(node.right, jobs);
  }

  public List<List<Integer>> levels() {
    List<List<Integer>> levels = new ArrayList<>();
    if (root == null) {
      return levels;
    }
    List<Node> currentLevel = new ArrayList<>();
    currentLevel.add(root);
    while (containsNode(currentLevel)) {
      levels.add(levelKeys(currentLevel));
      currentLevel = nextLevel(currentLevel);
    }
    return levels;
  }

  private boolean containsNode(List<Node> nodes) {
    return nodes.stream().anyMatch(Objects::nonNull);
  }

  private List<Integer> levelKeys(List<Node> nodes) {
    List<Integer> keys = new ArrayList<>();
    for (Node node : nodes) {
      keys.add(node == null ? null : node.key);
    }
    while (!keys.isEmpty() && keys.getLast() == null) {
      keys.removeLast();
    }
    return keys;
  }

  private List<Node> nextLevel(List<Node> nodes) {
    List<Node> children = new ArrayList<>();
    for (Node node : nodes) {
      if (node == null) {
        children.add(null);
        children.add(null);
        continue;
      }
      children.add(node.left);
      children.add(node.right);
    }
    return children;
  }

  public int size() {
    return size;
  }

  public int height() {
    return height(root);
  }

  public AvlTreeStatistics statistics() {
    return new AvlTreeStatistics(
        size, height(), insertions, deletions, leftRotations, rightRotations);
  }

  private static final class Node {
    private int key;
    private Job job;
    private int height = 1;
    private Node left;
    private Node right;

    private Node(Job job) {
      key = job.getId();
      this.job = job;
    }
  }
}
