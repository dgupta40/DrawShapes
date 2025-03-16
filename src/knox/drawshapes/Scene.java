package knox.drawshapes;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;

/**
 * A scene of shapes. Uses the Model-View-Controller (MVC) design pattern,
 * though note that model knows something about the view, as the draw()
 * method both in Scene and in Shape uses the Graphics object. That's kind of
 * sloppy,
 * but it also helps keep things simple.
 * 
 * @author jspacco
 *
 */
public class Scene implements Iterable<IShape> {
    private List<IShape> shapeList = new LinkedList<IShape>();
    private SelectionRectangle selectRect;
    private boolean isDrag;
    private Point startDrag;

    // Add undo/Redo stack
    private Stack<List<IShape>> undoStack = new Stack<>();
    private Stack<List<IShape>> redoStack = new Stack<>();

    public void updateSelectRect(Point drag) {
        for (IShape s : this) {
            s.setSelected(false);
        }
        if (drag.x > startDrag.x) {
            if (drag.y > startDrag.y) {
                // top-left to bottom-right
                selectRect = new SelectionRectangle(startDrag.x, drag.x, startDrag.y, drag.y);
            } else {
                // bottom-left to top-right
                selectRect = new SelectionRectangle(startDrag.x, drag.x, drag.y, startDrag.y);
            }
        } else {
            if (drag.y > startDrag.y) {
                // top-right to bottom-left
                selectRect = new SelectionRectangle(drag.x, startDrag.x, startDrag.y, drag.y);
            } else {
                // bottom-left to top-right
                selectRect = new SelectionRectangle(drag.x, startDrag.x, drag.y, startDrag.y);
            }
        }
        List<IShape> selectedShapes = this.select(selectRect);
        for (IShape s : selectedShapes) {
            s.setSelected(true);
        }
    }

    public void stopDrag() {
        this.isDrag = false;
    }

    public void startDrag(Point p) {
        this.isDrag = true;
        this.startDrag = p;
    }

    /**
     * Draw all the shapes in the scene using the given Graphics object.
     * 
     * @param g
     */
    public void draw(Graphics g) {
        for (IShape s : shapeList) {
            if (s != null) {
                s.draw(g);
            }
        }
        if (isDrag && selectRect != null) {
            selectRect.draw(g);
        }
    }

    public Iterator<IShape> iterator() {
        return shapeList.iterator();
    }

    /**
     * Return a list of shapes that contain the given point.
     * 
     * @param point The point
     * @return A list of shapes that contain the given point.
     */
    public List<IShape> select(Point point) {
        List<IShape> selected = new LinkedList<IShape>();
        for (IShape s : shapeList) {
            if (s.contains(point)) {
                selected.add(s);
            }
        }
        return selected;
    }

    /**
     * Return a list of shapes in the scene that intersect the given shape.
     * 
     * @param s The shape
     * @return A list of shapes intersecting the given shape.
     */
    public List<IShape> select(IShape shape) {
        List<IShape> selected = new LinkedList<IShape>();
        for (IShape s : shapeList) {
            if (s.intersects(shape)) {
                selected.add(s);
            }
        }
        return selected;
    }

    /**
     * Add a shape to the scene. It will be rendered next time
     * the draw() method is invoked.
     * 
     * @param s
     */
    public void addShape(IShape s) {
        saveStateForUndo(); // it saves current state before change
        shapeList.add(s);
        redoStack.clear(); // clear redo history after new changes
    }

    /**
     * Remove a list of shapes from the given scene.
     * 
     * @param shapesToRemove
     */
    public void removeShapes(Collection<IShape> shapesToRemove) {
        shapeList.removeAll(shapesToRemove);
    }

    public void removeSelected() {
        saveStateForUndo();
        // lambdas are SO FREAKING COOL!
        shapeList.removeIf(s -> s.isSelected());
        redoStack.clear();
    }

    public String toString() {
        String shapeText = "";
        for (IShape s : shapeList) {
            shapeText += s.toString() + "\n";
        }
        return shapeText;
    }

    public void saveToFile(String filename) throws IOException {
        writeToFile(toString(), filename);
    }

    private static void writeToFile(String text, String filename)
            throws IOException {
        PrintStream out = new PrintStream(new File(filename));
        out.print(text);
        out.flush();
        out.close();
    }

    public void loadFromFile(String filename)
            throws IOException {
        shapeList.clear();
        Scanner scan = new Scanner(new FileInputStream(filename));
        while (scan.hasNext()) {
            String type = scan.next();
            if (type.startsWith("SQUARE")) {
                // SQUARE left top size color selected
                int left = scan.nextInt();
                int top = scan.nextInt();
                int size = scan.nextInt();
                Color color = Util.stringToColor(scan.next());
                boolean selected = Boolean.parseBoolean(scan.next());
                Square sq = new Square(color, left + size / 2, top + size / 2, size);
                sq.setSelected(selected);
                addShape(sq);
            } else if (type.startsWith("CIRCLE")) {
                // CIRCLE centerx centery diameter color selected
                int centerx = scan.nextInt();
                int centery = scan.nextInt();
                int diameter = scan.nextInt();
                Color color = Util.stringToColor(scan.next());
                boolean selected = Boolean.parseBoolean(scan.next());
                Circle circle = new Circle(color, new Point(centerx, centery), diameter);
                circle.setSelected(selected);
                addShape(circle);
            } else if (type.startsWith("RECTANGLE")) {
                // RECTANGLE left top width height color selected
                int left = scan.nextInt();
                int top = scan.nextInt();
                int width = scan.nextInt();
                int height = scan.nextInt();
                Color color = Util.stringToColor(scan.next());
                boolean selected = Boolean.parseBoolean(scan.next());
                Rectangle rec = new Rectangle(new Point(left, top), width, height, color);
                rec.setSelected(selected);
                addShape(rec);
            }
        }
    }

    public void scale(double d) {
        for (IShape s : shapeList) {
            if (s.isSelected())
                s.scale(d);
        }
    }

    public void moveSelected(int dx, int dy) {
        for (IShape s : shapeList) {
            if (s.isSelected())
                s.move(dx, dy);
        }
    }

    public void clear() {
        shapeList.clear();
    }

    public void undo() {
        if (!undoStack.isEmpty()) {
            redoStack.push(new ArrayList<>(shapeList)); // Save current state for Redo
            shapeList = undoStack.pop();
        }
    }

    public void redo() {
        if (!redoStack.isEmpty()) {
            undoStack.push(new ArrayList<>(shapeList)); // Save current state for Undo
            shapeList = redoStack.pop();
        }
    }

    // Helper method to save current state before making changes
    private void saveStateForUndo() {
        undoStack.push(new ArrayList<>(shapeList));
    }
}