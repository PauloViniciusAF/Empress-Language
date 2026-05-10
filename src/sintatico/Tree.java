package sintatico;

public class Tree {
    private TreeNode root;
    private TreeNode currentNode;
    private int nodeCounter;

    public Tree() {
        this.root = null;
        this.currentNode = null;
        this.nodeCounter = 0;
    }

    public TreeNode createNode(String value, TreeNode parent) {
        this.nodeCounter++;
        return new TreeNode(this.nodeCounter, value, parent);
    }

    public void addRuleNode(String ruleValue) {
        TreeNode newNode = createNode(ruleValue, this.currentNode);
        if (this.root == null) {
            this.root = newNode;
        } else {
            this.currentNode.addChild(newNode);
        }
        this.currentNode = newNode;
    }

    public void addTerminalNode(String terminalValue) {
        TreeNode newNode = createNode(terminalValue, this.currentNode);
        this.currentNode.addChild(newNode);
    }

    public void endRuleNode() {
        if (this.currentNode != null) {
            this.currentNode = this.currentNode.getParent();
        }
    }

    public void printTree() {
        if (this.root != null) {
            System.out.println("\n" + "=".repeat(50));
            System.out.println("Árvore Sintática:");
            System.out.println("=".repeat(50));
            this.root.printNode();
            System.out.println("=".repeat(50) + "\n");
        } else {
            System.out.println("Tree is empty.");
        }
    }

    // Getters
    public TreeNode getRoot() { return root; }
    public TreeNode getCurrentNode() { return currentNode; }
    public int getNodeCounter() { return nodeCounter; }
}
