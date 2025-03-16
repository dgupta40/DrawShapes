# cs220-drawshapes-sol

## New Feature Added

1. **Custom Color Palette**  
   Users can select a custom color using a color chooser (via the "Color Palette" option in the Color menu) so that shapes can be drawn in any color beyond the default presets.

2. **Delete Shape with Delete/Backspace Key**  
   Users can delete any selected shape by pressing the Delete key (or Backspace key), making it easier to remove unwanted shapes.

3. **Undo/Redo Functionality**  
   The application supports Undo and Redo actions for shape operations:
   - **Undo:** Press **Ctrl+Z** to revert the last change.
   - **Redo:** Press **Ctrl+Y** to reapply the last undone change.

4. **Image Upload via Keyboard (I Key)**  
   By pressing the **I key**, users can open a file chooser to select an image from their computer. The selected image is loaded onto the canvas as a shape (using an `ImageShape` class).

## How to Use

- **Drawing Shapes:**  
  Click on the canvas to add shapes. You can choose the type of shape (Square, Circle, or Rectangle) from the Shape menu.

- **Changing Colors:**  
  Use the Color menu to select preset colors (Red, Green, Blue) or choose "Color Palette" for a custom color selection.

- **Deleting Shapes:**  
  Select a shape and press the **Delete** or **Backspace** key to remove it.

- **Undo/Redo Actions:**  
  - Press **Ctrl+Z** to undo an action.
  - Press **Ctrl+Y** to redo an undone action.

- **Uploading an Image:**  
  Press the **I key** to open a file chooser, then select an image file. The image will appear on the canvas as a new shape.

