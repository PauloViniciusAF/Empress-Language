package sintatico;

import java.util.ArrayList;
import java.util.List;

public class TreeNode {
    private int id;
    private String value;
    private String type;  // "error", "rule", "token"
    private TreeNode parent;
    private List<TreeNode> children;
    private String enter;
    private String exit;

    public TreeNode(int id, String value, TreeNode parent) {
        this.id = id;
        this.value = value;
        this.parent = parent;
        this.children = new ArrayList<>();
        this.enter = "";
        this.exit = "";
        
        // Determina o tipo do nó
        if (value != null && value.equals("X Erro!")) {
            this.type = "error";
        } else if (value != null && value.matches("[a-zA-Z_].*")) {
            this.type = "rule";  // Regras geralmente começam com letras
        } else {
            this.type = "token";
        }
    }

    public void addChild(TreeNode childNode) {
        childNode.parent = this;
        this.children.add(childNode);
    }

    public void printNode() {
        printNode(0, true, "");
    }

    private void printNode(int level, boolean isLast, String prefix) {
        String indent;
        if (level == 0) {
            indent = prefix + (isLast ? "   " : "│  ");
        } else {
            indent = prefix + (isLast ? "└─ " : "├─ ");
        }
        
        // Colorir saída baseado no tipo
        String colored = value;
        if ("error".equals(type)) {
            colored = "\u001B[31m" + value + "\u001B[0m";  // VERMELHO
        } else if ("rule".equals(type)) {
            colored = "\u001B[34m" + value + "\u001B[0m";  // AZUL?
        }
        
        System.out.println(indent + colored);
        
        for (int i = 0; i < children.size(); i++) {
            TreeNode child = children.get(i);
            boolean childIsLast = (i == children.size() - 1);
            String newPrefix = prefix + (isLast ? "   " : "│  ");
            child.printNode(level + 1, childIsLast, newPrefix);
        }
    }

    // Getters
    public int getId() { return id; }
    public String getValue() { return value; }
    public String getType() { return type; }
    public TreeNode getParent() { return parent; }
    public List<TreeNode> getChildren() { return children; }
    
    // Setters
    public void setType(String type) { this.type = type; }
}
